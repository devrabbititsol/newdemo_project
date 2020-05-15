package com.restassured.services;

import static io.restassured.RestAssured.given;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.configurations.GlobalData;
import com.utilities.ConfigFilesUtility;

import io.restassured.response.Response;
import io.restassured.response.ResponseBody;
import io.restassured.specification.RequestSpecification;

public class APIService {

	private static JSONArray jsonArray;

	@SuppressWarnings({ "unused", "static-access" })
	public static String callRequest(ConfigFilesUtility con, String urlParams, String headers, int requestType, int bodyType, String inputBody,
			String datatsetHeader, String dataResources,  Logger logger) {

		String[] bodyTpes = new String[] { "", "form-data", "x-www-form-urlencoded", "raw" };
		String[] types = new String[] { "", "GET", "POST", "PUT", "DELETE" };
		String contentType = "";
		JSONObject jsonoBj = new JSONObject();
		jsonArray = new JSONArray();
	
		try {
			
			//Constants.iS_WEB = false;
			//Constants.IS_TESTCASE = false;
			
			
			JSONObject jsonObject = new JSONObject(con.getProperty("PrimaryInfo"));
		
			String testCaseName = jsonObject.getString("testcase_name");
			String projectName = jsonObject.optString("project_name");
			String projectId = jsonObject.optString("project_id");
			String reportsPath = "reportsPath";
			String returnString = jsonObject.optString("returnString");
			String packageFolder = jsonObject.optString("moduleName");
			String type = types[requestType];
			jsonoBj.put("testcase_name",testCaseName + "-" + datatsetHeader);
			jsonoBj.put("datasets", jsonArray);
			new GlobalData().reportData(testCaseName, jsonoBj);
			new GlobalData().primaryInfoData(con);
			//extentHeaderLog( datatsetHeader);
			
			String Url = jsonObject.optString("project_url") + dataResources;
			//String format = jsonObject.optString("raw_type_format");
			reportCreation("info",Url);
			//test.log(LogStatus.INFO, "<b style = 'background-color: #ffffff; color : #000000 ;' >" + Url + "</b>");
			
			RequestSpecification requestSpec = given();

			JSONArray headersJsonArray = new JSONArray(headers);
			JSONArray parameters = new JSONArray(urlParams);
			JSONObject body = new JSONObject(inputBody);
			int raw_id = body.optInt("raw_id");	 //content type
			reportCreation("info","Request Type :  " + type);
			//test.log(LogStatus.INFO, "Request Type :  " + type);
			contentType = (raw_id == 5 ? "application/xml" : "application/json");
			reportCreation("info", "Content Type :  " + contentType);
			//test.log(LogStatus.INFO, "Content Type :  " + contentType);
			logger.info("Request Type :  " + type);

			if (headersJsonArray.length() > 0) {
				extentReportLog( "Headers");
				logger.info("Headers :  " + headersJsonArray.toString());
			} 
			
			for (int i = 0; i < headersJsonArray.length(); i++) {
				JSONObject headerObj = headersJsonArray.getJSONObject(i);
				String headerkey = headerObj.getString("header_key");
				String headerValue = headerObj.getString("header_value");
				reportCreation("info", headerkey + " : "+ headerValue);
				//test.log(LogStatus.INFO, headerkey + " : "+ headerValue );
				requestSpec.header(headerkey, headerValue);
			}
			
			if (parameters.length() > 0) {
				extentReportLog( "Input Parameters");
				logger.info("Parameters :  " + parameters.toString());
			} 
				
			for (int i = 0; i < parameters.length(); i++) {
				JSONObject parametersObj = parameters.getJSONObject(i);
				if (requestType > 1) {
					String key = parametersObj.getString("param_key");
					String value = parametersObj.getString("param_value");
					reportCreation("info", key + " : "+ value);
					//test.log(LogStatus.INFO, key + " : "+ value);
					requestSpec.queryParam(key, value);
				} else {
					String key = parametersObj.getString("param_key").replaceAll("\n", "");
					String value = parametersObj.getString("param_value").replaceAll("\n", "");
					reportCreation("info", key + " : "+ value);
				//test.log(LogStatus.INFO, key + " : "+ value);
					requestSpec.param(key, value);
				}
			}
			
			if (body.length() > 0) {
				extentReportLog( "Input Body");
				reportCreation("info", "body  :  " + body.toString());
				//test.log(LogStatus.INFO, "body  :  " + body.toString());
				logger.info("body :  " + body.toString());
			}
			
			Response response = null;
			if (requestType == 1) { // GET
				response = requestSpec.when().contentType(contentType).get(Url).then().extract().response();
			} else if (requestType > 1) { // POST,PUT,DELETE
				String rawBody = "";
				if (bodyType == 1 || bodyType == 2) { // form-data or x-www-form-urlencoded
					JSONArray bodyArray = body.optJSONArray(bodyTpes[bodyType]);
					for (int i = 0; i < body.length(); i++) {
						JSONObject bodyObj = bodyArray.getJSONObject(i);
						requestSpec.formParam(bodyObj.optString("key"), bodyObj.optString("value"));
					}
				} else if (bodyType == 3) { // raw data
					rawBody = body.optString("raw_text");
				}

				if (requestType == 2) {
					response = requestSpec.contentType(contentType).body(rawBody).when().post(Url);
				} else if (requestType == 3) {
					response = requestSpec.contentType(contentType).body(rawBody).when().put(Url);
				} else if (requestType == 4) {
					response = requestSpec.contentType(contentType).body(rawBody).when().delete(Url);
				}

			}
			
			
			extentReportLog( "Output");
			if (response != null) {	
				@SuppressWarnings("rawtypes")
				ResponseBody responseBody = response.getBody();
				int statusCode = response.getStatusCode();
				String responseString = responseBody.asString();

				// Assert that correct status code is returned.
				/*
				 * Assert.assertEquals(statusCode actual value , 200 expected value ,
				 * "Correct status code returned");
				 */
				if (statusCode == 200 || statusCode == 201) {
					reportCreation("pass", testCaseName + " API status code is : " + statusCode);
					//test.log(LogStatus.PASS, testCaseName + " API status code is : " + statusCode);
					logger.info(testCaseName + " API status code is :" + statusCode + " : " + responseString);
					System.out.println(responseString);
					//Constants.testName = Constants.testName + " - PASS $";
					//ExtentConfigurations.passedDataSets = ExtentConfigurations.passedDataSets + 1;
					return responseString;
				} else if(statusCode == 400) {	
					reportCreation("fail", responseString);
					//test.log(LogStatus.FAIL, responseString);
					logger.info("response :  " + responseString +"status :" + statusCode);
					return responseString;				
				} else if(statusCode == 404) {
					
					reportCreation("fail", "Invalid response : HTTP Status 404  Not Found ");
					//test.log(LogStatus.FAIL, "Invalid response : <br><b>HTTP Status 404 � Not Found </b> <br/> <b>Message :</b> " + Url + "<br/><b>Description :</b> The origin server did not find a current representation for the target resource or is not willing to disclose that one exists");
					logger.info("Invalid response body returned as :  " + responseString);
				} else {
					
					logger.info("Invalid response body" + responseString);
					if(contentType.equalsIgnoreCase("application/xml") && responseString.contains("<?xml")) {
						reportCreation("fail", "Invalid response body" + responseString);
						//test.log(LogStatus.FAIL, "Invalid response body" + responseString);
					} else {
						reportCreation("fail", "Response is in HTML content please check the logger file");
						//test.log(LogStatus.FAIL, "Response is in HTML content please check the logger file");
					}
					if (isJSONValid(responseString)) {
						reportCreation("fail", "Invalid response body" + responseString);
						//test.log(LogStatus.FAIL, "Invalid response body" + responseString);
					} else {
						reportCreation("fail", "Invalid JSON : Response is in HTML content please check the logger file");
						//test.log(LogStatus.FAIL, "Invalid JSON : Response is in HTML content please check the logger file");
					}
				}
				System.out.println(responseString);
			}
			
		} catch (Exception e) {
			extentReportLog( "Output");
			String exception = e.getClass().getSimpleName() + "-" + e.getLocalizedMessage();
			reportCreation("fail", "Invalid response : " + exception);
			//test.log(LogStatus.FAIL, "Invalid response : " + exception);
			logger.info("Invalid response body returned as :  " + exception);
			e.printStackTrace();
		}
		//ExtentConfigurations.failedDataSets = ExtentConfigurations.failedDataSets + 1;
		//Constants.testName = Constants.testName + " - FAIL $";
		return "";

	}
	
	public static void extentReportLog(String data) {
		reportCreation("info", data);
		//test.log(LogStatus.INFO, "<b style = 'background-color: #ffffff; color : #1976D2 ; font-size : 15px' >"+ data + "</b>");
	}
	
	public static void extentHeaderLog(String data) {
		reportCreation("info", data);
		//test.log(LogStatus.INFO, "<b style = 'background-color: #ffffff; color : #ff8f00 ; font-size : 18px' >"+ data + "</b>");
	}

	
	public static boolean isJSONValid(String json) {
		try {
			new JSONObject(json);
		} catch (JSONException ex) {
			try {
				new JSONArray(json);
			} catch (JSONException exception) {
				return false;
			}
		}
		return true;
	}
	
	
	 public static boolean isXMLLike(String inXMLStr) {

	        boolean retBool = false;
	        Pattern pattern;
	        Matcher matcher;
	        // REGULAR EXPRESSION TO SEE IF IT AT LEAST STARTS AND ENDS
	        // WITH THE SAME ELEMENT
	        final String XML_PATTERN_STR = "<(\\S+?)(.*?)>(.*?)</\\1>";
	        // IF WE HAVE A STRING
	        if (inXMLStr != null && inXMLStr.trim().length() > 0) {
	            // IF WE EVEN RESEMBLE XML
	            if (inXMLStr.trim().startsWith("<")) {

	                pattern = Pattern.compile(XML_PATTERN_STR,
	                Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);

	                // RETURN TRUE IF IT HAS PASSED BOTH TESTS
	                matcher = pattern.matcher(inXMLStr);
	                retBool = matcher.matches();
	            }
	        // ELSE WE ARE FALSE
	        }
	        return retBool;
	    }
	 
	public static void reportCreation(String result, String data) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("result_type", result);
		jsonObject.put("text", data);
		jsonArray.put(jsonObject);
	}



}
