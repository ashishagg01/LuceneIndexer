package com.ibm.ussd.fsl.utils;

import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * 
 * @author vikram
 *
 */

public class USSDIndexerProperties {

		
	private static Logger logger = Logger.getLogger(USSDIndexerProperties.class);

	/** Reference variable for instance of the properties */

	private static Properties propertiesHolder = new Properties();

	public USSDIndexerProperties(){
		
	}
	
	
	/**
	 * Method is used for getting the properties from the property file
	 * 
	 * @param pStrProperties
	 *            String property
	 * @return String value
	 */
	public static String getProperties(String pStrProperties) {
		return propertiesHolder.getProperty(pStrProperties);
	}// END of Method getProperties

	/**
	 * @param propertiesHolder
	 *            the propertiesHolder to set
	 */

	public void setPropertiesHolder(Properties propertiesHolder) {
		USSDIndexerProperties.propertiesHolder = propertiesHolder;
		logger.debug("PropertiesHolder : " + propertiesHolder.toString());
	}
	
}
