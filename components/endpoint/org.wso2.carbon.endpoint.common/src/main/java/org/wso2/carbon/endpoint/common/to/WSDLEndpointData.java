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

public class WSDLEndpointData {
    private int epType = 1;
    private String epName;
    private long epDur;
    private long epmaxSusDuration;
    private float epsusProgFactor;
    private String eperrorCodes;

    private String eptimdedOutErrorCodes;
    private String retryDisabledErrorCodes;
    private int epretryTimeout;
    private int epretryDelay;

    private String inLineWSDL;
    private String epServ;
    private String epUri;
    private String epPort;

    private int epwsdlTimeoutAction;

    private long epactionDuration;
    private boolean epaddressingOn;

    private boolean epwsaddSepListener;
    private boolean epsecutiryOn;

    private boolean eprelMesg;
    private String epwsdlSecutiryKey;

    private String eprmKey;
    private String description;
    private String properties;

    public WSDLEndpointData() {

    }

    public int getEpType() {
        return epType;
    }

    public void setEpType(int as7) {
        this.epType = as7;
    }

    public String getEpName() {
        return epName;
    }

    public void setEpName(String as1) {
        this.epName = as1;
    }


    public String getEpServ() {
        return epServ;
    }

    public void setEpServ(String epServ) {
        this.epServ = epServ;
    }

    public String getEpUri() {
        return epUri;
    }

    public void setEpUri(String epUri) {
        this.epUri = epUri;
    }


    public String getInLineWSDL() {
        return inLineWSDL;
    }

    public void setInLineWSDL(String inLineWSDL) {
        this.inLineWSDL = inLineWSDL;
    }

    public long getEpDur() {
        return epDur;
    }

    public void setEpDur(long epDur) {
        this.epDur = epDur;
    }

    public String getEpPort() {
        return epPort;
    }

    public void setEpPort(String epPort) {
        this.epPort = epPort;
    }

    public int getEpwsdlTimeoutAction() {
        return epwsdlTimeoutAction;
    }

    public void setEpwsdlTimeoutAction(int epwsdlTimeoutAction) {
        this.epwsdlTimeoutAction = epwsdlTimeoutAction;
    }

    public long getEpactionDuration() {
        return epactionDuration;
    }

    public void setEpactionDuration(long epactionDuration) {
        this.epactionDuration = epactionDuration;
    }

    public boolean isEpaddressingOn() {
        return epaddressingOn;
    }

    public void setEpaddressingOn(boolean epaddressingOn) {
        this.epaddressingOn = epaddressingOn;
    }

    public boolean isEpwsaddSepListener() {
        return epwsaddSepListener;
    }

    public void setEpwsaddSepListener(boolean epwsaddSepListener) {
        this.epwsaddSepListener = epwsaddSepListener;
    }

    public boolean isEpsecutiryOn() {
        return epsecutiryOn;
    }

    public void setEpsecutiryOn(boolean epsecutiryOn) {
        this.epsecutiryOn = epsecutiryOn;
    }

    public boolean isEprelMesg() {
        return eprelMesg;
    }

    public void setEprelMesg(boolean eprelMesg) {
        this.eprelMesg = eprelMesg;
    }

    public String getEpwsdlSecutiryKey() {
        return epwsdlSecutiryKey;
    }

    public void setEpwsdlSecutiryKey(String epwsdlSecutiryKey) {
        this.epwsdlSecutiryKey = epwsdlSecutiryKey;
    }


    public String getEprmKey() {
        return eprmKey;
    }

    public void setEprmKey(String eprmKey) {
        this.eprmKey = eprmKey;
    }

    public long getEpmaxSusDuration() {
        return epmaxSusDuration;
    }

    public void setEpmaxSusDuration(long epmaxSusDuration) {
        this.epmaxSusDuration = epmaxSusDuration;
    }

    public float getEpsusProgFactor() {
        return epsusProgFactor;
    }

    public void setEpsusProgFactor(float epsusProgFactor) {
        this.epsusProgFactor = epsusProgFactor;
    }

    public String getEperrorCodes() {
        return eperrorCodes;
    }

    public void setEperrorCodes(String eperrorCodes) {
        this.eperrorCodes = eperrorCodes;
    }

    public String getEptimdedOutErrorCodes() {
        return eptimdedOutErrorCodes;
    }

    public void setEptimdedOutErrorCodes(String eptimdedOutErrorCodes) {
        this.eptimdedOutErrorCodes = eptimdedOutErrorCodes;
    }

    public int getEpretryTimeout() {
        return epretryTimeout;
    }

    public void setEpretryTimeout(int epretryTimeout) {
        this.epretryTimeout = epretryTimeout;
    }

    public int getEpretryDelay() {
        return epretryDelay;
    }

    public void setEpretryDelay(int epretryDelay) {
        this.epretryDelay = epretryDelay;
    }
    public void setRetryDisabledErrorCodes(String retryDisabledErrorCodes) {
          this.retryDisabledErrorCodes = retryDisabledErrorCodes;
    }

    public String getRetryDisabledErrorCodes() {
        return retryDisabledErrorCodes;
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
