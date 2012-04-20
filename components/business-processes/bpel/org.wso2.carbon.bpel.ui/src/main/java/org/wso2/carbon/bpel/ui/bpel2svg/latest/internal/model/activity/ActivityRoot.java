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

import java.util.HashSet;
import java.util.Set;

/**
 * This class represents the root activity that contains all activities with no parent activity.
 *
 * @author Gregor Latuske
 */
public class ActivityRoot
        extends ActivityChoice {

    /**
     * Serial version UID
     */
    private static final long serialVersionUID = -5757720168043930991L;

    /**
     * The set of links assigned to all flow activities.
     */
    private final Set<Link> links;

    /**
     * A set of the used activity types.
     */
    private final Set<String> activityTypes;

    /**
     * A set of the activity names.
     */
    private final Set<String> activityNames;

    /**
     * The maximum depth of child activities.
     */
    private int maxDepth;

    /**
     * Constructor of RootActivity.
     *
     * @param name The name of the activity.
     */
    public ActivityRoot(String name) {
        super(name, "root", null, null);

        this.links = new HashSet<Link>();
        this.activityTypes = new HashSet<String>();
        this.activityNames = new HashSet<String>();
        this.maxDepth = 0;
    }

    /**
     * Returns the value of links.
     *
     * @return The value of links.
     */
    public Set<Link> getLinks() {
        return this.links;
    }

    /**
     * Returns the value of activityTypes.
     *
     * @return The value of activityTypes.
     */
    public Set<String> getActivityTypes() {
        return this.activityTypes;
    }

    /**
     * Returns the value of activityNames.
     *
     * @return The value of activityNames.
     */
    public Set<String> getActivityNames() {
        return this.activityNames;
    }

    /**
     * Returns the value of maxDepth.
     *
     * @return The value of maxDepth.
     */
    public int getMaxDepth() {
        return this.maxDepth;
    }

    /**
     * Sets the value of maxDepth to maxDepth.
     *
     * @param maxDepth The new value of maxDepth.
     */
    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ActivityRoot)) {
            return false;
        } else if (getName() == null || getName().isEmpty()) {
            return super.equals(obj);
        }

        ActivityRoot root = (ActivityRoot) obj;

        return getName().equals(root.getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        if (getName() == null || getName().isEmpty()) {
            return super.hashCode();
        }

        return getName().hashCode() * 11;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Name: " + getName();
    }

}
