/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.bam.core.persistence;

import org.wso2.carbon.bam.core.configurations.IndexConfiguration;
import org.wso2.carbon.bam.core.dataobjects.Cursor;
import org.wso2.carbon.bam.core.dataobjects.Record;
import org.wso2.carbon.bam.core.persistence.exceptions.IndexingException;

import java.util.List;
import java.util.Map;

public interface IndexingStrategy {
    
    public void createIndex(IndexConfiguration configuration, Map<String, String> credentials)
            throws IndexingException;
    
    public void editIndex(IndexConfiguration configuration, Map<String, String> credentials)
        throws IndexingException;
    
    public void deleteIndex(String indexName, Map<String, String> credentials)
            throws IndexingException;

    public void indexData(IndexConfiguration configuration, Cursor cursor,
                          Map<String, String> credentials) throws IndexingException;
    
    public Map<String, String> getIndexValuesOfRecord(Record record,
                                                      IndexConfiguration configuration);

    public Map<String, List<String>> getIndexValues(String indexName,
                                                    Map<String, String> credentials)
            throws IndexingException;
    
    public String[] getNextSubIndexValues(String indexName, String subIndex, String subIndexValue,
                                          Map<String, String> credentials) throws IndexingException;

}
