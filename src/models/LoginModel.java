package models;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import Dao.DBConnect;
import util.MD5Encript;

public class LoginModel extends DBConnect {
 
	private Boolean admin;
	private int id;
	private String role;
 
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public Boolean isAdmin() {
		return admin;
	}
	public String role() {
		return role;
	}
	public void setAdmin(Boolean admin) {
		this.admin = admin;
	}
		
	public Boolean getCredentials(String username, String password){
			password = MD5Encript.passwordEncript(password);
        	String query = "SELECT * FROM ms_users WHERE email = ? and password = ?;";
            try(PreparedStatement stmt = connection.prepareStatement(query)) {
               stmt.setString(1, username);
               stmt.setString(2, password);
               ResultSet rs = stmt.executeQuery();
                if(rs.next()) { 
                 
                	setId(rs.getInt("userId"));
                	setAdmin(rs.getString("role").equals("ADMIN"));
                	setRole(rs.getString("role"));
                	System.out.println("Admin : "+isAdmin());
                	return true;
               	}
             }catch (SQLException e) {
            	e.printStackTrace();   
             }
			return false;
    }

}//end class