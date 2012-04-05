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

package org.wso2.carbon.messagebox.queue;

public class QueueUserPermission {
    private String userName;
    private boolean isAllowedToConsume;
    private boolean isAllowedToPublish;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean isAllowedToConsume() {
        return isAllowedToConsume;
    }

    public void setAllowedToConsume(boolean allowedToConsume) {
        isAllowedToConsume = allowedToConsume;
    }

    public boolean isAllowedToPublish() {
        return isAllowedToPublish;
    }

    public void setAllowedToPublish(boolean allowedToPublish) {
        isAllowedToPublish = allowedToPublish;
    }
}
