/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.governance.services.ui.utils;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.api.util.GovernanceConstants;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.ui.CarbonUIUtil;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/* This is the class which generate the service UI by reading service-config.xml */
public class AddServiceUIGenerator {

    private static final Log log = LogFactory.getLog(AddServiceUIGenerator.class);

    private String dataElement;
    private String dataNamespace;

    public AddServiceUIGenerator() {
        this(UIGeneratorConstants.DATA_ELEMENT, UIGeneratorConstants.DATA_NAMESPACE);
    }

    public AddServiceUIGenerator(String dataElement, String dataNamespace) {
        this.dataElement = dataElement;
        this.dataNamespace = dataNamespace;
    }

    //StringBuffer serviceUI;

    public OMElement getUIConfiguration(String content, HttpServletRequest request,
                                        ServletConfig config, HttpSession session) throws Exception {
        OMElement omElement = null;
        try {
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(content));
            StAXOMBuilder builder = new StAXOMBuilder(reader);
            omElement = builder.getDocumentElement();
        } catch (XMLStreamException e) {
            log.error("Unable to parse the UI configuration.", e);
        }
        return omElement;
    }

    public String printWidgetWithValues(OMElement widget, OMElement data,
                                        boolean isFilterOperation, HttpServletRequest request,
                                        ServletConfig config) {
        return printWidgetWithValues(widget, data, isFilterOperation, true, true, request, config);
    }

    public String printWidgetWithValues(OMElement widget, OMElement data,
                                        boolean isFilterOperation, boolean markReadonly, boolean hasValue, HttpServletRequest request,
                                        ServletConfig config) {
        if (isFilterOperation && Boolean.toString(false).equals(
                widget.getAttributeValue(new QName(null, UIGeneratorConstants.FILTER_ATTRIBUTE)))) {
            return "";
        }
        int columns = 2; //default value of number of columns is 2
        String widgetName = widget.getAttributeValue(new QName(null, UIGeneratorConstants.ARGUMENT_NAME));
        boolean collapsed = true;  // Default collapse set to true
        String widgetCollapse = widget.getAttributeValue(new QName(null, UIGeneratorConstants.WIDGET_COLLAPSED));
        if (widgetCollapse != null) {
            collapsed = Boolean.valueOf(widgetCollapse);
        }

        String divId = "_collapse_id_" + widgetName.replaceAll(" ", "");

        OMElement dataHead = null;
        if (data != null) {
            dataHead = AddServicesUtil.getChildWithName(data, widgetName, dataNamespace);
        }
        if (widget.getAttributeValue(new QName(null, UIGeneratorConstants.WIDGET_COLUMN)) != null) {
            columns = Integer.parseInt(widget.getAttributeValue(new QName(null, UIGeneratorConstants.WIDGET_COLUMN)));
        }
        Iterator subHeadingIt = widget.getChildrenWithName(new QName(null, UIGeneratorConstants.SUBHEADING_ELEMENT));
        StringBuilder table = new StringBuilder();
        table.append("<div id=\"" + divId + "\"  "+"onmouseover='title=\"\"' onmouseout='title=\""+String.valueOf(collapsed)+"\"'"
                + " title=\"" + String.valueOf(collapsed) + "\"><table class=\"normal-nopadding\" cellspacing=\"0\">");
        List<String> subList = new ArrayList<String>();
        OMElement sub = null;
        if (subHeadingIt != null && subHeadingIt.hasNext()) {
            sub = (OMElement) subHeadingIt.next(); // NO need to have multiple subheading elements in a single widget element
        }
        if (sub != null && UIGeneratorConstants.SUBHEADING_ELEMENT.equals(sub.getLocalName())) {
            Iterator headingList = sub.getChildrenWithLocalName(UIGeneratorConstants.HEADING_ELEMENT);
            while (headingList.hasNext()) {
                OMElement subheading = (OMElement) headingList.next();
                subList.add(subheading.getText());
            }
            if (subList.size() > columns) {
                /*This is the place where special scenario comes in to play with number of columns other
              than having two columns
                */
                return ""; // TODO: throw an exception
            }
        }
        table.append(printMainHeader(widgetName, columns));
        if (subList.size() > 2) {
            //if the column size is not 2 we print sub-headers first before going in to loop
            //In this table there should not be any field with maxOccurs unbounded//
            table.append(printSubHeaders(subList.toArray(new String[subList.size()])));
        }
        Iterator arguments = widget.getChildrenWithLocalName(UIGeneratorConstants.ARGUMENT_ELMENT);
        int columnCount = 0;
        int rowCount = 0;
        OMElement inner = null;
        while (arguments.hasNext()) {
            OMElement arg = (OMElement) arguments.next();
            String maxOccurs = "";
            if (UIGeneratorConstants.ARGUMENT_ELMENT.equals(arg.getLocalName())) {
                if (isFilterOperation && Boolean.toString(false).equals(
                        arg.getAttributeValue(new QName(null, UIGeneratorConstants.FILTER_ATTRIBUTE)))) {
                    continue;
                }
                rowCount++; //this variable used to find the which raw is in and use this to print the sub header
                String elementType = arg.getAttributeValue(new QName(null, UIGeneratorConstants.TYPE_ATTRIBUTE));
                //Read the maxOccurs value
                maxOccurs = arg.getAttributeValue(new QName(null, UIGeneratorConstants.MAXOCCUR_ELEMENT));
                if (maxOccurs != null) {
                    if (!UIGeneratorConstants.MAXOCCUR_BOUNDED.equals(maxOccurs) && !UIGeneratorConstants.MAXOCCUR_UNBOUNDED.equals(maxOccurs)) {
                        //if user has given something else other than unbounded
                        return ""; //TODO: throw an exception
                    }
                    if (!UIGeneratorConstants.MAXOCCUR_UNBOUNDED.equals(maxOccurs)) {
                        //if maxOccurs is not unbounded then print the sub header otherwise we will show the adding link
                        if (rowCount == 1) {
                            // We print the sub header only when we parse the first element otherwise we'll print sub header for each field element
                            table.append(printSubHeaders(subList.toArray(new String[subList.size()])));
                        }
                    }
                } else {
                    if (subList.size() == 2 && rowCount == 1) {
                        // We print the sub header only when we parse the first element otherwise we'll print sub header for each field element
                        // sub headers are printed in this position only if column number is exactly 2//
                        table.append(printSubHeaders(subList.toArray(new String[subList.size()])));
                    }
                }
                if (dataHead != null) {
                    //if the data xml contains the main element then get the element contains value
                    inner = AddServicesUtil.getChildWithName(dataHead, arg.getFirstChildWithName
                            (new QName(null, UIGeneratorConstants.ARGUMENT_NAME)).getText().replaceAll(" ", "-"),
                            dataNamespace);
                }
                String value = null;
                String optionValue = null;
                if (UIGeneratorConstants.TEXT_FIELD.equals(elementType)) {
                    String mandat = arg.getAttributeValue(new QName(null, UIGeneratorConstants.MANDETORY_ATTRIBUTE));

                    boolean isReadOnly = false;

                    if (markReadonly && "true".equals(arg.getAttributeValue(new QName(null, UIGeneratorConstants.READONLY_ATTRIBUTE)))) {
                        isReadOnly = true;
                    }
                    if (isFilterOperation) {
                        mandat = "false";
                    }
                    boolean isURL = Boolean.toString(true).equals(arg.getAttributeValue(
                            new QName(null, UIGeneratorConstants.URL_ATTRIBUTE)));
                    String urlTemplate = arg.getAttributeValue(
                            new QName(null, UIGeneratorConstants.URL_TEMPLATE_ATTRIBUTE));
                    boolean isPath = Boolean.toString(true).equals(arg.getAttributeValue(
                            new QName(null, UIGeneratorConstants.PATH_ATTRIBUTE)));
                    if (inner != null) {
                        //if the element contains value is not null get the value
                        value = inner.getText();
                    } else {
                        value = arg.getAttributeValue(new QName(null, UIGeneratorConstants.DEFAULT_ATTRIBUTE));
                    }
                    if (columns > 2) {
                        if (columnCount == 0) {
                            table.append("<tr>");
                        }
                        if (value != null) {
                            table.append(printTextSkipName(arg.getFirstChildWithName(new QName(null,
                                    UIGeneratorConstants.ARGUMENT_NAME)).getText(), widgetName, value, isURL, urlTemplate, isPath, isReadOnly, hasValue, request));
                        } else {
                            table.append(printTextSkipName(arg.getFirstChildWithName(new QName(null,
                                    UIGeneratorConstants.ARGUMENT_NAME)).getText(), widgetName, isPath, isReadOnly, request));
                        }
                        columnCount++;
                        if (columnCount == columns) {
                            table.append("</tr>");
                            columnCount = 0;
                        }

                    } else {
                        OMElement firstChildWithName = arg.getFirstChildWithName(
                                new QName(null, UIGeneratorConstants.ARGUMENT_NAME));
                        String name = firstChildWithName.getText();
                        String label = firstChildWithName.getAttributeValue(
                                new QName(UIGeneratorConstants.ARGUMENT_LABEL));

                        if (label == null) {
                            label = name;
                        }

                        if (value != null) {
                            table.append(printTextField(label, name, mandat, widgetName, value, isURL, urlTemplate, isPath, isReadOnly, hasValue, request));
                        } else {
                            table.append(printTextField(label, name, mandat, widgetName, isPath, isReadOnly, request));
                        }

                    }
                } else if (UIGeneratorConstants.OPTION_FIELD.equals(elementType)) {
                    OMElement firstChildWithName = arg.getFirstChildWithName(
                            new QName(null, UIGeneratorConstants.ARGUMENT_NAME));
                    String name = firstChildWithName.getText();
                    String label = firstChildWithName.getAttributeValue(
                            new QName(UIGeneratorConstants.ARGUMENT_LABEL));

                    if (label == null) {
                        label = name;
                    }

                    if (inner != null) {
                        //if the element contains value is not null get the value
                        optionValue = inner.getText();
                    }
                    List<String> optionValues = getOptionValues(arg, request, config);
                    if (isFilterOperation) {
                        optionValues.add(0, "");
                    }
                    if (columns > 2) {
                        if (columnCount == 0) {
                            table.append("<tr>");
                        }
                        if (optionValue != null) {
                            table.append(printDropDownSkipName(name,
                                    optionValues.toArray(new String[optionValues.size()]),
                                    widgetName, optionValue));
                        } else {
                            table.append(printDropDownSkipName(name,
                                    optionValues.toArray(new String[optionValues.size()]),
                                    widgetName));
                        }
                        columnCount++;
                        if (columnCount == columns) {
                            table.append("</tr>");
                            columnCount = 0;
                        }

                    } else {
                        if (optionValue != null) {
                            table.append(printDropDown(label, name,
                                    optionValues.toArray(new String[optionValues.size()]),
                                    widgetName, optionValue));
                        } else {
                            table.append(printDropDown(label, name,
                                    optionValues.toArray(new String[optionValues.size()]),
                                    widgetName));
                        }
                    }
                } else if (UIGeneratorConstants.CHECKBOX_FIELD.equals(elementType)) {
                    String name = arg.getFirstChildWithName(
                            new QName(null, UIGeneratorConstants.ARGUMENT_NAME)).getText();
                    if (inner != null) {
                        //if the element contains value is not null get the value
                        optionValue = inner.getText();
                    }
                    if (columns > 2) {
                        if (columnCount == 0) {
                            table.append("<tr>");
                        }
                        table.append(printCheckboxSkipName(name, widgetName, optionValue));
                        columnCount++;
                        if (columnCount == columns) {
                            table.append("</tr>");
                            columnCount = 0;
                        }

                    } else {
                        table.append(printCheckbox(name, widgetName, optionValue));
                    }
                } else if (UIGeneratorConstants.TEXT_AREA_FIELD.equals(elementType)) {
                    String mandet = arg.getAttributeValue(new QName(null, UIGeneratorConstants.MANDETORY_ATTRIBUTE));
                    String richText = arg.getAttributeValue(new QName(null, UIGeneratorConstants.IS_RICH_TEXT));

                    boolean isReadOnly = false;

                    if (markReadonly && "true".equals(arg.getAttributeValue(new QName(null, UIGeneratorConstants.READONLY_ATTRIBUTE)))) {
                        isReadOnly = true;
                    }

                    boolean isRichText = false; //By default rich text is off
                    if (richText != null) {
                        isRichText = Boolean.valueOf(richText);
                    }

                    if (isFilterOperation) {
                        mandet = "false";
                    }
                    if (inner != null) {
                        //if the element contains value is not null get the value
                        value = inner.getText();
                    }
                    int height = -1;
                    int width = -1;
                    String heightString = arg.getAttributeValue(new QName(null, UIGeneratorConstants.HEIGHT_ATTRIBUTE));
                    if (heightString != null) {
                        try {
                            height = Integer.parseInt(heightString);
                        } catch (NumberFormatException ignored) {
                        }
                    }
                    String widthString = arg.getAttributeValue(new QName(null, UIGeneratorConstants.WIDTH_ATTRIBUTE));
                    if (widthString != null) {
                        try {
                            width = Integer.parseInt(widthString);
                        } catch (NumberFormatException ignored) {
                        }
                    }
                    if (columns > 2) {
                        if (columnCount == 0) {
                            table.append("<tr>");
                        }
                        if (value != null) {
                            table.append(printTextAreaSkipName(arg.getFirstChildWithName(
                                    new QName(null, UIGeneratorConstants.ARGUMENT_NAME)).getText(), widgetName, value, height, width, isReadOnly));
                        } else {
                            table.append(printTextAreaSkipName(arg.getFirstChildWithName(
                                    new QName(null, UIGeneratorConstants.ARGUMENT_NAME)).getText(), widgetName, height, width, isReadOnly));
                        }
                        columnCount++;
                        if (columnCount == columns) {
                            table.append("</tr>");
                            columnCount = 0;
                        }
                    } else {
                        OMElement firstChildWithName = arg.getFirstChildWithName(
                                new QName(null, UIGeneratorConstants.ARGUMENT_NAME));
                        String name = firstChildWithName.getText();
                        String label = firstChildWithName.getAttributeValue(
                                new QName(UIGeneratorConstants.ARGUMENT_LABEL));

                        if (label == null) {
                            label = name;
                        }

                        if (value != null) {
                            table.append(printTextArea(label, name, mandet, widgetName, value, height, width, isReadOnly, isRichText));
                        } else {
                            table.append(printTextArea(label, name, mandet, widgetName, height, width, isReadOnly, isRichText));
                        }
                    }
                } else if (UIGeneratorConstants.OPTION_TEXT_FIELD.equals(elementType)) {
                    if (UIGeneratorConstants.MAXOCCUR_UNBOUNDED.equals(maxOccurs)) {
                        // This is the code segment to run in maxoccur unbounded situation
//                        String addedItems = "0";
//                        if(dataHead != null){
//                            addedItems = dataHead.getFirstChildWithName(new QName(null,UIGeneratorConstants.COUNT)).getText();
//                        }
                        OMElement firstChildWithName = arg.getFirstChildWithName(
                                new QName(null, UIGeneratorConstants.ARGUMENT_NAME));
                        String name = firstChildWithName.getText();
                        String label = firstChildWithName.getAttributeValue(
                                new QName(UIGeneratorConstants.ARGUMENT_LABEL));

                        if (label == null) {
                            label = name;
                        }
                        boolean isURL = Boolean.toString(true).equals(arg.getAttributeValue(
                                new QName(null, UIGeneratorConstants.URL_ATTRIBUTE)));
                        String urlTemplate = arg.getAttributeValue(
                                new QName(null, UIGeneratorConstants.URL_TEMPLATE_ATTRIBUTE));
                        boolean isPath = Boolean.toString(true).equals(arg.getAttributeValue(
                                new QName(null, UIGeneratorConstants.PATH_ATTRIBUTE)));


//                        String addedOptionValues [] = new String[Integer.parseInt(addedItems)];
//                        String addedValues[] = new String[Integer.parseInt(addedItems)];
                        List<String> addedOptionValues = new ArrayList<String>();
                        List<String> addedValues = new ArrayList<String>();
                        int addedItemsCount = 0;
                        if (dataHead != null) {
                            //if the element contains value is not null get the value
                            // with option-text field we put text value like this text_value.replaceAll(" ","-")
                            Iterator itemChildIt = dataHead.getChildElements();
                            int i = 0;
                            while (itemChildIt.hasNext()) {
                                // get all the filled values to the newly added fields
                                Object itemChildObj = itemChildIt.next();
                                if (!(itemChildObj instanceof OMElement)) {
                                    continue;
                                }
                                OMElement itemChildEle = (OMElement) itemChildObj;

                                if (!(itemChildEle.getQName().equals(new QName(dataNamespace,
                                        UIGeneratorConstants.ENTRY_FIELD)))) {
                                    continue;
                                }

                                String entryText = itemChildEle.getText();
                                String entryKey = null;
                                String entryVal;
                                int colonIndex = entryText.indexOf(":");
                                if (colonIndex < entryText.length() - 1) {
                                    entryKey = entryText.substring(0, colonIndex);
                                    entryText = entryText.substring(colonIndex + 1);
                                }
                                entryVal = entryText;

                                if (entryKey != null && !entryKey.equals("")) {
                                    addedOptionValues.add(entryKey);
                                } else {
                                    addedOptionValues.add("0");
                                }

                                if (entryVal != null) {
                                    addedValues.add(entryVal);
                                }

                                i++;
                            }
                            addedItemsCount = i;
                        }
                        /* if there are no added items headings of the table will hide,else display */
                        if (addedItemsCount == 0) {
                            table.append(printAddLink(label, name,
                                    UIGeneratorConstants.ADD_ICON_PATH,
                                    widgetName,
                                    subList.toArray(new String[subList.size() + 1]), isPath));
                        } else if (addedItemsCount > 0) {
                            table.append(printAddLinkWithDisplay(label, name,
                                    UIGeneratorConstants.ADD_ICON_PATH,
                                    widgetName,
                                    subList.toArray(new String[subList.size() + 1]), isPath));
                        }
                        List<String> optionValues = getOptionValues(arg, request, config);
                        if (addedItemsCount > 0) {
                            // This is the place where we fill already added entries
                            for (int i = 0; i < addedItemsCount; i++) {
                                String addedOptionValue = addedOptionValues.get(i);
                                String addedValue = addedValues.get(i);
                                if (addedOptionValue != null && addedValue != null) {
                                    table.append(printOptionTextWithId(name, (i + 1),
                                            optionValues.toArray(new String[optionValues.size()]),
                                            widgetName,
                                            addedOptionValue,
                                            addedValue,
                                            isURL, urlTemplate, isPath, request));
                                }
                            }
                        }
                        table.append(printCloseAddLink(name, addedItemsCount)); // add the previously added items and then close the tbody
                    } else {
                        OMElement firstChildWithName = arg.getFirstChildWithName(
                                new QName(null, UIGeneratorConstants.ARGUMENT_NAME));
                        String name = firstChildWithName.getText();
                        String label = firstChildWithName.getAttributeValue(
                                new QName(UIGeneratorConstants.ARGUMENT_LABEL));

                        if (label == null) {
                            label = name;
                        }

                        boolean isURL = Boolean.toString(true).equals(arg.getAttributeValue(
                                new QName(null, UIGeneratorConstants.URL_ATTRIBUTE)));
                        String urlTemplate = arg.getAttributeValue(
                                new QName(null, UIGeneratorConstants.URL_TEMPLATE_ATTRIBUTE));
                        boolean isPath = Boolean.toString(true).equals(arg.getAttributeValue(
                                new QName(null, UIGeneratorConstants.PATH_ATTRIBUTE)));
                        if (dataHead != null) {
                            //if the element contains value is not null get the value
                            // with option-text field we put text value like this text_value.replaceAll(" ","-")

                            inner = AddServicesUtil.getChildWithName(dataHead, UIGeneratorConstants.TEXT_FIELD +
                                    arg.getFirstChildWithName(
                                            new QName(null, UIGeneratorConstants.ARGUMENT_NAME)).getText(),
                                    dataNamespace);
                            if (inner != null) {
                                value = inner.getText();
                            }
                            OMElement optionValueElement = AddServicesUtil.getChildWithName(dataHead, arg.getFirstChildWithName
                                    (new QName(null, UIGeneratorConstants.ARGUMENT_NAME)).getText(),
                                    dataNamespace);
                            if (optionValueElement != null) {
                                optionValue = optionValueElement.getText();
                            }

                        }
                        List<String> optionValues = getOptionValues(arg, request, config);
                        if (optionValue != null && value != null) {
                            table.append(printOptionText(label, name,
                                    optionValues.toArray(new String[optionValues.size()]),
                                    widgetName, optionValue, value, isURL, urlTemplate, isPath,
                                    request));
                        } else {
                            table.append(printOptionText(label, name,
                                    optionValues.toArray(new String[optionValues.size()]),
                                    widgetName, isPath, request));
                        }
                    }
                }
            }
        }
        table.append("</table></div>");
        return table.toString();
    }

    public String printMainHeader(String header, int columns) {
        StringBuilder head = new StringBuilder();
        head.append("<thead><tr><th style=\"border-right:0\" colspan=\"" + columns + "\">");
        head.append(header);
        head.append("</th></tr></thead>");
        return head.toString();

    }

    public String printSubHeaders(String[] headers) {
        StringBuilder subHeaders = new StringBuilder();
        subHeaders.append("<tr>");
        for (String header : headers) {
            subHeaders.append("<td class=\"sub-header\">");
            subHeaders.append((header == null) ? "" : header);
            subHeaders.append("</td>");
        }
        subHeaders.append("<td class=\"sub-header\"></td>");
        subHeaders.append("</tr>");
        return subHeaders.toString();
    }

    public String printTextField(String label, String name, String mandatory, String widget, boolean isPath, boolean isReadOnly, HttpServletRequest request) {
        StringBuilder element = new StringBuilder();
        String selectResource = "";
        String id = "id_" + widget.replaceAll(" ", "_") + "_" + name.replaceAll(" ", "-");
        if (isPath) {
            selectResource = " <input type=\"button\" class=\"button\" value=\"..\" title=\"" + CarbonUIUtil.geti18nString("select.path",
                    "org.wso2.carbon.governance.services.ui.i18n.Resources", request.getLocale()) + "\" onclick=\"showGovernanceResourceTree('" + id + "');\"/>";
        }
        if ("true".equals(mandatory)) {
            element.append("<tr><td class=\"leftCol-big\">" + label + "<span class=\"required\">*</span></td>\n" +
                    " <td><input type=\"text\" name=\"" + widget.replaceAll(" ", "_") + "_" + name.replaceAll(" ", "-")
                    + "\" id=\"" + id + "\" style=\"width:" + UIGeneratorConstants.DEFAULT_WIDTH + "px\"" + (isReadOnly ? " readonly" : "") + "/>" + (isPath ? selectResource : "") + "</td></tr>");
        } else {
            element.append("<tr><td class=\"leftCol-big\">" + label + "</td>\n" +
                    " <td><input type=\"text\" name=\"" + widget.replaceAll(" ", "_") + "_" + name.replaceAll(" ", "-")
                    + "\" id=\"" + id + "\" style=\"width:" + UIGeneratorConstants.DEFAULT_WIDTH + "px\"" + (isReadOnly ? " readonly" : "") + "/>" + (isPath ? selectResource : "") + "</td></tr>");
        }
        return element.toString();
    }

    public String printDropDown(String label, String name, String[] values, String widget) {
        StringBuilder dropDown = new StringBuilder();
        dropDown.append("<tr><td class=\"leftCol-big\">" + label + "</td>\n" +
                "<td><select name=\"" + widget.replaceAll(" ", "_") + "_" + name.replaceAll(" ", "-") + "\">");

        for (int i = 0; i < values.length; i++) {
            dropDown.append("<option value=\"" + values[i] + "\">");
            dropDown.append(values[i]);
            dropDown.append("</option>");
        }
        dropDown.append("</select></td></tr>");
        return dropDown.toString();
    }

    public String printTextArea(String label, String name, String mandatory, String widget, int height, int width, boolean isReadOnly, boolean isRichText) {
        StringBuilder element = new StringBuilder();
        StringBuilder size = new StringBuilder("style=\"");
        if (height > 0) {
            size.append("height:").append(height).append("px;");
        }
        if (width > 0) {
            size.append("width:").append(width).append("px\"");
        } else {
            size.append("width:").append(UIGeneratorConstants.DEFAULT_WIDTH).append("px\"");
        }
        if ("true".equals(mandatory)) {
            if (isRichText) {
                element.append("<tr><td class=\"leftCol-big\">" + label + "<span class=\"required\">*</span></td>\n" +
                        " <td  style=\"font-size:8px\" class=\"yui-skin-sam\"><textarea  name=\"" + widget.replaceAll(" ", "_") + "_" + name.replaceAll(" ", "-") + "\" id=\"id_"
                        + widget.replaceAll(" ", "_") + "_" + name.replaceAll(" ", "-") + "\" " + size  + (isReadOnly ? " readonly" : "") + " ></textarea>");
                element = appendRichTextScript(element, width, height, widget, name);
                element.append("</td></tr>");

            } else {
                element.append("<tr><td class=\"leftCol-big\">" + label + "<span class=\"required\">*</span></td>\n" +
                        " <td><textarea  name=\"" + widget.replaceAll(" ", "_") + "_" + name.replaceAll(" ", "-") + "\" id=\"id_"
                        + widget.replaceAll(" ", "_") + "_" + name.replaceAll(" ", "-") + "\" " + size  + (isReadOnly ? " readonly" : "") + " ></textarea></td></tr>");
                element = appendEmptyScript(element, widget, name);
                element.append("</td></tr>");

            }

        } else {
            if (isRichText) {
                element.append("<tr><td class=\"leftCol-big\">" + label + "</td>\n" +
                        " <td  style=\"font-size:8px\" class=\"yui-skin-sam\"><textarea  name=\"" + widget.replaceAll(" ", "_") + "_" + name.replaceAll(" ", "-") + "\" id=\"id_"
                        + widget.replaceAll(" ", "_") + "_" + name.replaceAll(" ", "-") + "\" " + size  + (isReadOnly ? " readonly" : "") + " ></textarea>");
                element = appendRichTextScript(element, width, height, widget, name);
                element.append("</td></tr>");

            } else {
                element.append("<tr><td class=\"leftCol-big\">" + label + "</td>\n" +
                        " <td><textarea  name=\"" + widget.replaceAll(" ", "_") + "_" + name.replaceAll(" ", "-") + "\" id=\"id_"
                        + widget.replaceAll(" ", "_") + "_" + name.replaceAll(" ", "-") + "\" " + size  + (isReadOnly ? " readonly" : "") + " ></textarea></td></tr>");
                element = appendEmptyScript(element, widget, name);
                element.append("</td></tr>");

            }
        }
        return element.toString();
    }

    public String printTextAreaSkipName(String name, String widget, int height, int width, boolean isReadOnly) {
        StringBuilder element = new StringBuilder();
        StringBuilder size = new StringBuilder();
        if (height > 0 || width > 0) {
            size = size.append("");
            if (height > 0) {
                size.append("height:").append(height).append("px;");
            }
            if (width > 0) {
                size.append("width:").append(width).append("px\"");
            }
            size.append("\"");
        }
        element.append("<td><textarea  name=\"" + widget.replaceAll(" ", "_") + "_" + name.replaceAll(" ", "-") + "\" " +
                "id=\"id_" + widget.replaceAll(" ", "_") + "_" + name.replaceAll(" ", "-") + "\" " + size + (isReadOnly ? " readonly" : "") + " ></textarea></td>");
        return element.toString();
    }

    public String printTextSkipName(String name, String widget, boolean isPath, boolean isReadOnly, HttpServletRequest request) {
        StringBuilder element = new StringBuilder();
        String id = "id_" + widget.replaceAll(" ", "_") + "_" + name.replaceAll(" ", "-");
        String selectResource = "";
        if (isPath) {
            selectResource = " <input type=\"button\" class=\"button\" value=\"..\" title=\"" + CarbonUIUtil.geti18nString("select.path",
                    "org.wso2.carbon.governance.services.ui.i18n.Resources", request.getLocale()) + "\" onclick=\"showGovernanceResourceTree('" + id + "');\"/>";
        }
        element.append("<td><input type=\"text\" name=\"" + widget.replaceAll(" ", "_") + "_" + name.replaceAll(" ", "-")
                + "\" id=\"" + id + "\"" + (isReadOnly ? " readonly" : "") + " />" + (isPath ? selectResource : "") + "</td>");
        return element.toString();
    }

    public String printDropDownSkipName(String name, String[] values, String widget) {
        StringBuilder dropDown = new StringBuilder();
        dropDown.append("<td><select name=\"" + widget.replaceAll(" ", "_") + "_" + name.replaceAll(" ", "-") + "\">");
        for (int i = 0; i < values.length; i++) {
            dropDown.append("<option value=\"" + values[i] + "\">");
            dropDown.append(values[i]);
            dropDown.append("</option>");
        }
        dropDown.append("</select></td>");
        return dropDown.toString();
    }

    public String printOptionText(String label, String name, String[] values, String widget, boolean isPath, HttpServletRequest request) {
        StringBuilder dropDown = new StringBuilder();
        dropDown.append("<tr><td class=\"leftCol-big\"><select name=\"" + widget.replaceAll(" ", "_") + "_" + name.replaceAll(" ", "-") + "\">");
        for (int i = 0; i < values.length; i++) {
            dropDown.append("<option value=\"" + values[i] + "\">");
            dropDown.append(values[i]);
            dropDown.append("</option>");
        }
        dropDown.append("</select></td>");
        String id = "id_" + widget.replaceAll(" ", "_") + "_" + name.replaceAll(" ", "-");
        String selectResource = "";
        if (isPath) {
            selectResource = " <input type=\"button\" class=\"button\" value=\"..\" title=\"" + CarbonUIUtil.geti18nString("select.path",
                    "org.wso2.carbon.governance.services.ui.i18n.Resources", request.getLocale()) + "\" onclick=\"showGovernanceResourceTree('" + id + "');\"/>";
        }
        dropDown.append("<td width=500px><input type=\"text\" name=\"" + widget.replaceAll(" ", "_") + UIGeneratorConstants.TEXT_FIELD
                + "_" + name.replaceAll(" ", "-") + "\" id=\"" + id + "\" style=\"width:" + UIGeneratorConstants.DEFAULT_WIDTH + "px\"/>" + (isPath ? selectResource : "") + "</td>");
        dropDown.append("<td><a class=\"icon-link\" title=\"delete\" onclick=\"" + "delete" + name.replaceAll(" ", "-") + "_" + widget.replaceAll(" ", "_") + "(this.parentNode.parentNode.rowIndex)\" style=\"background-image:url(../admin/images/delete.gif);\">Delete</a></td>");
        dropDown.append("</tr>");
        return dropDown.toString();
    }

    public String printTextField(String label, String name, String mandatory, String widget, String value, boolean isURL, String urlTemplate, boolean isPath, boolean isReadOnly,
                                 boolean hasValue, HttpServletRequest request) {
        StringBuilder element = new StringBuilder();
        String id = "id_" + widget.replaceAll(" ", "_") + "_" + name.replaceAll(" ", "-");
        String selectResource = "";
        String selectResourceButton = "$('" + id + "_button').style.display='';";
        if (isPath) {
            selectResource = " <input id=\"" + id + "_button\" type=\"button\" class=\"button\" value=\"..\" title=\"" + CarbonUIUtil.geti18nString("select.path",
                    "org.wso2.carbon.governance.services.ui.i18n.Resources", request.getLocale()) + "\" onclick=\"showGovernanceResourceTree('" + id + "');\"/>";
        }
        String div = "<div id=\"" + id + "_link\"><a target=\"_blank\" href=\"" + (isPath ? "../resources/resource.jsp?region=region3&item=resource_browser_menu&path=" + RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH : "") + (urlTemplate != null ? urlTemplate.replace("@{value}", value) : value) + "\">" + value + "</a>" +
                "&nbsp;<a onclick=\"$('" + id + "_link').style.display='none';$('" + id +
                "')." +
                "style.display='';" + (isPath ? selectResourceButton : "") + "\" title=\"" + CarbonUIUtil.geti18nString("edit",
                "org.wso2.carbon.governance.services.ui.i18n.Resources", request.getLocale()) +
                "\" " +
                "class=\"icon-link\" style=\"background-image: url('../admin/images/edit.gif');float: none\"></a></div>";
        //+ (hasValue ? "value=\"" + value + "\"" : "") +
        if ("true".equals(mandatory)) {
            element.append("<tr><td class=\"leftCol-big\">" + label + "<span class=\"required\">*</span></td>\n" +
                    " <td>" + (isURL ? div : "") + "<input" + (isURL ? " style=\"display:none\"" : "") + " type=\"text\" name=\"" + widget.replaceAll(" ", "_") + "_" + name.replaceAll(" ", "-")
                    + "\"" + (hasValue ? "value=\"" + value + "\"" : "") + " id=\"" + id + "\" style=\"width:" + UIGeneratorConstants.DEFAULT_WIDTH + "px\"" + (isReadOnly ? " readonly" : "") + "/>" + (isPath ? selectResource : "") + "</td></tr>");
        } else {
            element.append("<tr><td class=\"leftCol-big\">" + label + "</td>\n" +
                    " <td>" + (isURL ? div : "") + "<input" + (isURL ? " style=\"display:none\"" : "") + " type=\"text\" name=\"" + widget.replaceAll(" ", "_") + "_" + name.replaceAll(" ", "-")
                    + "\"" + (hasValue ? "value=\"" + value + "\"" : "") + " id=\"" + id + "\" style=\"width:" + UIGeneratorConstants.DEFAULT_WIDTH + "px\"" + (isReadOnly ? " readonly" : "") + "/>" + (isPath ? selectResource : "") + "</td></tr>");
        }
        return element.toString();
    }

    public String printDropDown(String label, String name, String[] values, String widget, String value) {
        StringBuilder dropDown = new StringBuilder();
        dropDown.append("<tr><td class=\"leftCol-big\">" + label + "</td>\n" +
                "<td><select name=\"" + widget.replaceAll(" ", "_") + "_" + name.replaceAll(" ", "-") + "\">");
        for (int i = 0; i < values.length; i++) {
            dropDown.append("<option value=\"" + values[i] + "\"");
            if (values[i].equals(value)) {
                dropDown.append(" selected>");
            } else {
                dropDown.append(">");
            }
            dropDown.append(values[i]);
            dropDown.append("</option>");
        }
        dropDown.append("</select></td></tr>");
        return dropDown.toString();
    }

    public String printCheckbox(String name, String widget, String value) {
        if (Boolean.toString(true).equals(value)) {
            return "<tr><td class=\"leftCol-big\">" + name + "</td>\n" +
                    "<td><input type=\"checkbox\" checked=\"checked\" name=\"" +
                    widget.replaceAll(" ", "_") + "_" + name.replaceAll(" ", "-") +
                    "\" value=\"true\" /></td>";
        } else {
            return "<tr><td class=\"leftCol-big\">" + name + "</td>\n" +
                    "<td><input type=\"checkbox\" name=\"" + widget.replaceAll(" ", "_") + "_" +
                    name.replaceAll(" ", "-") + "\" value=\"true\" /></td>";
        }
    }

    public String printTextArea(String label, String name, String mandatory, String widget, String value, int height, int width, boolean isReadOnly, boolean isRichText) {
        StringBuilder element = new StringBuilder();
        StringBuilder size = new StringBuilder("style=\"");
        if (height > 0) {
            size.append("height:").append(height).append("px;");
        }
        if (width > 0) {
            size.append("width:").append(width).append("px\"");
        } else {
            size.append("width:").append(UIGeneratorConstants.DEFAULT_WIDTH).append("px\"");
        }
        if ("true".equals(mandatory)) {
            if (isRichText) {
                element.append("<tr><td class=\"leftCol-big\">" + label + "<span class=\"required\">*</span></td>\n" +
                        " <td  style=\"font-size:8px\" class=\"yui-skin-sam\"><textarea  name=\"" + widget.replaceAll(" ", "_") + "_" + name.replaceAll(" ", "-") + "\" id=\"id_"
                        + widget.replaceAll(" ", "_") + "_" + name.replaceAll(" ", "-") + "\" " + size  + (isReadOnly ? " readonly" : "") + " >" + value + "</textarea>");
                element = appendRichTextScript(element, width, height, widget, name);
                element.append("</td></tr>");

            } else {
                element.append("<tr><td class=\"leftCol-big\">" + label + "<span class=\"required\">*</span></td>\n" +
                        " <td><textarea  name=\"" + widget.replaceAll(" ", "_") + "_" + name.replaceAll(" ", "-") + "\" id=\"id_"
                        + widget.replaceAll(" ", "_") + "_" + name.replaceAll(" ", "-") + "\" " + size  + (isReadOnly ? " readonly" : "") + " >" + value + "</textarea>");
                element = appendEmptyScript(element, widget, name);
                element.append("</td></tr>");

            }
        } else {
            if (isRichText) {
                element.append("<tr><td class=\"leftCol-big\">" + label + "<span class=\"required\">*</span></td>\n" +
                        " <td  style=\"font-size:8px\" class=\"yui-skin-sam\"><textarea  name=\"" + widget.replaceAll(" ", "_") + "_" + name.replaceAll(" ", "-") + "\" id=\"id_"
                        + widget.replaceAll(" ", "_") + "_" + name.replaceAll(" ", "-") + "\" " + size  + (isReadOnly ? " readonly" : "") + " >" + value + "</textarea>");
                element = appendRichTextScript(element, width, height, widget, name);
                element.append("</td></tr>");

            } else {
                element.append("<tr><td class=\"leftCol-big\">" + label + "</td>\n" +
                        " <td><textarea  name=\"" + widget.replaceAll(" ", "_") + "_" + name.replaceAll(" ", "-") + "\" id=\"id_"
                        + widget.replaceAll(" ", "_") + "_" + name.replaceAll(" ", "-") + "\" " + size  + (isReadOnly ? " readonly" : "") + " >" + value + "</textarea>");
                element = appendEmptyScript(element, widget, name);
                element.append("</td></tr>");

            }
        }
        return element.toString();
    }

    private StringBuilder appendEmptyScript(StringBuilder element, String widget, String name) {
        //Create a empty JS function to avoid errors in rich text false state;
        String eleName = "id_" + widget.replaceAll(" ", "_") + "_" + name.replaceAll(" ", "-");
        String fun_name = "set_" + eleName;
        element.append("<script>\n");
        element.append("function " + fun_name + "(){}");
        element.append("</script>");
        return element;
    }

    private StringBuilder appendRichTextScript(StringBuilder element, int width, int height, String widget, String name) {
        String attrName = widget.replaceAll(" ", "_") + "_" + name.replaceAll(" ", "-");
        String eleName = "id_" + widget.replaceAll(" ", "_") + "_" + name.replaceAll(" ", "-");
        String ele_id = "_id_" + widget.replaceAll(" ", "_") + "_" + name.replaceAll(" ", "-");
        String fun_name = "set_" + eleName;
        String richTextAttrName = "yui_txt_" + eleName;
        element.append("<script>\n" +
                "\n" + "var " + richTextAttrName + ";\n" +
                "(function() {\n" +
                "    var Dom = YAHOO.util.Dom,\n" +
                "        Event = YAHOO.util.Event;\n" +
                "    \n" +
                "    var myConfig = {\n" +
                "        height: '" + "120" + "px',\n" +
                "        width: '" + "400" + "px',\n" +
                "        dompath: true,\n" +
                "        focusAtStart: true\n" +
                "    };\n" +
                "\n" +
                "    YAHOO.log('Create the Editor..', 'info', 'example');\n" +
                "    " + richTextAttrName + " = new YAHOO.widget.SimpleEditor('" + eleName + "', myConfig);\n" +
                "    " + richTextAttrName + ".render();\n" +
                "\n" +
                "})();\n");

        element.append("function " + fun_name + "(){\n" +
                "        var form1 = document.getElementById('CustomUIForm');\n" +
                "        var newInput = document.createElement('input');\n" +
                "        newInput.setAttribute('type','hidden');\n" +
                "        newInput.setAttribute('name','" + attrName + "');\n" +
                "        newInput.setAttribute('id','" + ele_id + "');\n" +
                "        form1.appendChild(newInput);" +

                "    var contentText=\"\";\n" +
                "    " + richTextAttrName + ".saveHTML();\n" +
                "    contentText = " + richTextAttrName + ".get('textarea').value;\n" +
                "    document.getElementById(\"" + ele_id + "\").value = contentText;\n" +
                "}");

        element.append("</script>");

        return element;
    }

    public String printTextAreaSkipName(String name, String widget, String value, int height, int width, boolean isReadOnly) {
        StringBuilder element = new StringBuilder();
        StringBuilder size = new StringBuilder();
        if (height > 0 || width > 0) {
            size = size.append("");
            if (height > 0) {
                size.append("height:").append(height).append("px;");
            }
            if (width > 0) {
                size.append("width:").append(width).append("px\"");
            }
            size.append("\"");
        }
        element.append("<td><textarea  name=\"" + widget.replaceAll(" ", "_") + "_" + name.replaceAll(" ", "-") + "\" " +
                "id=\"id_" + widget.replaceAll(" ", "_") + "_" + name.replaceAll(" ", "-") + "\" " + size  + (isReadOnly ? " readonly" : "") + " >" + value + "</textarea></td>");
        return element.toString();
    }

    public String printTextSkipName(String name, String widget, String value, boolean isURL, String urlTemplate, boolean isPath, boolean isReadOnly, boolean hasValue, HttpServletRequest request) {
        StringBuilder element = new StringBuilder();
        String id = "id_" + widget.replaceAll(" ", "_") + "_" + name.replaceAll(" ", "-");
        if (isURL) {
            String selectResource = "";
            String selectResourceButton = "$('" + id + "_button').style.display='';";
            if (isPath) {
                selectResource = " <inputid=\"" + id + "_button\" type=\"button\" class=\"button\" value=\"..\" title=\"" + CarbonUIUtil.geti18nString("select.path",
                        "org.wso2.carbon.governance.services.ui.i18n.Resources", request.getLocale()) + "\" onclick=\"showGovernanceResourceTree('" + id + "');\"/>";
            }
            String div = "<div id=\"" + id + "_link\"><a target=\"_blank\" href=\"" + (isPath ? "../resources/resource.jsp?region=region3&item=resource_browser_menu&path=" + RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH : "") + (urlTemplate != null ? urlTemplate.replace("@{value}", value) : value) + "\">" + value + "</a>" +
                    "&nbsp;<a onclick=\"$('" + id + "_link').style.display='none';$('" + id +
                    "')." +
                    "style.display='';" + (isPath ? selectResourceButton : "") + "\" title=\"" + CarbonUIUtil.geti18nString("edit",
                    "org.wso2.carbon.governance.services.ui.i18n.Resources", request.getLocale()) +
                    "\" " +
                    "class=\"icon-link\" style=\"background-image: url('../admin/images/edit.gif');float: none\"></a></div>";
            element.append("<td>" + div + "<input style=\"display:none\" type=\"text\" name=\"" + widget.replaceAll(" ", "_") + "_" + name.replaceAll(" ", "-")
                    + "\"" + (hasValue ? "value=\"" + value + "\"" : "") + " id=\"" + id + "\"" + (isReadOnly ? " readonly" : "") + " />" + (isPath ? selectResource : "") + "</td>");
        } else {
            String selectResource = "";
            if (isPath) {
                selectResource = " <input type=\"button\" class=\"button\" value=\"..\" title=\"" + CarbonUIUtil.geti18nString("select.path",
                        "org.wso2.carbon.governance.services.ui.i18n.Resources", request.getLocale()) + "\" onclick=\"showGovernanceResourceTree('" + id + "');\"/>";
            }
            element.append("<td><input type=\"text\" name=\"" + widget.replaceAll(" ", "_") + "_" + name.replaceAll(" ", "-")
                    + "\"" + (hasValue ? "value=\"" + value + "\"" : "") + " id=\"" + id + "\"" + (isReadOnly ? " readonly" : "") + " />" + (isPath ? selectResource : "") + "</td>");
        }
        return element.toString();
    }

    public String printDropDownSkipName(String name, String[] values, String widget, String value) {
        StringBuilder dropDown = new StringBuilder();
        dropDown.append("<td><select name=\"" + widget.replaceAll(" ", "_") + "_" + name.replaceAll(" ", "-") + "\">");

        for (int i = 0; i < values.length; i++) {
            dropDown.append("<option value=\"" + values[i] + "\"");
            if (values[i].equals(value)) {
                dropDown.append(" selected>");
            } else {
                dropDown.append(">");
            }
            dropDown.append(values[i]);
            dropDown.append("</option>");
        }
        dropDown.append("</select></td>");
        return dropDown.toString();
    }

    public String printCheckboxSkipName(String name, String widget, String value) {
        if (Boolean.toString(true).equals(value)) {
            return "<td><input type=\"checkbox\" checked=\"checked\" name=\"" +
                    widget.replaceAll(" ", "_") + "_" + name.replaceAll(" ", "-") +
                    "\" value=\"true\" /></td>";
        } else {
            return "<td><input type=\"checkbox\" name=\"" + widget.replaceAll(" ", "_") + "_" +
                    name.replaceAll(" ", "-") + "\" value=\"true\" /></td>";
        }
    }

    public String printOptionText(String label, String name, String[] values, String widget, String option, String text, boolean isURL, String urlTemplate, boolean isPath, HttpServletRequest request) {
        StringBuilder dropDown = new StringBuilder();
        dropDown.append("<tr><td class=\"leftCol\"><select name=\"" + widget.replaceAll(" ", "_") + "_" + name.replaceAll(" ", "-") + "\">");
        for (int i = 0; i < values.length; i++) {
            dropDown.append("<option value=\"" + values[i] + "\"");
            if (values[i].equals(option)) {
                dropDown.append(" selected>");
            } else {
                dropDown.append(">");
            }
            dropDown.append(values[i]);
            dropDown.append("</option>");
        }
        dropDown.append("</select></td>");
        String id = "id_" + widget.replaceAll(" ", "_") + "_" + name.replaceAll(" ", "-");
        if (isURL) {
            String selectResource = "";
            String selectResourceButton = "$('" + id + "_button').style.display='';";
            if (isPath) {
                selectResource = " <input style=\"display:none\" id=\"" + id + "_button\" type=\"button\" class=\"button\" value=\"..\" title=\"" + CarbonUIUtil.geti18nString("select.path",
                        "org.wso2.carbon.governance.services.ui.i18n.Resources", request.getLocale()) + "\" onclick=\"showGovernanceResourceTree('" + id + "');\"/>";
            }
            String div = "<div id=\"" + id + "_link\"><a target=\"_blank\" href=\"" + (isPath ? "../resources/resource.jsp?region=region3&item=resource_browser_menu&path=" + RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH : "") + (urlTemplate != null ? urlTemplate.replace("@{value}", text) : text) + "\">" + text + "</a>" +
                    "&nbsp;<a onclick=\"$('" + id + "_link').style.display='none';$('" + id +
                    "')." +
                    "style.display='';" + (isPath ? selectResourceButton : "") + "\" title=\"" + CarbonUIUtil.geti18nString("edit",
                    "org.wso2.carbon.governance.services.ui.i18n.Resources", request.getLocale()) +
                    "\" " +
                    "class=\"icon-link\" style=\"background-image: url('../admin/images/edit.gif');float: none\"></a></div>";
            dropDown.append("<td>" + div + "<input style=\"display:none\" type=\"text\" name=\"" + widget.replaceAll(" ", "_") + UIGeneratorConstants.TEXT_FIELD
                    + "_" + name.replaceAll(" ", "-") + "\" value=\"" + text + "\" id=\"" + id + "\" style=\"width:400px\"/>" + (isPath ? selectResource : "") + "</td>");

        } else {
            String selectResource = "";
            if (isPath) {
                selectResource = " <input type=\"button\" class=\"button\" value=\"..\" title=\"" + CarbonUIUtil.geti18nString("select.path",
                        "org.wso2.carbon.governance.services.ui.i18n.Resources", request.getLocale()) + "\" onclick=\"showGovernanceResourceTree('" + id + "');\"/>";
            }
            dropDown.append("<td width=500px><input type=\"text\" name=\"" + widget.replaceAll(" ", "_") + UIGeneratorConstants.TEXT_FIELD
                    + "_" + name.replaceAll(" ", "-") + "\" value=\"" + text + "\" id=\"" + id + "\" style=\"width:" + UIGeneratorConstants.DEFAULT_WIDTH + "px\"/>" + (isPath ? selectResource : "") + "</td>");
        }
        dropDown.append("<td><a class=\"icon-link\" title=\"delete\" onclick=\"" + "delete" + name.replaceAll(" ", "-") + "_" + widget.replaceAll(" ", "_") + "(this.parentNode.parentNode.rowIndex)\" style=\"background-image:url(../admin/images/delete.gif);\">Delete</a></td>");
        dropDown.append("</tr>");
        return dropDown.toString();
    }

    public String printOptionTextWithId(String originalName, int index, String[] values, String widget, String option, String text, boolean isURL, String urlTemplate, boolean isPath, HttpServletRequest request) {
        String name = originalName + index;
        StringBuilder dropDown = new StringBuilder();
        dropDown.append("<tr><td class=\"leftCol\"><select name=\"" + widget.replaceAll(" ", "_") + "_" + name.replaceAll(" ", "-") + "\">");
        String id = "id_" + widget.replaceAll(" ", "_") + "_" + name.replaceAll(" ", "-");
        for (int i = 0; i < values.length; i++) {
            dropDown.append("<option value=\"" + values[i] + "\"");
            if (values[i].equals(option)) {
                dropDown.append(" selected>");
            } else {
                dropDown.append(">");
            }
            dropDown.append(values[i]);
            dropDown.append("</option>");
        }
        dropDown.append("</select></td>");
        if (isURL) {
            String selectResource = "";
            String selectResourceButton = "$('" + id + "_button').style.display='';";
            if (isPath) {
                selectResource = " <input style=\"display:none\" id=\"" + id + "_button\" type=\"button\" class=\"button\" value=\"..\" title=\"" + CarbonUIUtil.geti18nString("select.path",
                        "org.wso2.carbon.governance.services.ui.i18n.Resources", request.getLocale()) + "\" onclick=\"showGovernanceResourceTree('" + id + "');\"/>";
            }
            String div = "<div id=\"" + id + "_link\"><a target=\"_blank\" href=\"" + (isPath ? "../resources/resource.jsp?region=region3&item=resource_browser_menu&path=" + RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH : "") + (urlTemplate != null ? urlTemplate.replace("@{value}", text) : text) + "\">" + text + "</a>" +
                    "&nbsp;<a onclick=\"$('" + id + "_link').style.display='none';$('" + id +
                    "')." +
                    "style.display='';" + (isPath ? selectResourceButton : "") + "\" title=\"" + CarbonUIUtil.geti18nString("edit",
                    "org.wso2.carbon.governance.services.ui.i18n.Resources", request.getLocale()) +
                    "\" " +
                    "class=\"icon-link\" style=\"background-image: url('../admin/images/edit.gif');float: none\"></a></div>";
            dropDown.append("<td>" + div + "<input style=\"display:none\" type=\"text\" name=\"" + widget.replaceAll(" ", "_") + UIGeneratorConstants.TEXT_FIELD
                    + "_" + name.replaceAll(" ", "-") + "\" value=\"" + text + "\" id=\"" + id + "\" style=\"width:400px\"/>" + (isPath ? selectResource : "") + "</td>");
        } else {
            String selectResource = "";
            if (isPath) {
                selectResource = " <input type=\"button\" class=\"button\" value=\"..\" title=\"" + CarbonUIUtil.geti18nString("select.path",
                        "org.wso2.carbon.governance.services.ui.i18n.Resources", request.getLocale()) + "\" onclick=\"showGovernanceResourceTree('" + id + "');\"/>";
            }
            dropDown.append("<td width=500px><input type=\"text\" name=\"" + widget.replaceAll(" ", "_") + UIGeneratorConstants.TEXT_FIELD
                    + "_" + name.replaceAll(" ", "-") + "\" value=\"" + text + "\" id=\"" + id + "\" style=\"width:400px\"/>" + (isPath ? selectResource : "") + "</td>");
        }
        dropDown.append("<td><a class=\"icon-link\" title=\"delete\" onclick=\"" + "delete" + originalName.replaceAll(" ", "-") + "_" + widget.replaceAll(" ", "_") + "(this.parentNode.parentNode.rowIndex)\" style=\"background-image:url(../admin/images/delete.gif);\">Delete</a></td>");
        dropDown.append("</tr>");
        return dropDown.toString();
    }

    public String printAddLink(String label, String name, String addIconPath, String widget, String[] subList, boolean isPath) {
        StringBuilder link = new StringBuilder();
        link.append("<tr><td colspan=\"3\"><a class=\"icon-link\" style=\"background-image: url(");
        link.append(addIconPath);
        link.append(");\" onclick=\"");
        link.append("add" + name.replaceAll(" ", "-") + "_" + widget.replaceAll(" ", "_") + "(" + (isPath ? "'path'" : "") + ")\">"); //creating a JavaScript onclick method name which should be identical ex: addEndpoint_Endpoint
        link.append("Add " + label.replaceAll(" ", "-")); //This is the display string for add item ex: Add EndPoint
        link.append("</a></td></tr>");
        link.append("<tr><td colspan=\"3\">");
        link.append("<table class=\"styledLeft\" style=\"display:none;border: 1px solid rgb(204, 204, 204) ! important;\"><thead>" +
                printSubHeaders(subList) +
                "</thead><tbody id=\"" + name.replaceAll(" ", "-") + "Mgt\">");
        return link.toString();
    }

    public String printAddLinkWithDisplay(String label, String name, String addIconPath, String widget, String[] subList, boolean isPath) {
        StringBuilder link = new StringBuilder();
        link.append("<tr><td colspan=\"3\"><a class=\"icon-link\" style=\"background-image: url(");
        link.append(addIconPath);
        link.append(");\" onclick=\"");
        link.append("add" + name.replaceAll(" ", "-") + "_" + widget.replaceAll(" ", "_") + "(" + (isPath ? "'path'" : "") + ")\">"); //creating a JavaScript onclick method name which should be identical ex: addEndpoint_Endpoint
        link.append("Add " + label.replaceAll(" ", "-")); //This is the display string for add item ex: Add EndPoint
        link.append("</a></td></tr>");
        link.append("<tr><td colspan=\"3\">");
        link.append("<table class=\"styledLeft\" style=\"border: 1px solid rgb(204, 204, 204) ! important;\"><thead>" +
                printSubHeaders(subList) +
                "</thead><tbody id=\"" + name.replaceAll(" ", "-") + "Mgt\">");
        return link.toString();
    }

    public String printCloseAddLink(String name, int count) {
        StringBuilder link = new StringBuilder();
        link.append("</tbody></table>");
        link.append("<input id=\"" + name.replaceAll(" ", "-") + "CountTaker\" type=\"hidden\" value=\"" +
                count + "\" name=\"");
        link.append(name.replaceAll(" ", "-") + UIGeneratorConstants.COUNT + "\"/>\n");

        link.append("</td></tr>");
        return link.toString();
    }

    /* This is the method which extract information from the UI and embedd them to xml using value elements */
    public OMElement getDataFromUI(OMElement head, HttpServletRequest request) {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace namespace = fac.createOMNamespace(dataNamespace, "");
        OMElement data = fac.createOMElement(dataElement, namespace);
        Iterator it = head.getChildrenWithName(new QName(UIGeneratorConstants.WIDGET_ELEMENT));
        while (it.hasNext()) {
            OMElement widget = (OMElement) it.next();
            String widgetName = widget.getAttributeValue(new QName(null, UIGeneratorConstants.ARGUMENT_NAME));
            OMElement widgetData = fac.createOMElement(AddServicesUtil.getDataElementName(widgetName),
                    namespace);
            Iterator arguments = widget.getChildrenWithLocalName(UIGeneratorConstants.ARGUMENT_ELMENT);
            OMElement arg = null;
            while (arguments.hasNext()) {
                arg = (OMElement) arguments.next();
                if (UIGeneratorConstants.ARGUMENT_ELMENT.equals(arg.getLocalName())) {
                    String elementType = arg.getAttributeValue(new QName(null, UIGeneratorConstants.TYPE_ATTRIBUTE));
                    String name = arg.getFirstChildWithName(new QName(null, UIGeneratorConstants.ARGUMENT_NAME)).getText();
                    if (UIGeneratorConstants.OPTION_TEXT_FIELD.equals(elementType)) {
                        if (UIGeneratorConstants.MAXOCCUR_UNBOUNDED.equals(
                                arg.getAttributeValue(new QName(null, UIGeneratorConstants.MAXOCCUR_ELEMENT)))) {
                            //implement the new way of extracting data if the maxoccurs unbounded happend in option-text field
                            String count = request.getParameter(name.replaceAll(" ", "-") + UIGeneratorConstants.COUNT);

                            for (int i = 0; i < Integer.parseInt(count); i++) {
                                String entryValue = "";
                                String input = request.getParameter(widgetName.replaceAll(" ", "_") +
                                        "_" + name.replaceAll(" ", "-") + (i + 1));
                                if (input != null && !("".equals(input)) && !("None".equals(input))) {
                                    entryValue += input;
                                }
                                entryValue += ":";
                                String inputTextValue = request.getParameter(widgetName.replaceAll(" ", "_") +
                                        UIGeneratorConstants.TEXT_FIELD +
                                        "_" + name.replaceAll(" ", "-") + (i + 1));
                                if (inputTextValue != null && !("".equals(inputTextValue))) {
                                    entryValue += inputTextValue;
                                }
                                if (!":".equals(entryValue)) {
                                    OMElement entryElement = fac.createOMElement(UIGeneratorConstants.ENTRY_FIELD,
                                            namespace);
                                    entryElement.setText(entryValue);
                                    widgetData.addChild(entryElement);
                                }
                            }

                        }
                        // if maxoccurs unbounded is not mentioned use the default behaviour
                        else {
                            String input = request.getParameter(widgetName.replaceAll(" ", "_") + "_" +
                                    name.replaceAll(" ", "-"));
                            if (input != null && !("".equals(input)) && !("None".equals(input))) {
                                OMElement text = fac.createOMElement(AddServicesUtil.getDataElementName(name),
                                        namespace);
                                text.setText(input);
                                widgetData.addChild(text);
                            }
                            String inputOption = request.getParameter(widgetName.replaceAll(" ", "_") +
                                    UIGeneratorConstants.TEXT_FIELD +
                                    "_" + name.replaceAll(" ", "-"));
                            if (inputOption != null && !("".equals(inputOption))) {
                                OMElement value = fac.createOMElement(
                                        AddServicesUtil.getDataElementName(UIGeneratorConstants.TEXT_FIELD + name),
                                        namespace);
                                value.setText(inputOption);
                                widgetData.addChild(value);
                            }
                        }
                    } else {
                        String input = request.getParameter(widgetName.replaceAll(" ", "_") + "_" +
                                name.replaceAll(" ", "-"));
                        OMElement text = null;

                        if (input != null && !("".equals(input)) && !("None".equals(input))) {
                            text = fac.createOMElement(AddServicesUtil.getDataElementName(name), namespace);
                            text.setText(input);
                            widgetData.addChild(text);

                        } else {
                            if (name.equals("Name")) {
                                text = fac.createOMElement(AddServicesUtil.getDataElementName(name), namespace);
                                text.setText(GovernanceConstants.DEFAULT_SERVICE_NAME);
                                widgetData.addChild(text);
                            }
                        }

                    }
                }
            }
            data.addChild(widgetData);
        }
        return AddServicesUtil.addExtraElements(data, request);
    }

    public String[] getMandatoryIdList(OMElement head) {
        List<String> id = new ArrayList<String>();
        Iterator it = head.getChildrenWithName(new QName(UIGeneratorConstants.WIDGET_ELEMENT));
        while (it.hasNext()) {
            OMElement widget = (OMElement) it.next();
            String widgetName = widget.getAttributeValue(new QName(null, UIGeneratorConstants.ARGUMENT_NAME));
            Iterator arguments = widget.getChildrenWithLocalName(UIGeneratorConstants.ARGUMENT_ELMENT);
            OMElement arg = null;
            while (arguments.hasNext()) {
                arg = (OMElement) arguments.next();
                if (UIGeneratorConstants.ARGUMENT_ELMENT.equals(arg.getLocalName())) {
                    String name = arg.getFirstChildWithName(new QName(null, UIGeneratorConstants.ARGUMENT_NAME)).getText();
                    //check the mandatory fields and get the id's of them
                    if (arg.getAttributeValue(new QName(null, UIGeneratorConstants.MANDETORY_ATTRIBUTE)) != null) {
                        id.add("id_" + widgetName.replaceAll(" ", "_") + "_" + name.replaceAll(" ", "-"));
                    }
                }
            }
        }
        return id.toArray(new String[id.size()]);
    }

    public String[] getMandatoryNameList(OMElement head) {
        List<String> name = new ArrayList<String>();
        Iterator it = head.getChildrenWithName(new QName(UIGeneratorConstants.WIDGET_ELEMENT));
        while (it.hasNext()) {
            OMElement widget = (OMElement) it.next();
            Iterator arguments = widget.getChildrenWithLocalName(UIGeneratorConstants.ARGUMENT_ELMENT);
            OMElement arg = null;
            while (arguments.hasNext()) {
                arg = (OMElement) arguments.next();
                if (UIGeneratorConstants.ARGUMENT_ELMENT.equals(arg.getLocalName())) {
                    String name_element = arg.getFirstChildWithName(new QName(null, UIGeneratorConstants.ARGUMENT_NAME)).getText();
                    if (arg.getAttributeValue(new QName(null, UIGeneratorConstants.MANDETORY_ATTRIBUTE)) != null) {
                        name.add(name_element);
                    }
                }
            }
        }
        return name.toArray(new String[name.size()]);
    }

    public String[] getUnboundedNameList(OMElement head) {
        List<String> name = new ArrayList<String>();
        Iterator it = head.getChildrenWithName(new QName(UIGeneratorConstants.WIDGET_ELEMENT));
        while (it.hasNext()) {
            OMElement widget = (OMElement) it.next();
            Iterator arguments = widget.getChildrenWithLocalName(UIGeneratorConstants.ARGUMENT_ELMENT);
            OMElement arg = null;
            while (arguments.hasNext()) {
                arg = (OMElement) arguments.next();
                if (UIGeneratorConstants.ARGUMENT_ELMENT.equals(arg.getLocalName())) {
                    //check the unbounded fields and get the names of them
                    if (UIGeneratorConstants.OPTION_TEXT_FIELD.equals(arg.getAttributeValue(new QName(null, UIGeneratorConstants.TYPE_ATTRIBUTE)))) {
                        //previous check is used to check the max occur unbounded only with option-text fields with other fields it will ignore
                        if (UIGeneratorConstants.MAXOCCUR_UNBOUNDED.equals(arg.getAttributeValue(new QName(null,
                                UIGeneratorConstants.MAXOCCUR_ELEMENT)))) {
                            name.add(arg.getFirstChildWithName(new QName(null, UIGeneratorConstants.ARGUMENT_NAME)).getText());
                        }
                    }
                }
            }
        }
        return name.toArray(new String[name.size()]);
    }

    public String[] getUnboundedWidgetList(OMElement head) {
        List<String> widgetList = new ArrayList<String>();
        Iterator it = head.getChildrenWithName(new QName(UIGeneratorConstants.WIDGET_ELEMENT));
        while (it.hasNext()) {
            OMElement widget = (OMElement) it.next();
            Iterator arguments = widget.getChildrenWithLocalName(UIGeneratorConstants.ARGUMENT_ELMENT);
            OMElement arg = null;
            while (arguments.hasNext()) {
                arg = (OMElement) arguments.next();
                if (UIGeneratorConstants.ARGUMENT_ELMENT.equals(arg.getLocalName())) {
                    //check the unbounded fields and get the widget names of them
                    if (UIGeneratorConstants.OPTION_TEXT_FIELD.equals(arg.getAttributeValue(new QName(null, UIGeneratorConstants.TYPE_ATTRIBUTE)))) {
                        //previous check is used to check the max occur unbounded only with option-text fields with other fields it will ignore
                        if (UIGeneratorConstants.MAXOCCUR_UNBOUNDED.equals(arg.getAttributeValue(
                                new QName(null, UIGeneratorConstants.MAXOCCUR_ELEMENT)))) {
                            widgetList.add(widget.getAttributeValue(new QName(null, UIGeneratorConstants.WIDGET_NAME)));
                        }
                    }
                }
            }
        }
        return widgetList.toArray(new String[widgetList.size()]);
    }

    public String[][] getUnboundedValues(OMElement head, HttpServletRequest request,
                                         ServletConfig config) {
        List<String[]> values = new ArrayList<String[]>();
        Iterator it = head.getChildrenWithName(new QName(UIGeneratorConstants.WIDGET_ELEMENT));
        while (it.hasNext()) {
            OMElement widget = (OMElement) it.next();
            Iterator arguments = widget.getChildrenWithLocalName(UIGeneratorConstants.ARGUMENT_ELMENT);
            OMElement arg = null;
            while (arguments.hasNext()) {
                arg = (OMElement) arguments.next();
                if (UIGeneratorConstants.ARGUMENT_ELMENT.equals(arg.getLocalName())) {
                    //check the unbounded fields and get the values of drop-down in option-text type
                    if (UIGeneratorConstants.OPTION_TEXT_FIELD.equals(arg.getAttributeValue(new QName(null, UIGeneratorConstants.TYPE_ATTRIBUTE)))) {
                        //previous check is used to check the max occur unbounded only with option-text fields with other fields it will ignore
                        if (UIGeneratorConstants.MAXOCCUR_UNBOUNDED.equals(
                                arg.getAttributeValue(new QName(null, UIGeneratorConstants.MAXOCCUR_ELEMENT)))) {
                            List<String> inner = getOptionValues(arg, request, config);
                            values.add(inner.toArray(new String[inner.size()]));
                        }
                    }
                }
            }
        }
        return values.toArray(new String[0][0]);
    }

    private List<String> getOptionValues(OMElement arg, HttpServletRequest request,
                                         ServletConfig config) {
        OMElement values = arg.getFirstChildWithName(new QName(null,
                UIGeneratorConstants.OPTION_VALUES));
        Iterator iterator = values.getChildrenWithLocalName(UIGeneratorConstants.OPTION_VALUE);
        List<String> inner = new ArrayList<String>();
        if (iterator != null && iterator.hasNext()) {
            while (iterator.hasNext()) {
                inner.add(((OMElement) iterator.next()).getText());
            }
            return inner;
        } else {
            try {
                String className = values.getAttributeValue(new QName(null,
                        UIGeneratorConstants.OPTION_VALUE_CLASS));
                ClassLoader loader = Thread.currentThread().getContextClassLoader();
                Class<?> populatorClass = Class.forName(className, true, loader);

                DropDownDataPopulator populator = (DropDownDataPopulator) populatorClass.newInstance();
                String[] list = populator.getList(request, config);
                return new ArrayList<String>(Arrays.asList(list));
            } catch (ClassNotFoundException e) {
                log.error("Unable to load populator class", e);
            } catch (InstantiationException e) {
                log.error("Unable to load populator class", e);
            } catch (IllegalAccessException e) {
                log.error("Unable to load populator class", e);
            }
        }
        return inner;
    }

}
