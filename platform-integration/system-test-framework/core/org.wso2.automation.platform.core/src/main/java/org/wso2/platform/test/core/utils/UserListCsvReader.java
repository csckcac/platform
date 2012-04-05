/**Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *WSO2 Inc. licenses this file to you under the Apache License,
 *Version 2.0 (the "License"); you may not use this file except
 *in compliance with the License.
 *You may obtain a copy of the License at
 *
 *http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing,
 *software distributed under the License is distributed on an
 *"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *KIND, either express or implied.  See the License for the
 *specific language governing permissions and limitations
 *under the License.
 */

package org.wso2.platform.test.core.utils;

import au.com.bytecode.opencsv.CSVReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserListCsvReader {
    private static final Log log = LogFactory.getLog(UserListCsvReader.class);
    private static CSVReader reader;
    private static ArrayList<UserInfo> userList;
    private static boolean stratosTestStatus;

    static {
        try {
            reader = createReader();
        } catch (FileNotFoundException e) {
            log.error("Configuration file not found");
        }
        try {
            userList = csvUserReader();
        } catch (IOException ignored) {
        }
    }

    /*
       create open csv reader - reads userList.csv
    */
    private static CSVReader createReader() throws FileNotFoundException {
        log.debug("inside create reader");
        EnvironmentBuilder env = new EnvironmentBuilder();
        stratosTestStatus = env.getFrameworkSettings().getEnvironmentSettings().is_runningOnStratos();
        CSVReader reader;
        try {
            if (stratosTestStatus) {
                reader = new CSVReader(new FileReader(new File(ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION +
                                                               File.separator + "tenantList.csv")));
            } else {
                reader = new CSVReader(new FileReader(new File(ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION +
                                                               File.separator + "userList.csv")));
            }
        } catch (FileNotFoundException e) {
            log.error("User list not found" + e.getMessage());
            throw new FileNotFoundException("User list not found" + e.getMessage());
        }
        return reader;
    }


    private static ArrayList<UserInfo> csvUserReader() throws IOException {
        log.debug("inside list generator");
        List userDetailsList;
        try {
            userDetailsList = reader.readAll();
        } catch (IOException e) {
            log.error("unable to read user details: " + e.getMessage());
            throw new IOException("Cannot read user details");
        }

        //array list to store user details
        ArrayList<UserInfo> userList = new ArrayList<UserInfo>();

        //Store userDetails instances to user list array
        return storeUserInfo(userDetailsList, userList);
    }

    private static ArrayList<UserInfo> storeUserInfo(List userDetailsList,
                                                     ArrayList<UserInfo> userList) {
        for (Object anUserDetailsList : userDetailsList) {
            String[] tempUserInfoStr = (String[]) anUserDetailsList;
            for (int elementCounter = 0; elementCounter < tempUserInfoStr.length; elementCounter++) {
                if (!(tempUserInfoStr[elementCounter] == null) &&
                    !(tempUserInfoStr[elementCounter + 1] == null) &&
                    !(tempUserInfoStr[elementCounter + 2] == null)) {

                    if (stratosTestStatus) {
                        userList.add(new UserInfo(tempUserInfoStr[elementCounter],
                                                  tempUserInfoStr[elementCounter + 1],
                                                  tempUserInfoStr[elementCounter + 2],
                                                  domainNameExtractor(tempUserInfoStr[elementCounter + 1])));
                    } else {
                        userList.add(new UserInfo(tempUserInfoStr[elementCounter],
                                                  tempUserInfoStr[elementCounter + 1],
                                                  tempUserInfoStr[elementCounter + 2],
                                                  null));
                    }
                }
                break;
            }
        }
        return userList;
    }

    public static UserInfo getUserInfo(int userId) {
        log.debug("Inside test user details by ID");
        return userList.get(userId);
    }

    public static int getUserId(String key) {
        String inputInt;
        Pattern intsOnly = Pattern.compile("\\d+");
        Matcher makeMatch = intsOnly.matcher(key);
        makeMatch.find();
        inputInt = makeMatch.group();

        return (Integer.parseInt(inputInt));
    }

    private static String domainNameExtractor(String userName) {
        int index = userName.indexOf("@");
        return userName.substring(index + 1);
    }

    public static int getUserCount() {
        return userList.size();
    }
}
