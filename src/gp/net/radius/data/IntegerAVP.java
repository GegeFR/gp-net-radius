/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gp.net.radius.data;

import gp.utils.array.impl.Integer32Array;

/**
 *
 * @author gege
 */
public class IntegerAVP extends BytesAVP
{
    public IntegerAVP(int type, int value)
    {
        super(type, new Integer32Array(value));
    }

    public IntegerAVP(BytesAVP bytesAVP)
    {
        super(bytesAVP);
    }
    
    public int getValue()
    {
        return ((this.getData().get(0) & 0xFF) << 24) + 
               ((this.getData().get(1) & 0xFF) << 16) +
               ((this.getData().get(1) & 0xFF) << 8) +
               (this.getData().get(3) & 0xFF);
    }
}
