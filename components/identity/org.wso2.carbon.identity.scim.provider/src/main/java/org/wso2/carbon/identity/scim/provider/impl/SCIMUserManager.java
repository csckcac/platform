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
package org.wso2.carbon.identity.scim.provider.impl;

import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.user.api.Claim;
import org.wso2.carbon.user.api.ClaimManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.charon.core.attributes.Attribute;
import org.wso2.charon.core.attributes.SimpleAttribute;
import org.wso2.charon.core.exceptions.CharonException;
import org.wso2.charon.core.exceptions.NotFoundException;
import org.wso2.charon.core.extensions.UserManager;
import org.wso2.charon.core.objects.Group;
import org.wso2.charon.core.objects.User;

import org.apache.commons.logging.Log;
import org.wso2.charon.core.schema.ResourceSchema;
import org.wso2.charon.core.schema.SCIMSchemaDefinitions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SCIMUserManager implements UserManager {
    UserStoreManager carbonUM = null;
    ClaimManager carbonClaimManager = null;

    private static Log log = LogFactory.getLog(SCIMUserManager.class);

    public SCIMUserManager(UserStoreManager carbonUserStoreManager) {
        carbonUM = carbonUserStoreManager;
    }

    public User createUser(User user) throws CharonException {
        log.info("User: " + user.getUserName() + " is created.");
        //get user attributes map: <String(scimAttributeName),Attribute-(simple,multivalue,complex)>

        //if attribute == simple, new attribute map: <String(attributeName),value>
        /*if attribute == complex,
          read all subattributes,
            if sub attribute == simple, addsimple attribute to map  (attributename == mainattribute.subattributename)
            if sub attribute == mt  addmt attribute
         */
        /**
         * if attribute == MTAttribute,
         * if attribute value, simple attribute, add simple attribute (attributename == MTAttributeName#valueAttributename)
         */

        Map<String, String> claims = new HashMap<String, String>();
        //String[] roles = (String[]) user.getGroups().toArray();

        try {
            carbonUM.addUser(user.getUserName(), user.getPassword(), null, claims, null);
        } catch (UserStoreException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        //carbonUM.
        // when user claims are returned convert them to SCIM claims and construct SCIM attributes.

        return user;
    }

    public User getUser(String userName) throws CharonException {
        if (log.isDebugEnabled()) {
            log.debug("Get user: " + userName);
        }
        ResourceSchema userResourceSchema = SCIMSchemaDefinitions.SCIM_USER_SCHEMA;
        User scimUser = null;
        try {
            //TODO:make atributeschema list a map

            //get claims related to SCIM claim dialect
            carbonClaimManager.getAllClaims("scim.claim");
            //obtain user claim values
            Claim[] claims = carbonUM.getUserClaimValues(userName, null);
            if (claims != null && claims.length != 0) {
                scimUser = new User();
                for (Claim claim : claims) {
                    String scimAttributeName = claim.getDisplayTag();
                    String value = claim.getValue();
                    //SCIMAttributeSchema attributeSchema = userResourceSchema.getAttributeSchemaMap.get(scimAttributeName);
                    //if it is not found, i.e. it is a sub attribute. find it in attributes, sub attributes
                    //if attribute schema found, and if sub attribute, find its parent,
                    //if parent exist in user, fill sub attribute, if not, create parent and fill sub attribute.
                    //if (scimAttributeName.equals(SCIMSchemaDefinitions.USER_NAME.getName())) {
                    if(scimAttributeName.equals("")){
                        Attribute userNameAttribute = new SimpleAttribute(scimAttributeName,value);
                        //create attribute using defaultattrfactory
                        scimUser.setAttribute(userNameAttribute);
                    }
                }
            }

        } catch (UserStoreException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return scimUser;
    }

    public List<User> listUsers() throws CharonException {
        return null;
    }

    public List<User> listUsersByAttribute(Attribute attribute) {
        return null;
    }

    public List<User> listUsersByFilter(String s) {
        return null;
    }

    public List<User> listUsersBySort(String s, String s1) {
        return null;
    }

    public List<User> listUsersWithPagination(int i, int i1) {
        return null;
    }

    public User updateUser(User user) throws CharonException {
        return null;
    }

    public User updateUser(List<Attribute> attributes) {
        return null;
    }

    public void deleteUser(String s) throws NotFoundException, CharonException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public Group createGroup(Group group) throws CharonException {
        return null;
    }

    public Group getGroup(String s) throws CharonException {
        return null;
    }

    public List<Group> listGroups() throws CharonException {
        return null;
    }

    public List<Group> listGroupsByAttribute(Attribute attribute) throws CharonException {
        return null;
    }

    public List<Group> listGroupsByFilter(String s) throws CharonException {
        return null;
    }

    public List<Group> listGroupsBySort(String s, String s1) throws CharonException {
        return null;
    }

    public List<Group> listGroupsWithPagination(int i, int i1) {
        return null;
    }

    public Group updateGroup(Group group) throws CharonException {
        return null;
    }

    public Group updateGroup(List<Attribute> attributes) throws CharonException {
        return null;
    }

    public void deleteGroup(String s) throws NotFoundException, CharonException {

    }

}
