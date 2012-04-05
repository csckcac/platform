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

/**
 * This class represents a link between two activities.
 *
 * @author Gregor Latuske
 */
public class Link
	implements Serializable {

	/** Serial version UID */
	private static final long serialVersionUID = -8047887226112876681L;

	/** The name of the link. */
	private String name;

	/** The source activity of the link. */
	private Activity source;

	/** The target activity of the link. */
	private Activity target;

	/**
	 * Constructor of Link.
	 *
	 * @param name The name of the link.
	 */
	public Link(String name) {
		this.name = name;
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
	 * Sets the value of name to name.
	 *
	 * @param name The new value of name.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the value of source.
	 *
	 * @return The value of source.
	 */
	public Activity getSource() {
		return this.source;
	}

	/**
	 * Sets the value of source to source.
	 *
	 * @param source The new value of source.
	 */
	public void setSource(Activity source) {
		this.source = source;
	}

	/**
	 * Sets the value of target to target.
	 *
	 * @param target The new value of target.
	 */
	public void setTarget(Activity target) {
		this.target = target;
	}

	/**
	 * Returns the value of target.
	 *
	 * @return The value of target.
	 */
	public Activity getTarget() {
		return this.target;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Link)) {
			return false;
		}

		Link link = (Link) obj;
		return getName().equals(link.getName());
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		if (getSource() == null || getTarget() == null) {
			return super.hashCode();
		}

		return getName().hashCode() * 7;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "Name: " + getName() + ", Source (" + getSource() + "), Target (" + getTarget() + ")";
	}

}
