
package org.wso2.carbon.lb.common.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.lb.common.dto.Bridge;
import org.wso2.carbon.lb.common.dto.HostMachine;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

/**
 * This class handles the database access relevant to host machine data
 *
 */
public class HostMachineDAO extends AbstractDAO{

    protected Log log = LogFactory.getLog(HostMachineDAO.class);
    Connection con = null;
        String url = "jdbc:mysql://localhost:3306/";
        String db = "hosting_mgt_db";
        String driver = "com.mysql.jdbc.Driver";
        String dbUsername = "root";
        String dbPassword = "root";
        Statement statement = null;
/* Register a host machine when it is physically added to a zone. When a host machine is first
 * created it will first check whether the zone is already exist. If not this method will call
 * methods to register zone first.
*/

    public boolean create(HostMachine hostMachine, String[] domains) throws SQLException {
        boolean successfullyAdded = false;
        try{
            Class.forName(driver);
            con = DriverManager.getConnection(url + db, dbUsername, dbPassword);
            statement = con.createStatement();
            String sql = "INSERT INTO host_machine VALUES('" + hostMachine.getEpr() + "','"
                         + hostMachine.getIp()+ "',"
                         + hostMachine.isAvailable() + ",'" + hostMachine.getContainerRoot()+ "','"
                         + hostMachine.getZone() + "')";
            statement.executeUpdate(sql);
            Bridge[] bridges = hostMachine.getBridges();
            for (Bridge bridge : bridges) {
            //add all bridge details
                String sqlForBridge = "INSERT INTO bridge VALUES(" + bridge.isAvailable() + ",'"
                                      + bridge.getHostMachine() + "',"
                                      + bridge.getMaximumCountIps() + ","
                                      + bridge.getCurrentCountIps() + ",'"
                                      + bridge.getBridgeIp()+ "','"
                                      + bridge.getNetMask() + "','"
                                      + bridge.getNetGateway() + "')";
                statement.executeUpdate(sqlForBridge);
                //add available ip table details
                String sqlForStartingIp = "INSERT INTO available_ip (ip, bridge) VALUES('"
                                          + incrementIp(bridge.getBridgeIp()) + "','"
                                          + bridge.getBridgeIp() + "')";
                statement.executeUpdate(sqlForStartingIp);
            }
            successfullyAdded = true;
        }catch (SQLException s){
            String msg = "Error while inserting host machine data" + s.getMessage();
            log.error(msg);
            throw new SQLException(s + msg);
        }catch (ClassNotFoundException s){
            String msg = "Error while sql connection :" + s.getMessage();
            log.error(msg);
            throw new SQLException(msg);
        }
        finally {
            try { if (statement != null) statement.close(); } catch(SQLException e) {}
            try { if (con != null) con.close(); } catch(Exception e) {}
        }
        return successfullyAdded;
    }

    /**
     * Make the host machine unavailable when the resources of the host machine is not enough for
     * creating the largest possible container. This request comes if container creation call
     * to agent manager returns false.
     * @param endPoint
     * @throws SQLException
     */
    public void makeUnavailable(String endPoint) throws SQLException {
        try{
            Class.forName(driver);
            con = DriverManager.getConnection(url + db, dbUsername, dbPassword);
            statement = con.createStatement();
            String sql =  "UPDATE host_machine SET available=false WHERE epr='" + endPoint + "'";
                            statement.executeUpdate(sql);
                            //make host machine unavailable
        }catch (SQLException s){
           String msg = "Error while deleting container data" + s.getMessage();
           log.error(msg);
           throw new SQLException(s + msg);
        }catch (ClassNotFoundException s){
           String msg = "Error while sql connection :" + s.getMessage();
           log.error(msg);
           throw new SQLException(msg);
        }
        finally {
            try { if (statement != null) statement.close(); } catch(SQLException e) {}
            try { if (con != null) con.close(); } catch(Exception e) {}
        }
    }

    /**
     * Make the host machine available when the resources of the host machine is not enough for
     * creating the largest possible container. This request comes if container creation call
     * to agent manager returns false.
     * @param endPoint
     * @throws SQLException
     */
    public void makeAvailable(String endPoint) throws SQLException {
        try{
            Class.forName(driver);
            con = DriverManager.getConnection(url + db, dbUsername, dbPassword);
            statement = con.createStatement();
            String sql =  "UPDATE host_machine SET available=false WHERE epr='" + endPoint + "'";
                            statement.executeUpdate(sql);
                            //make host machine unavailable
        }catch (SQLException s){
           String msg = "Error while deleting container data" + s.getMessage();
           log.error(msg);
           throw new SQLException(s + msg);
        }catch (ClassNotFoundException s){
           String msg = "Error while sql connection :" + s.getMessage();
           log.error(msg);
           throw new SQLException(msg);
        }
        finally {
            try { if (statement != null) statement.close(); } catch(SQLException e) {}
            try { if (con != null) con.close(); } catch(Exception e) {}
        }
    }

    /**
     * This returns true if the input epr is exist in the database. This will be called to check if
     * the input epr is already there before inserting a new host machine.
     * @param endPoint
     * @return
     * @throws SQLException
     */
    public boolean isHostMachineExist(String endPoint) throws SQLException {
        boolean isExist = false;
        ResultSet resultSet = null;
        try{
            Class.forName(driver);
            con = DriverManager.getConnection(url + db, dbUsername, dbPassword);
            statement = con.createStatement();
            Statement statement = con.createStatement();
            String sql =  "SELECT 1 FROM host_machine WHERE epr='" + endPoint + "'";
            //return a result if there is a record
            resultSet = statement.executeQuery(sql);
            isExist = resultSet.next();
        }catch (SQLException s){
            String msg = "SQL statement is not executed for host machine exist !";
            log.error(msg);
            throw new SQLException(s + msg);
        }catch (ClassNotFoundException s){
            String msg = "DB connection not successful !";
            log.error(msg);
            throw new SQLException(msg);
        }
        finally {
            try { if (resultSet != null) resultSet.close(); } catch(Exception e) {}
            try { if (statement != null) statement.close(); } catch(SQLException e) {}
            try { if (con != null) con.close(); } catch(Exception e) {}
        }
        return isExist;
    }

    /**
     * This is to check whether there are any available host machines in input domain and will be
     * called when container creation request come.
     * @param domain
     * @return
     * @throws SQLException
     */
    public boolean isAvailableInDomain(String domain)
            throws SQLException {
        boolean isAvailable = false;
        ResultSet resultSet = null;
        try{
            Class.forName(driver);
            con = DriverManager.getConnection(url + db, dbUsername, dbPassword);
            statement = con.createStatement();
            Statement statement = con.createStatement();
            String sql =  "SELECT zone FROM domain WHERE domain_name='" + domain + "'";
            resultSet = statement.executeQuery(sql);
            if(resultSet.next()){ //get the first result as only one zone is per domain
                String zone = resultSet.getString("zone");
                sql =  "SELECT 1 FROM host_machine WHERE zone='" + zone + "'";
                resultSet = statement.executeQuery(sql);
                isAvailable = resultSet.next();
            }
        }catch (SQLException s){
            String msg = "SQL statement is not executed for host machine exist !";
            log.error(msg);
            throw new SQLException(s + msg);
        }catch (ClassNotFoundException s){
            String msg = "DB connection not successful !";
            log.error(msg);
            throw new SQLException(msg);
        }
        finally {
            try { if (resultSet != null) resultSet.close(); } catch(Exception e) {}
            try { if (statement != null) statement.close(); } catch(SQLException e) {}
            try { if (con != null) con.close(); } catch(Exception e) {}
        }
        return isAvailable;
    }

    /**
     * This method will delete host machine from database when host machine is removed from the zone.
     * (Host machine is no longer available for any operation)
     * @param epr
     * @return
     * @throws SQLException
     */
    public boolean delete(String epr) throws SQLException {
        boolean successfullyDeleted = false;
        try{
           Class.forName(driver);
           con = DriverManager.getConnection(url + db, dbUsername, dbPassword);
           statement = con.createStatement();
           String sql = "DELETE FROM host_machine WHERE epr='" + epr + "'";

           statement.executeUpdate(sql);
            successfullyDeleted = true;
       }catch (SQLException s){
           String msg = "Error while deleting host machine data " + s.getMessage();
           log.error(msg);
           throw new SQLException(s + msg);
       }catch (ClassNotFoundException s){
           String msg = "Error while sql connection :" + s.getMessage();
           log.error(msg);
           throw new SQLException(msg);
       }
       finally {
            try { if (statement != null) statement.close(); } catch(SQLException e) {}
            try { if (con != null) con.close(); } catch(Exception e) {}
       }
        return successfullyDeleted;
    }

    /**
     * This will be called form LXC adapter for recovery of
     * @return Map which include all the container ids as key, and relevant host machine eprs as value
     * @throws SQLException
     */
    public HashMap<String, String> getContainerIdToAgentMap() throws SQLException {
        HashMap containerToAgentMap = new HashMap<String, String>();
        ResultSet resultSetForContainer = null;
        ResultSet resultSetForBridge = null;
        Statement statementForBridge = null;
        try{
            Class.forName(driver);
            con = DriverManager.getConnection(url + db, dbUsername, dbPassword);
            statement = con.createStatement();
            String sql =  "SELECT container_id, bridge FROM container" ;
            resultSetForContainer = statement.executeQuery(sql);
            while(resultSetForContainer.next()){
                String containerId = resultSetForContainer.getString("container_id");
                String  bridgeIp = resultSetForContainer.getString("bridge");
                statementForBridge = con.createStatement();
                sql =  "SELECT host_machine FROM bridge WHERE bridge_ip='" + bridgeIp +"'"  ;
                resultSetForBridge = statementForBridge.executeQuery(sql);
                String hostMachineEpr = null;
                if(resultSetForBridge.next()){
                    hostMachineEpr = resultSetForBridge.getString("host_machine");
                }
                containerToAgentMap.put(containerId, hostMachineEpr);
            }
        }catch (SQLException s){
            String msg = "Error while getting container id to agent map " + s.getMessage();
            log.error(msg);
            throw new SQLException(s + msg);
        }catch (ClassNotFoundException s){
            String msg = "Error while sql connection :" + s.getMessage();
            log.error(msg);
            throw new SQLException(msg);
        }
        finally {
            try { if (resultSetForBridge != null) resultSetForBridge.close(); } catch(Exception e) {}
            try { if (resultSetForContainer != null) resultSetForContainer.close(); } catch(Exception e) {}
            try { if (statement != null) statement.close(); } catch(SQLException e) {}
            try { if (con != null) con.close(); } catch(Exception e) {}
        }
        return containerToAgentMap;
    }

    /**
     *
     * @return Map which include all the host machine(agent) epr as key, and relevant container roots as value
     * @throws SQLException
     */
    public HashMap<String, String> getAgentToContainerRootMap() throws SQLException {
        HashMap eprToContainerRootMap = new HashMap<String, String>();
        ResultSet resultSet = null;
        try{
            Class.forName(driver);
            con = DriverManager.getConnection(url + db, dbUsername, dbPassword);
            statement = con.createStatement();
            String sql =  "SELECT epr, container_root FROM host_machine" ;
            resultSet = statement.executeQuery(sql);
            while(resultSet.next()){
                String epr = resultSet.getString("epr");
                String  containerRoot = resultSet.getString("container_root");
                eprToContainerRootMap.put(epr, containerRoot);
            }
        }catch (SQLException s){
            String msg = "Error while getting container id to agent map " + s.getMessage();
            log.error(msg);
            throw new SQLException(s + msg);
        }catch (ClassNotFoundException s){
            String msg = "Error while sql connection :" + s.getMessage();
            log.error(msg);
            throw new SQLException(msg);
        }
        finally {
            try { if (resultSet != null) resultSet.close(); } catch(Exception e) {}
            try { if (statement != null) statement.close(); } catch(SQLException e) {}
            try { if (con != null) con.close(); } catch(Exception e) {}
        }
        return eprToContainerRootMap;
    }

}