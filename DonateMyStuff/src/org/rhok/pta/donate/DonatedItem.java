package org.rhok.pta.donate;

import java.io.Serializable;
import java.util.UUID;

import org.rhok.pta.donate.DonateMyStuffUtils.DonatedItemType;

import com.google.gson.JsonObject;
/**
 * Class representing something (item) being donated or being requested for donation
 * @author Ishmael Makitla
 *
 */
public class DonatedItem implements Serializable{
  /**
	 * 
	 */
  private static final long serialVersionUID = 5223677159201175984L;
  private String id;
  private String name;
  private int gendercode;
  int size = 0;
  int agerestriction = 0;
  int age = 0;
  String type; 
  DonatedItemType itemType;
  
  
  public DonatedItem(){
	  this.id = UUID.randomUUID().toString();
  }
  
public DonatedItemType getItemType() {
	return itemType;
}



public String getType() {
	return type;
}



public void setType(String _type) {	
	this.type = _type;
	this.itemType = DonatedItemType.toDonatedItemType(type);
}



public String getId() {
	return id;
}


public void setId(String id) {
	this.id = id;
}


public String getName() {
	return name;
}


public void setName(String name) {
	this.name = name;
}


public int getSize() {
	return size;
}


public void setSize(int size) {
	this.size = size;
}


public int getAgeRestriction() {
	return agerestriction;
}


public void setAgeRestriction(int ageRestriction) {
	this.agerestriction = ageRestriction;
}


public int getAge() {
	return age;
}


public void setAge(int age) {
	this.age = age;
}



  public int getGenderCode() {
	return gendercode;
}


public void setGenderCode(int genderCode) {
	this.gendercode = genderCode;
}


/**
   * This method returns a JSON version of the Donated-Item
   * @return
   */
  public JsonObject asJSON(){
	  
	  JsonObject donatedItem = new JsonObject();
	  donatedItem.addProperty("id", id);
	  donatedItem.addProperty("name", name);
	  donatedItem.addProperty("size", size);
	  donatedItem.addProperty("age", age);
	  donatedItem.addProperty("gender", gendercode);
	  donatedItem.addProperty("agerestriction", 0);
	  donatedItem.addProperty("type",type);
		
	  return donatedItem;
  }
  @Override
  public String toString(){
	  return asJSON().toString();
  }
  /**
   * 
   * @param json
   * @return
   */
  public static DonatedItem asDonatedItem(JsonObject json){
	  DonatedItem item = new DonatedItem();
	  
	  String id = null;
	  if(json.get("id") != null){
		  id = json.get("id").toString();
	  }
	  String name = json.get("name").toString();
	  String size = json.get("size").toString();
	  String age = json.get("age").toString();
	  String genderCode = json.get("gender").toString();
	  String ageRestriction = json.get("age_restriction").toString();
	  
	  //set values
	  //if ID is not in the JSON, then leave generated id as is
	  if(id != null){item.setId(id); }
	  
	  item.setAge(Integer.parseInt(age));
	  item.setAgeRestriction(Integer.parseInt(ageRestriction));
	  item.setSize(Integer.parseInt(size));
	  item.setGenderCode(Integer.parseInt(genderCode));
	  return item;
  }
  
}
