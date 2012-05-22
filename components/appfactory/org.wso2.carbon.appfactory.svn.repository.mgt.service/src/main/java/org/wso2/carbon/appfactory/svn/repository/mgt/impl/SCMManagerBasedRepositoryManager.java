/*
 * Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.wso2.carbon.appfactory.svn.repository.mgt.impl;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.svn.repository.mgt.RepositoryMgtException;
import org.wso2.carbon.appfactory.svn.repository.mgt.beans.Permission;
import org.wso2.carbon.appfactory.svn.repository.mgt.beans.PermissionType;
import org.wso2.carbon.appfactory.svn.repository.mgt.beans.Repository;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * SCM-manager specific repository manager implementation
 */
public class SCMManagerBasedRepositoryManager extends AbstractRepositoryManager {
    private static final Log log = LogFactory.getLog(SCMManagerBasedRepositoryManager.class);
    //rest URIs
    private static final String REST_BASE_URI = "/scm/api/rest";
    private static final String REST_CREATE_REPOSITORY_URI = "/repositories";
    private static final String REST_GET_REPOSITORY_URI = "/repositories/svn/";

    //repositories xml elements
    private static final String REPOSITORY_XML_ROOT_ELEMENT = "repositories";
    private static final String REPOSITORY_NAME_ELEMENT = "name";
    private static final String REPOSITORY_TYPE_ELEMENT = "type";
    private static final String REPOSITORY_URL_ELEMENT = "url";

    //permission xml elements
    private static final String PERMISSION_XML_ROOT_ELEMENT = "permissions";
    private static final String PERMISSION_TYPE_ELEMENT = "type";
    private static final String PERMISSION_NAME_ELEMENT = "name";
    private static final String PERMISSION_GROUP_PERMISSION_ELEMENT = "groupPermission";
    private AppFactoryConfiguration configuration;

    @Override
    public String createRepository(String projectKey) throws RepositoryMgtException {

        HttpClient client = getClient(configuration);
        PostMethod post = new PostMethod(getServerURL(configuration) + REST_BASE_URI +
                                         REST_CREATE_REPOSITORY_URI);
        Repository repository = new Repository();
        repository.setName(projectKey);
        repository.setType("svn");

        Permission permission = new Permission();
        permission.setGroupPermission(true);
        permission.setName(projectKey);
        permission.setType(PermissionType.WRITE);
        ArrayList<Permission> permissions = new ArrayList<Permission>();
        permissions.add(permission);

        repository.setPermissions(permissions);


        post.setRequestEntity(new ByteArrayRequestEntity(getRepositoryAsString(repository)));
        post.setDoAuthentication(true);
        post.addRequestHeader("Content-Type", "application/xml;charset=UTF-8");

        String url = null;
        try {
            client.executeMethod(post);
        } catch (IOException e) {
            String msg = "Error while invoking the web service";
            log.error(msg, e);
            throw new RepositoryMgtException(msg, e);
        } finally {
            HttpConnectionManager manager = client.getHttpConnectionManager();
            if (manager instanceof SimpleHttpConnectionManager) {
                ((SimpleHttpConnectionManager) manager).shutdown();
            }
        }
        if (post.getStatusCode() == HttpStatus.SC_CREATED) {
            url = getURL(projectKey);
        } else {
            String msg = "Repository creation is failed for " + projectKey + " server returned status " +
                         post.getStatusText();
            log.error(msg);
            throw new RepositoryMgtException(msg);
        }
        return url;
    }

    private byte[] getRepositoryAsString(Repository repo) throws RepositoryMgtException {
        OMFactory factory = OMAbstractFactory.getOMFactory();

        OMElement repository = factory.createOMElement(REPOSITORY_XML_ROOT_ELEMENT, null);

        OMElement name = factory.createOMElement(REPOSITORY_NAME_ELEMENT, null);
        name.setText(repo.getName());
        repository.addChild(name);
        OMElement type = factory.createOMElement(REPOSITORY_TYPE_ELEMENT, null);
        type.setText(repo.getType());
        repository.addChild(type);

        for (Permission perm : repo.getPermissions()) {


            OMElement permission = factory.createOMElement(PERMISSION_XML_ROOT_ELEMENT, null);
            OMElement groupPermission = factory.createOMElement(PERMISSION_GROUP_PERMISSION_ELEMENT, null);
            groupPermission.setText(String.valueOf(perm.getGroupPermission()));
            permission.addChild(groupPermission);

            OMElement permName = factory.createOMElement(PERMISSION_NAME_ELEMENT, null);
            permName.setText(perm.getName());
            OMElement permType = factory.createOMElement(PERMISSION_TYPE_ELEMENT, null);
            permType.setText(perm.getType().toString());

            permission.addChild(permName);
            permission.addChild(permType);
            repository.addChild(permission);
        }
        StringWriter writer = new StringWriter();
        try {
            repository.serialize(writer);
        } catch (XMLStreamException e) {
            String msg = "Error while serializing the payload";
            log.error(msg, e);
            throw new RepositoryMgtException(msg, e);
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                String msg = "Error while closing the reader";
                log.error(msg, e);

            }
        }

        return writer.toString().getBytes();


    }

    @Override
    public String getURL(String projectKey) throws RepositoryMgtException {

        HttpClient client = getClient(configuration);
        GetMethod get = new GetMethod(getServerURL(configuration) + REST_BASE_URI + REST_GET_REPOSITORY_URI
                                      + projectKey);
        get.setDoAuthentication(true);
        get.addRequestHeader("Content-Type", "application/xml;charset=UTF-8");
        String repository = null;
        try {
            client.executeMethod(get);
            repository = getRepositoryFromStream(get.getResponseBodyAsStream()).getUrl();
        } catch (IOException e) {
            String msg = "Error while invoking the service";
            log.error(msg, e);
            throw new RepositoryMgtException(msg, e);
        } finally {
            HttpConnectionManager manager = client.getHttpConnectionManager();
            if (manager instanceof SimpleHttpConnectionManager) {
                ((SimpleHttpConnectionManager) manager).shutdown();
            }
        }

        return repository;
    }

    @Override
    public void setConfig(AppFactoryConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public AppFactoryConfiguration getConfig() {
        return configuration;
    }

    private Repository getRepositoryFromStream(InputStream responseBodyAsStream)
            throws RepositoryMgtException {
        XMLInputFactory xif = XMLInputFactory.newInstance();
        XMLStreamReader reader = null;
        Repository repository = new Repository();
        try {
            reader = xif.createXMLStreamReader(responseBodyAsStream);
            StAXOMBuilder builder = new StAXOMBuilder(reader);
            OMElement rootElement = builder.getDocumentElement();
            if (REPOSITORY_XML_ROOT_ELEMENT.equals(rootElement.getLocalName())) {
                Iterator elements = rootElement.getChildElements();
                while (elements.hasNext()) {
                    Object object = elements.next();
                    if (object instanceof OMElement) {
                        OMElement element = (OMElement) object;
                        if (REPOSITORY_URL_ELEMENT.equals(element.getLocalName())) {
                            repository.setUrl(element.getText());
                            break;
                        }
                    }

                }
            } else {
                String msg = "In the payload no repository information is found";
                log.error(msg);
                throw new RepositoryMgtException(msg);
            }
        } catch (XMLStreamException e) {
            String msg = "Error while reading the stream";
            log.error(msg, e);
            throw new RepositoryMgtException(msg, e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (XMLStreamException e) {
                String msg = "Error while serializing the payload";
                log.error(msg, e);

            }
        }


        return repository;
    }
}
