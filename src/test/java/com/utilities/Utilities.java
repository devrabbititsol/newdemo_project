package com.utilities;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.winium.WiniumDriver;

public class Utilities {

	private static boolean isMobile = false;

	// Capture Screen Shot and save in the location
	public static String captureScreenshot(WebDriver driver, String screenShotName) {
		String path = "";
		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH-mm-ss");
		Date dt = new Date();
		String html = "";
		try {
			/*if(Constants.IS_TESTCASE && Constants.iS_WEB || !Constants.iS_WEB) {
				Constants.TOTAL_TC_FAILED = Constants.TOTAL_TC_FAILED + 1;
				Constants.IS_TESTCASE = false;
			} */
			
			System.out.println(dateFormat.format(dt));
			TakesScreenshot ts = (TakesScreenshot) driver;
			File source = ts.getScreenshotAs(OutputType.FILE);
			html = covertScreenshotToBase64(source , screenShotName);
			path = System.getProperty("user.dir") + File.separator + "screenshots";
			createDirectory(path);
			FileUtils.copyFile(source, new File(path + File.separator  + dateFormat.format(dt) + "_" + screenShotName + ".png"));
			System.out.println("screenshot is taken");

		} catch (Exception e) {
			System.out.println("exception while taking screenshot" + e.getMessage());
		}
		
		return html;
	}
	

	public static String captureScreenshotDesktopApplication(WiniumDriver driver, String screenShotName) {
		String path = "";
		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH-mm-ss");
		Date dt = new Date();
		String html = "";
		try {
			/*if(Constants.IS_TESTCASE && Constants.iS_WEB || !Constants.iS_WEB) {
				Constants.TOTAL_TC_FAILED = Constants.TOTAL_TC_FAILED + 1;
				Constants.IS_TESTCASE = false;
			} */
			
			System.out.println(dateFormat.format(dt));
			TakesScreenshot ts = (TakesScreenshot) driver;
			File source = ts.getScreenshotAs(OutputType.FILE);
			html = covertScreenshotToBase64(source , screenShotName);
			path = System.getProperty("user.dir") + File.separator + "screenshots";
			createDirectory(path);
			FileUtils.copyFile(source, new File(path + File.separator  + dateFormat.format(dt) + "_" + screenShotName + ".png"));
			System.out.println("screenshot is taken");

		} catch (Exception e) {
			System.out.println("exception while taking screenshot" + e.getMessage());
		}
		
		return html;
	}

	
	

	
	@SuppressWarnings("resource")
	public static String covertScreenshotToBase64(File file, String name) {
		try {		
		FileInputStream fis = new FileInputStream(file);
		byte byteArray[] = new byte[(int)file.length()];
		fis.read(byteArray);
		String imageString = Base64.encodeBase64String(byteArray);
		return doImageClickAnimation(imageString, name);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	@SuppressWarnings("unused")
	public static String doImageClickAnimation(String img, String screenName) {
		
		int width = 500;
		int height = 250;
		if(isMobile) {
			width = 400;
			height = 700;
		} 
		//String image = "<img onclick='clickImage(this)' src=\"data:image/png;base64, " + img + "\" alt=\""+ screenName +"\" width=\"" + width + "\" height=\"" + height + "\"/>";
		return (img);
		
	}
	
	// make zip of reports
	/*public static void zip(String filepath) {
		try {
			File inFolder = new File(filepath);
			File outFolder = new File("Reports.zip");
			ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(outFolder)));
			BufferedInputStream in = null;
			byte[] data = new byte[1000];
			String files[] = inFolder.list();
			for (int i = 0; i < files.length; i++) {
				in = new BufferedInputStream(new FileInputStream(inFolder.getPath() + File.separator + files[i]), 1000);
				out.putNextEntry(new ZipEntry(files[i]));
				int count;
				while ((count = in.read(data, 0, 1000)) != -1) {
					out.write(data, 0, count);
				}
				out.closeEntry();
			}
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/
	
	public static boolean createDirectory(String directory) {
		File fileDirectory = new File(directory);
		if (!fileDirectory.exists()) {
			fileDirectory.mkdir();
			return true;
		}
		return false;
	}

	@SuppressWarnings("resource")
	public static String getElapsedTime1(String filePath) {
		File file = new File(filePath);
		Scanner in = null;
		try {
			in = new Scanner(file);
			
			while (in.hasNext()) {
				String line = in.nextLine();
				if (line.contains("suite-total-time-overall-value panel-lead")) {
					return line.split("<span class='suite-total-time-overall-value panel-lead'>")[1].replaceAll("</span>", "");
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return "";
	}
	


	public static void setMobilePlatform() {
		isMobile  = true;
	}
	
	
	public static Object[][] sauceDevicesList() throws Exception {
		
		 List<Object> data = new ArrayList<>(); 
		 ConfigFilesUtility  configFileObj = new ConfigFilesUtility();
		 configFileObj.loadPropertyFile("mobileconfig.properties");
		 String executionDevicePlatform = configFileObj.getProperty("executionPlatform");
		 if(executionDevicePlatform.equalsIgnoreCase("local")) {
			 	configFileObj.loadPropertyFile("DeviceCapabilities.properties");
				Object obj = new Object[]{
				configFileObj.getProperty("deviceName"),
				configFileObj.getProperty("platformName"),
				configFileObj.getProperty("platformVersion"),
				configFileObj.getProperty("appium_version"),
				configFileObj.getProperty("device_orientation"),
				configFileObj.getProperty(configFileObj.getProperty("projectName")),
				configFileObj.getProperty("udid"),
				""
			};
		  data.add(obj);
		 } else {
			configFileObj = new ConfigFilesUtility();
			configFileObj.loadPropertyFile("SauceDeviceCapabilities.properties");
			String js = configFileObj.getProperty("devices");
			String app = configFileObj.getProperty("app");
			JSONArray devicesArray = new JSONArray(js);

			for(int i=0;i<devicesArray.length();i++) {
				
					JSONObject devicesObj = devicesArray.getJSONObject(i);
					String deviceName = "";
					if(executionDevicePlatform.equals("saucelabvirtural")) {
						deviceName = devicesObj.optString("deviceName");		
					} else {
						deviceName = devicesObj.optString("sauceDeviceId");	
					}
					//String appe = "https://github.com/saucelabs-training/demo-java/blob/master/appium-example/resources/android/GuineaPigApp-debug.apk?raw=true";
					Object obj = new Object[]{
					deviceName,
					devicesObj.optString("devicePlatformName"),
					devicesObj.optString("devicePlatformVersion"),
					devicesObj.optString("appium_version","1.9.1"),
					devicesObj.optString("device_orientation","portrait"),
					configFileObj.getProperty("projectName"),
					configFileObj.getProperty("testObjApiKey"),
					app,	 
					};
				
				 data.add(obj);
			}
		 }
		
		Object[][] array = new Object[data.size()][];
		for (int i = 0; i < data.size(); i++) {
		    Object row =  data.get(i);
		    array[i] = (Object[]) row;
		}
		return  array;
	}

	
	public static boolean doSaveElementsToServer(String url, String json) throws Exception {
		try {
			URL obj = new URL(url);
			System.out.println(url);
			TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				@Override
				public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
					// TODO Auto-generated method stub

				}

				@Override
				public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
					// TODO Auto-generated method stub

				}
			} };

			// Install the all-trusting trust manager
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

			// Create all-trusting host name verifier
			HostnameVerifier allHostsValid = new HostnameVerifier() {
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			};
			// Install the all-trusting host verifier
			HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json");
			con.setConnectTimeout(200000);
			con.setConnectTimeout(200000);
			con.setDoOutput(true);
			OutputStream os = con.getOutputStream();
			DataOutputStream wr = new DataOutputStream(os);
			byte[] isoString = json.getBytes("UTF-8");
			
			wr.write(isoString, 0, isoString.length);
			// wr.writeBytes(json);
			wr.flush();
			wr.close();
			os.close();
			con.connect();
			int responseCode = con.getResponseCode();
			System.out.println("Response Code : " + responseCode);
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			System.out.println(response.toString());

			JSONObject jsonObject = new JSONObject(response.toString());
			if (jsonObject.has("status") && !jsonObject.getString("status").equalsIgnoreCase("SUCCESS")) {
				System.out.println("Report Exception " + jsonObject.optString("message"));
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getLocalizedMessage());
			return false;
		}
		return true;
	}
}