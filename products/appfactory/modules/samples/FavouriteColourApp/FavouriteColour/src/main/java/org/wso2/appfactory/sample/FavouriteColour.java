package org.wso2.appfactory.sample;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class FavouriteColour {

	public String getColour(String person) {
		String result = null;
		DataSource ds = null;
		try {
			ds = (DataSource) InitialContext.doLookup("jdbc/colourApp");
		} catch (NamingException e) {
			e.printStackTrace();
		}
		Connection dbConnection = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			dbConnection = ds.getConnection();
			prepStmt = dbConnection
					.prepareStatement("SELECT colour FROM colour where person=?");
			prepStmt.setString(1, person);
			rs = prepStmt.executeQuery();
			if (rs.next()) {
				result = Float.toString(rs.getFloat("colour"));
			}
			dbConnection.close();
			prepStmt.close();
			rs.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return result;

	}

	public String setColourColour(String person, String colour) {
		String result = null;
		DataSource ds = null;
		try {
			ds = (DataSource) InitialContext.doLookup("jdbc/colourApp");
		} catch (NamingException e) {
			e.printStackTrace();
		}
		Connection dbConnection = null;
		PreparedStatement prepStmt = null;

		try {
			dbConnection = ds.getConnection();
			prepStmt = dbConnection
					.prepareStatement("INSERT INTO colour (person, colour) "
							+ "VALUES (?, ?)");
			prepStmt.executeUpdate();
			dbConnection.close();
			prepStmt.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return result;

	}

}