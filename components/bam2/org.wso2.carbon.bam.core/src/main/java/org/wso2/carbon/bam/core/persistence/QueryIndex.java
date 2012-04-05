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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryIndex {

    private final String indexName;
    
    Map<String, List<String>> compositeRanges = new HashMap<String, List<String>>();
    
    public QueryIndex(String indexName) {
        this.indexName = indexName;
    }

    public String getIndexName() {
        return indexName;   
    }
    
    public void addCompositeRange(String compositeColumn, String rangeFirst, String rangeLast) {
        List<String> ranges = new ArrayList<String>();
        ranges.add(rangeFirst);
        ranges.add(rangeLast);
        
        compositeRanges.put(compositeColumn, ranges);
    }
    
    public Map<String, List<String>> getCompositeRanges() {
        return compositeRanges;
    }
    
}
