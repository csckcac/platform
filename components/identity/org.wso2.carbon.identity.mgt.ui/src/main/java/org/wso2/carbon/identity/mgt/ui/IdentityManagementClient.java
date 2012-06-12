/*
*  Copyright (c)  WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.mgt.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.captcha.mgt.beans.xsd.CaptchaInfoBean;
import org.wso2.carbon.identity.mgt.stub.IdentityManagementServiceStub;
import org.wso2.carbon.identity.mgt.stub.beans.UserMgtBean;
import org.wso2.carbon.identity.mgt.stub.beans.VerificationBean;
import org.wso2.carbon.identity.mgt.stub.dto.ChallengeQuestionDTO;
import org.wso2.carbon.identity.mgt.stub.dto.UserChallengesDTO;

/**
 *
 */
public class IdentityManagementClient {

    public static final String CHALLENGE_QUESTION = "challenge.question";

    public static final String USER_CHALLENGE_QUESTION = "user.challenge.question";

    protected IdentityManagementServiceStub stub = null;
       
    protected static Log log = LogFactory.getLog(IdentityManagementClient.class);

    public IdentityManagementClient(String url, ConfigurationContext configContext)
            throws java.lang.Exception {
        try {
            stub = new IdentityManagementServiceStub(configContext, url + "IdentityManagementService");
        } catch (java.lang.Exception e) {
            handleException(e.getMessage(), e);
        }
    }

    public IdentityManagementClient(String cookie, String url, ConfigurationContext configContext)
            throws java.lang.Exception {
        try {
            stub = new IdentityManagementServiceStub(configContext, url + "IdentityManagementService");
            ServiceClient client = stub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        } catch (java.lang.Exception e) {
            handleException(e.getMessage(), e);
        }
    }
    
    public ChallengeQuestionDTO[] getChallengeQuestions() throws AxisFault {

        try {
            return stub.getAllChallengeQuestions();
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }

        return null;
    }


    public void setChallengeQuestions(ChallengeQuestionDTO[] challengeQuestionDTOs)
                                                                        throws AxisFault {
        try {
            stub.setChallengeQuestions(challengeQuestionDTOs);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
    }

    
    public CaptchaInfoBean generateRandomCaptcha() throws AxisFault {
        
        try {
            return stub.generateRandomCaptcha();
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
        return null;
    }

    public VerificationBean verifyUser(String userId, CaptchaInfoBean captchaInfoBean)
                                                                        throws AxisFault {
        try {
            UserMgtBean userMgtBean = new UserMgtBean();
            userMgtBean.setUserId(userId);
            return stub.verifyUser(userMgtBean, captchaInfoBean);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }

        return null;
    }

    public boolean processPasswordRecoveryLink(String userId, String userKey)
                                                                        throws AxisFault {
        try {
            UserMgtBean bean = new UserMgtBean();
            bean.setUserId(userId);
            bean.setUserKey(userKey);
            return stub.processPasswordRecovery(bean);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
        return false;
    }

    public UserChallengesDTO[] getChallengeQuestionsOfUser(String userId, String userKey)
                                                                        throws AxisFault {
        try {
            UserMgtBean bean = new UserMgtBean();
            bean.setUserId(userId);
            bean.setUserPassword(userKey);
            return stub.getChallengeQuestionsOfUser(bean);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }

        return null;
    }

    public VerificationBean verifyChallengeQuestion(String userId, String userKey, String question,
                                                        String answer) throws AxisFault {
        try {
            UserMgtBean bean = new UserMgtBean();
            UserChallengesDTO dto = new UserChallengesDTO();
            dto.setQuestion(question);
            dto.setAnswer(answer);
            bean.setUserId(userId);
            bean.setUserKey(userKey);
            bean.addUserChallenges(dto);
            return stub.verifyChallengeQuestion(bean);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }

        return null;
    }

    public VerificationBean confirmLink(String confirmationKey) throws AxisFault {
        try {
            return stub.confirmUserAccount(confirmationKey);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }

        return null;
    }

    public boolean updateCredential(UserMgtBean userMgtBean, CaptchaInfoBean captchaInfoBean)
                                                                                throws AxisFault {
        try {
            return stub.updateCredential(userMgtBean, captchaInfoBean);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
        return false;
    }

    public boolean unlockUserAccount(UserMgtBean userMgtBean) throws AxisFault {
        try {
            return stub.unlockUserAccount(userMgtBean);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
        return false;
    }

    public boolean processAccountRecovery(UserMgtBean userMgtBean) throws AxisFault {
        try {
            return stub.processAccountRecovery(userMgtBean);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
        return false;
    }

    private String[] handleException(String msg, Exception e)  throws AxisFault {
        log.error(msg, e);
        throw new AxisFault(msg, e);
    }

}
