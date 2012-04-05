/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.issue.tracker.core;

import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.lang.management.ManagementFactory;


public class OperatingEnvironment {

    private String osName;
    private String osVersion;
    private String osArchitecture;
    private String javaVendor;
    private String javaVersion;
    private int numOfProcessors;
    private long totalMemory;
    private long freeMemory;
    private String productName;
    private String productVersion;
    private double totalDiskSpace;
    private double freeDiskSpace;
    private String patchList;

    public OperatingEnvironment() {

        // set System variables
        this.setOsName(ManagementFactory.getOperatingSystemMXBean().getName());
        this.setOsVersion(ManagementFactory.getOperatingSystemMXBean().getVersion());
        this.setOsArchitecture(ManagementFactory.getOperatingSystemMXBean().getArch());
        this.setJavaVendor(System.getProperty(IssueTrackerConstants.JAVA_VENDOR));
        this.setJavaVersion(System.getProperty(IssueTrackerConstants.JAVA_VERSION));

        // set runtime variables
        Runtime runtime = Runtime.getRuntime();
        this.setNumOfProcessors(runtime.availableProcessors());

        //set memory details
        com.sun.management.OperatingSystemMXBean mxbean =
                (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        
        this.setTotalMemory(mxbean.getTotalPhysicalMemorySize());
        this.setFreeMemory(mxbean.getFreePhysicalMemorySize());


        // server information

        this.setProductName(ServerConfiguration.getInstance().
                getFirstProperty(IssueTrackerConstants.PRODUCT_NAME));
        this.setProductVersion(ServerConfiguration.getInstance().
                getFirstProperty(IssueTrackerConstants.PRODUCT_VERSION));


        // get disk space

        captureDiskSpace();

        // get patch list

        capturePatchList();

    }


    /**
     * method to capture total and free disk space from system
     * this will capture the space allocated for the partition set as home only
     */
    private void captureDiskSpace() {
        String path = System.getProperty(IssueTrackerConstants.USER_HOME);
        File home = new File(path);

        // get total diskspace in GB
        double totalSpace = (home.getTotalSpace()) / (double)(1024 *1024*1024);

        //get free disk space in GB
        double freeSpace = (home.getFreeSpace()) / (double)(1024 *1024*1024);

        this.setTotalDiskSpace(totalSpace);
        this.setFreeDiskSpace(freeSpace);
    }



    public void capturePatchList() {

        StringBuffer patchList = new StringBuffer();

        String path = CarbonUtils.getCarbonHome() + IssueTrackerConstants.PATCH_DIR_PATH;

        File dir = new File(path);
        String[] patches = dir.list();
        if (null != patches) {
            for (int i = 0; i < patches.length; i++) {
                // Get filename of file or directory
                patchList.append( patches[i]+ ", ");
            }
        }


        this.setPatchList(patchList.toString());

    }




    /**
     * Method to get operating environmental data and construct a string out of them
     *
     * @param operatingEnvironment
     * @return string containing environment info
     */
    public static String getEnvironmentData(OperatingEnvironment operatingEnvironment) {
        String data;

        StringBuffer string = new StringBuffer();

        // append data line by line
        string.append("Operating Environment : ").append(operatingEnvironment.getOsName()).append(" ");
        string.append(operatingEnvironment.getOsVersion()).append(" ");
        string.append(operatingEnvironment.getOsArchitecture()).append("\n");
        string.append("JAVA Vendor : ").append(operatingEnvironment.getJavaVendor()).append("\n");
        string.append("JAVA Version : ").append(operatingEnvironment.getJavaVersion()).append("\n");
        string.append("Number of Processors : ").append(operatingEnvironment.getNumOfProcessors()).append("\n");
        string.append("Total physical memory : ").append(operatingEnvironment.getTotalMemory()/1024/1024).append(" (MB)\n");
        string.append("Free physical memory : ").append(operatingEnvironment.getFreeMemory()/1024/1024).append(" (MB)\n");
        string.append("Product Name : ").append(operatingEnvironment.getProductName()).append("\n");
        string.append("Product Version : ").append(operatingEnvironment.getProductVersion()).append("\n");
        string.append("Total disk space : ").append(operatingEnvironment.getTotalDiskSpace()).append(" (GB)\n");
        string.append("Free disk space : ").append(operatingEnvironment.getFreeDiskSpace()).append(" (GB)\n");
        string.append("Patch List : ").append(operatingEnvironment.getPatchList()).append("\n");
        data = string.toString();

        return data;
    }



    public double getTotalDiskSpace() {
        return totalDiskSpace;
    }

    public void setTotalDiskSpace(double totalDiskSpace) {
        this.totalDiskSpace = totalDiskSpace;
    }

    public double getFreeDiskSpace() {
        return freeDiskSpace;
    }

    public void setFreeDiskSpace(double freeDiskSpace) {
        this.freeDiskSpace = freeDiskSpace;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductVersion() {
        return productVersion;
    }

    public void setProductVersion(String productVersion) {
        this.productVersion = productVersion;
    }

    public String getOsArchitecture() {
        return osArchitecture;
    }

    public void setOsArchitecture(String osArchitecture) {
        this.osArchitecture = osArchitecture;
    }

    public String getOsName() {
        return osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getJavaVendor() {
        return javaVendor;
    }

    public void setJavaVendor(String javaVendor) {
        this.javaVendor = javaVendor;
    }

    public String getJavaVersion() {
        return javaVersion;
    }

    public void setJavaVersion(String javaVersion) {
        this.javaVersion = javaVersion;
    }

    public int getNumOfProcessors() {
        return numOfProcessors;
    }

    public void setNumOfProcessors(int numOfProcessors) {
        this.numOfProcessors = numOfProcessors;
    }

    public long getTotalMemory() {
        return totalMemory;
    }

    public void setTotalMemory(long totalMemory) {
        this.totalMemory = totalMemory;
    }

    public long getFreeMemory() {
        return freeMemory;
    }

    public void setFreeMemory(long freeMemory) {
        this.freeMemory = freeMemory;
    }

    public String getPatchList() {
        return patchList;
    }

    public void setPatchList(String patchList) {
        this.patchList = patchList;
    }
}
