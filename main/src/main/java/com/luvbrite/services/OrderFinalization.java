package com.luvbrite.services;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.luvbrite.dao.CartOrderDAO;
import com.luvbrite.dao.OrderDAO;
import com.luvbrite.utils.Exceptions;
import com.luvbrite.web.models.CartOrder;
import com.luvbrite.web.models.Order;
import com.luvbrite.web.models.OrderLineItem;
import com.luvbrite.web.models.OrderLineItemCart;
import com.luvbrite.web.models.OrderNotes;

@Service
public class OrderFinalization {
	
	private static Logger logger = LoggerFactory.getLogger(OrderFinalization.class);
	private static boolean offhourPromoActive = true;
	
	private long orderNumber = 0;	
	public long getOrderNumber() {
		return orderNumber;
	}
	
	private Order order;
	public Order getOrder(){
		return order;
	}

	@Autowired
	private OrderDAO dao;
	
	@Autowired
	private CartOrderDAO cartDAO;
	
	
	public String finalizeOrder(CartOrder cartOrder){
		String response = "";
		
		try {
			
			if(cartOrder != null 
					&& cartOrder.get_id() != 0 
					&& cartOrder.getStatus().equals("incart")
					&& cartOrder.getLineItems() != null
					&& cartOrder.getLineItems().size() > 0){
				
				//Create new Order
				order = new Order();
				
				//Check for offhour promo
				offHourPromo(cartOrder);
				
				//Copy info from cartOrder to Order
				copyCartOrder(cartOrder, order);
				
				//Get new orderId, orderNumber
				order.set_id(dao.getNextSeq());
				
				orderNumber = dao.getNextOrderNumber();				
				order.setOrderNumber(orderNumber);
				
				//Set the order status
				order.setStatus("new");
				
				//First Order Check
				firstOrderCheck();
				
				//Save the order.
				dao.save(order);
				
				//Delete or Update CartOrder
				manageCartOrder(cartOrder);
				
				response = "";
			}
			else{
				response = "Not a valid order";
			}
			
		}catch(Exception e){
			
			response = "There was some error creating the order, please try later.";
			logger.error(Exceptions.giveStackTrace(e));
		}

		return response;
	}
	
	private void copyCartOrder(CartOrder co, Order o){
		
		o.setSubTotal(co.getSubTotal());
		o.setTotal(co.getTotal());
		o.setSource(co.getSource());
		o.setDate(Calendar.getInstance().getTime());
		
		if(co.getBilling() != null) 	o.setBilling(co.getBilling());
		if(co.getShipping() != null) 	o.setShipping(co.getShipping());
		if(co.getNotes() != null) 		o.setNotes(co.getNotes());
		if(co.getCustomer() != null) 	o.setCustomer(co.getCustomer());
		

		List<OrderLineItemCart> coItems = co.getLineItems();
		List<OrderLineItemCart> newCoItems = new ArrayList<OrderLineItemCart>();
		List<OrderLineItem> items = new ArrayList<OrderLineItem>();
		
		if(coItems != null){
			
			for(OrderLineItemCart coItem : coItems){
				if(coItem.isInstock()) {
					items.add(coItem);					
				}	
				else{
					newCoItems.add(coItem);
				}
			}
			
			o.setLineItems(items);
			co.setLineItems(newCoItems);
		}
	}
	
	/**
	 * If the order is placed during offhours, add the free item
	 * to the order 
	 * */
	private void offHourPromo(CartOrder co){
		
		if(offhourPromoActive){
			
			//Check if it off hour
			Calendar now = Calendar.getInstance();
			int hour = now.get(Calendar.HOUR_OF_DAY);
			if((hour >= 23) || (hour <= 10)){
				
				//Add the new item
				OrderLineItemCart newItem = new OrderLineItemCart();
				newItem.setTaxable(false);
				newItem.setType("item");
				newItem.setName("Kiva chocolate (Offhour Promo)");
				newItem.setPromo("offhourpromo");
				newItem.setProductId(11825);
				newItem.setVariationId(0);
				newItem.setQty(1);
				newItem.setCost(10d);
				newItem.setPrice(0d);
				newItem.setImg("/uploads/2015/04/edibles-kiva-confections-large.jpg");

				List<OrderLineItemCart> olic = co.getLineItems();
				olic.add(newItem);
				co.setLineItems(olic);
				
				//System.out.println("inside");
			}
			
			//System.out.println("Time - " + hour);
		}
	}
	
	/**
	 * Check is this is customers first order and update order notes accordingly
	 **/
	private void firstOrderCheck(){
		
		Order check = dao.createQuery()
				.field("status").notEqual("cancelled")
				.field("customer._id").equal(order.getCustomer().get_id())
				.order("_id")
				.limit(1)
				.retrievedFields(true, "orderNumber")
				.get();
		
		if(check != null && check.getOrderNumber()==orderNumber){

			OrderNotes notes = order.getNotes();
			if(notes == null) notes = new OrderNotes();
			if(notes.getAdditonalNotes()==null){
				notes.setAdditonalNotes("**FIRST ORDER**");
			}
			else {
				notes.setAdditonalNotes(notes.getAdditonalNotes() + "**FIRST ORDER**");
			}
			
			order.setNotes(notes);
		}		
	};
	
	/**
	 * Delete the cart order if all the items are copied to the
	 * placed order. If any item remains, set ordertotal to zero and 
	 * keep it. 
	 * */
	private void manageCartOrder(CartOrder co){
		if(co.getLineItems()!=null && co.getLineItems().size()>0){
			co.setSubTotal(0d);
			co.setTotal(0d);
			co.getBilling().getPmtMethod().setCardData(null);
			co.setNotes(null);

			cartDAO.save(co);			
		}

		else {
			//Delete Cartorder
			cartDAO.deleteById(co.get_id());
		}
	}
	
}
