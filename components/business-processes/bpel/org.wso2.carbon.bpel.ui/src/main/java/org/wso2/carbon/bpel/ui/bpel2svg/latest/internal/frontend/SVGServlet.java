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
package org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.frontend;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.frontend.model.SVGDataModel;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.frontend.util.MainBeanUtil;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.SVG;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This class creates the SVG according to the selection in the {@link MainBean}.
 * 
 * @author Gregor Latuske
 */
public class SVGServlet
	extends HttpServlet {

	/** Serial version UID */
	private static final long serialVersionUID = 3228900451863976570L;
    private static Log log = LogFactory.getLog(SVGServlet.class);

	/** {@inheritDoc} */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException {

		// Get SVG from SVG data model map or single SVG data model
		SVG svg = getSVG(req);

		// Check if SVG is available and generate the output
		if (svg != null) {
			ServletOutputStream sos = resp.getOutputStream();

			try {
				sos.write(svg.getOutput().getBytes());
				sos.flush();
			} finally {
				sos.close();
				resp.setContentType("image/svg+xml");
			}
		}
	}

	/**
	 * Retrieves the SVG from the MainBean.
	 * 
	 * @param bean The instance of the main bean.
	 * @return The SVG from the MainBean.
	 */
	private SVG getSVG(HttpServletRequest req) {
		String id = req.getParameter("id");
        if (id == null) {
            log.error("\"id\" is null", new NullPointerException("\"id\" is null. Please check whether " +
                                             "this value is correctly set in the HttpServletRequest"));
        }
        String mainBeanId = MainBeanUtil.generateMainBeanId(id);

        MainBean bean = (MainBean) req.getSession().getAttribute(mainBeanId);

		SVGDataModel svgDataModel = null;

		if (bean != null) {
			if (id == null || id.isEmpty() || id.equals("null")) {
				svgDataModel = bean.getSvgDataModel();
			} else {
				svgDataModel = bean.getSvgDataModel(id);
			}
		}

		return svgDataModel == null ? null : svgDataModel.getSvg();
	}
}
