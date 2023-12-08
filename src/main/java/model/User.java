package model;


import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.X509EncodedKeySpec;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

//this file is a POJO: Plain old java object
public class User {
	
	private static Connection getcon() throws SQLException, ClassNotFoundException {
		Class.forName("com.mysql.cj.jdbc.Driver");
		String jdbcurl = "jdbc:mysql://localhost:3306/DoubleHelix"; //3306 is port id of mysql
        Connection con = DriverManager.getConnection(jdbcurl,"root","Shivam@123");
        return con;
	}
	
	
	//add method 
	public static void add(String username,String password,String email) throws ClassNotFoundException, SQLException, NoSuchAlgorithmException {
		Connection con=getcon();
        String query="insert into user(username,password,email) values(?,?,?)";
        PreparedStatement pstmt = con.prepareStatement(query);
        pstmt.setString(1, username);
        pstmt.setString(2, password);
        pstmt.setString(3, email);
        pstmt.executeUpdate();
        
        Integer userId = find(username);
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(128);
        SecretKey secretKey = keyGenerator.generateKey();
        
        String insertQuery = "INSERT INTO kms(userId, secretKey) VALUES (?, ?)";
        pstmt = con.prepareStatement(insertQuery);
        pstmt.setInt(1, userId);
        pstmt.setBytes(2, secretKey.getEncoded());
        pstmt.executeUpdate();
	}
	
	public static Boolean test() throws Exception {
		Connection con=getcon();
        String query="select secretKey from kms";
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        byte[] secretKeyBytes = null;
        if(rs.next()) {
        	secretKeyBytes = rs.getBytes(1);
        }
        SecretKey secretKey = new SecretKeySpec(secretKeyBytes, 0, secretKeyBytes.length, "AES");
        String encryptedTest = encrypt("test",secretKey);
        System.out.println(encryptedTest);
        System.out.println(decrypt(encryptedTest,secretKey));
        return "test".compareToIgnoreCase(decrypt(encryptedTest,secretKey)) == 0;
	}
	
	private static String encrypt(String text, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        byte[] encryptedBytes = cipher.doFinal(text.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }
	
	private static String decrypt(String encryptedText, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedText);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

        return new String(decryptedBytes);
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
	
	public static String getPassword(String username) throws ClassNotFoundException, SQLException {
		Connection con=getcon();
        String query="select * from user";
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(query);

        while(rs.next()) {
        	if((rs.getString(2)).compareToIgnoreCase(username)==0) {
        		return rs.getString(3);
        	}
        }
        return "";
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
			add("Shivam","1234","sid56rajawat@gmail.com");
	        if(test()) {
				System.out.println("Test passed successfully");
			}
			else {
				System.out.println("lmao ded");
			}
		} catch(Exception e) {
			System.out.println(e);
		}
	}
}
