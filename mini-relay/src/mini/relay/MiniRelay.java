/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mini.relay;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import static java.net.SocketOptions.SO_RCVBUF;
import java.net.StandardSocketOptions;
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
    private PipeMap pipeMap=new PipeMap();
    private String lockStr = "lock";
    private boolean isMajorOnListen = false;
    private InetSocketAddress listenAddress=null;
    private InetSocketAddress forwardAddress=null;
    public MiniRelay(InetSocketAddress listenAddress,InetSocketAddress forwardAdddress,boolean majorListen){
        isMajorOnListen = majorListen;
        this.listenAddress = listenAddress;
        this.forwardAddress = forwardAdddress;
        this.startListening();
    }
    
    private void connectMajorSocket(Pipe pipe){
        try {
            AsynchronousSocketChannel majorSock = AsynchronousSocketChannel.open();
            majorSock.setOption(StandardSocketOptions.TCP_NODELAY, true);
            pipe.setMajor(majorSock);
            majorSock.connect(forwardAddress, pipe, new CompletionHandler<Void, Pipe>() {

                @Override
                public void completed(Void result, Pipe attachment) {
                    attachment.setMajorReady();
                }

                @Override
                public void failed(Throwable exc, Pipe attachment) {
                    attachment.close();
                }
            });
            } catch (IOException ex) {
            Logger.getLogger(MiniRelay.class.getName()).log(Level.SEVERE, null, ex);
            pipe.close();
        }
    }
    
    private void inboundConnectionReceived(UUID uuid,AsynchronousSocketChannel socket){
        System.out.println("inbound received "+uuid);
        Pipe pipe;
        if (!(pipeMap.PipeExists(uuid))){
            pipe = new Pipe(uuid);
            pipeMap.map.put(uuid, pipe);
            this.connectMajorSocket(pipe);
        }
        pipe = pipeMap.map.get(uuid);
        pipe.setInbound(socket);
       
        // do inbound init
        pipe.setInboundReady();
    }
    
    private void outboundConnectionReceived(UUID uuid,AsynchronousSocketChannel socket){
        System.out.println("outbound received "+uuid);
        Pipe pipe;
        if (!(pipeMap.PipeExists(uuid))){
            pipe = new Pipe(uuid);
            pipeMap.map.put(uuid, pipe);
            this.connectMajorSocket(pipe);

        }
        pipe = pipeMap.map.get(uuid);
        pipe.setOutbound(socket);
        
        // do outbound init
        ByteBuffer newBuffer = HeaderFactory.getGetResponseHeader();
        pipe.getOutbound().write(newBuffer, pipe, new CompletionHandler<Integer, Pipe>() {
            
            @Override
            public void completed(Integer result, Pipe attachment) {
                attachment.setOutboundReady();
            }

            @Override
            public void failed(Throwable exc, Pipe attachment) {
                attachment.close();
            }
        });
        
    }
    
   
    private synchronized void handleNewlyAcceptedConnection(AsynchronousSocketChannel socket){
        System.out.println(socket);
        if (isMajorOnListen){
            this.newPipeRequested(socket);
        }else{
         //   ByteBuffer bb = ByteBuffer.allocate(1024);

            ByteBuffer bb = ByteBuffer.allocate(HeaderFactory.getPostHeaderSize());
            socket.read(bb, bb, new CompletionHandler<Integer, ByteBuffer>() {

                @Override
                public void completed(Integer result, ByteBuffer attachment) {
//                    MiniRelay.this.prh();
                    bb.flip();
                    if (HeaderFactory.isAGetHeader(attachment)){
                        outboundConnectionReceived(HeaderFactory.getUUIDFromHeader(attachment),socket);
                    }else{
                        inboundConnectionReceived(HeaderFactory.getUUIDFromHeader(attachment),socket);
                    }
                }

                @Override
                public void failed(Throwable exc, ByteBuffer attachment) {
//                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                    System.out.println("couldn't read uuid from socket");
                }
            });
        }
        
    }
    
    private void initiateInboundSocket(Pipe newPipe){
        try {
            AsynchronousSocketChannel inboundSock = AsynchronousSocketChannel.open();
            inboundSock.setOption(StandardSocketOptions.TCP_NODELAY, true);

            newPipe.setInbound(inboundSock);
            inboundSock.connect(forwardAddress, newPipe, new CompletionHandler<Void, Pipe>() {

                @Override
                public void completed(Void result, Pipe pipe) {
                    System.out.println("inbound connected  UUID: "+newPipe.getUUID());
                    ByteBuffer newBuffer = HeaderFactory.getHttpGetMethodFromUUID(newPipe.getUUID());
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
                                    pipe.close();
                                }
                            });
                        }
                        
                        @Override
                        public void failed(Throwable exc, Pipe pipe) {
                            pipe.close();
                        }
                    });
                    
                }

                @Override
                public void failed(Throwable exc, Pipe pipe) {
                    System.out.println("failed to connect ");
                    pipe.close();
                }
            });
        } catch (IOException ex) {
            Logger.getLogger(MiniRelay.class.getName()).log(Level.SEVERE, null, ex);
            newPipe.close();
        }
            
        
    }
    
    private void initiateOutboundSocket(Pipe newPipe){
        try {
            AsynchronousSocketChannel outboundSocket = AsynchronousSocketChannel.open();
            outboundSocket.setOption(StandardSocketOptions.TCP_NODELAY, true);

            newPipe.setOutbound(outboundSocket);
            outboundSocket.connect(forwardAddress, newPipe, new CompletionHandler<Void, Pipe>() {

                @Override
                public void completed(Void result, Pipe pipe) {
                    System.out.println("outbound connected UUID: "+newPipe.getUUID());
                    ByteBuffer newBuffer = HeaderFactory.getHttpPostMethodFromUUID(newPipe.getUUID());
                    pipe.getOutbound().write(newBuffer, pipe, new CompletionHandler<Integer, Pipe>() {

                        @Override
                        public void completed(Integer result, Pipe pipe) {
//                            if (newBuffer.hasRemaining()){
//                                pipe.getOutbound().write(newBuffer, pipe, this);
//                            }else{
                            pipe.setOutboundReady();
//                            }
                        }

                        @Override
                        public void failed(Throwable exc, Pipe pipe) {
                            pipe.close();
                        }
                    });
                    
                
                }

                @Override
                public void failed(Throwable exc, Pipe pipe) {
                    pipe.close();
                }
            });
        } catch (IOException ex) {
            Logger.getLogger(MiniRelay.class.getName()).log(Level.SEVERE, null, ex);
            newPipe.close();
        }
            
    }
    
    private void newPipeRequested(AsynchronousSocketChannel majorSocket){
        
            Pipe newPipe = new Pipe();
            
            newPipe.setMajor(majorSocket);
            newPipe.setMajorReady();
           
            this.initiateOutboundSocket(newPipe);
         this.initiateInboundSocket(newPipe);
        
        
        
    }
    
    private void startListening(){
        try {
            
            AsynchronousServerSocketChannel serverSocket = AsynchronousServerSocketChannel.open().bind(this.listenAddress);
            serverSocket.accept(null,new CompletionHandler<AsynchronousSocketChannel, Void>() {

                @Override
                public void completed(AsynchronousSocketChannel result, Void attachment) {
                    MiniRelay.this.handleNewlyAcceptedConnection(result);
                    serverSocket.accept(null, this);
                    
                }

                @Override
                public void failed(Throwable exc, Void attachment) {
                    System.out.println("failed to accept new connection");
                }
            });
            
            
        } catch (IOException ex) {
            Logger.getLogger(MiniRelay.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("open, bind failed");
        }
//        try {
//            Thread.sleep(1000000);
//        } catch (InterruptedException ex) {
//            Logger.getLogger(MiniRelay.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }
    
    
    
    public static void main(String[] args) {
        // TODO code application logic here
        
        new MiniRelay(new InetSocketAddress(Integer.parseInt(args[0])),new InetSocketAddress(args[1],Integer.parseInt(args[2])), true);
        
        try {
            Thread.sleep(342234234);
//        new MiniRelay(new InetSocketAddress(6900), new InetSocketAddress("127.0.0.1",7000), true);
//        UUID u1 = UUID.randomUUID();
//        ByteBuffer getMethod = HeaderFactory.getHttpPostMethodFromUUID(u1);
//        UUID u2 = HeaderFactory.getUUIDFromPostMethod(getMethod);
//        boolean isget = HeaderFactory.isAGetHeader(getMethod);
//        System.out.println("done");
        } catch (InterruptedException ex) {
            Logger.getLogger(MiniRelay.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
