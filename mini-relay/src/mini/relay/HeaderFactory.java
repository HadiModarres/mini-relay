/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mini.relay;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 *
 * @author hadi
 */
public class HeaderFactory {
    public static String getHttpGetMethodFromUUID(UUID uuid){
        return "dummy";
    }
    public static String getHttpPostMethodFromUUID(UUID uuid){
        return "dummy";
    }
    public static boolean isAPostMethod(String method){
        return true;
    }
    
    public static UUID getUUIDFromHeader(ByteBuffer bb){
        if (isAGetHeader(bb)){
            return getUUIDFromGetMethod(bb);
        }else{
            return getUUIDFromPostMethod(bb);
        }
    }
    
    public static boolean isAGetHeader(ByteBuffer bb){
        return true;
    }
    
    public static UUID getUUIDFromGetMethod(ByteBuffer bb){
        return null;
    }
    public static UUID getUUIDFromPostMethod(ByteBuffer bb){
        return null;
    }
    
    public static String getGetResponseHeader(){
        return "dummy";
    }
    
}
