package model;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

//this file is a POJO: Plain old java object
public class User {
	
	private static Connection getcon() throws SQLException, ClassNotFoundException {
		Class.forName("com.mysql.cj.jdbc.Driver");
		String jdbcurl = "jdbc:mysql://localhost:3306/DoubleHelix"; //3306 is port id of mysql
        Connection con = DriverManager.getConnection(jdbcurl,"root","Shivam@123");
        return con;
	}
	
	
	//add method 
	public static void add(String username,String password,String email) throws ClassNotFoundException, SQLException {
		Connection con=getcon();
        String query="insert into user(username,password,email) values(?,?,?)";
        PreparedStatement pstmt = con.prepareStatement(query);
        pstmt.setString(1, username);
        pstmt.setString(2, password);
        pstmt.setString(3, email);
        pstmt.executeUpdate();
	}
	
	public static boolean isValid(String username, String password) throws ClassNotFoundException, SQLException {
		Connection con=getcon();
        String query="select * from user";
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        boolean val=false;
        while(rs.next()) {
        	// System.out.println(rs.getString(2)+", "+rs.getString(3));
        	if((rs.getString(2)).compareToIgnoreCase(username)==0 && (rs.getString(3)).compareTo(password)==0) {
        		val=true;
        	}
        }
		return val;
	}
	
	public static Integer find(String username) throws SQLException, ClassNotFoundException {
		Integer userId=0;
		Connection con=getcon();
        String query="select * from user";
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        boolean val=false;
        while(rs.next()) {
        	if((rs.getString(2)).compareToIgnoreCase(username)==0) {
        		userId = rs.getInt(1);
        	}
        }
		return userId;
	}
	
	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		try{
			Connection con = getcon();
			String query="insert into user(username,password,email) values('shivam','12345','sid56rajawat')";
	        PreparedStatement pstmt = con.prepareStatement(query);
	        pstmt.executeUpdate();
		} catch(Exception e) {
			System.out.println(e);
		}
	}
}
