package org.wso2.rnd.nosql;


import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class GetConfigurations {
private String clusterName;
private String clusterHost;
private String keyspace;
private String username;
private String password;
private org.w3c.dom.Document document;
org.w3c.dom.Element element;

/**
 * 
 * @return
 */
public String getClusterName() {
	return clusterName;
}

public GetConfigurations() {
	 parseXMLFile();
	parseDocument();
	
}

/**
 * Parse XML file
 */
private void parseXMLFile() 
{
	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	DocumentBuilder builder;
	try {
		builder = factory.newDocumentBuilder();
		document = builder.parse("~/emr/modules/war/src/main/webapp/WEB-INF/configuration.xml");
	} catch (ParserConfigurationException e1) {
		e1.printStackTrace();
	} catch (SAXException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
}

/**
 * Parsing document
 */
private void parseDocument()
{
	element = document.getDocumentElement();
	setClusterHost(getTextValue(element,"cluster_host"));
	setClusterName(getTextValue(element,"cluster_name"));
	setKeyspace(getTextValue(element, "keyspace"));
	setUsername(getTextValue(element, "username"));
	setPassword(getTextValue(element, "password"));
}

/**
 * Get text value of a element
 * @param element
 * @param tagName
 * @return
 */
private String getTextValue(org.w3c.dom.Element element, String tagName)
{
	String textValue = null;
	NodeList nodelist = element.getElementsByTagName(tagName);
	if(nodelist != null && nodelist.getLength() > 0) {
		org.w3c.dom.Element element1 = (Element) nodelist.item(0);
		textValue = element1.getFirstChild().getNodeValue();
	}
	return textValue;
}

/**
 * 
 * @param clusterName
 */
public void setClusterName(String clusterName) {
	this.clusterName = clusterName;
}

/**
 * 
 * @return
 */
public String getClusterHost() {
	return clusterHost;
}

/**
 * 
 * @param clusterHost
 */
public void setClusterHost(String clusterHost) {
	this.clusterHost = clusterHost;
}

/**
 * 
 * @return
 */
public String getKeyspace() {
	return keyspace;
}

/**
 * 
 * @param keyspace
 */
public void setKeyspace(String keyspace) {
	this.keyspace = keyspace;
}

/**
 * 
 * @return
 */
public String getUsername() {
	return username;
}

/**
 * 
 * @param username
 */
public void setUsername(String username) {
	this.username = username;
}

/**
 * 
 * @return
 */
public String getPassword() {
	return password;
}

/**
 * 
 * @param password
 */
public void setPassword(String password) {
	this.password = password;
}

}
