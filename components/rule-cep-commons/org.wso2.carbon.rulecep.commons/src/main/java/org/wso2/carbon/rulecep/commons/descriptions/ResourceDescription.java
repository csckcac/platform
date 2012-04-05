/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.rulecep.commons.descriptions;

import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.description.java2wsdl.TypeTable;
import org.jaxen.BaseXPath;
import org.wso2.carbon.rulecep.commons.utils.XPathCache;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Resource's Mata-data - represents input, output and any other resources used in the rule component
 */
public class ResourceDescription {

    /* The identifier of the Resource . This is a mandatory attribute.Any Resource cannot exists without name */
    private String name;

    /* The static value of the Resource. */
    private Object value;

    /* The expression to which is used to extractPayload data from XML using XPath.This provide dynamic nature */
    private BaseXPath expression;

    /* This key attribute is used to pick data from Context */
    private String key;

    /* The type of this Resource's value */
    private String type;

    /* The childResources that should go to custom java object.This provides description of the property
    and the value will be computed at run time and set to the object */
    private final List<ResourceDescription> childResources = new ArrayList<ResourceDescription>();

    /**
     * Providing Element QName mapping for this resource
     */
    private QName elementQName;

    /**
     * Default name spaces when a default expression is used. *
     */
    private Collection<OMNamespace> nameSpaces = new ArrayList<OMNamespace>();

    /**
     * Contains schemas related to this resource *
     */
    private TypeTable typeTable;

    /**
     * Class loader to be used to load the any classes in this resource
     */
    private ClassLoader resourceClassLoader;

    /**
     * XPath factory to be used when deriving XPaths from this resource*
     */
    private XPathFactory xPathFactory;
    /**
     * Cache for the xpath instances created based on this resource *
     */
    private XPathCache xPathCache;     // todo to be removed

    private final Collection<String> childrenNames = new ArrayList<String>();

    private final Collection<Class> childrenClasses = new ArrayList<Class>();

    private QName parentElementQName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public BaseXPath getExpression() {
        return expression;
    }

    public void setExpression(BaseXPath expression) {
        this.expression = expression;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * Sets all childResources that should be gone to the custom java object.
     * This is used only if type is pojo, o.w this will be ignored.
     *
     * @param description the Resource
     */
    public void addChildResource(ResourceDescription description) {
        childResources.add(description);
        String name = description.getName();
        if (name != null) {
            childrenNames.add(name);
        }
    }

    /**
     * This childResources is only available if the type is CUSTOM
     *
     * @return Returns the child Resources iterator
     */
    public Collection<ResourceDescription> getChildResources() {
        return childResources;
    }

    public boolean hasChildren() {
        return !childResources.isEmpty();
    }

    public void setElementQName(QName elementQName) {
        this.elementQName = elementQName;
    }

    public ClassLoader getResourceClassLoader() {
        return resourceClassLoader;
    }

    public void setResourceClassLoader(ClassLoader resourceClassLoader) {
        this.resourceClassLoader = resourceClassLoader;
    }

    public QName getElementQName() {
        return elementQName;
    }

    public Collection<String> getChildrenNames() {
        return childrenNames;
    }

    public TypeTable getTypeTable() {
        return typeTable;
    }

    public void setTypeTable(TypeTable typeTable) {
        this.typeTable = typeTable;
    }

    public void addNameSpaces(Collection<OMNamespace> omNamespaces) {
        nameSpaces.addAll(omNamespaces);
    }

    public Collection<OMNamespace> getNameSpaces() {
        return nameSpaces;
    }

    @SuppressWarnings("unused")
    public XPathFactory getXPathFactory() {
        return xPathFactory;
    }

    public void setXPathFactory(XPathFactory xPathFactory) {
        this.xPathFactory = xPathFactory;
    }

    public XPathCache getXPathCache() {
        if (xPathCache == null) {
            xPathCache = new XPathCache(xPathFactory, nameSpaces);
        }
        return xPathCache;
    }

    public void addChildrenClasses(Collection<Class> classes) {
        childrenClasses.clear();
        childrenClasses.addAll(classes);
    }

    public Collection<Class> getChildrenClasses() {
        return childrenClasses;
    }

    public QName getParentElementQName() {
        return parentElementQName;
    }

    public void setParentElementQName(QName parentElementQName) {
        this.parentElementQName = parentElementQName;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[ Resource name :").append(name);
        sb.append("type : ").append(type).append(" ]");
        return sb.toString();
    }
}
