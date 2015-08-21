/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mini.relay;

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
    
}
