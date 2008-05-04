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

import gp.utils.array.impl.Array;
import gp.utils.array.impl.DefaultArray;
import gp.utils.array.impl.Integer08Array;
import gp.utils.array.impl.SubArray;
import gp.utils.array.impl.SupArray;

/**
 *
 * @author gege
 */
@Deprecated
public class VendorData
{

    private Array header;
    private Integer08Array code;
    private Integer08Array length;
    private Array data;

    public VendorData()
    {
        this.header = new DefaultArray(2);
        this.code = new Integer08Array(new SubArray(this.header, 0, 1));
        this.length = new Integer08Array(new SubArray(this.header, 1, 1));
        this.setLength(2);
    }

    public VendorData(int code, Array data)
    {
        this();
        this.setCode(code);
        this.setData(data);
    }

    public VendorData(Array bytes)
    {
        this.header = new SubArray(bytes, 0, 2);

        this.code = new Integer08Array(new SubArray(this.header, 0, 1));
        this.length = new Integer08Array(new SubArray(this.header, 1, 1));
        this.setData(new SubArray(bytes, this.header.length, this.getLength() - this.header.length));
    }

    public int getCode()
    {
        return this.code.getValue();
    }

    public void setCode(int value)
    {
        this.code.setValue(value);
    }

    public int getLength()
    {
        return this.length.getValue();
    }

    public void setLength(int value)
    {
        this.length.setValue(value);
    }

    public void setData(Array data)
    {
        this.data = data;
        if (this.getLength() != data.length + header.length)
        {
            this.setLength(data.length + header.length);
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
