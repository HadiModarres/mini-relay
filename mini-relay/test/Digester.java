
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author hadi
 */
public class Digester {
    private HashSet<ByteBuffer> digested;
    public Digester(){
        digested = new HashSet<>();
    }
    public void digest(byte[] data){
        try {
            System.out.println("digesting data, length: "+data.length);
            byte[] digest = MessageDigest.getInstance("MD5").digest(data);
            digested.add(ByteBuffer.wrap(digest));
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Digester.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    public void remove(byte[] digest){
        digested.remove(ByteBuffer.wrap(digest));
    }
    public boolean isEmpty(){
        return digested.isEmpty();
    }
    
    public boolean hasDigestForData(byte[] data){
        try {
            System.out.println("does have the with length: "+data.length);
            byte[] digest = MessageDigest.getInstance("MD5").digest(data);
            boolean answer = digested.contains(ByteBuffer.wrap(digest));
            return answer;
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Digester.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    public byte[] getDigestForData(byte[] data){
        try {
            return MessageDigest.getInstance("MD5").digest(data);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Digester.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
}
