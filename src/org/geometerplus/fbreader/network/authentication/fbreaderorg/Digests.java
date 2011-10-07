package org.geometerplus.fbreader.network.authentication.fbreaderorg;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;


public class Digests{
	
    public static String hashSHA256(String message){
    	try{
        	MessageDigest md = MessageDigest.getInstance("SHA-256");
            return byteArrayToHexString(md.digest(message.getBytes()));            
        }
        catch (NoSuchAlgorithmException e){
            return null;
        }
    }
    
    public static String fileHashSHA256(String filename) {
    	try {
        	MessageDigest SHA256 = MessageDigest.getInstance("SHA-256");
        	InputStream filestream = new FileInputStream(filename);
        	
        	byte[] bytes = new byte[1024];
        	int nread = 0;
        	while((nread = filestream.read(bytes)) != -1){
        		SHA256.update(bytes, 0, nread);
        	}
        	return byteArrayToHexString(SHA256.digest());
    	}
    	catch (IOException e){
    		return null;
    	}
    	catch (NoSuchAlgorithmException e) {
			return null;
		}
    }

    
    public static String hmacSHA256(InputStream inStream, String key)
    					throws InvalidKeyException {
    	
    	Mac hmacSHA256 = null;
        Key k = null;	
        try{
            hmacSHA256 = Mac.getInstance("hmacSHA256");
            k = new SecretKeySpec(key.getBytes(),"hmacSHA256");
        }
        catch (NoSuchAlgorithmException e){
            return null;
        }
        
        try{
        	hmacSHA256.init(k);
        	byte[] bytes = new byte[1024];
            int nread = 0;
            
            while((nread = inStream.read(bytes)) != -1){
                hmacSHA256.update(bytes, 0, nread);
            }
            return byteArrayToHexString(hmacSHA256.doFinal());
        }
        catch (IOException e){
            return null;
        }
    }
    
    
    public static String hmacSHA256(String message, String key) 
    throws InvalidKeyException {
    	
    	Mac hmacSHA256 = null;
        Key k = null;	
        try{
            hmacSHA256 = Mac.getInstance("hmacSHA256");
            k = new SecretKeySpec(key.getBytes(),"hmacSHA256");
        }
        catch (NoSuchAlgorithmException e){
            return null;
        }
        
        hmacSHA256.init(k);
        hmacSHA256.update(message.getBytes());
        return byteArrayToHexString(hmacSHA256.doFinal());
    }
    
    
    private static String byteArrayToHexString (byte[] bytes){
    	StringBuilder sb = new StringBuilder();
        for (byte b : bytes){
          sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }
}
