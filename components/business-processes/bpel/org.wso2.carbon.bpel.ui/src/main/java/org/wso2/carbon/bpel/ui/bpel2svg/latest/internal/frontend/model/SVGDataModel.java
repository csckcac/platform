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
package org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.frontend.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.frontend.MainBean;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.frontend.model.components.svg.ActivityAdjuster;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.frontend.model.components.svg.CompactModeAdjuster;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.frontend.model.components.svg.ProcessInstanceSlider;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.frontend.model.components.svg.ProcessModelSlider;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.BPIException;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.ProcessInstance;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.ProcessModel;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.activity.ActivityRoot;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.service.BPIService;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.service.BPIService.BPIServiceFactory;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.SVG;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.settings.Settings;

/**
 * This class encapsulates the view of the {@link SVG}.
 * 
 * @author Gregor Latuske
 */
public class SVGDataModel
	extends AbstractDataModel<SVGDataModel, ProcessInstance> {

	/** Serial version UID */
	private static final long serialVersionUID = 3980081375350445121L;

	/** The logger instance. */
	private static final Log log = LogFactory.getLog(SVGDataModel.class);

	/** The associated {@link ProcessInstance}. */
	private ProcessInstance processInstance;

	/** The SVG, that should be displayed. */
	private SVG svg;

	/** The activity type highlighting component. */
	private final ActivityAdjuster typeHighlighting;

	/** The activity name highlighting component. */
	private final ActivityAdjuster nameHighlighting;

	/** The activity type omitting component. */
	private final ActivityAdjuster typeOmitting;

	/** The activity name omitting component. */
	private final ActivityAdjuster nameOmitting;

	/** The compact mode enabling component. */
	private final CompactModeAdjuster compactMode;

	/** The process model slider component. */
	private final ProcessModelSlider pmSlider;

	/** The process instance slider component. */
	private final ProcessInstanceSlider piSlider;

	/**
	 * Constructor of SVGDataModel.
	 */
	public SVGDataModel() {
		super(null);

		this.typeHighlighting = new ActivityAdjuster(this);
		this.nameHighlighting = new ActivityAdjuster(this);
		this.typeOmitting = new ActivityAdjuster(this);
		this.nameOmitting = new ActivityAdjuster(this);
		this.compactMode = new CompactModeAdjuster(this);
		this.pmSlider = new ProcessModelSlider(this);
		this.piSlider = new ProcessInstanceSlider(this);
	}

    /**
     * This method use to generate the SVG for the processModel
     * @param data
     */
    public void update(ProcessModel data) {
        if (data == null) {
            String errMsg = "ProcessModel is null at org.wso2.carbon.bpel.ui.bpel2svg.latest.internal." +
                            "frontend.model.SVGDataModel.update";
            log.error(errMsg, new NullPointerException(errMsg));
        } else {
            // Generate SVG
            generateSVG(data, new Settings());

            // Update activity adjuster
            ActivityRoot root = data.getActivityRoot();

            // Compact mode reset
            this.compactMode.reset();

            // Highlighting reset
            this.typeHighlighting.reset();
            this.typeHighlighting.setItems(root.getActivityTypes());
            this.nameHighlighting.reset();
            this.nameHighlighting.setItems(root.getActivityNames());

            // Omitting reset
            this.typeOmitting.reset();
            this.typeOmitting.setItems(root.getActivityTypes());
            this.nameOmitting.reset();
            this.nameOmitting.setItems(root.getActivityNames());

            // Slider reset
            this.pmSlider.reset();
            this.pmSlider.setMaxRange(root.getMaxDepth());
            this.piSlider.reset();
        }
    }

	/** {@inheritDoc} */
	@Override
	public void update(ProcessInstance data) {
		this.processInstance = data;

		if (data == null) {
			this.svg = null;
		} else {
			// Generate SVG
			generateSVG(new Settings());

			// Update activity adjuster
			ActivityRoot root = this.processInstance.getProcessModel().getActivityRoot();

			// Compact mode reset
			this.compactMode.reset();

			// Highlighting reset
			this.typeHighlighting.reset();
			this.typeHighlighting.setItems(root.getActivityTypes());
			this.nameHighlighting.reset();
			this.nameHighlighting.setItems(root.getActivityNames());

			// Omitting reset
			this.typeOmitting.reset();
			this.typeOmitting.setItems(root.getActivityTypes());
			this.nameOmitting.reset();
			this.nameOmitting.setItems(root.getActivityNames());

			// Slider reset
			this.pmSlider.reset();
			this.pmSlider.setMaxRange(root.getMaxDepth());
			this.piSlider.reset();
		}
	}

	/**
	 * Updates the SVG output.
	 */
	public void updateSVG() {
		Settings settings = new Settings();
		settings.setHightlightedTypes(this.typeHighlighting.getValues());
		settings.setHightlightedNames(this.nameHighlighting.getValues());
		settings.setOmittedTypes(this.typeOmitting.getValues());
		settings.setOmittedNames(this.nameOmitting.getValues());
		settings.setUseCompactMode(this.compactMode.getSelection());
		settings.setProcessModelGranularity(Integer.valueOf(this.pmSlider.getSelection()));
		settings.setProcessInstanceGranularity(Integer.valueOf(this.piSlider.getSelection()));

		generateSVG(settings);
	}

	/**
	 * Generates a new SVG.
	 * 
	 * @param settings The assoicated SVG settings.
	 */
	private void generateSVG(Settings settings) {
        if (settings.getProcessInstance() == null) {
            settings.setProcessInstance(this.processInstance);
        }

		try {
			BPIService service = new BPIServiceFactory().createService();
			this.svg = service.getSVG(this.processInstance, settings);
		} catch (BPIException exception) {
			log.error("Could not retrieve event data.", exception);
		}
	}

    /**
	 * Generates a new SVG for a given ProcessModel.
	 *
	 * @param settings The assoicated SVG settings.
	 */
	private void generateSVG(ProcessModel data, Settings settings) {
        try {
			BPIService service = new BPIServiceFactory().createService();
			this.svg = service.getSVG(data, settings);
		} catch (BPIException exception) {
			log.error("Could not retrieve event data.", exception);
		}
    }

	/**
	 * Returns the value of processInstance.
	 * 
	 * @return The value of processInstance.
	 */
	public ProcessInstance getProcessInstance() {
		return this.processInstance;
	}

	/**
	 * Returns the value of svg.
	 * 
	 * @return The value of svg.
	 */
	public SVG getSvg() {
		return this.svg;
	}

	/**
	 * Returns the value of typeHighlighting.
	 * 
	 * @return The value of typeHighlighting.
	 */
	public ActivityAdjuster getTypeHighlighting() {
		return this.typeHighlighting;
	}

	/**
	 * Returns the value of nameHighlighting.
	 * 
	 * @return The value of nameHighlighting.
	 */
	public ActivityAdjuster getNameHighlighting() {
		return this.nameHighlighting;
	}

	/**
	 * Returns the value of typeOmitting.
	 * 
	 * @return The value of typeOmitting.
	 */
	public ActivityAdjuster getTypeOmitting() {
		return this.typeOmitting;
	}

	/**
	 * Returns the value of nameOmitting.
	 * 
	 * @return The value of nameOmitting.
	 */
	public ActivityAdjuster getNameOmitting() {
		return this.nameOmitting;
	}

	/**
	 * Returns the value of compactMode.
	 * 
	 * @return The value of compactMode.
	 */
	public CompactModeAdjuster getCompactMode() {
		return this.compactMode;
	}

	/**
	 * Returns the value of pmGranularity.
	 * 
	 * @return The value of pmGranularity.
	 */
	public ProcessModelSlider getPmSlider() {
		return this.pmSlider;
	}

	/**
	 * Returns the value of piGranularity.
	 * 
	 * @return The value of piGranularity.
	 */
	public ProcessInstanceSlider getPiSlider() {
		return this.piSlider;
	}
}
