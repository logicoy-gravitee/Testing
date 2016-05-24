package com.luvbrite.web.models;

import java.util.Date;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;


@Entity("users")
public class User {

	@Id
	private long _id;
	private int orderCount;
	
	private double moneySpent;
	
	private boolean active;
	
	private String username;
	private String password;
	private String email;
	private String fname;
	private String lname;
	private String role;
	private String phone;
	private String notes;
	
	@Property("joined")
	private Date dateRegistered;
	
	private Address billing;
	
	private UserIdentity identifications;
	private UserMarketing marketing;
	
	private Date dob;
	private String gender;
	public long get_id() {
		return _id;
	}
	public void set_id(long _id) {
		this._id = _id;
	}
	public int getOrderCount() {
		return orderCount;
	}
	public void setOrderCount(int orderCount) {
		this.orderCount = orderCount;
	}
	public double getMoneySpent() {
		return moneySpent;
	}
	public void setMoneySpent(double moneySpent) {
		this.moneySpent = moneySpent;
	}
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getFname() {
		return fname;
	}
	public void setFname(String fname) {
		this.fname = fname;
	}
	public String getLname() {
		return lname;
	}
	public void setLname(String lname) {
		this.lname = lname;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}
	public Date getDateRegistered() {
		return dateRegistered;
	}
	public void setDateRegistered(Date dateRegistered) {
		this.dateRegistered = dateRegistered;
	}
	public Address getBilling() {
		return billing;
	}
	public void setBilling(Address billing) {
		this.billing = billing;
	}
	public UserIdentity getIdentifications() {
		return identifications;
	}
	public void setIdentifications(UserIdentity identifications) {
		this.identifications = identifications;
	}
	public UserMarketing getMarketing() {
		return marketing;
	}
	public void setMarketing(UserMarketing marketing) {
		this.marketing = marketing;
	}
	public Date getDob() {
		return dob;
	}
	public void setDob(Date dob) {
		this.dob = dob;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
}
