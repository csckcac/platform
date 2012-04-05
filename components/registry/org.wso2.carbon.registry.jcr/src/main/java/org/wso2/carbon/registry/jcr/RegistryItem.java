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

import org.wso2.carbon.registry.core.exceptions.RegistryException;

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.version.VersionException;

public class RegistryItem implements Item {

    RegistryNode node = null;
    RegistryProperty property = null;

    Object anItem;

    public RegistryItem(Object anItem) {
        this.anItem = anItem;
        validate(anItem);
    }

    private void validate(Object item) {

        if (item instanceof RegistryNode) {
            this.node = (RegistryNode) item;
        } else if (item instanceof RegistryProperty) {
            this.property = (RegistryProperty) item;
        }
    }

    public String getPath() throws RepositoryException {
        String path = null;
        if (isNode()) {
            path = node.getPath();
        } else {
            path = property.getPath();
        }

        return path;
    }

    public String getName() throws RepositoryException {
        if (isNode()) {
            return node.getName();
        } else {
            return property.getName();
        }
    }

    public Item getAncestor(int i) throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        if (isNode()) {
            return node.getAncestor(i);
        } else {
            return property.getAncestor(i);
        }
    }


    public Node getParent() throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        if (isNode()) return node.getParent();
        else
            return property.getParent();
    }

    public int getDepth() throws RepositoryException {

        if (isNode()) {
            return node.getDepth();
        } else {
            return property.getDepth();
        }
    }

    public Session getSession() throws RepositoryException {

        if (isNode()) {
            return node.getSession();
        } else {
            return property.getSession();
        }

    }

    public boolean isNode() {
        boolean isNode = false;

        if (anItem instanceof RegistryNode) {
            isNode = true;
        }

        return isNode;
    }

    public boolean isNew() {
        boolean isNew = false;

        if (isNode()) {

            isNew = node.isNew();

        }

        return isNew;
    }

    public boolean isModified() {

        boolean isModf = false;

        if (isNode()) {

            isModf = node.resource.isContentModified();
            return isModf;

        } else {
            if (property.getPropColl() != null) {
                isModf = property.getPropColl().isContentModified();
            }
        }

        return isModf;

    }

    public boolean isSame(Item item) throws RepositoryException {

        boolean isSame = false;
        if (anItem instanceof Item) {
            isSame = true;

        }
        return isSame;
    }

    public void accept(ItemVisitor itemVisitor) throws RepositoryException {   //TODO


    }

    public void save() throws AccessDeniedException, ItemExistsException, ConstraintViolationException, InvalidItemStateException, ReferentialIntegrityException, VersionException, LockException, NoSuchNodeTypeException, RepositoryException {  //TODO


    }

    public void refresh(boolean b) throws InvalidItemStateException, RepositoryException {  //TODO


    }

    public void remove() throws VersionException, LockException, ConstraintViolationException, AccessDeniedException, RepositoryException {

        try {

            if (isNode()) {

                node.getSession().getUserRegistry().delete(node.getPath());

            } else {
                property.getSession().getUserRegistry().delete(property.getPath());

            }

        } catch (RegistryException e) {

        }
    }
}
