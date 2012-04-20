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
package org.wso2.carbon.mediator.rule.ui.internal;

import org.wso2.carbon.rule.common.Fact;
import org.wso2.carbon.rule.common.Input;
import org.wso2.carbon.rule.common.Output;
import org.wso2.carbon.rule.mediator.config.RuleMediatorConfig;
import org.wso2.carbon.rule.mediator.config.Source;
import org.wso2.carbon.rule.mediator.config.Target;
import org.wso2.carbon.sequences.ui.util.SequenceEditorHelper;
import org.wso2.carbon.sequences.ui.util.ns.NameSpacesInformation;
import org.wso2.carbon.sequences.ui.util.ns.NameSpacesInformationRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Method;
import java.util.Map;


/**
 *
 */
public class RuleMediatorClientHelper {

    public static void populateSource(HttpServletRequest request, RuleMediatorConfig ruleMediatorConfig){

        Source source = ruleMediatorConfig.getSource();
        if (request.getParameter("mediator.rule.source.value") != null) {
            source.setValue(request.getParameter("mediator.rule.source.value"));
        }

        if (request.getParameter("mediator.rule.source.xpath") != null) {
            source.setXpath(request.getParameter("mediator.rule.source.xpath"));
        }

    }

    public static void populateTarget(HttpServletRequest request, RuleMediatorConfig ruleMediatorConfig){

        Target target = ruleMediatorConfig.getTarget();
        if (request.getParameter("mediator.rule.target.value") != null){
            target.setValue(request.getParameter("mediator.rule.target.value"));
        }

        if (request.getParameter("mediator.rule.target.resultXpath") != null){
            target.setResultXpath(request.getParameter("mediator.rule.target.resultXpath"));
        }

        if (request.getParameter("mediator.rule.target.xpath") != null){
            target.setXpath(request.getParameter("mediator.rule.target.xpath"));
        }

        if (request.getParameter("mediator.rule.target.action") != null){
            target.setAction(request.getParameter("mediator.rule.target.action"));
        }

    }
//    public static String getPropertyXML(String id,
//                                        Collection<PropertyDescription> mediatorPropertyList,
//                                        Locale locale) {
//
//        String propertyTableStyle = !mediatorPropertyList.isEmpty() ? "" : "display:none;";
//        ResourceBundle bundle = ResourceBundle.
//                getBundle("org.wso2.carbon.mediator.rule.ui.i18n.Resources",
//                        locale);
//        String header = bundle.getString(id + ".properties");
//        String name = bundle.getString("th.property.name");
//        String valueExpr = bundle.getString("th.value");
//        String type = bundle.getString("th.property.type");
//        String nsEditor = bundle.getString("namespaceeditor");
//        String action = bundle.getString("th.action");
//        String addProperty = bundle.getString("add.property");
//        String namespaces = bundle.getString("namespaces");
//        String delete = bundle.getString("delete");
//        String prefix = "<tr>\n" +
//                "<td>\n" +
//                "<h3 class=\"mediator\">" + header + "</h3>\n" +
//                "<div style=\"margin-top:0px;\">\n" +
//                "<table id=\"" + id + "propertytable\" style=\"" + propertyTableStyle + "\" " +
//                "class=\"styledInner\">\n" +
//                "<thead>\n" +
//                "<tr>\n" +
//                "<th width=\"15%\">" + name + "</th>\n" +
//                "<th width=\"15%\">" + valueExpr + "</th>\n" +
//                "<th>" + action + "</th>\n" +
//                "</tr>\n" +
//                "<tbody id=\"" + id + "propertytbody\">";
//
//        String suffix = "</tbody>\n" +
//                "</thead>\n" +
//                "</table>\n" +
//                "</div>\n" +
//                "</td>\n" +
//                "</tr>\n" +
//                "<tr>\n" +
//                "<td>\n" +
//                "<div style=\"margin-top:0px;\">\n" +
//                "<a name=\"add" + id + "NameLink\"></a>\n" +
//                "<a class=\"add-icon-link\" href=\"#add" + id + "NameLink\" " +
//                "onclick=\"addProperty('" + id + "')\">" + addProperty + "</a>\n" +
//                "</div>\n" +
//                "</td>\n" +
//                "</tr>";
//
//        String body = "";
//        int i = 0;
//        String valueMsg = bundle.getString("value");
//        String exprMsg = bundle.getString("expression");
//        for (PropertyDescription mp : mediatorPropertyList) {
//            if (mp != null) {
//                String propertyValue = mp.getValue();
//                body += "<tr id=\"" + id + "propertyRaw" + i + "\">\n" +
//                        "<td><input type=\"text\" name=\"" + id + "propertyName" + i + "\" id=\"" +
//                        id + "propertyName" + i + "\" " +
//                        "value=\"" + mp.getName() + "\"/>\n" +
//                        "</td>\n" +
//                        "<td>\n";
//
//                if (propertyValue == null) {
//                    propertyValue = "";
//                }
//                body += "<input id=\"" + id + "propertyValue" + i + "\" name=\"" + id +
//                        "propertyValue" + i + "\"" + " type=\"text\" value=\"" +
//                        propertyValue + "\"" + " />\n";
//                body += "</td>\n" +
//                        "<td><a href=\"#\" class=\"delete-icon-link\"" +
//                        " onclick=\"deleteProperty('" + i + "','" + id + "');return false;\">" +
//                        delete + "</a></td>" +
//                        "</tr>";
//            }
//            i++;
//        }
//        body += " <input type=\"hidden\" name=\"" + id + "propertyCount\" id=\"" +
//                id + "propertyCount\" value=\"" + i + "\" />";
//
//        return prefix + body + suffix;
//    }

    public static void registerNameSpaces(Map<String, String> properties, String baseId,
                                          HttpSession httpSession) {

        if (properties == null || baseId == null || "".equals(baseId)) {
            return;
        }

        int i = 0;
//        for (String key : properties.keySet()) {
//            if (key != null) {
//                BaseXPath xPath = property.getExpression();
//                if (xPath instanceof AXIOMXPath) {
//                    NameSpacesRegistrar.getInstance().registerNameSpaces((AXIOMXPath) xPath,
//                            baseId + String.valueOf(i), httpSession);
//                }
//            }
//            i++;
//        }
    }

    public static void setProperty(HttpServletRequest request,
                                   Object configuration, String mName, String id) {

        String registrationPropertyCount = request.getParameter(id + "propertyCount");
        if (registrationPropertyCount != null && !"".equals(registrationPropertyCount)) {
            int propertyCount = 0;
            try {
                propertyCount = Integer.parseInt(registrationPropertyCount.trim());

                for (int i = 0; i <= propertyCount; i++) {
                    String name = request.getParameter(id + "propertyName" + i);
                    if (name != null && !"".equals(name)) {
                        String valueId = id + "propertyValue" + i;
                        String value = request.getParameter(valueId);
                        if (value == null || "".equals(value.trim())) {
                            continue;
                        }
//                        PropertyDescription mp = new PropertyDescription();
//                        mp.setName(name.trim());
//                        mp.setValue(value.trim());
//                        invokeInstanceProperty(mName, mp, configuration);
                    }
                }
            } catch (NumberFormatException ignored) {
            }
        }
    }

    public static void updateInputFacts(HttpServletRequest request,
                                          RuleMediatorConfig ruleMediatorConfig,
                                          String id) {
        NameSpacesInformationRepository repository =
                (NameSpacesInformationRepository) request.getSession().getAttribute(
                        NameSpacesInformationRepository.NAMESPACES_INFORMATION_REPOSITORY);
        NameSpacesInformation information = null;
        String ownerID = SequenceEditorHelper.getEditingMediatorPosition(request.getSession());
        String inputCountParameter = request.getParameter(id + "Count");
        if (inputCountParameter != null && !"".equals(inputCountParameter)) {
            int inputCount = 0;
            try {
                inputCount = Integer.parseInt(inputCountParameter.trim());
                if (inputCount > 0) {
                    Input input = ruleMediatorConfig.getInput();
                    input.getFacts().clear();
                    Fact inputFact = null;
                    for (int i = 0; i < inputCount; i++) {
                        String type = request.getParameter(id + "Type" + i);
                        String elementName = request.getParameter(id + "ElementName" + i);
                        String namespace = request.getParameter(id + "Namespace" + i);
                        String xpath = request.getParameter(id + "Xpath" + i);
                        String nsID = id + "Value" + i;
                        if(type != null && !"".equals(type)){

                            inputFact = new Fact();
                            inputFact.setType(type);
                            inputFact.setElementName(elementName);
                            inputFact.setNamespace(namespace);
                            inputFact.setXpath(xpath);
                            input.addFact(inputFact);
                        }
                        if (repository != null) {

                            information = repository.getNameSpacesInformation(ownerID, nsID);
                            if (information != null) {
                                inputFact.setPrefixToNamespaceMap(information.getNameSpaces());
                            }
                        }
                    }
                }
            } catch (NumberFormatException ignored) {
            }
        }
    }

    public static void updateOutputFacts(HttpServletRequest request,
                                          RuleMediatorConfig ruleMediatorConfig,
                                          String id) {
        String outputCountParameter = request.getParameter(id + "Count");
        if (outputCountParameter != null && !"".equals(outputCountParameter)) {
            int outputCount = 0;
            try {
                outputCount = Integer.parseInt(outputCountParameter.trim());
                if (outputCount > 0) {
                    Output outPut = ruleMediatorConfig.getOutput();
                    outPut.getFacts().clear();
                    Fact outputFact = null;
                    for (int i = 0; i < outputCount; i++) {
                        String type = request.getParameter(id + "Type" + i);
                        String elementName = request.getParameter(id + "ElementName" + i);
                        String namespace = request.getParameter(id + "Namespace" + i);
//                        String xpath = request.getParameter(id + "Xpath" + i);
                        if(type != null && !"".equals(type)){

                            outputFact = new Fact();
                            outputFact.setType(type);
                            outputFact.setElementName(elementName);
                            outputFact.setNamespace(namespace);
//                            outputFact.setXpath(xpath);
                            outPut.addFact(outputFact);
                        }
                    }
                }
            } catch (NumberFormatException ignored) {
            }
        }
    }

    private static void invokeInstanceProperty(String mName, Object val, Object target) {
        Class<?> aClass = target.getClass();
        try {
            Method method = aClass.getMethod(mName, val.getClass());
            method.invoke(target, val);
        } catch (Exception e) {
            throw new RuntimeException("Error setting property : " + mName
                    + " into" + aClass + " : " + e.getMessage(), e);
        }
    }

}
