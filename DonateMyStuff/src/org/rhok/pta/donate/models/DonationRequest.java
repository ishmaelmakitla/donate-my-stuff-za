package org.rhok.pta.donate.models;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import com.google.gson.JsonObject;

public class DonationRequest {
	String id;
    String beneficiaryid;
    String donationofferid;
    Date requestdate;
    DonatedItem item;
    boolean collect;
    
    int status;
    int flag;
    
    public DonationRequest(){
    	this.id = UUID.randomUUID().toString();
    }
    
        
        
    public int getStatus() {
		return status;
	}


	public void setStatus(int status) {
		this.status = status;
	}


	public int getFlag() {
		return flag;
	}


	public void setFlag(int flag) {
		this.flag = flag;
	}


	public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
	}


	public String getBeneficriaryId() {
		return beneficiaryid;
	}


	public void setBeneficriaryId(String beneficriaryId) {
		this.beneficiaryid = beneficriaryId;
	}


	public String getDonationOfferId() {
		return donationofferid;
	}


	public void setDonationOfferId(String donationOfferId) {
		this.donationofferid = donationOfferId;
	}


	public Date getRequestDate() {
		return requestdate;
	}


	public void setRequestDate(Date requestDate) {
		this.requestdate = requestDate;
	}


	public DonatedItem getRequestedDonationItem() {
		return item;
	}


	public void setRequestedDonationItem(DonatedItem requestedDonationItem) {
		this.item = requestedDonationItem;
	}


	public boolean getCollect() {
		return collect;
	}


	public void setCollect(boolean collect) {
		this.collect = collect;
	}


	/**
	 * This method returns a JSON version of the instantiated DonationOffer object
	 * @return
	 */
	public JsonObject asJSON(){
		JsonObject json = new JsonObject();
		json.addProperty("id", id);
		json.addProperty("requestdate", requestdate.toString());
		json.addProperty("beneficiaryid", beneficiaryid);
		json.addProperty("donationofferid", donationofferid);
		//donated item
		JsonObject donatedItemJson = null;
		if(item != null){
			donatedItemJson = item.asJSON();	
		}
		
		json.add("item", donatedItemJson);
		
		System.out.println(json.toString());
		
		return json;
	}
	
}
