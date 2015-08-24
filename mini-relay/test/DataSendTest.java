/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import mini.relay.MiniRelay;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author hadi
 */
public class DataSendTest {
    
    private int clientCount=1;
    private int passedClientCount= 0;
    private RandomDataPool dataPool = new RandomDataPool();
    private Digester digester = new Digester();
    private int maximumDataSize = 1024;
 //   private InetSocketAddress entranceAddress= new InetSocketAddress;
    
    private int entrancePort=10000;
    private int exitPort =10002;
    
    public DataSendTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        dataPool.generate(clientCount, maximumDataSize);
        MiniRelay m1 = new MiniRelay(new InetSocketAddress(entrancePort), new InetSocketAddress("127.0.0.1", 40001), true);
        MiniRelay m2 = new MiniRelay(new InetSocketAddress(40001), new InetSocketAddress("127.0.0.1", exitPort), false);
        System.out.println("setup");
     
    }
    
    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    private void runServer(){
        try {
            AsynchronousServerSocketChannel serverSocket = AsynchronousServerSocketChannel.open().bind(new InetSocketAddress(exitPort));
            serverSocket.accept(null,new CompletionHandler<AsynchronousSocketChannel, Void>() {

                @Override
                public void completed(AsynchronousSocketChannel result, Void attachment) {
                    ByteBuffer bb = ByteBuffer.allocate(maximumDataSize);
                    readDataFromSocket(result,bb);
                    
                    serverSocket.accept(null, this);
                    
                }

                @Override
                public void failed(Throwable exc, Void attachment) {
                    System.out.println("failed to accept new connection");
                }
            });
        } catch (IOException ex) {
            Logger.getLogger(DataSendTest.class.getName()).log(Level.SEVERE, null, ex);
        }
            
        
    }
    
    private void readDataFromSocket(AsynchronousSocketChannel socket,ByteBuffer bb){
        socket.read(bb, null, new CompletionHandler<Integer, Void>() {

            @Override
            public void completed(Integer result, Void attachment) {
                if (result == -1){
                    bb.flip();
                    processReceivedData(bb);
                }else{
                    readDataFromSocket(socket, bb);
                }
            }

            @Override
            public void failed(Throwable exc, Void attachment) {
            }
        });
    }
    
    private synchronized void processReceivedData(ByteBuffer bb){
        if (digester.hasDigestForData(bb.array())){
            passedClientCount++;
            digester.remove(digester.getDigestForData(bb.array()));
            if (passedClientCount == clientCount){
                DataSendTest.this.notify();
            }
        }
    }
    
    @Test
    public synchronized void dataSendTest() {
        for (int i=0;i<this.clientCount;i++){
            byte[] data = dataPool.pop();
            digester.digest(data);
            this.sendData(data);
            
        }
        this.runServer();
        try {
            this.wait();
        } catch (InterruptedException ex) {
            Logger.getLogger(DataSendTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void flushBufferToSocket(AsynchronousSocketChannel socket, ByteBuffer buffer){
        socket.write(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {

            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                
                if (attachment.position()<attachment.limit()) {
                    flushBufferToSocket(socket, attachment);
                }else{
                    try {
                        socket.shutdownInput();
                        socket.shutdownOutput();
                    } catch (IOException ex) {
                        Logger.getLogger(DataSendTest.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });
    }
    
    private void sendData(byte[] data){
        try {
            AsynchronousSocketChannel socket = AsynchronousSocketChannel.open();
            socket.connect(new InetSocketAddress("127.0.0.1", entrancePort), null, new CompletionHandler<Void, Void>() {

                @Override
                public void completed(Void result, Void attachment) {
                    ByteBuffer bb = ByteBuffer.wrap(data);
                    flushBufferToSocket(socket, bb);
                }

                @Override
                public void failed(Throwable exc, Void attachment) {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }
            });
            
        } catch (IOException ex) {
            Logger.getLogger(DataSendTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    
    
}
