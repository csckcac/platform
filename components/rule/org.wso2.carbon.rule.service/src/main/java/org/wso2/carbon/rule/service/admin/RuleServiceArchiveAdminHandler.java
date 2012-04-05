/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.rule.service.admin;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.wso2.carbon.rule.core.RuleConstants;
import org.wso2.carbon.rule.service.RuleServiceManagementException;
import org.wso2.carbon.rule.service.RuleServiceManger;
import org.wso2.carbon.rulecep.commons.descriptions.service.ServiceDescription;
import org.wso2.carbon.rulecep.commons.descriptions.service.ServiceDescriptionSerializer;
import org.wso2.carbon.rulecep.commons.utils.OMElementHelper;
import org.wso2.carbon.utils.ArchiveManipulator;
import org.wso2.carbon.utils.FileManipulator;

import javax.activation.DataHandler;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Handler for executing rule service management operations for rule services
 * based on the rule service archive files
 */
public class RuleServiceArchiveAdminHandler extends AbstractRuleServiceAdminHandler {

    public void saveRuleService(AxisConfiguration axisConfiguration,
                                AxisService axisService,
                                ServiceDescription ruleServiceDescription) {

        Paths paths = createTempRuleServiceFile(axisConfiguration,
                ruleServiceDescription.getName());

        File metaINF = new File(paths.getWorkingDirPath() + File.separator + "META-INF");
        if (!metaINF.exists()) {
            metaINF.mkdirs();
        }
        // save services.xml
        File file = new File(paths.getWorkingDirPath() + File.separator + "META-INF" +
                File.separator + "services.xml");
        File serviceXMLFile = file.getAbsoluteFile();

        if (ruleServiceDescription.isContainsServicesXML()) {
            try {
                OMElement parent = null;
                if (serviceXMLFile.exists()) {
                    BufferedInputStream inputStream =
                            new BufferedInputStream(new FileInputStream(serviceXMLFile));
                    parent = OMElementHelper.getInstance().toOM(inputStream);
                    parent.build();
                    inputStream.close();
                    serviceXMLFile.delete();
                }
                serviceXMLFile.createNewFile();

                OutputStream os = new FileOutputStream(file);
                OMElement servicesXML =
                        ServiceDescriptionSerializer.serializeToServiceXML(
                                ruleServiceDescription, parent, AXIOM_XPATH_SERIALIZER);
                servicesXML.build();
                servicesXML.serialize(os);
            } catch (Exception e) {
                throw new RuleServiceManagementException("Cannot write services XML", e, log);
            }

        } else {
            if (serviceXMLFile.exists()) {
                serviceXMLFile.delete();
            }
        }

        String rslFileName = ruleServiceDescription.getName() + "." +
                RuleConstants.RULE_FILE_EXTENSION;
        File ruleFile = new File(paths.getWorkingDirPath() + File.separator + "META-INF" +
                File.separator + rslFileName);
        File absoluteFile = ruleFile.getAbsoluteFile();
        if (absoluteFile.exists()) {
            absoluteFile.delete();
        }
        try {
            absoluteFile.createNewFile();
        } catch (IOException e) {
            throw new RuleServiceManagementException("Error creating a rule service file : " +
                    absoluteFile, log);
        }

        try {
            OutputStream os = new FileOutputStream(ruleFile);
            OMElement ruleServiceXML =
                    ServiceDescriptionSerializer.serializeToRuleServiceConfiguration(
                            ruleServiceDescription, NULL_NS,
                            AXIOM_XPATH_SERIALIZER, CONFIGURATION_EXTENSION_SERIALIZER);
            ruleServiceXML.build();
            ruleServiceXML.serialize(os);
        } catch (Exception e) {
            throw new RuleServiceManagementException("Cannot write to the rule service file : " +
                    ruleFile, e, log);
        }

        ArchiveManipulator archiveManipulator = new ArchiveManipulator();
        try {
            String servicePath = paths.getServicePath();
            if (!servicePath.endsWith(RuleConstants.RULE_SERVICE_ARCHIVE_EXTENSION)) {
                File serviceFile = new File(servicePath);
                File absoluteServiceFile = serviceFile.getAbsoluteFile();
                if (absoluteServiceFile.exists()) {
                    absoluteServiceFile.delete();
                }
                servicePath = servicePath.substring(0, servicePath.lastIndexOf(".") + 1) +
                        RuleConstants.RULE_SERVICE_ARCHIVE_EXTENSION;
            }
            archiveManipulator.archiveDir(servicePath, paths.getWorkingDirPath());
        } catch (IOException e) {
            throw new RuleServiceManagementException("Error creating a archive a rule service ", e,
                    log);
        }
//        saveToRegistry(paths, ruleServiceDescription.getName());
        cleanUp(paths);
    }

    public OMElement getRuleService(AxisConfiguration axisConfiguration, String name) {

        Paths paths = createTempRuleServiceFile(axisConfiguration, name);
        File ruleFile = new File(paths.getWorkingDirPath() + File.separator + "META-INF" +
                File.separator + name + "." + RuleConstants.RULE_FILE_EXTENSION);
        OMElement result = createXML(name, ruleFile);
        File servicesXML = new File(paths.getWorkingDirPath() + File.separator + "META-INF" +
                File.separator + "services.xml");
        if (servicesXML.exists()) {
            result.addAttribute(
                    OM_FACTORY.createOMAttribute(
                            RuleConstants.ATT_GENERATE_SERVICES_XML.getLocalPart(),
                            NULL_NS, String.valueOf(true)));
        }
        return result;
    }

    public String[] uploadFacts(AxisConfiguration axisConfiguration,
                                String serviceName,
                                String fileName,
                                DataHandler dataHandler) {

        Paths paths = createTempRuleServiceFile(axisConfiguration, serviceName);

        File lib = new File(paths.getWorkingDirPath() + File.separator + "lib");
        if (!lib.exists()) {
            lib.mkdirs();
        }

        File factFile = new File(lib, fileName);
        File absoluteFile = factFile.getAbsoluteFile();

        if (absoluteFile.exists()) {
            absoluteFile.delete();
        }
        try {
            absoluteFile.createNewFile();
            final FileOutputStream fos = new FileOutputStream(factFile);
            dataHandler.writeTo(fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            throw new RuleServiceManagementException("Cannot write facts", e, log);
        }
        ArchiveManipulator archiveManipulator = new ArchiveManipulator();
        try {
            String[] strings = archiveManipulator.check(factFile.getAbsolutePath());
            List<String> list = filterClasses(strings);
            return list.toArray(new String[list.size()]);
        } catch (IOException e) {
            throw new RuleServiceManagementException("Cannot extractPayload classes from the fact" +
                    " file", e, log);
        }
    }

    public void uploadRuleFile(AxisConfiguration axisConfiguration,
                               String serviceName,
                               String fileName,
                               DataHandler dataHandler) {

        Paths paths = createTempRuleServiceFile(axisConfiguration, serviceName);

        File conf = new File(paths.getWorkingDirPath() + File.separator + "conf");
        if (!conf.exists()) {
            conf.mkdirs();
        }

        File factFile = new File(conf, fileName);
        File absoluteFile = factFile.getAbsoluteFile();

        if (absoluteFile.exists()) {
            absoluteFile.delete();
        }
        try {
            absoluteFile.createNewFile();
            final FileOutputStream fos = new FileOutputStream(factFile);
            dataHandler.writeTo(fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            throw new RuleServiceManagementException("Cannot write Rule File", e, log);
        }
    }

    public String[] getAllFacts(AxisConfiguration axisConfiguration,
                                String serviceName) {
        final List<String> facts = new ArrayList<String>();
        RuleServiceManger ruleServiceManger = RuleServiceManger.getInstance();
        facts.addAll(ruleServiceManger.getResultAdapters());
        Collection<String> factTypes = ruleServiceManger.getFactAdapters();
        for (String factType : factTypes) {
            if (!facts.contains(factType)) {
                facts.add(factType);
            }
        }
        Paths paths = createTempRuleServiceFile(axisConfiguration, serviceName);
        File lib = new File(paths.getWorkingDirPath() + File.separator + "lib");
        if (lib.exists()) {
            File[] jars = FileManipulator.getMatchingFiles(
                    lib.getAbsolutePath(), null, ".jar");
            for (File file : jars) {
                try {
                    String[] strings =
                            new ArchiveManipulator().check(file.getAbsolutePath());
                    facts.addAll(filterClasses(strings));
                } catch (IOException e) {
                    throw new RuleServiceManagementException("Cannot extractPayload classes from " +
                            "the fact file", e, log);
                }
            }
        }
        if(facts.contains("dom")){
            facts.remove("dom");
        }
        if(facts.contains("omelement")){
            facts.remove("omelement");
        }
        if(facts.contains("context")){
            facts.remove("context");
        }
        return facts.toArray(new String[facts.size()]);
    }

    private Paths createTempRuleServiceFile(AxisConfiguration axisConfig,
                                            String serviceName) {
        String servicesDir = createServiceRepository(axisConfig);
        String servicePath = getServicePath(axisConfig, serviceName);
        if (servicePath == null || "".equals(servicePath)) {
            servicePath = servicesDir + File.separator + serviceName + "." +
                    RuleConstants.RULE_SERVICE_ARCHIVE_EXTENSION;
        }

        File sourceFile = new File(servicePath);
        String targetDirectory = getTempDir() + File.separator + serviceName + "." +
                RuleConstants.RULE_SERVICE_ARCHIVE_EXTENSION;

        if (sourceFile.exists() &&
                servicePath.endsWith(RuleConstants.RULE_SERVICE_ARCHIVE_EXTENSION)) {
            ArchiveManipulator manipulator = new ArchiveManipulator();
            try {
                manipulator.extractFromStream(new FileInputStream(sourceFile), targetDirectory);
            } catch (IOException e) {
                throw new RuleServiceManagementException(
                        "Error extracting files from a source:  " + sourceFile +
                                " into destination : " + targetDirectory, log);
            }
        } else {
            new File(targetDirectory).mkdirs();
        }

        return new Paths(servicePath, targetDirectory);
    }

    private List<String> filterClasses(String[] strings) {
        if (strings == null) {
            return new ArrayList<String>();
        }
        final List<String> classes = new ArrayList<String>();
        for (String s : strings) {
            if (s != null && s.endsWith(".class")) {
                classes.add(getClassNameFromResourceName(s));
            }
        }
        return classes;
    }

    private String getClassNameFromResourceName(String resourceName) {
        if (!resourceName.endsWith(".class")) {
            throw new RuleServiceManagementException("The resource name doesn't refer to" +
                    " a class file", log);
        }
        return resourceName.substring(0, resourceName.length() - 6).replace('/', '.');
    }
}
