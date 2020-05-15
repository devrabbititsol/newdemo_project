package com.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
public class ConfigFilesUtility {
	/* Set the Configuration setUp path */
	File src;
	FileInputStream fis;
	Properties prop;
	public void loadPropertyFile(String configFileName) throws Exception {
		src = new File("." + File.separator + "ConfigFiles" + File.separator + configFileName);
		fis = new FileInputStream(src);
		prop = new Properties();
		prop.load(fis);
	}
	
	public String getProperty(String propKey) {
		return prop.getProperty(propKey, "");
	}
	
	public int getIntProperty(String propKey) {
		String value =  prop.getProperty(propKey, "0");
		return Integer.parseInt(value);
	}
	
	
}
