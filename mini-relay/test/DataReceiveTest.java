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
public class DataReceiveTest {
    private int clientCount=40;
    private int passedClientCount= 0;
    private RandomDataPool dataPool = new RandomDataPool();
    private Digester digester = new Digester();
    private int maximumDataSize = 4*1024*1024;
 //   private InetSocketAddress entranceAddress= new InetSocketAddress;
    
    private int entrancePort=10000;
    private int exitPort =10002;
    
    public DataReceiveTest() {
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
            final AsynchronousServerSocketChannel serverSocket = AsynchronousServerSocketChannel.open().bind(new InetSocketAddress(exitPort));
            serverSocket.accept(null,new CompletionHandler<AsynchronousSocketChannel, Void>() {

                @Override
                public void completed(AsynchronousSocketChannel result, Void attachment) {
                    
                    byte[] data = dataPool.pop();
                    digester.digest(data);
                    sendData(result,data);
                    
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
    
    private synchronized void readDataFromSocket(final AsynchronousSocketChannel socket,final ByteBuffer bb){
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
        byte[] ext = new byte[bb.remaining()];
        bb.get(ext);
        if (digester.hasDigestForData(ext)){
            
            passedClientCount++;
            System.out.println("passed clients: "+passedClientCount);
            digester.remove(digester.getDigestForData(ext));
            if (passedClientCount == clientCount){
                DataReceiveTest.this.notify();
            }
        }else{
            fail("data with this digest doesn't exist");

        }
        
    }
    
    @Test
    public synchronized void dataReceiveTest() {
        for (int i=0;i<this.clientCount;i++){
            
            try{
            final AsynchronousSocketChannel socket = AsynchronousSocketChannel.open();
            
            socket.connect(new InetSocketAddress("127.0.0.1", entrancePort), null, new CompletionHandler<Void, Void>() {

                @Override
                public void completed(Void result, Void attachment) {
                    ByteBuffer bb = ByteBuffer.allocate(maximumDataSize);
                    readDataFromSocket(socket,bb);
                }

                @Override
                public void failed(Throwable exc, Void attachment) {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }
            });
    
            }catch(IOException ioe){
                System.err.println(ioe.getMessage());
            }
            
//            ByteBuffer bb = ByteBuffer.allocate(maximumDataSize);
//            readDataFromSocket(result,bb);
//            
            
            
            
        }
        this.runServer();
        try {
            this.wait();
            assertEquals(1, 1);
        } catch (InterruptedException ex) {
            Logger.getLogger(DataSendTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    
    
    private void flushBufferToSocket(final AsynchronousSocketChannel socket, ByteBuffer buffer){
        
        socket.write(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {

            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                System.out.println("what");
                if (attachment.hasRemaining()) {
    //                flushBufferToSocket(socket, attachment);
                    socket.write(attachment, attachment, this);
                }else{
                    try {
                        socket.shutdownInput();
                        socket.shutdownOutput();
                        socket.close();
                    
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
    
    private synchronized void sendData(AsynchronousSocketChannel socket,byte[] data){
        
        ByteBuffer bb = ByteBuffer.wrap(data);
        flushBufferToSocket(socket, bb);
            
        
    }
    

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
}
