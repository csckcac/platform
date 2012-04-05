package org.wso2.carbon.registry.indexing.indexer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrException.ErrorCode;
import org.wso2.carbon.registry.indexing.AsyncIndexer.File2Index;
import org.wso2.carbon.registry.indexing.solr.IndexDocument;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XMLIndexer implements Indexer {

	public static final Log log = LogFactory.getLog(XMLIndexer.class);

	public IndexDocument getIndexedDocument(File2Index fileData) throws SolrException {
		try {
			// we register both the content as it is and only text content
			String xmlAsStr = new String(fileData.data);

			final StringBuffer contentOnly = new StringBuffer();
			ByteArrayInputStream inData = new ByteArrayInputStream(fileData.data);

			// this will handle text content
			DefaultHandler handler = new DefaultHandler() {
				public void characters(char ch[], int start, int length)
				throws SAXException {
					contentOnly.append(new String(ch, start, length)).append(" ");
				}
			};
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(inData, handler);

			return new IndexDocument(fileData.path, xmlAsStr,
			                   contentOnly.toString());
		} catch (MalformedURLException e) {
			throw new SolrException(ErrorCode.SERVER_ERROR, "Error at indexing", e);
		} catch (IOException e) {
			throw new SolrException(ErrorCode.SERVER_ERROR, "Error at indexing", e);
		} catch (ParserConfigurationException e) {
			throw new SolrException(ErrorCode.SERVER_ERROR, "Error at indexing", e);
		} catch (SAXException e) {
			throw new SolrException(ErrorCode.SERVER_ERROR, "Error at indexing", e);
		}


	}

}
