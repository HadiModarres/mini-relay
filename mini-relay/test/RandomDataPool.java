
import java.util.HashSet;
import java.util.Random;
import java.util.Stack;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author hadi
 */
public class RandomDataPool {
    private Stack<byte[]> set;
    private int defaultSizeLimit = 1024*1024;
    public RandomDataPool(){
        set = new Stack<>();
        
    }
    public void generate(int count){
        this.generate(count, defaultSizeLimit);
    }
    public void generate(int count,int sizeLimit){
        for (int i=0;i<count;i++)
        {
            int randLimit = (int) (Math.random()*sizeLimit);
            byte[] newd = new byte[randLimit];
            new Random().nextBytes(newd);
            set.push(newd);
        }
    }
    public byte[] pop(){
        if (set.isEmpty()){
            return null;
        }else{
            return set.pop();
        }
    }
}
