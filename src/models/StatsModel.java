package models;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import Dao.DBConnect;

public class StatsModel extends DBConnect {



	public void callStoredProcedure(){
		try {
			
			System.out.println("calling stored procedure ms_InsertStats ...");
			CallableStatement cs = connection.prepareCall("{CALL ms_InsertStats()}");

			cs.executeUpdate();


		} catch (Exception e) {
			System.out.println(e);
		} 
	}


	public List<Alerts>  getAlerts() {

		List<Alerts> alerts = new ArrayList<>();
		ResultSet rs=null;
		// SELECT ALL FROM TICKETS TABLE
		try {
			// Execute a query
			System.out.println("select records from the table...");

			// Include all object data to the database table

			PreparedStatement stmt = connection.prepareStatement("SELECT alertId,alert,status,createdTimestamp FROM ms_alerts");

			rs=stmt.executeQuery();  

			while (rs.next()) {
				Alerts alert = new Alerts();

				// grab record data by table field name into user object
				alert.setId(rs.getString("alertId"));
				alert.setAlert(rs.getString("alert"));
				alert.setStatus(rs.getString("status"));
				alert.setCreatedTimestamp(rs.getString("createdTimestamp"));

				alerts.add(alert); // add tickets data to arraylist
			}

			System.out.println("Got tickets list");

		} catch (SQLException se) {
			se.printStackTrace();
		}
		return alerts;
	}

	public void  closeAlert(String id) {

		// update alert
		try {
			// Execute a query
			System.out.println("select records from the table...");

			// Include all object data to the database table

			PreparedStatement stmt = connection.prepareStatement("UPDATE ms_alerts SET status='CLOSED' WHERE alertId=?");
			stmt.setString(1, id);
			stmt.executeUpdate();


			System.out.println("CLosed Alert");

		} catch (SQLException se) {
			se.printStackTrace();
		}

	}

	// RETRIVE RECORDS FOR DISPLAY
	public ResultSet retrieveLiveStats() throws SQLException {
		ResultSet rs = null;

		Timestamp oneMinuteAgo = new Timestamp(System.currentTimeMillis() - (6 * 1000));
		System.out.println(oneMinuteAgo);

		try {
			System.out.println("Fetching records from the table to display...");

			PreparedStatement stmt = connection.prepareStatement("SELECT sum(newOrders),sum(failedOrders) from ms_stats where time_interval>=?");

			stmt.setTimestamp(1, oneMinuteAgo);
			rs = stmt.executeQuery();

		}catch (SQLException se) {
			se.printStackTrace();  
		}

		return rs;
	}

	// RETRIVE RECORDS FOR DISPLAY
	public ResultSet retrieveAnalyticsStats(Timestamp from, Timestamp to) throws SQLException {
		ResultSet rs = null;

		try {
			System.out.println("Fetching records from the table to display...");

			PreparedStatement stmt = connection.prepareStatement("SELECT sum(channelMobile),sum(channelDesktop),sum(channelStore), sum(payCreditCard),sum(payApplePay),sum(payCOD) from ms_stats where time_interval between ? and ?");

			stmt.setTimestamp(1, from);
			stmt.setTimestamp(2, to);
			rs = stmt.executeQuery();


		}catch (SQLException se) {
			se.printStackTrace();  
		}

		return rs;
	}

	// RETRIVE RECORDS FOR DISPLAY
	public void createOrderAlert(String issue, String status) throws SQLException {

		try {

			PreparedStatement stmt = connection.prepareStatement("INSERT into ms_alerts (alert,status) values (?,?)");

			stmt.setString(1, issue);
			stmt.setString(2, status);
			stmt.executeUpdate();


		}catch (SQLException se) {
			se.printStackTrace();  
		}
	}

}
