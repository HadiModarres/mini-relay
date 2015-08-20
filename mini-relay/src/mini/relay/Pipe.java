package mini.relay;


import java.nio.channels.AsynchronousSocketChannel;
import java.util.UUID;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author hadi
 */
public class Pipe {
    private AsynchronousSocketChannel inboundStream= null;
    private AsynchronousSocketChannel outboundStream=null ;
    private AsynchronousSocketChannel majorStream = null ;
    
    private boolean majorReady = false;
    private boolean inboundReady = false ;
    private boolean outboundReady = false;
    
    private UUID pipeUUID = null;
    
    public Pipe(){
        super();
        // register with a new UUID
        UUID newUUID = UUID.randomUUID();
        pipeUUID = newUUID;
        PipeMap.map.put(newUUID, this);

    }
    
    public Pipe(UUID pipeUUID){
        super();
        this.pipeUUID = pipeUUID;
        PipeMap.map.put(pipeUUID, this);
        
    }
    
    public void setInbound(AsynchronousSocketChannel inb){
        this.inboundStream = inb ;
        
    }
    public void setOutbound(AsynchronousSocketChannel out){
        this.outboundStream = out ;
    }
    public void setMajor(AsynchronousSocketChannel maj){
        this.majorStream = maj ;
    }
    private void initiateRelay(){
        // initiate the relay process
    }
    
    private void checkIfPipeReady(){
        if (inboundReady && outboundReady && majorReady)
            this.initiateRelay();
    }
    
    public void setInboundReady(){
        this.inboundReady = true ;
        checkIfPipeReady();
    }
    public void setOutboundReady(){
        this.outboundReady = true ;
        checkIfPipeReady();
    }
    public  void setMajorReady(){
        this.majorReady = true ;
        checkIfPipeReady();
    }
    
}
