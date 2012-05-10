/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.bam.analyzer.task;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.analyzer.Utils;
import org.wso2.carbon.bam.analyzer.engine.*;

import javax.xml.stream.XMLStreamException;
import java.util.HashMap;
import java.util.Map;

public class BAMDefaultAnalyzerTask extends AbstractAnalyzerTask {

    private static Log log = LogFactory.getLog(BAMDefaultAnalyzerTask.class);

    private Map<String, String> props;

    @Override
    public void setProperties(Map<String, String> props) {
        this.props = props;
    }

    @Override
    public void init() {

    }

    @Override
    public void execute() {
        String analyserSeqXML = getProperties().get(AnalyzerConfigConstants.ANALYZER_SEQUENCE);
        try {
            DataContext ctxt = createDataContext(getProperties());
            OMElement analyserSeqEle = AXIOMUtil.stringToOM(analyserSeqXML);
            AnalyzerSequence analyserSequence =
                    Utils.getAnalyzerSequence(ctxt.getExecutingTenant(), analyserSeqEle);

            if (log.isDebugEnabled()) {
                log.debug("Starting analyzer sequence " + analyserSequence.getName() + "..");
            }

            for (Analyzer analyzer : analyserSequence.getAnalyzers()) {
                analyzer.analyze(ctxt);
            }

            if (log.isDebugEnabled()) {
                log.debug("Finished executing analyzer sequence " + analyserSequence.getName() +
                        "..");
            }
        } catch (AnalyzerException e) {
            String msg = "Error while processing analyzers";
            log.error(msg, e);
        } catch (XMLStreamException e) {
            String msg = "Error while converting analyzer configuration XML string to OM";
            log.error(msg, e);
        }

    }

    public Map<String, String> getProperties() {
        return props;
    }

    private DataContext createDataContext(Map<String, String> props) {
        int tenantId = Integer.parseInt(props.get(AnalyzerConfigConstants.TENANT_ID));
        String analyserSeqName = props.get(AnalyzerConfigConstants.ANALYZER_SEQUENCE_NAME);

        Map<String, String> credentials = new HashMap<String, String>();
        credentials.put(AnalyzerConfigConstants.USERNAME,
                props.get(AnalyzerConfigConstants.USERNAME));
        credentials.put(AnalyzerConfigConstants.PASSWORD,
                props.get(AnalyzerConfigConstants.PASSWORD));

        DataContext ctxt = new DataContext(tenantId);
        ctxt.setCredentials(credentials);
        ctxt.setSequenceProperties(analyserSeqName, new HashMap<String, String>());

        return ctxt;
    }

}
