/*
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */

package gp.net.radius.data;

import gp.utils.arrays.Integer32Array;

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
