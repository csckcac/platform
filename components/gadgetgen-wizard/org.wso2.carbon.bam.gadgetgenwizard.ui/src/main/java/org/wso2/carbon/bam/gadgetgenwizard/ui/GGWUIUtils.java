package org.wso2.carbon.bam.gadgetgenwizard.ui;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.bam.gadgetgenwizard.stub.beans.*;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
public class GGWUIUtils {
    public static JSONObject convertToJSONObj(WSResultSet wsResultSet) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonCols = new JSONArray(wsResultSet.getColumnNames());
        jsonObject.put("ColumnNames", jsonCols);
        JSONArray allRows = new JSONArray();
        WSRow[] wsRows = wsResultSet.getRows();
        for (WSRow wsRow : wsRows) {
            JSONArray row = new JSONArray(wsRow.getRow());
            allRows.put(row);
        }
        jsonObject.put("Rows", allRows);
        return jsonObject;
    }

    public static String getSQL(HttpSession session) {
        return (session.getAttribute("sql") != null) ? ((String[]) session.getAttribute("sql")) [0] : null;
    }

    public static DBConnInfo constructDBConnInfo(HttpSession session) {
        String jdbcurl = (session.getAttribute("jdbcurl") != null) ? ((String[]) session.getAttribute("jdbcurl")) [0] : "";
        String driver = (session.getAttribute("driver") != null) ? ((String[]) session.getAttribute("driver")) [0] : "";
        String username = (session.getAttribute("username") != null) ? ((String[]) session.getAttribute("username")) [0] : "";
        String password = (session.getAttribute("password") != null) ? ((String[]) session.getAttribute("password")) [0] : "";
        DBConnInfo dbConnInfo = new DBConnInfo();
        dbConnInfo.setJdbcURL(jdbcurl);
        dbConnInfo.setDriverClass(driver);
        dbConnInfo.setUsername(username);
        dbConnInfo.setPassword(password);
        return dbConnInfo;
    }

    public static WSMap constructWSMap(HttpSession session, List<String> sessionAttrKey) {
        List<WSMapElement> sessionValues = new ArrayList<WSMapElement>();
        for (String key : sessionAttrKey) {
            WSMapElement wsMapElement = new WSMapElement();
            wsMapElement.setKey(key);
            wsMapElement.setValue(((String[]) session.getAttribute(key))[0]);
            sessionValues.add(wsMapElement);
        }
        WSMap wsMap = new WSMap();
        wsMap.setWsMapElements(sessionValues.toArray(new WSMapElement[sessionValues.size()]));

        return wsMap;
    }
}
