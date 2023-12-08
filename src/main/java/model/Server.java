package model;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Base64;

import javax.crypto.Cipher;

import java.io.InputStream;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import com.mysql.cj.jdbc.Blob;

//this file is a POJO: Plain old java object
public class Server {
	
	private static Connection getcon() throws SQLException, ClassNotFoundException {
		Class.forName("com.mysql.cj.jdbc.Driver");
		String jdbcurl = "jdbc:mysql://localhost:3306/Cloud"; //3306 is port id of mysql
        Connection con = DriverManager.getConnection(jdbcurl,"root","Shivam@123");
        return con;
	}
	
	private static String encrypt(String text, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        byte[] encryptedBytes = cipher.doFinal(text.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }
	
	private static String decrypt(String encryptedText, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedText);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

        return new String(decryptedBytes);
    }
	
	//add method 
	public static void add(Integer serverId, byte[] publicKey,byte[] privateKey) throws ClassNotFoundException, SQLException {
		Connection con=getcon();
        String query="insert into servers(serverId,publicKey,privateKey) values(?,?,?)";
        PreparedStatement pstmt = con.prepareStatement(query);
        pstmt.setInt(1, serverId);
        pstmt.setBytes(2, publicKey);
        pstmt.setBytes(3, privateKey);
        pstmt.executeUpdate();
	}
	
	public static Boolean test() throws Exception {
		Connection con = getcon();
		String query = "SELECT publicKey, privateKey from servers where serverId=1";
		Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        byte[] publicKeyBytes = null, privateKeyBytes = null;
        if(rs.next()) {
        	publicKeyBytes = rs.getBytes(1);
        	privateKeyBytes = rs.getBytes(2);
        }
        PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(publicKeyBytes));
        PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
        
        String encryptedTest = encrypt("test", publicKey);
        System.out.println(encryptedTest);
        System.out.println(decrypt(encryptedTest,privateKey));
        return "test".compareToIgnoreCase(decrypt(encryptedTest,privateKey)) == 0;
	}
	
	 private static KeyPair generateRSAKeyPair() throws NoSuchAlgorithmException {
	    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
	    keyPairGenerator.initialize(1024); // You can use 1024, 2048, or 3072 bits
	    return keyPairGenerator.generateKeyPair();
	 }
	 
	 public static void uploadToServer(InputStream data, String identifier, Integer serverId) throws Exception {
		 // getting public key of server
		 Connection con = getcon();
		 String query = "SELECT publicKey from servers where serverId=?";
		 PreparedStatement pstmt = con.prepareStatement(query);
		 pstmt.setInt(1, serverId);
	     ResultSet rs = pstmt.executeQuery();
	     rs.next();
	     byte[] publicKeyBytes = rs.getBytes(1);

	     PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(publicKeyBytes));
	     
	     // encrypting identifier and storing it on the server
	     String encryptedIdentifier = encrypt(identifier,publicKey);
		 query = "insert into server_?(identifier,data) values (?,?)";
		 pstmt = con.prepareStatement(query);
		 pstmt.setInt(1, serverId);
		 pstmt.setString(2, encryptedIdentifier);
		 pstmt.setBlob(3, data);
		 pstmt.executeUpdate();
		 System.out.println("Block stored in server_"+serverId);
	 }
	
	 public static InputStream downloadFromServer(Integer serverId, String identifier) throws Exception {
		// getting private key of server
		Connection con = getcon();
		String query = "SELECT privateKey from servers where serverId=?";
		PreparedStatement pstmt = con.prepareStatement(query);
		pstmt.setInt(1, serverId);
		ResultSet rs = pstmt.executeQuery();
		rs.next();
		byte[] privateKeyBytes = rs.getBytes(1);
		PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
			     
		// searching for block in server
		con = getcon();
		query = "SELECT identifier,data FROM server_?";
		pstmt = con.prepareStatement(query);
		pstmt.setInt(1,serverId);
		rs = pstmt.executeQuery();
		while(rs.next()) {
			String encryptedIdentifier = rs.getString(1);
			if(identifier.compareToIgnoreCase(decrypt(encryptedIdentifier, privateKey)) == 0) {
				return rs.getBlob(2).getBinaryStream();
			}
		}
		return null;
	 }
	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		try{
			/* for(int i=1; i<11; i++) {
				KeyPair keyPair = generateRSAKeyPair();
				add(i, keyPair.getPublic().getEncoded(), keyPair.getPrivate().getEncoded());
				System.out.println("Keys added for server_"+i);
			} */
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
