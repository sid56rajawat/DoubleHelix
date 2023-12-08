package model;

import model.Server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

//this file is a POJO: Plain old java object
public class File {
	
	private static Connection getcon() throws SQLException, ClassNotFoundException {
		Class.forName("com.mysql.cj.jdbc.Driver");
		String jdbcurl = "jdbc:mysql://localhost:3306/DoubleHelix"; //3306 is port id of mysql
        Connection con = DriverManager.getConnection(jdbcurl,"root","Shivam@123");
        return con;
	}
	
	public static Integer getFileId(String fileName, Integer owner) throws SQLException, ClassNotFoundException {
		Connection con=getcon();
		String query = "SELECT fileId FROM files WHERE fileName LIKE ? AND owner = ?";
        PreparedStatement pstmt = con.prepareStatement(query);
        
        pstmt.setString(1, fileName);
        pstmt.setInt(2, owner);
        
        ResultSet rs = pstmt.executeQuery();
        if(rs.next())
        	return rs.getInt(1);
        
        return -1;
		
	}
	
	public static int getRandomNumber(int min, int max) {
	    return (int) ((Math.random() * (max - min)) + min);
	}
	
	private static byte[] encrypt(byte[] input, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(input);
    }
	
	private static byte[] decrypt(byte[] input, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return cipher.doFinal(input);
    }
	
	public static ArrayList<InputStream> splitInputStream(InputStream originalStream, int numberOfParts, SecretKey secretKey) throws Exception {
        // Read the entire content of the original stream into a byte array
        byte[] originalBytes = originalStream.readAllBytes();

        // Calculate the size of each part
        int partSize = originalBytes.length / numberOfParts;
        ArrayList<InputStream> partStreams = new ArrayList<>();

        // Create a ByteArrayInputStream for each part
        for (int i = 0; i < numberOfParts; i++) {
            int start = i * partSize;
            int end = (i == numberOfParts - 1) ? originalBytes.length : (i + 1) * partSize;
            byte[] partBytes = Arrays.copyOfRange(originalBytes, start, end);
            partBytes = encrypt(partBytes,secretKey);
            InputStream partStream = new ByteArrayInputStream(partBytes);
            partStreams.add(partStream);
        }

        return partStreams;
    }
	
	//add method 
	public static void upload(String fileName, InputStream file_data, Integer owner) throws Exception {
		// create entry in files table
		Connection con=getcon();
        String query="insert into files(filename, owner) values(?,?)";
        PreparedStatement pstmt = con.prepareStatement(query);
        pstmt.setString(1, fileName);
        pstmt.setInt(2, owner);
        pstmt.executeUpdate();
        
        // fetch secret key of user
        query = "select secretKey from kms where userId=?";
        pstmt = con.prepareStatement(query);
        pstmt.setInt(1, owner);
        ResultSet rs = pstmt.executeQuery();
        rs.next();
        byte[] secretKeyBytes = rs.getBytes(1);
        SecretKey secretKey = new SecretKeySpec(secretKeyBytes, 0, secretKeyBytes.length, "AES");
        
        // split the file into encrypted blocks
        Integer fileId = getFileId(fileName,owner);
        ArrayList<InputStream> blocks = splitInputStream(file_data,4,secretKey);
        
        for(int blockId=1; blockId<=4; blockId++) {
        	Integer serverId = getRandomNumber(1,10); // deciding server
        	// creating entry for each block in blocks table
        	query = "insert into blocks(blockId,fileId,serverId) values(?,?,?)";
        	pstmt = con.prepareStatement(query);
            pstmt.setInt(1, blockId);
            pstmt.setInt(2, fileId);
            pstmt.setInt(3, serverId);
            pstmt.executeUpdate();
            
            String identifier = "("+owner+","+fileId+","+blockId+")";
            Server.uploadToServer(blocks.get(blockId-1), identifier, serverId);
        }
	}
	
	private static byte[] mergeByteArrays(List<byte[]> byteArrays) {
        int totalLength = byteArrays.stream().mapToInt(byteArray -> byteArray.length).sum();
        byte[] mergedArray = new byte[totalLength];
        int offset = 0;

        for (byte[] byteArray : byteArrays) {
            System.arraycopy(byteArray, 0, mergedArray, offset, byteArray.length);
            offset += byteArray.length;
        }

        return mergedArray;
    }
	
	public static InputStream mergeInputStream(InputStream[] blocks, SecretKey secretKey) {
        try {
            List<byte[]> decryptedBlocks = new ArrayList<>();

            // Decrypt each block
            for (InputStream block : blocks) {
                byte[] encryptedData = block.readAllBytes();
                byte[] decryptedData = decrypt(encryptedData, secretKey);
                decryptedBlocks.add(decryptedData);
            }

            // Merge decrypted blocks into a single InputStream
            byte[] mergedData = mergeByteArrays(decryptedBlocks);
            return new ByteArrayInputStream(mergedData);
        } catch (Exception e) {
            e.printStackTrace(); // Handle exception appropriately
            return null;
        }
	}
    
	
	public static InputStream download(String fileName, Integer owner) throws Exception {
		Integer fileId = getFileId(fileName,owner);
		
		// getting serverId of each block
		Connection con = getcon();
		String query = "Select blockId,serverId from blocks where fileId=?";
		PreparedStatement pstmt = con.prepareStatement(query);
		pstmt.setInt(1, fileId);
		ResultSet rs = pstmt.executeQuery();
		int[] blockServers = new int[4];
		while(rs.next()) {
			blockServers[rs.getInt(1)-1] = rs.getInt(2);
		}
		
		// downloading blocks from their servers
		InputStream[] blocks = new InputStream[4];
		for(int i=1; i<=4; i++) {
			Integer serverId = blockServers[i-1];
			String identifier = "("+owner+","+fileId+","+i+")";
			blocks[i-1] = Server.downloadFromServer(serverId,identifier);
		}
		
		// fetch secret key of user
        query = "select secretKey from kms where userId=?";
        pstmt = con.prepareStatement(query);
        pstmt.setInt(1, owner);
        rs = pstmt.executeQuery();
        rs.next();
        byte[] secretKeyBytes = rs.getBytes(1);
        SecretKey secretKey = new SecretKeySpec(secretKeyBytes, 0, secretKeyBytes.length, "AES");
		
		return mergeInputStream(blocks,secretKey);
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
