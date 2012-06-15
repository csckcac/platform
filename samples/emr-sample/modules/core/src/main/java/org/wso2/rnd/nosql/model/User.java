package org.wso2.rnd.nosql.model;
/**
 ~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ WSO2 Inc. licenses this file to you under the Apache License,
 ~ Version 2.0 (the "License"); you may not use this file except
 ~ in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~    http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied.  See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
 **/

/**
 * Keep EMR User data
 */
public class User {


    private String userID;
    private String password;
    private String email;
    private String gender;


    private String fullName;
    private String dateOfBirth;
    private String bloodGroup;
    private String ethnicity;

    public User() {
    }

    public User(String userId, String password, String email, String gender) {
        this.password = password;
        this.userID = userId;
        this.email = email;
        this.gender = gender;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setFullName(String name) {
        fullName = name;
    }

    public void setDateOfBirth(String dob) {
        dateOfBirth = dob;
    }

    public void setBloodGroup(String blood) {
        bloodGroup = blood;
    }

    public void setEthnicity(String ethnicity) {
        ethnicity = ethnicity;
    }

    public String getUserID() {
        return userID;
    }

    public String getFullName() {
        return fullName;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public String getEmail() {
        return email;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getPassword() {
        return password;
    }

    public boolean comparePassword(String compare) {
        return password.equals(compare);
    }

    public String getGender() {
        return gender;
    }

    public String getEthnicity() {
        return ethnicity;  //To change body of created methods use File | Settings | File Templates.
    }
}
