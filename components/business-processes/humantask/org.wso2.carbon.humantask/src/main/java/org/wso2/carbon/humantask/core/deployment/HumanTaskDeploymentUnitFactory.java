/*
 * Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.humantask.core.deployment;

import org.apache.axis2.util.XMLUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlbeans.XmlException;
import org.w3c.dom.Document;
import org.wso2.carbon.humantask.HumanInteractionsDocument;
import org.wso2.carbon.humantask.core.deployment.config.HTDeploymentConfigDocument;
import org.wso2.carbon.humantask.core.store.HumanTaskArtifactContentType;
import org.xml.sax.SAXException;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Deprecated
public class HumanTaskDeploymentUnitFactory {
    private static Log log = LogFactory.getLog(HumanTaskDeploymentUnitFactory.class);

    private static final String JAVAX_WSDL_VERBOSE_MODE_KEY = "javax.wsdl.verbose";

    /**
     * Create Human Task Deployment unit by processing Human Task archive.
     *
     * @param humanTaskArchive human task archive file
     * @return HumanTaskDeploymentUnit
     * @throws HumanTaskDeploymentException when an error occurred during deployment
     */
    public static HumanTaskDeploymentUnit createHumanTaskDeploymentUnitFromArchive(
            File humanTaskArchive)
            throws HumanTaskDeploymentException {
        String errMsg;
        InputStream htArchiveInputStream = null;
        try {
            htArchiveInputStream = new FileInputStream(humanTaskArchive);
            /* return new HumanTaskDeploymentUnit(
         getHumanInteractionsDefinitionFromArchive(htArchiveInputStream),
         getHumanTaskConfiguration(htArchiveInputStream),
         new ArrayList<Definition>(getWSDLs(htArchiveInputStream).values()),
         getHumanTaskArchiveName(humanTaskArchive));   */
        } catch (IOException e) {
            errMsg = "IO error while reading human task archive " + humanTaskArchive.getPath() + ".";
            log.error(errMsg, e);
            throw new HumanTaskDeploymentException(errMsg, e);
        }/* catch (XmlException e) {
            errMsg = "XML parsing error while processing human task archive " + humanTaskArchive.getPath() + ".";
            log.error(errMsg);
            throw new HumanTaskStoreException(errMsg, e);
        } catch (WSDLException e) {
            errMsg = "WSDL parsing error while processing human task archive " + humanTaskArchive.getPath() + ".";
            log.error(errMsg);
            throw new HumanTaskStoreException(errMsg, e);
        }  */ finally {
            if (htArchiveInputStream != null) {
                try {
                    htArchiveInputStream.close();
                } catch (IOException e) {
                    errMsg = "IO error";
                    log.error(errMsg, e);
                }
            }
        }

        return null;
    }

    public static HumanTaskDeploymentUnit createHumanTaskDeploymentUnitFromRegistry(
            String humanTaskArtifactLocation) {
        throw new UnsupportedOperationException();
    }

    private static HumanInteractionsDocument getHumanInteractionsDefinitionFromArchive(
            InputStream htArtifact)
            throws IOException, XmlException {
        return HumanInteractionsDocument.Factory.parse(getInputStreamByFileType(htArtifact,
                                                                                HumanTaskArtifactContentType.HT_DEFINITION));
    }

    private static HTDeploymentConfigDocument getHumanTaskConfiguration(InputStream htArtifact)
            throws IOException, XmlException {
        return HTDeploymentConfigDocument.Factory.parse(getInputStreamByFileType(htArtifact,
                                                                                 HumanTaskArtifactContentType.HT_CONFIGURATION));
    }

    private static Map<String, Definition> getWSDLs(InputStream htArtifact)
            throws IOException, WSDLException {
        Map<String, InputStream> wsdlInputStreams = getMapOfInputStreamsByFileType(htArtifact,
                                                                                   HumanTaskArtifactContentType.WSDL);
        Map<String, Definition> wsdls = new HashMap<String, Definition>();

        for (Map.Entry<String, InputStream> entry : wsdlInputStreams.entrySet()) {
            wsdls.put(entry.getKey(), readInTheWSDLFile(entry.getValue(), entry.getKey()));
        }

        return wsdls;
    }

    private static Map<String, InputStream> getMapOfInputStreamsByFileType(
            InputStream htArtifact,
            HumanTaskArtifactContentType type) throws IOException {
        Map<String, InputStream> inputStreamMap = new HashMap<String, InputStream>();
        ZipInputStream zin = new ZipInputStream(htArtifact);

        ZipEntry entry;
        byte[] buffer = new byte[1024];
        int read;
        ByteArrayOutputStream out;
        while ((entry = zin.getNextEntry()) != null) {
            if (entry.getName().toLowerCase().endsWith(type.fileExtension())) {
                out = new ByteArrayOutputStream();

                while ((read = zin.read(buffer)) > 0) {
                    out.write(buffer, 0, read);
                }

                inputStreamMap.put(entry.getName(), new ByteArrayInputStream(out.toByteArray()));
            }
        }

        return inputStreamMap;
    }

    private static InputStream getInputStreamByFileType(InputStream htArtifact,
                                                        HumanTaskArtifactContentType type)
            throws IOException {
        ZipInputStream zin = new ZipInputStream(htArtifact);

        ZipEntry entry;
        byte[] buffer = new byte[1024];
        int read;
        ByteArrayOutputStream out;
        while ((entry = zin.getNextEntry()) != null) {
            if (entry.getName().toLowerCase().endsWith(type.fileExtension())) {
                out = new ByteArrayOutputStream();

                while ((read = zin.read(buffer)) > 0) {
                    out.write(buffer, 0, read);
                }

                return new ByteArrayInputStream(out.toByteArray());
            }
        }

        return null;
    }

    /**
     * Read the WSDL file given the input stream for the WSDL source
     *
     * @param in        WSDL input stream
     * @param entryName ZIP file entry name
     * @return WSDL Definition
     * @throws javax.wsdl.WSDLException at parser error
     */
    private static Definition readInTheWSDLFile(InputStream in, String entryName)
            throws WSDLException {

        WSDLReader reader = WSDLFactory.newInstance().newWSDLReader();

        // switch off the verbose mode for all usecases
        reader.setFeature(JAVAX_WSDL_VERBOSE_MODE_KEY, false);
        reader.setFeature("javax.wsdl.importDocuments", true);

        Definition def;
        Document doc;
        try {
            doc = XMLUtils.newDocument(in);
        } catch (ParserConfigurationException e) {
            throw new WSDLException(WSDLException.PARSER_ERROR,
                                    "Parser Configuration Error", e);
        } catch (SAXException e) {
            throw new WSDLException(WSDLException.PARSER_ERROR,
                                    "Parser SAX Error", e);

        } catch (IOException e) {
            throw new WSDLException(WSDLException.INVALID_WSDL, "IO Error",
                                    e);
        }

        // Log when and from where the WSDL is loaded.
        if (log.isDebugEnabled()) {
            log.debug("Reading 1.1 WSDL with base uri = " + entryName);
            log.debug("  the document base uri = " + entryName);
        }

        def = reader.readWSDL(entryName, doc.getDocumentElement());

        def.setDocumentBaseURI(entryName);
        return def;

    }

    private static String getHumanTaskArchiveName(File humanTaskArchive) {
        String archiveName = humanTaskArchive.getName();
        return archiveName.substring(0, archiveName.indexOf("."));
    }
}
