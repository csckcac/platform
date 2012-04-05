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
package org.wso2.carbon.gauges.ui;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.UUID;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.axis2.AxisFault;
import org.wso2.carbon.gauges.ui.beans.AssociationBean;
import org.wso2.carbon.gauges.ui.registry.core.xsd.Association;

public class ResourceImpactDataProcesssor {

	ResourceImpactAdminClient client;
	public ResourceImpactDataProcesssor(ServletConfig config,
			HttpSession session, HttpServletRequest request) throws AxisFault {
		client =  new ResourceImpactAdminClient(config, session, request);
		
	}
	
	public String getResourceImpactJSONTree(String path) throws RemoteException, RegistryException {
		StringBuilder builder1 = new StringBuilder();
		ArrayList<AssociationBean> associationBeans = getBean(path);
		associationBeans.trimToSize();
		builder1.append(buildOpenJSONForNameandChildren("Resource Impact"));
		if (!associationBeans.isEmpty()) {
			builder1.append(buildOpenJSONForNameandChildren(trimPath(path)));
			int k = 1;
			for (AssociationBean bean : associationBeans) {
				String currentType = bean.getAssociationType();
				builder1.append(buildOpenJSONForNameandChildren(currentType));
				ArrayList<String> paths = bean.getDestinationPaths();
				paths.trimToSize();
				int i = 1;
				for (String destPath : paths) {
					builder1.append(buildJSONwithNoChildren(trimPath(destPath)));

					if (i != paths.size()) {
						builder1.append(" , \n");
					}
					i++;
				}
				builder1.append("\n");
				builder1.append(buildCloseJSONForNameandChildren());
				if (k != associationBeans.size()) {
					builder1.append(" , \n");
				}
				k++;
			}
			builder1.append(buildCloseJSONForNameandChildren());
		} else {
                        builder1.append(buildOpenJSONForNameandChildren("No resource/associations available"));
			builder1.append(buildCloseJSONForNameandChildren());
		}
		builder1.append(buildCloseJSONForNameandChildren());
		return builder1.toString();
	}
	
	private String trimPath(String originalPath) {
		String[] parts = originalPath.split("/");
		return parts[parts.length-1];
	}
	
	
	private ArrayList<AssociationBean> getBean(String path) throws RemoteException, RegistryException {
		Association[] associations = client.getResourceAssociations(path);
		
		ArrayList<String> associationTypeList = new ArrayList<String>();
		ArrayList<AssociationBean> beanList = new ArrayList<AssociationBean>();
		if (associations != null) {
			for (int i = 0; i < associations.length; i++) {
				String currentType = associations[i].getAssociationType();
				String currentDestPath = associations[i].getDestinationPath();
				String currentSourcePath = associations[i].getSourcePath();
				String pathToAdd = (currentDestPath.equals(path)) ? (currentSourcePath) : (currentDestPath);
				if (!associationTypeList.contains(currentType)) {
					associationTypeList.add(currentType);
					AssociationBean assoBean = new AssociationBean();
					assoBean.setAssociationType(currentType);

					assoBean.getDestinationPaths().add(pathToAdd);
					beanList.add(assoBean);
				}
				else {
					for (AssociationBean bean : beanList) {
						if (bean.getAssociationType().equals(currentType)) {
							bean.getDestinationPaths().add(pathToAdd);
						}
					}
				}

			}
		}
		return beanList;
	}
	private String buildCloseJSONForNameandChildren() {
		StringBuilder builder = new StringBuilder();
		builder.append("] } \n");
		return builder.toString();
	}
	
	private String buildOpenJSONForNameandChildren(String name) {
		StringBuilder builder = new StringBuilder();
		builder.append("{ ");
		builder.append(buildJSONforBeanAttributes(UUID.randomUUID().toString(), name, "") + appendChildrenJSON());
		return builder.toString();
	}
	
	private String appendChildrenJSON() {
		return ", \"children\" : [";
	}
	
	
	private String buildJSONforBeanAttributes(String id, String name, String path) {
		StringBuilder builder = new StringBuilder();
		builder.append(buildJSONNameValuePair("id", id) + "," + buildJSONNameValuePair("name", name) + "," + buildJSONNameValuePair("data", "{}"));
		return builder.toString();
	}
	
	private String buildJSONwithNoChildren(String name) {
		StringBuilder builder = new StringBuilder();
		builder.append("{ " + buildJSONforBeanAttributes(UUID.randomUUID().toString(), name, ""));
		builder.append(", " +  buildJSONNameValuePair("children", "[]") + "}");
		return builder.toString();		
	}
	
	private String buildJSONNameValuePair(String name, String value) {
		if (value.contentEquals("[]") || value.contentEquals("{}")) return new String("\"" + name + "\" : " + value + " ");
		return new String("\"" + name + "\" : \"" + value + "\" ");
		
	}
}
