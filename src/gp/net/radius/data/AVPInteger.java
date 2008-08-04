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

import gp.utils.arrays.Integer08Array;
import gp.utils.arrays.Integer32Array;

/**
 *
 * @author Gwenhael Pasquiers
 */
public class AVPInteger extends AVPBytes
{
    public AVPInteger(int type, int value)
    {
        super(type, new Integer32Array(value));
    }

    public AVPInteger(AVPBytes bytesAVP)
    {
        super(bytesAVP);
    }
    
    public int getValue()
    {
        Integer08Array integer08Array = new Integer08Array(this.getData());
        return integer08Array.getValue();
    }
}
