/**
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bam.analyzer.analyzers.builders;


import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.wso2.carbon.bam.analyzer.analyzers.AlertTrigger;
import org.wso2.carbon.bam.analyzer.analyzers.AnalyzerBuilder;
import org.wso2.carbon.bam.analyzer.analyzers.AnalyzerConfig;
import org.wso2.carbon.bam.analyzer.analyzers.configs.AlertConfig;
import org.wso2.carbon.bam.analyzer.engine.Analyzer;
import org.wso2.carbon.bam.analyzer.engine.AnalyzerConfigConstants;
import org.wso2.carbon.bam.analyzer.engine.AnalyzerException;

public class AlertBuilder extends AnalyzerBuilder {
    @Override
    protected AnalyzerConfig buildConfig(OMElement analyzerXML) throws AnalyzerException {
        AlertConfig alertConfig = new AlertConfig();

        OMElement fields = analyzerXML.getFirstChildWithName(AnalyzerConfigConstants.ALERT_FIELDS);
        OMAttribute toEmailAddress = analyzerXML.getAttribute(AnalyzerConfigConstants.TO_EMAIL);
        OMAttribute subject = analyzerXML.getAttribute(AnalyzerConfigConstants.SUBJECT);
        OMAttribute fromEmailAddress = analyzerXML.getAttribute(AnalyzerConfigConstants.FROM_EMAIL);
        OMAttribute mailHost = analyzerXML.getAttribute(AnalyzerConfigConstants.MAILHOST);
        OMAttribute mailUsername = analyzerXML.getAttribute(AnalyzerConfigConstants.MAIL_USERNAME);
        OMAttribute mailPassword = analyzerXML.getAttribute(AnalyzerConfigConstants.MAIL_PASSWORD);
        OMAttribute mailTransport = analyzerXML.getAttribute(AnalyzerConfigConstants.MAIL_TRANSPORT);

        assert toEmailAddress != null;
        assert subject != null;
        assert fromEmailAddress != null;
        assert mailHost != null;
        assert mailUsername != null;
        assert mailPassword != null;
        assert mailTransport != null;

        alertConfig.setToEmailAddr(toEmailAddress.getAttributeValue());
        alertConfig.setSubject(subject.getAttributeValue());
        alertConfig.setFromAddress(fromEmailAddress.getAttributeValue());
        alertConfig.setMailHost(mailHost.getAttributeValue());
        alertConfig.setUserName(mailUsername.getAttributeValue());
        alertConfig.setPassword(mailPassword.getAttributeValue());
        alertConfig.setTransport(mailTransport.getAttributeValue());

        String alertFields = null;
        if (fields != null) {
            alertFields = fields.getText();
        } else {
            throw new AnalyzerException("fields must be present..");
        }
        alertConfig.setFields(alertFields.split(","));
        return alertConfig;
    }

    @Override
    public Analyzer buildAnalyzer(OMElement analyzerXML) throws AnalyzerException {
        return new AlertTrigger(buildConfig(analyzerXML));
    }
}
