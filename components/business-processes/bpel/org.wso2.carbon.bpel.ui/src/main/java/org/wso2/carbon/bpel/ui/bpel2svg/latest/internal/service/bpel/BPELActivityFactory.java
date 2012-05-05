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
package org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.service.bpel;

import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.activity.*;

/**
 * Factory-Class of the different {@link Activity}s.
 */
public final class BPELActivityFactory {
    private BPELActivityFactory() {
    }

    /**
     * Creates on the basis of the type an according {@link Activity}.
     *
     * @param name   The name of the activity.
     * @param type   The type of the activity.
     * @param parent The parent activity of this activity.
     * @param root   The root of all activities.
     * @return The created {@link Activity} or <code>null</code>, if the type does not match to an existing
     *         one.
     */
    public static Activity createActivity(String type, String name, ActivityComplex parent, ActivityRoot root) {
        Activity activity = null;

        for (BPELActivityType bpelType : BPELActivityType.values()) {
            if (bpelType.getName().equalsIgnoreCase(type)) {

                if (bpelType.getType().equals(ActivitySimple.class)) {
                    activity = new ActivitySimple(name, bpelType.getName(), parent, root);
                } else if (bpelType.getType().equals(ActivityFlow.class)) {
                    activity = new ActivityFlow(name, bpelType.getName(), parent, root);
                } else if (bpelType.getType().equals(ActivitySequence.class)) {
                    activity = new ActivitySequence(name, bpelType.getName(), parent, root);
                } else if (bpelType.getType().equals(ActivityChoice.class)) {
                    activity = new ActivityChoice(name, bpelType.getName(), parent, root);
                }
            }
        }

        return activity;
    }
}
