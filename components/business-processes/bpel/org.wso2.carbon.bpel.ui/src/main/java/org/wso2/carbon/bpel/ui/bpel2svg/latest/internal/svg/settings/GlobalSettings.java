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

/**
 * This class saves the settings which can taken to the SVG.
 */
public final class GlobalSettings {

    /**
     * The instance of {@link GlobalSettings}
     */
    private static GlobalSettings instance;

    /**
     * The path to the CSS file.
     */
    private String cssPath;

    /**
     * The path to the JavaScript file.
     */
    private String[] jsPaths;

    /**
     * Boxes with all available information.
     */
    private Dimension fullMode;

    /**
     * Smaller boxes with a subset of information.
     */
    private Dimension compactMode;

    /**
     * The {@link Dimension} of a type image.
     */
    private Dimension typeImageSize;

    /**
     * The path to the type images.
     */
    private String typeImagePath;

    /**
     * The file extension of the type images.
     */
    private String typeImageExtension;

    /**
     * The {@link Dimension} of a status image.
     */
    private Dimension statusImageSize;

    /**
     * The path to the status images.
     */
    private String statusImagePath;

    /**
     * The file extension of the status images.
     */
    private String statusImageExtension;

    /**
     * The {@link Dimension} of a date image.
     */
    private Dimension dateImageSize;

    /**
     * The {@link Dimension} of a status based action image.
     */
    private Dimension statusBasedActionImageSize;

    /**
     * The path to the date images.
     */
    private String dateImagePath;

    /**
     * The file extension of the date images.
     */
    private String dateImageExtension;

    /**
     * The path to the images for actions based on activity status
     */
    private String statusBasedActionImagePath;

    /**
     * The file extension of the images for actions based on activity status.
     */
    private String statusBasedActionImageExtension;

    /**
     * Constructor of SVGSettings.
     */
    private GlobalSettings() {
        this.cssPath = "./visualizer/resources/styles/svg.css";
        this.jsPaths = new String[]{"../dialog/js/jqueryui/tabs/jquery-1.2.6.min.js", "./js/bpel-main.js", "./js/instance_view_management_actions.js", "./visualizer/resources/scripts/svg.js"};
        this.fullMode = new Dimension(270, 15, 90, 20);
        this.compactMode = new Dimension(150, 10, 30, 10);
        this.typeImageSize = new Dimension(20, 5, 20, 15);
        this.typeImagePath = "./visualizer/resources/icons/type/";
        this.typeImageExtension = ".png";
        this.statusImageSize = new Dimension(24, 5, 24, 5);
        this.statusImagePath = "./visualizer/resources/icons/status/activity/";
        this.statusImageExtension = ".png";
        this.dateImageSize = new Dimension(16, 5, 16, 5);
        this.dateImagePath = "./visualizer/resources/icons/date/";
        this.dateImageExtension = ".png";
        this.statusBasedActionImagePath = "./visualizer/resources/icons/status/action/";
        this.statusBasedActionImageExtension = ".gif";
        this.statusBasedActionImageSize = new Dimension(16, 5, 16, 5);
    }

    /**
     * Returns the instance of {@link GlobalSettings}.
     *
     * @return The instance of {@link GlobalSettings}.
     */
    public static synchronized GlobalSettings getInstance() {
        if (instance == null) {
            instance = new GlobalSettings();
        }
        return instance;
    }

    /**
     * Returns the value of cssPath.
     * <p/>
     * Default: "./visualizer/resources/styles/svg.css"
     *
     * @return The value of cssPath.
     */
    public String getCssPath() {
        return this.cssPath;
    }

    /**
     * Sets the value of cssPath to cssPath.
     * <p>
     * Default: "./visualizer/resources/styles/svg.css"
     *
     * @param cssPath The new value of cssPath.
     */
//	public void setCssPath(String cssPath) {
//		this.cssPath = cssPath;
//	}

    /**
     * Returns the value of jsPaths.
     * <p/>
     * Default: "./visualizer/resources/scripts/svg.js"
     *
     * @return The value of jsPaths.
     */
    public String[] getJsPaths() {
        return this.jsPaths;
    }

    /**
     * Sets the value of jsPaths to jsPaths.
     * <p>
     * Default: "./visualizer/resources/scripts/svg.js"
     *
     * @param jsPaths The new value of jsPaths.
     */
//	public void setJsPaths(String[] jsPaths) {
//		this.jsPaths = jsPaths;
//	}

    /**
     * Returns the value of fullMode.
     * <p/>
     * Default: "new Dimension(270, 15, 90, 20)"
     *
     * @return The value of fullMode.
     */
    public Dimension getFullMode() {
        return this.fullMode;
    }

    /**
     * Sets the value of fullMode to fullMode.
     * <p>
     * Default: "new Dimension(270, 15, 90, 20)"
     *
     * @param fullMode The new value of fullMode.
     */
//	public void setFullMode(Dimension fullMode) {
//		this.fullMode = fullMode;
//	}

    /**
     * Returns the value of compactMode.
     * <p/>
     * Default: "new Dimension(150, 10, 30, 10)"
     *
     * @return The value of compactMode.
     */
    public Dimension getCompactMode() {
        return this.compactMode;
    }

    /**
     * Sets the value of compactMode to compactMode.
     * <p>
     * Default: "new Dimension(150, 10, 30, 10)"
     *
     * @param compactMode The new value of compactMode.
     */
//	public void setCompactMode(Dimension compactMode) {
//		this.compactMode = compactMode;
//	}

    /**
     * Returns the value of typeImageSize.
     * <p/>
     * Default: new SVGDimension(20, 5, 20, 15);
     *
     * @return The value of typeImageSize.
     */
    public Dimension getTypeImageSize() {
        return this.typeImageSize;
    }

    /**
     * Sets the value of typeImageSize to typeImageSize.
     * <p>
     * Default: new SVGDimension(20, 5, 20, 15);
     *
     * @param typeImageSize The new value of typeImageSize.
     */
//	public void setTypeImageSize(Dimension typeImageSize) {
//		this.typeImageSize = typeImageSize;
//	}

    /**
     * Returns the value of typeImagePath.
     * <p/>
     * Default: "./visualizer/resources/icons/type/"
     *
     * @return The value of typeImagePath.
     */
    public String getTypeImagePath() {
        return this.typeImagePath;
    }

    /**
     * Sets the value of typeImagePath to typeImagePath.
     * <p>
     * Default:"./visualizer/resources/icons/type/"
     *
     * @param typeImagePath The new value of typeImagePath.
     */
//	public void setTypeImagePath(String typeImagePath) {
//		this.typeImagePath = typeImagePath;
//	}

    /**
     * Returns the value of typeImageExtension.
     * <p/>
     * Default: ".png"
     *
     * @return The value of typeImageExtension.
     */
    public String getTypeImageExtension() {
        return this.typeImageExtension;
    }

    /**
     * Sets the value of typeImageExtension to typeImageExtension.
     * <p>
     * Default: ".png"
     *
     * @param typeImageExtension The new value of typeImageExtension.
     */
//	public void setTypeImageExtension(String typeImageExtension) {
//		this.typeImageExtension = typeImageExtension;
//	}

    /**
     * Returns the value of statusImageSize.
     * <p/>
     * Default: new SVGDimension(24, 5, 24, 5);
     *
     * @return The value of statusImageSize.
     */
    public Dimension getStatusImageSize() {
        return this.statusImageSize;
    }

    /**
     * Sets the value of statusImageSize to statusImageSize.
     * <p>
     * Default: new SVGDimension(24, 5, 24, 5);
     *
     * @param statusImageSize The new value of statusImageSize.
     */
//	public void setStatusImageSize(Dimension statusImageSize) {
//		this.statusImageSize = statusImageSize;
//	}

    /**
     * Returns the value of statusImagePath.
     * <p/>
     * Default: "./visualizer/resources/icons/status/activity/"
     *
     * @return The value of statusImagePath.
     */
    public String getStatusImagePath() {
        return this.statusImagePath;
    }

    /**
     * Sets the value of statusImagePath to statusImagePath.
     * <p>
     * Default: "./visualizer/resources/icons/status/activity/"
     *
     * @param statusImagePath The new value of statusImagePath.
     */
//	public void setStatusImagePath(String statusImagePath) {
//		this.statusImagePath = statusImagePath;
//	}

    /**
     * Returns the value of statusImageExtension.
     * <p/>
     * Default: ".png"
     *
     * @return The value of statusImageExtension.
     */
    public String getStatusImageExtension() {
        return this.statusImageExtension;
    }

    /**
     * Sets the value of statusImageExtension to statusImageExtension.
     * <p>
     * Default: ".png"
     *
     * @param statusImageExtension The new value of statusImageExtension.
     */
//	public void setStatusImageExtension(String statusImageExtension) {
//		this.statusImageExtension = statusImageExtension;
//	}

    /**
     * Returns the value of dateImageSize.
     * <p/>
     * Default: new SVGDimension(16, 5, 16, 5);
     *
     * @return The value of dateImageSize.
     */
    public Dimension getDateImageSize() {
        return this.dateImageSize;
    }

    /**
     * Returns the value of statusBasedActionImageSize.
     * <p/>
     * Default: new SVGDimension(16, 5, 16, 5);
     *
     * @return The value of statusBasedActionImageSize.
     */
    public Dimension getStatusBasedActionImageSize() {
        return this.statusBasedActionImageSize;
    }

    /**
     * Sets the value of dateImageSize to dateImageSize.
     * <p>
     * Default: new SVGDimension(16, 5, 16, 5);
     *
     * @param dateImageSize The new value of dateImageSize.
     */
//	public void setDateImageSize(Dimension dateImageSize) {
//		this.dateImageSize = dateImageSize;
//	}

    /**
     * Returns the value of dateImagePath.
     * <p/>
     * Default: "./visualizer/resources/icons/date/"
     *
     * @return The value of dateImagePath.
     */
    public String getDateImagePath() {
        return this.dateImagePath;
    }

    /**
     * Sets the value of dateImagePath to dateImagePath.
     * <p>
     * Default: "./visualizer/resources/icons/date/"
     *
     * @param dateImagePath The new value of dateImagePath.
     */
//	public void setDateImagePath(String dateImagePath) {
//		this.dateImagePath = dateImagePath;
//	}

    /**
     * Returns the value of dateImageExtension.
     * <p/>
     * Default: ".png"
     *
     * @return The value of dateImageExtension.
     */
    public String getDateImageExtension() {
        return this.dateImageExtension;
    }

    /**
     * Sets the value of dateImageExtension to dateImageExtension.
     * <p>
     * Default: ".png"
     *
     * @param dateImageExtension The new value of dateImageExtension.
     */
//	public void setDateImageExtension(String dateImageExtension) {
//		this.dateImageExtension = dateImageExtension;
//	}

    /**
     * Returns the value of statusBasedActionImageExtension.
     * <p/>
     * Default: ".png"
     *
     * @return The value of statusBasedActionImageExtension.
     */
    public String getStatusBasedActionImageExtension() {
        return this.statusBasedActionImageExtension;
    }

    /**
     * Returns the value of statusBasedActionImagePath.
     * <p/>
     * Default: "./visualizer/resources/icons/status/action/"
     *
     * @return The value of dateImagePath.
     */
    public String getStatusBasedActionImagePath() {
        return this.statusBasedActionImagePath;
    }

}
