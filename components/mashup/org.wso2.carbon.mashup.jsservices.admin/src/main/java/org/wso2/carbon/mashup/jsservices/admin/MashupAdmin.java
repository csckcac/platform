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
package org.wso2.carbon.mashup.jsservices.admin;

import org.apache.axiom.om.util.Base64;
import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisService;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.mashup.utils.MashupConstants;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class MashupAdmin extends AbstractAdmin {

    public Boolean saveMashupServiceSource(String serviceName, String type, String contents)
            throws CarbonException {
        boolean success;

        try {
            contents = new String(Base64.decode(contents));
            AxisService axisService = getAxisConfig().getServiceForActivation(serviceName);

            String contentsFilePath = "";
            if (axisService == null) {
                // New service
                String axis2RepoDirectory = getAxisConfig().getRepository().getPath();

                String[] nameParts = serviceName.split("/");
                String jsServiceDirectory =
                        axis2RepoDirectory + MashupConstants.JS_SERVICE_REPO + File.separator +
                        nameParts[0];

                // create the directory, if it does not exist
                File directory = new File(jsServiceDirectory);
                if (!directory.exists() && !directory.mkdirs()) {
                    throw new CarbonException("Unable to create directory " + directory.getName());
                }

                if ("js".equals(type)) {
                    contentsFilePath = jsServiceDirectory + File.separator + nameParts[1] +
                                       MashupConstants.JS_SERVICE_EXTENSION;
                } else {
                    File resourcesDirectory =
                            new File(jsServiceDirectory, nameParts[1] + ".resources");
                    File webResourcesDirectory = new File(resourcesDirectory, "www");
                    if (!resourcesDirectory.mkdir()) {
                        throw new CarbonException(
                                "Unable to create directory " + resourcesDirectory.getName());
                    }
                    if (!webResourcesDirectory.mkdir()) {
                        throw new CarbonException(
                                "Unable to create directory " + webResourcesDirectory.getName());
                    }

                    if ("html".equals(type)) {
                        contentsFilePath = webResourcesDirectory + File.separator + "index.html";
                    } else if ("gadget".equals(type)) {
                        contentsFilePath = webResourcesDirectory + File.separator + "gadget.xml";
                    }
                }

                File file = new File(contentsFilePath);
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    throw new CarbonException(
                            "Error while creating the file for the new service " + serviceName, e);
                }

            } else {
                if (!"js".equals(type)) {
                    File resourcesDirectory = (File) axisService.
                            getParameterValue(MashupConstants.RESOURCES_FOLDER);
                    File webResourcesDirectory = new File(resourcesDirectory, "www");
                    if (!resourcesDirectory.exists() && !resourcesDirectory.mkdir()) {
                        throw new CarbonException(
                                "Unable to create directory " + resourcesDirectory.getName());
                    }
                    if (!webResourcesDirectory.exists() && !webResourcesDirectory.mkdir()) {
                        throw new CarbonException(
                                "Unable to create directory " + webResourcesDirectory.getName());
                    }

                    if ("html".equals(type)) {
                        contentsFilePath = webResourcesDirectory + File.separator + "index.html";
                    } else if ("gadget".equals(type)) {
                        contentsFilePath = webResourcesDirectory + File.separator + "gadget.xml";
                    }
                    File file = new File(contentsFilePath);
                    if (!file.exists()) {
                        try {
                            file.createNewFile();
                        } catch (IOException e) {
                            throw new CarbonException(
                                    "Error while creating the file for the new service " +
                                    serviceName, e);
                        }
                    }
                } else {
                    contentsFilePath =
                            (String) axisService.getParameterValue(MashupConstants.SERVICE_JS);
                }
            }

            //Writing the file with the source provided
            BufferedWriter out = new BufferedWriter(new FileWriter(contentsFilePath));
            out.write(contents);
            out.flush();
            out.close();
            success = true;

        } catch (AxisFault axisFault) {
            throw new CarbonException("Cannot save service Source", axisFault);
        } catch (IOException e) {
            throw new CarbonException("Cannot save service Source", e);
        }

        return success;
    }

    public String[] getMashupServiceContentAsString(String serviceName) throws AxisFault {

        AxisService axisService = getAxisConfig().getServiceForActivation(serviceName);
        String[] sources = new String[3];
        String filePath;
        String resourceFilePath = null;
        //construct Mashup service configuration file path
        if (axisService != null) {
            filePath = getFilePath((String) axisService.getParameterValue(MashupConstants.SERVICE_JS));
            File resourceFile =
                    (File) axisService.getParameterValue(MashupConstants.RESOURCES_FOLDER);
            if (resourceFile != null) {
                resourceFilePath = getFilePath(resourceFile.getAbsolutePath());
            }
        } else {
            // Service could be a fault one. Loading contents directly from
            // repository
            String axis2RepoDirectory = getAxisConfig().getRepository().getPath();
            String[] nameParts = serviceName.split("/");
            String jsServiceDirectory =
                    axis2RepoDirectory + MashupConstants.JS_SERVICE_REPO + File.separator +
                    nameParts[0];
            resourceFilePath = jsServiceDirectory + File.separator + nameParts[1] + ".resources";
            filePath = jsServiceDirectory + File.separator + nameParts[1] +
                       MashupConstants.JS_SERVICE_EXTENSION;

        }

        //load file content into a string buffer
        if (filePath != null) {
            File config = new File(filePath);
            try {
                FileReader fileReader = new FileReader(config);
                BufferedReader in = new BufferedReader(fileReader);
                String str;
                StringBuffer fileContents = new StringBuffer();
                while ((str = in.readLine()) != null) {
                    fileContents.append(str).append("\n");
                }
                in.close();
                sources[0] = fileContents.toString();
            } catch (IOException e) {
                throw new AxisFault(
                        "Error while reading the contents from the service " + serviceName, e);
            }
        }
        if (resourceFilePath != null) {
            File customUIFile = new File(
                    resourceFilePath + File.separator + "www" + File.separator + "index.html");
            if (customUIFile.exists() && customUIFile.isFile()) {
                try {
                    FileReader fileReader = new FileReader(customUIFile);
                    BufferedReader in = new BufferedReader(fileReader);
                    String str;
                    StringBuffer fileContents = new StringBuffer();
                    while ((str = in.readLine()) != null) {
                        fileContents.append(str).append("\n");
                    }
                    in.close();
                    sources[1] = fileContents.toString();
                } catch (IOException e) {
                    throw new AxisFault(
                            "Error while reading the contents of the custom UI of the service " +
                            serviceName, e);
                }
            } else {
                sources[1] =
                        "A custom UI was not found for this mashup. You can use the 'Generate Template' button below to generate a sample.";
            }
            File gadgetUIFile = new File(
                    resourceFilePath + File.separator + "www" + File.separator + "gadget.xml");
            if (gadgetUIFile.exists() && gadgetUIFile.isFile()) {
                try {
                    FileReader fileReader = new FileReader(gadgetUIFile);
                    BufferedReader in = new BufferedReader(fileReader);
                    String str;
                    StringBuffer fileContents = new StringBuffer();
                    while ((str = in.readLine()) != null) {
                        fileContents.append(str).append("\n");
                    }
                    in.close();
                    sources[2] = fileContents.toString();
                } catch (IOException e) {
                    throw new AxisFault(
                            "Error while reading the contents of the gadget UI of the service " +
                            serviceName, e);
                }
            } else {
                sources[2] =
                        "A gadget UI was not found for this mashup. You can use the 'Generate Template' button below to generate a sample.";
            }
        }
        return sources;
    }

    public boolean doesServiceExists(String serviceName) throws CarbonException {

        AxisService axisService;
        axisService = getAxisConfig().getServiceForActivation(serviceName);
        if (axisService != null) {
            return true;
        }

        // Even if there isnt a service with this name we should check whether there is a file with
        // this service name.
        String axis2RepoDirectory = getAxisConfig().getRepository().getPath();
        String[] nameParts = serviceName.split("/");
        if (nameParts.length != 2) {
            throw new CarbonException("Invalid service name : \"" + serviceName +
                                      "\". Service name should be in the format \"username/mashup\"");
        }
        String jsServiceDirectory =
                axis2RepoDirectory + MashupConstants.JS_SERVICE_REPO + File.separator +
                nameParts[0];
        String filePath = jsServiceDirectory + File.separator + nameParts[1] +
                          MashupConstants.JS_SERVICE_EXTENSION;
        return new File(filePath).exists();
    }

    public String[] doesServicesExists(String[] serviceNames) throws CarbonException {
        ArrayList<String> existingServices = new ArrayList<String>();
        for (String serviceName : serviceNames) {
            if (doesServiceExists(serviceName)) {
                existingServices.add(serviceName);
            }
        }
        return existingServices.toArray(new String[existingServices.size()]);
    }

    public String getBackendHttpPort() throws CarbonException {
        String httpPort = null;
        try {
            httpPort =
                    (String) getAxisConfig().getTransportIn("http").getParameter("port").getValue();
        } catch (Exception e) {
            throw new CarbonException(e);
        }

        return httpPort;
    }

    /**
     * @param filePath Absolute File Path
     * @return Corrected file path relative to CARBON HOME
     */
    private String getFilePath(String filePath) {
        //If file absolute path is different from ms product path
        if (!filePath.contains(CarbonUtils.getCarbonHome().concat("/"))) {
            String latter = filePath.split("/repository")[1];
            filePath = CarbonUtils.getCarbonHome() + "/repository" + latter;
        }
        return filePath;

    }
}
