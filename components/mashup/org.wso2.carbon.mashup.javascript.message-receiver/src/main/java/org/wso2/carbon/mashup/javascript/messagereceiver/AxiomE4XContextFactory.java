/*
 * Copyright 2006,2007 WSO2, Inc. http://www.wso2.org
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
package org.wso2.carbon.mashup.javascript.messagereceiver;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.xml.XMLLib;

/**
 * This class is used to set the default E4X implementation to the WSO2's custom implementation.
 */
public class AxiomE4XContextFactory extends ContextFactory {

    static {
        // Initialize Global Factory with custom factory
        ContextFactory.initGlobal(new AxiomE4XContextFactory());
    }

    /**
     * This method is used to get the information of the availability of a particular feature within
     * the context.
     *
     * @param cx context being checked for a particular feature
     * @param featureIndex index of the feature that needs to be checked
     * @return <tt>true</tt> if the feature is available in the context
     */
    protected boolean hasFeature(Context cx, int featureIndex) {
        int version;
        switch (featureIndex) {
            case Context.FEATURE_NON_ECMA_GET_YEAR:
                /*
                * During the great date rewrite of 1.3, we tried to track the
                * evolving ECMA standard, which then had a definition of
                * getYear which always subtracted 1900.  Which we
                * implemented, not realizing that it was incompatible with
                * the old behavior...  now, rather than thrash the behavior
                * yet again, we've decided to leave it with the - 1900
                * behavior and point people to the getFullYear method.  But
                * we try to protect existing scripts that have specified a
                * version...
                */
                version = cx.getLanguageVersion();
                return (version == Context.VERSION_1_0
                        || version == Context.VERSION_1_1
                        || version == Context.VERSION_1_2);

            case Context.FEATURE_MEMBER_EXPR_AS_FUNCTION_NAME:
                return false;

            case Context.FEATURE_RESERVED_KEYWORD_AS_IDENTIFIER:
                return false;

            case Context.FEATURE_TO_STRING_AS_SOURCE:
                version = cx.getLanguageVersion();
                return version == Context.VERSION_1_2;

            case Context.FEATURE_PARENT_PROTO_PROPERTIES:
                return true;

            case Context.FEATURE_E4X:
                return true;

            case Context.FEATURE_DYNAMIC_SCOPE:
                return false;

            case Context.FEATURE_STRICT_VARS:
                return true;

            case Context.FEATURE_STRICT_EVAL:
                return true;

            case Context.FEATURE_LOCATION_INFORMATION_IN_ERROR:
                return true;

            case Context.FEATURE_STRICT_MODE:
                return true;

            case Context.FEATURE_WARNING_AS_ERROR:
                return false;

            case Context.FEATURE_ENHANCED_JAVA_ACCESS:
                return false;
        }
        // It is a bug to call the method with unknown featureIndex
        throw new IllegalArgumentException(String.valueOf(featureIndex));
    }

    /**
     * This methods is used to get the E4x implementation factory.
     * @return  the factory which is used 
     */
    protected XMLLib.Factory getE4xImplementationFactory() {
        return org.mozilla.javascript.xml.XMLLib.Factory.create(
                "org.wso2.javascript.xmlimpl.XMLLibImpl"
        );
    }

    // TODO for the moment lets keep this open. Do we want this in a carbon env?
    /*protected void onContextCreated(Context cx) {
        cx.setClassShutter(new ClassShutterImpl());
        super.onContextCreated(cx);
    }*/
}
