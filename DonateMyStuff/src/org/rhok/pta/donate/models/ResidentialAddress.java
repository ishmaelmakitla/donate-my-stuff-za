package org.rhok.pta.donate.models;

import java.io.Serializable;

import org.mortbay.util.ajax.JSONObjectConvertor;

import com.google.gson.JsonObject;

public class ResidentialAddress implements Serializable{

	private static final long serialVersionUID = -5764412644200007856L;
	
	private String unitnumber;
	private String unitname;
	private String streetname;
	private String areaname;
	private String city;
	private String province;
	private String country;
	//geo-coding
	private long xcoordinate;
	private long ycoordinate;
	
	public ResidentialAddress(){
		//
	}

	public String getUnitNumber() {
		return unitnumber;
	}

	public void setUnitNumber(String unitNumber) {
		this.unitnumber = unitNumber;
	}

	public String getUnitName() {
		return unitname;
	}

	public void setUnitName(String unitName) {
		this.unitname = unitName;
	}

	public String getStreetName() {
		return streetname;
	}

	public void setStreetName(String streetName) {
		this.streetname = streetName;
	}

	public String getAreaName() {
		return areaname;
	}

	public void setAreaName(String areaName) {
		this.areaname = areaName;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public long getxCoordinate() {
		return xcoordinate;
	}

	public void setxCoordinate(long xCoordinate) {
		this.xcoordinate = xCoordinate;
	}

	public long getyCoordinate() {
		return ycoordinate;
	}

	public void setyCoordinate(long yCoordinate) {
		this.ycoordinate = yCoordinate;
	}
	@Override
	public String toString(){
		String address = "";
		JsonObject jsonAddress = new JsonObject();
		jsonAddress.addProperty("unit_number", unitnumber);
		jsonAddress.addProperty("unit_name", unitname);
		jsonAddress.addProperty("street", streetname);
		jsonAddress.addProperty("area", areaname);
		jsonAddress.addProperty("city", city);
		jsonAddress.addProperty("province", province);
		jsonAddress.addProperty("country", country);
		jsonAddress.addProperty("x", xcoordinate);
		jsonAddress.addProperty("y", ycoordinate);
		address = jsonAddress.toString();
		return address;
	}
}
