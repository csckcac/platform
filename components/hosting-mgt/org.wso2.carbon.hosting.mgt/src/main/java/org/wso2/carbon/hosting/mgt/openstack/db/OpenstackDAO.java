package org.wso2.carbon.hosting.mgt.openstack.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.hosting.mgt.utils.CartridgeConstants;

public class OpenstackDAO {

	private Connection con = null;
	private String driver = "com.mysql.jdbc.Driver";
	private PreparedStatement pst = null;
	private ResultSet rs = null;
	
    private static final Log log = LogFactory.getLog(OpenstackDAO.class);

	/**
	 * 
	 * 
	 * @return List of  addresses of instances that needs to be deleted because of expiring
	 * its validity period
	 * 
	 * This is applicable only for "free" account types
	 * 
	 */
    
    public OpenstackDAO() {
		init();
	}
    
    
	private void init() {
		try {
		Class.forName(driver);
		con = DriverManager.getConnection(System.getProperty(CartridgeConstants.OPENSTACK_DB_URL)
				+ System.getProperty(CartridgeConstants.OPENSTACK_DB_SCHEMA),
					System.getProperty(CartridgeConstants.OPENSTACK_DB_USERNAME),
					System.getProperty(CartridgeConstants.OPENSTACK_DB_PASSWORD));
		} catch (Exception e) {
			logDBAccessException(e);
		}
	}


	private void logDBAccessException(Exception e) {
		log.error(" Exception is occurred in accessing database. Reason:" + e.getMessage());
	}


	public List<String> readInstancesToDelete() {
		List<String> instanceListToBeDeleted = new ArrayList<String>();

		// TODO filter by account-type
		try {
			String selectIpQuery =
						"SELECT address FROM fixed_ips f where instance_id in " +
						"(SELECT id FROM instances where datediff(curdate(), created_at) > "+ 
						System.getProperty(CartridgeConstants.OPENSTACK_FREE_ACCOUNT_MAX_DAYS) +
						" and deleted = '0' and hostname like '%wso2-php-domain%')";
			
			pst = con.prepareStatement(selectIpQuery);
			rs = pst.executeQuery();

			while (rs.next()) {
				instanceListToBeDeleted.add(rs.getString("address"));
			}

		} catch (Exception e) {
			logDBAccessException(e);
		} finally {			
			doCleanupResources();
		}
		
		return instanceListToBeDeleted;
	}


	/**
	 * This returns the public IP correspond to the given private IP of the 
	 * Openstack instance. 
	 * 
	 * @param privateIp
	 * @return publicIp
	 */
	public String getPublicIp(String privateIp) {
		String publicIp = null;
		
		String publicIpQuery = "SELECT address FROM floating_ips where fixed_ip_id in " +
							   "(SELECT id FROM fixed_ips where address = '"+privateIp+"' and deleted = '0') and deleted = '0'";
		
		try {
			pst = con.prepareStatement(publicIpQuery);
			rs = pst.executeQuery();

			while (rs.next()) {
				publicIp = rs.getString("address");
			}
		} catch (Exception e) {
			logDBAccessException(e);
		} finally {
			doCleanupResources();
		}
		
		return publicIp;
	}
	
	/**
	 * Database connection resources are cleaned up
	 */
	private void doCleanupResources() {
		try {
		    if (rs != null) {
		        rs.close();
		    }
		    if (pst != null) {
		        pst.close();
		    }
		    if (con != null) {
		        con.close();
		    }

		} catch (SQLException ex) {
			logDBAccessException(ex);
		}
	}
}
