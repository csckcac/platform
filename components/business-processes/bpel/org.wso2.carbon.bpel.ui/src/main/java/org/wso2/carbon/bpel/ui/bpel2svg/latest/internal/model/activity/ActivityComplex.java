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

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a complex activity of a process model.
 *
 * @author Gregor Latuske
 */
public abstract class ActivityComplex
	extends Activity {

	/** Serial version UID */
	private static final long serialVersionUID = -8589353770607796041L;

	/** The child activities of this activity. */
	private final List<Activity> children;

	/**
	 * Constructor of ActivityComplex.
	 *
	 * @param name The name of the activity.
	 * @param type The type of the activity.
	 * @param parent The parent activity of this activity.
	 * @param root The root of all activities.
	 */
	public ActivityComplex(String name, String type, ActivityComplex parent, ActivityRoot root) {
		super(name, type, parent, root);
		this.children = new ArrayList<Activity>();
	}

	/**
	 * Returns the value of activities.
	 *
	 * @return The value of activities.
	 */
	public List<Activity> getChildren() {
		return this.children;
	}

}
