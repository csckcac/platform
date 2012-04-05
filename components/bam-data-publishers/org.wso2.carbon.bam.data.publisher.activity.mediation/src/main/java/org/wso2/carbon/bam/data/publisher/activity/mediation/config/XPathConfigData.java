/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.bam.data.publisher.activity.mediation.config;

import java.util.Arrays;

public class XPathConfigData {

    private String key;
    private String xpath;
    private String alias;
    private String[] nameSpaces;
    private boolean isEditing;
    private int id;


    public String getKey() {
        return key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof XPathConfigData)) {
            return false;
        }

        XPathConfigData that = (XPathConfigData) o;

        if (alias != null ? !alias.equals(that.alias) : that.alias != null) {
            return false;
        }
        if (key != null ? !key.equals(that.key) : that.key != null) {
            return false;
        }
        if (!Arrays.equals(nameSpaces, that.nameSpaces)) {
            return false;
        }
        if (xpath != null ? !xpath.equals(that.xpath) : that.xpath != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (xpath != null ? xpath.hashCode() : 0);
        result = 31 * result + (alias != null ? alias.hashCode() : 0);
        result = 31 * result + (nameSpaces != null ? Arrays.hashCode(nameSpaces) : 0);
        return result;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getXpath() {
        return xpath;
    }

    public void setXpath(String xpath) {
        this.xpath = xpath;
    }

    public String[] getNameSpaces() {
        return nameSpaces;
    }

    public void setNameSpaces(String[] nameSpaces) {
        this.nameSpaces = nameSpaces;
    }

    public boolean isEditing() {
        return isEditing;
    }

    public void setEditing(boolean editing) {
        isEditing = editing;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}