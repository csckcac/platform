/*
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.mgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.mgt.constants.IdentityMgtConstants;
import org.wso2.carbon.identity.mgt.dto.ChallengeQuestionDTO;
import org.wso2.carbon.identity.mgt.dto.UserChallengesDTO;
import org.wso2.carbon.identity.mgt.internal.IdentityMgtServiceComponent;
import org.wso2.carbon.identity.mgt.util.ClaimsMgtUtil;
import org.wso2.carbon.identity.mgt.util.PasswordUtil;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.claim.Claim;
import org.wso2.carbon.user.core.claim.ClaimMapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * process user challenges and questions 
 */
public class ChallengeQuestionProcessor {

    private static final Log log = LogFactory.getLog(ChallengeQuestionProcessor.class);

    /**
     *
     * @return
     * @throws IdentityMgtException
     */
    public List<ChallengeQuestionDTO> getAllChallengeQuestions() throws IdentityMgtException {

        List<ChallengeQuestionDTO> questionDTOs = new ArrayList<ChallengeQuestionDTO>();
        try{
            Registry registry = IdentityMgtServiceComponent.getRegistryService().
                                                                getConfigSystemRegistry();
            if(registry.resourceExists(IdentityMgtConstants.IDENTITY_MANAGEMENT_QUESTIONS)){
                Collection collection = (Collection) registry.
                                    get(IdentityMgtConstants.IDENTITY_MANAGEMENT_QUESTIONS);
                String[] children =  collection.getChildren();
                for(String child : children){
                    Resource resource = registry.get(child);
                    String question = resource.getProperty("question");
                    String isPromoteQuestion = resource.getProperty("isPromoteQuestion");
                    String questionSetId = resource.getProperty("questionSetId");
                    if(question != null){
                        ChallengeQuestionDTO questionDTO = new ChallengeQuestionDTO();
                        questionDTO.setQuestion(question);
                        if(isPromoteQuestion != null){
                            if(Boolean.parseBoolean(isPromoteQuestion)){
                                questionDTO.setPromoteQuestion(true);
                            }
                        }
                        if(questionSetId != null){
                            questionDTO.setQuestionSetId(questionSetId);
                        }
                        questionDTO.setPromoteQuestion(false);
                        questionDTOs.add(questionDTO);
                    }
                }

            }
        } catch (RegistryException e) {
            throw new IdentityMgtException(e.getMessage(), e);
        }
        return questionDTOs;
    }

    /**
     *
     * @param questionDTOs
     * @throws IdentityMgtException
     */
    public void setChallengeQuestions(ChallengeQuestionDTO[] questionDTOs) throws IdentityMgtException {
        Registry registry = null;
        try {
            registry = IdentityMgtServiceComponent.getRegistryService().getConfigSystemRegistry();

            Resource identityMgtResource = registry.get(IdentityMgtConstants.IDENTITY_MANAGEMENT_PATH);
            if(identityMgtResource != null){
                String questionCollectionPath = IdentityMgtConstants.IDENTITY_MANAGEMENT_QUESTIONS;
                if(registry.resourceExists(questionCollectionPath)){
                    registry.delete(questionCollectionPath);
                }

                Collection questionCollection = registry.newCollection();
                registry.put(questionCollectionPath, questionCollection);

                for(int i = 0; i < questionDTOs.length; i++){
                    Resource resource = registry.newResource();
                    resource.addProperty("question", questionDTOs[i].getQuestion());
                    resource.addProperty("isPromoteQuestion", String.valueOf(questionDTOs[i].isPromoteQuestion()));
                    resource.addProperty("questionSetId", questionDTOs[i].getQuestionSetId());
                    registry.put(IdentityMgtConstants.IDENTITY_MANAGEMENT_QUESTIONS +
                            RegistryConstants.PATH_SEPARATOR + "question" + i +
                            RegistryConstants.PATH_SEPARATOR, resource);
                    if(questionDTOs[i].getQuestionSetId() != null){
                        String setId = questionDTOs[i].getQuestionSetId().trim();
                        UserRealm realm = IdentityMgtServiceComponent.getRealmService().getBootstrapRealm();
                        String[] claimUris = realm.getClaimManager().getAllClaimUris();
                        if(!(Arrays.asList((claimUris.length)).contains(setId))){
                            ClaimMapping mapping = new ClaimMapping();
                            Claim claim =  new Claim();
                            claim.setClaimUri(setId);
                            claim.setDisplayTag(setId);
                            claim.setDescription(setId);
                            claim.setDialectURI(UserCoreConstants.DEFAULT_CARBON_DIALECT);
                            mapping.setClaim(claim);
                            realm.getClaimManager().addNewClaimMapping(mapping);
                        }
                    }
                }
            }
        } catch (RegistryException e) {
            throw new IdentityMgtException(e.getMessage(), e);
        } catch (UserStoreException e) {
             throw new IdentityMgtException(e.getMessage(), e);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new IdentityMgtException(e.getMessage(), e);
        }

    }

    /**
     *  // TODO manage oder
     * @param userName
     * @param tenantId
     * @return
     */
    public UserChallengesDTO[] getChallengeQuestionsOfUser(String userName, int tenantId){

        ArrayList<UserChallengesDTO> challengesDTOs = new ArrayList<UserChallengesDTO>();
        try {
            if (log.isDebugEnabled()) {
                log.debug("Retrieving Challenge question from the user profile.");
            }
            List<String> challengesUris = getChallengeQuestionUris(userName, tenantId);

            for(int i = 0; i < challengesUris.size(); i ++){
                String challengesUri = challengesUris.get(i).trim();
                String challengeValue = ClaimsMgtUtil.getClaimFromUserStoreManager(userName,
                                                                        tenantId, challengesUri);

                String[] challengeValues = challengeValue.split(",");
                if(challengeValues != null && challengeValues.length == 2){
                    UserChallengesDTO dto = new UserChallengesDTO();
                    dto.setId(challengesUri);
                    dto.setQuestion(challengeValues[0].trim());
                    dto.setAnswer(challengeValues[1].trim());
                    dto.setOrder(i);
                    dto.setPrimary(false);
                    challengesDTOs.add(dto);
                }
            }

        } catch (Exception e) {
            String msg = "No associated challenge question found for the user";
            log.debug(msg, e);
        }

        if(challengesDTOs.size() > 0){
            return challengesDTOs.toArray(new UserChallengesDTO[challengesDTOs.size()]);
        } else {
            return new UserChallengesDTO[0];
        }

    }

    /**
     *
     * @param userName
     * @param tenantId
     * @return
     */
    public List<String> getChallengeQuestionUris(String userName, int tenantId) {

        if (log.isDebugEnabled()) {
            log.debug("Challenge Question from the user profile.");
        }

        List<String> challenges = new ArrayList<String>();
        String claimValue = null;
        String[] challengesUris;

        try {
            claimValue = ClaimsMgtUtil.getClaimFromUserStoreManager(userName, tenantId,
                                   UserCoreConstants.ClaimTypeURIs.CHALLENGES_URI);
        } catch (IdentityMgtException e) {
            log.error("");
        }

        if(claimValue != null){
            if(claimValue.contains(",")){
                challengesUris = claimValue.split(",");
            } else {
                challengesUris =  new String[]{claimValue.trim()};
            }

            for(String challengesUri : challengesUris){
                challenges.add(challengesUri.trim());
            }
        }

        return challenges;
    }

    /**
     *
     * @param userName
     * @param tenantId
     * @return
     */
    public int getNoOfChallengeQuestions(String userName, int tenantId) {

        List<String> questions = getChallengeQuestionUris(userName, tenantId);
        if(questions != null){
            return questions.size();
        }

        return 0;
    }

    /**
     *
     * @param userName
     * @param tenantId
     * @param challengesDTOs
     * @throws IdentityMgtException
     */
    public void setChallengesOfUser(String userName, int tenantId,
                                       UserChallengesDTO[] challengesDTOs) throws IdentityMgtException {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Challenge Question from the user profile.");
            }
            List<String> challengesUris = getChallengeQuestionUris(userName, tenantId);
            String challengesUrisValue = "";

            if(challengesDTOs != null && challengesDTOs.length > 0) {
                for(UserChallengesDTO dto : challengesDTOs){
                    if(dto.getId() != null){
                        if(dto.getQuestion() != null && dto.getAnswer() != null){
                            String claimValue = dto.getQuestion().trim() + "," +
                                    PasswordUtil.doHash(dto.getAnswer().trim());
                            ClaimsMgtUtil.setClaimInUserStoreManager(userName,
                                    tenantId, dto.getId().trim(), claimValue);
                            if(!challengesUris.contains(dto.getId().trim())){
                                challengesUris.add(dto.getId().trim());
                            }
                        }
                    }
                }

                for(String challengesUri: challengesUris){
                    if("".equals(challengesUrisValue)){
                        challengesUrisValue = challengesUri;
                    } else {
                        challengesUrisValue = challengesUrisValue + "," + challengesUri;
                    }

                }

                ClaimsMgtUtil.setClaimInUserStoreManager(userName, tenantId,
                            UserCoreConstants.ClaimTypeURIs.CHALLENGES_URI, challengesUrisValue);

            }
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            String msg = "No associated challenge question found for the user";
            throw new IdentityMgtException(msg, e);
        }
    }

    /**
     *
     * @param userName
     * @param tenantId
     * @param challengesDTOs
     * @return
     */
    public boolean verifyChallengeQuestion(String userName, int tenantId,
                                                        UserChallengesDTO[] challengesDTOs) {

        boolean verification = false;
        try {
            if (log.isDebugEnabled()) {
                log.debug("Challenge Question from the user profile.");
            }

            UserChallengesDTO[] storedDto = getChallengeQuestionsOfUser(userName, tenantId) ;

            for(UserChallengesDTO challengesDTO : challengesDTOs){
                if(challengesDTO.getAnswer() == null || challengesDTO.getAnswer().trim().length() < 1){
                    return false;
                }

                for(UserChallengesDTO dto : storedDto){
                    if(challengesDTO.getId() == null || !challengesDTO.getId().trim().equals(dto.getId())){
                        if(challengesDTO.getQuestion() == null || !challengesDTO.getQuestion().
                                trim().equals(dto.getQuestion())){
                            continue;
                        }
                    }

                    String hashedAnswer = PasswordUtil.doHash(challengesDTO.getAnswer().trim().toLowerCase());
                    if(hashedAnswer.equals(dto.getAnswer())){
                        verification = true;
                    } else {
                        return false;
                    }

                }
            }
        } catch (Exception e) {
            String msg = "No associated challenge question found for the user";
            log.debug(msg, e);
        }

        return verification;
    }


    /**
     * 
     * @param userName
     * @param tenantId
     * @return
     */
    public UserChallengesDTO[] getPrimaryChallengeQuestionsOfUser(String userName, int tenantId) {
        
        ArrayList<UserChallengesDTO> challengesDTOs = new ArrayList<UserChallengesDTO>();
        try {
            if (log.isDebugEnabled()) {
                log.debug("Challenge Question from the user profile.");
            }
            String claimValue;

            claimValue = ClaimsMgtUtil.getClaimFromUserStoreManager(userName, tenantId,
                                   UserCoreConstants.ClaimTypeURIs.PRIMARY_CHALLENGES);

            String[] challenges = claimValue.split(",");
            for(String challenge : challenges){
                UserChallengesDTO dto = new UserChallengesDTO();
                String question = challenge.substring(0, challenge.indexOf("="));
                dto.setQuestion(question);
                dto.setPrimary(true);
                challengesDTOs.add(dto);
            }

        } catch (Exception e) {
            String msg = "No associated challenge question found for the user";
            log.debug(msg, e);
        }

        if(challengesDTOs.size() > 0){
            return challengesDTOs.toArray(new UserChallengesDTO[challengesDTOs.size()]);
        } else {
            return new UserChallengesDTO[0];
        }
    }

    /**
     * 
     * @param userName
     * @param tenantId
     * @param challengesDTOs
     * @return
     * @throws org.wso2.carbon.user.api.UserStoreException
     */
    public boolean verifyPrimaryChallengeQuestion(String userName, int tenantId,
                                                        UserChallengesDTO[] challengesDTOs) {

        boolean verification = false;
        try {
            if (log.isDebugEnabled()) {
                log.debug("Challenge Question from the user profile.");
            }
            String claimValue;

            for(UserChallengesDTO challengesDTO : challengesDTOs){
                claimValue = ClaimsMgtUtil.getClaimFromUserStoreManager(userName, tenantId,
                                       UserCoreConstants.ClaimTypeURIs.PRIMARY_CHALLENGES);

                String[] challenges = claimValue.split(",");
                for(String challenge : challenges){
                    String challengeQuestion = challenge.substring(0, challenge.indexOf("=")).trim();
                    if(challengeQuestion.equals(challengesDTO.getQuestion().trim())){
                        String challengeAnswer = challenge.substring(challenge.indexOf("=") + 1).trim();
                        if(challengeAnswer.
                                equals(PasswordUtil.doHash(challengesDTO.getAnswer().trim().toLowerCase()))){
                            verification = true;
                        } else {
                            return false;
                        }
                    }
                }
            }
        } catch (Exception e) {
            String msg = "No associated challenge question found for the user";
            log.debug(msg, e);
        }

        return verification;
    }
   
}
