/**********************************************************************
Copyright (c) 2005 Erik Bengtson and others. All rights reserved.
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

Contributors:
    ...
**********************************************************************/
package org.datanucleus;

import org.datanucleus.identity.OIDImpl;
import org.datanucleus.store.StoreManager;

/**
 * Constants with classes (class created to reduce overhead on calling Class.class *performance*)
 */
public class ClassConstants
{
    /** Loader for NucleusContext (represents the loader for "core"). */
    public static final ClassLoader NUCLEUS_CONTEXT_LOADER = NucleusContext.class.getClassLoader();

    /** org.datanucleus.ClassLoaderResolver **/
    public static final Class CLASS_LOADER_RESOLVER = ClassLoaderResolver.class;

    /** org.datanucleus.store.StoreManager **/
    public static final Class STORE_MANAGER = StoreManager.class;

    /** org.datanucleus.identity.OIDImpl **/
    public static final Class OID_IMPL = OIDImpl.class;
}