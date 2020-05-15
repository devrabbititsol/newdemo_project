package com.restassured.services;
import java.text.SimpleDateFormat; 
import java.util.Date;

public class ReportPaths {
	
	public static final String  reportPathName =  new SimpleDateFormat("YYYY-MM-dd-hh-mm-ss-SSS").format(new Date())+"_Report.html";

}
