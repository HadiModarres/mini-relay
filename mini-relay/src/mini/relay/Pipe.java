package mini.relay;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.UUID;
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
public class  Pipe {
    private AsynchronousSocketChannel inboundStream= null;
    private AsynchronousSocketChannel outboundStream=null ;
    private AsynchronousSocketChannel majorStream = null ;
    
    private boolean majorReady = false;
    private boolean inboundReady = false ;
    private boolean outboundReady = false;
    
   ByteBuffer inboundToMajor = ByteBuffer.allocate(1024);
   ByteBuffer majorToOutbound = ByteBuffer.allocate(1024);
    
    private UUID pipeUUID = null;
    private boolean initiated = false ;
    
    
    public Pipe(){
        super();
        // register with a new UUID
        UUID newUUID = UUID.randomUUID();
        pipeUUID = newUUID;
//        PipeMap.map.put(newUUID, this);

    }
    
    public Pipe(UUID pipeUUID){
        super();
        this.pipeUUID = pipeUUID;
//        PipeMap.map.put(pipeUUID, this);
        
    }
    public UUID getUUID(){
        return pipeUUID;
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
    
    public AsynchronousSocketChannel getInbound(){
        return this.inboundStream;
    }
    
    public AsynchronousSocketChannel getOutbound(){
        return this.outboundStream ;
    }
    public AsynchronousSocketChannel getMajor(){
        return this.majorStream;
    }
    
    
    private void readFromInbound(){
        inboundStream.read(inboundToMajor, inboundToMajor, new CompletionHandler<Integer, ByteBuffer>() {

            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                if (result == -1){
                    inboundSocketDataDone();
                    return;
                }
                attachment.flip();
                majorStream.write(attachment, attachment, new CompletionHandler<Integer, ByteBuffer>() {

                    @Override
                    public void completed(Integer result, ByteBuffer attachment) {
                         attachment.clear();
                         readFromInbound();
                    }

                    @Override
                    public void failed(Throwable exc, ByteBuffer attachment) {
                        close();
                    }
                });
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                System.out.println(exc);
                close();
            }
        });
    }
    
    private void majorSocketDataDone(){
  //      this.close();
    }
    private void inboundSocketDataDone(){
        this.close();
    }
    
    public void close(){
        try {
            inboundStream.close();
        } catch (IOException ex) {
            Logger.getLogger(Pipe.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            outboundStream.close();
        } catch (IOException ex) {
            Logger.getLogger(Pipe.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            majorStream.close();
        } catch (IOException ex) {
            Logger.getLogger(Pipe.class.getName()).log(Level.SEVERE, null, ex);
        }
        
//        PipeMap.map.remove(this.pipeUUID); // remove from dictionary
//        System.out.println("pipes remaining in dict: "+PipeMap.map.size());
    }
    
    private void readFromMajor(){
        majorStream.read(majorToOutbound, majorToOutbound, new CompletionHandler<Integer, ByteBuffer>() {

            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                if (result==-1){
                   majorSocketDataDone();
                   return;
                }
                attachment.flip();
                outboundStream.write(attachment, attachment, new CompletionHandler<Integer, ByteBuffer>() {

                    @Override
                    public void completed(Integer result, ByteBuffer attachment) {
                         attachment.clear();
                         readFromMajor();
                    }

                    @Override
                    public void failed(Throwable exc, ByteBuffer attachment) {
                        close();
                    }
                });
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                close();
            }
        });
    }
    
    private void initiateRelay(){
        // initiate the relay process
        synchronized(this){
            if (initiated)
                return;
        initiated = true ;
        System.out.println("initiating relay");
        this.readFromInbound();
        this.readFromMajor();
        }
    }
    
    private void checkIfPipeReady(){
        if (inboundReady && outboundReady && majorReady)
            this.initiateRelay();
    }
    
    public void setInboundReady(){
        synchronized(this){
        this.inboundReady = true ;
        checkIfPipeReady();
        }
    }
    public void setOutboundReady(){
        synchronized(this){

        this.outboundReady = true ;
        checkIfPipeReady();
        }
    }
    public  void setMajorReady(){
        synchronized(this){

        this.majorReady = true ;
        checkIfPipeReady();
        }
    }
    
}
