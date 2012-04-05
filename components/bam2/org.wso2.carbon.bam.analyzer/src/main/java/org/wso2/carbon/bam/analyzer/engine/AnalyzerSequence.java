/**
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.bam.analyzer.engine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.analyzer.analyzers.GetAnalyzer;
import org.wso2.carbon.bam.analyzer.analyzers.configs.GetConfig;
import org.wso2.carbon.bam.core.dataobjects.Cursor;
import org.wso2.carbon.bam.core.persistence.MetaDataManager;
import org.wso2.carbon.bam.core.persistence.exceptions.ConfigurationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalyzerSequence {

    private static final Log log = LogFactory.getLog(AnalyzerSequence.class);

    private List<Analyzer> analyzers = new ArrayList<Analyzer>();
    private String name;
    //    private int frequencyInSecs;
    private String cron;
    private int interval;
    private int count;
    
    private Cursor cursor;
    
    private Map<String, String> credentials = new HashMap<String, String>();

    private int tenantId;

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<Analyzer> getAnalyzers() {
        return analyzers;
    }

    public void setAnalyzers(List<Analyzer> analyzers) {
        this.analyzers = analyzers;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    public Cursor getCursor() {
        return cursor;
    }

    public void setCursor(Cursor cursor) {
        this.cursor = cursor;
    }
    
    public void setCredentials(Map<String, String> credentials) {
        this.credentials = credentials;
    }
    
    // Remove cursors of this sequence
    public void cleanup() {
        if (analyzers != null) {

            MetaDataManager metaDataManager = MetaDataManager.getInstance();
            for (int i = 0; i < analyzers.size() ; i++) {
                
                Analyzer analyzer = analyzers.get(i);
                if (analyzer instanceof GetAnalyzer) {
                    GetAnalyzer getAnalyzer = (GetAnalyzer)analyzer;
                    GetConfig config = (GetConfig)getAnalyzer.getAnalyzerConfig();

                    if (isBatchSizeDefined(config)) {

                        Cursor cursor = new Cursor(config.getTable(), name, i);
                        cursor.setResumePoint("");

                        try {
                            metaDataManager.storeCursorMetaData(credentials, cursor);
                        } catch (ConfigurationException e) {
                            log.error("Error while clearing cursor meta data..");
                        }
                    }
                }
            }
        }
    }

    private boolean isBatchSizeDefined(GetConfig config) {
        if (config.getBatchSize() < Integer.MAX_VALUE) {
            return true;
        }

        return false;
    }

}
