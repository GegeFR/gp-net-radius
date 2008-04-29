/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gp.net.radius.data;

import gp.utils.array.impl.DefaultArray;
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
