package com.utilities;

import java.io.File;

import org.openqa.selenium.winium.WiniumDriverService;

public class Test {
public static void main(String[] args) {
	String winiumDriverPath = "C:\\Users\\Ralimili\\Desktop\\Winium.Desktop.Driver.exe";
	File drivePath = new File(winiumDriverPath);
	System.out.println("Test in progress B");
	    WiniumDriverService service = new WiniumDriverService.Builder().usingDriverExecutable(drivePath).usingPort(9999).withVerbose(true).withSilent(false).buildDesktopService();
	    System.out.println("Test in progress C");
	    try{
	    	try{
	    		service.stop();
	    	}catch(Exception ex) {
		    }
	    	service.start(); //Build and Start a Winium Driver service
	    }catch(Exception ex) {
	    }
	    System.out.println("Test in progress D");

}
}
