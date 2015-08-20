/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mini.relay;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
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
      
    }
    
    private void handleNewlyAcceptedConnection(AsynchronousSocketChannel socket){
        
    }
    
    private void startListening(){
        try {
            AsynchronousServerSocketChannel serverSocket = AsynchronousServerSocketChannel.open().bind(this.listenAddress);
            serverSocket.accept(null,new CompletionHandler<AsynchronousSocketChannel, Void>() {

                @Override
                public void completed(AsynchronousSocketChannel result, Void attachment) {
                    serverSocket.accept(null, this);
                    
                }

                @Override
                public void failed(Throwable exc, Void attachment) {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }
            });
            
            
        } catch (IOException ex) {
            Logger.getLogger(MiniRelay.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    
    public static void main(String[] args) {
        // TODO code application logic here
    }
    
}
