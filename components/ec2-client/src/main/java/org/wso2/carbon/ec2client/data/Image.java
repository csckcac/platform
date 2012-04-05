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

import java.util.List;

/**
 *
 */
public class Image {
    private String imageId;
    private String architecture;
    private String kernelId;
    private String ramDiskId;
    private String location;
    private String ownerId;
    private String type;
    private String state;
    private boolean isPublic;
    private List<String> productCodes;
    private ImageType imageType;

    //TODO: Handle image type
    //TODO: Handle image attributes

    public Image(String imageId) {
        this.imageId = imageId;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public ImageType getImageType() {
        return imageType;
    }

    public void setImageType(ImageType imageType) {
        this.imageType = imageType;
    }

    public String getArchitecture() {
        return architecture;
    }

    public void setArchitecture(String architecture) {
        this.architecture = architecture;
    }

    public String getKernelId() {
        return kernelId;
    }

    public void setKernelId(String kernelId) {
        this.kernelId = kernelId;
    }

    public String getRamDiskId() {
        return ramDiskId;
    }

    public void setRamDiskId(String ramDiskId) {
        this.ramDiskId = ramDiskId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public List<String> getProductCodes() {
        return productCodes;
    }

    public void setProductCodes(List<String> productCodes) {
        this.productCodes = productCodes;
    }

    public String toString() {
        return "Image{" +
               "imageId='" + imageId + '\'' +
               ", architecture='" + architecture + '\'' +
               ", kernelId='" + kernelId + '\'' +
               ", ramDiskId='" + ramDiskId + '\'' +
               ", location='" + location + '\'' +
               ", ownerId='" + ownerId + '\'' +
               ", type='" + type + '\'' +
               ", state='" + state + '\'' +
               ", isPublic=" + isPublic +
               '}';
    }
}
