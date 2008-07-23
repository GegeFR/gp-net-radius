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

import gp.utils.arrays.DefaultArray;
import java.io.UnsupportedEncodingException;

/**
 *
 * @author gege
 */
public class StringAVP extends BytesAVP
{
    String encoding;
    
    public StringAVP(int type, String data, String encoding) throws UnsupportedEncodingException
    {
        super(type, new DefaultArray(data.getBytes(encoding)));
        this.encoding = encoding;
    }
    
    public StringAVP(BytesAVP bytesAVP, String encoding)
    {
        super(bytesAVP);
        this.encoding = encoding;
    }

    public String getValue() throws UnsupportedEncodingException
    {
        return new String(super.getData().getBytes(), this.encoding);
    }
}
