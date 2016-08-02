package com.luvbrite.web.models;

import java.util.List;

public class ControlOptions {

	private double doubleDown = 0d;
	private double doubleDownOfferValue = 0d;
	private double orderMinimum = 0d;
	private List<Integer> doubleDownEligibleProducts;
	
	public double getDoubleDown() {
		return doubleDown;
	}
	public void setDoubleDown(double doubleDown) {
		this.doubleDown = doubleDown;
	}
	public double getDoubleDownOfferValue() {
		return doubleDownOfferValue;
	}
	public void setDoubleDownOfferValue(double doubleDownOfferValue) {
		this.doubleDownOfferValue = doubleDownOfferValue;
	}
	public double getOrderMinimum() {
		return orderMinimum;
	}
	public void setOrderMinimum(double orderMinimum) {
		this.orderMinimum = orderMinimum;
	}
	public List<Integer> getDoubleDownEligibleProducts() {
		return doubleDownEligibleProducts;
	}
	public void setDoubleDownEligibleProducts(
			List<Integer> doubleDownEligibleProducts) {
		this.doubleDownEligibleProducts = doubleDownEligibleProducts;
	}
	
	
	
	private List<Integer> localZipcodes;
	private List<Integer> shippingZipcodes;

	public List<Integer> getLocalZipcodes() {
		return localZipcodes;
	}
	public void setLocalZipcodes(List<Integer> localZipcodes) {
		this.localZipcodes = localZipcodes;
	}
	public List<Integer> getShippingZipcodes() {
		return shippingZipcodes;
	}
	public void setShippingZipcodes(List<Integer> shippingZipcodes) {
		this.shippingZipcodes = shippingZipcodes;
	}
	

	private boolean dev = true;
	public boolean isDev() {
		return dev;
	}
	public void setDev(boolean dev) {
		this.dev = dev;
	}
}
