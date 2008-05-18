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
import gp.utils.array.impl.ConstantArray;
import gp.utils.array.impl.Array;
import gp.utils.array.impl.DefaultArray;
import gp.utils.array.impl.DigestArray;
import gp.utils.array.impl.Integer08Array;
import gp.utils.array.impl.Integer16Array;
import gp.utils.array.impl.SubArray;
import gp.utils.array.impl.SupArray;
import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.LinkedList;

/**
 *
 * @author gege
 */
public class RadiusMessage
{
    private Array header;
    private Integer08Array code;
    private Integer08Array identifier;
    private Integer16Array length;
    
    private Array authenticator;
    
    private Array secret;
    
    private LinkedList<BytesAVP> avps;

    private LinkedList<BytesAVP> userPasswordAvps;
    
    private InetSocketAddress localAddress;
    
    private InetSocketAddress remoteAddress;
    
    public RadiusMessage()
    {
        this.secret = null;
        
        this.header = new DefaultArray(4);
        this.code           = new Integer08Array(new SubArray(this.header, 0, 1));
        this.identifier     = new Integer08Array(new SubArray(this.header, 1, 1));
        this.length         = new Integer16Array(new SubArray(this.header, 2, 2));

        this.authenticator = null;

        this.avps = new LinkedList<BytesAVP>();
        
        this.userPasswordAvps = new LinkedList<BytesAVP>();
        
        this.setLength(20);
    }
    
    public RadiusMessage(Array data) throws RadiusException
    {
        this.secret = null;
        
        this.header         = new SubArray(data, 0, 4);
        this.code           = new Integer08Array(new SubArray(this.header, 0, 1));
        this.identifier     = new Integer08Array(new SubArray(this.header, 1, 1));
        this.length         = new Integer16Array(new SubArray(this.header, 2, 2));
        
        this.authenticator  = new SubArray(data, 4, 16);
        
        this.avps = new LinkedList<BytesAVP>();
        
        this.userPasswordAvps = new LinkedList<BytesAVP>();
        
        if(this.getLength() != data.length)
        {
            throw new RadiusException("Invalid length of message (" + data.length + ") or invalid length in header (" + this.length.getValue() + ")");
        }
        
        int offset = 20;
        int length = this.getLength();
        while(offset < length)
        {
            BytesAVP avp = new BytesAVP(new SubArray(data, offset));
            offset += avp.getLength();
            this.addAVP(avp, false);
        }
    }
    
    public void setSecret(Array secret)
    {
        this.secret = secret;
    }
    
    public Array getSecret()
    {
        return this.secret;
    }

    public void setCode(int value)
    {
        this.code.setValue(value);
    }

    public int getCode()
    {
        return this.code.getValue();
    }
    
    public void setIdentifier(int value)
    {
        this.identifier.setValue(value);
    }

    public int getIdentifier()
    {
        return this.identifier.getValue();
    }
    
    private void setLength(int value)
    {
        this.length.setValue(value);
    }

    public int getLength()
    {
        return this.length.getValue();
    }

    public void setAuthenticator(Array data)
    {
       this.authenticator = data;
    }
    
    public Array getAuthenticator()
    {
       return authenticator;
    }

    public void addAVP(BytesAVP avp)
    {
        this.addAVP(avp, true);
    }

    private void addAVP(BytesAVP avp, boolean setLength)
    {
        // special behavior for User-Password AVP
        if(avp.getType() == 2)
        {
            avp.setData(RadiusMessageUtils.padUserPassword(avp.getData()));
            this.userPasswordAvps.addLast(avp);
        }
        
        avps.addLast(avp);
        if(setLength)
        {
            this.setLength(this.getLength() + avp.getLength());
        }
    }
    
    public Iterator<BytesAVP> getAVPs()
    {
        return this.avps.iterator();
    }

    public void encodeUserPasswordAvps()
    {
        this.assertAccessRequest();
        this.assertSecretPresent();
        this.assertAuthenticatorPresent();
        for(BytesAVP userPasswordAvp:userPasswordAvps)
        {
            userPasswordAvp.setData(RadiusMessageUtils.encodeUserPassword(this.authenticator, this.secret, userPasswordAvp.getData()));
        }
    }
    
    public void decodeUserPasswordAvps()
    {
        this.assertAccessRequest();
        this.assertSecretPresent();
        this.assertAuthenticatorPresent();
        for(BytesAVP userPasswordAvp:userPasswordAvps)
        {
            userPasswordAvp.setData(RadiusMessageUtils.decodeUserPassword(this.authenticator, this.secret, userPasswordAvp.getData()));
        }
    }

    public void computeRequestAuthenticator()
    {
        if(this.getCode() == 1)
        {
            this.authenticator = RadiusMessageUtils.generateUniqueAuthenticator();
        }
        else
        {
            this.authenticator = computeAuthenticator(new ConstantArray((byte) 0, 16));
        }
    }
    
    public void computeResponseAuthenticator(Array requestAuthenticator)
    {
        this.authenticator = computeAuthenticator(requestAuthenticator);
    }
    
    public boolean hasValidRequestAuthenticator()
    {
        if(this.getCode() == 1)
        {
            // an Access-Request authenticator being random, it is always correct
            return true;
        }

        return this.authenticator.equals(computeAuthenticator(new ConstantArray((byte) 0, 16)));
    }
    
    public boolean hasValidResponseAuthenticator(Array requestAuthenticator)
    {
        return this.authenticator.equals(computeAuthenticator(requestAuthenticator));
    }

    private Array computeAuthenticator(Array inputAuthenticator)
    {
        this.assertSecretPresent();
        SupArray temp = new SupArray().addLast(this.header).addLast(inputAuthenticator);
        for(BytesAVP avp:avps)
        {
            temp.addLast(avp.getArray());
        }
        temp.addLast(this.secret);

        return new DigestArray(temp, "MD5");
    }
    
    public Array getArray()
    {
        this.assertAuthenticatorPresent();
        SupArray array = new SupArray();
        array.addLast(this.header);
        array.addLast(this.authenticator);
        for(BytesAVP avp:avps)
        {
            array.addLast(avp.getArray());
        }
        return array;
    }
    
    private void assertAccessRequest()
    {
        if(this.getCode() != 1)
        {
            throw new RuntimeException("Assert Access-Request code failed (" + this.getCode() +")");
        }
    }
    
    private void assertSecretPresent()
    {
        if(null == this.secret)
        {
            throw new RuntimeException("Assert secret is present failed");
        }
    }
    
    private void assertAuthenticatorPresent()
    {
        if(null == this.authenticator)
        {
            throw new RuntimeException("Assert authenticator is present failed");
        }
    }

    public InetSocketAddress getLocalAddress()
    {
        return localAddress;
    }

    public void setLocalAddress(InetSocketAddress srcAddress)
    {
        this.localAddress = srcAddress;
    }

    public InetSocketAddress getRemoteAddress()
    {
        return remoteAddress;
    }

    public void setRemoteAddress(InetSocketAddress dstAddress)
    {
        this.remoteAddress = dstAddress;
    }
}
