package org.rhok.pta.donate;

/**
 * This class represents a donation bid- this is when a beneficiary expresses interest in a donation-offer
 * 
 * @author IMakitla
 *
 */
public class DonationBid {

	private String offerid;
	private String beneficiaryid;
	
	public DonationBid(){}

	public String getOfferid() {
		return offerid;
	}

	public void setOfferid(String offerid) {
		this.offerid = offerid;
	}

	public String getBeneficiaryid() {
		return beneficiaryid;
	}

	public void setBeneficiaryid(String beneficiaryid) {
		this.beneficiaryid = beneficiaryid;
	}
	
	
	
}
