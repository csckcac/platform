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
package org.wso2.carbon.issue.tracker.mgt.ldap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.naming.NamingException;
import javax.naming.directory.*;

/**
 * this class implements methods to manage ldap groups and add/remove members using ApacheDS
 */
public class ApacheDsLdapConnector {

    public static Log log = LogFactory.getLog(ApacheDsLdapConnector.class.getName());


    public void addMemberToLdapGroup(DirContext ctx, String userUID, String groupCN) {
        String memberAttrValue = "uid=" + userUID + "," + LdapConstants.LDAP_OU_USERS_DN;
        String groupDName = "cn=" + groupCN + "," + LdapConstants.LDAP_OU_GROUPS_DN;
        Attribute attr = new BasicAttribute(LdapConstants.LDPA_OU_GROUP_MEMBER_ATTR, memberAttrValue);
        ModificationItem mi = new ModificationItem(DirContext.ADD_ATTRIBUTE, attr);
        ModificationItem[] modArray = {mi};

        try {
            if (!isGroupExistedInOUGroups(ctx, groupCN)) {
                //i.e. ou=groupCN is not created. So it need to be created with a first user
                createGroupInLDAP(ctx, groupCN, attr);
                if (log.isDebugEnabled()) {
                    log.debug(groupDName + " : user group created in LDAP with first user: " + attr.get());
                }
            } else {
                ctx.modifyAttributes(groupDName, modArray);
                if (log.isDebugEnabled()) {
                    log.debug(attr.get() + " : user was added to group: " + groupDName);
                }
            }

        } catch (NoSuchAttributeException e) {
            log.error(e.getMessage(), e);

        } catch (AttributeInUseException e) {
            log.error("The value:" + memberAttrValue + " is already exists for attribute:" + LdapConstants.LDPA_OU_GROUP_MEMBER_ATTR + " in DN:" + groupDName + ". See the exception stack for more details.", e);
        } catch (NamingException e) {
            log.error(e.getMessage(), e);

        }

    }


    public void removeMemberFromLdapGroup(DirContext dirContext,String userUID, String groupCN) {

        String memberAttributeValue = "uid=" +userUID+ "," + LdapConstants.LDAP_OU_USERS_DN;
        String groupDName = "cn="+groupCN + "," + LdapConstants.LDAP_OU_GROUPS_DN;
        Attribute attribute = new BasicAttribute(LdapConstants.LDPA_OU_GROUP_MEMBER_ATTR, memberAttributeValue);
        ModificationItem modificationItem = new ModificationItem(DirContext.REMOVE_ATTRIBUTE, attribute);
        ModificationItem[] modificationItems = {modificationItem};
        try {
            dirContext.modifyAttributes(groupDName, modificationItems);
        } catch (NamingException e) {
            log.error(e.getMessage());
        }
    }



    public void createGroupInLDAP(DirContext ctx, String groupCN) {
        BasicAttributes basicAttributes = new BasicAttributes(true);
        BasicAttribute objectClass = new BasicAttribute("objectClass");
        objectClass.add("groupOfNames");
        objectClass.add("top");

        BasicAttribute cn = new BasicAttribute("cn");
        cn.add(groupCN);

        basicAttributes.put(objectClass);
        basicAttributes.put(cn);

        try {
            String name = "cn=" + groupCN + "," + LdapConstants.LDAP_OU_GROUPS_DN;
            ctx.createSubcontext(name, basicAttributes);
            if (log.isDebugEnabled()) {
                log.debug("Created group:" + name);
            }
        } catch (NamingException e) {
            log.error("Creating group:" + groupCN + " failed in " + LdapConstants.LDAP_OU_GROUPS_DN, e);
        }
    }


    public boolean isUserExistedInOUUsers(DirContext ctx, String userUID) {
        String userEntryName = "uid=" + userUID + "," + LdapConstants.LDAP_OU_USERS_DN;
        try {
            Attributes attrs = ctx.getAttributes(userEntryName);
            if (log.isDebugEnabled()) {
                log.debug("uid:" + userUID + " existed at " + LdapConstants.LDAP_OU_USERS_DN);
            }
            return true;
        } catch (NamingException e) {
            if (log.isDebugEnabled()) {
                log.debug(userEntryName + " is not existed.", e);
            }
            return false;
        }
    }

    public boolean isGroupExistedInOUGroups(DirContext ctx, String groupCN) {
        String groupEntryName = "cn=" + groupCN + "," + LdapConstants.LDAP_OU_GROUPS_DN;

        try {
            Attributes attrs = ctx.getAttributes(groupEntryName);
            if (log.isDebugEnabled()) {
                log.debug(groupEntryName + " existed.");
            }
            return true;
        } catch (NamingException e) {
            //e.printStackTrace();
            if (log.isDebugEnabled()) {
                log.debug(groupEntryName + " : is not found.", e);
            }
            return false;
        }
    }


    public void createGroupInLDAP(DirContext ctx, String groupCN, Attribute firstUser) {
        BasicAttributes basicAttributes = new BasicAttributes(true);
        BasicAttribute objectClass = new BasicAttribute("objectClass");
        objectClass.add("groupOfNames");
        objectClass.add("top");

        BasicAttribute cn = new BasicAttribute("cn");
        cn.add(groupCN);

        basicAttributes.put(objectClass);
        basicAttributes.put(cn);
        basicAttributes.put(firstUser);

        try {
            String name = "cn=" + groupCN + "," + LdapConstants.LDAP_OU_GROUPS_DN;
            ctx.createSubcontext(name, basicAttributes);
            if (log.isDebugEnabled()) {
                log.debug("Created group:" + name);
            }
        } catch (NamingException e) {
            log.error("Creating group:" + groupCN + " failed in " + LdapConstants.LDAP_OU_GROUPS_DN, e);
        }
    }
}

