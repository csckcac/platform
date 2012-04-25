package org.wso2.carbon.lb.common.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.lb.common.dto.Bridge;
import org.wso2.carbon.lb.common.dto.Container;
import org.wso2.carbon.lb.common.dto.ContainerInformation;
import org.wso2.carbon.lb.common.dto.HostMachine;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 *  This class handles the database access relevant to container data.
 *
 */
public class ContainerDAO extends AbstractDAO{
    protected Log log = LogFactory.getLog(ContainerDAO.class);
    Connection con = null;
        String url = "jdbc:mysql://localhost:3306/";
        String db = "hosting_mgt_db";
        String driver = "com.mysql.jdbc.Driver";
        String dbUsername = "root";
        String dbPassword = "root";
        Statement statement = null;

   /**
    *  This is for adding container details to database. This will be called after successfully
    *  creating a container in a host machine.
    *
    */
    public void create(Container container) throws SQLException {
        try{
            Class.forName(driver);
            con = DriverManager.getConnection(url + db, dbUsername, dbPassword);
            statement = con.createStatement();
            String sql = "INSERT INTO container VALUES('" + container.getContainerId() + "','"
                        + container.getType() + "','"
                        + container.getLabel() + "','" + container.getDescription() + "',"
                        + container.isStarted() + ",'" + container.getTenant() + "','"
                        + container.getContainerKeysFile() + "','" + container.getTemplate() + "','"
                        + container.getIp() + "','" + container.getBridge() + "')";

           statement.executeUpdate(sql);
       }catch (SQLException s){
           String msg = "Error while inserting container data" + s.getMessage();
           log.error(msg);
           throw new SQLException(s + msg);
       }catch (ClassNotFoundException s){
            String msg = "Error while sql connection :" + s.getMessage();
            log.error(msg);
            throw new SQLException(msg, s);
       }
       finally {
            try { if (statement != null) statement.close(); } catch(SQLException e) {}
            try { if (con != null) con.close(); } catch(Exception e) {}
       }
    }

    /**
     * This will delete the container from database. This will be called when the container is idle
     * for a particular time, so it need to recover resources of the machine.
     *
     * @param containerId
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public void delete(String containerId) throws SQLException {
        try{
            Class.forName(driver);
            con = DriverManager.getConnection(url + db, dbUsername, dbPassword);
            statement = con.createStatement();
            String sql = "DELETE FROM container WHERE container_id='" + containerId + "'";
            statement.executeUpdate(sql);
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
     * This is called to update the status of the container. We may make started = false if
     * the container is created but stoped after some time due to some reason.
     *
     * @param containerId
     * @param status
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public void changeState(String containerId, Boolean status)
            throws SQLException {
        try{
            Class.forName(driver);
           con = DriverManager.getConnection(url + db, dbUsername, dbPassword);
           statement = con.createStatement();
           String sql = "UPDATE container SET started="+ status +" WHERE container_id='"
                        + containerId+ "'";
           statement.executeUpdate(sql);
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
     * This method helps to identify the available host machine from the zone requested.
     * It will select the host machine which has least load at the moment. This method
     * is separated with the intention of isolating the algorithm of selection host machine. For
     * example instead of the least loaded host machine, we can select host machine in a round robin
     * way. Replacing this method with round robin logic will be enough to do the needful.
     *
     * @param domain name of the zone which search Host machine in.
     * @throws Exception related to database transactions
     */
    private HostMachine getAvailableHostMachine(String domain)
            throws SQLException {
        HostMachine hostMachine = new HostMachine();
        ResultSet resultSetForHM = null;
        Statement statementForBridge = null;
        Statement statementForContainer = null;
        ResultSet resultSetForBridge = null;
        ResultSet resultSetForContainer = null;
        try{
            Class.forName(driver);
            con = DriverManager.getConnection(url + db, dbUsername, dbPassword);
            statement = con.createStatement();

            String sqlDomain =  "SELECT zone FROM domain WHERE domain_name='" + domain + "'" ;
            //Here we get all the host machines that maps to zone
            resultSetForHM = statement.executeQuery(sqlDomain);
            resultSetForHM.next();
            String zone = resultSetForHM.getString("zone");
            String sql =  "SELECT * FROM host_machine WHERE zone='" + zone + "' AND available=true" ;
            //Here we get all the host machines that maps to zone
            resultSetForHM = statement.executeQuery(sql);
            int containerCountOfHostMachine;
            int minimumContainerCountHM = -1;
            while(resultSetForHM.next()){ //Iterate through worker nodes relevant to zone
                containerCountOfHostMachine = 0;
                String hostMachineEndPoint = resultSetForHM.getString("epr");
                Bridge bridges[] = new Bridge[1]; //Here we only get one available bridge for this
                // particular host machine. Therefore only one bridge is included in array
                bridges[0] = new Bridge();
                statementForBridge = con.createStatement();
                sql = "SELECT * FROM bridge WHERE host_machine='" + hostMachineEndPoint
                                          + "' AND available=true";
                resultSetForBridge = statementForBridge.executeQuery(sql);
                int minimumContainerCountBridge = -1;
                while(resultSetForBridge.next()){
                    statementForContainer = con.createStatement();
                    sql = "SELECT COUNT(bridge) FROM container WHERE bridge='"
                          + resultSetForBridge.getString("bridge_ip") + "'";
                    resultSetForContainer = statementForContainer.executeQuery(sql);
                    resultSetForContainer.next();
                    int containerCount = resultSetForContainer.getInt(1);
                    containerCountOfHostMachine += containerCount;
                    if(minimumContainerCountBridge == -1 || minimumContainerCountBridge > containerCount)
                    { //check if it's the first one or lower than before
                        minimumContainerCountBridge = containerCount;
                        bridges[0].setBridgeIp(resultSetForBridge.getString("bridge_ip"));
                        bridges[0].setNetGateway(resultSetForBridge.getString("net_gateway"));
                        bridges[0].setNetMask(resultSetForBridge.getString("net_mask"));
                    }
                }
                if(minimumContainerCountHM == -1 || minimumContainerCountHM > containerCountOfHostMachine)
                { //check if it's the first one or lower than before
                    minimumContainerCountHM = containerCountOfHostMachine;
                    hostMachine.setEpr(hostMachineEndPoint);
                    hostMachine.setContainerRoot(resultSetForHM.getString("container_root"));
                    hostMachine.setAvailable(true);
                    hostMachine.setIp(resultSetForHM.getString("ip"));
                    hostMachine.setZone(zone);
                    hostMachine.setBridges(bridges);
                }
            }
        }catch (SQLException s){
           String msg = "Error while retrieving container data" + s.getMessage();
           log.error(msg);
           throw new SQLException(s + msg);
        }
        catch (ClassNotFoundException s){
           String msg = "Error while sql connection :" + s.getMessage();
           log.error(msg);
           throw new SQLException(msg);
        }
        finally {
            try { if (resultSetForContainer != null) resultSetForContainer.close(); } catch(SQLException e) {}
            try { if (resultSetForBridge != null) resultSetForBridge.close(); } catch(SQLException e) {}
            try { if (resultSetForHM != null) resultSetForHM.close(); } catch(SQLException e) {}
            try { if (statement != null) statement.close(); } catch(SQLException e) {}
            try { if (statementForBridge != null) statementForBridge.close(); } catch(SQLException e) {}
            try { if (statementForContainer != null) statementForContainer.close(); } catch(SQLException e) {}
            try { if (con != null) con.close(); } catch(Exception e) {}
        }
        return hostMachine;
    }



    /**
     * This method will return the next available ip for the input bridge ip. For a particular bridge
     * there will be at-least one ip at the table. If there is only one ip, it is the available ip to
     * next container to be created and that will be returned. Because ips are filled up to that level
     * in the bridge. Then it should increase and put back to the table.
     * If there are more than one ips, we need to select second of the result set to
     * return and should be deleted as well. That's because we add un-used(ips of destroyed containers
     * at the end of this table with relevant bridge
     *
     * @param bridgeIp
     * @throws Exception
     */
    private String getAvailableIp(String bridgeIp) throws SQLException {
        String availableIp = null;
        ResultSet resultSet = null;
        try{
            Class.forName(driver);
            con = DriverManager.getConnection(url + db, dbUsername, dbPassword);
            statement = con.createStatement();
            String sql =  "SELECT * FROM available_ip WHERE bridge='" + bridgeIp + "'" ;
            //Here we have to get all the ips relevant to the bridge then select the ip according to
            // the algorithms described at method comment. BTW:Here we have to get all the ips to be
            // generic sql and hence we can't use LIMTI 2(LIMIT is mysql specific)
            resultSet = statement.executeQuery(sql);
            int ipCount = 0;
            while(resultSet.next()){
                availableIp = resultSet.getString("ip").trim();
                ipCount++;
                if(ipCount == 2){
                    sql =  "DELETE FROM available_ip WHERE ip='" + availableIp+ "'";
                    statement.executeUpdate(sql);
                    break;
                }
            }
            if(ipCount == 1){
                sql =  "UPDATE available_ip SET ip='"+ incrementIp(availableIp) +"' WHERE ip='"
                       + availableIp + "'";
                statement.executeUpdate(sql);
            }
        }catch (SQLException s){
            String msg = "Error while getting available ip " + s.getMessage();
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
        return availableIp;
    }


    /**
     * This method will return ContainerInformation object which has all the information for
     * creating the container physically. This method use 'getAvailableHostMachine' and 'getAvailableIp'
     * for information gathering.
     *
     * @param zone
     * @throws Exception
     */


    public ContainerInformation retrieveAvailableContainerInformation(String zone)
            throws SQLException {

        ContainerInformation containerInformation = new ContainerInformation();
        HostMachine hostMachine = getAvailableHostMachine(zone);
        containerInformation.setEpr(hostMachine.getEpr());
        Bridge[] bridges = hostMachine.getBridges();
        String bridgeIp = bridges[0].getBridgeIp();
        containerInformation.setContainerRoot(hostMachine.getContainerRoot());
        containerInformation.setBridge(bridgeIp);
        containerInformation.setIp(getAvailableIp(bridgeIp));
        containerInformation.setNetGateway(bridges[0].getNetGateway());
        containerInformation.setNetMask(bridges[0].getNetMask());
        containerInformation.setType("S");        //set a default for current implementation

        //Container type, container id can not be set from here. They will be set from adapter level.
        return containerInformation;
    }



}
