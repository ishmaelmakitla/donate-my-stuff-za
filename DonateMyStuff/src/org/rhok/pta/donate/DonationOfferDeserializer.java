package org.rhok.pta.donate;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class DonationOfferDeserializer implements JsonDeserializer<DonationOffer>{

	@Override
	public DonationOffer deserialize(JsonElement json, Type arg1,
			JsonDeserializationContext arg2) throws JsonParseException {
		DonationOffer offer = new DonationOffer();
		//set values from the incoming JSON object
		
		return offer;
	}

}
