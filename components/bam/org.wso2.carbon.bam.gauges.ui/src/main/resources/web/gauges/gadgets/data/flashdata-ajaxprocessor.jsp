<%
  /**
  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.

  WSO2 Inc. licenses this file to you under the Apache License,
  Version 2.0 (the "License"); you may not use this file except
  in compliance with the License.
  You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
 */

%>
<%@ page import="java.util.Random"%>
<%
	String funcName = request.getParameter("funcName");
	Random randomGenerator = new Random();

    if("servers".equals(funcName)) {
        out.print("<level0>");
            out.print("<level1 name='https://10.100.1.119:8243/esb'>");
            out.print("</level1>");
            out.print("<level1 name='https://10.100.1.245:8243/esb'>");
            out.print("</level1>");
        out.print("</level0>");
    }
    else if("faults".equals(funcName)) {
        out.print("<level0 name='https://10.100.1.119:8243/esb'>");
            out.print("<stats name='Faults' count='"+randomGenerator.nextInt(50)+"'/>");
            out.print("<level1 name='Fault 03'>");
                out.print("<stats name='Faults' count='"+randomGenerator.nextInt(50)+"'/>");
	        out.print("</level1>");
            out.print("<level1 name='Fault 02'>");
                out.print("<stats name='Faults' count='"+randomGenerator.nextInt(50)+"'/>");
	        out.print("</level1>");
            out.print("<level1 name='Fault 01'>");
                out.print("<stats name='Faults' count='"+randomGenerator.nextInt(50)+"'/>");
	        out.print("</level1>");
        out.print("</level0>");
    }
    else if("faults-https://10.100.1.119:8243/esb".equals(funcName)) {
        out.print("<level0 name='https://10.100.1.119:8243/esb'>");
            out.print("<stats name='Faults' count='"+randomGenerator.nextInt(50)+"'/>");
            out.print("<level1 name='Fault 03'>");
                out.print("<stats name='Faults' count='"+randomGenerator.nextInt(50)+"'/>");
	        out.print("</level1>");
            out.print("<level1 name='Fault 02'>");
                out.print("<stats name='Faults' count='"+randomGenerator.nextInt(50)+"'/>");
	        out.print("</level1>");
            out.print("<level1 name='Fault 01'>");
                out.print("<stats name='Faults' count='"+randomGenerator.nextInt(50)+"'/>");
	        out.print("</level1>");
        out.print("</level0>");
    }
    else if("faults-https://10.100.1.245:8243/esb".equals(funcName)) {
        out.print("<level0 name='https://10.100.1.245:8243/esb'>");
            out.print("<stats name='Faults' count='"+randomGenerator.nextInt(50)+"'/>");
            out.print("<level1 name='Fault 05'>");
                out.print("<stats name='Faults' count='"+randomGenerator.nextInt(50)+"'/>");
	        out.print("</level1>");
            out.print("<level1 name='Fault 04'>");
                out.print("<stats name='Faults' count='"+randomGenerator.nextInt(50)+"'/>");
	        out.print("</level1>");
            out.print("<level1 name='Fault 03'>");
                out.print("<stats name='Faults' count='"+randomGenerator.nextInt(50)+"'/>");
	        out.print("</level1>");
            out.print("<level1 name='Fault 02'>");
                out.print("<stats name='Faults' count='"+randomGenerator.nextInt(50)+"'/>");
	        out.print("</level1>");
            out.print("<level1 name='Fault 01'>");
                out.print("<stats name='Faults' count='"+randomGenerator.nextInt(50)+"'/>");
	        out.print("</level1>");
        out.print("</level0>");
    }
    else if("operations".equals(funcName)) {
        out.print("<level0 name='https://10.100.1.119:8243/esb'>");
            out.print("<stats name='Requests' count='"+(randomGenerator.nextInt(30)+10)+"' />");
	        out.print("<stats name='Faults' count='"+randomGenerator.nextInt(10)+"'/>");
	        out.print("<level1 name='endpoint 03'>");
                out.print("<stats name='Requests' count='"+(randomGenerator.nextInt(30)+10)+"' />");
		        out.print("<stats name='Faults' count='"+randomGenerator.nextInt(10)+"'/>");
                    out.print("<level2 name='operation 04'>");
                        out.print("<stats name='Requests' count='"+(randomGenerator.nextInt(30)+10)+"' />");
	                    out.print("<stats name='Faults' count='"+randomGenerator.nextInt(10)+"'/>");
                    out.print("</level2>");
                    out.print("<level2 name='operation 03'>");
                        out.print("<stats name='Requests' count='"+(randomGenerator.nextInt(30)+10)+"' />");
	                    out.print("<stats name='Faults' count='"+randomGenerator.nextInt(10)+"'/>");
                    out.print("</level2>");
                    out.print("<level2 name='operation 02'>");
                        out.print("<stats name='Requests' count='"+(randomGenerator.nextInt(30)+10)+"' />");
	                    out.print("<stats name='Faults' count='"+randomGenerator.nextInt(10)+"'/>");
                    out.print("</level2>");
                    out.print("<level2 name='operation 01'>");
                        out.print("<stats name='Requests' count='"+(randomGenerator.nextInt(30)+10)+"' />");
	                    out.print("<stats name='Faults' count='"+randomGenerator.nextInt(10)+"'/>");
                    out.print("</level2>");
	        out.print("</level1>");

            out.print("<level1 name='endpoint 02'>");
                out.print("<stats name='Requests' count='"+(randomGenerator.nextInt(30)+10)+"' />");
		        out.print("<stats name='Faults' count='"+randomGenerator.nextInt(10)+"'/>");
                    out.print("<level2 name='operation 04'>");
                        out.print("<stats name='Requests' count='"+(randomGenerator.nextInt(30)+10)+"' />");
	                    out.print("<stats name='Faults' count='"+randomGenerator.nextInt(10)+"'/>");
                    out.print("</level2>");
                    out.print("<level2 name='operation 03'>");
                        out.print("<stats name='Requests' count='"+(randomGenerator.nextInt(30)+10)+"' />");
	                    out.print("<stats name='Faults' count='"+randomGenerator.nextInt(10)+"'/>");
                    out.print("</level2>");
                    out.print("<level2 name='operation 02'>");
                        out.print("<stats name='Requests' count='"+(randomGenerator.nextInt(30)+10)+"' />");
	                    out.print("<stats name='Faults' count='"+randomGenerator.nextInt(10)+"'/>");
                    out.print("</level2>");
                    out.print("<level2 name='operation 01'>");
                        out.print("<stats name='Requests' count='"+(randomGenerator.nextInt(30)+10)+"' />");
	                    out.print("<stats name='Faults' count='"+randomGenerator.nextInt(10)+"'/>");
                    out.print("</level2>");
	        out.print("</level1>");

            out.print("<level1 name='endpoint 01'>");
                out.print("<stats name='Requests' count='"+(randomGenerator.nextInt(30)+10)+"' />");
		        out.print("<stats name='Faults' count='"+randomGenerator.nextInt(10)+"'/>");
                    out.print("<level2 name='operation 04'>");
                        out.print("<stats name='Requests' count='"+(randomGenerator.nextInt(30)+10)+"' />");
	                    out.print("<stats name='Faults' count='"+randomGenerator.nextInt(10)+"'/>");
                    out.print("</level2>");
                    out.print("<level2 name='operation 03'>");
                        out.print("<stats name='Requests' count='"+(randomGenerator.nextInt(30)+10)+"' />");
	                    out.print("<stats name='Faults' count='"+randomGenerator.nextInt(10)+"'/>");
                    out.print("</level2>");
                    out.print("<level2 name='operation 02'>");
                        out.print("<stats name='Requests' count='"+(randomGenerator.nextInt(30)+10)+"' />");
	                    out.print("<stats name='Faults' count='"+randomGenerator.nextInt(10)+"'/>");
                    out.print("</level2>");
                    out.print("<level2 name='operation 01'>");
                        out.print("<stats name='Requests' count='"+(randomGenerator.nextInt(30)+10)+"' />");
	                    out.print("<stats name='Faults' count='"+randomGenerator.nextInt(10)+"'/>");
                    out.print("</level2>");
	        out.print("</level1>");
        out.print("</level0>");
    }
    else if("op-https://10.100.1.119:8243/esb".equals(funcName)) {
        out.print("<level0 name='https://10.100.1.119:8243/esb'>");
            out.print("<stats name='Requests' count='"+(randomGenerator.nextInt(30)+10)+"' />");
	        out.print("<stats name='Faults' count='"+randomGenerator.nextInt(10)+"'/>");
	        out.print("<level1 name='endpoint 03'>");
                out.print("<stats name='Requests' count='"+(randomGenerator.nextInt(30)+10)+"' />");
		        out.print("<stats name='Faults' count='"+randomGenerator.nextInt(10)+"'/>");
                    out.print("<level2 name='operation 04'>");
                        out.print("<stats name='Requests' count='"+(randomGenerator.nextInt(30)+10)+"' />");
	                    out.print("<stats name='Faults' count='"+randomGenerator.nextInt(10)+"'/>");
                    out.print("</level2>");
                    out.print("<level2 name='operation 03'>");
                        out.print("<stats name='Requests' count='"+(randomGenerator.nextInt(30)+10)+"' />");
	                    out.print("<stats name='Faults' count='"+randomGenerator.nextInt(10)+"'/>");
                    out.print("</level2>");
                    out.print("<level2 name='operation 02'>");
                        out.print("<stats name='Requests' count='"+(randomGenerator.nextInt(30)+10)+"' />");
	                    out.print("<stats name='Faults' count='"+randomGenerator.nextInt(10)+"'/>");
                    out.print("</level2>");
                    out.print("<level2 name='operation 01'>");
                        out.print("<stats name='Requests' count='"+(randomGenerator.nextInt(30)+10)+"' />");
	                    out.print("<stats name='Faults' count='"+randomGenerator.nextInt(10)+"'/>");
                    out.print("</level2>");
	        out.print("</level1>");

            out.print("<level1 name='endpoint 02'>");
                out.print("<stats name='Requests' count='"+(randomGenerator.nextInt(30)+10)+"' />");
		        out.print("<stats name='Faults' count='"+randomGenerator.nextInt(10)+"'/>");
                    out.print("<level2 name='operation 04'>");
                        out.print("<stats name='Requests' count='"+(randomGenerator.nextInt(30)+10)+"' />");
	                    out.print("<stats name='Faults' count='"+randomGenerator.nextInt(10)+"'/>");
                    out.print("</level2>");
                    out.print("<level2 name='operation 03'>");
                        out.print("<stats name='Requests' count='"+(randomGenerator.nextInt(30)+10)+"' />");
	                    out.print("<stats name='Faults' count='"+randomGenerator.nextInt(10)+"'/>");
                    out.print("</level2>");
                    out.print("<level2 name='operation 02'>");
                        out.print("<stats name='Requests' count='"+(randomGenerator.nextInt(30)+10)+"' />");
	                    out.print("<stats name='Faults' count='"+randomGenerator.nextInt(10)+"'/>");
                    out.print("</level2>");
                    out.print("<level2 name='operation 01'>");
                        out.print("<stats name='Requests' count='"+(randomGenerator.nextInt(30)+10)+"' />");
	                    out.print("<stats name='Faults' count='"+randomGenerator.nextInt(10)+"'/>");
                    out.print("</level2>");
	        out.print("</level1>");

            out.print("<level1 name='endpoint 01'>");
                out.print("<stats name='Requests' count='"+(randomGenerator.nextInt(30)+10)+"' />");
		        out.print("<stats name='Faults' count='"+randomGenerator.nextInt(10)+"'/>");
                    out.print("<level2 name='operation 04'>");
                        out.print("<stats name='Requests' count='"+(randomGenerator.nextInt(30)+10)+"' />");
	                    out.print("<stats name='Faults' count='"+randomGenerator.nextInt(10)+"'/>");
                    out.print("</level2>");
                    out.print("<level2 name='operation 03'>");
                        out.print("<stats name='Requests' count='"+(randomGenerator.nextInt(30)+10)+"' />");
	                    out.print("<stats name='Faults' count='"+randomGenerator.nextInt(10)+"'/>");
                    out.print("</level2>");
                    out.print("<level2 name='operation 02'>");
                        out.print("<stats name='Requests' count='"+(randomGenerator.nextInt(30)+10)+"' />");
	                    out.print("<stats name='Faults' count='"+randomGenerator.nextInt(10)+"'/>");
                    out.print("</level2>");
                    out.print("<level2 name='operation 01'>");
                        out.print("<stats name='Requests' count='"+(randomGenerator.nextInt(30)+10)+"' />");
	                    out.print("<stats name='Faults' count='"+randomGenerator.nextInt(10)+"'/>");
                    out.print("</level2>");
	        out.print("</level1>");
        out.print("</level0>");
    }
    else if("op-https://10.100.1.245:8243/esb".equals(funcName)) {
        out.print("<level0 name='https://10.100.1.245:8243/esb'>");
            out.print("<stats name='Requests' count='"+(randomGenerator.nextInt(30)+10)+"' />");
	        out.print("<stats name='Faults' count='"+randomGenerator.nextInt(10)+"'/>");
            out.print("<level1 name='endpoint 04'>");
                out.print("<stats name='Requests' count='"+(randomGenerator.nextInt(30)+10)+"' />");
		        out.print("<stats name='Faults' count='"+randomGenerator.nextInt(10)+"'/>");
                    out.print("<level2 name='operation 04'>");
                        out.print("<stats name='Requests' count='"+(randomGenerator.nextInt(30)+10)+"' />");
	                    out.print("<stats name='Faults' count='"+randomGenerator.nextInt(10)+"'/>");
                    out.print("</level2>");
                    out.print("<level2 name='operation 03'>");
                        out.print("<stats name='Requests' count='"+(randomGenerator.nextInt(30)+10)+"' />");
	                    out.print("<stats name='Faults' count='"+randomGenerator.nextInt(10)+"'/>");
                    out.print("</level2>");
                    out.print("<level2 name='operation 02'>");
                        out.print("<stats name='Requests' count='"+(randomGenerator.nextInt(30)+10)+"' />");
	                    out.print("<stats name='Faults' count='"+randomGenerator.nextInt(10)+"'/>");
                    out.print("</level2>");
                    out.print("<level2 name='operation 01'>");
                        out.print("<stats name='Requests' count='"+(randomGenerator.nextInt(30)+10)+"' />");
	                    out.print("<stats name='Faults' count='"+randomGenerator.nextInt(10)+"'/>");
                    out.print("</level2>");
	        out.print("</level1>");

	        out.print("<level1 name='endpoint 03'>");
                out.print("<stats name='Requests' count='"+(randomGenerator.nextInt(30)+10)+"' />");
		        out.print("<stats name='Faults' count='"+randomGenerator.nextInt(10)+"'/>");
                    out.print("<level2 name='operation 04'>");
                        out.print("<stats name='Requests' count='"+(randomGenerator.nextInt(30)+10)+"' />");
	                    out.print("<stats name='Faults' count='"+randomGenerator.nextInt(10)+"'/>");
                    out.print("</level2>");
                    out.print("<level2 name='operation 03'>");
                        out.print("<stats name='Requests' count='"+(randomGenerator.nextInt(30)+10)+"' />");
	                    out.print("<stats name='Faults' count='"+randomGenerator.nextInt(10)+"'/>");
                    out.print("</level2>");
                    out.print("<level2 name='operation 02'>");
                        out.print("<stats name='Requests' count='"+(randomGenerator.nextInt(30)+10)+"' />");
	                    out.print("<stats name='Faults' count='"+randomGenerator.nextInt(10)+"'/>");
                    out.print("</level2>");
                    out.print("<level2 name='operation 01'>");
                        out.print("<stats name='Requests' count='"+(randomGenerator.nextInt(30)+10)+"' />");
	                    out.print("<stats name='Faults' count='"+randomGenerator.nextInt(10)+"'/>");
                    out.print("</level2>");
	        out.print("</level1>");

            out.print("<level1 name='endpoint 02'>");
                out.print("<stats name='Requests' count='"+(randomGenerator.nextInt(30)+10)+"' />");
		        out.print("<stats name='Faults' count='"+randomGenerator.nextInt(10)+"'/>");
                    out.print("<level2 name='operation 04'>");
                        out.print("<stats name='Requests' count='"+(randomGenerator.nextInt(30)+10)+"' />");
	                    out.print("<stats name='Faults' count='"+randomGenerator.nextInt(10)+"'/>");
                    out.print("</level2>");
                    out.print("<level2 name='operation 03'>");
                        out.print("<stats name='Requests' count='"+(randomGenerator.nextInt(30)+10)+"' />");
	                    out.print("<stats name='Faults' count='"+randomGenerator.nextInt(10)+"'/>");
                    out.print("</level2>");
                    out.print("<level2 name='operation 02'>");
                        out.print("<stats name='Requests' count='"+(randomGenerator.nextInt(30)+10)+"' />");
	                    out.print("<stats name='Faults' count='"+randomGenerator.nextInt(10)+"'/>");
                    out.print("</level2>");
                    out.print("<level2 name='operation 01'>");
                        out.print("<stats name='Requests' count='"+(randomGenerator.nextInt(30)+10)+"' />");
	                    out.print("<stats name='Faults' count='"+randomGenerator.nextInt(10)+"'/>");
                    out.print("</level2>");
	        out.print("</level1>");

            out.print("<level1 name='endpoint 01'>");
                out.print("<stats name='Requests' count='"+(randomGenerator.nextInt(30)+10)+"' />");
		        out.print("<stats name='Faults' count='"+randomGenerator.nextInt(10)+"'/>");
                    out.print("<level2 name='operation 04'>");
                        out.print("<stats name='Requests' count='"+(randomGenerator.nextInt(30)+10)+"' />");
	                    out.print("<stats name='Faults' count='"+randomGenerator.nextInt(10)+"'/>");
                    out.print("</level2>");
                    out.print("<level2 name='operation 03'>");
                        out.print("<stats name='Requests' count='"+(randomGenerator.nextInt(30)+10)+"' />");
	                    out.print("<stats name='Faults' count='"+randomGenerator.nextInt(10)+"'/>");
                    out.print("</level2>");
                    out.print("<level2 name='operation 02'>");
                        out.print("<stats name='Requests' count='"+(randomGenerator.nextInt(30)+10)+"' />");
	                    out.print("<stats name='Faults' count='"+randomGenerator.nextInt(10)+"'/>");
                    out.print("</level2>");
                    out.print("<level2 name='operation 01'>");
                        out.print("<stats name='Requests' count='"+(randomGenerator.nextInt(30)+10)+"' />");
	                    out.print("<stats name='Faults' count='"+randomGenerator.nextInt(10)+"'/>");
                    out.print("</level2>");
	        out.print("</level1>");
        out.print("</level0>");
    }
%>

