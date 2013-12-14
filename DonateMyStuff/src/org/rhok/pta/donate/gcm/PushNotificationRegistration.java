package org.rhok.pta.donate.gcm;

/**
 * This is the class that represets a Push Notification Registration (request) which is sent by all instance of Donate-My-Stuff APP
 * to be activated for Push Notifications (Google Cloud Messaging)
 * 
 * @author Ishmael Makitla
 *         GDG/RHoK Pretoria
 *         2013
 *         South Africa
 *
 */
public class PushNotificationRegistration {
  //Registration ID assigned by the GCM Registrar
  private String registration_id;
  //Donate-My-Stuff-ID of the beneficiary/donor assigned by Donate-My-Stuff Service
  private String dms_id;
  //Friendly name of the beneficiary/donor (we may use email address)
  private String handle;
  //GCM operation type (0-registration, 1 send/push notification)
  private int opcode =0;
  //message payload
  private NotificationMessage message;
  
  public static final int SEND = 1;
  public static final int REGISTER = 0;
  
  public PushNotificationRegistration(){
	  //
  }
  
  //Constructor for new registrations...
  public PushNotificationRegistration(String registration_id, String dms_id,String handle) {	
	this.registration_id = registration_id;
	this.dms_id = dms_id;
	this.handle = handle;
	//set opcode to register (0)
	this.opcode = REGISTER;
  }
    
 //constructor used to create a instance of Notification-request for sending a message
  public PushNotificationRegistration(NotificationMessage message) {
	this.message = message;
	//set opcode to SEND (1)
	this.opcode = SEND;
  }

  public String getRegistration_id() {
	return registration_id;
  }

  public void setRegistration_id(String registration_id) {
	this.registration_id = registration_id;
  }

  public String getDms_id() {
	return dms_id;
  }

  public void setDms_id(String dms_id) {
	this.dms_id = dms_id;
  }

  public String getHandle() {
	return handle;
  }

  public void setHandle(String handle) {
	this.handle = handle;
  }

 public int getOpcode() {
	return opcode;
 }

 public void setOpcode(int opcode) {
	this.opcode = opcode;
 }

 public NotificationMessage getMessage() {
	return message;
 }

 public void setMessage(NotificationMessage message) {
	this.message = message;
 }
  
}
