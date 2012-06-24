/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.autoscaler.service.util;

import java.util.Comparator;

public enum IaaSProviderComparator implements Comparator<IaaSProvider> {
    SCALE_UP_SORT {
        public int compare(IaaSProvider o1, IaaSProvider o2) {
            return Integer.valueOf(o1.getScaleUpOrder()).compareTo(o2.getScaleUpOrder());
        }},
    SCALE_DOWN_SORT {
        public int compare(IaaSProvider o1, IaaSProvider o2) {
            return Integer.valueOf(o1.getScaleDownOrder()).compareTo(o2.getScaleDownOrder());
        }};

    public static Comparator<IaaSProvider> ascending(final Comparator<IaaSProvider> other) {
        return new Comparator<IaaSProvider>() {
            public int compare(IaaSProvider o1, IaaSProvider o2) {
                return other.compare(o1, o2);
            }
        };
    }

    public static Comparator<IaaSProvider> getComparator(final IaaSProviderComparator... multipleOptions) {
        return new Comparator<IaaSProvider>() {
            public int compare(IaaSProvider o1, IaaSProvider o2) {
                for (IaaSProviderComparator option : multipleOptions) {
                    int result = option.compare(o1, o2);
                    if (result != 0) {
                        return result;
                    }
                }
                return 0;
            }
        };
    }
}

