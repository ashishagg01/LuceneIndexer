/**
-----------------------------------------------------------------------------
	PROJECT			:	
	MODULE			:	
	CLASS NAME		:	
	DESCRIPTION		: 	
	Copyright (C) 2012 IBM Global Services
    ALL RIGHTS RESERVED
-----------------------------------------------------------------------------
 */
package com.ibm.ussd.fsl.indexer;

import java.io.File;
import java.io.RandomAccessFile;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.util.Version;

import com.ibm.ussd.fsl.common.ae.SubscriberAE;
import com.ibm.ussd.fsl.utils.USSDIndexerConstants;
import com.ibm.ussd.fsl.utils.USSDIndexerProperties;
import com.ibm.ussd.menu.fsl.dao.utils.Encryption;
import com.ibm.ussd.menu.fsl.dao.utils.IEncryption;

/**
 * @author vikram
 *
 */
public class USSDSubscriberSearch {

	/** Reference variable for Logger for this class. */
	private static final Logger logger = Logger.getLogger(USSDSubscriberSearch.class.getName());
	
	/** Reference variable for the instance of the class */
	private static final USSDSubscriberSearch INSTANCE = new USSDSubscriberSearch();
	
	/** Reference variable for the core to be used for susbscriber
	 * search
	 **/
	private String strCore;
	
	/** Reference variable for analyzer	 */
	private Analyzer analyzer;
	
	/** Reference variable for index reader */
	private IndexReader indexReader;
	
	private IndexSearcher searcher;
	
	//private QueryParser queryParser;
	
	private USSDSubscriberSearch(){
		initidexer();
	}
	
	public static USSDSubscriberSearch getInstance(){
		return INSTANCE;
	}

	/**
	 * Method is used for searching the subscriber inside the B Tree
	 * @param msisdn  
	 * 			MSISDN of the subscriber
	 * @return
	 * @throws Exception
	 */
	public SubscriberAE searchSubscriber(String msisdn) throws Exception{
		logger.debug("Entering searchSubscriber");
		SubscriberAE subsAe = null;
		Document document = searchDocumentIndex(msisdn);
		if(document != null){
			subsAe = new SubscriberAE();
			subsAe.setMsisdn(msisdn);
			subsAe.setLangId(document.get("langid"));
			subsAe.setSubsId(document.get("subsid"));
			subsAe.setCountryId(document.get("countryid"));
		}
		document = null;
		logger.debug("Exiting searchSubscriber");
		return subsAe;
	}

	private Document searchDocumentIndex(String msisdn) throws Exception{
		logger.debug("Entering searchDocumentIndex");
		Document document = null;
		try{
			QueryParser queryParser = new QueryParser(Version.LUCENE_36,"msisdn",analyzer);
			// Search for emails that contain the words 'job openings' and '.net' and 'pune'
			logger.debug("Query PArser " + queryParser);
			logger.debug("MSISDN " + msisdn);
			Query query = queryParser.parse(msisdn);
			logger.debug("Searcher " + searcher);
			TopDocs topDocs = searcher.search(query,20);
			
			logger.debug(" Hit count for :"+msisdn+" :>"+topDocs.totalHits);
			if(topDocs.totalHits>0){
				document = indexReader.document(topDocs.scoreDocs[0].doc);
			}else{
				logger.info(" Hit count for :"+msisdn+" :>"+topDocs.totalHits);
			}
			query = null;
			queryParser = null;
			logger.debug("Exiting searchDocumentIndex");
		}catch(Exception oException){
			logger.error("Error", oException);
			throw oException;
		}
		return document;
	}



	/**
	 * Method is used for checking the last entry of the lock file
	 * @throws Exception
	 */
	private String checkLockFile() throws Exception {
		logger.debug("Entering checkLockFile");
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
		//SWAP the core
		String coreName = USSDIndexerProperties
		.getProperties(USSDIndexerConstants.CORE_FILE_NAME1);

		// SWAP the core
		if (lockCore != null
			&& (!coreName.equals(lockCore))) {
		lockCore = coreName;
		} else {
		lockCore = USSDIndexerProperties
		.getProperties(USSDIndexerConstants.CORE_FILE_NAME2);
		}
		logger.debug("Exiting checkLockFile");
		return lockCore;
	}//END OF Method - createLockFile


	public synchronized void initidexer(){
		try{
			logger.debug("Entering initidexer");
			strCore = checkLockFile();
			logger.debug("Core To be Searched for :"+strCore);
			StringBuffer strBuffer = new StringBuffer();
			strBuffer.append(USSDIndexerProperties.getProperties(USSDIndexerConstants.CORE_FILE_PATH));
			strBuffer.append("/");
			strBuffer.append(strCore);
			logger.info("Store Path:"+strBuffer.toString());
			FSDirectory fsDirectory =  NIOFSDirectory.open(new File(strBuffer.toString()));
			indexReader =IndexReader.open(fsDirectory);
			searcher = new IndexSearcher(indexReader);
			analyzer =  new StandardAnalyzer(Version.LUCENE_36);
			
			logger.debug("Exiting initidexer");
		}catch(Exception oException){
			
		}
	}
	
}
