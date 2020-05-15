package com.utilities;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;

import com.configurations.GlobalData;
import com.saucelabs.common.SauceOnDemandAuthentication;
import com.saucelabs.saucerest.SauceREST;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileBy;
import io.appium.java_client.MobileElement;
import io.appium.java_client.TouchAction;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.IOSElement;
import io.appium.java_client.remote.AndroidMobileCapabilityType;
import io.appium.java_client.remote.AutomationName;
import io.appium.java_client.remote.MobileCapabilityType;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import io.appium.java_client.touch.offset.PointOption;

public class MobileBase {
	
	// Environment variables path
	private String appiumServerJSPath = System.getenv("APPIUM_JS_PATH");
	private String nodeJSPath = System.getenv("NODE_PATH");
	
	public static String projectPath = System.getProperty("user.dir");
	private AppiumDriverLocalService appiumDriverService;
	private String platformVersion = StringUtils.EMPTY;
	public AppiumDriver<MobileElement> appiumDriver;
	private ConfigFilesUtility configFileObj;
	public String empty = StringUtils.EMPTY;
	public String device;
	private JSONArray jsonArray;
	@SuppressWarnings("rawtypes")
	private ThreadLocal<AppiumDriver> driverThread;
	public String id;
	
	public String seleniumURI = "@ondemand.saucelabs.com:443";
	private boolean isSauce = false;
	private String username;
	private String accesskey;
	private String sauceUrl;
   
	/**************************************************** Desired Capabilities (Android/iOS) ************************************************/
	/*
	 * Set up desired capabilities and pass the Android app-activity and app-package to Appium
	 * Create AppiumDriver instance and connect to the Appium server
	 * It will launch the Application in Android Device using the configurations specified in Desired Capabilities
	 */

	public AppiumDriver<MobileElement> launchMobileDriver() throws Exception {
		
		//configFileObj = new ConfigFilesUtility();
		//configFileObj.loadPropertyFile("DeviceCapabilities.properties");
		
		//start Appium server
		appiumDriverService = setUpAppiumDriver();
		startAppiumServer(); 

		//ExtentTest test = reports.startTest("Device Information");
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, configFileObj.getProperty("deviceName").trim());		
		capabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, configFileObj.getProperty("platformName").trim());		
		capabilities.setCapability(MobileCapabilityType.FULL_RESET,  configFileObj.getProperty("fullreset").trim());	     
		capabilities.setCapability(MobileCapabilityType.UDID, configFileObj.getProperty("udid").trim());
		capabilities.setCapability(MobileCapabilityType.NEW_COMMAND_TIMEOUT, 60*10); //ending the session 10 minutes
		capabilities.setCapability(MobileCapabilityType.APP, projectPath + File.separator + "mobile" + File.separator + (configFileObj.getProperty("app").trim().contains(".ipa") ? "ios" : "android") + File.separator +  configFileObj.getProperty("app").trim());
		platformVersion = configFileObj.getProperty("platformVersion");	
		if(configFileObj.getProperty("app").trim().contains(".ipa")) {
			capabilities.setCapability("autoAcceptAlerts",true);
			capabilities.setCapability("bundleid", configFileObj.getProperty("appPackage").trim());
			capabilities.setCapability("wdaLaunchTimeout",60000); // launching webdriver agent ti
			capabilities.setCapability(MobileCapabilityType.PLATFORM_VERSION, configFileObj.getProperty("platformVersion").trim());
			capabilities.setCapability("wdaStartupRetries", 2); //webdriveragent retries 4 times
			capabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, "XCUITest");
			appiumDriver = new IOSDriver<MobileElement>(new URL(appiumDriverService.getUrl().toString()), capabilities);
		} else {
			capabilities.setCapability(AndroidMobileCapabilityType.AUTO_GRANT_PERMISSIONS,true);
			capabilities.setCapability("autoAcceptAlerts", true);
			capabilities.setCapability("appPackage", configFileObj.getProperty("appPackage").trim());
			capabilities.setCapability("appActivity", configFileObj.getProperty("appActivity").trim());
			//capabilities.setCapability("appWaitActivity", configFileObj.getProperty("appActivity").trim());
			if ((platformVersion != null) && !(platformVersion.trim().isEmpty())) {
				capabilities.setCapability(MobileCapabilityType.PLATFORM_VERSION, configFileObj.getProperty("platformVersion").trim());
			} 
			if ((platformVersion != null) && !platformVersion.trim().isEmpty() && (Integer.valueOf(platformVersion.trim().split("\\.")[0]) >= 7) ) {
				capabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, AutomationName.ANDROID_UIAUTOMATOR2);
				capabilities.setCapability("unicodeKeyboard",  false);
				capabilities.setCapability("resetKeyboard", false);
			} 
			appiumDriver = new AndroidDriver<MobileElement>(new URL(appiumDriverService.getUrl().toString()), capabilities);
		}
		appiumDriver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
		
		System.out.println("sessionDetails" + new JSONObject(appiumDriver.getSessionDetails()));
		JSONObject sessionObj = new JSONObject(appiumDriver.getSessionDetails());
		@SuppressWarnings("unused")
		String deviceScreenSize = sessionObj.optString("deviceScreenSize");
		
		String deviceModel = sessionObj.optString("deviceModel");
		String deviceManufacturer = sessionObj.optString("deviceManufacturer");
		String platformName = sessionObj.optString("platformName");
		
		if(configFileObj.getProperty("app").trim().contains(".ipa")) {
			JSONObject rectObj = sessionObj.optJSONObject("viewportRect");
			deviceScreenSize = "Width: " + rectObj.optString("width") + ", Height: " + rectObj.optString("height") + ", Top: " + rectObj.optString("top");
			deviceModel = sessionObj.optString("deviceName");
			platformVersion = platformVersion.isEmpty() ? configFileObj.getProperty("VERSION") : platformVersion;
		} else {
			reportCreation("info", "Device Manufacture : " + deviceManufacturer);
		}
		reportCreation("info", "Device Model : " + deviceModel);
		reportCreation("info", "Platform Name : " + platformName);
		reportCreation("info", "Platform Version : " + platformVersion);		
		return appiumDriver;
	}
	

	
	
	@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	protected AppiumDriver<MobileElement> createDriver(
			  String deviceName,
	            String platformName,
	            String platformVersion,
	            String appiumVersion,
	            String deviceOrientation,
	            String projectName,
	            String testObjectApiKey,
	            String className,
	            String app)
	            throws Exception {
		 
		 device = deviceName;
		 
		if(!isSauce) {
			 return launchMobileDriver();
		 } else {
			DesiredCapabilities capabilities = new DesiredCapabilities();
			capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, deviceName);
			capabilities.setCapability("browserName", "");
			capabilities.setCapability("deviceOrientation", deviceOrientation);
			capabilities.setCapability("noReset", "false");
			capabilities.setCapability("name", className);
			capabilities.setCapability("testobject_session_creation_timeout", "900000");
			capabilities.setCapability("unicodeKeyboard", false);
			capabilities.setCapability("resetKeyboard", false);
			capabilities.setCapability(AndroidMobileCapabilityType.AUTO_GRANT_PERMISSIONS, true);
			capabilities.setCapability("autoAcceptAlerts", true);
			capabilities.setCapability("testobject_test_name ", "Default Appium Test");
			capabilities.setCapability(MobileCapabilityType.PLATFORM_VERSION, platformVersion);
			capabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, platformName);
			capabilities.setCapability("testobject_suite_name ", "Default Appium Suite");
			capabilities.setCapability("testobject_test_name ", "Default Appium Test");
			 String executionDevicePlatform = configFileObj.getProperty("executionPlatform");
			if (executionDevicePlatform.equalsIgnoreCase("saucelabdevice")) {
				//capabilities.setCapability(MobileCapabilityType.APP, app);
				capabilities.setCapability("testobject_api_key", testObjectApiKey);
				capabilities.setCapability("testobject_app_id", "1");
				//capabilities.setCapability("appiumVersion ", "1.15.1");
				sauceUrl = app;
			} else {
				capabilities.setCapability(MobileCapabilityType.APP, app);
				// capabilities.setCapability(MobileCapabilityType.APPIUM_VERSION,appiumVersion);
				capabilities.setCapability("build", projectName);

				 username = configFileObj.getProperty("userName");
				 accesskey = configFileObj.getProperty("accessKey");
				SauceOnDemandAuthentication authentication = new SauceOnDemandAuthentication(username, accesskey);
				sauceUrl = "https://" + authentication.getUsername() + ":" + authentication.getAccessKey() + seleniumURI + "/wd/hub";
			}
			

			/**
			 * ThreadLocal variable which contains the {@link AndroidDriver} instance which
			 * is used to perform browser interactions with.
			 */
			driverThread = new ThreadLocal<AppiumDriver>();

			/**
			 * ThreadLocal variable which contains the Sauce Job Id.
			 */
			ThreadLocal<String> sessionId = new ThreadLocal<String>();
			
			if(platformName.equalsIgnoreCase("Android")){
				capabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, AutomationName.ANDROID_UIAUTOMATOR2);
			// Launch remote browser and set it as the current thread
				driverThread.set(new AndroidDriver(new URL(sauceUrl), capabilities));
			} else {
				capabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, AutomationName.IOS_XCUI_TEST);
				driverThread.set(new IOSDriver(new URL(sauceUrl), capabilities));	
			}
			 id = ((RemoteWebDriver) driverThread.get()).getSessionId().toString();
			sessionId.set(id);
			appiumDriver = driverThread.get();
	        
		 }
	      return appiumDriver;
	      
	  }
	 

	 
	/**
	 * DataProvider that explicitly sets the browser combinations to be used.
	 *
	 * @param testMethod
	 * @return Two dimensional array of objects with browser, version, and platform
	 *         information
	 * @throws Exception 
	 */
	@DataProvider(name = "capabilities", parallel = false)
	public  Object[][] sauceBrowserDataProvider(Method testMethod) throws Exception {
		return sauceDevicesList();
	}
	
	private Object[][] sauceDevicesList() throws Exception {
		
		 List<Object> data = new ArrayList<>(); 
		 configFileObj = new ConfigFilesUtility();
		 configFileObj.loadPropertyFile("mobileconfig.properties");
		 String executionDevicePlatform = configFileObj.getProperty("executionPlatform");
		 if(executionDevicePlatform.equalsIgnoreCase("local")) {
			 isSauce = false;
			 configFileObj.loadPropertyFile("DeviceCapabilities.properties");
				Object obj = new Object[]{
				configFileObj.getProperty("deviceName"),
				configFileObj.getProperty("platformName"),
				configFileObj.getProperty("platformVersion"),
				configFileObj.getProperty("appium_version"),
				configFileObj.getProperty("device_orientation"),
				configFileObj.getProperty(configFileObj.getProperty("projectName")),
				configFileObj.getProperty("testobject_api_key"),
				configFileObj.getProperty("app"),		 
			};
		  data.add(obj);
		 } else {
			isSauce = true;
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

	
	/********************************** Hide Keyboard (Android/iOS) ****************************/
	public void hideKeyboard() {
		if(appiumDriver != null) {
			if(configFileObj.getProperty("app").contains(".ipa")) {
				//hideKeyBoard();
				//((IOSDriver) appiumDriver).hideKeyboard(HideKeyboardStrategy.TAP_OUTSIDE);
			} else {
				try {
					if (!isSauce) {
						//((AndroidDriver<MobileElement>) appiumDriver).pressKey(new KeyEvent(AndroidKey.ENTER));
						//appiumDriver.hideKeyboard();
					}
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		}
	}
	
	@SuppressWarnings("rawtypes")
	public void hideKeyBoard() {
		IOSElement element = (IOSElement) appiumDriver.findElementByClassName("XCUIElementTypeKeyboard");
		Point keyboardPoint = element.getLocation();
		TouchAction touchAction = new TouchAction(appiumDriver);
		touchAction.tap(PointOption.point(keyboardPoint.getX() + 2, keyboardPoint.getY() - 2)).perform();
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/************************************** APPIUM SETUP ***************************************/
	// Appium server setup service 
	// Appium server setup service 
	private AppiumDriverLocalService setUpAppiumDriver() {
			 // Only for iOS/Mac
			if(configFileObj.getProperty("app").contains(".ipa") || isMac()) {
				nodeJSPath = configFileObj.getProperty("nodePath").trim();
				appiumServerJSPath = configFileObj.getProperty("appiumJSPATH").trim();
			}
			
			AppiumDriverLocalService appiumDriverService = AppiumDriverLocalService.buildService(new AppiumServiceBuilder()
					.usingAnyFreePort()
					.usingDriverExecutable(new File(nodeJSPath))
					.withAppiumJS(new File(appiumServerJSPath)));
		return appiumDriverService;
	}
		
	
	public boolean checkIfServerIsRunnning(int port) {
		
		boolean isServerRunning = false;
		ServerSocket serverSocket;
		try {
			serverSocket = new ServerSocket(port);
			serverSocket.close();
		} catch (IOException e) {
			//If control comes here, then it means the port is in use
			isServerRunning = true;
		} finally {
			serverSocket = null;
		}
		return isServerRunning;
	}	
 
	
	// used to start appium server
	private boolean startAppiumServer() {
		if (appiumDriverService != null) {
			appiumDriverService.start();
			return appiumDriverService.isRunning();
		}
		return false;
	}
	
	// used to stop appium server
	protected void stopAppiumServer() {
		if (appiumDriverService != null) {
			appiumDriverService.stop();
			System.out.println(" Stopped Appium Server");
		}   
	}
	
	
	/**************************************************** Find Element **************************************************/
	
	protected MobileElement findElement(String xpath,boolean isSkip,boolean needToScroll) {
		MobileElement element = implementationToFindElement(xpath,isSkip,needToScroll);;
		return element;
	}
	

	
	/**
	 * used to find element by text  
	 * Used TouchAction for scrolling to find the element
	 * @param xpath
	 * @return MobileElement
	 */
	// used to finding the element in screen automatically scrolls full page for finding the element by Text
	protected MobileElement implementationToFindElement(String xpath, boolean isSkipElement, boolean needToScroll) {
		isElementDispalyed = true;
		MobileElement element = null;
		int maxSwipes = 10;
		if (needToScroll) {
			appiumDriver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		} else {
			appiumDriver.manage().timeouts().implicitlyWait(40, TimeUnit.SECONDS);
		}
		try {
			element = appiumDriver.findElementByXPath(xpath);
		} catch (Exception e) {
			isElementDispalyed = false;
			if (needToScroll) {
				try {
					scrollDown();
					for (int i = 0; i < maxSwipes; i++) {
						if (isElementFound(xpath)) {
							isElementDispalyed = true;
							element = appiumDriver.findElementByXPath(xpath);
						} else {
							scrollDown();
						}
					}

				} catch (Exception e1) {
					System.err.println("Element not found");
				}

				if (isSkipElement && !isElementDispalyed) {
					//if (extentTest != null)
						//extentTest.log(LogStatus.SKIP, "Skipped Element : " + xpath);
				}
			}
		}
 
	    return element;
	}
	
	//Only For android password field
	protected WebElement findElementByPwd(String Pwd, String value) {
		WebElement PasswordTextInputText = waitForExpectedElement(appiumDriver, Pwd, 60);
		PasswordTextInputText.sendKeys(value);
		return PasswordTextInputText;
	}

	
	public boolean isElementFound(String text) {
        try {
           appiumDriver.findElement(By.xpath(text));
        } catch(Exception e){
            return false;
        }
        return true;
    }
	
	
	public MobileElement scrollByText(String text, String xpath) {
		try {
			return appiumDriver.findElement(MobileBy.AndroidUIAutomator("new UiScrollable(new UiSelector().scrollable(true).instance(0)).scrollIntoView(new UiSelector().text(\"" + text + "\").instance(0));"));
		} catch (Exception e) {
			WebElement isElementPresence = waitForExpectedElement(appiumDriver, xpath, 40);
			if (isElementPresence.isDisplayed()) {
				return appiumDriver.findElementByXPath(xpath);
			}
		}
		return null;
	}
	
	// wait 60 seconds for finding the element
	public static WebElement waitForExpectedElement(AppiumDriver<MobileElement> appiumDriver, String locator, long timeOutInSeconds) {
		WebDriverWait wait = new WebDriverWait(appiumDriver, timeOutInSeconds);
		//appiumDriver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
		return wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(locator)));
	}
	
	
	
	/*********************************** ScrollDown (Android/iOS) ***********************************/
	
	private void scrollDown() {
	    //if pressX was zero it didn't work for me
	    int pressX = appiumDriver.manage().window().getSize().width / 2;
	    // 4/5 of the screen as the bottom finger-press point
	    int bottomY = appiumDriver.manage().window().getSize().height * 4/5;
	    // just non zero point, as it didn't scroll to zero normally
	    int topY = appiumDriver.manage().window().getSize().height / 8;
	    //scroll with TouchAction by itself
	    scroll(pressX, bottomY, pressX, topY);
	    System.out.println(""+pressX + "y: ::: " + bottomY);
	}
	
	
	/*
	 * Don't forget that it's "natural scroll" where 
	 * fromY is the point where you press and toY where you release it
	 */
	@SuppressWarnings("rawtypes")
	private void scroll(int fromX, int fromY, int toX, int toY) {
	    TouchAction touchAction = new TouchAction(appiumDriver);
	    touchAction.longPress(PointOption.point(fromX, fromY)).moveTo(PointOption.point(toX, toY)).release().perform();
	}


	//For New Reports
	
	/************************************** Reports ******************************************/
	//For Mobile
		public void setTestcaseName(String tescaseName, ConfigFilesUtility confObj) {	
			JSONObject jsonoBj= new JSONObject();
			jsonArray = new JSONArray();
			jsonoBj.put("testcase_name",tescaseName);
			jsonoBj.put("datasets", jsonArray);
			GlobalData.reportData(tescaseName, jsonoBj);
			GlobalData.primaryInfoData(confObj);
			jsonoBj.put("browser_type", "Mobile");
			//reportCreation("DeviceName", device);
		}

	
		
		
	// These methods used to write the extent reports and logger
	public void testPass(String message, Logger logger) {
		reportCreation("pass", message);
		//test.log(LogStatus.PASS, message);
		logger.info(message);
	}

	public void testInfo(String message,  Logger logger) {
		reportCreation("info", message);
		//test.log(LogStatus.INFO, message);
		logger.info(message);
	}

	public void testFail(String message,  Logger logger, AppiumDriver<MobileElement> mobileDriver) {
		//reportCreation("fail", message);
		//test.log(LogStatus.FAIL, message);
		logger.error(message);
		Utilities.setMobilePlatform();
		reportFailureCreation("fail", message,Utilities.captureScreenshot(mobileDriver, message + " is Failed"));
	}
	
	//private static ExtentTest extentTest;
	// ScreenName Header for Reports
	public void testLogHeader(String data) {
		//extentTest =  test;
		reportHeadersCreation("info", data);
		//test.log(LogStatus.INFO, "<b style = 'background-color: #ffffff; color : #ff8f00 ; font-size : 18px' >"+ data + "</b>");
	}
	
	public void reportHeadersCreation(String result, String data) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("result_type", "screen");
		jsonObject.put("text", data);
		jsonArray.put(jsonObject);
		//GlobalData.primaryInfoData(conObj);
	}
	
/*	public void reportCreation(String result, String data) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("result_type", result);
		jsonObject.put("text", data);
		jsonArray.put(jsonObject);
	}*/

	
	public void reportCreation(String result, String data) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("result_type", result);
		jsonObject.put("text", data);
		jsonArray.put(jsonObject);
	}
	
	public void reportFailureCreation(String result, String data, String image) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("result_type", result);
		jsonObject.put("text", data);
		jsonObject.put("screenshot", image);
		jsonArray.put(jsonObject);
	}
	
	
	/*********************************** Find the Client OS ****************************************/
	
	private static String OS = System.getProperty("os.name").toLowerCase();
	private static boolean isMac() {
		return (OS.indexOf("mac") >= 0);
	}
	
	public void autoCompleteSelection(String xpath, String text) throws Exception {
		WebElement element = appiumDriver.findElement(By.xpath(xpath));
		element.sendKeys(text);
		int x = element.getLocation().getX();
		int y = element.getLocation().getY();

		System.out.println("X value: "+x+" Y value: "+y);
		Thread.sleep(2000);

		@SuppressWarnings("rawtypes")
		TouchAction action = new TouchAction(appiumDriver).press(PointOption.point(x+60, y+260)).release();
		action.perform();
	}
	
	
	public void uploadImageFromGallery() {
		//List<MobileElement> pic= appiumDriver.findElements(By.className("//android.view.View"));
		//pic.get(1).click(); // click based on index of image
		appiumDriver.findElement(By.xpath("//android.widget.LinearLayout[1]/android.view.view[1][@clickable='True']")).click();
	}
	
	@AfterMethod
    public void tearDown(ITestResult result) throws Exception {
		
		System.out.println("=============Tear+===================");
		if (!isSauce) {
			appiumDriver.quit();
			stopAppiumServer();
		} else {
			SauceREST client = new SauceREST(username,accesskey);
			Map<String, Object> updates = new HashMap<>();
			updates.put("passed", result.isSuccess() ? "passed" : "failed");
			client.updateJobInfo(driverThread.get().getSessionId().toString(), updates);
	        driverThread.get().quit();
		}
       
    }

	
	public void tearDown() {
		if (!isSauce) {
		System.out.println("=============Tear2+===================");
		appiumDriver.quit();
		stopAppiumServer();
		}
	}
	
	
	
	
//	@SuppressWarnings("rawtypes")
//	public void swipingHorizontal(String xpath) throws InterruptedException {
//		  //Get the size of screen.
//		  int size = 0;
//
//		  //Find swipe x points from screen's with and height.
//		  //Find x1 point which is at right side of screen.
//		  int x1 = (int) (appiumDriver.manage().window().getSize().width * 0.70);
//		  //Find x2 point which is at left side of screen.
//		  int x2 = (int) (appiumDriver.manage().window().getSize().width * 0.30);
//		  
//		  //Create object of TouchAction class.
//		  TouchAction action = new TouchAction(appiumDriver);
//		  
//		  //Find element to swipe from right to left.
//		 // MobileElement ele1 =  appiumDriver.findElementById(xpath);  
//		  //Create swipe action chain and perform horizontal(right to left) swipe.
//		  //Here swipe to point x1 Is at left side of screen. So It will swipe element from right to left.
//		 // action.longPress(LongPressOptions.longPressOptions().withElement(ElementOption.element(ele1))).moveTo(PointOption.point(x1,580)).release().perform();
// 
//		  //Find element to swipe from left to right.
//		  MobileElement ele2 =  appiumDriver.findElementById(xpath);
//		  //Create swipe action chain and perform horizontal(left to right) swipe.
//		  //Here swipe to point x2 Is at right side of screen. So It will swipe element from left to right.
//		  
//		  action.longPress(LongPressOptions.longPressOptions().withElement(ElementOption.element(ele2))).moveTo(PointOption.point(x2,580)).release().perform();
//	}
//	
//	public  void swipeLeft(String xpath) {
//		Point[] points = getXYtoHSwipe();
//		 MobileElement ele2 =  appiumDriver.findElementByXPath(xpath);
//		 int x =  ele2.getLocation().getX();
//		 int y =  ele2.getLocation().getY();
//
//		new TouchAction(appiumDriver).longPress(LongPressOptions.longPressOptions().withPosition(PointOption.point(x,y))).moveTo(PointOption.point(points[1].x, points[1].y)).release().perform();
//	}
//	
//	/**
//	 *
//	 * @return start and end points for horizontal(left-right) swipe
//	 */
//	private  Point[] getXYtoHSwipe() {
//		// Get screen size.
//		Dimension size = appiumDriver.manage().window().getSize();
//
//		// Find starting point x which is at right side of screen.
//		int startx = (int) (size.width * 0.70);
//		// Find ending point x which is at left side of screen.
//		int endx = (int) (size.width * 0.30);
//		// Find y which is in middle of screen height.
//		int startEndy = size.height / 2;
//
//		return new Point[] { new Point(startx, startEndy), new Point(endx, startEndy) };
//	}
//	
//
//	/**
//	 * Swipe from Left to Right
//	 */
//	public  void swipeRight(String xpath) {
//		Point[] points = getXYtoHSwipe();
//		 MobileElement ele2 =  appiumDriver.findElementByXPath(xpath);
//		 int x =  (int)(ele2.getLocation().getX() * 0.20);
//		 int y =  (int)(ele2.getLocation().getY() * 0.80);
//		new TouchAction(appiumDriver).longPress(LongPressOptions.longPressOptions().withPosition(PointOption.point(x,y))).moveTo(PointOption.point(points[0].x, points[0].y)).release().perform();
//	}
//	
	

	
	private boolean isElementDispalyed = false;
	public boolean skipifElementisNotDisplayed() {
		return isElementDispalyed;
	}
	
}
