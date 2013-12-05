package org.rhok.pta.donate.utils;

import com.google.gson.JsonObject;

/**
 * 
 * @author Ishmael Makitla
 *
 */
public class DonateMyStuffUtils {

	public enum DonateMyStuffType {OFFER, REQUEST;
	public static DonateMyStuffType toDonateMyStuffType(String type){
		return DonateMyStuffType.valueOf(type.toUpperCase());
	}
	};
	
	//TYPE OF REGISTRATION
	public enum RegistrationType {DONOR, BENEFICIARY;
	public static RegistrationType toRegistrationType(String type){
		return RegistrationType.valueOf(type.toUpperCase());
	}
	};
	
	//type of donor/beneficiary
	/**
	 * Individual donor -  a person who is donating in his personal capacity
	 * Organization donor - a charitable organization donating as a collective
	 * Individual beneficiary - a person who makes donation requests or receives donation in his personal capacity
	 * Beneficiary organization - these organizations include NGOs, SOS, and other relief organizations who seek out donation
	 *                            on behalf of the marginalized and needy.
	 * @author IMakitla
	 *
	 */
	
	public enum RoleType {INDIVIDUAL_DONOR,DONOR_ORGANIZATION, INDIVIDUAL_BENEFICIARY, BENEFICIARY_ORGANIZATION;
	public static RoleType toRoleType(String type){
		return RoleType.valueOf(type.toUpperCase());
	}
	};
	
	public enum DonatedItemType {BOOK,SHOES, CLOTHES, BLANKETS, SCHOOL_UNIFORM,TROUSER, DRESS,T_SHIRT,SHIRT,SHORTS,STATIONARY;
	public static DonatedItemType toDonatedItemType(String type){
		return DonatedItemType.valueOf(type.toUpperCase());
	   }
	};
	
	/**
	 * 
	 * @param status
	 * @param message
	 * @return
	 */
	public static String asServerResponse(int status, String message){
		String response = "";
		JsonObject responseJSON = new JsonObject();
		responseJSON.addProperty("status", status);
		responseJSON.addProperty("message", message);
		response = responseJSON.toString();
		return response;
	}
	
	
}
