/**
-----------------------------------------------------------------------------
	PROJECT			:	USSD
	MODULE			:	USSD INDEXER
	CLASS NAME		:	USSDSubscriberIndexer
	DESCRIPTION		: 	This class is used for creating the index file of the
						customer. The class will be singleton in nature as 
						there cannot be more than one writer at any time.
	Copyright (C) 2012 IBM Global Services
    ALL RIGHTS RESERVED
-----------------------------------------------------------------------------
 */
package com.ibm.ussd.fsl.indexer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.util.Version;

import com.ibm.ussd.fsl.utils.USSDIndexerConstants;
import com.ibm.ussd.fsl.utils.USSDIndexerProperties;
import com.ibm.ussd.menu.fsl.dao.connection.impl.DBConnectionManager;
import com.ibm.ussd.menu.fsl.dao.utils.Encryption;
import com.ibm.ussd.menu.fsl.dao.utils.IEncryption;
import com.ibm.ussd.menu.fsl.dao.utils.USSDDAOConstants;
import com.ibm.ussd.menu.fsl.dao.utils.USSDDAOProperties;

/**
 * @author vikram
 *
 */
public class USSDSubscriberIndexer implements USSDSubscriberIndexerInterface{

	/** Instance of the logger */
	private static final Logger logger = Logger.getLogger(USSDSubscriberIndexer.class.getName());
	
	/** Reference variable for creating the subscriber index */
	private static final USSDSubscriberIndexer INSTANCE = new  USSDSubscriberIndexer();

	/** Default constructor */
	private USSDSubscriberIndexer(){

	}
	/**
	 * Method is used for getting the instance of the class
	 * @return
	 */
	public static USSDSubscriberIndexer getInstance(){
		return INSTANCE;
	}

	/**
	 * Method is used for creating the index
	 * @throws Exception
	 */
	public String createIndex() throws Exception{
		logger.debug("Entering Create Index ");
		// 1. Check the lock file to be updated to
		String strCreateCore = checkLockFile();
		logger.info("Lock File to be Created in core:"+strCreateCore);
		// 2. Create the Index file from database
		generateIndexes(strCreateCore);
		logger.info("Indexes Generated");
		// 3. Update/Create the lock file once index file created.
		touchLockFile(strCreateCore);
		logger.info("Lock file touched and updated");
		logger.debug("Exiting Create Index ");
		return strCreateCore;
		// Put message in JMS queue
	}

	/**
	 * Method is used for generating the indexes
	 * @param strCore
	 * @throws IOException
	 * @throws Exception
	 */
	private void generateIndexes(String strCore) throws IOException,Exception{
		logger.debug("Entering generateIndexes ");
		IndexWriter writer=null;
		try{
			StringBuffer strBuffer = new StringBuffer();
			strBuffer.append(USSDIndexerProperties.getProperties(USSDIndexerConstants.CORE_FILE_PATH));
			strBuffer.append("/");
			strBuffer.append(strCore);
			logger.info("Corde file path:"+strBuffer);
			
			File indexDir = new File(strBuffer.toString());
			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_36);
			
			Directory fsDirectory =  NIOFSDirectory.open(indexDir);
			IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_36,analyzer);
			conf.setOpenMode(OpenMode.CREATE);
			writer = new IndexWriter(fsDirectory,conf);
			
			indexSubscriberInfo(writer);
			
			long numDocsIndexed = writer.numDocs();
			logger.info("Number of Docs Indexed:"+numDocsIndexed);
		}finally{	
			if(writer!= null){
				writer.optimize();
				writer.close();
			}	
		}	
		logger.debug("Existing generateIndexes ");
	}

	/**
	 * Method is used for getting and creating index for all the subscribers
	 * stored in database.
	 * @param pIndexWriter
	 * @throws Exception
	 */
	private void indexSubscriberInfo(IndexWriter pIndexWriter) throws Exception{
		logger.debug("Entering indexSubscriberInfo ");
		Connection connection = null;
		ResultSet resultSet = null;
		PreparedStatement pstmt = null;
		try{
			// Get the connection object
			connection = DBConnectionManager.getDBConnection();
			logger.info("Connection Created"+connection);
			pstmt = connection.prepareStatement(
										USSDDAOProperties.getProperties(
													USSDDAOConstants.SUBSCRIBER_INFO
												)
											);
			resultSet = pstmt.executeQuery();
			Document document = null;
			
			while(resultSet.next()){
				document = createDocument(resultSet);
				pIndexWriter.addDocument(document);
			}
			
		}finally{
			DBConnectionManager.releaseResources(pstmt, resultSet);
			DBConnectionManager.releaseResources(connection);
			logger.info("Connection Released");
		}
		logger.debug("Existing indexSubscriberInfo ");
	}
	
	/**	
	 * Method is used for creating the documents from the result set
	 * @param resultSet
	 * @return
	 * @throws Exception
	 */
	private Document createDocument(ResultSet resultSet) throws Exception{
		Document docObj=null;
		// Create new document
		docObj = new Document();
		// Create new field to be added to document
		Field field = new Field("msisdn",resultSet.getString("MSISDN"),Field.Store.YES, Field.Index.ANALYZED);
		docObj.add(field);
		// Create new field to be added to document
		field = new Field("subsid",resultSet.getString("SUBS_ID"),Field.Store.YES, Field.Index.NO );
		docObj.add(field);
		// Create new field to be added to document
		field = new Field("langid",resultSet.getString("LANG_ID"),Field.Store.YES, Field.Index.NO );
		docObj.add(field);
		// Create new field to be added to document
		field = new Field("countryid",resultSet.getString("COUNTRY_ID"),Field.Store.YES, Field.Index.NO);
		docObj.add(field);
		
		return docObj;
	} 


	/**
	 * Method is used for checking the last entry of the lock file
	 * @throws Exception
	 */
	private String checkLockFile() throws Exception {
		logger.debug("Entering checkLockFile ");
		RandomAccessFile bufferedReader = null;
		String lockCore = null;
		IEncryption encrpt = new Encryption();
		StringBuffer strBuffer = new StringBuffer();
		strBuffer.append(USSDIndexerProperties.getProperties(USSDIndexerConstants.LOCK_FILE_PATH));
		strBuffer.append("/");
		strBuffer.append(USSDIndexerProperties.getProperties(USSDIndexerConstants.LOCK_FILE_NAME));
		logger.info("Lock File Path"+strBuffer.toString());
		try{
			// Lock file contains the file which is being updated 
			File file = new File(strBuffer.toString());
			if(file.exists()){
				bufferedReader = new RandomAccessFile(file,"r");
				lockCore = bufferedReader.readLine();
				lockCore = encrpt.decrypt(lockCore);
				if(lockCore==null){
					logger.error("Lock File not containing entry");
					throw new Exception("Lock File not containing entry");
				}
			}//END IF
		}finally{
			try{
				if(bufferedReader!= null)
					bufferedReader.close();
			}catch(Exception oException){
				logger.error("Exception",oException);
			}
		}
		//SWAP the core
		String coreName = USSDIndexerProperties
		.getProperties(USSDIndexerConstants.CORE_FILE_NAME1);
		logger.info("LockCore"+lockCore);
		logger.info("CoreName"+coreName);
		// SWAP the core
		if (lockCore != null && (!coreName.equals(lockCore))) {
		lockCore = coreName;
		} else {
		lockCore = USSDIndexerProperties
		.getProperties(USSDIndexerConstants.CORE_FILE_NAME2);
		}
		logger.debug("Existing checkLockFile ");
		return lockCore;
	}//END OF Method - createLockFile

	/**
	 * Method is used for touching the lock file
	 * @throws Exception
	 */
	private void touchLockFile(String lockCore) throws Exception{
		logger.debug("Entering touchLockFile ");
		StringBuffer strBuffer = new StringBuffer();
		strBuffer.append(USSDIndexerProperties.getProperties(USSDIndexerConstants.LOCK_FILE_PATH));
		strBuffer.append("/");
		strBuffer.append(USSDIndexerProperties.getProperties(USSDIndexerConstants.LOCK_FILE_NAME));
		
		File file = new File(strBuffer.toString());
		IEncryption encrpt = new Encryption();
		// Create the buffered writer
		BufferedWriter lockFile = new BufferedWriter(
				new FileWriter(file, false)
		);
		lockFile.write(encrpt.encrypt(lockCore));
		lockFile.flush();
		lockFile.close();
		logger.debug("Existing touchLockFile ");
	}//END of Method - touchLockFile

}
