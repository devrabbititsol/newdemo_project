package com.fileupload;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.AfterSuite;
import com.configurations.*;
import com.restassured.services.ReportPaths;
import com.utilities.BaseClass;
import com.utilities.PrimaryInfo;
import com.utilities.Utilities;

@SuppressWarnings("unused")
public class DesktopAutomationReportUpload extends BaseClass {
	String primaryInfo = "";
	private String projectPath = System.getProperty("user.dir");
	private String reportsPath = projectPath + File.separator + "DesktopReports" + File.separator + "DesktopAutomationReport.txt";	
	private String resultCount;
	private String datasetResult;
	private String reportstatus;


	/*
	 * Uploading file to server
	 * @moduleId 
	 * @testCaseId
	 * @userID
	 * @testsetId
	 * @moduleDescription
	 */
	private void writeFile(String str, String className, String filePath, String fileExtension) throws Exception {
		Writer out = new java.io.BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath + File.separator + className + fileExtension), "UTF-8"));
		try {
			out.write(str);
			out.flush();
			out.close();
			setPermissionsToFile(filePath + File.separator + className + fileExtension);
		} finally {
			out.close();
		}
	}
	  private void setPermissionsToFile(String path) throws Exception {
			if (isMac() || isSolaris() || isUnix()) {
				File file = new File(path);
				boolean isFile = file.exists();
				if (isFile) {
					Set<PosixFilePermission> perms = new HashSet<>();
					perms.add(PosixFilePermission.OWNER_READ);
					perms.add(PosixFilePermission.OWNER_WRITE);
					perms.add(PosixFilePermission.OWNER_EXECUTE);

					perms.add(PosixFilePermission.OTHERS_READ);
					perms.add(PosixFilePermission.OTHERS_WRITE);
					perms.add(PosixFilePermission.OTHERS_EXECUTE);

					perms.add(PosixFilePermission.GROUP_READ);
					perms.add(PosixFilePermission.GROUP_WRITE);
					perms.add(PosixFilePermission.GROUP_EXECUTE);

					Files.setPosixFilePermissions(file.toPath(), perms);
					System.out.println("File permissions changed.");
				}
			} else {
				File file = new File(path);
				if (file.exists()) {
					file.setReadable(true);
					file.setExecutable(true);
					file.setWritable(true);
				}
			}
		}
	    
	  
	@AfterSuite
	public void uploadFile() throws Exception {
//		Class<?> c = Class.forName(primaryInfo);
//		String primaryInfoObject = (String) c.getField("primaryInfo").get(c.getSuperclass().getName());
		

		try {
			System.out.println("===============");
			GlobalData.getPrimaryInfo();
			if(new PrimaryInfo().primaryInfoData() == null) {
				JSONObject primaryInfo = new JSONObject();
				
					primaryInfo.put("report_type", "winium");
					long startExecutionTime = GlobalData.getStartTime();
					long endExecutionTime = GlobalData.getEndTime();
					JSONArray testcasesArray = GlobalData.getReportData();
					primaryInfo.put("testcases_result", testcasesArray);
				primaryInfo.put("start_time", String.valueOf(startExecutionTime));
				primaryInfo.put("end_time", String.valueOf(endExecutionTime));
				System.out.println(projectPath +File.separator + "DesktopReports" + File.separator);
				writeFile(primaryInfo.toString(), "DesktopAutomationReport", projectPath +File.separator + "DesktopReports" + File.separator, ".json");
				System.out.println("Successfully created file.");
				return;
			}
			JSONObject primaryInfoObj = new JSONObject(new PrimaryInfo().primaryInfoData());
			boolean is_web = primaryInfoObj.optBoolean("is_web");
			boolean isDesktopAutomation = primaryInfoObj.optBoolean("is_Desktop_Automation");
			String mobile = primaryInfoObj.optString("mobile_platform");
			String moduleDescription = primaryInfoObj.optString("module_description");
			//System.err.println(Constants.testName);
			String moduleId = primaryInfoObj.optString("module_id");
			String userId = primaryInfoObj.optString("user_id");
			String executedUserId = primaryInfoObj.optString("executed_user_id");
			long startExecutionTime = GlobalData.getStartTime();
			long endExecutionTime = GlobalData.getEndTime();
			String client_timezoneId = primaryInfoObj.optString("client_timezone_id");
			String report_upload_url = primaryInfoObj.optString("report_upload_url");
			String testcaseId = primaryInfoObj.optString("testcase_id");
			String datasetId = primaryInfoObj.optString("testcase_id");
			String subModuleId = primaryInfoObj.isNull("sub_module_id") ? null : primaryInfoObj.optString("sub_module_id");
			String testsetId = primaryInfoObj.optString("testset_id").equals("0") ? "" : primaryInfoObj.optString("testset_id");
			String testsetName = (primaryInfoObj.optString("testset_name") == null || primaryInfoObj.optString("testset_name").equals("null") || primaryInfoObj.optString("testset_name").isEmpty()) ? "" : primaryInfoObj.optString("testset_name");
			JSONArray testcasesArray = GlobalData.getReportData();
			JSONObject primaryInfo = new JSONObject();
			primaryInfo.put("testset_name", testsetName);
			//String elapsedTime = Utilities.getElapsedTime(reportPath)
			//MultipartUtility multipart = new MultipartUtility(reportUploadURL, charset);
			primaryInfo.put("user_id", userId);
			primaryInfo.put("executed_user_id", executedUserId);
			if (!testsetId.isEmpty()) {
				primaryInfo.put("testset_id", testsetId);
			} else {
				primaryInfo.put("testcase_id", testcaseId);
			}
			 
			if(mobile != null && (mobile.equalsIgnoreCase("Android")||mobile.equalsIgnoreCase("iOS"))) {
				primaryInfo.put("report_type", mobile);
			} else if(isDesktopAutomation) {
				primaryInfo.put("report_type", "winium");
			} else {
				primaryInfo.put("report_type", is_web ? "web" : "api");
			}
			
			primaryInfo.put("module_id", (subModuleId == null || subModuleId.isEmpty() || subModuleId.equals("0")) ? moduleId : subModuleId);
			//primaryInfo.put("report_result", result);
			//primaryInfo.put("dataset_result", datasetResult);
			primaryInfo.put("start_time", String.valueOf(startExecutionTime));
			primaryInfo.put("end_time", String.valueOf(endExecutionTime));
			
			long elapsedtime = startExecutionTime- endExecutionTime;
			primaryInfo.put("execution_time", String.valueOf(elapsedtime));
			//primaryInfo.put("report_status", reportstatus);
			primaryInfo.put("client_time_zone_id", client_timezoneId);
			//System.out.println("client_time_zone_id"+  client_timezoneId);
			//primaryInfo.put("report_data", GlobalData.getReportData().toString());
			primaryInfo.put("testcases_result", testcasesArray);
			
			System.out.println(primaryInfo.toString());
			
			Utilities.doSaveElementsToServer(report_upload_url, primaryInfo.toString());
			
		
		//new FileUploaderClient().uploadFile(report_upload_url, reportsPath, userId,executedUserId, testcaseId, testsetId, moduleId, subModuleId, is_web, resultCount, testsetName, mobile, client_timezoneId,datasetResult,true,startExecutionTime,endExecutionTime);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private String getTestcasesFailCount(String filePath){
		int passcount = 0;
		int failcount = 0;
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(filePath));
			String line = reader.readLine();
		
			
			while (line != null) {
				if(line.contains("Device Information")) {
					break;
				}
				if(line.contains("test-status label right outline capitalize pass")) {
					//System.out.println(line);
					passcount = passcount +1;
				}
				if(line.contains("test-status label right outline capitalize fail")) {
					//System.out.println(line);
					failcount = failcount +1;
				}
				
				
				// read next line
				line = reader.readLine();
			}
			//System.out.println(passcount + "fail " +failcount);
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return passcount +  " PASS / " + failcount + " FAIL";
		
	}
	
	private String getDatasetsResult(String filePath) {
		int passcount = 0;
		int failcount = 0;
		BufferedReader reader;
		String result = "";
		try {
			//filePath = "/Users/admin/Desktop/FWCode/SmartQE/MobileReports/2019-02-18-03-27-20-418_Report.html";
			reader = new BufferedReader(new FileReader(filePath));
			String line = reader.readLine();
			while (line != null) {
				if(line.contains("Device Information")) {
					break;
				}
				String exactLine = "<span class='test-name'>";
				if(line.contains(exactLine) && (filePath.contains("MobileReports") || filePath.contains("APIReports"))) {
					result = result +  ((result.endsWith("Fail") || result.endsWith("Pass")) ? " $ ": "");
					result = result + line.replace(exactLine,"").replace("</span>", "").replace("\t", "");
				} else if(line.contains(exactLine) && filePath.contains("WebReports")) {{
					result = result +  ((result.endsWith("Fail") || result.endsWith("Pass")) ? " $ ": "");
					result = result +  line.split("class=\"left\">")[1].replaceAll("</span>", "");
				}
				
				}
			
				if(line.contains("test-status label right outline capitalize pass")) {
					passcount = passcount + 1;
					result = result + " - Pass";
					//System.err.println(line);
				}
				
				if(line.contains("test-status label right outline capitalize fail")) {
					result = result + " - Fail";
				}
				// read next line
				line = reader.readLine();
			}
			//System.out.println(passcount + "fail " +failcount);
			reader.close();
			System.err.println("datasetResult" + result);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
		
	}

}
