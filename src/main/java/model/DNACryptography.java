package model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class DNACryptography {

    public static InputStream encode(InputStream inputStream, String userPassword) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            
            byte[] passwordBytes = userPassword.getBytes();
            int length = passwordBytes.length;
            int last8Bits = passwordBytes[length - 1] & 0xFF;
            
            int data;
            while ((data = inputStream.read()) != -1) {
                String binaryString = String.format("%8s", Integer.toBinaryString(data & 0xFF)).replace(' ', '0');
                String encodedString = "";
                for(int i=0; i<8; i+=2) {
                	String encodedCharacter = encodeBinaryString(binaryString.substring(i, i+2));
                	encodedString += encodedCharacter;
                }
                
                // System.out.println(binaryString + " = " + encodedString);
                outputStream.write(encodedString.getBytes());
            }

            return new ByteArrayInputStream(outputStream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static InputStream decode(InputStream inputStream, String userPassword) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] passwordBytes = userPassword.getBytes();
            int length = passwordBytes.length;
            int last8Bits = passwordBytes[length - 1] & 0xFF;

            int data;
            StringBuilder binaryBuffer = new StringBuilder();
            // System.out.println("last 8 bits :"+ Integer.toString(last8Bits));
            
            while ((data = inputStream.read()) != -1) {
                binaryBuffer.append(decodeCharacter((char) data));

                // Convert the binary string back to bytes
                if (binaryBuffer.length() >= 8) {
                    int byteValue = Integer.parseInt(binaryBuffer.substring(0, 8), 2);
                    outputStream.write(byteValue);
                    binaryBuffer.delete(0, 8); // Remove processed bytes from the buffer
                }
            }

            return new ByteArrayInputStream(outputStream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String encodeBinaryString(String binaryString) {
        switch (binaryString) {
            case "00": return "A"; 
            case "01": return "T"; 
            case "10": return "C"; 
            case "11": return "G"; 
            default:
                // Handle other cases if needed
                return "";
        }
    }

    private static String decodeCharacter(char character) {
        switch (character) {
            case 'A': return "00"; 
            case 'T': return "01";
            case 'C': return "10";
            case 'G': return "11";
            // Handle other characters if needed
            default:
                // Return default binary for unknown characters
                return "";
        }
    }
}
