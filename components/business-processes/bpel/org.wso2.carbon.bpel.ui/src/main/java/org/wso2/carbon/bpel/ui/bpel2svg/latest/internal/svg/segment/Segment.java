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
package org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.segment;

/**
 * This class represents the basis of all SVG segments.
 *
 * @author Gregor Latuske
 */
public abstract class Segment {

	/** The saved child segments as a StringBuffer */
	private StringBuffer sb;

	/**
	 * Constructor of Segment.
	 */
	protected Segment() {
		this.sb = new StringBuffer();
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return this.sb.toString();
	}

	/**
	 * Appends the given text to the output.
	 *
	 * @param value The text, that should be appended to the output.
	 */
	protected void append(String value) {
		this.sb.append(value);
	}

	/**
	 * Appends the given {@link Segment} to the output.
	 *
	 * @param segment The {@link Segment}, that should be appended to the output.
	 */
	protected void append(Segment segment) {
		this.sb.append(segment.toString());
	}

}
