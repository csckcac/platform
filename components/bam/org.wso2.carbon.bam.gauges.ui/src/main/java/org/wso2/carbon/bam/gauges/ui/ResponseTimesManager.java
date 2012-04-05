package org.wso2.carbon.bam.gauges.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.bam.stub.statquery.Count;
import org.wso2.carbon.bam.util.BAMException;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.net.SocketException;

/**
 * used to process time related data only for gauges
 */
public class ResponseTimesManager {

    private static final Log log = LogFactory.getLog(ResponseTimesManager.class);
    BAMStatQueryDSClient bamDSClient;

    public ResponseTimesManager(ServletConfig config, HttpSession session,
                                HttpServletRequest request)
            throws AxisFault, SocketException {

        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        bamDSClient = new BAMStatQueryDSClient(cookie, backendServerURL, configContext, request.getLocale());

    }

    public String getAvgResponseTime(int serviceID) {
        String value;
        String range = "1000";
        try {
            // serverUrl = new String(Hex.decodeHex(serverUrl.toCharArray()));
            value = bamDSClient.getAvgResponseTime(serviceID);
            if (value == null) {
                value = "0";
            } else {
                int dVal = (int) Double.parseDouble(value);

                // Correcting extreme values.
                if (dVal < 10) {
                    range = "10";
                } else if (dVal < 100) {
                    range = "100";
                } else if (dVal < 1000) {
                    range = "1000";
                } else if (dVal < 0) {
                    dVal = 0;
                } else {
                    dVal = 1000;
                    range = "1000";
                }
                value = "" + dVal;
            }
        } catch (Exception e) {
            log.debug(e);
            value = "0";
        }

        return "&value=" + value + "&range=" + range;
    }

    public String getMinResponseTimeSystem(int serverID) {
        String value = "0";
        try {
            // serverUrl = new String(Hex.decodeHex(serverUrl.toCharArray()));
            // value = bamDSClient.getMinResponseTimeSystem(serverID);
            value = "0";
            if (value == null) {
                value = "0";
            } else {
                int dVal = (int) Double.parseDouble(value);

                // Correcting extreme values.
                if (dVal > 1000) {
                    dVal = 1000;
                } else if (dVal < 0) {
                    dVal = 0;
                }

                value = "" + dVal;
            }
        } catch (Exception e) {
            log.debug(e);
            value = "0";
        }
        return value;
    }

    public String getMaxResponseTimeSystem(String serverUrl) {
        String value = "0";
        try {
            // serverUrl = new String(Hex.decodeHex(serverUrl.toCharArray()));
            // value = bamDSClient.getMaxResponseTimeSystem(serverUrl);
            value = "0";
            if (value == null) {
                value = "0";
            } else {
                int dVal = (int) Double.parseDouble(value);

                // Correcting extreme values.
                if (dVal > 1000) {
                    dVal = 1000;
                } else if (dVal < 0) {
                    dVal = 0;
                }

                value = "" + dVal;
            }
        } catch (Exception e) {
            log.debug(e);
            value = "0";
        }

        return value;
    }

    public String getAvgResponseTimeSystem(String serverUrl) {

        String value = "0";
        try {
            // serverUrl = new String(Hex.decodeHex(serverUrl.toCharArray()));
            // value = bamDSClient.getAvgResponseTimeSystem(serverUrl);
            value = "0";
            if (value == null) {
                value = "0";
            } else {
                int dVal = (int) Double.parseDouble(value);

                // Correcting extreme values.
                if (dVal > 1000) {
                    dVal = 1000;
                } else if (dVal < 0) {
                    dVal = 0;
                }

                value = "" + dVal;
            }
        } catch (Exception e) {
            log.debug(e);
            value = "0";
        }

        return value;
    }

    public String getAvgResponseTimeSystem() {
        String resp = "100";
        return resp;
    }

    public String getMaxResponseTime(int serviceID) {
        String value = "0";
        try {
            // serverUrl = new String(Hex.decodeHex(serverUrl.toCharArray()));
            value = bamDSClient.getMaxResponseTime(serviceID);

            if (value == null) {
                value = "0";
            } else {
                int dVal = (int) Double.parseDouble(value);

                // Correcting extreme values.
                if (dVal > 1000) {
                    dVal = 1000;
                } else if (dVal < 0) {
                    dVal = 0;
                }

                value = "" + dVal;
            }
        } catch (Exception e) {
            log.debug(e);
            value = "0";
        }
        return value;
    }

    public String getMinResponseTime(int serviceID) {
        String value = "0";
        try {
            // serverUrl = new String(Hex.decodeHex(serverUrl.toCharArray()));
            value = bamDSClient.getMinResponseTime(serviceID);

            if (value == null) {
                value = "0";
            } else {
                int dVal = (int) Double.parseDouble(value);

                // Correcting extreme values.
                if (dVal > 1000) {
                    dVal = 1000;
                } else if (dVal < 0) {
                    dVal = 0;
                }

                value = "" + dVal;
            }
        } catch (Exception e) {
            log.debug(e);
            value = "0";
        }
        return value;
    }

    public String getLastMinuteRequestCount(int serviceID) {
        String resp = "";
        try {
            // serverUrl = new String(Hex.decodeHex(serverUrl.toCharArray()));
            Count[] response = bamDSClient.getLastMinuteRequestCount(serviceID);
            if (response != null) {
                return response[0].getCount();
            }
        } catch (Exception e) {
            log.debug(e);
        }

        return "0";
    }

    public String getMinMaxAverageRespTimesService(int serviceID) throws BAMException {
        String minVal = "0";
        String maxVal = "0";
        String avgVal = "0";

        minVal = bamDSClient.getMinResponseTime(serviceID);
        maxVal = bamDSClient.getMaxResponseTime(serviceID);
        avgVal = bamDSClient.getAvgResponseTime(serviceID);

        return minVal + "&" + maxVal + "&" + avgVal;
    }


}
