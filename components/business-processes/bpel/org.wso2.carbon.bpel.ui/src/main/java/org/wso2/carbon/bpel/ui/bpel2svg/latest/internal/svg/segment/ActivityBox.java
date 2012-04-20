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

import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.activity.Activity;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.activity.ActivityExecData;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.status.ActivityExecStatus;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.settings.Dimension;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.settings.Position;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.settings.Settings;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.wso2.svg.javascript.JSFunction;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.wso2.svg.segment.CancelButton;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.wso2.svg.segment.RetryButton;

import java.util.Calendar;

import static org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.settings.Dimension.*;

/**
 * This class represents an activity box segment.
 *
 * @author Gregor Latuske
 */
public class ActivityBox
	extends Segment {

	/**
	 * Constructor of ActivityBox.
	 *
	 * @param activity The associated {@link Activity}.
	 * @param dimension The {@link Dimension} of the {@link ActivityBox}.
	 * @param position The {@link Position} of the {@link ActivityBox}.
	 * @param settings The associated {@link Settings}.
	 */
	public ActivityBox(Activity activity, Dimension dimension, Position position, Settings settings) {
		super();

		// Determine status information
		ActivityExecData data = settings.getActivityExecData(activity);

		// Determine name
		String name = activity.getName();
		if (name == null || name.equals("")) {
			name = activity.getType();
		}

		// Determine CSS classes
		String cssClass = "frame activity";

		// Check for highlighting
		if (settings.isHighlighted(activity)) {
			cssClass = "frame activity-highlighted";
		}

		append("<g transform=\"translate(" + position.getX() + " " + position.getY() + ")\">");
		append(new Rectangle(dimension, new Position(), cssClass, settings.getBackgroundColor(activity)));

		// Full/Compact mode used?
		if (settings.isUseCompactMode()) {
			createTypeCompact(activity.getType(), dimension);
			createNameCompact(name, dimension);
			createStatusCompact(data.getStatus(), dimension);
		} else {
			createPaths(dimension);
			createType(activity.getType(), dimension);
			createStatus(data.getStatus(), dimension);
			createName(name, dimension);
            //if the activity status is failure, the action items should be included
            if (data.getStatus().getNameLowerCase().equals(ActivityExecStatus.FAILURE.getNameLowerCase())) {
                createActivityAction(data.getActivityId(), data.getProcessInstance().getIid() ,dimension);
            }
			createStartDate(data.getStartDate(), dimension);
			createEndDate(data.getEndDate(), dimension);
		}

		append("</g>\n\n");
	}

	/**
	 * Creates the lines inside the frame.
	 *
	 * @param dimension The {@link Dimension} of the segment.
	 */
	private void createPaths(Dimension dimension) {
		Position pos1 = new Position(0, dimension.getHeight() / 3);
		Position pos2 = new Position(dimension.getWidth(), dimension.getHeight() / 3);
		append(new Path(pos1, pos2));

		pos1 = new Position(0, (dimension.getHeight() / 3) * 2);
		pos2 = new Position(dimension.getWidth(), (dimension.getHeight() / 3) * 2);
		append(new Path(pos1, pos2));

		pos1 = new Position(dimension.getWidth() / 2, 0);
		pos2 = new Position(dimension.getWidth() / 2, dimension.getHeight() / 3);
		append(new Path(pos1, pos2));

		pos1 = new Position(dimension.getWidth() / 2, (dimension.getHeight() / 3) * 2);
		pos2 = new Position(dimension.getWidth() / 2, dimension.getHeight());
		append(new Path(pos1, pos2));
	}

	/**
	 * Creates the type view.
	 *
	 * @param type The type of the {@link Activity}.
	 * @param dimension The {@link Dimension} of the segment.
	 */
	private void createType(String type, Dimension dimension) {
		Position iconPos = new Position();
		iconPos.setX(getTypeImageDimension().getMarginHorizontal());
		iconPos.setY((dimension.getHeight() / 6) - (getTypeImageDimension().getHeight() / 2));
		append(new TypeImage(type, iconPos));

		Position textPos = new Position();
		textPos.setX(getTypeImageDimension().getWidthWithMargin());
		textPos.setY((dimension.getHeight() / 6) + (8 / 2));
		append(new Text(type, 13, textPos, "type"));
	}

	/**
	 * Creates the type view (compact).
	 *
	 * @param type The type of the {@link Activity}.
	 * @param dimension The {@link Dimension} of the segment.
	 */
	private void createTypeCompact(String type, Dimension dimension) {
		Position iconPos = new Position();
		iconPos.setX(getTypeImageDimension().getMarginHorizontal());
		iconPos.setY(dimension.getHeight() / 2 - getTypeImageDimension().getHeight() / 2);
		append(new TypeImage(type, iconPos));
	}

	/**
	 * Creates the status view.
	 *
	 * @param status The status of the {@link Activity}.
	 * @param dimension The {@link Dimension} of the segment.
	 */
	private void createStatus(ActivityExecStatus status, Dimension dimension) {
		Position iconPos = new Position();
		iconPos.setX(dimension.getWidth() / 2 + getStatusImageDimension().getMarginHorizontal());
		iconPos.setY((dimension.getHeight() / 6) - (getStatusImageDimension().getHeight() / 2));
		append(new StatusImage(status, iconPos));

		Position textPos = new Position();
		textPos.setX(dimension.getWidth() / 2 + getStatusImageDimension().getWidthWithMargin());
		textPos.setY((dimension.getHeight() / 6) + (8 / 2));
		append(new Text(status.getName(), textPos, "status"));
    }

    /**
     * Creates action buttons required when the status of the activity is fault.
     */
    private void createActivityAction(String activityId, String instanceId, Dimension dimension) {
        Position buttonPos1 = new Position();
        buttonPos1.setX((dimension.getWidth() * 8) / 10 + getStatusBasedActionImageDimension().getMarginHorizontal());
        buttonPos1.setY((dimension.getHeight() / 2) - getStatusBasedActionImageDimension().getHeight() / 2);

        //Creating the JSFunction to be executed
        JSFunction retryFunction = new JSFunction("retryActivity");
        retryFunction.addInputParameters(new String[]{instanceId, activityId});

        append(new RetryButton(buttonPos1, retryFunction));

        Position buttonPos2 = new Position();
        buttonPos2.setX((dimension.getWidth() * 83) / 100 + getStatusBasedActionImageDimension().getWidthWithMargin());
        buttonPos2.setY((dimension.getHeight() / 2) - getStatusBasedActionImageDimension().getHeight() / 2);

        //Creating the JSFunction to be executed
        JSFunction cancelFunction = new JSFunction("cancelActivity");
        cancelFunction.addInputParameters(new String[]{instanceId, activityId});

        append(new CancelButton(buttonPos2, cancelFunction));
    }

    /**
	 * Creates the status view (compact).
	 *
	 * @param status The status of the {@link Activity}.
	 * @param dimension The {@link Dimension} of the segment.
	 */
	private void createStatusCompact(ActivityExecStatus status, Dimension dimension) {
		Position iconPos = new Position();
		iconPos.setX(dimension.getWidth() - getStatusImageDimension().getWidthWithMargin());
		iconPos.setY(dimension.getHeight() / 2 - getStatusImageDimension().getHeight() / 2);
		append(new StatusImage(status, iconPos));
	}

	/**
	 * Creates the name view.
	 *
	 * @param name The name of the {@link Activity}.
	 * @param dimension The {@link Dimension} of the segment.
	 */
	private void createName(String name, Dimension dimension) {
		Position textPos = new Position();
		textPos.setX(dimension.getWidth() / 2);
		textPos.setY((dimension.getHeight() / 2) + (12 / 2));
		append(new Text(name, 30, textPos, "name", "text-anchor=\"middle\""));
	}

	/**
	 * Creates the name view (compact).
	 *
	 * @param name The name of the {@link Activity}.
	 * @param dimension The {@link Dimension} of the segment.
	 */
	private void createNameCompact(String name, Dimension dimension) {
		Position textPos = new Position();
		textPos.setX(dimension.getWidth() / 2);
		textPos.setY((dimension.getHeight() / 2) + (8 / 2));
		append(new Text(name, 14, textPos, "name-compact", "text-anchor=\"middle\""));
	}

	/**
	 * Creates the start date view.
	 *
	 * @param start The start date of the {@link ActivityExecData}.
	 * @param dimension The {@link Dimension} of the segment.
	 */
	private void createStartDate(Calendar start, Dimension dimension) {
		Position iconPos = new Position();
		iconPos.setX(getDateImageDimension().getMarginHorizontal());
		iconPos.setY(Math.round((dimension.getHeight() / 6.0f) * 5.0f) - (getDateImageDimension().getHeight() / 2));

		Position textPos = new Position();
		textPos.setX(getDateImageDimension().getWidthWithMargin());
		textPos.setY(Math.round((dimension.getHeight() / 6.0f) * 5.0f) + (8 / 2));

		append(new DateImage("Start", iconPos, start != null));
		append(new DateText(start, textPos));
	}

	/**
	 * Creates the end date view.
	 *
	 * @param end The end date of the {@link ActivityExecData}.
	 * @param dimension The {@link Dimension} of the segment.
	 */
	private void createEndDate(Calendar end, Dimension dimension) {
		Position iconPos = new Position();
		iconPos.setX(getDateImageDimension().getMarginHorizontal() + dimension.getWidth() / 2);
		iconPos.setY(Math.round((dimension.getHeight() / 6.0f) * 5.0f) - (getDateImageDimension().getHeight() / 2));

		Position textPos = new Position();
		textPos.setX(getDateImageDimension().getWidthWithMargin() + dimension.getWidth() / 2);
		textPos.setY(Math.round((dimension.getHeight() / 6.0f) * 5.0f) + (8 / 2));

		append(new DateImage("End", iconPos, end != null));
		append(new DateText(end, textPos));
	}

}
