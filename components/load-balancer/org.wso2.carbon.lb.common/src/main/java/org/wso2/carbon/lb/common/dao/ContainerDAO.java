package org.wso2.carbon.lb.common.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.lb.common.dto.Bridge;
import org.wso2.carbon.lb.common.dto.Container;
import org.wso2.carbon.lb.common.dto.WorkerNode;
import org.wso2.carbon.hosting.wnagent.stub.services.xsd.dto.ContainerInformation;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 *  This class handles the database access relevant to container data
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

    public void create(Container container) throws SQLException, ClassNotFoundException {
        try{
           con = DriverManager.getConnection(url + db, dbUsername, dbPassword);
           Class.forName(driver);
           statement = con.createStatement();
           String sql = "INSERT INTO container VALUES('" + container.getContainerId() + "','"
                        + container.getType() + "','"
                        + container.getLabel() + "','" + container.getDescription() + "',"
                        + container.isStarted() + ",'" + container.getTenant() + "','"
                        + container.getJailKeysFile() + "','" + container.getTemplate() + "','"
                        + container.getIp() + "','" + container.getBridge() + "')";

           statement.executeUpdate(sql);
       }catch (SQLException s){
           String msg = "Error while inserting container data" + s.getMessage();
           log.error(msg);
           throw new SQLException(s);
       }catch (ClassNotFoundException s){
           String msg = "Error while sql connection :" + s.getMessage();
           log.error(msg);
           throw new ClassNotFoundException(msg);
       }
       finally {
            try { if (statement != null) statement.close(); } catch(SQLException e) {}
            try { if (con != null) con.close(); } catch(Exception e) {}
       }
    }

    public void delete(String containerId) throws SQLException, ClassNotFoundException {
        try{
           con = DriverManager.getConnection(url + db, dbUsername, dbPassword);
           Class.forName(driver);
           statement = con.createStatement();
           String sql = "DELETE FROM container WHERE container_id='" + containerId + "'";

           statement.executeUpdate(sql);
       }catch (SQLException s){
           String msg = "Error while deleting container data" + s.getMessage();
           log.error(msg);
           throw new SQLException(s);
       }catch (ClassNotFoundException s){
           String msg = "Error while sql connection :" + s.getMessage();
           log.error(msg);
           throw new ClassNotFoundException(msg);
       }
       finally {
            try { if (statement != null) statement.close(); } catch(SQLException e) {}
            try { if (con != null) con.close(); } catch(Exception e) {}
       }
    }

    public void changeState(String containerId, Boolean status)
            throws SQLException, ClassNotFoundException {
        try{
           con = DriverManager.getConnection(url + db, dbUsername, dbPassword);
           Class.forName(driver);
           statement = con.createStatement();
           String sql = "UPDATE container SET started="+ status +" WHERE name='" + containerId+ "'";

           statement.executeUpdate(sql);
       }catch (SQLException s){
           String msg = "Error while deleting container data" + s.getMessage();
           log.error(msg);
           throw new SQLException(s);
       }catch (ClassNotFoundException s){
           String msg = "Error while sql connection :" + s.getMessage();
           log.error(msg);
           throw new ClassNotFoundException(msg);
       }
       finally {
            try { if (statement != null) statement.close(); } catch(SQLException e) {}
            try { if (con != null) con.close(); } catch(Exception e) {}
       }
    }



    /**
     * This method helps to identify the available worker node from the zone requested. This method
     * and 'retrieveAvailableContainerInformation' method are separated for distributing the complexity.
     * This will be called from retrieveAvailableContainerInformation method.
     *
     * @param zone
     * @throws Exception
     */
    public WorkerNode getAvailableWorkerNode(String zone)
            throws SQLException, ClassNotFoundException {
        WorkerNode workerNode = new WorkerNode();
        ResultSet resultSet = null;
        try{
            con = DriverManager.getConnection(url + db, dbUsername, dbPassword);
            Class.forName(driver);
            statement = con.createStatement();
            String sql =  "SELECT * FROM worker_node WHERE zone='" + zone + "' AND available=true" ;
            //Here we have to get all the worker nodes to be generic sql and hence we can't use
            // LIMTI 1(mysql specific)
            resultSet = statement.executeQuery(sql);
            int workerNodeCount = 0;
            boolean workerNodeFilled = false;
            while(resultSet.next()){
                workerNodeCount++;
                if(workerNodeCount == 1){
                    String workerNodeEndPoint = resultSet.getString("end_point");
                    workerNode.setContainerRoot(resultSet.getString("container_root"));
                    workerNode.setAvailable(true);
                    workerNode.setIp(resultSet.getString("ip"));
                    workerNode.setZone(zone);

                    Bridge bridges[] = new Bridge[1]; //Here we only get one available bridge for this
                    // particular worker node. Therefore only one bridge is included in array
                    bridges[0] = new Bridge();

                    sql = "SELECT * FROM bridge WHERE worker_node='" + workerNodeEndPoint
                          + "' AND available=true";
                    //Here we have to get all the bridges to be generic sql and hence we can't use
                    // LIMTI 1(mysql specific),

                    resultSet = statement.executeQuery(sql);
                    int bridgeCount = 0;
                    boolean bridgeFilled = false;
                    while(resultSet.next()){
                        bridgeCount++;
                        if (bridgeCount == 1){
                            bridges[0].setAvailable(true);
                            bridges[0].setBridgeIp(resultSet.getString("bridge_ip"));
                            bridges[0].setCurrentCountIps(resultSet.getInt("current_count_ips"));
                            bridges[0].setMaximumCountIps(resultSet.getInt("max_count_ips"));
                            bridges[0].setNetGateway(resultSet.getString("net_gateway"));
                            bridges[0].setNetMask(resultSet.getString("net_mask"));
                            bridges[0].setWorkerNode(workerNodeEndPoint);
                            workerNode.setBridges(bridges);
                            if(bridges[0].getCurrentCountIps() + 1 == bridges[0].getMaximumCountIps()){
                                bridgeFilled = true;
                                sql =  "UPDATE bridge SET available=false WHERE bridge_ip='"
                                       + bridges[0].getBridgeIp() + "'";
                                statement.executeUpdate(sql);
                                //make bridge unavailable
                            }
                        }
                    }
                    if(bridgeCount == 1 && bridgeFilled){
                        workerNodeFilled = true;
                        sql =  "UPDATE worker_node SET available=false WHERE name='"
                               + workerNodeEndPoint + "'";
                        statement.executeUpdate(sql);
                        //make worker node unavailable
                    }
                }
                if(workerNodeCount == 1 && workerNodeFilled){
                    sql =  "UPDATE zone_resource_plan SET available=false WHERE zone='" + zone + "'";
                    statement.executeUpdate(sql);
                    //make zone unavailable
                }
            }
        }catch (SQLException s){
           String msg = "Error while deleting container data" + s.getMessage();
           log.error(msg);
           throw new SQLException(s);
        }catch (ClassNotFoundException s){
           String msg = "Error while sql connection :" + s.getMessage();
           log.error(msg);
           throw new ClassNotFoundException(msg);
        }
        finally {
            try { if (statement != null) statement.close(); } catch(SQLException e) {}
            try { if (con != null) con.close(); } catch(Exception e) {}
            try { if (resultSet != null) resultSet.close(); } catch(Exception e) {}
        }
        return workerNode;
    }




    /**
     * This method will return the next available ip for the input bridge ip. For a particular bridge
     * there will be at-least one ip at the table. If there is only one ip, it is the available ip to
     * next container to be created and that will be returned. Then it should increase and put back
     * to the table. If there are more than one ips, we need to select second of the result set to
     * return and should be deleted as well.
     *
     * @param bridgeIp
     * @throws Exception
     */
    public String getAvailableIp(String bridgeIp) throws SQLException {
        String availableIp = null;
        ResultSet resultSet;
        try{
            con = DriverManager.getConnection(url + db, dbUsername, dbPassword);
            Class.forName(driver);
            statement = con.createStatement();
            String sql =  "SELECT * FROM available_ip WHERE bridge='" + bridgeIp + "'" ;
            //Here we have to get all the ips relevant to the bridge then select the ip according to
            // the algorithms described at method comment. BTW:Here we have to get all the ips to be
            // generic sql and hence we can't use LIMTI 2(mysql specific)
            resultSet = statement.executeQuery(sql);
            int ipCount = 0;
            while(resultSet.next()){
                availableIp = resultSet.getString("ip").trim();
                ipCount++;
                if(ipCount == 2){
                    System.out.println("two");
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
            throw new SQLException(s);
        }catch (ClassNotFoundException s){
            String msg = "Error while sql connection :" + s.getMessage();
            log.error(msg);
            throw new SQLException(s);
        }
        finally {
           statement.close();
           con.close();
        }

        return availableIp;
    }


    /**
     * This method will return ContainerInformation object which has all the information for
     * creating the container physically. This method use 'getAvailableWorkerNode' and 'getAvailableIp'
     * for information gathering.
     *
     * @param zone
     * @throws Exception
     */
    public ContainerInformation retrieveAvailableContainerInformation(String zone)
            throws ClassNotFoundException, SQLException {

        ContainerInformation containerInformation = new ContainerInformation();
        containerInformation.setZone(zone);
        WorkerNode workerNode = getAvailableWorkerNode(zone);
        Bridge[] bridges = workerNode.getBridges();
        Bridge bridge = bridges[0];
        String bridgeIp =bridge.getBridgeIp();
        containerInformation.setContainerRoot(workerNode.getContainerRoot());
        containerInformation.setBridge(bridgeIp);
        containerInformation.setIp(getAvailableIp(bridgeIp));
        return containerInformation;
    }
}
