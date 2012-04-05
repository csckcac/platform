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
package org.wso2.carbon.ec2client.data;

import java.util.Calendar;
import java.util.List;

/**
 *
 */
public class Instance {
    private String instanceId;
    private Image image;
    private String ownerId;
    private InstanceState currentState;
    private InstanceState previousState; // Only applicable to terminating instances
    private String reservationId;
    private String[] groupIds;
    private String internalName;
    private String externalName;
    private Calendar launchTime;
    private String instanceType;
    private AvailabilityZone availabilityZone;

    public AvailabilityZone getAvailabilityZone() {
        return availabilityZone;
    }

    public void setAvailabilityZone(AvailabilityZone availabilityZone) {
        this.availabilityZone = availabilityZone;
    }

    public Image getImage() {
        return image;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String[] getGroupIds() {
        return groupIds;
    }

    public void setGroupIds(String[] groupIds) {
        this.groupIds = groupIds;
    }

    public String getInternalName() {
        return internalName;
    }

    public void setInternalName(String internalAddress) {
        this.internalName = internalAddress;
    }

    public String getExternalName() {
        return externalName;
    }

    public void setExternalName(String externalAddress) {
        this.externalName = externalAddress;
    }

    public Calendar getLaunchTime() {
        return launchTime;
    }

    public void setLaunchTime(Calendar launchTime) {
        this.launchTime = launchTime;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerID) {
        this.ownerId = ownerID;
    }

    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    public String getInstanceType() {
        return instanceType;
    }

    public void setInstanceType(String instanceType) {
        this.instanceType = instanceType;
    }

    public InstanceState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(InstanceState currentState) {
        this.currentState = currentState;
    }

    public InstanceState getPreviousState() {
        return previousState;
    }

    public void setPreviousState(InstanceState previousState) {
        this.previousState = previousState;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Instance instance = (Instance) o;

        return instanceId.equals(instance.getInstanceId());

    }

    public int hashCode() {
        return instanceId.hashCode();
    }

    public String toString() {
        return "Instance{" +
               "instanceId='" + instanceId + '\'' +
               ", image=" + image +
               ", ownerId='" + ownerId + '\'' +
               ", currentState=" + currentState +
               ", previousState=" + previousState +
               ", reservationId='" + reservationId + '\'' +
               ", groupId='" + ((groupIds != null)?groupIds[0]:"none") + '\'' +
               ", internalName='" + internalName + '\'' +
               ", externalName='" + externalName + '\'' +
               ", launchTime=" + launchTime +
               ", instanceType='" + instanceType + '\'' +
               ", availabilityZone=" + availabilityZone +
               '}';
    }
}
