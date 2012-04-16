package org.wso2.carbon.lb.common.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 *
 */
public abstract class AbstractDAO {

    protected Log log = LogFactory.getLog(AbstractDAO.class);
    /**
     * This method is for handling string - int conversions. Ip is in String and when last part needs
     * to be increased by one digit this method will be called.
     * @param ip
     */
    protected String incrementIp(String ip){
        String incrementedIp = null;
        try{
            int lastIpPart = Integer.parseInt(ip.substring(ip.lastIndexOf(".") + 1));
            lastIpPart++;
            incrementedIp = ip.substring(0, ip.lastIndexOf(".") + 1) + lastIpPart;
        }catch (NumberFormatException e){
            e.printStackTrace();
            log.error(e.getMessage());
        }
        return incrementedIp;
    }

}
