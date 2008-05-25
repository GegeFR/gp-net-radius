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

import gp.net.radius.exceptions.RadiusException;
import gp.utils.array.impl.Array;
import gp.utils.array.impl.DefaultArray;
import gp.utils.array.impl.Integer08Array;
import gp.utils.array.impl.SubArray;
import gp.utils.array.impl.SupArray;

/**
 *
 * @author gege
 */
public class BytesAVP
{

    private Array header;
    private Integer08Array code;
    private Integer08Array length;
    private Array data;

    public BytesAVP()
    {
        header = new DefaultArray(2);
        this.code = new Integer08Array(this.header.subArray(0, 1));
        this.length = new Integer08Array(this.header.subArray(1, 1));
        this.setLength(this.header.length);
    }

    public BytesAVP(int type)
    {
        this();
        this.setType(type);
    }

    public BytesAVP(int type, Array data)
    {
        this(type);
        this.setData(data);
    }

    public BytesAVP(Array bytes) throws RadiusException
    {
        try
        {
            this.header = bytes.subArray(0, 2);
            this.code   = new Integer08Array(this.header.subArray(0, 1));
            this.length = new Integer08Array(this.header.subArray(1, 1));
            this.data   = bytes.subArray(2, this.getLength() - 2);
        }
        catch(Exception e)
        {
            throw new RadiusException("Error while decoding an AVP from an Array. This is probably due to a wrong length header field.", e);
        }
    }

    public BytesAVP(BytesAVP bytesAVP)
    {
        this.header = bytesAVP.header;
        this.data = bytesAVP.data;
    }

    public int getType()
    {
        return this.code.getValue();
    }

    public void setType(int value)
    {
        this.code.setValue(value);
    }

    public int getLength()
    {
        return this.length.getValue();
    }

    private void setLength(int value)
    {
        this.length.setValue(value);
    }

    public void setData(Array data)
    {
        this.data = data;
        if (data.length + 2 != this.getLength())
        {
            setLength(data.length + 2);
        }
    }

    public Array getData()
    {
        return this.data;
    }

    public Array getArray()
    {
        SupArray array = new SupArray();
        array.addLast(this.header);
        array.addLast(this.data);
        return array;
    }
}
