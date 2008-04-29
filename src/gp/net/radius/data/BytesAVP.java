/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
public class BytesAVP
{

    private Array header;
    private Integer08Array code;
    private Integer08Array length;
    private Array data;

    public BytesAVP()
    {
        header = new DefaultArray(2);
        this.code = new Integer08Array(new SubArray(this.header, 0, 1));
        this.length = new Integer08Array(new SubArray(this.header, 1, 1));
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

    public BytesAVP(Array bytes)
    {
        this.header = new SubArray(bytes, 0, 2);
        this.code = new Integer08Array(new SubArray(this.header, 0, 1));
        this.length = new Integer08Array(new SubArray(this.header, 1, 1));
        this.data = new SubArray(bytes, 2, this.getLength() - 2);
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
