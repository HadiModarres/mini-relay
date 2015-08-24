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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import mini.relay.MiniRelay;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author hadi
 */
public class ConnectivityTest {
    
    byte[] sendHash ;
    byte[] receiveHash;
    int arrayCapacity=1024;
    
    public ConnectivityTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        
        MiniRelay m1 = new MiniRelay(new InetSocketAddress(40000), new InetSocketAddress("127.0.0.1", 40001), true);
        MiniRelay m2 = new MiniRelay(new InetSocketAddress(40001), new InetSocketAddress("127.0.0.1", 40002), false);
        System.out.println("setup");
        
    }
    
    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    
    @Test
    public void testConnectivity(){
        this.sendData();
        this.setupServer();
        Assert.assertArrayEquals(sendHash, receiveHash);
    }
    
    private void setupServer(){
        try {
            AsynchronousServerSocketChannel asc = AsynchronousServerSocketChannel.open().bind(new InetSocketAddress(40002));
            AsynchronousSocketChannel sock = asc.accept().get();
            ByteBuffer bb = ByteBuffer.allocate(arrayCapacity);
            int bytesRead = sock.read(bb).get();
            receiveHash = MessageDigest.getInstance("MD5").digest(bb.array());
            
     
            
        } catch (IOException ex) {
            Logger.getLogger(ConnectivityTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(ConnectivityTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(ConnectivityTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(ConnectivityTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
     
     private void sendData() {
        try {
            byte[] byb = new byte[arrayCapacity];
            new Random().nextBytes(byb);
            sendHash = MessageDigest.getInstance("MD5").digest(byb);
            AsynchronousSocketChannel s1 = AsynchronousSocketChannel.open();
            s1.connect(new InetSocketAddress("127.0.0.1",40000), null, new CompletionHandler<Void, Void>() {

                @Override
                public void completed(Void result, Void attachment) {
                  s1.write(ByteBuffer.wrap(byb),null,new CompletionHandler<Integer, Void>() {

                      @Override
                      public void completed(Integer result, Void attachment) {
//                         try {
//                              s1.shutdownInput();
//                              s1.shutdownOutput();
//                              s1.close();
//                          } catch (IOException ex) {
//                              Logger.getLogger(ConnectivityTest.class.getName()).log(Level.SEVERE, null, ex);
//                         }
                      }

                      @Override
                      public void failed(Throwable exc, Void attachment) {
                          throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                      }
                  });
                }

                @Override
                public void failed(Throwable exc, Void attachment) {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }
            });
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(ConnectivityTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ConnectivityTest.class.getName()).log(Level.SEVERE, null, ex);
        }
         
         
     }
}
