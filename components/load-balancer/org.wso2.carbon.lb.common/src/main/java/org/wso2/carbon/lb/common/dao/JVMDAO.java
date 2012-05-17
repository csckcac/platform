package org.wso2.carbon.lb.common.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

/**
 *
 *
 */
public class JVMDAO extends AbstractDAO{
    //protected Log log = LogFactory.getLog(ContainerDAO.class);
    private Connection con = null;
    private String url = "jdbc:mysql://localhost:3306/";
    private String db = "hosting_mgt_db";
    private String driver = "com.mysql.jdbc.Driver";
    private String dbUsername = "root";
    private String dbPassword = "root";
    private Statement statement = null;


    public boolean addToEprToInstanceCountTable(String epr, int instanceCount) throws SQLException {
        boolean successfullyAdded = false;
        try{
            Class.forName(driver);
            con = DriverManager.getConnection(url + db, dbUsername, dbPassword);
            statement = con.createStatement();
            String sql = "INSERT INTO epr_to_instance_count VALUES('" + epr +  "', '" + instanceCount + "')";
            statement.executeUpdate(sql);
            successfullyAdded = true;  //Adding instance details is succeeded
        }catch (SQLException s){
            String msg = "Error while inserting JVM instance data";
            log.error(msg + s.getMessage());
            throw new SQLException(s + msg);
        }catch (ClassNotFoundException s){
            String msg = "DB connection not successful !";
            log.error(msg);
            throw new SQLException(msg);
        }
        finally {
            try { if (statement != null) statement.close(); } catch(SQLException e) {}
            try { if (con != null) con.close(); } catch(Exception e) {}
        }
        return successfullyAdded;
    }


    public static void main(String[] args) throws SQLException {
        JVMDAO jvmdao = new JVMDAO();
        System.out.println(jvmdao.addToEprToInstanceCountTable("epr", 10));
    }
}
