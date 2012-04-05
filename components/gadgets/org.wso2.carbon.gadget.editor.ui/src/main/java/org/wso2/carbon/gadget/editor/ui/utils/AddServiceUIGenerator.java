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
package org.wso2.carbon.gadget.editor.ui.utils;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.xpath.AXIOMXPath;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.namespace.QName;
import javax.servlet.http.HttpServletRequest;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

public class AddServiceUIGenerator {
    StringBuffer serviceui;
    public OMElement getUIconfiguration(String content)throws Exception{
        OMElement omElement = null;
        try {
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(content));
            StAXOMBuilder builder = new StAXOMBuilder(reader);
            omElement = builder.getDocumentElement();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        return omElement;
    }

   /*
    public String printwidgetwithvalues(OMElement widget,OMElement data, boolean markRequired){
        int columns = 2; //default value of number of columns is 2
        String widgetname = widget.getAttributeValue(new QName(null,UIGeneratorConstants.ARGUMENT_NAME));
        OMElement datahead = null;
        if(data != null){
            datahead = data.getFirstChildWithName(new QName(null,widgetname.replaceAll(" ","-")));
        }
        if(widget.getAttributeValue(new QName(null,UIGeneratorConstants.WIDGET_COLUMN)) != null){
            columns = Integer.parseInt(widget.getAttributeValue(new QName(null,UIGeneratorConstants.WIDGET_COLUMN)));
        }
        Iterator subheadingit = widget.getChildrenWithName(new QName(null,UIGeneratorConstants.SUBHEADING_ELEMENT));
        StringBuffer table = new StringBuffer();
        table.append("<table class=\"normal-nopadding\" cellspacing=\"0\">");
        List<String> sub_list = new ArrayList();
        OMElement sub = (OMElement)subheadingit.next(); // NO need to have multilple subheading elements in a single widget element
        if(UIGeneratorConstants.SUBHEADING_ELEMENT.equals(sub.getLocalName())){
            Iterator headinglist = sub.getChildrenWithLocalName(UIGeneratorConstants.HEADING_ELEMENT);
            while(headinglist.hasNext()){
                OMElement subheading = (OMElement)headinglist.next();
                sub_list.add(subheading.getText());
            }
            if(sub_list.size() > columns){

                if(columns != sub_list.size()){
                    return "";
                }
            }
        }
        table.append(printmainheader(widgetname,columns));
        if(sub_list.size() > 2){
            //if the column size is not 2 we print subheaders first before going in to loop
            //In this table there should not be any field with maxoccurs unbounded//
            table.append(printsubheaders(sub_list.toArray(new String[0])));
        }
        Iterator arguments = widget.getChildrenWithLocalName(UIGeneratorConstants.ARGUMENT_ELMENT);
        int columncount = 0;
        int rawcount = 0;
        OMElement inner = null;
        while(arguments.hasNext()){
            OMElement arg = (OMElement)arguments.next();
            String maxoccurs = "";
            if(UIGeneratorConstants.ARGUMENT_ELMENT.equals(arg.getLocalName())){
                rawcount++; //this variable used to find the which raw is in and use this to print the subheader
                String elementtype = arg.getAttributeValue(new QName(null,UIGeneratorConstants.TYPE_ATTRIBUTE));
                //Read the maxoccurs value
                if(arg.getAttributeValue(new QName(null,UIGeneratorConstants.MAXOCCUR_ELEMENT)) != null){
                    maxoccurs = arg.getAttributeValue(new QName(null,UIGeneratorConstants.MAXOCCUR_ELEMENT));
                    if(!UIGeneratorConstants.MAXOCCUR_BOUNDED.equals(maxoccurs) && !UIGeneratorConstants.MAXOCCUR_UNBOUNDED.equals(maxoccurs)){
                        //if user has given something else other than unbounded
                        return "";
                    }
                    if(!UIGeneratorConstants.MAXOCCUR_UNBOUNDED.equals(maxoccurs)){
                        //if maxoccurs is not unbounded then print the sub header otherwise we will show the adding link
                        if(rawcount == 1){
                            // We print the subheader only when we parse the firest element otherwise we'll print subheader for each field element
                            table.append(printsubheaders(sub_list.toArray(new String[0])));
                        }
                    }
                }
                else{
                    if(sub_list.size() == 2 && rawcount == 1){
                        // We print the subheader only when we parse the firest element otherwise we'll print subheader for each field element
                        // subheaders are printing in this position only column number is exacly 2//
                        table.append(printsubheaders(sub_list.toArray(new String[0])));
                    }
                }
                if(datahead != null){
                    //if the data xml contains the main element then get the element contains value
                    inner = datahead.getFirstChildWithName(new QName(null,arg.getFirstChildWithName
                            (new QName(null,UIGeneratorConstants.ARGUMENT_NAME)).getText().replaceAll(" ","-")));
                }
                String value = null;
                String optionvalue = null;
                if(UIGeneratorConstants.TEXT_FIELD.equals(elementtype)){
                    String mandet = arg.getAttributeValue(new QName(null,UIGeneratorConstants.MANDETORY_ATTRIBUTE));
                    if (!markRequired) {
                        mandet = "false";
                    }
                    if(inner != null){
                        //if the element contains value is not null get the value
                        value = inner.getText();
                    }
                    if(columns > 2){
                        if(columncount==0){
                            table.append("<tr>");
                        }
                        if(value != null){
                            table.append(printtextskipname(arg.getFirstChildWithName(new QName(null,
                                    UIGeneratorConstants.ARGUMENT_NAME)).getText(),widgetname,value));
                        }
                        else{
                            table.append(printtextskipname(arg.getFirstChildWithName(new QName(null,
                                    UIGeneratorConstants.ARGUMENT_NAME)).getText(),widgetname));
                        }
                        columncount++;
                        if(columncount == columns){
                            table.append("</tr>");
                            columncount = 0;
                        }

                    }
                    else{
                        if(value != null){
                            table.append(printtextfield(arg.getFirstChildWithName(new QName(null,
                                    UIGeneratorConstants.ARGUMENT_NAME)).getText(),mandet,widgetname,value));
                        }
                        else{
                            table.append(printtextfield(arg.getFirstChildWithName(new QName(null,
                                    UIGeneratorConstants.ARGUMENT_NAME)).getText(),mandet,widgetname));
                        }

                    }
                }
                else if(UIGeneratorConstants.OPTION_FIELD.equals(elementtype)){
                    String name = arg.getFirstChildWithName(new QName(null,UIGeneratorConstants.ARGUMENT_NAME)).getText();
                    if(inner != null){
                        //if the element contains value is not null get the value
                        optionvalue = inner.getText();
                    }
                    Iterator values = arg.getFirstChildWithName(new QName(null,
                            UIGeneratorConstants.OPTION_VALUES)).getChildrenWithLocalName(UIGeneratorConstants.OPTION_VALUE);
                    if(values != null){
                        List<String> optionvalues = new ArrayList();
                        while(values.hasNext()){
                            optionvalues.add(((OMElement)values.next()).getText());
                        }
                        if(columns > 2){
                            if(columncount==0){
                                table.append("<tr>");
                            }
                            if(optionvalue != null){
                                table.append(printdropdownskipname(name,optionvalues.toArray(new String[0]),widgetname,optionvalue));
                            }
                            else{
                                table.append(printdropdownskipname(name,optionvalues.toArray(new String[0]),widgetname));
                            }
                            columncount++;
                            if(columncount == columns){
                                table.append("</tr>");
                                columncount = 0;
                            }

                        }
                        else{
                            if(optionvalue != null){
                                table.append(printdropdown(name,optionvalues.toArray(new String[0]),widgetname,optionvalue));
                            }
                            else{
                                table.append(printdropdown(name,optionvalues.toArray(new String[0]),widgetname));
                            }
                        }
                    }
                    else{
                        return "";
                    }
                }
                else if(UIGeneratorConstants.TEXT_AREA_FIELD.equals(elementtype)){
                    String mandet = arg.getAttributeValue(new QName(null,UIGeneratorConstants.MANDETORY_ATTRIBUTE));
                    if (!markRequired) {
                        mandet = "false";
                    }
                    if(inner != null){
                        //if the element contains value is not null get the value
                        value = inner.getText();
                    }
                    if(columns > 2){
                        if(columncount==0){
                            table.append("<tr>");
                        }
                        if(value != null){
                            table.append(printtextareaskipname(arg.getFirstChildWithName(
                                    new QName(null,UIGeneratorConstants.ARGUMENT_NAME)).getText(),widgetname,value));
                        }
                        else{
                            table.append(printtextareaskipname(arg.getFirstChildWithName(
                                    new QName(null,UIGeneratorConstants.ARGUMENT_NAME)).getText(),widgetname));
                        }
                        columncount++;
                        if(columncount == columns){
                            table.append("</tr>");
                            columncount = 0;
                        }
                    }
                    else{
                        if(value != null){
                            table.append(printtextarea(arg.getFirstChildWithName(
                                    new QName(null,UIGeneratorConstants.ARGUMENT_NAME)).getText(),mandet,widgetname,value));
                        }
                        else{
                            table.append(printtextarea(arg.getFirstChildWithName(
                                    new QName(null,UIGeneratorConstants.ARGUMENT_NAME)).getText(),mandet,widgetname));
                        }                                                           
                    }
                }
                else if(UIGeneratorConstants.OPTION_TEXT_FIELD.equals(elementtype)){
                    if(UIGeneratorConstants.MAXOCCUR_UNBOUNDED.equals(maxoccurs)){
                        // This is the code segment to run in maxoccur unbounded situation
                        String addeditems = "0";
                        if(datahead != null){
                            addeditems = datahead.getFirstChildWithName(new QName(null,UIGeneratorConstants.COUNT)).getText();
                        }
                        table.append(printaddlink(arg.getFirstChildWithName(
                                new QName(null,UIGeneratorConstants.ARGUMENT_NAME)).getText(),UIGeneratorConstants.ADD_ICON_PATH,widgetname,sub_list.toArray(new String[0]),addeditems));
                        String name = arg.getFirstChildWithName(new QName(null,UIGeneratorConstants.ARGUMENT_NAME)).getText();

                        String addedoptionvalues [] = new String[Integer.parseInt(addeditems)];
                        String addedvalues[] = new String[Integer.parseInt(addeditems)];
                        if(datahead != null){
                            //if the element contains value is not null get the value
                            // with option-text field we put text value like this text_value.replaceAll(" ","-")
                            for(int i=0;i<addedvalues.length;i++){
                                // get all the filled values to the newly added fields
                                String tempname = arg.getFirstChildWithName
                                        (new QName(null,UIGeneratorConstants.ARGUMENT_NAME)).getText();
                                inner = datahead.getFirstChildWithName(new QName(null,UIGeneratorConstants.TEXT_FIELD +
                                        "_" + (tempname.replaceAll(" ","-") + (i+1))));
                                if(inner != null){
                                    addedvalues[i] = inner.getText();
                                } else {
                                    // there is a possibility that _ is broken to another sub element
                                    try {
                                        inner = evaluateXPathToElement("//" + UIGeneratorConstants.TEXT_FIELD + "/" + 
                                                (tempname.replaceAll(" ","-") + (i+1)), datahead);
                                        if(inner != null){
                                            addedvalues[i] = inner.getText();
                                        }
                                    } catch (Exception ignore) {
                                        // no exception is thrown in failure, just letting it be null
                                    }
                                }
                                if(datahead.getFirstChildWithName(new QName(null,tempname.replaceAll(" ","-") + (i+1))) != null){
                                    addedoptionvalues[i] = datahead.getFirstChildWithName(new QName(null,tempname.replaceAll(" ","-") + (i+1))).getText();
                                }
                                else{
                                    addedoptionvalues[i] = "0";
                                }
                            }
                        }
                        Iterator values = arg.getFirstChildWithName(new QName(null,UIGeneratorConstants.OPTION_VALUES)).
                                getChildrenWithLocalName(UIGeneratorConstants.OPTION_VALUE);
                        if(values != null){
                            List<String> optionvalues = new ArrayList();
                            while(values.hasNext()){
                                optionvalues.add(((OMElement)values.next()).getText());
                            }
                            if(Integer.parseInt(addeditems) > 0){
                                // This is the place where we fill already added entries
                                for(int i=0;i<Integer.parseInt(addeditems);i++){
                                    if(addedoptionvalues[i] != null && addedvalues[i] != null){
                                        table.append(printoptiontextwithid(name+(i+1),optionvalues.toArray(new String[0]),widgetname,addedoptionvalues[i],addedvalues[i],i+1));
                                    }
                                }
                            }
                            else{
                                //just show the add button only 
                            }
                        }
                        table.append(printcloseaddlink()); // add the previously added items and then close the tbody
                    }
                    else{
                        String name = arg.getFirstChildWithName(new QName(null,UIGeneratorConstants.ARGUMENT_NAME)).getText();
                        if(datahead != null){
                            //if the element contains value is not null get the value
                            // with option-text field we put text value like this text_value.replaceAll(" ","-")

                            inner = datahead.getFirstChildWithName(new QName(null,UIGeneratorConstants.TEXT_FIELD + "_" + arg.getFirstChildWithName
                                    (new QName(null,UIGeneratorConstants.ARGUMENT_NAME)).getText().replaceAll(" ","-")));
                            if(inner != null){
                                value = inner.getText();
                            }
                            optionvalue = datahead.getFirstChildWithName(new QName(null,arg.getFirstChildWithName
                                    (new QName(null, UIGeneratorConstants.ARGUMENT_NAME)).getText().replaceAll(" ","-"))).getText();

                        }
                        Iterator values = arg.getFirstChildWithName(new QName(null,UIGeneratorConstants.OPTION_VALUES)).
                                getChildrenWithLocalName(UIGeneratorConstants.OPTION_VALUE);
                        if(values != null){
                            List<String> optionvalues = new ArrayList();
                            while(values.hasNext()){
                                optionvalues.add(((OMElement)values.next()).getText());
                            }
                            if(optionvalue != null && value != null){
                                table.append(printoptiontext(name,optionvalues.toArray(new String[0]),widgetname,optionvalue,value));
                            }
                            else{
                                table.append(printoptiontext(name,optionvalues.toArray(new String[0]),widgetname));
                            }
                        }
                    }
                }
            }
        }
        table.append("</table>");
        return table.toString();
    }
    public String printmainheader(String header,int columns){
        StringBuffer head = new StringBuffer();
        if(columns == 2){
            head.append("<thead><tr><th colspan=\"2\">");
        }
        else{
            head.append("<thead><tr><th colspan=\"" + columns + "\">");
        }
        head.append(header);
        head.append("</th></tr></thead>");
        return head.toString();

    }
    public String printsubheaders(String[] headers){
        StringBuffer subheaders = new StringBuffer();
        subheaders.append("<tr>");
        for(int i=0;i<headers.length;i++){
            subheaders.append("<td class=\"sub-header\">");
            subheaders.append(headers[i]);
            subheaders.append("</td>");
        }
        subheaders.append("</tr>");
        return subheaders.toString();
    }

    public String printtextfield(String name,String mandetory,String widget){
        StringBuffer element = new StringBuffer();
        if("true".equals(mandetory)){
            element.append("<tr><td >" + name + "<span class=\"required\">*</span></td>\n" +
                    " <td><input type=\"text\" name=\""+ widget.replaceAll(" ","_") + "_" + name.replaceAll(" ","-")
                    + "\" id=\"id_" + widget.replaceAll(" ","_") + "_" + name.replaceAll(" ","_") + "\" style=\"width:"+ UIGeneratorConstants.DEFAULT_WIDTH + "px\"/></td></tr>");
        }
        else{
            element.append("<tr><td>" + name + "</td>\n" +
                    " <td><input type=\"text\" name=\"" + widget.replaceAll(" ","_") + "_" + name.replaceAll(" ","-")
                    +"\" id=\"id_" + widget.replaceAll(" ","_") + "_" + name.replaceAll(" ","-") + "\" style=\"width:"+ UIGeneratorConstants.DEFAULT_WIDTH +"px\"/></td></tr>");
        }
        return element.toString();
    }
    public String printdropdown(String name,String[] values,String widget){
        StringBuffer dropdown = new StringBuffer();
        dropdown.append("<tr><td >" + name + "</td>\n" +
                "<td><select name=\"" + widget.replaceAll(" ","_") + "_" + name.replaceAll(" ","-") + "\">");
        for(int i=0;i<values.length;i++){
            dropdown.append("<option value=\"" + i +"\">");
            dropdown.append(values[i]);
            dropdown.append("</option>");
        }
        dropdown.append("</select></td></tr>");
        return dropdown.toString();
    }
    public String printtextarea(String name,String mandetory,String widget){
        StringBuffer element = new StringBuffer();
        if("true".equals(mandetory)){
            element.append("<tr><td >" + name + "<span class=\"required\">*</span></td>\n" +
                    " <td><textarea  name=\""+ widget.replaceAll(" ","_") + "_" + name.replaceAll(" ","-") + "\" id=\"id_"
                    +  widget.replaceAll(" ","_") + "_" +  name + "\" style=\"width:"+ UIGeneratorConstants.DEFAULT_WIDTH +"px\"></textarea></td></tr>");
        }
        else{
            element.append("<tr><td>" + name + "</td>\n" +
                    " <td><textarea  name=\"" + widget.replaceAll(" ","_") + "_" + name.replaceAll(" ","-") +"\" id=\"id_"
                    + widget.replaceAll(" ","_") + "_" + name + "\" style=\"width:"+ UIGeneratorConstants.DEFAULT_WIDTH +"px\"></textarea></td></tr>");
        }
        return element.toString();
    }
    public String printtextareaskipname(String name,String widget){
        StringBuffer element = new StringBuffer();
        element.append("<td><textarea  name=\"" + widget.replaceAll(" ","_") + "_" + name.replaceAll(" ","-") +"\" " +
                "id=\"id_" + widget.replaceAll(" ","_") + "_" + name + "\" ></textarea></td>");
        return element.toString();
    }
    public String printtextskipname(String name,String widget){
        StringBuffer element = new StringBuffer();
        element.append("<td><input type=\"text\" name=\"" + widget.replaceAll(" ","_") + "_" + name.replaceAll(" ","-")
                +"\" id=\"id_" + widget.replaceAll(" ","_") + "_" + name.replaceAll(" ","-") + "\" /></td>");
        return element.toString();
    }
    public String printdropdownskipname(String name,String[] values,String widget){
        StringBuffer dropdown = new StringBuffer();
        dropdown.append("<td><select name=\"" +  widget.replaceAll(" ","_") + "_" + name.replaceAll(" ","-") + "\">");
        for(int i=0;i<values.length;i++){
            dropdown.append("<option value=\"" + i +"\">");
            dropdown.append(values[i]);
            dropdown.append("</option>");
        }
        dropdown.append("</select></td>");
        return dropdown.toString();
    }
    public String printoptiontext(String name,String[] values,String widget){
        StringBuffer dropdown = new StringBuffer();
        dropdown.append("<tr><td><select name=\"" + widget.replaceAll(" ","_") + "_" + name.replaceAll(" ","-") + "\">");
        for(int i=0;i<values.length;i++){
            dropdown.append("<option value=\"" + i +"\">");
            dropdown.append(values[i]);
            dropdown.append("</option>");
        }
        dropdown.append("</select></td>");
        dropdown.append("<td><input type=\"text\" name=\""+ widget.replaceAll(" ","_") + UIGeneratorConstants.TEXT_FIELD
                + "_" + name.replaceAll(" ","-") + "\" id=\"id_" + widget.replaceAll(" ","_") + "_" + name + "\" style=\"width:"+ UIGeneratorConstants.DEFAULT_WIDTH +"px\"/></td></tr>");
        return dropdown.toString();
    }
    public String printtextfield(String name,String mandetory,String widget,String value){
        StringBuffer element = new StringBuffer();
        if("true".equals(mandetory)){
            element.append("<tr><td >" + name + "<span class=\"required\">*</span></td>\n" +
                    " <td><input type=\"text\" name=\""+ widget.replaceAll(" ","_") + "_" + name.replaceAll(" ","-")
                    + "\" value=\"" + value + "\" id=\"id_" + widget.replaceAll(" ","_") + "_" + name.replaceAll(" ","_")
                    + "\" style=\"width:"+ UIGeneratorConstants.DEFAULT_WIDTH +"px\"/></td></tr>");
        }
        else{
            element.append("<tr><td>" + name + "</td>\n" +
                    " <td><input type=\"text\" name=\"" + widget.replaceAll(" ","_") + "_" + name.replaceAll(" ","-")
                    +"\" value=\"" + value + "\"  id=\"id_" + widget.replaceAll(" ","_") + "_" + name.replaceAll(" ","-") + "\" style=\"width:"+ UIGeneratorConstants.DEFAULT_WIDTH +"px\"/></td></tr>");
        }
        return element.toString();
    }
    public String printdropdown(String name,String[] values,String widget,String value){
        StringBuffer dropdown = new StringBuffer();
        dropdown.append("<tr><td >" + name + "</td>\n" +
                "<td><select name=\"" + widget.replaceAll(" ","_") + "_" + name.replaceAll(" ","-") + "\">");
        for(int i=0;i<values.length;i++){
            dropdown.append("<option value=\"" + i + "\"");
            if(Integer.parseInt(value) == i){
                dropdown.append(" selected>");
            }
            else{
                dropdown.append(">");
            }
            dropdown.append(values[i]);
            dropdown.append("</option>");
        }
        dropdown.append("</select></td></tr>");
        return dropdown.toString();
    }
    public String printtextarea(String name,String mandetory,String widget,String value){
        StringBuffer element = new StringBuffer();
        if("true".equals(mandetory)){
            element.append("<tr><td >" + name + "<span class=\"required\">*</span></td>\n" +
                    " <td><textarea  name=\""+ widget.replaceAll(" ","_") + "_" + name.replaceAll(" ","-") + "\" id=\"id_"
                    + widget.replaceAll(" ","_") + "_" + name + "\" style=\"width:"+ UIGeneratorConstants.DEFAULT_WIDTH + "px\">" + value +"</textarea></td></tr>");
        }
        else{
            element.append("<tr><td>" + name + "</td>\n" +
                    " <td><textarea  name=\"" + widget.replaceAll(" ","_") + "_" + name.replaceAll(" ","-") +"\" id=\"id_"
                    + widget.replaceAll(" ","_") + "_" + name + "\" style=\"width:"+ UIGeneratorConstants.DEFAULT_WIDTH +"px\">" + value + "</textarea></td></tr>");
        }
        return element.toString();
    }
    public String printtextareaskipname(String name,String widget,String value){
        StringBuffer element = new StringBuffer();
        element.append("<td><textarea  name=\"" + widget.replaceAll(" ","_") + "_" + name.replaceAll(" ","-") +"\" " +
                "id=\"id_" + widget.replaceAll(" ","_") + "_" + name + "\" >" + value + "</textarea></td>");
        return element.toString();
    }
    public String printtextskipname(String name,String widget,String value){                         
        StringBuffer element = new StringBuffer();
        element.append("<td><input type=\"text\" name=\"" + widget.replaceAll(" ","_") + "_" + name.replaceAll(" ","-")
                +"\"  value=\"" + value + "\" id=\"id_" + widget.replaceAll(" ","_") + "_" + name.replaceAll(" ","-") + "\" /></td>");
        return element.toString();
    }
    public String printdropdownskipname(String name,String[] values,String widget,String value){
        StringBuffer dropdown = new StringBuffer();
        dropdown.append("<td><select name=\"" +  widget.replaceAll(" ","_") + "_" + name.replaceAll(" ","-") + "\">");

        for(int i=0;i<values.length;i++){
            dropdown.append("<option value=\"" + i + "\"");
            if(Integer.parseInt(value) == i){
                dropdown.append(" selected>");
            }
            else{
                dropdown.append(">");
            }
            dropdown.append(values[i]);
            dropdown.append("</option>");
        }
        dropdown.append("</select></td>");
        return dropdown.toString();
    }
    public String printoptiontext(String name,String[] values,String widget,String option,String text){
        StringBuffer dropdown = new StringBuffer();
        dropdown.append("<tr><td><select name=\"" + widget.replaceAll(" ","_") + "_" + name.replaceAll(" ","-") + "\">");
        for(int i=0;i<values.length;i++){
            dropdown.append("<option value=\"" + i + "\"");
            if(Integer.parseInt(option) == i){
                dropdown.append(" selected>");
            }
            else{
                dropdown.append(">");
            }
            dropdown.append(values[i]);
            dropdown.append("</option>");
        }
        dropdown.append("</select></td>");
        dropdown.append("<td><input type=\"text\" name=\""+ widget.replaceAll(" ","_") + UIGeneratorConstants.TEXT_FIELD
                + "_" + name.replaceAll(" ","-") + "\" value=\"" + text + "\" id=\"id_" + widget.replaceAll(" ","_") + "_" + name + "\" style=\"width:400px\"/></td></tr>");
        return dropdown.toString();
    }
     public String printoptiontextwithid(String name,String[] values,String widget,String option,String text,int index){
        StringBuffer dropdown = new StringBuffer();
        dropdown.append("<tr><td><select name=\"" + widget.replaceAll(" ","_") + "_" + name.replaceAll(" ","-") + "\">");
        for(int i=0;i<values.length;i++){
            dropdown.append("<option value=\"" + i + "\"");
            if(Integer.parseInt(option) == i){
                dropdown.append(" selected>");
            }
            else{
                dropdown.append(">");
            }
            dropdown.append(values[i]);
            dropdown.append("</option>");
        }
        dropdown.append("</select></td>");
        dropdown.append("<td><input type=\"text\" name=\""+ widget.replaceAll(" ","_") + UIGeneratorConstants.TEXT_FIELD
                + "_" + name.replaceAll(" ","-") + "\" value=\"" + text + "\" id=\""+ index +"\" style=\"width:400px\"/></td></tr>");
        return dropdown.toString();
    }
    public String printaddlink(String name,String addiconpath,String widget,String[] sublist,String count){
        StringBuffer link = new StringBuffer();
        link.append("<tr><td colspan=\"3\"><a class=\"icon-link\" style=\"background-image: url(");
        link.append(addiconpath);
        link.append(");\" onclick=\"");
        link.append("add" + name.replaceAll(" ","-") + "_" + widget.replaceAll(" ","_") + "()\">"); //creating a JavaScript onclick method name which should be identical ex: addEndpoint_Endpoint
        link.append("Add " + name.replaceAll(" ","-")); //This is the display string for add item ex: Add EndPoint
        link.append("</a></td></tr>");
        link.append("<tr><td colspan=\"3\">" +
                "<input id=\"" + name.replaceAll(" ","-") +"CountTaker\" type=\"hidden\" value=\""+count +"\" name=\"");

        link.append(name.replaceAll(" ","-") + UIGeneratorConstants.COUNT + "\"/>\n" +
                "<table class=\"styledLeft\" style=\"border: 1px solid rgb(204, 204, 204) ! important;\"><thead>" +
                printsubheaders(sublist) +
                "</thead><tbody id=\"" + name.replaceAll(" ","-") + "Mgt\">");
        return link.toString();
    }
    public String printcloseaddlink(){
        StringBuffer link = new StringBuffer();
        link.append("</tbody></table></td></tr>");
        return link.toString();
    }
     public static OMElement getdatafromUI(OMElement head, HttpServletRequest request){
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMElement data = fac.createOMElement(UIGeneratorConstants.DATA_ELEMENT,null);
        Iterator it = head.getChildrenWithName(new QName(UIGeneratorConstants.WIDGET_ELEMENT));
        while(it.hasNext()){
            OMElement widget = (OMElement)it.next();
            String widgetname = widget.getAttributeValue(new QName(null,UIGeneratorConstants.ARGUMENT_NAME));
            OMElement widgetdata = fac.createOMElement(widgetname.replaceAll(" ","-"),null);
            Iterator arguments = widget.getChildrenWithLocalName(UIGeneratorConstants.ARGUMENT_ELMENT);
            OMElement arg = null;
            while(arguments.hasNext()){
                arg = (OMElement)arguments.next();
                if(UIGeneratorConstants.ARGUMENT_ELMENT.equals(arg.getLocalName())){
                    String elementtype = arg.getAttributeValue(new QName(null,UIGeneratorConstants.TYPE_ATTRIBUTE));
                    String name = arg.getFirstChildWithName(new QName(null,UIGeneratorConstants.ARGUMENT_NAME)).getText();
                    if(UIGeneratorConstants.OPTION_TEXT_FIELD.equals(elementtype)){
                        if(UIGeneratorConstants.MAXOCCUR_UNBOUNDED.equals(
                                arg.getAttributeValue(new QName(null,UIGeneratorConstants.MAXOCCUR_ELEMENT)))){
                            //implement the new way of extracting data if the maxoccurs unbounded happend in option-text field
                            String count = request.getParameter(name.replaceAll(" ","-") + UIGeneratorConstants.COUNT);
                            OMElement countelem = fac.createOMElement(UIGeneratorConstants.COUNT,null);
                            countelem.setText(count);
                            widgetdata.addChild(countelem);
                            for(int i=0;i<Integer.parseInt(count);i++){
                                String input = request.getParameter(widgetname.replaceAll(" ","_") + "_" + name.replaceAll(" ","-") + (i+1));
                                if(input != null && !("".equals(input))){
                                    OMElement text = fac.createOMElement(name.replaceAll(" ","-") + (i+1),null);
                                    text.setText(input);
                                    widgetdata.addChild(text);
                                }
                                String inputoption = request.getParameter(widgetname.replaceAll(" ","_") + UIGeneratorConstants.TEXT_FIELD
                                        + "_" + name.replaceAll(" ","-") + (i+1));
                                if(inputoption != null && !("".equals(inputoption))){
                                    OMElement value = fac.createOMElement(UIGeneratorConstants.TEXT_FIELD + "_" + name.replaceAll(" ","-")+ (i+1),null);
                                    value.setText(inputoption);
                                    widgetdata.addChild(value);
                                }
                            }

                        }
                        // if maxoccurs unbounded is not mentioned use the default behaviour
                        else{
                            String input = request.getParameter(widgetname.replaceAll(" ","_") + "_" + name.replaceAll(" ","-"));
                            if(input != null && !("".equals(input))){
                                OMElement text = fac.createOMElement(name.replaceAll(" ","-"),null);
                                text.setText(input);
                                widgetdata.addChild(text);
                            }
                            String inputoption = request.getParameter(widgetname.replaceAll(" ","_") + UIGeneratorConstants.TEXT_FIELD
                                    + "_" + name.replaceAll(" ","-"));
                            if(inputoption != null && !("".equals(inputoption))){
                                OMElement value = fac.createOMElement(UIGeneratorConstants.TEXT_FIELD + "_" + name.replaceAll(" ","-"),null);
                                value.setText(inputoption);
                                widgetdata.addChild(value);
                            }
                        }
                    }
                    else{
                        String input = request.getParameter(widgetname.replaceAll(" ","_") + "_" + name.replaceAll(" ","-"));
                        if(input != null && !("".equals(input))){
                            OMElement text = fac.createOMElement(name.replaceAll(" ","-"),null);
                            text.setText(input);
                            widgetdata.addChild(text);
                        }
                    }
                }
            }
            data.addChild(widgetdata);
        }
        return AddServicesUtil.addExtraElements(data,request);
    }
    public String[] getmandatoryidlist(OMElement head){
        List<String> id = new ArrayList();
        Iterator it = head.getChildrenWithName(new QName(UIGeneratorConstants.WIDGET_ELEMENT));
        while(it.hasNext()){
            OMElement widget = (OMElement)it.next();
            String widgetname = widget.getAttributeValue(new QName(null,UIGeneratorConstants.ARGUMENT_NAME));
            Iterator arguments = widget.getChildrenWithLocalName(UIGeneratorConstants.ARGUMENT_ELMENT);
            OMElement arg = null;
            while(arguments.hasNext()){
                arg = (OMElement)arguments.next();
                if(UIGeneratorConstants.ARGUMENT_ELMENT.equals(arg.getLocalName())){
                    String name = arg.getFirstChildWithName(new QName(null,UIGeneratorConstants.ARGUMENT_NAME)).getText();
                    //check the mandetory fields and get the id's of them
                    if(arg.getAttributeValue(new QName(null,UIGeneratorConstants.MANDETORY_ATTRIBUTE)) != null){
                        id.add("id_" + widgetname.replaceAll(" ","_") + "_" + name.replaceAll(" ","_"));
                    }
                }
            }
        }
        return id.toArray(new String[0]);
    }
    public String[] getmandatorynamelist(OMElement head){
        List<String> name = new ArrayList();
        Iterator it = head.getChildrenWithName(new QName(UIGeneratorConstants.WIDGET_ELEMENT));
        while(it.hasNext()){
            OMElement widget = (OMElement)it.next();
            Iterator arguments = widget.getChildrenWithLocalName(UIGeneratorConstants.ARGUMENT_ELMENT);
            OMElement arg = null;
            while(arguments.hasNext()){
                arg = (OMElement)arguments.next();
                if(UIGeneratorConstants.ARGUMENT_ELMENT.equals(arg.getLocalName())){
                   String name_element = arg.getFirstChildWithName(new QName(null,UIGeneratorConstants.ARGUMENT_NAME)).getText();
	          if(arg.getAttributeValue(new QName(null,UIGeneratorConstants.MANDETORY_ATTRIBUTE)) != null){
                        name.add(name_element);
                    } 
                }
            }
        }
        return name.toArray(new String[0]);
    }
    public String[] getunboundednamelist(OMElement head){
        List<String> name = new ArrayList();
        Iterator it = head.getChildrenWithName(new QName(UIGeneratorConstants.WIDGET_ELEMENT));
        while(it.hasNext()){
            OMElement widget = (OMElement)it.next();
            Iterator arguments = widget.getChildrenWithLocalName(UIGeneratorConstants.ARGUMENT_ELMENT);
            OMElement arg = null;
            while(arguments.hasNext()){
                arg = (OMElement)arguments.next();
                if(UIGeneratorConstants.ARGUMENT_ELMENT.equals(arg.getLocalName())){
                    //check the unbounded fields and get the names of them
                    if(UIGeneratorConstants.OPTION_TEXT_FIELD.equals(arg.getAttributeValue(new QName(null,UIGeneratorConstants.TYPE_ATTRIBUTE)))){
                        //previous check is used to check the max occur unbounded only with option-text fields with other fields it will ignore
                        if(UIGeneratorConstants.MAXOCCUR_UNBOUNDED.equals(arg.getAttributeValue(new QName(null,
                                UIGeneratorConstants.MAXOCCUR_ELEMENT)))){
                            name.add(arg.getFirstChildWithName(new QName(null,UIGeneratorConstants.ARGUMENT_NAME)).getText());
                        }
                    }
                }
            }
        }
        return name.toArray(new String[0]);
    }
    public String[] getunboundedwidgetlist(OMElement head){
        List<String> widgetlist = new ArrayList();
        Iterator it = head.getChildrenWithName(new QName(UIGeneratorConstants.WIDGET_ELEMENT));
        while(it.hasNext()){
            OMElement widget = (OMElement)it.next();
            Iterator arguments = widget.getChildrenWithLocalName(UIGeneratorConstants.ARGUMENT_ELMENT);
            OMElement arg = null;
            while(arguments.hasNext()){
                arg = (OMElement)arguments.next();
                if(UIGeneratorConstants.ARGUMENT_ELMENT.equals(arg.getLocalName())){
                    //check the unbounded fields and get the widget names of them
                    if(UIGeneratorConstants.OPTION_TEXT_FIELD.equals(arg.getAttributeValue(new QName(null,UIGeneratorConstants.TYPE_ATTRIBUTE)))){
                        //previous check is used to check the max occur unbounded only with option-text fields with other fields it will ignore
                        if(UIGeneratorConstants.MAXOCCUR_UNBOUNDED.equals(arg.getAttributeValue(
                                new QName(null,UIGeneratorConstants.MAXOCCUR_ELEMENT)))){
                            widgetlist.add(widget.getAttributeValue(new QName(null,UIGeneratorConstants.WIDGET_NAME)));
                        }
                    }
                }
            }
        }
        return widgetlist.toArray(new String[0]);
    }
    public String[][] getunboundedvalues(OMElement head){
        List<String[]> values = new ArrayList();
        Iterator it = head.getChildrenWithName(new QName(UIGeneratorConstants.WIDGET_ELEMENT));
        while(it.hasNext()){
            OMElement widget = (OMElement)it.next();
            Iterator arguments = widget.getChildrenWithLocalName(UIGeneratorConstants.ARGUMENT_ELMENT);
            OMElement arg = null;
            while(arguments.hasNext()){
                arg = (OMElement)arguments.next();
                if(UIGeneratorConstants.ARGUMENT_ELMENT.equals(arg.getLocalName())){
                    //check the unbounded fields and get the values of dropdown in option-text type
                    if(UIGeneratorConstants.OPTION_TEXT_FIELD.equals(arg.getAttributeValue(new QName(null,UIGeneratorConstants.TYPE_ATTRIBUTE)))){
                        //previous check is used to check the max occur unbounded only with option-text fields with other fields it will ignore
                        if(UIGeneratorConstants.MAXOCCUR_UNBOUNDED.equals(
                                arg.getAttributeValue(new QName(null,UIGeneratorConstants.MAXOCCUR_ELEMENT)))){
                            Iterator iterator = arg.getFirstChildWithName(new QName(null,
                                    UIGeneratorConstants.OPTION_VALUES)).getChildrenWithLocalName(UIGeneratorConstants.OPTION_VALUE);
                            List<String> inner = new ArrayList();
                            while(iterator.hasNext()){
                                inner.add(((OMElement)iterator.next()).getText());
                            }
                            values.add(inner.toArray(new String[0]));
                        }
                    }
                }
            }
        }
        return values.toArray(new String[0][0]);
    }
    public String[] getcurrentcountvalues(OMElement data,OMElement config){
        //This method reads the current count values and return an array containing all of then in the order
        List<String> countlist = new ArrayList();
        Iterator it = data.getChildren();
        while(it.hasNext()){
            OMElement count = (OMElement)it.next();
            Iterator inter = count.getChildrenWithLocalName(UIGeneratorConstants.COUNT);
            while(inter.hasNext()){
                OMElement countelement = (OMElement)inter.next();
                if(UIGeneratorConstants.COUNT.equals(countelement.getLocalName())){
                    countlist.add(countelement.getText());
                }

            }
        }
          String[] unboundedname = this.getunboundednamelist(config);
        while(unboundedname.length > countlist.size()){
            countlist.add("0");
        }
        return countlist.toArray(new String[0]);
    }
        private static OMElement evaluateXPathToElement(String expression,
                                                           OMElement root) throws Exception {
        List<OMElement> nodes = evaluateXPathToElements(expression, root);
        if (nodes == null || nodes.size() == 0) {
            return null;
        }
        return nodes.get(0);
    }

    private static List<OMElement> evaluateXPathToElements(String expression,
                                                           OMElement root) throws Exception {
        AXIOMXPath xpathExpression = new AXIOMXPath(expression);

        return (List<OMElement>)xpathExpression.selectNodes(root);
    }   */
}
