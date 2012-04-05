/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.governance.api.util;

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.utils.component.xml.config.ManagementPermission;

import javax.xml.namespace.QName;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Configuration of a Governance Artifact
 */
public class GovernanceArtifactConfiguration {

    private String mediaType;
    private int iconSet = 0;
    private String key;
    private String singularLabel;
    private String pluralLabel;
    private String pathExpression;
    private OMElement uiConfigurations;
    private List<Association> relationships = new LinkedList<Association>();
    private OMElement contentDefinition;
    private List<ManagementPermission> uiPermissions = new LinkedList<ManagementPermission>();
    private UIListConfiguration[] listConfigurations;
    private String artifactNameAttribute = "overview_name";
    private String artifactNamespaceAttribute = "overview_namespace";
    private String artifactElementRoot = "metadata";
    private String artifactElementNamespace = "http://www.wso2.org/governance/metadata";

    /**
     * Method to obtain the media type.
     *
     * @return the media type.
     */
    public String getMediaType() {
        return mediaType;
    }

    /**
     * Method to set the media type.
     *
     * @param mediaType the media type.
     */
    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    /**
     * Method to obtain the icon set to be used.
     *
     * @return the icon set to be used.
     */
    public int getIconSet() {
        return iconSet;
    }

    /**
     * Method to set the icon set to be used.
     *
     * @param iconSet the icon set to be used.
     */
    public void setIconSet(int iconSet) {
        this.iconSet = iconSet;
    }

    /**
     * Method to obtain the attribute that specifies the artifact name.
     *
     * @return the attribute that specifies the artifact name.
     */
    public String getArtifactNameAttribute() {
        return artifactNameAttribute;
    }

    /**
     * Method to set the attribute that specifies the artifact name.
     *
     * @param artifactNameAttribute the attribute that specifies the artifact name.
     */
    public void setArtifactNameAttribute(String artifactNameAttribute) {
        this.artifactNameAttribute = artifactNameAttribute;
    }

    /**
     * Method to obtain the attribute that specifies the artifact namespace.
     *
     * @return the attribute that specifies the artifact namespace.
     */
    public String getArtifactNamespaceAttribute() {
        return artifactNamespaceAttribute;
    }

    /**
     * Method to set the attribute that specifies the artifact namespace.
     *
     * @param artifactNamespaceAttribute the attribute that specifies the artifact namespace.
     */
    public void setArtifactNamespaceAttribute(String artifactNamespaceAttribute) {
        this.artifactNamespaceAttribute = artifactNamespaceAttribute;
    }

    /**
     * Method to obtain the name of the root element of an XML governance artifact.
     *
     * @return the name of the root element of an XML governance artifact.
     */
    @SuppressWarnings("unused")
    public String getArtifactElementRoot() {
        return artifactElementRoot;
    }

    /**
     * Method to set the name of the root element of an XML governance artifact.
     *
     * @param artifactElementRoot the name of the root element of an XML governance artifact.
     */
    public void setArtifactElementRoot(String artifactElementRoot) {
        this.artifactElementRoot = artifactElementRoot;
    }

    /**
     * Method to obtain the namespace of the root element of an XML governance artifact.
     *
     * @return the namespace of the root element of an XML governance artifact.
     */
    public String getArtifactElementNamespace() {
        return artifactElementNamespace;
    }

    /**
     * Method to set the namespace of the root element of an XML governance artifact.
     *
     * @param artifactElementNamespace the namespace of the root element of an XML governance
     *                                 artifact.
     */
    public void setArtifactElementNamespace(String artifactElementNamespace) {
        this.artifactElementNamespace = artifactElementNamespace;
    }

    /**
     * Method to retrieve the relationship details.
     *
     * @return the relationship details.
     */
    public Association[] getRelationshipDefinitions() {
        return relationships.toArray(new Association[relationships.size()]);
    }

    /**
     * Method to set the relationship details.
     *
     * @param relationships the relationship details.
     */
    public void setRelationshipDefinitions(Association[] relationships) {
        this.relationships = Arrays.asList(relationships);
    }

    /**
     * Method to obtain the content definition element.
     *
     * @return the content definition element.
     */
    @SuppressWarnings("unused")
    public OMElement getContentDefinition() {
        return contentDefinition;
    }

    /**
     * Method to set the content definition element.
     *
     * @param contentDefinition the content definition element.
     */
    public void setContentDefinition(OMElement contentDefinition) {
        this.contentDefinition = contentDefinition;
    }

    /**
     * Method to obtain the UI configurations element.
     *
     * @return the UI configurations element.
     */
    @SuppressWarnings("unused")
    public OMElement getUIConfigurations() {
        return uiConfigurations;
    }

    /**
     * Method to set the UI configurations element.
     *
     * @param uiConfigurations the UI configurations element.
     */
    public void setUIConfigurations(OMElement uiConfigurations) {
        this.uiConfigurations = uiConfigurations;
    }

    /**
     * Method to obtain the UI permissions element.
     *
     * @return the UI permissions element.
     */
    @SuppressWarnings("unused")
    public ManagementPermission[] getUIPermissions() {
        return uiPermissions.toArray(new ManagementPermission[uiPermissions.size()]);
    }

    /**
     * Method to set the UI permissions element.
     *
     * @param uiPermissions the UI permissions element.
     */
    public void setUIPermissions(ManagementPermission[] uiPermissions) {
        this.uiPermissions = Arrays.asList(uiPermissions);
    }

    /**
     * Method to set the configuration for the artifact list UI.
     *
     * @param listConfigurations the configuration for the artifact list UI.
     */
    public void setUIListConfigurations(OMElement listConfigurations) {
        Iterator iterator =
                listConfigurations.getChildrenWithName(
                        new QName("column"));
        List<UIListConfiguration> configurations =
                new LinkedList<UIListConfiguration>();
        while (iterator.hasNext()) {
            OMElement configurationElement = (OMElement) iterator.next();
            OMElement dataElement =
                    configurationElement.getFirstChildWithName(
                            new QName("data"));
            if (dataElement != null) {
                String name = configurationElement.getAttributeValue(new QName("name"));
                String key = dataElement.getAttributeValue(new QName("value"));
                String type = dataElement.getAttributeValue(new QName("type"));
                String expression = dataElement.getAttributeValue(new QName("href"));
                if (key != null && name != null) {
                    configurations.add(new UIListConfiguration(key, type, name, expression));
                }
            }

        }
        this.listConfigurations =
                configurations.toArray(new UIListConfiguration[configurations.size()]);
    }

    /**
     * Method to obtain the key that can be used to locate an artifact.
     *
     * @return the key that can be used to locate an artifact.
     */
    public String getKey() {
        return key;
    }

    /**
     * Method to set the key that can be used to locate an artifact.
     *
     * @param key the key that can be used to locate an artifact.
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Method to obtain the label used in singular representations of the artifact.
     *
     * @return the label used in singular representations of the artifact.
     */
    public String getSingularLabel() {
        return singularLabel;
    }

    /**
     * Method to set the label used in singular representations of the artifact.
     *
     * @param singularLabel the label used in singular representations of the artifact.
     */
    public void setSingularLabel(String singularLabel) {
        this.singularLabel = singularLabel;
    }

    /**
     * Method to obtain the label used in plural representations of the artifact.
     *
     * @return the label used in plural representations of the artifact.
     */
    public String getPluralLabel() {
        return pluralLabel;
    }

    /**
     * Method to set the label used in plural representations of the artifact.
     *
     * @param  pluralLabel the label used in plural representations of the artifact.
     */
    public void setPluralLabel(String pluralLabel) {
        this.pluralLabel = pluralLabel;
    }

    /**
     * Method to obtain the expression that can be used to compute where to store the artifact.
     *
     * @return the expression that can be used to compute where to store the artifact.
     */
    public String getPathExpression() {
        return pathExpression;
    }

    /**
     * Method to set the expression that can be used to compute where to store the artifact.
     *
     * @param pathExpression the expression that can be used to compute where to store the artifact.
     */
    public void setPathExpression(String pathExpression) {
        this.pathExpression = pathExpression;
    }

    /**
     * Method to obtain the list of keys that will be used to generate the artifact list UI.
     *
     * @return the list of keys that will be used to generate the artifact list UI.
     */
    public String[] getKeysOnListUI() {
        String[] keysOnListUI = new String[listConfigurations.length];
        for (int i = 0; i < listConfigurations.length; i++) {
            keysOnListUI[i] = listConfigurations[i].getKey();
        }
        return keysOnListUI;
    }

    /**
     * Method to obtain the list of names that will be displayed on the artifact list UI.
     *
     * @return the list of names that will be displayed on the artifact list UI.
     */
    public String[] getNamesOnListUI() {
        String[] namesOnListUI = new String[listConfigurations.length];
        for (int i = 0; i < listConfigurations.length; i++) {
            namesOnListUI[i] = listConfigurations[i].getName();
        }
        return namesOnListUI;
    }

    /**
     * Method to obtain the list of types that will be used to populate the content on the artifact
     * list UI.
     *
     * @return the list of types that will be used to populate the content on the artifact list UI.
     */
    public String[] getTypesOnListUI() {
        String[] typesOnListUI = new String[listConfigurations.length];
        for (int i = 0; i < listConfigurations.length; i++) {
            typesOnListUI[i] = listConfigurations[i].getType();
        }
        return typesOnListUI;
    }

    /**
     * Method to obtain the list of expressions that will be used to populate the content on the
     * artifact list UI.
     *
     * @return the list of expressions that will be used to populate the content on the artifact
     *         list UI.
     */
    public String[] getExpressionsOnListUI() {
        String[] expressionsOnListUI = new String[listConfigurations.length];
        for (int i = 0; i < listConfigurations.length; i++) {
            expressionsOnListUI[i] = listConfigurations[i].getExpression();
        }
        return expressionsOnListUI;
    }

    private static class UIListConfiguration {

        private String key;
        private String type;
        private String name;
        private String expression;

        private UIListConfiguration(String key, String type, String name, String expression) {
            this.key = key;
            this.type = type;
            this.name = name;
            this.expression = expression;
        }

        public String getKey() {
            return key;
        }

        public String getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public String getExpression() {
            return expression;
        }
    }
}
