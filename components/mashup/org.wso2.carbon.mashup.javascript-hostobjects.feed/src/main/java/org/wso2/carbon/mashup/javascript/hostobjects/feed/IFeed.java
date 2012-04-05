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
package org.wso2.carbon.mashup.javascript.hostobjects.feed;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.NativeArray;
import org.wso2.carbon.CarbonException;


public interface IFeed {

    public Scriptable jsConstructor();

    void jsSet_author(Object author);

    String jsGet_author();

    void jsSet_updated(Object updated) throws CarbonException;

    String jsGet_updated();

    void jsSet_category(Object category);

    String jsGet_category();

    void jsSet_contributor(Object contributor);

    String jsGet_contributor();

    void jsSet_link(Object link);

    NativeArray jsGet_link();

    void jsSet_title(Object title);

    String jsGet_title();

    /**
     * Returns the E4X XML contents of this AtomFeed object
     *
     * @return E4X XML
     */
    public Scriptable jsGet_XML() throws CarbonException;

}
