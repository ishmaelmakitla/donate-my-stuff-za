package org.rhok.pta.donate.models;

import java.util.UUID;

/**
 * This class represents a donation bid- this is when a beneficiary expresses interest in a donation-offer
 * 
 * @author IMakitla
 *
 */
public class DonationBid {

	private String id;
	private String offerid;
	private String requestid;
	private String beneficiaryid;
	
	public DonationBid(){
		this.id = UUID.randomUUID().toString();
	}

	public String getOfferid() {
		return offerid;
	}

	public void setOfferid(String offerid) {
		this.offerid = offerid;
	}
	
	
	public String getRequestid() {
		return requestid;
	}

	public void setRequestid(String requestid) {
		this.requestid = requestid;
	}

	public String getBeneficiaryid() {
		return beneficiaryid;
	}

	public void setBeneficiaryid(String beneficiaryid) {
		this.beneficiaryid = beneficiaryid;
	}

	public String getId() {
		return id;
	}

	public void setId(String _id) {
		this.id = (id == null? _id: id);
	}
	
	
	
}
