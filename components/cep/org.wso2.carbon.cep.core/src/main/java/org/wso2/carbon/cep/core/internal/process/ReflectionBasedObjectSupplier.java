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

package org.wso2.carbon.cep.core.internal.process;

import org.apache.axis2.engine.ObjectSupplier;
import org.apache.axis2.AxisFault;

public class ReflectionBasedObjectSupplier implements ObjectSupplier {

    public Object getObject(Class aClass) throws AxisFault {

        Object newInstance = null;
        try {
            newInstance = aClass.newInstance();
            return newInstance;
        } catch (InstantiationException e) {
            throw new AxisFault("Can not instantiate object or the type " + aClass.getName(), e);
        } catch (IllegalAccessException e) {
            throw new AxisFault("Can not access object or the type " + aClass.getName(), e);
        }
    }
}
