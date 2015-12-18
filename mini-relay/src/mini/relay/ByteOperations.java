/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mini.relay;

/**
 *
 * @author hadi
 */
public class ByteOperations {
    public static void reverse(byte[] arr){
//        byte[] ans = new byte[arr.length];
        for (int i=0;i<arr.length;i++)
            arr[i] = (byte) (arr[i] ^ 0b11111111);
//        return ans;
    }
    
    
}
