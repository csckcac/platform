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
package org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.settings;

import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.ProcessInstance;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.activity.*;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.status.ActivityExecStatus;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.service.bpel.BPELActivityType;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * This class saves the settings which can taken to the SVG.
 * 
 * @author Gregor Latuske
 */
public class Settings {

	/** Light orange for the activity rectangle background. */
	private static final int LIGHT_ORANGE = 0xFFF3DE;

	/** Dark orange for the activity rectangle background. */
	private static final int DARK_ORANGE = 0xFC9E00;

	/** The minimum value of the process model granularity. */
	public static final int PM_GRANULARITY_MIN = 5;

	/** The value of the process instance granularity. */
	public static final int PI_GRANULARITY = 3;

    public ProcessInstance getProcessInstance() {
        return processInstance;
    }

    /** The associated process instance. */
	private ProcessInstance processInstance;

	/** The map with the status information. */
	private Map<String, ActivityExecData> activityExecData;

	/** The set of highlighted activity types. */
	private Set<String> hightlightedTypes;

	/** The set of highlighted activity names. */
	private Set<String> hightlightedNames;

	/** The set of omitted activity types. */
	private Set<String> omittedTypes;

	/** The set of omitted activity names. */
	private Set<String> omittedNames;

	/** Compact mode used? */
	private boolean useCompactMode;

	/** The granularity of the process model. */
	private int pmGranularity;

	/** The granularity of the process instance. */
	private int piGranularity;

	/**
	 * Constructor of SVGSettings.
	 */
	public Settings() {
		this.processInstance = null;
		this.activityExecData = new HashMap<String, ActivityExecData>();
		this.hightlightedTypes = new TreeSet<String>();
		this.hightlightedNames = new TreeSet<String>();
		this.omittedTypes = new TreeSet<String>();
		this.omittedNames = new TreeSet<String>();
		this.useCompactMode = false;
		this.pmGranularity = 0;
		this.piGranularity = 0;
	}

	/**
	 * Returns the {@link ActivityExecData} of the given {@link Activity} or the default data if no data was
	 * found.
	 * 
	 * @param activity The given {@link Activity}.
	 * @return the {@link ActivityExecData} of the given {@link Activity} or the default data if no data was
	 *         found.
	 */
	public ActivityExecData getActivityExecData(Activity activity) {
		ActivityExecData data = this.activityExecData.get(activity.getName());

		if (data == null) {
			data = new ActivityExecData(null, ActivityExecStatus.OUTSTANDING, null);
		}

		return data;
	}

	/**
	 * Returns true if the activity is highlighted.
	 * 
	 * @param activity The activity to check.
	 * @return True if the activity is highlighted.
	 */
	public boolean isHighlighted(Activity activity) {
		// Type is highlighted
		if (this.hightlightedTypes.contains(activity.getType())) {
			return true;
		}

		// Name is highlighted
		if (this.hightlightedNames.contains(activity.getName())) {
			return true;
		}

		// PI Step 1
		if (this.piGranularity >= 1) {
			if (isPathHighlighted(activity)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns true if the arrow to the activity is highlighted.
	 * 
	 * @param activity The activity to check.
	 * @return True if the arrow to the activity is highlighted.
	 */
	public boolean isArrowHighlighted(Activity activity) {
		// PI Step 1
		if (this.piGranularity >= 1) {
			if (isPathHighlighted(activity)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns true if the activity is highlighted.
	 * 
	 * @param activity The activity to check.
	 * @return True if the activity is highlighted.
	 */
	private boolean isPathHighlighted(Activity activity) {
		ActivityExecStatus status = getActivityExecData(activity).getStatus();
		if (!status.equals(ActivityExecStatus.OUTSTANDING) && !status.equals(ActivityExecStatus.SKIPPED)) {
			return true;
		}

		// If any sibling after the given activity in a ActivitySequence is highlighted
		if (activity.getParent() != null && activity.getParent() instanceof ActivitySequence) {
			List<Activity> siblings = activity.getParent().getChildren();
			int index = siblings.indexOf(activity) + 1;

			while (index < siblings.size()) {
				if (isPathHighlighted(siblings.get(index))) {
					return true;
				}

				index++;
			}
		}

		// Check targets
		for (Activity target : activity.getTargets()) {
			if (isPathHighlighted(target) && target.getSources().size() == 1) {
				return true;
			}
		}

		// If any child is highlighted
		if (activity instanceof ActivityComplex) {
			for (Activity child : ((ActivityComplex) activity).getChildren()) {
				if (isPathHighlighted(child)) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Returns true if the activity is omitted.
	 * 
	 * @param activity The activity to check.
	 * @return True if the activity is omitted.
	 */
	public boolean isOmitted(Activity activity) {
		// Root cannot be omitted
		if (activity instanceof ActivityRoot) {
			return false;
		}

		String type = activity.getType();
		String parentType = activity.getParent().getType();

		// Type is omitted
		if (this.omittedTypes.contains(activity.getType())) {
			return true;
		}

		// Name is omitted
		if (this.omittedNames.contains(activity.getName())) {
			return true;
		}

		// PM Step 2
		if (this.pmGranularity >= 2) {
			if (type.equalsIgnoreCase(BPELActivityType.ASSIGN.getName())
				|| type.equalsIgnoreCase(BPELActivityType.EMPTY.getName())) {
				return true;
			}
		}

		// PM Step 3
		if (this.pmGranularity >= 3) {
			if (type.equalsIgnoreCase(BPELActivityType.THROW.getName())
				|| type.equalsIgnoreCase(BPELActivityType.RETHROW.getName())
				|| type.equalsIgnoreCase(BPELActivityType.VALIDATE.getName())) {
				return true;
			}
		}

		// PM Step 4
		if (this.pmGranularity >= 4) {
			if (parentType.equalsIgnoreCase(BPELActivityType.COMPENSATION_HANDLER.getName())
				|| parentType.equalsIgnoreCase(BPELActivityType.TERMINATION_HANDLER.getName())) {
				return true;
			}
		}

		// PM Step 5
		if (this.pmGranularity >= 5) {
			if (parentType.equalsIgnoreCase(BPELActivityType.CATCH.getName())
				|| parentType.equalsIgnoreCase(BPELActivityType.CATCH_ALL.getName())
				|| parentType.equalsIgnoreCase(BPELActivityType.ON_ALARM.getName())
				|| parentType.equalsIgnoreCase(BPELActivityType.ON_EVENT.getName())
				|| parentType.equalsIgnoreCase(BPELActivityType.ON_MESSAGE.getName())) {
				return true;
			}
		}

		// PM Step 6 or higher
		if (this.pmGranularity >= 6) {
			int omitDepth = this.pmGranularity - PM_GRANULARITY_MIN;
			if (omitDepth > 0) {
				if (activity.getRoot().getMaxDepth() - activity.getDepth() < omitDepth) {
					return true;
				}
			}
		}

		// PI Step 3 or higher
		if (this.piGranularity >= 3) {

			// Activity must no be highlighted && link check have to be OK to omit
			if (!isPathHighlighted(activity) && checkSources(activity)) {

				// Parent: choice (at least one sibling have to be highlighted)
				if (activity.getParent() instanceof ActivityChoice) {
					boolean siblingHighlighted = false;

					for (Activity child : activity.getParent().getChildren()) {
						siblingHighlighted = siblingHighlighted || isPathHighlighted(child);
					}

					return siblingHighlighted;
				}

				// Parent: sequence (parent is omitted)
				if (activity.getParent() instanceof ActivitySequence) {
					return isOmitted(activity.getParent());
				}

				// Parent: Flow (parent is completed)
				if (activity.getParent() instanceof ActivityFlow) {
					return isFinished(activity.getParent());
				}
			}
		}

		return false;
	}

	/**
	 * Returns true if an activity is finished.
	 * <p>
	 * Status: COMPLETED, FAILURE, RECOVERY, SKIPPED
	 * 
	 * @param activity The activity to check.
	 * @return True if an activity is finished.
	 */
	private boolean isFinished(Activity activity) {
		ActivityExecStatus status = getActivityExecData(activity).getStatus();
		return status.equals(ActivityExecStatus.COMPLETED) || status.equals(ActivityExecStatus.FAILURE)
			|| status.equals(ActivityExecStatus.RECOVERY) || status.equals(ActivityExecStatus.SKIPPED);
	}

	/**
	 * Checks the sources of the given activity.
	 * <p>
	 * If all sources in finished status return true.
	 * <p>
	 * If the first check returns false, check children recursively.
	 * 
	 * @param activity
	 * @return True if all sources in finished status.
	 *         <p>
	 *         If the first check returns false, check children recursively.
	 */
	private boolean checkSources(Activity activity) {
		// Check all sources for finished status
		boolean sourcesFinished = true;

		for (Activity source : activity.getSources()) {
			sourcesFinished = sourcesFinished && isFinished(source);
		}

		// If all finished, check recursively
		if (sourcesFinished && activity instanceof ActivityComplex) {
			for (Activity child : ((ActivityComplex) activity).getChildren()) {
				sourcesFinished = sourcesFinished && checkSources(child);
			}
		}

		return sourcesFinished;
	}

	/**
	 * Returns the value of useCompactMode.
	 * <p>
	 * Default: "false"
	 * 
	 * @return The value of useCompactMode.
	 */
	public boolean isUseCompactMode() {
		return this.useCompactMode || this.pmGranularity > 0;
	}

	/**
	 * Calculates the background colour of an activity.
	 * 
	 * @param activity The activity to calculate the background colour.
	 * @return The background colour of an activity.
	 */
	public Color getBackgroundColor(Activity activity) {
		if (this.piGranularity < 2 || this.processInstance == null || !isPathHighlighted(activity)) {
			return null;
		}

		// The start colours
		Color light = new Color(LIGHT_ORANGE);
		Color dark = new Color(DARK_ORANGE);

		// Total time of the process instance
		long totalTime = this.processInstance.getLastActivityDate().getTimeInMillis();
		totalTime = totalTime - this.processInstance.getStartDate().getTimeInMillis();

		// Time of the activity
		ActivityExecData data = getActivityExecData(activity);
		Calendar startDate = data.getStartDate();
		Calendar endDate = data.getEndDate();
		long time = 1;

		// Start date && end date != null
		if (startDate != null && endDate != null) {
			time = endDate.getTimeInMillis() - startDate.getTimeInMillis();

			// Necessary if the last active date is not correct
			if (time > totalTime) {
				time = totalTime;
			}
		}

		// The ratio
		double ratio = (double) time / (double) totalTime;

		// The RGB values
		int red = (int) (light.getRed() - Math.round(ratio * (light.getRed() - dark.getRed())));
		int green = (int) (light.getGreen() - Math.round(ratio * (light.getGreen() - dark.getGreen())));
		int blue = (int) (light.getBlue() - Math.round(ratio * (light.getBlue() - dark.getBlue())));

		return new Color(red, green, blue);
	}

	/**
	 * Sets the value of processInstance to processInstance.
	 * 
	 * @param processInstance The new value of processInstance.
	 */
	public void setProcessInstance(ProcessInstance processInstance) {
		this.processInstance = processInstance;
	}

	/**
	 * Sets the value of activityExecData to activityExecData.
	 * 
	 * @param activityExecData The new value of activityExecData.
	 */
	public void setActivityExecData(Map<String, ActivityExecData> activityExecData) {
		this.activityExecData = activityExecData;
	}

	/**
	 * Sets the value of hightlightedTypes to hightlightedTypes.
	 * 
	 * @param hightlightedTypes The new value of hightlightedTypes.
	 */
	public void setHightlightedTypes(Set<String> hightlightedTypes) {
		if (hightlightedTypes != null) {
			this.hightlightedTypes = hightlightedTypes;
		}
	}

	/**
	 * Sets the value of hightlightedNames to hightlightedNames.
	 * 
	 * @param hightlightedNames The new value of hightlightedNames.
	 */
	public void setHightlightedNames(Set<String> hightlightedNames) {
		if (hightlightedNames != null) {
			this.hightlightedNames = hightlightedNames;
		}
	}

	/**
	 * Sets the value of omittedTypes to omittedTypes.
	 * 
	 * @param omittedTypes The new value of omittedTypes.
	 */
	public void setOmittedTypes(Set<String> omittedTypes) {
		if (omittedTypes != null) {
			this.omittedTypes = omittedTypes;
		}
	}

	/**
	 * Sets the value of omittedNames to omittedNames.
	 * 
	 * @param omittedNames The new value of omittedNames.
	 */
	public void setOmittedNames(Set<String> omittedNames) {
		if (omittedNames != null) {
			this.omittedNames = omittedNames;
		}
	}

	/**
	 * Sets the value of useCompactMode to useCompactMode.
	 * <p>
	 * Default: "false"
	 * 
	 * @param useCompactMode The new value of useCompactMode.
	 */
	public void setUseCompactMode(boolean useCompactMode) {
		this.useCompactMode = useCompactMode;
	}

	/**
	 * Sets the value of granularity to pmGranularity.
	 * <p>
	 * Default: "0"
	 * 
	 * @param pmGranularity The new value of pmGranularity.
	 */
	public void setProcessModelGranularity(int pmGranularity) {
		this.pmGranularity = pmGranularity;
	}

	/**
	 * Sets the value of granularity to piGranularity.
	 * <p>
	 * Default: "0"
	 * 
	 * @param piGranularity The new value of piGranularity.
	 */
	public void setProcessInstanceGranularity(int piGranularity) {
		this.piGranularity = piGranularity;
	}

}
