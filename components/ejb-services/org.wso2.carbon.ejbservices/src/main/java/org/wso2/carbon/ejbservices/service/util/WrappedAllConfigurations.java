/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
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
package org.wso2.carbon.ejbservices.service.util;

import java.util.Arrays;

@SuppressWarnings("UnusedDeclaration")
public class WrappedAllConfigurations {
    private EJBAppServerData[] appServerData;
    private EJBAppServerData[] appServerNameList;
    private EJBProviderData[] ejbProviderData;

    public EJBAppServerData[] getAppServerNameList() {
        return appServerNameList;
    }

    public void setAppServerNameList(EJBAppServerData[] appServerNameList) {
        if(appServerNameList == null) {
            this.appServerNameList = new EJBAppServerData[0];
        } else {
            this.appServerNameList = Arrays.copyOf(appServerNameList, appServerNameList.length);
        }
    }

    public EJBProviderData[] getEjbProviderData() {
        return ejbProviderData;
    }

    public void setEjbProviderData(EJBProviderData[] ejbProviderData) {
        if (ejbProviderData == null) {
            this.ejbProviderData = new EJBProviderData[0];
        } else {
            this.ejbProviderData = Arrays.copyOf(ejbProviderData, ejbProviderData.length);
        }
    }

    public EJBAppServerData[] getAppServerData() {
        return appServerData;
    }

    public void setAppServerData(EJBAppServerData[] appServerData) {
        if (appServerData == null) {
            this.appServerData = new EJBAppServerData[0];
        } else {
            this.appServerData = Arrays.copyOf(appServerData, appServerData.length);
        }
    }
}
