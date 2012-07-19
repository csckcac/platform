/*
 * Copyright 2004,2005 The Apache Software Foundation.
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

package org.wso2.carbon.cep.siddhi;

import junit.framework.Assert;
import org.wso2.carbon.cep.core.mapping.output.Output;
import org.wso2.carbon.cep.core.listener.CEPEventListener;

import java.util.HashMap;
import java.util.List;

public class DummyCEPListener extends CEPEventListener {

    int testNumber = 0;

    public DummyCEPListener(Output output,  int tenantId, String userName, int testNumber) {
        super(output, tenantId, userName);
        this.testNumber = testNumber;
    }

    @Override
    public void onComplexEvent(List list) {

        HashMap mapEvent = null;
        for (Object event : list) {
            switch (testNumber) {
                case 0:
                    mapEvent = (HashMap) event;
                    Assert.assertTrue(((Long) mapEvent.get("totalRequestCount") == 21));
                    break;
                case 1:
                    mapEvent = (HashMap) event;
                    Assert.assertTrue(((Double) mapEvent.get("avgMaximumResponseTime") == 121.0));
                    break;
                case 2:
                    mapEvent = (HashMap) event;
                    Assert.assertTrue(((Double) mapEvent.get("averagePrice") == 25.23) || ((Double) mapEvent.get("averagePrice") == 27.73) || ((Double) mapEvent.get("averagePrice") == 31.896666666666665));
                    break;

            }
//            for (Object key : mapEvent.keySet()) {
//                System.out.println("Key ==> " + key + " value " + mapEvent.get(key).toString());
//            }
        }
    }
}
