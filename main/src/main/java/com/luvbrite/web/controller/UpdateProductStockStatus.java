package com.luvbrite.web.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luvbrite.apis.APIs;
import com.luvbrite.dao.ProductDAO;
import com.luvbrite.services.TrackingLinkToCustomer;
import com.luvbrite.utils.CustomGenericConnection;
import com.luvbrite.web.models.Product;
import com.models.web.tookan.*;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(value = "/updateproductStockStatus")
public class UpdateProductStockStatus {

	private static Logger logger = Logger.getLogger(UpdateProductStockStatus.class);

	APIs apisContants = new APIs();
	// private static final String postTookanNotification =
	// "http://localhost:8080/inventory/apps/getTookanNotification?json";
	private final String postTookanNotification = apisContants.POST_TOOKAN_NOTICATION;

	@Autowired
	ProductDAO prdDao;

	@RequestMapping(value = "/updateProds", method = RequestMethod.POST)
	public ResponseEntity<String> updateProductStockStatus(@RequestBody String statusAndMongoID) {
		logger.debug("raw String recieved-->" + statusAndMongoID);
		System.out.println("raw String recieved-->" + statusAndMongoID);
		try {
			statusAndMongoID = URLDecoder.decode(statusAndMongoID, StandardCharsets.UTF_8.toString());
			logger.debug("After Decoding String recieved-->" + statusAndMongoID);

		} catch (UnsupportedEncodingException e) {
			logger.error("Exception while Decoding data recieved from Inventory");
			e.printStackTrace();
		}

		String[] statusAndid = statusAndMongoID.split("~");
		String stockStatus = statusAndid[0];
		Long mongoId = Long.parseLong(statusAndid[1]);

		Product productDb = prdDao.get(mongoId);

		if (productDb != null) {
			productDb.setStockStat(stockStatus);
			prdDao.save(productDb);
			return new ResponseEntity<String>("SUCCESS", HttpStatus.OK);
		} else {
			logger.debug("Can not find mongo productid " + mongoId);
			return new ResponseEntity<String>("Can not find mongo productid " + mongoId, HttpStatus.OK);
		}

	}

	@RequestMapping(value = "/test", method = RequestMethod.POST)
	public ResponseEntity<String> getTookanNotification(@RequestBody String tookanNotification) {

//		String tookanNotification1 = "{\"job_id\":50880172,\"created_by\":5,\"order_id\":\"31615\",\"recurring_id\":\"0\",\"recurring_count\":0,\"partner_order_id\":null,\"team_id\":228977,\"vertical\":0,\"merchant_id\":0,\"geofence\":0,\"tags\":\"\",\"auto_assignment\":0,\"dispatcher_id\":null,\"job_hash\":\"71f3f76aeba984899f970db115685051\",\"has_pickup\":0,\"has_delivery\":1,\"is_routed\":0,\"pickup_delivery_relationship\":\"5088017215707856596958498\",\"job_description\":\"delivering cannabis\",\"job_pickup_datetime\":\"Invalid date\",\"job_pickup_name\":\"\",\"job_pickup_phone\":\"0\",\"job_delivery_datetime\":\"10/17/2019 02:50 pm\",\"job_pickup_latitude\":\"\",\"job_pickup_longitude\":\"\",\"job_pickup_address\":\"\",\"job_pickup_email\":null,\"job_latitude\":\"34.0348428\",\"job_longitude\":\"-118.4522523\",\"customer_username\":\"Day2Day Printing\",\"customer_email\":\"admin@day2dayprinting.com\",\"customer_phone\":\"+1 310-996-6789\",\"job_address\":\"2030 S Westgate Ave , Los Angeles - 90292\",\"creation_datetime\":\"2019-10-11T09:20:59.000Z\",\"fleet_id\":419408,\"user_id\":219320,\"fleet_rating\":5,\"customer_comment\":null,\"is_customer_rated\":0,\"customer_id\":14930727,\"arrived_datetime\":\"2019-10-11 14:57:35\",\"started_datetime\":\"2019-10-11 14:57:2\",\"completed_datetime\":\"2019-10-11 14:57:35\",\"acknowledged_datetime\":\"2019-10-11 14:55:41\",\"job_status\":2,\"is_active\":1,\"job_type\":1,\"completed_by_admin\":0,\"open_tracking_link\":0,\"timezone\":\"-330\",\"job_time\":\"2019-10-17 02:50:0\",\"job_date\":\"2019-10-17T00:00:00.000Z\",\"job_time_utc\":\"2019-10-17T09:20:00.000Z\",\"job_date_utc\":\"2019-10-17T00:00:00.000Z\",\"total_distance_travelled\":0,\"form_id\":null,\"customer_rating\":5,\"driver_comment\":null,\"remarks\":null,\"barcode\":null,\"ride_type\":0,\"matched_pickup_delivery_relationship\":null,\"job_vertical\":0,\"days_started\":\"0\",\"tookan_shared_secret\":\"ZraU2QLFxyZZ3YrG\",\"distance_in\":\"KM\",\"access_token\":\"13a76798c468b7571eb7d2a0c66267cc\",\"domain\":null,\"agent_workflow\":0,\"fleet_name\":\"Hemraj Shaqawal\",\"fleet_email\":\"hemraj.shaqawal@logicoy.com\",\"fleet_phone\":\"+91 89559 09361\",\"fleet_latitude\":\"13.0057533\",\"fleet_longitude\":\"77.650761\",\"transport_type\":2,\"license\":\"\",\"transport_desc\":\"\",\"fleet_image\":\"app/img/user.png\",\"job_details_by_fleet\":419408,\"external_fleet_id\":\"\",\"fleet_vehicle_type\":2,\"fleet_vehicle_color\":\"\",\"fleet_vehicle_description\":\"\",\"task_status\":2,\"promo_used\":\"\",\"custom_fields\":[{\"label\":\"Products\",\"display_name\":\"Products\",\"data_type\":\"Text\",\"app_side\":\"1\",\"required\":1,\"value\":1,\"data\":\"AD Moxie Shatter - Indica,Apex x Cali Kosher | Cannabis Cup Winner | 1g Live Resin Sauce| Papaya,Apex x SOG | Mendo Breath\",\"input\":\"\",\"before_start\":0,\"template_id\":\"Cannabis_Delivery\"},{\"label\":\"Quantity\",\"display_name\":\"Quantity\",\"data_type\":\"Text\",\"app_side\":\"1\",\"required\":1,\"value\":1,\"data\":\"'1','1','1'\",\"input\":\"\",\"before_start\":0,\"template_id\":\"Cannabis_Delivery\"},{\"label\":\"TotalTax\",\"display_name\":\"TotalTax\",\"data_type\":\"Text\",\"app_side\":\"1\",\"required\":1,\"value\":1,\"data\":\"30\",\"input\":\"\",\"before_start\":0,\"template_id\":\"Cannabis_Delivery\"},{\"label\":\"Total\",\"display_name\":\"Total\",\"data_type\":\"Text\",\"app_side\":\"1\",\"required\":0,\"value\":1,\"data\":\"145\",\"input\":\"\",\"before_start\":0,\"template_id\":\"Cannabis_Delivery\"}],\"ref_images\":[\"http://tookanapp.com/wp-content/uploads/2015/11/logo_dark.png\",\"http://tookanapp.com/wp-content/uploads/2015/11/logo_dark.png\"],\"tracking_link\":\"https://app.tookanapp.com/tracking/index.html?jobID=71f3f76aeba984899f970db115685051\",\"task_history\":[{\"id\":316685318,\"job_id\":50880172,\"fleet_id\":null,\"fleet_name\":null,\"latitude\":null,\"longitude\":null,\"type\":\"state_changed\",\"description\":\"Created By Ali-219320\",\"extra_fields\":null,\"creation_datetime\":\"2019-10-11T09:20:59.000Z\",\"creation_date\":\"2019-10-11T00:00:00.000Z\",\"label_description\":\"<span class='created'>CREATED</span>By Ali-219320\"},{\"id\":316687073,\"job_id\":50880172,\"fleet_id\":419408,\"fleet_name\":\"Hemraj Shaqawal\",\"latitude\":\"13.0056663\",\"longitude\":\"77.650999\",\"type\":\"state_changed\",\"description\":\"Assigned Hemraj Shaqawal to task - 219320\",\"extra_fields\":null,\"creation_datetime\":\"2019-10-11T09:22:41.000Z\",\"creation_date\":\"2019-10-11T00:00:00.000Z\",\"label_description\":\"<span class='assigned'>ASSIGNED</span>Hemraj Shaqawal to task - 219320\"},{\"id\":316690113,\"job_id\":50880172,\"fleet_id\":419408,\"fleet_name\":\"Hemraj Shaqawal\",\"latitude\":\"13.0057533\",\"longitude\":\"77.650761\",\"type\":\"state_changed\",\"description\":\"Accepted at\",\"extra_fields\":null,\"creation_datetime\":\"2019-10-11T09:25:41.000Z\",\"creation_date\":\"2019-10-11T00:00:00.000Z\",\"label_description\":\"<span class='accepted'>ACCEPTED</span>at\"},{\"id\":316691490,\"job_id\":50880172,\"fleet_id\":419408,\"fleet_name\":\"Hemraj Shaqawal\",\"latitude\":\"13.0057533\",\"longitude\":\"77.650761\",\"type\":\"state_changed\",\"description\":\"Started at\",\"extra_fields\":null,\"creation_datetime\":\"2019-10-11T09:27:02.000Z\",\"creation_date\":\"2019-10-11T00:00:00.000Z\",\"label_description\":\"<span class='started'>STARTED</span>at\"},{\"id\":316691857,\"job_id\":50880172,\"fleet_id\":419408,\"fleet_name\":\"Hemraj Shaqawal\",\"latitude\":\"13.0057533\",\"longitude\":\"77.650761\",\"type\":\"signature_image_added\",\"description\":\"https://tookan.s3.amazonaws.com/acknowledgement_images/WnVn1570786039075-tasksignature.png\",\"extra_fields\":\"\",\"creation_datetime\":\"2019-10-11T09:27:19.000Z\",\"creation_date\":\"2019-10-11T00:00:00.000Z\",\"label_description\":\"<span class='uploaded'>UPLOADED</span>by Hemraj Shaqawal\"},{\"id\":316692148,\"job_id\":50880172,\"fleet_id\":419408,\"fleet_name\":\"Hemraj Shaqawal\",\"latitude\":\"13.0057533\",\"longitude\":\"77.650761\",\"type\":\"state_changed\",\"description\":\"Successful at\",\"extra_fields\":null,\"creation_datetime\":\"2019-10-11T09:27:35.000Z\",\"creation_date\":\"2019-10-11T00:00:00.000Z\",\"label_description\":\"<span class='successful'>SUCCESSFUL</span>at\"}],\"job_state\":\"Successful\",\"task_state\":\"SUCCESSFUL\",\"completed_datetime_formatted\":\"11 Oct 2019 02:57 pm\",\"started_datetime_formatted\":\"11 Oct 2019 02:57 pm\",\"arrived_datetime_formatted\":\"11 Oct 2019 02:57 pm\",\"acknowledged_datetime_formatted\":\"11 Oct 2019 02:55 pm\",\"job_token\":\"5088017215707856596958498\",\"job_time_formatted\":\"17 Oct 2019 02:50 pm\",\"timestamp_diff\":33000,\"total_time_spent_at_task_till_completion\":\"33 seconds\",\"job_pickup_datetime_formatted\":\"Invalid date\",\"job_delivery_datetime_formatted\":\"17 Oct 2019 02:50 pm\",\"total_distance\":\"0.00 Km\",\"webhook_type\":0,\"format\":\"application/json\",\"template_key\":\"SUCCESSFUL\",\"is_internal\":0,\"full_tracking_link\":\"https://app.tookanapp.com/tracking/index.html?jobID=71f3f76aeba984899f970db115685051\"}";

		System.out.println("tookanNotification:--->" + tookanNotification);

		TookanWebhookResponse tookanResponse = createTookanResponseObject(tookanNotification);

		if ("STARTED".equals(tookanResponse.getTask_state().trim())) {

			TrackingLinkEmailInfo trackLnkEmailInfoObj = new TrackingLinkEmailInfo();
			trackLnkEmailInfoObj.setOrderDate(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
			trackLnkEmailInfoObj.setOrderNumber(tookanResponse.getOrder_id());
			// trackLnkEmailInfoObj.setOrderTotal(getOrderTotal(tookanResponse) + " (inc. of
			// all taxes)");
			trackLnkEmailInfoObj.setTrackingLink(tookanResponse.getFull_tracking_link());
			trackLnkEmailInfoObj.setRecipentEmail(tookanResponse.getCustomer_email());
			trackLnkEmailInfoObj.setRecipentName(tookanResponse.getCustomer_username());
			new TrackingLinkToCustomer().send(trackLnkEmailInfoObj, tookanResponse.getCustomer_email());

		}

		/* POST TO INVENTORY SERVER */
		CustomGenericConnection conn = new CustomGenericConnection();
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", "text/plain");
		String resp = "";
		try {
			resp = conn.contactService(tookanNotification, new URL(postTookanNotification), false, headers);
		} catch (Exception ex) {
			logger.error("Exception occured while sending tookanNotification to  inventory", ex);
		}

		return new ResponseEntity<String>(resp, HttpStatus.OK);

	}

	private static TookanWebhookResponse createTookanResponseObject(String tookanResponse) {
		ObjectMapper mapper = null;
		TookanWebhookResponse responseobj = null;

		try {
			mapper = new ObjectMapper();
			mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
			responseobj = mapper.readValue(tookanResponse, TookanWebhookResponse.class);

		} catch (Exception e) {
			logger.error("Exception occured while converting TookanResponse to Object", e);
			return null;

		}
		return responseobj;
	}

	public static void main(String[] args) {
		String tookanNotification1 = "{\"job_id\":63352682,\"created_by\":5,\"order_id\":\"34861\",\"recurring_id\":\"0\",\"recurring_count\":0,\"partner_order_id\":null,\"team_id\":228977,\"vertical\":0,\"merchant_id\":0,\"geofence\":0,\"tags\":\"\",\"auto_assignment\":0,\"dispatcher_id\":null,\"job_hash\":\"248c57d7fb61cec6a8eaecc34fdfaf25\",\"has_pickup\":0,\"has_delivery\":1,\"is_routed\":0,\"pickup_delivery_relationship\":\"6335268215768304346193017\",\"job_description\":\"delivering cannabis\",\"job_pickup_datetime\":\"Invalid date\",\"job_pickup_name\":\"\",\"job_pickup_phone\":\"0\",\"job_delivery_datetime\":\"12/25/2019 01:57 pm\",\"job_pickup_latitude\":\"\",\"job_pickup_longitude\":\"\",\"job_pickup_address\":\"\",\"job_pickup_email\":null,\"job_latitude\":\"34.0348428\",\"job_longitude\":\"-118.4522523\",\"customer_username\":\"Hargun Singh\",\"customer_email\":\"hargunsingh12@gmail.com\",\"customer_phone\":\"+1 234-567-8901\",\"job_address\":\"2030 S Westgate Ave , Los Angeles - 90025\",\"creation_datetime\":\"2019-12-20T08:27:14.000Z\",\"fleet_id\":419408,\"user_id\":219320,\"fleet_rating\":5,\"customer_comment\":null,\"is_customer_rated\":0,\"customer_id\":18814104,\"arrived_datetime\":\"\",\"started_datetime\":\"2019-12-20 13:58:17\",\"completed_datetime\":\"\",\"acknowledged_datetime\":\"2019-12-20 13:58:8\",\"job_status\":1,\"is_active\":1,\"job_type\":1,\"completed_by_admin\":0,\"open_tracking_link\":0,\"timezone\":\"-330\",\"job_time\":\"2019-12-25 01:57:0\",\"job_date\":\"2019-12-25T00:00:00.000Z\",\"job_time_utc\":\"2019-12-25T08:27:00.000Z\",\"job_date_utc\":\"2019-12-25T00:00:00.000Z\",\"total_distance_travelled\":0,\"form_id\":null,\"customer_rating\":5,\"driver_comment\":null,\"remarks\":null,\"barcode\":null,\"ride_type\":0,\"matched_pickup_delivery_relationship\":null,\"job_vertical\":0,\"days_started\":\"0\",\"tookan_shared_secret\":\"ZraU2QLFxyZZ3YrG\",\"distance_in\":\"MILE\",\"access_token\":\"13a76798c468b7571eb7d2a0c66267cc\",\"domain\":\"https://delivery.luvbrite.com\",\"agent_workflow\":0,\"fleet_name\":\"Hemraj Shaqawal\",\"fleet_email\":\"hemraj.shaqawal@logicoy.com\",\"fleet_phone\":\"+91 89559 09361\",\"fleet_latitude\":\"13.0050785\",\"fleet_longitude\":\"77.6538061\",\"transport_type\":2,\"license\":\"\",\"transport_desc\":\"\",\"fleet_image\":\"app/img/user.png\",\"job_details_by_fleet\":419408,\"external_fleet_id\":\"\",\"fleet_vehicle_type\":2,\"fleet_vehicle_color\":\"\",\"fleet_vehicle_description\":\"\",\"task_status\":1,\"promo_used\":\"\",\"custom_fields\":[{\"label\":\"Products\",\"display_name\":\"Products\",\"data_type\":\"Text\",\"app_side\":\"0\",\"required\":0,\"value\":1,\"data\":\"Pacific Stone | 3.5g | Lemon Sativa\",\"input\":\"\",\"before_start\":0,\"template_id\":\"Cannabis_Delivery\"},{\"label\":\"Quantity\",\"display_name\":\"Quantity\",\"data_type\":\"Text\",\"app_side\":\"0\",\"required\":0,\"value\":1,\"data\":\"1\",\"input\":\"\",\"before_start\":0,\"template_id\":\"Cannabis_Delivery\"},{\"label\":\"SubTotal\",\"display_name\":\"SubTotal\",\"data_type\":\"Text\",\"app_side\":\"0\",\"required\":0,\"value\":1,\"data\":\"15$\",\"input\":\"\",\"before_start\":0,\"template_id\":\"Cannabis_Delivery\"},{\"label\":\"TotalTax\",\"display_name\":\"TotalTax\",\"data_type\":\"Text\",\"app_side\":\"0\",\"required\":0,\"value\":1,\"data\":\"1.42 $\",\"input\":\"\",\"before_start\":0,\"template_id\":\"Cannabis_Delivery\"},{\"label\":\"Total\",\"display_name\":\"Total\",\"data_type\":\"Text\",\"app_side\":\"0\",\"required\":0,\"value\":1,\"data\":\"16.42 $\",\"input\":\"\",\"before_start\":0,\"template_id\":\"Cannabis_Delivery\"}],\"ref_images\":[\"https://lbrit.co/static/imgs/luvbrite-header-logo-s.png\",\"https://lbrit.co/static/imgs/luvbrite-header-logo-s.png\"],\"tracking_link\":\"https://jngl.ml/cc34ZSfdf\",\"task_history\":[{\"id\":399561900,\"job_id\":63352682,\"fleet_id\":null,\"fleet_name\":null,\"latitude\":null,\"longitude\":null,\"type\":\"state_changed\",\"description\":\"Created By Ali G.-219320\",\"extra_fields\":null,\"creation_datetime\":\"2019-12-20T08:27:14.000Z\",\"creation_date\":\"2019-12-20T00:00:00.000Z\",\"label_description\":\"<span class='created'>CREATED</span>By Ali G.-219320\"},{\"id\":399562520,\"job_id\":63352682,\"fleet_id\":419408,\"fleet_name\":\"Hemraj Shaqawal\",\"latitude\":\"13.0050785\",\"longitude\":\"77.6538061\",\"type\":\"state_changed\",\"description\":\"Assigned Hemraj Shaqawal to task - 219320\",\"extra_fields\":null,\"creation_datetime\":\"2019-12-20T08:27:49.000Z\",\"creation_date\":\"2019-12-20T00:00:00.000Z\",\"label_description\":\"<span class='assigned'>ASSIGNED</span>Hemraj Shaqawal to task - 219320\"},{\"id\":399562883,\"job_id\":63352682,\"fleet_id\":419408,\"fleet_name\":\"Hemraj Shaqawal\",\"latitude\":\"13.0050785\",\"longitude\":\"77.6538061\",\"type\":\"state_changed\",\"description\":\"Accepted at\",\"extra_fields\":null,\"creation_datetime\":\"2019-12-20T08:28:08.000Z\",\"creation_date\":\"2019-12-20T00:00:00.000Z\",\"label_description\":\"<span class='accepted'>ACCEPTED</span>at\"},{\"id\":399563002,\"job_id\":63352682,\"fleet_id\":419408,\"fleet_name\":\"Hemraj Shaqawal\",\"latitude\":\"13.0050785\",\"longitude\":\"77.6538061\",\"type\":\"state_changed\",\"description\":\"Started at\",\"extra_fields\":null,\"creation_datetime\":\"2019-12-20T08:28:17.000Z\",\"creation_date\":\"2019-12-20T00:00:00.000Z\",\"label_description\":\"<span class='started'>STARTED</span>at\"}],\"job_state\":\"Started\",\"task_state\":\"STARTED\",\"started_datetime_formatted\":\"20 Dec 2019 01:58 pm\",\"acknowledged_datetime_formatted\":\"20 Dec 2019 01:58 pm\",\"job_token\":\"6335268215768304346193017\",\"job_time_formatted\":\"25 Dec 2019 01:57 pm\",\"job_pickup_datetime_formatted\":\"Invalid date\",\"job_delivery_datetime_formatted\":\"25 Dec 2019 01:57 pm\",\"total_distance\":\"0.00 Mile\",\"webhook_type\":0,\"format\":\"application/json\",\"template_key\":\"AGENT_STARTED\",\"is_internal\":0,\"full_tracking_link\":\"https://delivery.luvbrite.com/tracking/index.html?jobID=248c57d7fb61cec6a8eaecc34fdfaf25\"}";

		// System.out.println(webhookresponse.replaceAll("\\", "").replaceAll("\\s*",
		// ""));

		System.out.println("tookanNotification:--->" + tookanNotification1);

		TookanWebhookResponse tookanResponse = createTookanResponseObject(tookanNotification1);

		if ("STARTED".equals(tookanResponse.getTask_state().trim())) {

			TrackingLinkEmailInfo trackLnkEmailInfoObj = new TrackingLinkEmailInfo();
			trackLnkEmailInfoObj.setOrderDate(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
			trackLnkEmailInfoObj.setOrderNumber(tookanResponse.getOrder_id());
			 trackLnkEmailInfoObj.setOrderTotal(getOrderTotal(tookanResponse) + " (inc. of all taxes)");
			trackLnkEmailInfoObj.setTrackingLink(tookanResponse.getFull_tracking_link());
			trackLnkEmailInfoObj.setRecipentEmail(tookanResponse.getCustomer_email());
			trackLnkEmailInfoObj.setRecipentName(tookanResponse.getCustomer_username());
			new TrackingLinkToCustomer().send(trackLnkEmailInfoObj, tookanResponse.getCustomer_email());
		}

	}

	public static String getOrderTotal(TookanWebhookResponse tookanResponse) {

		DecimalFormat df = new DecimalFormat("#.##");
		String total = "";
		List<Custom_field> custom_fields = tookanResponse.getCustom_fields();

		double orderTotal = 0.00d;
		for (Custom_field cf : custom_fields) {
			if ("Total".equals(cf.getLabel())) {
				orderTotal = Double.parseDouble(cf.getData());
				total = df.format(orderTotal);
			}
		}

		return total;

	}

}