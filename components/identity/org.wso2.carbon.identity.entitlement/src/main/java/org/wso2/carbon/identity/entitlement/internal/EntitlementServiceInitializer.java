/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.identity.entitlement.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.IdentityRegistryResources;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * This class puts resources to registry, which are used for creating a XACML policy
 */
public class EntitlementServiceInitializer {

    private static Log log = LogFactory.getLog(EntitlementServiceInitializer.class);

    RegistryService registryService;

    public EntitlementServiceInitializer(RegistryService registryService) {
        this.registryService = registryService;
    }

    public void putEntitlementPolicyResourcesToRegistry() throws IdentityException {
        //TODO Needs to clearly identify the required data to put as registry resources and Followings are Sample resources which must be modified or removed 
        String resourcePath = IdentityRegistryResources.ENTITLEMENT_POLICY_RESOURCES;
        try {
            UserRegistry registry = registryService.getGovernanceSystemRegistry(MultitenantConstants.
                    SUPER_TENANT_ID);
            Collection policyResourceCollection = registry.newCollection();
            registry.put(resourcePath, policyResourceCollection);
            if(!registry.resourceExists(resourcePath + "ruleCombiningAlgorithms")){

                Resource ruleCombiningAlgorithm  = registry.newResource();
                String ruleCombiningAlgorithms =
                    "deny-overrides\n" +
                    "permit-overrides\n" +
                    "first-applicable\n";
                InputStream inputStream = new ByteArrayInputStream(ruleCombiningAlgorithms.getBytes());
                ruleCombiningAlgorithm.setContent(inputStream);
                registry.put(resourcePath + "ruleCombiningAlgorithms" , ruleCombiningAlgorithm);
            }

            if(!registry.resourceExists(resourcePath + "policyCombiningAlgorithms")){
                Resource policyCombiningAlgorithm  = registry.newResource();
                String policyCombiningAlgorithms =
                    "deny-overrides\n" +
                    "permit-overrides\n" +
                    "first-applicable\n" +
                    "ordered-deny-overrides\n"+
                    "ordered-permit-overrides\n"+
                    "only-one-applicable";
                InputStream inputStream = new ByteArrayInputStream(policyCombiningAlgorithms.getBytes());
                policyCombiningAlgorithm.setContent(inputStream);
                registry.put(resourcePath + "policyCombiningAlgorithms" , policyCombiningAlgorithm);
            }


            if(!registry.resourceExists(resourcePath + "targetFunctionId")){
                Resource targetFunctionId  = registry.newResource();
                String targetFunctionIds =
                    "matching-with\n" +
                    "at-least-one-matching-with\n" +
                    "at-least-one-matching-reg-ex-with\n" +
                    "matching-set-with\n"+
                    "matching-reg-ex-set-with";
                InputStream inputStream = new ByteArrayInputStream(targetFunctionIds.getBytes());
                targetFunctionId.setContent(inputStream);
                registry.put(resourcePath + "targetFunctionId" , targetFunctionId);
            }

            if(!registry.resourceExists(resourcePath + "ruleFunctionId")){
                Resource ruleFunctionId  = registry.newResource();
                String ruleFunctionIds =
                    "is-in\n" +
                    "at-least-one-member-of\n" +
                    "at-least-one-matching-reg-ex-with\n" +
                    "matching-set-with\n"+
                    "matching-reg-ex-set-with";
                InputStream inputStream = new ByteArrayInputStream(ruleFunctionIds.getBytes());
                ruleFunctionId.setContent(inputStream);
                registry.put(resourcePath + "ruleFunctionId" , ruleFunctionId);
            }
                      
            if(!registry.resourceExists(resourcePath + "functionId")){

                Resource functionId = registry.newResource();
                String functionIds =
                "urn:oasis:names:tc:xacml:1.0:function:string-equal\n" +
                "urn:oasis:names:tc:xacml:1.0:function:string-intersection\n" +
                "urn:oasis:names:tc:xacml:1.0:function:string-at-least-one-member-of\n" +
                "urn:oasis:names:tc:xacml:1.0:function:string-union\n" +
                "urn:oasis:names:tc:xacml:1.0:function:string-subset\n" +
                "urn:oasis:names:tc:xacml:1.0:function:string-set-equals\n" +
                "urn:oasis:names:tc:xacml:1.0:function:string-normalize-space\n" +
                "urn:oasis:names:tc:xacml:1.0:function:string-normalize-to-lower-case\n" +
                "urn:oasis:names:tc:xacml:1.0:function:string-greater-than\n" +
                "urn:oasis:names:tc:xacml:1.0:function:string-greater-than-or-equal\n" +
                "urn:oasis:names:tc:xacml:1.0:function:string-less-than\n" +
                "urn:oasis:names:tc:xacml:1.0:function:string-less-than-or-equal\n" +
                "urn:oasis:names:tc:xacml:1.0:function:string-one-and-only\n" +
                "urn:oasis:names:tc:xacml:1.0:function:string-bag-size\n" +
                "urn:oasis:names:tc:xacml:1.0:function:string-is-in\n" +
                "urn:oasis:names:tc:xacml:1.0:function:string-bag\n" +
                "urn:oasis:names:tc:xacml:1.0:function:string-regexp-match\n" +
                "urn:oasis:names:tc:xacml:1.0:function:boolean-equal\n" +
                "urn:oasis:names:tc:xacml:1.0:function:boolean-one-and-only\n" +
                "urn:oasis:names:tc:xacml:1.0:function:boolean-bag-size\n" +
                "urn:oasis:names:tc:xacml:1.0:function:boolean-is-in\n" +
                "urn:oasis:names:tc:xacml:1.0:function:boolean-bag\n" +
                "urn:oasis:names:tc:xacml:1.0:function:boolean-intersection\n" +
                "urn:oasis:names:tc:xacml:1.0:function:boolean-at-least-one-member-of\n" +
                "urn:oasis:names:tc:xacml:1.0:function:boolean-union\n" +
                "urn:oasis:names:tc:xacml:1.0:function:boolean-subset\n" +
                "urn:oasis:names:tc:xacml:1.0:function:boolean-set-equals\n" +
                "rn:oasis:names:tc:xacml:1.0:function:integer-equal\n" +
                "urn:oasis:names:tc:xacml:1.0:function:integer-mod\n" +
                "urn:oasis:names:tc:xacml:1.0:function:integer-abs\n" +
                "urn:oasis:names:tc:xacml:1.0:function:integer-add\n" +
                "urn:oasis:names:tc:xacml:1.0:function:integer-divide\n" +
                "urn:oasis:names:tc:xacml:1.0:function:integer-multiply\n" +
                "urn:oasis:names:tc:xacml:1.0:function:integer-subtract\n" +
                "urn:oasis:names:tc:xacml:1.0:function:double-to-integer\n" +
                "urn:oasis:names:tc:xacml:1.0:function:integer-to-double\n" +
                "urn:oasis:names:tc:xacml:1.0:function:integer-greater-than\n" +
                "urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal\n" +
                "urn:oasis:names:tc:xacml:1.0:function:integer-less-than\n" +
                "urn:oasis:names:tc:xacml:1.0:function:integer-less-than-or-equal\n" +
                "urn:oasis:names:tc:xacml:1.0:function:integer-one-and-only\n" +
                "urn:oasis:names:tc:xacml:1.0:function:integer-bag-size\n" +
                "urn:oasis:names:tc:xacml:1.0:function:integer-is-in\n" +
                "urn:oasis:names:tc:xacml:1.0:function:integer-bag\n" +
                "urn:oasis:names:tc:xacml:1.0:function:double-equal\n" +
                "urn:oasis:names:tc:xacml:1.0:function:date-equal\n" +
                "urn:oasis:names:tc:xacml:1.0:function:time-equal\n" +
                "urn:oasis:names:tc:xacml:1.0:function:dateTime-equal\n" +
                "urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-equal\n" +
                "urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-equal\n" +
                "urn:oasis:names:tc:xacml:1.0:function:anyURI-equal\n" +
                "urn:oasis:names:tc:xacml:1.0:function:x500Name-equal\n" +
                "urn:oasis:names:tc:xacml:1.0:function:rfc822Name-equal\n" +
                "urn:oasis:names:tc:xacml:1.0:function:hexBinary-equal\n" +
                "urn:oasis:names:tc:xacml:1.0:function:base64Binary-equal\n" +
                "urn:oasis:names:tc:xacml:1.0:function:double-add\n" +
                "urn:oasis:names:tc:xacml:1.0:function:double-subtract\n" +
                "urn:oasis:names:tc:xacml:1.0:function:double-multiply\n" +
                "urn:oasis:names:tc:xacml:1.0:function:double-divide\n" +
                "urn:oasis:names:tc:xacml:1.0:function:double-abs\n" +
                "urn:oasis:names:tc:xacml:1.0:function:round\n" +
                "urn:oasis:names:tc:xacml:1.0:function:floor\n" +
                "urn:oasis:names:tc:xacml:1.0:function:or\n" +
                "urn:oasis:names:tc:xacml:1.0:function:and\n" +
                "urn:oasis:names:tc:xacml:1.0:function:n-of\n" +
                "urn:oasis:names:tc:xacml:1.0:function:not\n" +
                "urn:oasis:names:tc:xacml:1.0:function:double-greater-than\n" +
                "urn:oasis:names:tc:xacml:1.0:function:double-greater-than-or-equal\n" +
                "urn:oasis:names:tc:xacml:1.0:function:double-less-than\n" +
                "urn:oasis:names:tc:xacml:1.0:function:double-less-than-or-equal\n" +
                "urn:oasis:names:tc:xacml:1.0:function:dateTime-add-dayTimeDuration\n" +
                "urn:oasis:names:tc:xacml:1.0:function:dateTime-add-yearMonthDuration\n" +
                "urn:oasis:names:tc:xacml:1.0:function:dateTime-subtract-dayTimeDuration\n" +
                "urn:oasis:names:tc:xacml:1.0:function:dateTime-subtract-\n" +
                "yearMonthDuration\n" +
                "urn:oasis:names:tc:xacml:1.0:function:date-add-yearMonthDuration\n" +
                "urn:oasis:names:tc:xacml:1.0:function:date-subtract-yearMonthDuration\n" +
                "urn:oasis:names:tc:xacml:1.0:function:time-greater-than\n" +
                "urn:oasis:names:tc:xacml:1.0:function:time-greater-than-or-equal\n" +
                "urn:oasis:names:tc:xacml:1.0:function:time-less-than\n" +
                "urn:oasis:names:tc:xacml:1.0:function:time-less-than-or-equal\n" +
                "urn:oasis:names:tc:xacml:2.0:function:time-in-range\n" +
                "urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than\n" +
                "urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than-or-equal\n" +
                "urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than\n" +
                "urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than-or-equal\n" +
                "urn:oasis:names:tc:xacml:1.0:function:date-greater-than\n" +
                "urn:oasis:names:tc:xacml:1.0:function:date-greater-than-or-equal\n" +
                "urn:oasis:names:tc:xacml:1.0:function:date-less-than\n" +
                "urn:oasis:names:tc:xacml:1.0:function:date-less-than-or-equal\n" +
                "urn:oasis:names:tc:xacml:1.0:function:double-one-and-only\n" +
                "urn:oasis:names:tc:xacml:1.0:function:double-bag-size\n" +
                "urn:oasis:names:tc:xacml:1.0:function:double-is-in\n" +
                "urn:oasis:names:tc:xacml:1.0:function:double-bag\n" +
                "urn:oasis:names:tc:xacml:1.0:function:time-one-and-only\n" +
                "urn:oasis:names:tc:xacml:1.0:function:time-bag-size\n" +
                "urn:oasis:names:tc:xacml:1.0:function:time-is-in\n" +
                "urn:oasis:names:tc:xacml:1.0:function:time-bag\n" +
                "urn:oasis:names:tc:xacml:1.0:function:date-one-and-only\n" +
                "urn:oasis:names:tc:xacml:1.0:function:date-bag-size\n" +
                "urn:oasis:names:tc:xacml:1.0:function:date-is-in\n" +
                "urn:oasis:names:tc:xacml:1.0:function:date-bag\n" +
                "urn:oasis:names:tc:xacml:1.0:function:dateTime-one-and-only\n" +
                "urn:oasis:names:tc:xacml:1.0:function:dateTime-bag-size\n" +
                "urn:oasis:names:tc:xacml:1.0:function:dateTime-is-in\n" +
                "urn:oasis:names:tc:xacml:1.0:function:dateTime-bag\n" +
                "urn:oasis:names:tc:xacml:1.0:function:anyURI-one-and-only\n" +
                "urn:oasis:names:tc:xacml:1.0:function:anyURI-bag-size\n" +
                "urn:oasis:names:tc:xacml:1.0:function:anyURI-is-in\n" +
                "urn:oasis:names:tc:xacml:1.0:function:anyURI-bag\n" +
                "urn:oasis:names:tc:xacml:1.0:function:hexBinary-one-and-only\n" +
                "urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag-size\n" +
                "urn:oasis:names:tc:xacml:1.0:function:hexBinary-is-in\n" +
                "urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag\n" +
                "urn:oasis:names:tc:xacml:1.0:function:base64Binary-one-and-only\n" +
                "urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag-size\n" +
                "urn:oasis:names:tc:xacml:1.0:function:base64Binary-is-in\n" +
                "urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag\n" +
                "urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-one-and-only\n" +
                "urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-bag-size\n" +
                "urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-is-in\n" +
                "urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-bag\n" +
                "urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-one-and-only\n" +
                "urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-bag-size\n" +
                "urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-is-in\n" +
                "urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-bag\n" +
                "urn:oasis:names:tc:xacml:1.0:function:x500Name-one-and-only\n" +
                "urn:oasis:names:tc:xacml:1.0:function:x500Name-bag-size\n" +
                "urn:oasis:names:tc:xacml:1.0:function:x500Name-is-in\n" +
                "urn:oasis:names:tc:xacml:1.0:function:x500Name-bag\n" +
                "urn:oasis:names:tc:xacml:1.0:function:rfc822Name-one-and-only\n" +
                "urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag-size\n" +
                "urn:oasis:names:tc:xacml:1.0:function:rfc822Name-is-in\n" +
                "urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag\n" +
                "urn:oasis:names:tc:xacml:1.0:function:any-of\n" +
                "urn:oasis:names:tc:xacml:1.0:function:all-of\n" +
                "urn:oasis:names:tc:xacml:1.0:function:any-of-any\n" +
                "urn:oasis:names:tc:xacml:1.0:function:all-of-any\n" +
                "urn:oasis:names:tc:xacml:1.0:function:any-of-all\n" +
                "urn:oasis:names:tc:xacml:1.0:function:all-of-all\n" +
                "urn:oasis:names:tc:xacml:1.0:function:map\n" +
                "urn:oasis:names:tc:xacml:1.0:function:x500Name-match\n" +
                "urn:oasis:names:tc:xacml:1.0:function:rfc822Name-match\n" +
                "urn:oasis:names:tc:xacml:2.0:function:anyURI-regexp-match\n" +
                "urn:oasis:names:tc:xacml:2.0:function:ipAddress-regexp-match\n" +
                "urn:oasis:names:tc:xacml:2.0:function:dnsName-regexp-match\n" +
                "urn:oasis:names:tc:xacml:2.0:function:rfc822Name-regexp-match\n" +
                "urn:oasis:names:tc:xacml:2.0:function:x500Name-regexp-match\n" +
                "urn:oasis:names:tc:xacml:1.0:function:xpath-node-count\n" +
                "urn:oasis:names:tc:xacml:1.0:function:xpath-node-equal\n" +
                "urn:oasis:names:tc:xacml:1.0:function:xpath-node-match\n" +
                "urn:oasis:names:tc:xacml:1.0:function:integer-intersection\n" +
                "urn:oasis:names:tc:xacml:1.0:function:integer-at-least-one-member-of\n" +
                "urn:oasis:names:tc:xacml:1.0:function:integer-union\n" +
                "urn:oasis:names:tc:xacml:1.0:function:integer-subset\n" +
                "urn:oasis:names:tc:xacml:1.0:function:integer-set-equals\n" +
                "urn:oasis:names:tc:xacml:1.0:function:double-intersection\n" +
                "urn:oasis:names:tc:xacml:1.0:function:double-at-least-one-member-of\n" +
                "urn:oasis:names:tc:xacml:1.0:function:double-union\n" +
                "urn:oasis:names:tc:xacml:1.0:function:double-subset\n" +
                "urn:oasis:names:tc:xacml:1.0:function:double-set-equals\n" +
                "urn:oasis:names:tc:xacml:1.0:function:time-intersection\n" +
                "urn:oasis:names:tc:xacml:1.0:function:time-at-least-one-member-of\n" +
                "urn:oasis:names:tc:xacml:1.0:function:time-union\n" +
                "urn:oasis:names:tc:xacml:1.0:function:time-subset\n" +
                "urn:oasis:names:tc:xacml:1.0:function:time-set-equals\n" +
                "urn:oasis:names:tc:xacml:1.0:function:date-intersection\n" +
                "urn:oasis:names:tc:xacml:1.0:function:date-at-least-one-member-of\n" +
                "urn:oasis:names:tc:xacml:1.0:function:date-union\n" +
                "urn:oasis:names:tc:xacml:1.0:function:date-subset\n" +
                "urn:oasis:names:tc:xacml:1.0:function:date-set-equals\n" +
                "urn:oasis:names:tc:xacml:1.0:function:dateTime-intersection\n" +
                "urn:oasis:names:tc:xacml:1.0:function:dateTime-at-least-one-member-of\n" +
                "urn:oasis:names:tc:xacml:1.0:function:dateTime-union\n" +
                "urn:oasis:names:tc:xacml:1.0:function:dateTime-subset\n" +
                "urn:oasis:names:tc:xacml:1.0:function:dateTime-set-equals\n" +
                "urn:oasis:names:tc:xacml:1.0:function:anyURI-intersection\n" +
                "urn:oasis:names:tc:xacml:1.0:function:anyURI-at-least-one-member-of\n" +
                "urn:oasis:names:tc:xacml:1.0:function:anyURI-union\n" +
                "urn:oasis:names:tc:xacml:1.0:function:anyURI-subset\n" +
                "urn:oasis:names:tc:xacml:1.0:function:anyURI-set-equals\n" +
                "urn:oasis:names:tc:xacml:1.0:function:hexBinary-intersection\n" +
                "urn:oasis:names:tc:xacml:1.0:function:hexBinary-at-least-one-member-of\n" +
                "urn:oasis:names:tc:xacml:1.0:function:hexBinary-union\n" +
                "urn:oasis:names:tc:xacml:1.0:function:hexBinary-subset\n" +
                "urn:oasis:names:tc:xacml:1.0:function:hexBinary-set-equals\n" +
                "urn:oasis:names:tc:xacml:1.0:function:base64Binary-intersection\n" +
                "urn:oasis:names:tc:xacml:1.0:function:base64Binary-at-least-one-member-\n" +
                "of\n" +
                "urn:oasis:names:tc:xacml:1.0:function:base64Binary-union\n" +
                "urn:oasis:names:tc:xacml:1.0:function:base64Binary-subset\n" +
                "urn:oasis:names:tc:xacml:1.0:function:base64Binary-set-equals\n" +
                "urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-intersection\n" +
                "urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-at-least-one-\n" +
                "member-of\n" +
                "urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-union\n" +
                "urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-subset\n" +
                "urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-set-equals\n" +
                "urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-intersection\n" +
                "urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-at-least-one-\n" +
                "member-of\n" +
                "urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-union\n" +
                "urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-subset\n" +
                "urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-set-equals\n" +
                "urn:oasis:names:tc:xacml:1.0:function:x500Name-intersection\n" +
                "urn:oasis:names:tc:xacml:1.0:function:x500Name-at-least-one-member-of\n" +
                "urn:oasis:names:tc:xacml:1.0:function:x500Name-union\n" +
                "urn:oasis:names:tc:xacml:1.0:function:x500Name-subset\n" +
                "urn:oasis:names:tc:xacml:1.0:function:x500Name-set-equals\n" +
                "urn:oasis:names:tc:xacml:1.0:function:rfc822Name-intersection\n" +
                "urn:oasis:names:tc:xacml:1.0:function:rfc822Name-at-least-one-member-of\n" +
                "urn:oasis:names:tc:xacml:1.0:function:rfc822Name-union\n" +
                "urn:oasis:names:tc:xacml:1.0:function:rfc822Name-subset\n" +
                "urn:oasis:names:tc:xacml:1.0:function:rfc822Name-set-equals";

                InputStream inputStream = new ByteArrayInputStream(functionIds.getBytes());
                functionId.setContent(inputStream);
                registry.put(resourcePath + "functionId" , functionId);
            }

            if (!registry.resourceExists(resourcePath + "matchId")){

                Resource matchId = registry.newResource();
                String matchIds =
                "urn:oasis:names:tc:xacml:1.0:function:string-equal\n" +
                "urn:oasis:names:tc:xacml:1.0:function:integer-equal\n" +
                "urn:oasis:names:tc:xacml:1.0:function:double-equal\n" +
                "urn:oasis:names:tc:xacml:1.0:function:date-equal\n" +
                "urn:oasis:names:tc:xacml:1.0:function:time-equal\n" +
                "urn:oasis:names:tc:xacml:1.0:function:dateTime-equal\n" +
                "urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-equal\n" +
                "urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-equal\n" +
                "urn:oasis:names:tc:xacml:1.0:function:anyURI-equal\n" +
                "urn:oasis:names:tc:xacml:1.0:function:x500Name-equal\n" +
                "urn:oasis:names:tc:xacml:1.0:function:rfc822Name-equal\n" +
                "urn:oasis:names:tc:xacml:1.0:function:hexBinary-equal\n" +
                "urn:oasis:names:tc:xacml:1.0:function:base64Binary-equal\n" +
                "urn:oasis:names:tc:xacml:1.0:function:boolean-equal\n" +
                "urn:oasis:names:tc:xacml:1.0:function:string-regexp-match\n" +
                "urn:oasis:names:tc:xacml:1.0:function:x500Name-match\n" +
                "urn:oasis:names:tc:xacml:1.0:function:rfc822Name-match\n" +
                "urn:oasis:names:tc:xacml:2.0:function:anyURI-regexp-match\n" +
                "urn:oasis:names:tc:xacml:2.0:function:ipAddress-regexp-match\n" +
                "urn:oasis:names:tc:xacml:2.0:function:dnsName-regexp-match\n" +
                "urn:oasis:names:tc:xacml:2.0:function:rfc822Name-regexp-match\n" +
                "urn:oasis:names:tc:xacml:2.0:function:x500Name-regexp-match\n" +
                "urn:oasis:names:tc:xacml:1.0:function:xpath-node-match\n" +
                "urn:oasis:names:tc:xacml:1.0:function:string-less-than\n" +
                "urn:oasis:names:tc:xacml:1.0:function:string-less-than-or-equal\n" +
                "urn:oasis:names:tc:xacml:1.0:function:integer-less-than\n" +
                "urn:oasis:names:tc:xacml:1.0:function:integer-less-than-or-equal\n" +
                "urn:oasis:names:tc:xacml:1.0:function:double-less-than\n" +
                "urn:oasis:names:tc:xacml:1.0:function:double-less-than-or-equal\n" +
                "urn:oasis:names:tc:xacml:1.0:function:time-less-than\n" +
                "urn:oasis:names:tc:xacml:1.0:function:time-less-than-or-equal\n" +
                "urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than\n" +
                "urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than-or-equal\n" +
                "urn:oasis:names:tc:xacml:1.0:function:date-less-than\n" +
                "urn:oasis:names:tc:xacml:1.0:function:date-less-than-or-equal\n" +
                "urn:oasis:names:tc:xacml:1.0:function:string-greater-than\n" +
                "urn:oasis:names:tc:xacml:1.0:function:string-greater-than-or-equal\n" +
                "urn:oasis:names:tc:xacml:1.0:function:integer-greater-than\n" +
                "urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal\n" +
                "urn:oasis:names:tc:xacml:1.0:function:double-greater-than\n" +
                "urn:oasis:names:tc:xacml:1.0:function:double-greater-than-or-equal\n" +
                "urn:oasis:names:tc:xacml:1.0:function:time-greater-than\n" +
                "urn:oasis:names:tc:xacml:1.0:function:time-greater-than-or-equal\n" +
                "urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than\n" +
                "urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than-or-equal\n" +
                "urn:oasis:names:tc:xacml:1.0:function:date-greater-than\n" +
                "urn:oasis:names:tc:xacml:1.0:function:date-greater-than-or-equal";

                InputStream inputStream = new ByteArrayInputStream(matchIds.getBytes());
                matchId.setContent(inputStream);
                registry.put(resourcePath + "matchId" , matchId);
            }
            if(!registry.resourceExists(resourcePath + "dataType")) {

                Resource dataType = registry.newResource();
                String dataTypes =
                    "http://www.w3.org/2001/XMLSchema#string\n" +
                    "http://www.w3.org/2001/XMLSchema#boolean\n" +
                    "http://www.w3.org/2001/XMLSchema#integer\n" +
                    "http://www.w3.org/2001/XMLSchema#double\n" +
                    "http://www.w3.org/2001/XMLSchema#time\n" +
                    "http://www.w3.org/2001/XMLSchema#date\n" +
                    "http://www.w3.org/2001/XMLSchema#dateTime\n" +
                    "http://www.w3.org/TR/2002/WD-xquery-operators-20020816#dayTimeDuration\n" +
                    "http://www.w3.org/TR/2002/WD-xquery-operators-20020816#yearMonthDuration\n" +
                    "http://www.w3.org/2001/XMLSchema#anyURI\n" +
                    "http://www.w3.org/2001/XMLSchema#hexBinary\n" +
                    "http://www.w3.org/2001/XMLSchema#base64Binary\n" +
                    "urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name\n" +
                    "urn:oasis:names:tc:xacml:1.0:data-type:x500Name";
                
                InputStream inputStream = new ByteArrayInputStream(dataTypes.getBytes());
                dataType.setContent(inputStream);
                registry.put(resourcePath + "dataType" , dataType);
            }

            if(!registry.resourceExists(resourcePath + "mustBePresent")) {
                Resource mustBePresent = registry.newResource();
                String mustBePresents =
                        "true\n" +
                        "false";
                InputStream inputStream = new ByteArrayInputStream(mustBePresents.getBytes());
                mustBePresent.setContent(inputStream);
                registry.put(resourcePath + "mustBePresent" , mustBePresent);
            }

            if(!registry.resourceExists(resourcePath + "ruleEffect")) {
                Resource ruleEffect = registry.newResource();
                String ruleEffects =
                        "Permit\n" +
                        "Deny";
                InputStream inputStream = new ByteArrayInputStream(ruleEffects.getBytes());
                ruleEffect.setContent(inputStream);
                registry.put(resourcePath + "ruleEffect" , ruleEffect);
            }
            
        } catch (RegistryException e) {
            log.error("Error while adding entitlement policy resource", e);
            throw new IdentityException("Error while adding entitlement policy resource", e);
        }
    }
}
