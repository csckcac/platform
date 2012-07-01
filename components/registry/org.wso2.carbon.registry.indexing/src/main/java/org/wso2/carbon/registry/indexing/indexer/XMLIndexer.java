package org.wso2.carbon.registry.indexing.indexer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.common.SolrException;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.indexing.AsyncIndexer.File2Index;
import org.wso2.carbon.registry.indexing.solr.IndexDocument;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.ByteArrayInputStream;

public class XMLIndexer implements Indexer {

	public static final Log log = LogFactory.getLog(XMLIndexer.class);

	public IndexDocument getIndexedDocument(File2Index fileData) throws SolrException, RegistryException {
        // we register both the content as it is and only text content
        String xmlAsStr = RegistryUtils.decodeBytes(fileData.data);

        final StringBuffer contentOnly = new StringBuffer();
        ByteArrayInputStream inData = new ByteArrayInputStream(fileData.data);

        // this will handle text content
        DefaultHandler handler = new DefaultHandler() {
            public void characters(char ch[], int start, int length)
            throws SAXException {
                contentOnly.append(new String(ch, start, length)).append(" ");
            }
        };
//			SAXParserFactory factory = SAXParserFactory.newInstance();
//			SAXParser saxParser = factory.newSAXParser();
//			saxParser.parse(inData, handler);

        return new IndexDocument(fileData.path, xmlAsStr,
                           contentOnly.toString());


    }

}
