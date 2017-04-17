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
package com.ibm.ussd.fsl.utils;

import java.io.File;

import org.apache.lucene.document.Document;

import com.ibm.ussd.fsl.common.ae.SubscriberAE;
import com.ibm.ussd.fsl.indexer.USSDScubscriberIndexUpdate;
import com.ibm.ussd.fsl.indexer.USSDSubscriberIndexer;
import com.ibm.ussd.fsl.indexer.USSDSubscriberSearch;


/**
 * An example of using the PagePanel class to show PDFs. For more advanced
 * usage including navigation and zooming, look at the com.sun.pdfview.PDFViewer class.
 *
 * @author joshua.marinacci@sun.com
 */
public class Main {

	public static void main(String[] args) throws Exception {
		/*USSDSubscriberIndexer indexer = USSDSubscriberIndexer.getInstance();
		indexer.createIndex();*/
		
		
		/*USSDScubscriberIndexUpdate idex = USSDScubscriberIndexUpdate.getInstance();
		
		SubscriberAE subsae = new SubscriberAE();
		subsae.setCountryId("10002");
		subsae.setLangId("3");
		subsae.setMsisdn("9810000003");
		subsae.setSubsId("3");
		
		idex.updateIndex(subsae);
		StringBuffer strBuffer = new StringBuffer();
		strBuffer.append(USSDIndexerProperties.getProperties(USSDIndexerConstants.CORE_FILE_PATH));
		strBuffer.append("/core2");*/
		
	/*	Document document = idex.getUserObject(new File(strBuffer.toString()),subsae.getMsisdn());
		System.out.println(document.get("msisdn"));
		System.out.println(document.get("langid"));*/
		
		USSDSubscriberSearch search = USSDSubscriberSearch.getInstance();
		
		long start = System.currentTimeMillis();
		SubscriberAE searchedAE;
		for(long i=0;i<5000000;i++){
			SubscriberAE subsae = new SubscriberAE();
			subsae.setMsisdn(""+(9810000000L));
			if(i%100000==0){
				System.out.println("Index switch------------------------------");
				search.initidexer();
			}
			searchedAE = search.searchSubscriber(subsae.getMsisdn());
			System.out.println(searchedAE.getMsisdn()+":"+searchedAE.getLangId());
		}
		
		long end = System.currentTimeMillis();
		System.out.println((end-start)/1000);
		
	}

}
    

