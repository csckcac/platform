/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.registry.indexing.solr;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrException.ErrorCode;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.core.CoreContainer;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.indexing.AsyncIndexer.File2Index;
import org.wso2.carbon.registry.indexing.indexer.Indexer;
import org.wso2.carbon.registry.indexing.indexer.IndexerException;
import org.wso2.carbon.utils.CarbonUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class SolrClient {
	
	public static final Log log = LogFactory.getLog(SolrClient.class);

	private static volatile SolrClient instance;
	private SolrServer server;

	protected SolrClient() throws IOException, ParserConfigurationException, SAXException {
        File solrHome = new File(CarbonUtils.getCarbonHome() + File.separator + "repository" +
                File.separator + "conf", "solr");
        if (!solrHome.exists() && !solrHome.mkdirs()) {
            throw new IOException("Solr Home Directory could not be created. Path: " + solrHome);
        }
        File confDir = new File(solrHome, "conf");
        if (!confDir.exists() && !confDir.mkdirs()) {
            throw new IOException("Solf conf directory could not be created! Path: " + confDir);
        }
//		System.out.println(created + confDir.getAbsolutePath());

		String[] filePaths = new String[]{"elevate.xml", "protwords.txt", "schema.xml", 
		                                  "scripts.conf", "solrconfig.xml", "spellings.txt", "stopwords.txt", "synonyms.txt"};

		for (String path:filePaths) {
			InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(path);
			if(resourceAsStream == null){
				throw new SolrException(ErrorCode.NOT_FOUND, "Can not find resource "+ path + " from the classpath");
			}
            File file = new File(confDir, path);
            if (!file.exists()) {
                write2File(resourceAsStream, file);
            }
		}

		System.setProperty("solr.solr.home", solrHome.getPath());
		CoreContainer.Initializer initializer = new CoreContainer.Initializer();
		CoreContainer coreContainer = initializer.initialize();
		this.server = new EmbeddedSolrServer(coreContainer, "");
		//		this.server = new CommonsHttpSolrServer(url);
		//		if(url.startsWith("https")){
		//			//here we use server credentials to setup https
		//			enableSecurity();
		//		}
	}
	
	public static SolrClient getInstance() throws IndexerException {
		if (instance == null) {
            synchronized (SolrClient.class) {
			    try {
	                instance = new SolrClient();
                } catch (Exception e) {
            	    log.error("Could not instantiate Solr client", e);
	                throw new IndexerException("Could not instantiate Solr client", e);
                }
            }
		}
		return instance;
	}
	
	private void write2File(InputStream in, File file) throws IOException{
		byte[] buf = new byte[1024];
//		if (!file.exists()) {
//			file.createNewFile();
//		}
		FileOutputStream out = new FileOutputStream(file);
        try {
            int read = 0;

            while ((read = in.read(buf)) >= 0) {
                out.write(buf, 0, read);
            }
        } finally {
            out.close();
            in.close();
        }
	}
	
	public void enableSecurity() throws Exception{/*
		HttpClient client = ((CommonsHttpSolrServer)server).getHttpClient();
		List authPrefs = new ArrayList(2);
		authPrefs.add(AuthPolicy.BASIC);
		client.getParams().setAuthenticationPreemptive(true);

		String user = "carbonUI";
		String token = user + "_" + System.currentTimeMillis();
		
		ServerConfiguration config = ServerConfiguration.getInstance();
		String alias = config.getFirstProperty(RegistryResources.SecurityManagement.SERVER_PRIMARY_KEYSTORE_KEY_ALIAS);

		SignatureUtil.init();
		byte[] thumbPrint = SignatureUtil.getThumbPrintForAlias(alias);
		
		byte[] signature = SignatureUtil.doSignature(token);
		
		String authHeader = new StringBuffer(Base64.encode(token.getBytes())).append(":").append(Base64.encode(signature))
			.append(":").append(Base64.encode(thumbPrint)).toString();
		

		// This will exclude the NTLM authentication scheme
		client.getParams().setParameter(AuthPolicy.AUTH_SCHEME_PRIORITY,
				authPrefs);

		Credentials creds = new UsernamePasswordCredentials("admin", "admin");
		client.getState().setCredentials(AuthScope.ANY, creds);
		
		ArrayList<Header> headers = new ArrayList<Header>();
		headers.add(new Header("AUTHTOKEN",authHeader));
		client.getHostConfiguration().getParams().setParameter("http.default-headers", headers);

	*/}

//	public void updateIndex(String id, byte[] data)
//			throws SolrException {
//		// we register both the content as it is and only text content
//		String xmlAsStr = new String(data);
//		addDocument(id, xmlAsStr, null);
//	}
	
	private String generateId(int tenantId, String path) {
		return path + "tenantId" + tenantId;
	}

	private void addDocument(IndexDocument indexDoc)
			throws SolrException {
		try {
			String path = indexDoc.getPath();
			String rawContent = indexDoc.getRawContent();
			String contentAsText = indexDoc.getContentAsText();
			int tenantId = indexDoc.getTenantId();
			
			String id = generateId(tenantId, path);
			if (id == null) {
				id = new StringBuffer().append("id").append(
						rawContent.hashCode()).toString();
			}
			SolrInputDocument document = new SolrInputDocument();
			document.addField("id", id, 1.0f);
			//System.out.println(contentString);
			document.addField("text", rawContent, 1.0f);
			document.addField("tenantId", String.valueOf(tenantId));
			
			if (contentAsText != null) {
				document.addField("contentOnly", contentAsText);
			}
			server.add(document);
			UpdateResponse response = server.commit();
			
			if (log.isDebugEnabled()) {
				log.debug("Indexed document "+id + " with "+ response.getStatus());
			}
		} catch (SolrServerException e) {
			throw new SolrException(ErrorCode.SERVER_ERROR, "Error at indexing", e);
		} catch (IOException e) {
			throw new SolrException(ErrorCode.SERVER_ERROR, "Error at indexing", e);
		}
	}

	public void indexDocument(File2Index fileData, Indexer indexer) throws RegistryException {
		IndexDocument doc = indexer.getIndexedDocument(fileData);
		doc.setTenantId(fileData.tenantId);
		addDocument(doc);
	}

	public synchronized void deleteFromIndex(String path, int tenantId)
			throws SolrException {
		try {
			String id = generateId(tenantId, path);
			/*if (id == null) {
				throw new SolrException(ErrorCode.BAD_REQUEST, "ID not found");
			}*/
			server.deleteById(id);
			server.commit();
			if (log.isDebugEnabled()) {
				log.debug("Delete the document "+ id);
			}
		} catch (SolrServerException e) {
			throw new SolrException(ErrorCode.SERVER_ERROR,"Failure at deleting", e);
		} catch (IOException e) {
			throw new SolrException(ErrorCode.SERVER_ERROR,"Failure at deleting", e);
		}
	}
	
	public SolrDocumentList query(String keywords, int tenantId) throws SolrException{
		try {
			SolrQuery query = new SolrQuery(keywords);
            query.setRows(Integer.MAX_VALUE);
            //Solr does not allow to search with special characters ,
            //Therefore this fix allow to contain "-" in super tenant id.
            if(tenantId== MultitenantConstants.SUPER_TENANT_ID){
                query.addFilterQuery("tenantId:" + "\\"+tenantId);
            }else {
                query.addFilterQuery("tenantId:" + tenantId);
            }
			QueryResponse queryresponse = server.query(query);
			return queryresponse.getResults();
		} catch (SolrServerException e) {
			throw new SolrException(ErrorCode.SERVER_ERROR, "Failure at query "+ keywords, e);
		}
	}
	
	public void cleanAllDocuments(){
		try {
			//server.deleteByQuery("ICWS");
			
			
			QueryResponse results = server.query(new SolrQuery("ICWS"));
			SolrDocumentList resultsList = results.getResults();
			
			for(int i =0; i < resultsList.size(); i++) {
				String id = (String)resultsList.get(i).getFieldValue("id");
				UpdateResponse deleteById = server.deleteById(id);
				server.commit();
				log.debug("Deleted ID "+ id + " Status " + deleteById.getStatus());
			}
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
