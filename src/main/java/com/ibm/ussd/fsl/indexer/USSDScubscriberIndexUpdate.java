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
import java.io.FileReader;
import java.io.RandomAccessFile;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
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
public class USSDScubscriberIndexUpdate {

	/** Instance of the logger */
	private static final Logger logger = Logger
			.getLogger(USSDScubscriberIndexUpdate.class.getName());

	/** Reference variable for the instance of the Index Update */
	private static final USSDScubscriberIndexUpdate INSTANCE = new USSDScubscriberIndexUpdate();

	/** Reference variable for the core to be used for susbscriber
	 * search
	 **/
	private String strCore;
	
	private IndexWriter writer;
	
	private IndexSearcher searcher;
	
	private int counter=0;
	
	/**
	 * Default constructor
	 */
	private USSDScubscriberIndexUpdate() {
		//initidexer();
	}

	public static USSDScubscriberIndexUpdate getInstance() {
		return INSTANCE;
	}

	public void updateIndex(SubscriberAE ae) throws Exception {
		logger.debug("Entering updateIndex");
		//if(writer == null){
			initidexer();
		//}
		counter++;
		// 2. Modify index in core.
		modifiyIndex(strCore, ae);
		counter++;
		if (searcher != null) {
			searcher.close();
		}
		if (writer != null) {
			/*writer.setMaxMergeDocs(20);
			writer.optimize();*/
			writer.close();
			writer = null;
		}

		USSDSubscriberSearch.getInstance().initidexer();
	
		
		logger.debug("Existing updateIndex");
	}

	private void modifiyIndex(String strCore, SubscriberAE ae) throws Exception {
			logger.debug("Entering modifiyIndex");
			long startTime = System.currentTimeMillis();
			Term idTerm = new Term("msisdn", ae.getMsisdn());
			TermQuery query = new TermQuery(idTerm);
			TopDocs hits = searcher.search(query, 10);

			if (hits.scoreDocs.length != 0) {
				int docId = hits.scoreDocs[0].doc;
				//writer.deleteDocuments(idTerm);
				// retrieve the old document
				Document oldDocument = searcher.doc(docId);
				writer.deleteDocuments(idTerm);
				
				Document docObj=null; 
				// Create new document 
				docObj = new Document(); 
				// Create new field to be added to document 
				Field field = new Field("msisdn",ae.getMsisdn(),Field.Store.YES, Field.Index.ANALYZED); 
				docObj.add(field); 
				// Create new field to be added to document 
				field = new Field("subsid",ae.getSubsId(),Field.Store.YES,Field.Index.ANALYZED); 
				docObj.add(field); 
				// Create new field to be added to document 
				field = new Field("langid",ae.getLangId(),Field.Store.YES, Field.Index.ANALYZED); 
				docObj.add(field); 
				// Create new field to be added to document 
				field = new Field("countryid",ae.getCountryId(),Field.Store.YES,Field.Index.ANALYZED);
				docObj.add(field);
				writer.addDocument(docObj);
				//writer.commit();
				long endTime = System.currentTimeMillis();
				//System.out.println(endTime-startTime);
			}else{
				Document docObj=null; 
				// Create new document 
				docObj = new Document(); 
				// Create new field to be added to document 
				Field field = new Field("msisdn",ae.getMsisdn(),Field.Store.YES, Field.Index.ANALYZED); 
				docObj.add(field); 
				// Create new field to be added to document 
				field = new Field("subsid",ae.getSubsId(),Field.Store.YES,Field.Index.ANALYZED); 
				docObj.add(field); 
				// Create new field to be added to document 
				field = new Field("langid",ae.getLangId(),Field.Store.YES, Field.Index.ANALYZED); 
				docObj.add(field); 
				// Create new field to be added to document 
				field = new Field("countryid",ae.getCountryId(),Field.Store.YES,Field.Index.ANALYZED);
				docObj.add(field);
				writer.addDocument(docObj);
				//writer.commit();
			}

		logger.debug("Existing modifiyIndex");
	}


	/**
	 * Method is used for checking the last entry of the lock file
	 * 
	 * @throws Exception
	 */
	private String checkLockFile() throws Exception {
		logger.debug("Entering checkLockFile");
		RandomAccessFile randomAccessFile = null;
		String lockCore = null;
		IEncryption encrpt = new Encryption();
		StringBuffer strBuffer = new StringBuffer();
		strBuffer.append(USSDIndexerProperties
				.getProperties(USSDIndexerConstants.LOCK_FILE_PATH));
		strBuffer.append("/");
		strBuffer.append(USSDIndexerProperties
				.getProperties(USSDIndexerConstants.LOCK_FILE_NAME));
		logger.info("Lock File Path"+strBuffer.toString());
		try{
			// Lock file contains the file which is being updated
			File file = new File(strBuffer.toString());
			if (file.exists()) {
				randomAccessFile = new RandomAccessFile(file,"r");
				lockCore = randomAccessFile.readLine();
				lockCore = encrpt.decrypt(lockCore);
				if (lockCore == null) {
					throw new Exception("Lock File not containing entry");
				}
				
			}// END IF
		}finally{
			try{
				if(randomAccessFile!= null)
					randomAccessFile.close();
			}catch(Exception oException){
				logger.error("Exception", oException);
			}
		}
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
		//System.out.println(lockCore);
		logger.info("Core to be used:" + lockCore);

		logger.debug("Existing checkLockFile");
		return lockCore;
	}// END OF Method - createLockFile

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
			File indexDir = new File(strBuffer.toString());
			Directory fsDirectory = NIOFSDirectory.open(indexDir);
			writer = new IndexWriter(fsDirectory, new StandardAnalyzer(
					Version.LUCENE_36), false,
					IndexWriter.MaxFieldLength.UNLIMITED);
			//writer.setMergeFactor(50)
			searcher = new IndexSearcher(writer.getDirectory());
			
			logger.debug("Exiting initidexer");
		}catch(Exception oException){
			oException.printStackTrace();
		}
	}

	/**
	 * @param searcher the searcher to set
	 */
	public void setSearcher(IndexSearcher searcher) {
		this.searcher = searcher;
	}
	
}
