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
package org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.element;

import java.io.Serializable;

/**
 * This class represents a link between two elements.
 * 
 * @author Gregor Latuske
 */
public class LinkElement /*implements Serializable*/ {

	/** Serial version UID */
	private static final long serialVersionUID = -28410032816965589L;

	/** The name of the link. */
	private final String name;

	/** The source activity of the link. */
	private ActivityElement<?> source;

	/** The target activity of the link. */
	private ActivityElement<?> target;

	/** Is the link used in the grid already? */
	private boolean used;

	/**
	 * Constructor of Link.
	 * 
	 * @param name The name of the link.
	 */
	public LinkElement(String name) {
		this(name, null, null);
	}

	/**
	 * Constructor of Link.
	 * 
	 * @param name The name of the link.
	 * @param source The source of the link.
	 * @param target The target of the link.
	 */
	public LinkElement(String name, ActivityElement<?> source, ActivityElement<?> target) {
		this.name = name;
		this.source = source;
		this.target = target;
		this.used = false;
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
	 * Returns the value of source.
	 * 
	 * @return The value of source.
	 */
	public ActivityElement<?> getSource() {
		return this.source;
	}

	/**
	 * Sets the value of source to source.
	 * 
	 * @param source The new value of source.
	 */
	public void setSource(ActivityElement<?> source) {
		this.source = source;
	}

	/**
	 * Sets the value of target to target.
	 * 
	 * @param target The new value of target.
	 */
	public void setTarget(ActivityElement<?> target) {
		this.target = target;
	}

	/**
	 * Returns the value of target.
	 * 
	 * @return The value of target.
	 */
	public ActivityElement<?> getTarget() {
		return this.target;
	}

	/**
	 * Returns the value of used.
	 * 
	 * @return The value of used.
	 */
	public boolean isUsed() {
		return used;
	}

	/**
	 * Sets the value of used to true.
	 */
	public void use() {
		this.used = true;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof LinkElement)) {
			return false;
		}

		LinkElement link = (LinkElement) obj;
		return getName().equals(link.getName());
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return getName().hashCode() * 7;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "Name: " + getName() + ", Source (" + getSource() + "), Target (" + getTarget() + ")";
	}

}
