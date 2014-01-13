package org.rhok.pta.donate.utils;

public class DonateMyStuffConstants {

	
	/**
	 * Statuses for authentication
	 */
	public static final int LOGIN_SUCCESSFULL = 100;
	public static final int LOGIN_FAILED = 101;
	
	/**
	 * Statuses for making donation offers
	 * 
	 */
	public static final int DONATION_OFFER_SUCCESS = 200;
	public static final int DONATION_OFFER_FAILURE = 201;
	/**
	 * Statuses for making donation REQUESTS
	 * 
	 */
	public static final int DONATION_REQUEST_SUCCESS = 300;
	public static final int DONATION_REQUEST_FAILURE = 301;
	
	public static final int REGISTRATION_SUCCESSFULL = 400;
	public static final int REGISTRATION_FAILED = 401;
	/**
	 * General Purpose Status Codes
	 */
	public static final int OK = 0;
	public static final int ERROR=1;
	public static final int WARNING =2;
	
	/**
	 * Offer/Request Flag Values
	 */
	public static final int FLAG_UNVERIFIED = 0;
	public static final int FLAG_VALID = 1;
	public static final int FLAG_INVALID = -1;
	
	/**
	 * Offer/Request Status Values
	 */
	public static final int STATUS_OPEN = 0;
	public static final int STATUS_ALLOCATED = 1;
	public static final int STATUS_DELIVERED = 2;
	public static final int STATUS_CLOSED = 3;
	public static final int STATUS_CANCELLED = -1;
}
