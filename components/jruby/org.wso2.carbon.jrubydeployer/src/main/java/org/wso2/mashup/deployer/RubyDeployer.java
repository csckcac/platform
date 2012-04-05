/*
 * Copyright 2006,2007 WSO2, Inc. http://www.wso2.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.mashup.deployer;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.*;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.description.*;
import org.apache.axis2.description.java2wsdl.Java2WSDLConstants;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.constants.Constants;
import org.apache.ws.commons.schema.utils.NamespaceMap;
import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.RubyHash;
import org.jruby.runtime.Block;
//import org.wso2.javascript.rhino.JavaScriptEngineConstants;
import org.wso2.wsf.deployer.schemagenarator.types.ComplexType;

import org.wso2.wsf.deployer.schemagenarator.SchemaGenerator;
import org.wso2.wsf.deployer.schemagenarator.types.SimpleType;
import org.wso2.wsf.deployer.schemagenarator.types.Type;

import org.wso2.mashup.deployer.util.RubyScriptReader;
//import org.wso2.wsf.deployer.schemagenarator.types.MyTypes;

import javax.xml.namespace.QName;
import java.io.*;
import java.util.*;


public class RubyDeployer extends AbstractDeployer implements DeploymentConstants {
    private static final Log log = LogFactory.getLog(RubyDeployer.class);

    private AxisConfiguration axisConfig;

    protected Map schemaMap = new Hashtable();

    private ConfigurationContext configCtx;

    private File rubyFile;

    private SchemaGenerator schemaGenerator = null;

    

    public ArrayList processService(DeploymentFileData currentFile,
            AxisServiceGroup axisServiceGroup, HashMap wsdls, ConfigurationContext configCtx)
            throws AxisFault {
        try {
            String shortFileName = DescriptionBuilder.getShortFileName(currentFile.getName());
            String serviceName = shortFileName;
            axisServiceGroup.setServiceGroupName(serviceName);
            AxisService axisService = null;
            if (serviceName != null) {
                axisService = (AxisService) wsdls.get(serviceName);
            }
            if (axisService == null) {
                axisService = new AxisService();
            }

            String targetNamespace = "http://services.mashup.wso2.org/" + serviceName ;
            //Object serviceScopeObject = engine.get("scope");
            //String serviceScope = (String)serviceScopeObject;

            axisService.setParent(axisServiceGroup);
            axisService.setClassLoader(currentFile.getClassLoader());

                axisService.setName(serviceName);
                axisService.setTargetNamespace(targetNamespace);
              //  axisService.setScope(serviceScope);

                // adding name spaces
                NamespaceMap map = new NamespaceMap();
                map.put(Java2WSDLConstants.AXIS2_NAMESPACE_PREFIX, Java2WSDLConstants.AXIS2_XSD);
                map.put(Java2WSDLConstants.DEFAULT_SCHEMA_NAMESPACE_PREFIX,
                        Java2WSDLConstants.URI_2001_SCHEMA_XSD);
                axisService.setNameSpacesMap(map);
                String schemaTargetNamespace = "http://services.mashup.wso2.org/" + serviceName + "?xsd";
               /* String schemaString = "<xs:schema targetNamespace=\""
                        + schemaTargetNamespace
                        + "\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" elementFormDefault=\"qualified\">"
                        + "  <xs:element name=\"RubyParameter\" type=\"RubyParamType\"/>"
                        + "   <xs:complexType name=\"RubyParamType\">" + "       <xs:sequence>"
                        + "           <xs:element name=\"in\" type=\"xs:anyType\"/>"
                        + "       </xs:sequence>" + "   </xs:complexType>" + "</xs:schema>";
                XmlSchemaCollection schemaCol = new XmlSchemaCollection();
                XmlSchema schema = schemaCol.read(new StringReader(schemaString), null);*/

                MyTypes types = new MyTypes();

                schemaGenerator = new SchemaGenerator(schemaTargetNamespace, types);
                XmlSchema schema = schemaGenerator.getSchema();
                axisService.addSchema(schema);
                QName paramElementQname = new QName(schemaTargetNamespace, "RubyParameter");


            RubyScriptReader scriptreader = new RubyScriptReader();
            String scriptFile = scriptreader.readScript(currentFile.getFile());
            //HashMap annoMap = RubyOperationsAnnotationParser.parseRubyOperationsAnnotation(scriptFile);
            Map annoMap = RubyOperationsAnnotationParser.parseRubyOperationsAnnotation(scriptFile);
            annotationsToSchema(axisService,annoMap,schemaGenerator);


            Parameter serviceRubyParameter = new Parameter(RubyConstants.SERVICE_RUBY, currentFile.getAbsolutePath());
            axisService.addParameter(serviceRubyParameter);
            Parameter serviceScrptRubyParameter = new Parameter(RubyConstants.SERVICE_RUBY_SCRIPT, scriptFile);
            axisService.addParameter(serviceScrptRubyParameter);
            //Creating the service.resources dir
            File parentDir =currentFile.getFile().getParentFile();
            File resourcesDir = new File(parentDir,shortFileName+".resources");
            resourcesDir.mkdir();
            Parameter resourceFolderParameter = new Parameter(RubyConstants.RESOURCES_FOLDER, resourcesDir);
            axisService.addParameter(resourceFolderParameter);

            

            ArrayList serviceList = new ArrayList();
            serviceList.add(axisService);
            return serviceList;
        } catch (IOException e) {
            throw new DeploymentException(e);
        }
    }

    private void annotationsToSchema(AxisService service,Map annotationMap,SchemaGenerator schemaGen){


            Set keys = annotationMap.keySet();
            Iterator keyIt =  keys.iterator() ;


        //for each annotation
            while(keyIt.hasNext()){

                Object val = annotationMap.get(keyIt.next());
                ComplexType inComplexType = new ComplexType();
                ComplexType outComplexType = new ComplexType();
                XmlSchemaElement inSchemaElement = null;
                XmlSchemaElement outSchemaElement = null;
                QName inParamElementQname = null,outParamElementQname = null;

                if (val instanceof Map){

                       //mapOperationSchema((HashMap) val,inComplexType,inComplexType,outComplexType);
                        mapOperationSchema((Map) val,inComplexType,inComplexType,outComplexType);
                       String opName = inComplexType.getName();
                    try {
                        inSchemaElement = schemaGen.createInputElement(inComplexType, opName);
                        outSchemaElement = schemaGen.createOutputElement(outComplexType, opName);
                        inParamElementQname = inSchemaElement.getQName();
                        outParamElementQname = outSchemaElement.getQName();
                        String method = inComplexType.getName();
                        processOperations(service,inParamElementQname,outParamElementQname,method);
                        
                    } catch (Exception e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }

                }


            }
        

    }



    private void mapOperationSchema(Map map,Type parent,ComplexType INCOMPLEX_TYPE, ComplexType OUTCOMPLEX_TYPE){
              
               Set keys = map.keySet();
               Iterator keyIt =  keys.iterator() ;
               String opName="";

            while(keyIt.hasNext()){
                  Object key = keyIt.next();
                  Object val = map.get(key);
                //  Boolean keywordsOpFound=false,keywordsReturnFound=false;

                  if(RubyConstants.OPERATION_NAME.equals(key.toString())){
                          opName = (String) map.get(key);
                          INCOMPLEX_TYPE.setName(opName);
                          OUTCOMPLEX_TYPE.setName(opName+"Response");
                          


                  }
                  else if(RubyConstants.RETURN.equals(key.toString())){



                          schemaOp(OUTCOMPLEX_TYPE, INCOMPLEX_TYPE, OUTCOMPLEX_TYPE, key, val);
                         // processTypes();
                  } else{
                          schemaOp(parent, INCOMPLEX_TYPE, OUTCOMPLEX_TYPE, key, val);
                  }




            }


    }

    private void schemaOp(Type parent, ComplexType INCOMPLEX_TYPE, ComplexType OUTCOMPLEX_TYPE, Object key, Object val) {
        if(val instanceof Map){


                   ComplexType c = new ComplexType();
                   c.setName(key.toString());
                   if(parent!=null && parent instanceof ComplexType){
                     ((ComplexType)parent).addMember(c);
                   }
                   parent = c;

              // mapOperationSchema((HashMap) val,parent,INCOMPLEX_TYPE,OUTCOMPLEX_TYPE);
                 mapOperationSchema((Map) val,parent,INCOMPLEX_TYPE,OUTCOMPLEX_TYPE);
        }
        else{

                     SimpleType s = new SimpleType();
                     s.setName(key.toString());
                     s.setType(val.toString());
                     if(parent!=null && parent instanceof ComplexType){
                         ((ComplexType)parent).addMember(s);
                     }

        }
    }



    private void processOperations(AxisService service, QName inParamElementQname, QName outParamElementQname, String method) throws AxisFault {
           if(method!=null && !"".equals(method)){
               AxisOperation op = new InOutAxisOperation(new QName(method));
               op.setMessageReceiver(new RubyMessageReceiver());
               op.setStyle(WSDLConstants.STYLE_DOC);
               Parameter parameter = new Parameter();
               parameter.setName("OPERATION_NAME");
               parameter.setValue(method);
               op.addParameter(parameter);
               service.addOperation(op);

                AxisMessage inMessage = op
                .getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                    if (inMessage != null) {
                        // create the complex type & create a QName out of it and set it to the setElementQName of in message
                        inMessage.setName(method + Java2WSDLConstants.MESSAGE_SUFFIX);
                        inMessage.setElementQName(inParamElementQname);
                    }
                    AxisMessage outMessage = op.getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);

                    if (outMessage != null) {
                        outMessage.setName(method + Java2WSDLConstants.MESSAGE_SUFFIX);
                        outMessage.setElementQName(outParamElementQname);
                    }
                    if (op.getInputAction() == null) {
                        op.setSoapAction("urn:" + method);
                    }
                    axisConfig.getPhasesInfo().setOperationPhases(op); 

           }

    }

        /*
    private void processOperation(AxisService axisService, QName paramElementQname, String method) throws AxisFault {
        //JavaScriptOperationsAnnotationParser annotationParser = new JavaScriptOperationsAnnotationParser(
             //   function, method);
            try{
            AxisOperation axisOp = new InOutAxisOperation(new QName(method));
            axisOp.setMessageReceiver(new RubyMessageReceiver());
            axisOp.setStyle(WSDLConstants.STYLE_DOC);
            Parameter parameter = new Parameter();
            axisOp.addParameter(parameter);
            axisService.addOperation(axisOp);


            RubyOperationsAnnotationParser annotationParser = null;//new RubyOperationsAnnotationParser(new BufferedReader(new FileReader(rubyFile)),method);

            Hashtable inputParameters = annotationParser.getInputTypesNameObject();

            ComplexType inputComplexType = new ComplexType();
            inputComplexType.setName(method);

            Set keySet = inputParameters.keySet();
            Iterator keyIterator = keySet.iterator();

            while(keyIterator.hasNext()){
                Object key = keyIterator.next();
                SimpleType simpleType = new SimpleType();
                simpleType.setName(key.toString());
                simpleType.setType(inputParameters.get(key).toString());
                inputComplexType.addMember(simpleType);
            }

            AxisMessage inMessage = axisOp
                    .getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
            if (inMessage != null) {
                inMessage.setName(method + Java2WSDLConstants.MESSAGE_SUFFIX);
                XmlSchemaElement xmlSchemaElement = schemaGenerator.createInputElement(inputComplexType,method);
                if(xmlSchemaElement != null){
                inMessage.setElementQName(xmlSchemaElement.getQName());
                }
            }

            AxisMessage outMessage = axisOp.getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
            String outputType = (String)annotationParser.getOutputTypeNameObject();

            ComplexType outputComplexType = new ComplexType();
            outputComplexType.setName(method + "Response");
            SimpleType simpleType = new SimpleType();
            simpleType.setName("return");
            simpleType.setType(outputType);
            outputComplexType.addMember(simpleType);


            if (outMessage != null) {
                outMessage.setName(method + Java2WSDLConstants.MESSAGE_SUFFIX);
                XmlSchemaElement xmlSchemaElement = schemaGenerator.createOutputElement(outputComplexType, method);
                outMessage.setElementQName(xmlSchemaElement.getQName());
            }
            if (axisOp.getInputAction() == null) {
                axisOp.setSoapAction("urn:" + method);
            }
            axisConfig.getPhasesInfo().setOperationPhases(axisOp);
            }
            catch(Exception ex){
                ex.printStackTrace();
            }
        }
              */

    // To initialize the deployer
    public void init(ConfigurationContext configCtx) {
        this.configCtx = configCtx;
        this.axisConfig = this.configCtx.getAxisConfiguration();
    }

    // Will process the file and add that to axisConfig
    public void deploy(DeploymentFileData deploymentFileData) {
        StringWriter errorWriter = new StringWriter();
        String serviceStatus = "";
        try {
            deploymentFileData.setClassLoader(axisConfig.getServiceClassLoader());
            HashMap wsdlservice = new HashMap();
            AxisServiceGroup serviceGroup = new AxisServiceGroup(axisConfig);
            serviceGroup.setServiceGroupClassLoader(deploymentFileData.getClassLoader());
            ArrayList serviceList = processService(deploymentFileData, serviceGroup, wsdlservice,
                    configCtx);
            if (serviceList != null) {
                DeploymentEngine.addServiceGroup(serviceGroup, serviceList, deploymentFileData
                        .getFile().toURL(), deploymentFileData, axisConfig);
                log.info(Messages.getMessage(DeploymentErrorMsgs.DEPLOYING_WS, deploymentFileData
                        .getName()));
            }
            super.deploy(deploymentFileData);
        } catch (DeploymentException de) {
            de.printStackTrace();
            log.error(Messages.getMessage(DeploymentErrorMsgs.INVALID_SERVICE, deploymentFileData
                    .getName(), de.getMessage()), de);
            PrintWriter error_ptintWriter = new PrintWriter(errorWriter);
            de.printStackTrace(error_ptintWriter);
            serviceStatus = "Error:\n" + errorWriter.toString();
        } catch (AxisFault axisFault) {
            log.error(Messages.getMessage(DeploymentErrorMsgs.INVALID_SERVICE, deploymentFileData
                    .getName(), axisFault.getMessage()), axisFault);
            PrintWriter error_ptintWriter = new PrintWriter(errorWriter);
            axisFault.printStackTrace(error_ptintWriter);
            serviceStatus = "Error:\n" + errorWriter.toString();
        } catch (Exception e) {
            e.printStackTrace();
            if (log.isInfoEnabled()) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                log.info(Messages.getMessage(DeploymentErrorMsgs.INVALID_SERVICE,
                        deploymentFileData.getName(), sw.getBuffer().toString()));
            }
            PrintWriter error_ptintWriter = new PrintWriter(errorWriter);
            e.printStackTrace(error_ptintWriter);
            serviceStatus = "Error:\n" + errorWriter.toString();
        } catch (Throwable t) {
            t.printStackTrace();
            if (log.isInfoEnabled()) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                t.printStackTrace(pw);
                log.info(Messages.getMessage(DeploymentErrorMsgs.INVALID_SERVICE,
                        deploymentFileData.getName(), sw.getBuffer().toString()));
            }
            PrintWriter error_ptintWriter = new PrintWriter(errorWriter);
            t.printStackTrace(error_ptintWriter);
            serviceStatus = "Error:\n" + errorWriter.toString();
        } finally {
            if (serviceStatus.startsWith("Error:")) {
                axisConfig.getFaultyServices().put(deploymentFileData.getFile().getAbsolutePath(),
                        serviceStatus);
            }
        }
    }

    public void undeploy(String fileName) {
        try {
            axisConfig.removeServiceGroup(fileName);
            super.undeploy(fileName);
            log.info(Messages.getMessage(DeploymentErrorMsgs.SERVICE_REMOVED, fileName));
        } catch (AxisFault axisFault) {
            // May be a faulty service
            axisConfig.removeFaultyService(fileName);
        }
    }

    public void setDirectory(String directory) {
    }

    public void setExtension(String extension) {
    }
}
