package org.wso2.carbon.registry.indexing.indexer;

import org.apache.solr.common.SolrException;
import org.wso2.carbon.registry.indexing.AsyncIndexer.File2Index;
import org.wso2.carbon.registry.indexing.solr.IndexDocument;

public class PlainTextIndexer implements Indexer {

	public IndexDocument getIndexedDocument(File2Index fileData) throws SolrException {
		return new IndexDocument(fileData.path, new String(fileData.data), null);
	}

}
