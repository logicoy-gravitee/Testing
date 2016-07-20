package com.luvbrite.services;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luvbrite.dao.LogDAO;
import com.luvbrite.utils.Exceptions;
import com.luvbrite.utils.GenericConnection;
import com.luvbrite.web.models.Address;
import com.luvbrite.web.models.AttrValue;
import com.luvbrite.web.models.Log;
import com.luvbrite.web.models.Order;
import com.luvbrite.web.models.OrderLineItem;
import com.luvbrite.web.models.ordermeta.BillingAddress;
import com.luvbrite.web.models.ordermeta.LineItem;
import com.luvbrite.web.models.ordermeta.OrderMain;
import com.luvbrite.web.models.ordermeta.itemmeta.Meta;

@Service
public class PostOrderMeta {
	
	private static Logger logger = LoggerFactory.getLogger(PostOrderMeta.class);
	
	@Autowired
	private LogDAO logDao;
	
	private final String newOrderURL = "http://www.luvbrite.com/inventory/apps/a-ordermeta?json";
	private final String updateOrderURL = "http://www.luvbrite.com/inventory/apps/a-c-ordermeta?json";
	
	public String postOrder(Order order){
		
		String response = "";
		String postURL = newOrderURL;
		
		SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		dt.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		try {
			
			if(order == null) return "Order is NULL";

			
			com.luvbrite.web.models.ordermeta.Order o = new com.luvbrite.web.models.ordermeta.Order();
			
			o.setOrder_number(Long.valueOf(order.getOrderNumber()).intValue());
			
			if(order.getNotes()!=null) {
				o.setDelivery_notes(order.getNotes().getDeliveryNotes());
				o.setNote(order.getNotes().getAdditonalNotes());
			}
			
			if(order.getStatus().equals("cancelled")){
				o.setStatus("cancelled");
				postURL = updateOrderURL;
			}
			else if(order.getStatus().equals("new")) {
				o.setStatus("new");
			}
			
			
			Date completed = order.getDate();			
			o.setCompleted_at(dt.format(completed));
			
			
			o.setTotal(order.getTotal()+"");
			o.setTotal_discount((order.getSubTotal() - order.getTotal()) + "");			
			
			Address billing = order.getShipping().getAddress();
			if(billing != null){
				BillingAddress bi = new BillingAddress();
				
				bi.setAddress_1(billing.getAddress1());
				bi.setAddress_2(billing.getAddress2());
				bi.setCity(billing.getCity());
				bi.setFirst_name(billing.getFirstName());
				bi.setLast_name(billing.getLastName());
				bi.setPhone(billing.getPhone());
				bi.setPostcode(billing.getZip());
				
				o.setBilling_address(bi);
			}
			
			List<LineItem> line_items = new ArrayList<LineItem>();
			List<OrderLineItem> oils = order.getLineItems();
			if(oils != null){
				
				for(OrderLineItem oil : oils){
					LineItem li = new LineItem();
					
					li.setName(oil.getName());
					li.setQuantity(oil.getQty());
					
					List<Meta> meta = new ArrayList<Meta>();
					
					List<AttrValue> specs = oil.getSpecs();
					if(specs != null && specs.size() !=0){
						AttrValue spec = specs.get(0);
						
						meta.add(new Meta(spec.getAttr(), spec.getAttr(), spec.getValue()));
					}
					
					li.setMeta(meta);
					
					line_items.add(li);
				}
				
			}
			
			o.setLine_items(line_items);
			
			OrderMain om = new OrderMain();
			om.setOrder(o);
			
			
			/* Convert OrderMain to JSON */
			ObjectMapper mapper = new ObjectMapper();
			String orderMainString = mapper.writeValueAsString(om); 
			
			
			/* POST TO INVENTORY SERVER */
			GenericConnection conn = new GenericConnection();
			String resp = conn.contactService(orderMainString, new URL(postURL), false);
			
			
			/* Update Log */
			try {
				
				Log log = new Log();
				log.setCollection("orders");
				log.setDetails("Meta send. " + resp);
				log.setDate(Calendar.getInstance().getTime());
				log.setKey(order.get_id());
				log.setUser("System");
				
				logDao.save(log);					
			}
			catch(Exception e){
				logger.error(Exceptions.giveStackTrace(e));
			}
			
			
			response = "success";
			
		}catch(Exception e){
			
			response = "There was some error creating the order, please try later.";
			logger.error(Exceptions.giveStackTrace(e));
		}

		return response;
	}
	
}
