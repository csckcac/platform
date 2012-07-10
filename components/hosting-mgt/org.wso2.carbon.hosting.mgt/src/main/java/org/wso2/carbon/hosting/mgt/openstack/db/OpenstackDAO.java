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
import org.wso2.carbon.hosting.mgt.utils.PHPCartridgeConstants;

public class OpenstackDAO {

	private Connection con = null;
	private String driver = "com.mysql.jdbc.Driver";
	private static PreparedStatement pst = null;
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
	public List<String> readInstancesToDelete() {
		List<String> instanceListToBeDeleted = new ArrayList<String>();

		// TODO filter by account-type
		try {
			Class.forName(driver);
			con = DriverManager.getConnection(System.getProperty(PHPCartridgeConstants.OPENSTACK_DB_URL) 
					+ System.getProperty(PHPCartridgeConstants.OPENSTACK_DB_SCHEMA),
						System.getProperty(PHPCartridgeConstants.OPENSTACK_DB_USERNAME),
						System.getProperty(PHPCartridgeConstants.OPENSTACK_DB_PASSWORD));
			
			String ips = "SELECT address FROM floating_ips f where f.fixed_ip_id in " +
						"(SELECT id FROM fixed_ips f where instance_id in " +
						"(SELECT id FROM instances where datediff(curdate(), created_at) > "+ 
						System.getProperty(PHPCartridgeConstants.OPENSTACK_FREE_ACCOUNT_MAX_DAYS) +
						" and deleted = '0' and hostname like '%wso2-php-domain%') )";
			
			pst = con.prepareStatement(ips);
			rs = pst.executeQuery();

			while (rs.next()) {
				instanceListToBeDeleted.add(rs.getString("address"));
			}

		} catch (Exception e) {
			log.error(" Exception is occurred in accessing database. Reason:" + e.getMessage());
		} finally {
			
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
               log.error(" Exception is occurred in closing resources. Reason:" + ex.getMessage());
            }
		}
		
		return instanceListToBeDeleted;
	}
}
