/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mini.relay;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 *
 * @author hadi
 */
public class HeaderFactory {
    public static ByteBuffer getHttpGetMethodFromUUID(UUID uuid){
        String headerString = "GET /"+uuid.toString()+".iso HTTP/1.1\r\nFrom: "+"jmansee@gmail.com\r\nConnection: keep-alive\r\nUser-Agent: Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.132 Safari/537.36\r\naccept-encoding: gzip,deflate\r\nAccept-Language: en-US,en;q=0.8\r\n\r\n";
        return ByteBuffer.wrap(headerString.getBytes(StandardCharsets.US_ASCII));
    }
    public static ByteBuffer getHttpPostMethodFromUUID(UUID uuid){
        String headerString = "POST /"+uuid.toString()+".iso HTTP/1.0\r\ncontent-type:application/octet-stream\r\nhost: https://importexport.amazonaws.com\r\ncontent-length:20756565\r\n\r\n";
        return ByteBuffer.wrap(headerString.getBytes(StandardCharsets.US_ASCII));
    }
//    public static boolean isAPostMethod(ByteBuffer bb){
//        return true;
//    }
//    
    public static UUID getUUIDFromHeader(ByteBuffer bb){
        if (isAGetHeader(bb)){
            return getUUIDFromGetMethod(bb);
        }else{
            return getUUIDFromPostMethod(bb);
        }
    }
    
    public static boolean isAGetHeader(ByteBuffer bb){
        Byte b2 = bb.get(0);
        return bb.get(0) == 71;
    }
    
    public static UUID getUUIDFromGetMethod(ByteBuffer bb){
        String headerString = StandardCharsets.US_ASCII.decode(bb).toString();
        String uuidString = (headerString.split(".iso"))[0].split("/")[1];
        return UUID.fromString(uuidString);
    }
    public static UUID getUUIDFromPostMethod(ByteBuffer bb){
        String headerString = StandardCharsets.US_ASCII.decode(bb).toString();
        String uuidString = (headerString.split(".iso"))[0].split("/")[1];
        return UUID.fromString(uuidString);

    }
    
    public static ByteBuffer getGetResponseHeader(){
        String headerString = "HTTP/1.1 200 OK\r\nDate: Fri, 31 Dec 2014 23:59:59 GMT\r\nServer: Apache/2.2.23 (CentOS)\r\nLast-Modified: Wed, 18 Feb 2015 20:12:10 GMT\r\nContent-Length: 1044381696\r\nConnection: close\r\nContent-Type: application/octet-stream\r\n\r\n";
        return ByteBuffer.wrap(headerString.getBytes(StandardCharsets.US_ASCII));
    }
    
}
