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
    public HashMap<UUID,Pipe> map = new HashMap<>();
    public boolean PipeExists(UUID id){
        boolean f = map.containsKey(id);
        return f;
    }
    
}
