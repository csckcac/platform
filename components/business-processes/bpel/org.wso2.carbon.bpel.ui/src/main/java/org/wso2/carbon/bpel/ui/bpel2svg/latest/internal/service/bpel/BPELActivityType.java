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
 *
 * @author Gregor Latuske
 */
public enum BPELActivityType {

	ASSIGN("Assign", ActivitySimple.class),
	COMPENSATE("Compensate", ActivitySimple.class),
	COMPENSATE_SCOPE("CompensateScope", ActivitySimple.class),
	EMPTY("Empty", ActivitySimple.class),
	EXIT("Exit", ActivitySimple.class),
	EXTENSION_ACTIVITY("ExtensionActivity", ActivitySimple.class),
	OPAQUE_ACTIVITY("OpaqueActivity", ActivitySimple.class),
	RECEIVE("Receive", ActivitySimple.class),
	REPLY("Reply", ActivitySimple.class),
	RETHROW("Rethrow", ActivitySimple.class),
	THROW("Throw", ActivitySimple.class),
	VALIDATE("Validate", ActivitySimple.class),
	WAIT("Wait", ActivitySimple.class),
	FLOW("Flow", ActivityFlow.class),
	COMPENSATION_HANDLER("CompensationHandler", ActivityChoice.class),
	EVENT_HANDLERS("EventHandlers", ActivityChoice.class),
	FAULT_HANDLERS("FaultHandlers", ActivityChoice.class),
	IF("If", ActivityChoice.class),
	INVOKE("Invoke", ActivityChoice.class),
	PICK("Pick", ActivityChoice.class),
	SCOPE("Scope", ActivityChoice.class),
	TERMINATION_HANDLER("TerminationHandler", ActivityChoice.class),
	CATCH("Catch", ActivitySequence.class),
	CATCH_ALL("CatchAll", ActivitySequence.class),
	ELSE("Else", ActivitySequence.class),
	ELSE_IF("ElseIf", ActivitySequence.class),
	FOR_EACH("ForEach", ActivitySequence.class),
	ON_ALARM("OnAlarm", ActivitySequence.class),
	ON_EVENT("OnEvent", ActivitySequence.class),
	ON_MESSAGE("OnMessage", ActivitySequence.class),
	REPEAT_UNTIL("RepeatUntil", ActivitySequence.class),
	SEQUENCE("Sequence", ActivitySequence.class),
	WHILE("While", ActivitySequence.class);

	/** The name of the BPEL type. */
	private final String name;

	/** The type of the BPEL type. */
	private final Class<? extends Activity> type;

	/**
	 * Constructor of BPELActivityType.
	 *
	 * @param name The name of the BPEL type.
	 * @param type The type of the BPEL type.
	 */
	private BPELActivityType(String name, Class<? extends Activity> type) {
		this.name = name;
		this.type = type;
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
	public Class<? extends Activity> getType() {
		return this.type;
	}

}
