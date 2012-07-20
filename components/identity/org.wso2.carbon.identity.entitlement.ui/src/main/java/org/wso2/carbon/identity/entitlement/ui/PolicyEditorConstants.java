/*
*  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.identity.entitlement.ui;

/**
 * Policy editor related constants
 */
public class PolicyEditorConstants {


    public static final String ATTRIBUTE_SEPARATOR = ",";

    public static final String TARGET_ELEMENT = "Target";

    public static final String ANY_OF_ELEMENT = "AnyOf"; 

    public static final String ALL_OF_ELEMENT = "AllOf";

    public static final String COMBINE_FUNCTION_AND = "AND";

    public static final String COMBINE_FUNCTION_OR = "OR";

    public static final String COMBINE_FUNCTION_END = "END";

    public static final String MATCH_ELEMENT = "Match";

    public static final String  MATCH_ID = "MatchId";

    public static final String  ATTRIBUTE_ID = "AttributeId";

    public static final String  CATEGORY = "Category";

    public static final String  DATA_TYPE = "DataType";

    public static final String  ISSUER = "Issuer";

    public static final String  MUST_BE_PRESENT = "MustBePresent";

    public static final String ATTRIBUTE_DESIGNATOR = "AttributeDesignator";

    public static final String FUNCTION_GREATER_EQUAL_AND_LESS_EQUAL = "greater-than-or-equal-and-less-than-or-equal";

    public static final String FUNCTION_GREATER_AND_LESS_EQUAL= "greater-than-and-less-than-or-equal";

    public static final String FUNCTION_GREATER_EQUAL_AND_LESS = "greater-than-or-equal-and-less-than";

    public static final String FUNCTION_GREATER_AND_LESS = "greater-than-and-less-than";

    public static final String FUNCTION_GREATER = "greater-than";

    public static final String FUNCTION_GREATER_EQUAL = "greater-than-or-equal";

    public static final String FUNCTION_LESS = "less-than";

    public static final String FUNCTION_AT_LEAST_ONE = "at-least-one-member-of";

    public static final String FUNCTION_IS_IN = "is-in";

    public static final String FUNCTION_SET_EQUALS = "set-equals";

    public static final String FUNCTION_LESS_EQUAL = "less-than-or-equal";

    public static final String FUNCTION_ADD = "add";

    public static final String FUNCTION_MULTIPLY = "multiply";

    public static final String FUNCTION_SUBTRACT = "sub";

    public static final String FUNCTION_DIVIDE = "divide";

    public static final String FUNCTION_EQUAL = "equal";

    public static final String PRE_FUNCTION_IS = "is";

    public static final String PRE_FUNCTION_ARE = "are";

    public static final String RULE_EFFECT_PERMIT = "Permit";

    public static final String RULE_EFFECT_DENY = "Deny";

    public static final String  DAY_TIME_DURATION  = "http://www.w3.org/2001/XMLSchema#dayTimeDuration";
    
    public static final String  YEAR_MONTH_DURATION  = "http://www.w3.org/2001/XMLSchema#yearMonthDuration";

    public static final String RULE_COMBINING_DENY_OVERRIDE = "deny-overrides";
    
    public static final String RULE_COMBINING_PERMIT_OVERRIDE = "permit-overrides";

    public static final String RULE_COMBINING_FIRST_APPLICABLE = "first-applicable";
    
    public static final String RULE_COMBINING_ORDER_PERMIT_OVERRIDE = "ordered-permit-overrides";

    public static final String RULE_COMBINING_ORDER_DENY_OVERRIDE = "ordered-deny-overrides";
    
    public static final String RULE_COMBINING_DENY_UNLESS_PERMIT = "deny-unless-permit";

    public static final String RULE_COMBINING_PERMIT_UNLESS_DENY = "permit-unless-deny";

    public static final String RULE_ALGORITHM_IDENTIFIER_1 = "urn:oasis:names:tc:xacml:1.0:" +
                                                                        "rule-combining-algorithm:";

    public static final String RULE_ALGORITHM_IDENTIFIER_3 = "urn:oasis:names:tc:xacml:3.0:" +
                                                                        "rule-combining-algorithm:";


}
