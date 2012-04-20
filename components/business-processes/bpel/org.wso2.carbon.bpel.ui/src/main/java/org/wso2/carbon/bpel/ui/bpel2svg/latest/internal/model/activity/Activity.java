/*
   Copyright 2010 Gregor Latuske

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
*/
package org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.activity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents an activity of a process model.
 *
 * @author Gregor Latuske
 */
public abstract class Activity implements Serializable {

    /**
     * Serial version UID
     */
    private static final long serialVersionUID = -495088889974254969L;

    /**
     * The name of the activity.
     */
    private final String name;

    /**
     * The type of the activity.
     */
    private final String type;

    /**
     * The parent activity of this activity.
     */
    private final ActivityComplex parent;

    /**
     * The root of all activities.
     */
    private final ActivityRoot root;

    /**
     * The depth of the activity in the model.
     */
    private final int depth;

    /**
     * Constructor of Activity.
     *
     * @param name   The name of the activity.
     * @param type   The type of the activity.
     * @param parent The parent activity of this activity.
     * @param root   The root of all activities.
     */
    public Activity(String name, String type, ActivityComplex parent, ActivityRoot root) {
        this.name = name;
        this.type = type;
        this.parent = parent;

        // Add to parent & set depth
        if (this.parent != null) {
            this.parent.getChildren().add(this);
            this.depth = this.parent.getDepth() + 1;
        } else {
            this.depth = 0;
        }

        // Set root
        if (this instanceof ActivityRoot) {
            this.root = (ActivityRoot) this;
        } else {
            this.root = root;

            // Add type
            if (this.type != null && !this.type.isEmpty()) {
                this.root.getActivityTypes().add(this.type);
            }

            // Add name
            if (this.name != null && !this.name.isEmpty()) {
                this.root.getActivityNames().add(this.name);
            }

            // Set max depth
            if (this.depth > this.root.getMaxDepth()) {
                this.root.setMaxDepth(this.depth);
            }
        }
    }

    /**
     * Returns the value of name.
     *
     * @return The value of name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the value of type.
     *
     * @return The value of type.
     */
    public String getType() {
        return this.type;
    }

    /**
     * Returns the value of parent.
     *
     * @return The value of parent.
     */
    public ActivityComplex getParent() {
        return this.parent;
    }

    /**
     * Returns the value of root.
     *
     * @return The value of root.
     */
    public ActivityRoot getRoot() {
        return this.root;
    }

    /**
     * Returns the value of depth.
     *
     * @return The value of depth.
     */
    public int getDepth() {
        return this.depth;
    }

    /**
     * Returns the activity this activity is the source of the edge to them.
     *
     * @return The activity this activity is the source of the edge to them.
     */
    public List<Activity> getTargets() {
        List<Activity> targets = new ArrayList<Activity>();

        for (Link link : this.root.getLinks()) {
            if (equals(link.getSource()) && link.getTarget() != null) {
                targets.add(link.getTarget());
            }
        }

        return targets;
    }

    /**
     * Returns the activity this activity is the target of the edge from them.
     *
     * @return The activity this activity is the target of the edge from them.
     */
    public List<Activity> getSources() {
        List<Activity> sources = new ArrayList<Activity>();

        for (Link link : this.root.getLinks()) {
            if (equals(link.getTarget()) && link.getSource() != null) {
                sources.add(link.getSource());
            }
        }

        return sources;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Activity)) {
            return false;
        } else if (getName() == null || getName().isEmpty() || getRoot() == null) {
            return super.equals(obj);
        }

        Activity activity = (Activity) obj;

        return getType().equals(activity.getType()) && getRoot().equals(activity.getRoot())
                && getName().equals(activity.getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        if (getName() == null || getName().isEmpty() || getRoot() == null) {
            return super.hashCode();
        }

        return getType().hashCode() * 7 + getName().hashCode() * 11 + getRoot().hashCode() * 13;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Type: " + getType() + ", Name: " + getName() + ", Root (" + getRoot().getName() + ")";
    }

}
