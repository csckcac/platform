/**
 *  Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.endpoint.common.to;


public class AddressEndpointData {

    private int epType = 0;
    private String epName;
    private String address;
    private long suspendDurationOnFailure;
    private long maxSusDuration;
    private float susProgFactor;
    private String errorCodes;
    private String format;

    private boolean swa = false;
    private boolean mtom = false;

    private boolean soap11 = false;
    private boolean soap12 = false;
    private boolean get = false;
    private boolean rest = false;
    private boolean pox = false;

    private String timdedOutErrorCodes;
    private String retryDisabledErrorCodes;
    private int retryTimeout;
    private int retryDelay;

    private int timeoutAct;
    private long timeoutActionDur;
    private boolean wsadd;
    private boolean sepList;
    private boolean wssec;
    private boolean wsrm;
    private String secPolKey;
    private String rmPolKey;
    private String description = "";
    private String properties;

    public AddressEndpointData() {

    }

    public int getEpType() {
        return epType;
    }

    public void setEpType(int type) {
        this.epType = type;
    }

    public String getEpName() {
        return epName;
    }

    public void setEpName(String name) {
        this.epName = name;
    }

    public long getSuspendDurationOnFailure() {
        return suspendDurationOnFailure;
    }

    public void setSuspendDurationOnFailure(long suspendDurationOnFailure) {
        this.suspendDurationOnFailure = suspendDurationOnFailure;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public boolean isSwa() {
        return swa;
    }

    public void setSwa(boolean swa) {
        this.swa = swa;
    }

    public boolean isMtom() {
        return mtom;
    }

    public void setMtom(boolean mtom) {
        this.mtom = mtom;
    }


    public boolean isSoap11() {
        return soap11;
    }

    public void setSoap11(boolean soap11) {
        this.soap11 = soap11;
    }

    public boolean isSoap12() {
        return soap12;
    }

    public void setSoap12(boolean soap12) {
        this.soap12 = soap12;
    }

    public boolean isRest() {
        return rest;
    }

    public void setRest(boolean rest) {
        this.rest = rest;
    }

    public boolean isGet() {
        return get;
    }

    public void setGet(boolean get) {
        this.get = get;
    }

    public boolean isPox() {
        return pox;
    }

    public void setPox(boolean pox) {
        this.pox = pox;
    }

    public int getTimeoutAct() {
        return timeoutAct;
    }

    public void setTimeoutAct(int timeoutAct) {
        this.timeoutAct = timeoutAct;
    }

    public long getTimeoutActionDur() {
        return timeoutActionDur;
    }

    public void setTimeoutActionDur(long timeoutActionDur) {
        this.timeoutActionDur = timeoutActionDur;
    }

    public boolean isWsadd() {
        return wsadd;
    }

    public void setWsadd(boolean wsadd) {
        this.wsadd = wsadd;
    }

    public boolean isSepList() {
        return sepList;
    }

    public void setSepList(boolean sepList) {
        this.sepList = sepList;
    }

    public boolean isWssec() {
        return wssec;
    }

    public void setWssec(boolean wssec) {
        this.wssec = wssec;
    }

    public boolean isWsrm() {
        return wsrm;
    }

    public void setWsrm(boolean wsrm) {
        this.wsrm = wsrm;
    }

    public String getSecPolKey() {
        return secPolKey;
    }

    public void setSecPolKey(String secPolKey) {
        this.secPolKey = secPolKey;
    }

    public String getRmPolKey() {
        return rmPolKey;
    }

    public void setRmPolKey(String rmPolKey) {
        this.rmPolKey = rmPolKey;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public long getMaxSusDuration() {
        return maxSusDuration;
    }

    public void setMaxSusDuration(long maxSusDuration) {
        this.maxSusDuration = maxSusDuration;
    }

    public float getSusProgFactor() {
        return susProgFactor;
    }

    public void setSusProgFactor(float susProgFactor) {
        this.susProgFactor = susProgFactor;
    }

    public String getErrorCodes() {
        return errorCodes;
    }

    public void setErrorCodes(String errorCodes) {
        this.errorCodes = errorCodes;
    }


    public void setRetryDisabledErrorCodes(String retryDisabledErrorCodes) {
        this.retryDisabledErrorCodes = retryDisabledErrorCodes;
    }

    public String getRetryDisabledErrorCodes() {
        return retryDisabledErrorCodes;
    }

    public String getTimdedOutErrorCodes() {
        return timdedOutErrorCodes;
    }

    public void setTimdedOutErrorCodes(String timdedOutErrorCodes) {
        this.timdedOutErrorCodes = timdedOutErrorCodes;
    }

    public int getRetryTimeout() {
        return retryTimeout;
    }

    public void setRetryTimeout(int retryTimeout) {
        this.retryTimeout = retryTimeout;
    }

    public int getRetryDelay() {
        return retryDelay;
    }

    public void setRetryDelay(int retryDelay) {
        this.retryDelay = retryDelay;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProperties() {
        return properties;
    }

    public void setProperties(String properties) {
        this.properties = properties;
    }
}
