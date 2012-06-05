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
package org.wso2.carbon.identity.authenticator.saml2.sso.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.c14n.Canonicalizer;
import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.Response;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.XMLObjectBuilder;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallerFactory;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.security.x509.X509Credential;
import org.opensaml.xml.signature.KeyInfo;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.Signer;
import org.opensaml.xml.signature.X509Certificate;
import org.opensaml.xml.signature.X509Data;
import org.opensaml.xml.util.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.wso2.carbon.core.security.AuthenticatorsConfiguration;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.cert.CertificateEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * This class contains all the utility methods required by SAML2 SSO Authenticator module.
 */
public class Util {
    private static boolean bootStrapped = false;
    private static Log log = LogFactory.getLog(Util.class);
    private static Random random = new Random();
    private static final char[] charMapping = {
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o',
            'p'};
    private static String serviceProviderId = null;
    private static String identityProviderSSOServiceURL = null;
    private static String loginPage = "/carbon/admin/login.jsp";
    private static String landingPage = null;

    /**
     * Constructing the XMLObject Object from a String
     *
     * @param authReqStr
     * @return Corresponding XMLObject which is a SAML2 object
     * @throws org.wso2.carbon.identity.authenticator.saml2.sso.ui.SAML2SSOUIAuthenticatorException
     */
    public static XMLObject unmarshall(String authReqStr) throws SAML2SSOUIAuthenticatorException {
        try {
            doBootstrap();
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            DocumentBuilder docBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = docBuilder.parse(new ByteArrayInputStream(authReqStr.trim().getBytes()));
            Element element = document.getDocumentElement();
            UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
            Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(element);
            return unmarshaller.unmarshall(element);
        } catch (Exception e) {
            log.error("Error in constructing AuthRequest from the encoded String", e);
            throw new SAML2SSOUIAuthenticatorException("Error in constructing AuthRequest from " +
                                                     "the encoded String ", e);
        }
    }

    /**
     * Serializing a SAML2 object into a String
     *
     * @param xmlObject object that needs to serialized.
     * @return serialized object
     * @throws org.wso2.carbon.identity.authenticator.saml2.sso.ui.SAML2SSOUIAuthenticatorException
     */
    public static String marshall(XMLObject xmlObject) throws SAML2SSOUIAuthenticatorException {
        try {
            doBootstrap();
            System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
                               "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");

            MarshallerFactory marshallerFactory = org.opensaml.xml.Configuration.getMarshallerFactory();
            Marshaller marshaller = marshallerFactory.getMarshaller(xmlObject);
            Element element = marshaller.marshall(xmlObject);

            ByteArrayOutputStream byteArrayOutputStrm = new ByteArrayOutputStream();
            DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
            DOMImplementationLS impl =
                    (DOMImplementationLS) registry.getDOMImplementation("LS");
            LSSerializer writer = impl.createLSSerializer();
            LSOutput output = impl.createLSOutput();
            output.setByteStream(byteArrayOutputStrm);
            writer.write(element, output);
            return byteArrayOutputStrm.toString();
        } catch (Exception e) {
            log.error("Error Serializing the SAML Response");
            throw new SAML2SSOUIAuthenticatorException("Error Serializing the SAML Response", e);
        }
    }

    /**
     * Compressing and Encoding the response
     *
     * @param xmlString String to be encoded
     * @return compressed and encoded String
     */
    public static String encode(String xmlString) throws Exception {
        Deflater deflater = new Deflater(Deflater.DEFLATED, true);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(
                byteArrayOutputStream, deflater);

        deflaterOutputStream.write(xmlString.getBytes());
        deflaterOutputStream.close();

        // Encoding the compressed message
        String encodedRequestMessage = Base64.encodeBytes(byteArrayOutputStream
                .toByteArray(), Base64.DONT_BREAK_LINES);
        return encodedRequestMessage.trim();
    }

    /**
     * Decoding and deflating the encoded AuthReq
     *
     * @param encodedStr encoded AuthReq
     * @return decoded AuthReq
     */
    public static String decode(String encodedStr) throws SAML2SSOUIAuthenticatorException {
        try {
            org.apache.commons.codec.binary.Base64 base64Decoder = new org.apache.commons.codec.binary.Base64();
            byte[] xmlBytes = encodedStr.getBytes("UTF-8");
            byte[] base64DecodedByteArray = base64Decoder.decode(xmlBytes);

            try {
                Inflater inflater = new Inflater(true);
                inflater.setInput(base64DecodedByteArray);
                byte[] xmlMessageBytes = new byte[5000];
                int resultLength = inflater.inflate(xmlMessageBytes);

                if (inflater.getRemaining() > 0) {
                    throw new RuntimeException("didn't allocate enough space to hold "
                                               + "decompressed data");
                }

                inflater.end();
                return new String(xmlMessageBytes, 0, resultLength, "UTF-8");

            } catch (DataFormatException e) {
                ByteArrayInputStream bais = new ByteArrayInputStream(
                        base64DecodedByteArray);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                InflaterInputStream iis = new InflaterInputStream(bais);
                byte[] buf = new byte[1024];
                int count = iis.read(buf);
                while (count != -1) {
                    baos.write(buf, 0, count);
                    count = iis.read(buf);
                }
                iis.close();
                String decodedStr = new String(baos.toByteArray());
                return decodedStr;
            }
        } catch (IOException e) {
            throw new SAML2SSOUIAuthenticatorException("Error when decoding the SAML Request.", e);
        }

    }

    /**
     * This method is used to initialize the OpenSAML2 library. It calls the bootstrap method, if it
     * is not initialized yet.
     */
    public static void doBootstrap() {
        if (!bootStrapped) {
            try {
                DefaultBootstrap.bootstrap();
                bootStrapped = true;
            } catch (ConfigurationException e) {
                log.error("Error in bootstrapping the OpenSAML2 library", e);
            }
        }
    }

    public static Response setSignature(Response response, String signatureAlgorithm,
                                        X509Credential cred)
            throws SAML2SSOUIAuthenticatorException {
        doBootstrap();
        try {
            Signature signature = (Signature) buildXMLObject(Signature.DEFAULT_ELEMENT_NAME);
            signature.setSigningCredential(cred);
            signature.setSignatureAlgorithm(signatureAlgorithm);
            signature.setCanonicalizationAlgorithm(Canonicalizer.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);

            try {
                KeyInfo keyInfo = (KeyInfo) buildXMLObject(KeyInfo.DEFAULT_ELEMENT_NAME);
                X509Data data = (X509Data) buildXMLObject(X509Data.DEFAULT_ELEMENT_NAME);
                X509Certificate cert = (X509Certificate) buildXMLObject(X509Certificate.DEFAULT_ELEMENT_NAME);
                String value = org.apache.xml.security.utils.Base64.encode(cred.getEntityCertificate().getEncoded());
                cert.setValue(value);
                data.getX509Certificates().add(cert);
                keyInfo.getX509Datas().add(data);
                signature.setKeyInfo(keyInfo);
            } catch (CertificateEncodingException e) {
                throw new SAML2SSOUIAuthenticatorException("errorGettingCert");
            }

            response.setSignature(signature);

            List<Signature> signatureList = new ArrayList<Signature>();
            signatureList.add(signature);

            //Marshall and Sign
            MarshallerFactory marshallerFactory = org.opensaml.xml.Configuration.getMarshallerFactory();
            Marshaller marshaller = marshallerFactory.getMarshaller(response);

            marshaller.marshall(response);

            org.apache.xml.security.Init.init();
            Signer.signObjects(signatureList);
            return response;

        } catch (Exception e) {
            throw new SAML2SSOUIAuthenticatorException("Error When signing the assertion.", e);
        }
    }

    public static XMLObject buildXMLObject(QName objectQName)
            throws SAML2SSOUIAuthenticatorException {

        XMLObjectBuilder builder = org.opensaml.xml.Configuration.getBuilderFactory().getBuilder(objectQName);
        if (builder == null) {
            throw new SAML2SSOUIAuthenticatorException("Unable to retrieve builder for object QName "
                                                     + objectQName);
        }
        return builder.buildObject(objectQName.getNamespaceURI(), objectQName.getLocalPart(),
                                   objectQName.getPrefix());
    }


    /**
     * Generates a unique Id for Authentication Requests
     *
     * @return generated unique ID
     */
    public static String createID() {

        byte[] bytes = new byte[20]; // 160 bits
        random.nextBytes(bytes);

        char[] chars = new char[40];

        for (int i = 0; i < bytes.length; i++) {
            int left = (bytes[i] >> 4) & 0x0f;
            int right = bytes[i] & 0x0f;
            chars[i * 2] = charMapping[left];
            chars[i * 2 + 1] = charMapping[right];
        }

        return String.valueOf(chars);
    }


    /**
     * Sets the issuerID and IDP SSO Service URL during the server start-up by reading authenticators.xml
     */
    public static boolean initSSOConfigParams() {
        boolean initSuccess = false;
        AuthenticatorsConfiguration authenticatorsConfiguration = AuthenticatorsConfiguration.getInstance();
        AuthenticatorsConfiguration.AuthenticatorConfig authenticatorConfig =
                authenticatorsConfiguration.getAuthenticatorConfig(SAML2SSOAuthenticatorConstants.AUTHENTICATOR_NAME);
        if (authenticatorConfig != null) {
            Map<String, String> parameters = authenticatorConfig.getParameters();
            serviceProviderId = parameters.get(SAML2SSOAuthenticatorConstants.SERVICE_PROVIDER_ID);
            identityProviderSSOServiceURL = parameters.get(SAML2SSOAuthenticatorConstants.IDENTITY_PROVIDER_SSO_SERVICE_URL);
            loginPage = parameters.get(SAML2SSOAuthenticatorConstants.LOGIN_PAGE);
            landingPage = parameters.get(SAML2SSOAuthenticatorConstants.LANDING_PAGE);
            initSuccess = true;
        }
        return initSuccess;
    }

    /**
     * checks whether authenticator enable ot disable
     *
     * @return  True/False
     */
    public static boolean isAuthenticatorEnabled() {
        AuthenticatorsConfiguration authenticatorsConfiguration = AuthenticatorsConfiguration.getInstance();
        AuthenticatorsConfiguration.AuthenticatorConfig authenticatorConfig =
                authenticatorsConfiguration.getAuthenticatorConfig(SAML2SSOAuthenticatorConstants.AUTHENTICATOR_NAME);
        // if the authenticator is disabled, then do not register the servlet filter.
        return !authenticatorConfig.isDisabled();
    }

    /**
     * returns the service provider ID of a particular server
     * @return service provider ID
     */
    public static String getServiceProviderId() {
        return serviceProviderId;
    }

    /**
     * returns the Identity Provider SSO Service URL
     * @return dentity Provider SSO Service URL
     */
    public static String getIdentityProviderSSOServiceURL() {
        return identityProviderSSOServiceURL;
    }

    /**
     * Gets the login page URL that needs to be filtered.
     * @return login page URL.
     */
    public static String getLoginPage() {
        return loginPage;
    }

    /**
     * Returns the landing page to which the login requests will be redirected to.
     * @return URL of the landing page
     */
    public static String getLandingPage() {
        return landingPage;
    }
}
