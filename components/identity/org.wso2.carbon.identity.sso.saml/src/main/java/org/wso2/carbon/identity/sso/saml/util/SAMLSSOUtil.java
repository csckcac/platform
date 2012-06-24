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
package org.wso2.carbon.identity.sso.saml.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.c14n.Canonicalizer;
import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.RequestAbstractType;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.impl.IssuerBuilder;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.XMLObjectBuilder;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallerFactory;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.security.x509.X509Credential;
import org.opensaml.xml.signature.*;
import org.opensaml.xml.validation.ValidationException;
import org.osgi.framework.BundleContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.identity.base.IdentityConstants;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.sso.saml.SAMLSSOConstants;
import org.wso2.carbon.identity.sso.saml.builders.X509CredentialImpl;
import org.wso2.carbon.identity.sso.saml.exception.IdentitySAML2SSOException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.cert.CertificateEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

public class SAMLSSOUtil {

    private static Log log = LogFactory.getLog(SAMLSSOUtil.class);
    private static RegistryService registryService;
    private static BundleContext bundleContext;
    private static RealmService realmService;
    private static ConfigurationContextService configCtxService;
    private static boolean isBootStrapped = false;
    private static Random random = new Random();
    private static final char[] charMapping = {
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o',
            'p'};

    public static void setRegistryService(RegistryService registryService) {
        SAMLSSOUtil.registryService = registryService;
    }

    public static void setRealmService(RealmService realmService) {
        SAMLSSOUtil.realmService = realmService;
    }

    public static BundleContext getBundleContext() {
        return SAMLSSOUtil.bundleContext;
    }

    public static void setBundleContext(BundleContext bundleContext) {
        SAMLSSOUtil.bundleContext = bundleContext;
    }

    public static RegistryService getRegistryService() {
        return registryService;
    }

    public static RealmService getRealmService() {
        return realmService;
    }

    public static ConfigurationContextService getConfigCtxService() {
        return configCtxService;
    }

    public static void setConfigCtxService(ConfigurationContextService configCtxService) {
        SAMLSSOUtil.configCtxService = configCtxService;
    }

    /**
     * Constructing the AuthnRequest Object from a String
     *
     * @param authReqStr Decoded AuthReq String
     * @return AuthnRequest Object
     * @throws org.wso2.carbon.identity.base.IdentityException
     *
     */
    public static XMLObject unmarshall(String authReqStr) throws IdentityException {
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
            throw new IdentityException("Error in constructing AuthRequest from the encoded String ", e);
        }
    }

    /**
     * Serialize the Auth. Request
     *
     * @param xmlObject
     * @return serialized auth. req
     */
    public static String marshall(XMLObject xmlObject) throws IdentityException {
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
            throw new IdentityException("Error Serializing the SAML Response", e);
        }
    }

    /**
     * Encoding the response
     *
     * @param xmlString
     * @return encoded String
     */
    public static String encode(String xmlString) throws Exception {
        xmlString = xmlString.replaceAll("&", "&amp;").replaceAll("\"", "&quot;").replaceAll("'", "&apos;").
                replaceAll("<", "&lt;").replaceAll(">", "&gt;").replace("\n", "");

        return xmlString;
    }

    /**
     * Decoding and deflating the encoded AuthReq
     *
     * @param encodedStr encoded AuthReq
     * @return decoded AuthReq
     */
    public static String decode(String encodedStr) throws IdentityException {
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
            throw new IdentityException("Error when decoding the SAML Request.", e);
        }

    }

    /**
     * Get the Issuer
     *
     * @return Issuer
     */
    public static Issuer getIssuer() {
        Issuer issuer = new IssuerBuilder().buildObject();
        issuer.setValue(IdentityUtil
                .getProperty(IdentityConstants.ServerConfig.SSO_IDP_URL));
        issuer.setFormat(SAMLSSOConstants.NAME_ID_POLICY_ENTITY);
        return issuer;
    }

    public static void doBootstrap() {
        if (!isBootStrapped) {
            try {
                DefaultBootstrap.bootstrap();
                isBootStrapped = true;
            } catch (ConfigurationException e) {
                log.error("Error in bootstrapping the OpenSAML2 library", e);
            }
        }
    }

    public static Response setSignature(Response response, String signatureAlgorithm,
                                        X509Credential cred)
            throws IdentityException {
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
                throw new IdentityException("errorGettingCert");
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
            throw new IdentityException("Error When signing the assertion.", e);
        }
    }

    private static XMLObject buildXMLObject(QName objectQName) throws IdentityException {
        XMLObjectBuilder builder = org.opensaml.xml.Configuration.getBuilderFactory().getBuilder(objectQName);
        if (builder == null) {
            throw new IdentityException("Unable to retrieve builder for object QName "
                                        + objectQName);
        }
        return builder.buildObject(objectQName.getNamespaceURI(), objectQName.getLocalPart(),
                                   objectQName.getPrefix());
    }

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
     * Generate the key store name from the domain name
     *
     * @param tenantDomain tenant domain name
     * @return key store file name
     */
    public static String generateKSNameFromDomainName(String tenantDomain) {
        String ksName = tenantDomain.trim().replace(".", "-");
        return (ksName + ".jks");
    }

    /**
     * Get the X509CredentialImpl object for a particular tenant
     *
     * @param domainName domain name
     * @return X509CredentialImpl object containing the public certificate of that tenant
     * @throws org.wso2.carbon.identity.sso.saml.exception.IdentitySAML2SSOException
     *          Error when creating X509CredentialImpl object
     */
    public static X509CredentialImpl getX509CredentialImplForTenant(String domainName, String alias)
            throws IdentitySAML2SSOException {

        int tenantID = 0;
        RegistryService registryService = SAMLSSOUtil.getRegistryService();
        RealmService realmService = SAMLSSOUtil.getRealmService();

        // get the tenantID
        if (domainName != null) {
            try {
                tenantID = realmService.getTenantManager().getTenantId(domainName);
            } catch (org.wso2.carbon.user.api.UserStoreException e) {
                String errorMsg = "Error getting the TenantID for the domain name";
                log.error(errorMsg, e);
                throw new IdentitySAML2SSOException(errorMsg, e);
            }
        }

        KeyStoreManager keyStoreManager;
        // get an instance of the corresponding Key Store Manager instance
        keyStoreManager = KeyStoreManager.getInstance(tenantID);

        X509CredentialImpl credentialImpl = null;
        KeyStore keyStore;

        try {
            if (tenantID != 0) {    // for non zero tenants, load private key from their generated key store
                keyStore = keyStoreManager.getKeyStore(generateKSNameFromDomainName(domainName));
            } else {    // for tenant zero, load the default pub. cert using the config. in carbon.xml
                keyStore = keyStoreManager.getPrimaryKeyStore();
            }
            java.security.cert.X509Certificate cert =  (java.security.cert.X509Certificate) keyStore.getCertificate(alias);
            credentialImpl = new X509CredentialImpl(cert);

        } catch (Exception e) {
            String errorMsg = "Error instantiating an X509CredentialImpl object for the public cert.";
            log.error(errorMsg, e);
            throw new IdentitySAML2SSOException(errorMsg, e);
        }
        return credentialImpl;
    }

    /**
     * Validate the signature of the SAML assertion represented as a String
     *
     * @param assertion  SAML Assertion String
     * @param alias      alias of the cert against which the signature should be validated.
     * @param domainName domain name
     * @return true, if signature is valid.
     */
    public static boolean validateAssertionSignature(String assertion, String alias,
                                                     String domainName) {
        boolean isSignatureValid = false;
        try {
            RequestAbstractType request = (RequestAbstractType) SAMLSSOUtil.unmarshall(SAMLSSOUtil.decode(assertion));
            isSignatureValid = validateAssertionSignature(request, alias, domainName);
        } catch (IdentityException ignore) {
            log.warn("Signature Validation failed for the SAML Assertion : Failed to unmarshall the SAML Assertion");
        }
        return isSignatureValid;
    }

    /**
     * Validate the signature of an assertion
     * @param assertion SAML Assertion, this could be either a SAML Request or a LogoutRequest
     * @param alias Certificate alias against which the signature is validated.
     * @param domainName domain name of the subject
     * @return true, if the signature is valid.
     */
    public static boolean validateAssertionSignature(RequestAbstractType assertion, String alias,
                                                     String domainName) {
        boolean isSignatureValid = false;

        if (assertion.getSignature() != null) {
            try {
                SignatureValidator validator = new SignatureValidator(SAMLSSOUtil.getX509CredentialImplForTenant(domainName, alias));
                validator.validate(assertion.getSignature());
                isSignatureValid = true;
            } catch (IdentitySAML2SSOException ignore) {
                log.warn("Signature validation failed for the SAML Assertion : Failed to construct the X509CredentialImpl for the alias " + alias);
            } catch (ValidationException ignore) {
                log.warn("Signature Validation Failed for the SAML Assertion : Signature is invalid.");
            }
        }
        return isSignatureValid;
    }
}
