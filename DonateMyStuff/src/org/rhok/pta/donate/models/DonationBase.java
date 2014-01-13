package org.rhok.pta.donate.models;

import org.rhok.pta.donate.utils.DonateMyStuffConstants;

/**
 * This is the base class extended by both the Donation Offer and Request.
 * 
 * @author Ishmael Makitla
 *
 */
public class DonationBase {
	
	public DonationBase(){
		//
	}
 
	//flag is used to indicate if an offer is unverified, valid or invalid
    protected int flag = DonateMyStuffConstants.FLAG_UNVERIFIED; 
    
    protected int status  =DonateMyStuffConstants.STATUS_OPEN;
    
    public int getFlag() {
		return flag;
	}

	public void setFlag(int flag) {
		this.flag = flag;
	}
		
	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

}
