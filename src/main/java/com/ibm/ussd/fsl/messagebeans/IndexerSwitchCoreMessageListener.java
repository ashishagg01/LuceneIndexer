package com.ibm.ussd.fsl.messagebeans;

import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;

import com.ibm.ussd.fsl.indexer.USSDSubscriberSearch;


/**
 * Message-Driven Bean implementation class for: SimReplacementValidationQueue
 *
 */
@TransactionManagement(TransactionManagementType.BEAN)
public class IndexerSwitchCoreMessageListener implements MessageListener {

	/** Reference variable for Logger for this class. */
	private static final Logger logger = Logger.getLogger(IndexerSwitchCoreMessageListener.class.getName());
	
	/** Reference variable for subscriber search */
	private USSDSubscriberSearch subscriberSearch;
	

	/**
     * Default constructor. 
     */
    public IndexerSwitchCoreMessageListener() {
        // TODO Auto-generated constructor stub
    }
	
	/**
     * @see MessageListener#onMessage(Message)
     */
    public void onMessage(Message message) {
    	try{
    		logger.debug("Entering on Message");
	    	TextMessage txtMessage=(TextMessage)message;
	    	String strMessage = txtMessage.getText();
	    	logger.info("Switch Core :"+strMessage);
	    	this.subscriberSearch.initidexer();
	    	logger.debug("Exiting on Message");
    	}catch(JMSException e) {
    		e.printStackTrace();
		}
    }

	/**
	 * @param subscriberSearch the subscriberSearch to set
	 */
	public void setSubscriberSearch(USSDSubscriberSearch subscriberSearch) {
		this.subscriberSearch = subscriberSearch;
	}

	 
}
