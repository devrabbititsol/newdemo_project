package com.configurations;

import org.json.JSONArray;
import org.json.JSONObject;

import com.utilities.ConfigFilesUtility;

public class GlobalData {

	private static JSONArray testcaseArray;
	private static long startTime;
	private static long endTime;
	public static void reportData(String testcaseName,JSONObject jsonObj) {
		if (testcaseArray == null) {
			testcaseArray = new JSONArray();
			startTime = System.currentTimeMillis();
			
		}
		if(!testcaseName.isEmpty())
			testcaseArray.put(jsonObj);
	}
	
	public static JSONArray getReportData() {
		return testcaseArray;
	}
	
	
	private static String primaryInfo;
	public static void primaryInfoData(ConfigFilesUtility conObj) {
		if(conObj != null) {
			String data =  conObj.getProperty("PrimaryInfo");
			if(data != null && !data.isEmpty())
			primaryInfo = data;
		}
	}
	
	public static String getPrimaryInfo() {
		endTime = System.currentTimeMillis();
		return primaryInfo;
	}
	
	public static long getStartTime() {
		return startTime;
	}
	
	
	public static long getEndTime() {
		return endTime;
	}
	
	
}
