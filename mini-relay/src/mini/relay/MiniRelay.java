/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mini.relay;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hadi
 */
public class MiniRelay {

    /**
     * @param args the command line arguments
     */
    private boolean isMajorOnListen = false;
    private InetSocketAddress listenAddress=null;
    private InetSocketAddress forwardAddress=null;
    public MiniRelay(InetSocketAddress listenAddress,InetSocketAddress forwardAdddress,boolean majorListen){
        isMajorOnListen = majorListen;
        this.listenAddress = listenAddress;
        this.forwardAddress = forwardAdddress;
        this.startListening();
    }
    
    private void handleNewlyAcceptedConnection(AsynchronousSocketChannel socket){
        System.out.println(socket);
        if (isMajorOnListen){
            this.newPipeRequested(socket);
        }else{
            
        }
    }
    
    private void initiateInboundSocket(Pipe newPipe){
        try {
            AsynchronousSocketChannel inboundSock = AsynchronousSocketChannel.open();
            newPipe.setInbound(inboundSock);
            inboundSock.connect(forwardAddress, newPipe, new CompletionHandler<Void, Pipe>() {

                @Override
                public void completed(Void result, Pipe pipe) {
                    System.out.println("connected  UUID: "+newPipe.getUUID());
                    ByteBuffer newBuffer = ByteBuffer.wrap(HeaderFactory.getHttpGetMethodFromUUID(newPipe.getUUID()).getBytes());
                    pipe.getInbound().write(newBuffer, pipe, new CompletionHandler<Integer, Pipe>() {
                        
                        @Override
                        public void completed(Integer result, Pipe pipe) {
                            ByteBuffer dummyBuffer = ByteBuffer.allocate(1024);
                            pipe.getInbound().read(dummyBuffer, pipe, new CompletionHandler<Integer, Pipe>() {
                                
                                @Override
                                public void completed(Integer result, Pipe pipe) {
                                    System.out.println("GET response received, inbound is ready for relay");
                                    pipe.setInboundReady();
                                }
                                
                                @Override
                                public void failed(Throwable exc, Pipe pipe) {
                                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                                }
                            });
                        }
                        
                        @Override
                        public void failed(Throwable exc, Pipe pipe) {
                            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                        }
                    });
                    
                }

                @Override
                public void failed(Throwable exc, Pipe pipe) {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }
            });
        } catch (IOException ex) {
            Logger.getLogger(MiniRelay.class.getName()).log(Level.SEVERE, null, ex);
        }
            
        
    }
    
    private void initiateOutboundSocket(Pipe newPipe){
        try {
            AsynchronousSocketChannel outboundSocket = AsynchronousSocketChannel.open();
            newPipe.setOutbound(outboundSocket);
            outboundSocket.connect(forwardAddress, newPipe, new CompletionHandler<Void, Pipe>() {

                @Override
                public void completed(Void result, Pipe pipe) {
                    System.out.println("connected  outbound UUID: "+newPipe.getUUID());
                    ByteBuffer newBuffer = ByteBuffer.wrap(HeaderFactory.getHttpPostMethodFromUUID(newPipe.getUUID()).getBytes());
                    pipe.getOutbound().write(newBuffer, pipe, new CompletionHandler<Integer, Pipe>() {

                        @Override
                        public void completed(Integer result, Pipe pipe) {
                            pipe.setOutboundReady();
                        }

                        @Override
                        public void failed(Throwable exc, Pipe pipe) {
                            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                        }
                    });
                    
                
                }

                @Override
                public void failed(Throwable exc, Pipe pipe) {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }
            });
        } catch (IOException ex) {
            Logger.getLogger(MiniRelay.class.getName()).log(Level.SEVERE, null, ex);
        }
            
    }
    
    private void newPipeRequested(AsynchronousSocketChannel majorSocket){
        
            Pipe newPipe = new Pipe();
            newPipe.setMajor(majorSocket);
            newPipe.setMajorReady();
            this.initiateInboundSocket(newPipe);
            this.initiateOutboundSocket(newPipe);
        
        
        
        
    }
    
    private void startListening(){
        try {
            
            AsynchronousServerSocketChannel serverSocket = AsynchronousServerSocketChannel.open().bind(this.listenAddress);
            serverSocket.accept(null,new CompletionHandler<AsynchronousSocketChannel, Void>() {

                @Override
                public void completed(AsynchronousSocketChannel result, Void attachment) {
                    serverSocket.accept(null, this);
                    handleNewlyAcceptedConnection(result);
                }

                @Override
                public void failed(Throwable exc, Void attachment) {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }
            });
            
            
        } catch (IOException ex) {
            Logger.getLogger(MiniRelay.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            Thread.sleep(1000000);
        } catch (InterruptedException ex) {
            Logger.getLogger(MiniRelay.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    
    public static void main(String[] args) {
        // TODO code application logic here
        new MiniRelay(new InetSocketAddress(6900), new InetSocketAddress("www.google.com", 80), true);
    }
    
}
