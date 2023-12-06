package model;


import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.mysql.cj.jdbc.Blob;

//this file is a POJO: Plain old java object
public class File {
	
	private static Connection getcon() throws SQLException, ClassNotFoundException {
		Class.forName("com.mysql.cj.jdbc.Driver");
		String jdbcurl = "jdbc:mysql://localhost:3306/DoubleHelix"; //3306 is port id of mysql
        Connection con = DriverManager.getConnection(jdbcurl,"root","Shivam@123");
        return con;
	}
	
	
	//add method 
	public static void upload(String fileName, InputStream file_data, Integer owner) throws ClassNotFoundException, SQLException {
		Connection con=getcon();
        String query="insert into files(filename, file_data, owner) values(?,?,?)";
        PreparedStatement pstmt = con.prepareStatement(query);
        pstmt.setString(1, fileName);
        pstmt.setBlob(2, file_data);
        pstmt.setInt(3, owner);
        pstmt.executeUpdate();
	}
	
	public static InputStream download(String fileName, Integer owner) throws ClassNotFoundException, SQLException {
		Connection con=getcon();
		String query = "SELECT file_data FROM files WHERE fileName LIKE ? AND owner = ?";
        PreparedStatement pstmt = con.prepareStatement(query);
        
        pstmt.setString(1, fileName);
        pstmt.setInt(2, owner);
        
        ResultSet rs = pstmt.executeQuery();
        
        if(rs.next()) {
        	// System.out.println("reading file "+fileName);
        	java.sql.Blob blob = rs.getBlob(1);
        	return blob.getBinaryStream();
        }
        return null;
	}
	
	public static String[] getUserFileNames(Integer userId) throws SQLException, ClassNotFoundException {
		ArrayList<String> fileNames = new ArrayList<>();
		Connection con=getcon();
        String query="select fileName, owner from files";
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(query);

        while(rs.next()) {
        	if(rs.getInt(2) == userId) {
        		fileNames.add(rs.getString(1));
        	}
        }
		return fileNames.toArray(new String[0]);
	}
	
	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		
	}
}
