
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
    private HashSet<byte[]> digested;
    public Digester(){
        digested = new HashSet<>();
    }
    public void digest(byte[] data){
        try {
            byte[] digest = MessageDigest.getInstance("MD5").digest(data);
            digested.add(digest);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Digester.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    public void remove(byte[] digest){
        digested.remove(digest);
    }
    public boolean isEmpty(){
        return digested.isEmpty();
    }
    
    public boolean hasDigestForData(byte[] data){
        try {
            byte[] digest = MessageDigest.getInstance("MD5").digest(data);
            return digested.contains(digest);
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
