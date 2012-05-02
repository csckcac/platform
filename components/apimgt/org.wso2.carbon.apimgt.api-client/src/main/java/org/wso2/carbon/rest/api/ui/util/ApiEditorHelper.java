/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.rest.api.ui.util;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.wso2.carbon.rest.api.stub.types.carbon.APIData;
import org.wso2.carbon.rest.api.stub.types.carbon.ResourceData;
import org.wso2.carbon.utils.xml.XMLPrettyPrinter;

public class ApiEditorHelper {
	
	public static String parseStringToPrettyfiedString(String ugly){
		ByteArrayInputStream byteArrayInputStream
		= new ByteArrayInputStream(ugly.getBytes());
		XMLPrettyPrinter printer = new XMLPrettyPrinter(byteArrayInputStream);
		return printer.xmlFormat();
	}
	
	public static APIData convertStringToAPIData(String xml) throws XMLStreamException{
		APIData apiData = new APIData();
		
		OMElement apiOM = AXIOMUtil.stringToOM(xml);
		
		OMAttribute name = apiOM.getAttribute(new QName("name"));
		if(name != null){
			apiData.setName(name.getAttributeValue());
		}
		
		OMAttribute context = apiOM.getAttribute(new QName("context"));
		if(context != null){
			apiData.setContext(context.getAttributeValue());
		}
		
		OMAttribute host = apiOM.getAttribute(new QName("host"));
		if(host != null){
			apiData.setHost(host.getAttributeValue());
		}
		
		OMAttribute port = apiOM.getAttribute(new QName("port"));
		if(port != null){
			apiData.setPort(Integer.parseInt(port.getAttributeValue()));
		}
		
		Iterator childIterator = apiOM.getChildElements();
		if(childIterator == null){
			return apiData;
		}
		
		List<ResourceData> resources = new ArrayList<ResourceData>();
		
		while(childIterator.hasNext()){
			OMElement resourceOM = (OMElement)childIterator.next();
			ResourceData resource = new ResourceData();
			
			OMAttribute methods = resourceOM.getAttribute(new QName("methods"));
			if(methods != null){
				resource.setMethods(methods.getAttributeValue().split(" "));
			}
			
			OMAttribute uriTemplate = resourceOM.getAttribute(new QName("uri-template"));
			if(uriTemplate != null){
				resource.setUriTemplate(uriTemplate.getAttributeValue());
			}
			
			OMAttribute urlMapping = resourceOM.getAttribute(new QName("url-mapping"));
			if(urlMapping != null){
				resource.setUrlMapping(urlMapping.getAttributeValue());
			}
			
			OMAttribute contentType = resourceOM.getAttribute(new QName("contentType"));
			if(contentType != null){
				resource.setContentType(contentType.getAttributeValue());
			}
			
			OMAttribute protocol = resourceOM.getAttribute(new QName("protocol"));
			if(protocol != null){
				resource.setProtocol(Integer.parseInt(protocol.getAttributeValue()));
			}
			
			OMAttribute userAgent = resourceOM.getAttribute(new QName("userAgent"));
			if(userAgent != null){
				resource.setUserAgent(userAgent.getAttributeValue());
			}
			
			OMAttribute inSequence = resourceOM.getAttribute(new QName("inSequence"));
			if(inSequence != null){
				resource.setInSequenceKey(inSequence.getAttributeValue());
			}
			
			OMAttribute outSequence = resourceOM.getAttribute(new QName("outSequence"));
			if(outSequence != null){
				resource.setOutSequenceKey(outSequence.getAttributeValue());
			}
			
			OMAttribute faultSequence = resourceOM.getAttribute(new QName("faultSequence"));
			if(faultSequence != null){
				resource.setFaultSequenceKey(faultSequence.getAttributeValue());
			}
			
			resources.add(resource);
		}
		
		ResourceData[] resourceArray = new ResourceData[resources.size()];
		apiData.setResources(resources.toArray(resourceArray));
		
		return apiData;
	}

}
