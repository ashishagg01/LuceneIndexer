/**
-----------------------------------------------------------------------------
	PROJECT			:	USSD 
	MODULE			:	USSD INDEXER
	CLASS NAME		:	
	DESCRIPTION		: 	
	Copyright (C) 2012 IBM Global Services
    ALL RIGHTS RESERVED
-----------------------------------------------------------------------------
 */
package com.ibm.ussd.fsl.utils;


import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.ibm.ussd.fsl.indexer.USSDSubscriberIndexer;
import com.ibm.ussd.fsl.messagebeans.IndexerSwitchCoreQueueSender;

/**
 * @author vikram
 *
 */
public class SchedulerJob extends QuartzJobBean {
	 
	/** Log implementation */
	private static final Logger logger = Logger.getLogger(SchedulerJob.class.getName());
	
	private IndexerSwitchCoreQueueSender queueSender;
	
	private USSDSubscriberIndexer indexer;
	
	

	/**
	 * Overridden method for execute internal
	 * @param  JobExecutionContext context 
	 * @throws JobExecutionException
	 */
	protected void executeInternal(JobExecutionContext context)	throws JobExecutionException {
		logger.info("Entering  execute Internal ");
		try{
			String createIndex = indexer.createIndex();
			//if(createIndex){
			queueSender.sendMessage(createIndex);
			//}
		}catch(Exception oException){
			logger.error("Exception occurred",oException);
		}
		logger.info("Exiting  execute Internal ");
	}
		
	/**
	 * @param queueSender the queueSender to set
	 */
	public void setQueueSender(IndexerSwitchCoreQueueSender queueSender) {
		this.queueSender = queueSender;
	}
	/**
	 * @param indexer the indexer to set
	 */
	public void setIndexer(USSDSubscriberIndexer indexer) {
		this.indexer = indexer;
	}
}
