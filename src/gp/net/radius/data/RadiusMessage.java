/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gp.net.radius.data;

import gp.utils.array.impl.ConstantArray;
import gp.utils.array.impl.Array;
import gp.utils.array.impl.DefaultArray;
import gp.utils.array.impl.DigestArray;
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
        this.avps = new LinkedList<BytesAVP>();
        this.userPasswordAvps = new LinkedList<BytesAVP>();
        this.authenticator = null;
        this.setLength(20);
    }
    
    public RadiusMessage(Array data)
    {
        this.secret = null;
        this.header = new SubArray(data, 0, 4);
        this.authenticator = new SubArray(data, 4, 16);
        this.avps = new LinkedList<BytesAVP>();
        this.userPasswordAvps = new LinkedList<BytesAVP>();
        
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
        this.header.set(0, value);
    }

    public int getCode()
    {
        return this.header.get(0);
    }
    
    public void setIdentifier(int value)
    {
        this.header.set(1, value);
    }

    public int getIdentifier()
    {
        return this.header.get(1);
    }
    
    private void setLength(int value)
    {
        this.header.set(2, (value >> 8) & 0xFF);
        this.header.set(3, value & 0xFF);
    }

    public int getLength()
    {
        return ((int) (this.header.get(2) & 0xFF) << 8) + (int) (this.header.get(3) & 0xFF);
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
        // special behaviour for User-Password AVP
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
