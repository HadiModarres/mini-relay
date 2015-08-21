/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mini.relay;


import java.util.HashMap;
import java.util.UUID;

/**
 *
 * @author hadi
 */
public class PipeMap {
    public static HashMap<UUID,Pipe> map = new HashMap<>();
    public static boolean PipeExists(UUID id){
        return map.containsKey(id);
    }
    
}
