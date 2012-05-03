
package org.wso2.carbon.lb.common.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.lb.common.dto.Bridge;
import org.wso2.carbon.lb.common.dto.HostMachine;
import org.wso2.carbon.lb.common.dto.Zone;
import org.wso2.carbon.lb.common.persistence.AgentPersistenceManager;

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

    //protected Log log = LogFactory.getLog(HostMachineDAO.class);
    private Connection con = null;
    private String url = "jdbc:mysql://localhost:3306/";
    private String db = "hosting_mgt_db";
    private String driver = "com.mysql.jdbc.Driver";
    private String dbUsername = "root";
    private String dbPassword = "root";
    private Statement statement = null;
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
     * creating the largest possible container. Also when the creation of container is failed, there
     * may be temporary issue in the host machine. So we make that host machine unavailable. Later if
     * we found that host machine which made unavailable is up, we make it available by passing true
     * to this method.
     * @param endPoint
     * @param availability true or false
     * @throws SQLException
     */
    public void changeAvailability(String endPoint, boolean availability) throws SQLException {
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


    public static void main(String[] args) throws SQLException {
        String epr = "epr4";
        String zoneName = "zone1";
              Bridge[] bridges = new Bridge[3];
              bridges[0] = new Bridge();
              bridges[1] = new Bridge();
              bridges[2] = new Bridge();

              bridges[0].setBridgeIp("168.192.1.0");
              bridges[0].setAvailable(true);
              bridges[0].setCurrentCountIps(0);
              bridges[0].setMaximumCountIps(100);
              bridges[0].setNetGateway("net_gateway");
              bridges[0].setNetMask("net_mask");
              bridges[0].setHostMachine(epr);
              

              bridges[1].setBridgeIp("168.192.2.0");
              bridges[1].setAvailable(true);
              bridges[1].setCurrentCountIps(0);
              bridges[1].setMaximumCountIps(100);
              bridges[1].setNetGateway("net_gateway");
              bridges[1].setNetMask("net_mask");
              bridges[1].setHostMachine(epr);

              bridges[2].setBridgeIp("168.192.3.0");
              bridges[2].setAvailable(true);
              bridges[2].setCurrentCountIps(0);
              bridges[2].setMaximumCountIps(100);
              bridges[2].setNetGateway("net_gateway");
              bridges[2].setNetMask("net_mask");
              bridges[2].setHostMachine(epr);


              HostMachine hostMachine = new HostMachine();
              hostMachine.setAvailable(true);
              hostMachine.setContainerRoot("ContainerRoot");
              hostMachine.setIp("ip");
              hostMachine.setZone(zoneName);
              hostMachine.setBridges(bridges);
              hostMachine.setEpr(epr);

              String[] domains = new String[2];
              domains[0] = "domian1";
              domains[1] = "domain2";

              Zone zone = new Zone();
              zone.setName(zoneName);
              zone.setAvailable(true);
              AgentPersistenceManager agentPersistenceManager = AgentPersistenceManager.getPersistenceManager();
              if (!agentPersistenceManager.isZoneExist(zone.getName())) {
                  String msg = "Zone does not exists ";
                  System.out.println(msg);
                  agentPersistenceManager.addZone(zone, domains);
              } else {
                  String msg = "Zone exist";
                  System.out.println(msg);
              }

        HostMachineDAO hostMachineDAO = new HostMachineDAO();
        hostMachineDAO.create(hostMachine, domains);
    }

}