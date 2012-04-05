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

package org.wso2.carbon.gadget.taglib;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shindig.auth.BlobCrypterSecurityToken;
import org.apache.shindig.common.crypto.BasicBlobCrypter;
import org.apache.shindig.common.crypto.BlobCrypterException;
import org.apache.shindig.common.crypto.Crypto;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.utils.CarbonUtils;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * This Tag Handler class is used to add a gadget to a html/jsp page.
 */
public class GadgetGenerator extends BodyTagSupport {

    private static final Log log = LogFactory.getLog(GadgetGenerator.class);

    //Value to check whether user want to generate the gadget with its title,menu divs or
    //else does user need only the iframe-content
    private boolean applyDefaultStyle = false;
    //URL value,based on which a gadget will displayed.This can be either a url from registry stored
    //gadget[url pattern has to start from /_system/config/] or else a url from external repository.
    private String gadgetUrl;
    //Id of the gadget Eg: gadgetId=2 means 2nd gadget of the page
    private int gadgetId;
    //Title for gadget
    private String gadgetTitle;
    /**
     * Below fields will be useful to create iframe url format,which will pass to shindig when render
     * a gadget.
     */
    //User preferences list for the gadget-with format
    // "'userPrefName1':userPrefValue1,'userPrefName2':userPrefValue2' "
    private String userPrefsList;
    //Container name,which used to render gadgets
    private String container;
    //Owner name-the user logged into the browser[useful in opensocial gadgets]
    private String owner;
    //Viewer name-the user who owns the application[useful in opensocial gadgets]
    private String viewer;
    //Application id-useful in activity streaming gadgets
    private String appId;
    //Gadget View
    private String view;
    //To gadget localization
    private String country;
    //To gadget localization
    private String language;
    //cache busting MD5 of the gadget xml
    private String specVersion;


    /*Below are the getters and setters for the attributes defined in gadget.tld file which are relevant
     to gadget generator tag.lib
    */
    public Boolean getApplyDefaultStyle() {
        return applyDefaultStyle;
    }

    public void setApplyDefaultStyle(Boolean applyDefaultStyle) {
        this.applyDefaultStyle = applyDefaultStyle;
    }

    public String getGadgetUrl() {
        return gadgetUrl;
    }

    public void setGadgetUrl(String gadgetUrl) {
        this.gadgetUrl = gadgetUrl;
    }

    public int getGadgetId() {
        return gadgetId;
    }

    public void setGadgetId(int gadgetId) {
        this.gadgetId = gadgetId;
    }

    public String getGadgetTitle() {
        return gadgetTitle;
    }

    public void setGadgetTitle(String gadgetTitle) {
        this.gadgetTitle = gadgetTitle;
    }

    public void setUserPrefsList(String userPrefsList) {
        this.userPrefsList = userPrefsList;
    }

    public String getUserPrefsList() {
        return userPrefsList;
    }

    public void setContainer(String container) {
        this.container = container;
    }

    public String getContainer() {
        if (container == null) {
            container = "default";
        }
        return container;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getOwner() {
        return owner;
    }

    public void setViewer(String viewer) {
        this.viewer = viewer;
    }

    public String getViewer() {
        return viewer;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppId() {
        return appId;
    }

    public void setView(String view) {
        this.view = view;
    }

    public String getView() {
        if (view == null) {
            view = "default";
        }
        return view;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCountry() {
        if (country == null) {
            country = "ALL";
        }
        return country;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getLanguage() {
        if (language == null) {
            language = "ALL";
        }
        return language;
    }

    public void setSpecVersion(String specVersion) {
        this.specVersion = specVersion;
    }

    public String getSpecVersion() {
        if (specVersion == null) {
            specVersion = "2.0";
        }
        return specVersion;
    }

    /**
     * This method is invoked when the custom end tag is encountered.
     * The main function of this method is to add a gadget to a html/jsp page once an entry
     * with <gadget:gadget> tag added to that html/jsp page. For that first of all you need to import
     * <%@ taglib uri="http://wso2.org/jaggery/bsftags" prefix="gadget" %> to your jsp.
     *
     * @return return EVAL_PAGE - continue processing the page.
     * @throws javax.servlet.jsp.JspException
     */
    public int doEndTag() throws JspException {
        JspWriter writer = pageContext.getOut();
        //File path to mgt-transports.xml file
        String mgtTransportFilePath = CarbonUtils.getCarbonConfigDirPath() + File.separator +
                                      "mgt-transports.xml";

        //Get transport value of management console of the running server.
        String httpsTransport = CarbonUtils.getManagementTransport();
        String httpTransport = "http";

        //Process only if the gadget Url input has a value
        if (getGadgetUrl() != null) {
            ServerConfiguration serverConfiguration = ServerConfiguration.getInstance();
            String iframeURL;
            String registryStoredGadgetUrl = "/_system/config";
            String urlHostWithHTTPSPort = getHostWithPort(mgtTransportFilePath, httpsTransport);
            String urlHostWithHTTPPort = getHostWithPort(mgtTransportFilePath, httpTransport);
            //Creating iframe url
            //If gadget url is a registry url
            if (getGadgetUrl().contains(registryStoredGadgetUrl)) {
                iframeURL = urlHostWithHTTPSPort +
                            serverConfiguration.getFirstProperty("WebContextRoot") + "ifr?url=" +
                            urlHostWithHTTPPort +
                            "/registry/resource" + getGadgetUrl() + getIFrameParamList(urlHostWithHTTPPort);
            } else {//else gadget url is from external gadget repository
                iframeURL = urlHostWithHTTPSPort +
                            serverConfiguration.getFirstProperty("WebContextRoot") + "ifr?url="
                            + getGadgetUrl() + getIFrameParamList(urlHostWithHTTPPort);
            }
            try {
                String displayContext = writeHTMLContent(iframeURL);
                writer.write(displayContext);

            } catch (IOException e) {
                String msg = "Cannot write tag content";
                throw new JspException(msg, e);
            }
        }
        return EVAL_PAGE;

    }

    /**
     * This method is to generate html content of this gadget tag
     *
     * @param iframeURL iframe src value
     * @return html content to show
     */
    private String writeHTMLContent(String iframeURL) {
        String displayContent;
        //Create iframe html view
        String divStartContext = "<div id='gadget_div_"+getGadgetId()+"' class='gadget'>\n<div class='gadget_title'>";

        //Check gadget title needs to append or not
        divStartContext += (getGadgetTitle() != null ? getGadgetTitle() : "");
        //Add default gadget menu bar
        divStartContext += "</div><div class=\"controls\"><a href=\"#\"><img src=\"" +
                           "images/gad_settings.png\" onclick=\"toggleContent('quicktrip_set');\"" +
                           " alt=\"\" /></a><a href=\"#\"><img src=\"images/gad_maximize.png\" " +
                           "alt=\"\" /></a><a href=\"#\"><img src=\"images/gad_close.png\" " +
                           "alt=\"\" /></a>\n" +
                           "<ul id=\"quicktrip_set\" class=\"gad_settings\" " +
                           "onclick=\"toggleContent('quicktrip_set');\">\n" +
                           "<img src=\"images/arrow_exc.png\" class=\"arrow\"/>\n" +
                           "<li><a href=\"#\">maximize this</a></li>\n" +
                           "<li><a href=\"#\">Remove</a></li>\n" +
                           "<li><a href=\"#\">About this gadget</a></li>\n" +
                           "</ul>\n" +
                           "</div>\n" +
                           "<div style=\"clear:both;\"></div><div class='gadget_frame'> \n";
        //Create iframe content including its attributes as src,height,width,scrolling and frameborder
        String iframeContext = "<iframe id='remote_iframe_"+ getGadgetId()+ "' src=\"" + iframeURL + "\"" +
                               "height='auto' width='auto' scrolling='no' frameborder=\"0\">\n" +
                               "</iframe> \n";
        String divEndContext = "</div></div>";

        if (getApplyDefaultStyle()) {
            displayContent = divStartContext + iframeContext + divEndContext;
        } else {
            displayContent = iframeContext;
        }
        return displayContent;
    }

    /**
     * This method is to get query string of a standard iframe url which use to render a gadget from
     * shindig.[according to:https://cwiki.apache.org/confluence/display/SHINDIG/iframe+url+format]
     *
     * @param iframeUrl iframe url value
     * @return iframe query string
     */
    private String getIFrameParamList(String iframeUrl) {
        String iframeParamList = null;
        try {
            int token = (int) (0x7FFFFFFF * Math.random());

            String parentUrl = URLEncoder.encode(iframeUrl, "UTF-8");
            //Append query parameters generally needed while rendering gadgets as its container,
            //country and language using the gadget,view,spec version,parent url,id of gadget.

            iframeParamList = "&container=" + getContainer() + "&country=" + getCountry() + "&lang=" +
                              getLanguage() + "&view=" + getView() + "&v=" + getSpecVersion()
                              + "&parent=" + parentUrl + "&mid=" + getGadgetId();

            //Append query parameters required for opensocial gadgets such as owner,viewer of gadget
            //,application id on which the gadget shown and a security token generated from these
            //opensocial related query parameters to enable oauth support.

            iframeParamList += (getOwner() != null ? "&owner=" + getOwner() : "") +
                               (getViewer() != null ? "&viewer=" + getViewer() : "") +
                               (getAppId() != null ? "&aid=" + getAppId() : "") +
                               (getAppId() != null && getOwner() != null && getViewer() != null ?
                                "&st=" + generateSecurityToken() : "");

            iframeParamList += (getUserPrefsList() != null ?
                                getUserParams(getUserPrefsList()) : "") + "#rpctoken=" + token;

        } catch (UnsupportedEncodingException e) {
            log.error("Error while encoding 'parent' value in gadget iframe url" + e.getMessage());
        }
        return iframeParamList;
    }

    /**
     * This method is to generate a security token from the opensocial parameters as owner,viewer
     * of gadget,application id on which gadget is hosted,url of the gadget and id of the gadget.
     *
     * @return generated security token by using BlobCrypterSecurityToken
     */
    private String generateSecurityToken() {
        BasicBlobCrypter crypter = new BasicBlobCrypter(Crypto.getRandomBytes(20));
        String secTokenParam = null;

        Map<String, String> values = new HashMap<String, String>();
        values.put("appUrl", getGadgetUrl());
        values.put("ownerId", getOwner());
        values.put("appId", getAppId());
        values.put("viewerId", getViewer());
        values.put("moduleId", Integer.toString(getGadgetId()));

        BlobCrypterSecurityToken securityToken = new BlobCrypterSecurityToken(getContainer(),
                                                                              "GS", null, values);
        try {
            secTokenParam = securityToken.getContainer() + ":" + crypter.wrap(securityToken.toMap());
        } catch (BlobCrypterException e) {
            log.error("Error while generating security token." + e.getMessage());
        }
        return secTokenParam;
    }

    /**
     * This method is to get user preference values,which are added by user from the gadget tag.
     * The input for this method has to be in string format as 'prefName1:prefVal1,prefName2:prefVal2'
     * The input get from jsp page,split into name and value pairs and then append those userpref
     * names and values to query string with the prefix "up_"[eg:&up_name1=val1]
     *
     * @param userParamList string source which is to get userpref name,value pairs
     * @return userparams query string value
     */
    private String getUserParams(String userParamList) {
        String userParams = null;
        //If userparam list contains multiple params
        if (userParamList.contains(",")) {
            String[] nameValuePairArrays = userParamList.split(",");
            for (String nameValuePairArray : nameValuePairArrays) {
                String userPrefName = nameValuePairArray.split(":")[0];
                String userPrefValue = nameValuePairArray.split(":")[1];
                userParams += "&up_" + userPrefName + "=" + userPrefValue;
            }
        } else {
            if (userParamList.contains(":")) {
                String userPrefName = userParamList.split(":")[0];
                String userPrefValue = userParamList.split(":")[1];
                userParams = "&up_" + userPrefName + "=" + userPrefValue;
            } else {
                log.error("Error in the input format.It has to be in format of " +
                          "'userPrefName:userPredVal'");
            }
        }
        return userParams;

    }

    /**
     * This method is to get the hostName with port value in-order to create iframe url
     *
     * @param mgtTransportFilePath File path to mgt-transport.xml
     * @param transport            http/https
     * @return HostName with port value
     */
    private String getHostWithPort(String mgtTransportFilePath, String transport) {
        int port;
        //Get hostName[ip-address] of running server.
        String hostName = System.getProperty("carbon.local.ip");
        //Get port value of running server.
        if (transport.equalsIgnoreCase("https")) {
            port = getTransportPort(mgtTransportFilePath, 1);
        } else {
            port = getTransportPort(mgtTransportFilePath, 0);
        }
        return transport + "://" + hostName + ":" + port;
    }

    /**
     * This method is to get the https port value from mgt-transports.xml file.
     *
     * @param mgtTransportsXmlPath file path for mgt-transports.xml
     * @param increment            int value,which use for read items of xml
     * @return port
     */
    private int getTransportPort(String mgtTransportsXmlPath, int increment) {
        int transportPort = -1;
        int parameterPortEntry = 7;
        String port;
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            //Parse mgt-transports.xml file
            Document doc = docBuilder.parse(new File(mgtTransportsXmlPath));
            Node transports = doc.getElementsByTagName("transports").item(0);
            if (transports == null) {
                throw new RuntimeException("Transports element not found");
            }
            Element transportsEle = (Element) transports;
            String elementTagName = "transport";
            if (transportsEle.getElementsByTagName(elementTagName).getLength() > 0) {
                Node transport = transportsEle.getElementsByTagName(elementTagName).item(increment);
                Node transportName = transport.getAttributes().getNamedItem("name");
                if (transportName != null) {
                    Node parameter = transportsEle.getElementsByTagName(elementTagName).item
                            (increment).
                            getChildNodes().item(parameterPortEntry);
                    port = parameter.getFirstChild().getNodeValue().trim();
                    if (port.startsWith("${")) {
                        transportPort = CarbonUtils.getPortFromServerConfig(port);
                    } else {
                        transportPort = Integer.parseInt(port);
                    }

                }
            }
            return transportPort;
        } catch (Throwable t) {
            log.fatal("Failed to parse mgt-transports.xml", t);
            return 0;
        }

    }


}
