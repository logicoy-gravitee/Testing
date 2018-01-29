package com.luvbrite.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.luvbrite.dao.CartOrderDAO;
import com.luvbrite.dao.OrderDAO;
import com.luvbrite.dao.ProductDAO;
import com.luvbrite.utils.Exceptions;
import com.luvbrite.utils.Utility;
import com.luvbrite.web.models.Address;
import com.luvbrite.web.models.CartOrder;
import com.luvbrite.web.models.ControlOptions;
import com.luvbrite.web.models.Order;
import com.luvbrite.web.models.OrderLineItemCart;
import com.luvbrite.web.models.OrderTax;
import com.luvbrite.web.models.Product;
import com.luvbrite.web.models.TaxComponent;

@Service
public class CartLogics {
	
	private static Logger logger = Logger.getLogger(CartLogics.class);
	
	@Autowired
	private CartOrderDAO dao;
	
	@Autowired
	private OrderDAO completedOrderdao;
	
	@Autowired
	private ProductDAO prdDao;

	
	private int doubleDownItemIndex = -1,
		briteBoxIndex = -1,
		fifthFlowerIndex = -1,
		waxPromoIndex = -1,
		fiveGPromoIndex = -1;
	
	
	private void updateIndexes(List<OrderLineItemCart> items){
		
		int index = -1;
		
		doubleDownItemIndex = -1;
		briteBoxIndex = -1;
		fifthFlowerIndex = -1;
		waxPromoIndex = -1;
		fiveGPromoIndex = -1;
		
		for(OrderLineItemCart item : items){
			
			index++;
			
			if(item.getType().equals("item") && item.isInstock()){
				
				if(item.getProductId() == 11839){
					briteBoxIndex = index;
				}
				
				else if(item.getProductId() == 11871){
					fifthFlowerIndex = index;
				}
				
				else if(item.getProductId() == 11939){
					waxPromoIndex = index;
				}
				
				else if(item.getProductId() == 11951){
					fiveGPromoIndex = index;
				}
				
				
				if( item.getPromo() != null 
						&& "doubledownoffer".equals(item.getPromo()) ){
					
					doubleDownItemIndex = index;
				}
				
			}
		}
	}
	
	public void calculateSummary(CartOrder order){
		
		try {
			
			if(order!=null){

				double subTotal = 0d, 
						total = 0d, 
						discount = 0d,
						taxApplied = 0d,
						couponDiscount = 0d;
				
				int index = -1,
						couponIndex = -1;
				
				List<OrderLineItemCart> items = order.getLineItems();
				if(items != null){
					
					for(OrderLineItemCart item : items){
						
						index++;
						
						if(item.getType().equals("item") && item.isInstock()){
							
							double itemPrice = item.getPrice();
							if(itemPrice >= 0d){
								
								double cost = item.getCost();
								int qty = item.getQty();
								
								subTotal += (cost * qty);
								total += (itemPrice * qty);	
								
								if(item.getPromo()!=null && item.getPromo().equals("p")){
									couponDiscount += ((cost - itemPrice) * qty);
								}
							}
							
						}
						
						
						if(item.getType().equals("coupon")){
							couponIndex = index;
						}
					}
					
					
					
					if(couponIndex > -1){
						
						OrderLineItemCart item = items.get(couponIndex);
						item.setPrice(couponDiscount);
					}
				}
				
				
				if(total < 0d) total = 0d;
				if(subTotal < 0d) subTotal = 0d;
				
				subTotal = Utility.Round(subTotal, 2);
				total = Utility.Round(total, 2);
				
				discount = subTotal - total;				
				if(discount > subTotal)
					discount = subTotal;
				
				//tax
				//Update 01/14/2018 - Tax calculated on subtotal. 
				double taxCalculated = 0d;
				OrderTax orderTax = order.getOrderTax();
				if(orderTax == null) orderTax = new OrderTax();
				
				List<TaxComponent> taxComponents = orderTax.getTaxComponents();
				if(taxComponents != null){
					
					for(TaxComponent tc: taxComponents){
						double thisRate = tc.getRate();
						double thisTax = Utility.Round(subTotal * thisRate/100, 2);
						
						tc.setValue(thisTax);
						
						taxCalculated+= thisTax;
					}
				}

				orderTax.setApplicableTax(taxCalculated);
				taxApplied = taxCalculated;
				total+= taxApplied;
				
				
				/**
				 * Check for rush delivery
				 **/
				double rushFee = 0d;
				if(order.getShipping() != null && order.getShipping().isRushDelivery()){
					rushFee = order.getShipping().getRushFee();
				}

				total+= rushFee;

				order.setOrderTax(orderTax);
				order.setTax(taxApplied);
				order.setSubTotal(subTotal);
				order.setTotal(total);
			}
			
			
		} catch (Exception e){
			logger.error(Exceptions.giveStackTrace(e));
		}		
		
	}
	
	
	
	public Map<String, Boolean> availableDeals(CartOrder order){
		
		Map<String, Boolean> dealStat = new HashMap<>();
		
		try {
			
			boolean firstTimepatient = false;
			
			
			/**
			 * Run the order through eligibility check
			 * */
			
			//First BriteBoxcheck
			if(order.getCustomer() != null && 
					order.getCustomer().get_id() != 0){
				
				if(firstOrderCheck(order.getCustomer().get_id())
						.equalsIgnoreCase("Y")){
					firstTimepatient = true;					
				}
			}			
			dealStat.put("firstTimepatient", firstTimepatient);
			
			
		} catch (Exception e){
			logger.error(Exceptions.giveStackTrace(e));
		}	
		
		return dealStat;
	}
	
	public void applyDeals(CartOrder order, ControlOptions cOps, boolean saveOrder, HttpSession sess, CouponManager couponManager){
		applyDeals(order, cOps, saveOrder, sess);
	}
	
	public void applyDeals(CartOrder order, ControlOptions cOps, boolean saveOrder, HttpSession sess){
		
		try {

			
			if(order == null) return;
			
			double doubleDownThresholdAmt = cOps.getDoubleDown(),
					doubleDownOfferAmt = cOps.getDoubleDownOfferValue(),
					total = 0d;
			
			boolean orderChanged = false,
					couponPresent = false,
					waxPromoActive = false,
					waxPromoEligible = false,
					fiveGPromoActive = false,
					fiveGPromoEligible = false;
			
			
			List<Product> fivegProducts = new ArrayList<>();
			if(fiveGPromoActive){
				fivegProducts = prdDao.createQuery()
						.field("categories").equal("Exclusive")
						.retrievedFields(true, "_id")
						.asList();
			}
			
			//String couponCode = "";
			
			/**
			 * Any coupons applied will be removed if doubledown or britebox is applied.
			 * That done by calling remove coupon before applying either of it.
			 * 
			 * If a coupon is applied to an existing order which has doubledown,
			 * coupon takes precedence.
			 * 
			 * Option to apply coupon or double down is disabled for order with britebox.
			 * 
			 * If an order has coupon and then later qualified for britebox, then coupon has
			 * to be removed and britebox applied (provided autoAddBriteBox is true).
			 * 
			 * */
		

			/*Find the total to see if the item is eligible*/
			List<OrderLineItemCart> items = order.getLineItems();
			if(items != null){
				for(OrderLineItemCart item : items){
					
					if( item.getPromo() != null 
							&& "p".equals(item.getPromo()) ){
						
						couponPresent = true;
					}
					
					
					if(item.getType().equals("item") && item.isInstock()){
						
						double itemPrice = item.getPrice();
						
						if(item.getProductId() == 11889 || 
								item.getProductId() == 11881){
							waxPromoEligible = true;
						}
						
						if( item.getPromo() != null 
								&& "doubledownoffer".equals(item.getPromo()) ){
							
							itemPrice = item.getCost();
						}
						
						if(itemPrice > 0d){
							total += (itemPrice*item.getQty());								
						}
						
						if(!fivegProducts.isEmpty()){
							for(Product p : fivegProducts){
								if(p.get_id() == item.getProductId()){
									fiveGPromoEligible = true;
								}
							}
						}
					}
//					
//					else if(item.getType().equals("coupon")){
//						couponCode = item.getName();
//					}
				}
				
				updateIndexes(items);
			}

			
				
			//First BriteBoxcheck	
			
			
			//If briteBox is present remove it
			if(briteBoxIndex != -1){
				
				items.remove(briteBoxIndex);
				
				orderChanged = true;
				
				//Now since the item is remove all the indices need to be updated
				updateIndexes(items);	
			}
			
			
			
			
			//Here we are checking if the double down promo present is still valid			
			if( doubleDownItemIndex != -1 				&&
				doubleDownThresholdAmt > 0d 			&&  
				doubleDownOfferAmt > 0d  				&&
				!couponPresent 							&&  
				briteBoxIndex == -1						&& 
				fifthFlowerIndex == -1					&&  
				total >= doubleDownThresholdAmt) {				

				double itemCost = 0d;				
				
				if(doubleDownItemIndex > -1){
					
					OrderLineItemCart item = items.get(doubleDownItemIndex);
					itemCost = item.getCost();

					/*Check to see if the order qualifies for doubledown even if  
					 *this item (1 qty) is removed */
					if((total - itemCost) >= doubleDownThresholdAmt){
						/*It qualifies*/
					
						if(itemCost < doubleDownOfferAmt)
							doubleDownOfferAmt = itemCost;
						
						double newPrice = itemCost - ( doubleDownOfferAmt/item.getQty() );
						
						if(newPrice != item.getPrice()){
							
							item.setPrice(newPrice);
							item.setPromo("doubledownoffer");	
							
							orderChanged = true;
						}
						/* If there is no change in the price, no need to update the order! */
					}
					
					/* If item doesn't qualify */
					else {
						
						item.setPrice(itemCost);
						item.setPromo("");	
						
						orderChanged = true;
					}
				}				
			}
			
			
			/**
			 * Wax Promo 
			 **/
			//If waxpromo eligible and not applied, apply it.
			if(waxPromoActive && 
					waxPromoEligible && 
					waxPromoIndex == -1){
				
				//Add new item
				OrderLineItemCart newItem = new OrderLineItemCart();
				newItem.setTaxable(false);
				newItem.setInstock(true);
				newItem.setType("item");
				newItem.setName("Glass Globe Atomizer (Promo)");
				newItem.setPromo("waxpromo");
				newItem.setProductId(11939);
				newItem.setVariationId(0);
				newItem.setQty(1);
				newItem.setCost(5d);
				newItem.setPrice(0d);
				newItem.setImg("/products/GlassGlobe1.jpg");

				items.add(newItem);

				orderChanged = true;
			}

			
			//If promo is not active/eligible, but applied, remove it
			if( waxPromoIndex > -1 && 
					(!waxPromoActive  || !waxPromoEligible)){
				
				//Removed Item
				List<OrderLineItemCart> olic = order.getLineItems();
				olic.remove(waxPromoIndex);
				
				orderChanged = true;	
				
				//Now since the item is remove all the indices need to be updated
				updateIndexes(items);
			}
			
			
			
			
			/**
			 * 5g Promo 
			 **/
			//If 5gpromo eligible and not applied, apply it.
			if(fiveGPromoActive && 
					fiveGPromoEligible){
				
				
				if(fiveGPromoIndex == -1){
					
					//Add new item
					OrderLineItemCart newItem = new OrderLineItemCart();
					newItem.setTaxable(false);
					newItem.setInstock(true);
					newItem.setType("item");
					newItem.setName("Power Puff Roll Promo ");
					newItem.setPromo("freepowerpuff");
					newItem.setProductId(11951);
					newItem.setVariationId(0);
					newItem.setQty(1);
					newItem.setCost(10d);
					newItem.setPrice(0d);
					newItem.setImg("/products/PowerPuff.jpg");

					items.add(newItem);

					orderChanged = true;
				}
				else {
					
					//Check if the product has valid promo details (Some other logic might change the promo details)
					
					OrderLineItemCart promoItem = items.get(fiveGPromoIndex);
					if(promoItem.getQty() != 1 || 
							promoItem.getCost() != 10d ||
							promoItem.getPrice() != 0){
						
						promoItem.setQty(1);
						promoItem.setCost(10d);
						promoItem.setPrice(0d);
						promoItem.setName("Power Puff Roll Promo ");
						promoItem.setPromo("freepowerpuff");

						orderChanged = true;
					}
				}
			}

			
			//If promo is not active/eligible, but applied, remove it
			if( fiveGPromoIndex > -1 && 
					(!fiveGPromoActive  || !fiveGPromoEligible)){
				
				//Removed Item
				List<OrderLineItemCart> olic = order.getLineItems();
				olic.remove(fiveGPromoIndex);
				
				orderChanged = true;	
				
				//Now since the item is remove all the indices need to be updated
				updateIndexes(items);
			}
			
			
			
			
			//If doubledown is currently present, but order doesn't qualify anymore, then remove doubledown.
			// OR
			//Rare case where doubledown and britebox are present, remove doubledown.
			// OR
			//Case where doubledown and coupon are present, remove doubledown.
			if(doubleDownItemIndex != -1 && 
					(total < doubleDownThresholdAmt || doubleDownThresholdAmt == 0 
					|| briteBoxIndex != -1
					|| couponPresent)){
				
				OrderLineItemCart item = items.get(doubleDownItemIndex);
				
				item.setPrice(item.getCost());
				item.setPromo("");	
				
				orderChanged = true;				
			}
			
			
			if(saveOrder && orderChanged){
				
				/* Calculate order total */
				calculateSummary(order);
				
				/* Save Order*/
				dao.save(order);
			}
			
			
		} catch (Exception e){
			logger.error(Exceptions.giveStackTrace(e));
		}
		
		
	}
	
	//Check if this is customer's first order, ignores any previous cancelled orders.
	public String firstOrderCheck(long customerId){
		return firstOrderCheckLogic(customerId, false);
	}
	

	//Check if this is customer's first order including any previous cancelled orders.
	public String theFirstOrder(long customerId){	
		return firstOrderCheckLogic(customerId, true);
	}
	
	
	private String firstOrderCheckLogic(long customerId, boolean irrespectiveOfCancelledOrder){
		
		String response = "";
		
		Query<Order> q = completedOrderdao.createQuery()
				.field("customer._id").equal(customerId);
		
		if(!irrespectiveOfCancelledOrder){
			q.field("status").notEqual("cancelled");
		}
		
		if(completedOrderdao.count(q) == 0){
			response = "Y";
		}
		
		return response;
	}
	
	public Address getPrevShippingInfo(long customerId){
		
		Address a = null;
		Query<Order> q = completedOrderdao.createQuery()
				.field("customer._id").equal(customerId)
				.order("-orderNumber").limit(1);
		
		Order o = q.get();
		if(o != null && 
				o.getShipping() != null && 
				o.getShipping().getAddress() != null){
			
			a = o.getShipping().getAddress();
		}		
		
		return a;		
	}
}
