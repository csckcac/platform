/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.messagebox.admin.internal;

/**
 * Access key and secret access keys containing bean
 */
public class SQSKeys {
    private String accessKeyId;
    private String secretAccessKeyId;

    public SQSKeys(String accessKeyId, String secretAccessKeyId) {
        this.accessKeyId = accessKeyId;
        this.secretAccessKeyId = secretAccessKeyId;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getSecretAccessKeyId() {
        return secretAccessKeyId;
    }

    public void setSecretAccessKeyId(String secretAccessKeyId) {
        this.secretAccessKeyId = secretAccessKeyId;
    }
}
