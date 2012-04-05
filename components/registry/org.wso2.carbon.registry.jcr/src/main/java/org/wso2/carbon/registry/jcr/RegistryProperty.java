/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


package org.wso2.carbon.registry.jcr;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.CollectionImpl;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.jcr.nodetype.RegistryPropertyDefinition;
import org.wso2.carbon.registry.jcr.util.RegistryJCRItemOperationUtil;
import org.wso2.carbon.registry.jcr.util.RegistryJCRSpecificStandardLoderUtil;

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.version.VersionException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Calendar;


public class RegistryProperty implements Property {

    RegistryValue value;
    Value[] values;
    String[] stringMultipleVals;
    private String name;
    private CollectionImpl collectionP;  //a collection which has the required property
    private RegistrySession session;
    private boolean isResource = false;
    private Resource collectionR = null;
    private static Log log = LogFactory.getLog(RegistryNode.class);


    public RegistryProperty(RegistryNode node, String name) {

        this.name = name;
    }

//    public RegistryProperty(CollectionImpl aProp, RegistrySession session, String name) {
////        collectionP = aProp;
////        this.session = session;
////        this.name = name;
//
//    }

    public RegistryProperty(CollectionImpl aProp, RegistrySession session, String name, String value) {
        collectionP = aProp;
        this.session = session;
        this.name = name;
        setQValue(value);

    }

    public RegistryProperty(CollectionImpl aProp, RegistrySession session, String name, String[] values) {
        collectionP = aProp;
        this.session = session;
        this.name = name;
        setQValue(values);
    }

    public RegistryProperty(CollectionImpl aProp, RegistrySession session, String name, Node node) {
        collectionP = aProp;
        this.session = session;
        this.name = name;
        setQValue(node);
    }

       public RegistryProperty(CollectionImpl aProp, RegistrySession session, String name, Binary binary) {
        collectionP = aProp;
        this.session = session;
        this.name = name;
        setQValue(binary);
    }

//    public RegistryProperty(Resource aProp, RegistrySession session, String name) {
////        isResource = true;
////        collectionR = aProp;
////        this.session = session;
////        this.name = name;
//    }

    public RegistryProperty(Resource aProp, RegistrySession session, String name, BigDecimal bg) {
        isResource = true;
        collectionR = aProp;
        this.session = session;
        this.name = name;
        setQValue(bg);
    }

    public RegistryProperty(Resource aProp, RegistrySession session, String name, Calendar calendar) {
        isResource = true;
        collectionR = aProp;
        this.session = session;
        this.name = name;
        setQValue(calendar);
    }

    public RegistryProperty(Resource aProp, RegistrySession session, String name, InputStream inputStream) {
        isResource = true;
        collectionR = aProp;
        this.session = session;
        this.name = name;
        setQValue(inputStream);
    }

    public RegistryProperty(Resource aProp, RegistrySession session, String name, Value val) {
        isResource = true;
        collectionR = aProp;
        this.session = session;
        this.name = name;
        setQValue(val);
    }

    public RegistryProperty(Resource aProp, RegistrySession session, String name, Value[] vals) {
        isResource = true;
        collectionR = aProp;
        this.session = session;
        this.name = name;
        setQValue(vals);
    }

    public RegistryProperty(Resource aProp, RegistrySession session, String name, boolean val) {
        isResource = true;
        collectionR = aProp;
        this.session = session;
        this.name = name;
        setQValue(val);
    }


    public RegistryProperty(Resource aProp, RegistrySession session, String name, double val) {
        isResource = true;
        collectionR = aProp;
        this.session = session;
        this.name = name;
        setQValue(val);
    }


    public RegistryProperty(Resource aProp, RegistrySession session, String name, long val) {
        isResource = true;
        collectionR = aProp;
        this.session = session;
        this.name = name;
        setQValue(val);
    }

    public CollectionImpl getPropColl() {    //mine

        if (isResource) {
            return null;
        } else
            return collectionP;
    }

    private void validatePropertyModifyPrivilege() throws RepositoryException {
        // This is a already set property , so setValue is a modification
        if (RegistryJCRSpecificStandardLoderUtil.isSessionReadOnly(session.getUserID())) {
            throw new AccessDeniedException("A read only session must not be allowed to modify a property value");
        }
    }

    public void setValue(Value value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        validatePropertyModifyPrivilege();
        if (value != null) {
            this.value = (RegistryValue) value;
        } else {
            remove();
        }
    }

    public void setQValue(Value value) {
        if (value != null) {
            this.value = (RegistryValue) value;
        } else {
            try {
                remove();
            } catch (RepositoryException e) {
                log.error("Error occur while removing the value ");
            }
        }

    }

    public void setValue(Value[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        validatePropertyModifyPrivilege();

        if (values != null) {

            this.values = Arrays.copyOf(values, values.length);

        } else {

            remove();

        }

    }

    public void setQValue(Value[] values) {
        if (values != null) {
            this.values = Arrays.copyOf(values, values.length);
        } else {
            try {
                remove();
            } catch (RepositoryException e) {
                log.error("Error occur while removing the value ");
            }
        }
    }

    public void setValue(String s) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        validatePropertyModifyPrivilege();
        value = new RegistryValue(s);

    }

    private void setQValue(String s) {
        value = new RegistryValue(s);
    }

    public void setValue(String[] strings) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        validatePropertyModifyPrivilege();

        stringMultipleVals = strings.clone();
        values = new RegistryValue[strings.length];
        int i = 0;
        for (String s : strings) {
            values[i] = new RegistryValue(s);
            i++;
        }
    }

    private void setQValue(String[] strings) {
        stringMultipleVals = strings.clone();
        values = new RegistryValue[strings.length];
        int i = 0;
        for (String s : strings) {
            values[i] = new RegistryValue(s);
            i++;
        }
    }


    public void setValue(InputStream inputStream) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        validatePropertyModifyPrivilege();
        value = new RegistryValue(inputStream);
    }

    private void setQValue(InputStream inputStream) {
        value = new RegistryValue(inputStream);
    }


    public void setValue(Binary binary) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        validatePropertyModifyPrivilege();

        value = new RegistryValue(binary);
    }

    private void setQValue(Binary binary) {
        value = new RegistryValue(binary);
    }

    public void setValue(long l) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        validatePropertyModifyPrivilege();
        value = new RegistryValue(l);
    }

    private void setQValue(long l) {
        value = new RegistryValue(l);
    }

    public void setValue(double v) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        validatePropertyModifyPrivilege();
        value = new RegistryValue(v);
    }

    private void setQValue(double v) {
        value = new RegistryValue(v);
    }

    public void setValue(BigDecimal bigDecimal) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        validatePropertyModifyPrivilege();

        value = new RegistryValue(bigDecimal);

    }

    private void setQValue(BigDecimal bigDecimal) {
        value = new RegistryValue(bigDecimal);

    }

    public void setValue(Calendar calendar) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        validatePropertyModifyPrivilege();

        if (calendar != null) {
            value = new RegistryValue(calendar);
        }
    }

    private void setQValue(Calendar calendar) {

        if (calendar != null) {
            value = new RegistryValue(calendar);
        }
    }

    public void setValue(boolean b) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        validatePropertyModifyPrivilege();

        value = new RegistryValue(b);
    }

    private void setQValue(boolean b) {
        value = new RegistryValue(b);
    }

    public void setValue(Node node) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        validatePropertyModifyPrivilege();

        value = new RegistryValue(node);

    }

    private void setQValue(Node node) {
        value = new RegistryValue(node);
    }


    public Value getValue() throws ValueFormatException, RepositoryException {
        return value;
    }

    public Value[] getValues() throws ValueFormatException, RepositoryException {

        if (values != null) {
            return Arrays.copyOf(values, values.length);
        } else {
            throw new ValueFormatException("Invalid operation getValues for a single valued property");
        }
    }

    public String getString() throws ValueFormatException, RepositoryException {
        if (value != null) {
            return value.getString();
        } else {
            return "";
        }
    }


    public InputStream getStream() throws ValueFormatException, RepositoryException {


        if (value != null) {

            return value.getStream();
        } else {
            return new ByteArrayInputStream(new byte[]{12, 12, 22});
        }

    }

    public Binary getBinary() throws ValueFormatException, RepositoryException {

        if (value != null) {
            return value.getBinary();
        } else {
            return null;
        }

    }

    public long getLong() throws ValueFormatException, RepositoryException {

        if (value != null) {
            return value.getLong();
        } else {
            return 0;
        }
    }

    public double getDouble() throws ValueFormatException, RepositoryException {

        if (value != null) {
            return value.getDouble();
        } else {
            return 0.0;
        }
    }

    public BigDecimal getDecimal() throws ValueFormatException, RepositoryException {
        if (value != null)

            return value.getDecimal();
        else
            return new BigDecimal(0);
    }

    public Calendar getDate() throws ValueFormatException, RepositoryException {
        if (value != null)


            return value.getDate();
        else
            return null;

    }

    public boolean getBoolean() throws ValueFormatException, RepositoryException {
        if (value != null) {
            return value.getBoolean();
        } else
            return false;
    }

    public Node getNode() throws ItemNotFoundException, ValueFormatException, RepositoryException {

        if (value != null) {
            return value.getNode();
        } else {
            return null;
        }

    }

    public Property getProperty() throws ItemNotFoundException, ValueFormatException, RepositoryException {
        return this;
    }

    public long getLength() throws ValueFormatException, RepositoryException {
        long length = 0;

        if (value != null) {

            length = value.toString().length();

        }

        return length;

    }

    public long[] getLengths() throws ValueFormatException, RepositoryException {

        long[] lengths = new long[values.length];     //here only considers about strings,not binaries and all dat.

        for (int i = 0; i < values.length; i++) {

            lengths[i] = values[i].toString().length();
        }


        return lengths;
    }

    public PropertyDefinition getDefinition() throws RepositoryException {


        return new RegistryPropertyDefinition();
    }

    public int getType() throws RepositoryException {
        return value.getType();
    }

    public boolean isMultiple() throws RepositoryException {
        boolean isMul = false;

        if (value != null) {
            isMul = false;
        } else if (values != null) {
            isMul = true;
        }
        return isMul;
    }

    public String getPath() throws RepositoryException {     //as we cant ask for the path of a property in greg,we do this
        String path = "";
        if ((isResource) && (collectionR != null)) {
            path = collectionR.getPath();
        } else if (!isResource) {
            path = collectionP.getPath() + "/" + getName();
        }
        return path;
    }

    public String getName() throws RepositoryException {
        return name;
    }

    public Item getAncestor(int i) throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        return session.getItem(RegistryJCRItemOperationUtil
                .getAncestorPathAtGivenDepth(getPath(), i));
    }

    public Node getParent() throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        String parent = "";

        if (isResource) {

            parent = collectionR.getParentPath();
        } else {
            parent = collectionP.getParentPath();
        }

        RegistryNode par = new RegistryNode(parent, session);
//        par.resource.setPath(parent);

        return par;

    }

    public int getDepth() throws RepositoryException {
        if (getPath().equals("/")) {
            return 0;
        } else {
            return getPath().split("/").length - 1;
        }

    }

    public RegistrySession getSession() throws RepositoryException {

        return session;
    }

    public boolean isNode() {
        boolean isNode = false;

        if (value != null) {
            Object propType = value.getKey();

            if (propType instanceof Node) {
                isNode = true;
            }
        }
        return isNode;
    }

    public boolean isNew() {
        boolean isNew = false;

        if ((value != null) && (stringMultipleVals != null)) {
            if (values == null) {
                isNew = true;
            }
        }
        return isNew;
    }

    public boolean isModified() {

        boolean isModified = true;


        if (!isResource) {
            isModified = collectionP.isPropertiesModified();
        }

        return isModified;
    }

    public boolean isSame(Item item) throws RepositoryException {
        boolean isSame = false;
        if (name.equals(item.getName())) {
            isSame = true;
        }

        return isSame;
    }

    public void accept(ItemVisitor itemVisitor) throws RepositoryException { //TODO
    }

    public void save() throws AccessDeniedException, ItemExistsException, ConstraintViolationException, InvalidItemStateException, ReferentialIntegrityException, VersionException, LockException, NoSuchNodeTypeException, RepositoryException {//TODO
    }

    public void refresh(boolean b) throws InvalidItemStateException, RepositoryException { //TODO
    }

    public void remove() throws VersionException, LockException, ConstraintViolationException, AccessDeniedException, RepositoryException {

        RegistryJCRItemOperationUtil.validateReadOnlyItemOpr(session);

        try {
            if (isResource) {
                collectionR.removeProperty(this.name);
                session.getUserRegistry().put(getPath(), collectionR);
            } else {
                collectionP.removeProperty(this.name);
                session.getUserRegistry().put(getPath(), collectionP);
            }
        } catch (RegistryException e) {
            String msg = "failed to remove the property " + this;
            log.debug(msg);
            throw new RepositoryException(msg, e);
        }

    }
}
