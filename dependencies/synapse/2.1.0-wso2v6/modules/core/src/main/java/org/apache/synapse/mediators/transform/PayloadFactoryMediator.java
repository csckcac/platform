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

package org.apache.synapse.mediators.transform;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.soap.SOAPBody;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.synapse.MessageContext;
import org.apache.synapse.mediators.AbstractMediator;

import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PayloadFactoryMediator extends AbstractMediator {

    private String format;
    private List<Argument> argumentList = new ArrayList<Argument>();

    private Pattern pattern = Pattern.compile("\\$(\\d)+");

    public boolean mediate(MessageContext synCtx) {

        SOAPBody soapBody = synCtx.getEnvelope().getBody();

        StringBuffer result = new StringBuffer();
        regexTransform(result, synCtx);

        OMElement resultElement;
        try {
            resultElement = AXIOMUtil.stringToOM(result.toString());
        } catch (XMLStreamException e) {      /*Use the XMLStreamException and log the proper stack trace*/
            handleException("Unable to create a valid XML payload", synCtx);
            return false;
        }

        for (Iterator itr = soapBody.getChildElements(); itr.hasNext();) {
            OMElement child = (OMElement) itr.next();
            child.detach();
        }

        for (Iterator itr = resultElement.getChildElements(); itr.hasNext();) {
            OMElement child = (OMElement) itr.next();
            soapBody.addChild(child);
        }

        return true;
    }

    /* ToDO : Return string buffer*/
    private void regexTransform(StringBuffer result, MessageContext synCtx) {
        Object[] argValues = getArgValues(synCtx);
        Matcher matcher = pattern.matcher("<dummy>" + format + "</dummy>");
        while (matcher.find()) {
            String matchSeq = matcher.group();
            int argIndex = Integer.parseInt(matchSeq.substring(1));
            matcher.appendReplacement(result, argValues[argIndex - 1].toString());
        }
        matcher.appendTail(result);
    }

    private Object[] getArgValues(MessageContext synCtx) {

        Object[] argValues = new Object[argumentList.size()];
        for (int i = 0; i < argumentList.size(); ++i) {       /*ToDo use foreach*/
            Argument arg = argumentList.get(i);
            if (arg.getValue() != null) {
                argValues[i] = arg.getValue();
            } else if (arg.getExpression() != null) {
                String value = arg.getExpression().stringValueOf(synCtx);   /*ToDo We can change this to string array*/
                if (value != null) {
                    //escaping string unless there might be exceptions when tries to insert values
                    // such as string with &
                    value = StringEscapeUtils.escapeXml(value);
                    argValues[i] = value;
                } else {
                    argValues[i] = "";
                }
            } else {
                handleException("Unexpected arg type detected", synCtx);
            }
        }
        return argValues;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public void addArgument(Argument arg) {
        argumentList.add(arg);
    }

    public List<Argument> getArgumentList() {
        return argumentList;
    }

}
