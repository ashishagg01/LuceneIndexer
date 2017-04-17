package com.ibm.ussd.fsl.messagebeans;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.Session;

import org.apache.log4j.Logger;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

public class IndexerSwitchCoreQueueSender {
	
	/** Reference variable for Logger for this class. */
	private static final Logger logger = Logger.getLogger(IndexerSwitchCoreQueueSender.class.getName());
	
	/** Reference variable for jms template */
	private JmsTemplate jmsTemplate;
	/** Reference variable for queue */
	private Queue queue;
	/**
	 * Method used for sending message		
	 * @param coreName
	 * 			Name of the core to be switched to.
	 */
	public void sendMessage(final String coreName) {
		logger.debug("Entering sendMessage");
		logger.info("SWITCH_CORE"+coreName);
		this.jmsTemplate.send(this.queue, new MessageCreator() {
			// Method of a Anaonymous class.
			public Message createMessage(Session session) throws JMSException {
				// Create a text Message
				Message message  = session.createTextMessage(coreName);
				// Set application specific headers to the properties.
				// 1. Originating address of the message
				message.setStringProperty("SWITCH_CORE",coreName);
				return message;
			}//END of Method - Create Message
		});
		logger.debug("Existing sendMessage");
	}//END of Method - sendMessage
	/**
	 * Setter method for setting the connection factory
	 * @param cf
	 * 			Connection Factory
	 */
	public void setConnectionFactory(ConnectionFactory cf) {
		this.jmsTemplate = new JmsTemplate(cf);
	}//End of Method - setConnectionFactory
	/**
	 * Setter method for setting the queue
	 * @param queue
	 * 			Queue
	 */
	public void setQueue(Queue queue) {
		this.queue = queue;
	}//END OF Method - setQueue
}
