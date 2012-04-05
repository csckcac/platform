/*
 * Copyright WSO2, Inc. (http://wso2.com)
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
package org.wso2.carbon.cloud.csg.agent.observer;

/**
 * This represents the observer for subject.  When a server goes down an observer will be created
 * who is interested in subject(that is the status of the remote server)
 */
public interface CSGAgentObserver {
    /**
     * Action to perform once the event is received
     * @param s CSGAgentSubject to act upon
     */
    public void update(CSGAgentSubject s);

    /**
     * Returns the host name associated with the observer
     * @return the remote host name
     */
    public String getHostName();

    /**
     * Returns the port of the server associated with the observer
     * @return the port
     */
    public int getPort();
}
