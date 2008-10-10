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
import gp.utils.arrays.ConstantArray;
import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;
import gp.utils.arrays.DigestArray;
import gp.utils.arrays.Integer08Array;
import gp.utils.arrays.Integer16Array;
import gp.utils.arrays.MacArray;
import gp.utils.arrays.SupArray;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Gwenhael Pasquiers
 */
public class RadiusMessage
{
    private Array header;
    private Integer08Array code;
    private Integer08Array identifier;
    private Integer16Array length;
    
    private Array authenticator;
    
    private Array secret;
    
    private LinkedList<AVPBytes> avps;

    private AVPBytes userPasswordAvp;

    private AVPBytes messageAuthenticatorAvp;
    
    private boolean containsEAPMessage;
    
    private InetSocketAddress localAddress;
    
    private InetSocketAddress remoteAddress;
    
    public RadiusMessage()
    {
        this.secret = null;
        
        this.header = new DefaultArray(4);
        this.code           = new Integer08Array(this.header.subArray(0, 1));
        this.identifier     = new Integer08Array(this.header.subArray(1, 1));
        this.length         = new Integer16Array(this.header.subArray(2, 2));

        this.authenticator = null;

        this.avps = new LinkedList<AVPBytes>();
        
        this.userPasswordAvp = null;
        this.messageAuthenticatorAvp = null;
        this.containsEAPMessage = false;
        
        this.setLength(20);
    }
    
    public RadiusMessage(Array data) throws RadiusException
    {
        this.secret = null;
        
        this.header         = data.subArray(0, 4);
        this.code           = new Integer08Array(this.header.subArray(0, 1));
        this.identifier     = new Integer08Array(this.header.subArray(1, 1));
        this.length         = new Integer16Array(this.header.subArray(2, 2));
        
        this.authenticator  = data.subArray(4, 16);
        
        this.avps = new LinkedList<AVPBytes>();
        
        this.userPasswordAvp = null;
        this.messageAuthenticatorAvp = null;
        this.containsEAPMessage = false;

        if(this.getLength() != data.length)
        {
            throw new RadiusException("Invalid length of message (" + data.length + ") or invalid length in header (" + this.length.getValue() + ")");
        }
        
        int offset = 20;
        int length = this.getLength();
        while(offset < length)
        {
            AVPBytes avp = new AVPBytes(data.subArray(offset));
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

    public void addAVP(AVPBytes avp)
    {
        this.addAVP(avp, true);
    }

    private void addAVP(AVPBytes avp, boolean setLength)
    {
        int type = avp.getType();
        
        // special behavior for User-Password AVP
        if(type == 2)
        {
            avp.setData(RadiusMessageUtils.padUserPassword(avp.getData()));
            if(null == this.userPasswordAvp) this.userPasswordAvp = avp;
            else                             throw new RuntimeException("Only one User-Password AVP (2) is allowed.");

            if(null != this.messageAuthenticatorAvp) throw new RuntimeException("User-Password(2) cannot be in the same message as Message-Authenticator(80).");
            if(this.containsEAPMessage) throw new RuntimeException("User-Password(2) cannot be in the same message as EAP-Message(79).");
        }
        
        // special behavior for Message-Authenticator AVP
        if(type == 80)
        {
            if(null != this.userPasswordAvp) throw new RuntimeException("Message-Authenticator(80) cannot be in the same message as User-Password(2).");
            
            if(null == this.messageAuthenticatorAvp) this.messageAuthenticatorAvp = avp;
            else                                     throw new RuntimeException("Only one Message-Authenticator AVP (80) is allowed.");
        }

        // special behavior for EAP-Message AVP
        if(type == 79)
        {
            if(null != this.userPasswordAvp) throw new RuntimeException("Message-Authenticator(80) cannot be in the same message as User-Password(2).");

            this.containsEAPMessage = true;
        }
        
        avps.addLast(avp);
        if(setLength)
        {
            this.setLength(this.getLength() + avp.getLength());
        }
    }
    
    public List<AVPBytes> getAVPs()
    {
        return Collections.unmodifiableList(avps);
    }

    public void encodeUserPasswordAvp()
    {
        if(null != userPasswordAvp)
        {
            this.assertAccessRequest();
            this.assertSecretPresent();
            this.assertAuthenticatorPresent();
            this.userPasswordAvp.setData(RadiusMessageUtils.encodeUserPassword(this.authenticator, this.secret, this.userPasswordAvp.getData()));
        }
    }
    
    public void decodeUserPasswordAvp()
    {
        this.assertAccessRequest();
        this.assertSecretPresent();
        this.assertAuthenticatorPresent();
        this.userPasswordAvp.setData(RadiusMessageUtils.decodeUserPassword(this.authenticator, this.secret, this.userPasswordAvp.getData()));
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
    
    public void computeRequestMessageAuthenticator()
    {
        this.assertAuthenticatorPresent();
        if(this.getCode() == 1)
        {
            if(this.containsEAPMessage || null != this.messageAuthenticatorAvp)
            {
                if(null == this.messageAuthenticatorAvp)
                {
                    AVPBytes avpBytes = new AVPBytes();
                    avpBytes.setType(80);
                    avpBytes.setData(new ConstantArray((byte) 0, 16));
                    this.addAVP(avpBytes);
                }
                else
                {
                    this.messageAuthenticatorAvp.setData(new ConstantArray((byte) 0, 16));    
                }
                this.messageAuthenticatorAvp.setData(computeMessageAuthenticator(this.authenticator));
            }
        }
        // do nothing if it is not an Access request
    }
    
    public void computeResponseMessageAuthenticator(Array requestAuthenticator)
    {
        this.assertAuthenticatorPresent();
        if(this.getCode() == 2 || this.getCode() == 3 || this.getCode() == 11)
        {
            if(this.containsEAPMessage || null != this.messageAuthenticatorAvp)
            {
                if(null == this.messageAuthenticatorAvp)
                {
                    AVPBytes avpBytes = new AVPBytes();
                    avpBytes.setType(80);
                    avpBytes.setData(new ConstantArray((byte) 0, 16));
                    this.addAVP(avpBytes);
                }
                else
                {
                    this.messageAuthenticatorAvp.setData(new ConstantArray((byte) 0, 16));    
                }
                this.messageAuthenticatorAvp.setData(computeMessageAuthenticator(requestAuthenticator));
            }
        }
        // do nothing if it is not an Access request
    }
    
    public boolean hasValidRequestAuthenticator()
    {
        if(this.getCode() == 1)
        {
            // an Access-Request authenticator, being random, is always correct
            return true;
        }

        return this.authenticator.equals(computeAuthenticator(new ConstantArray((byte) 0, 16)));
    }

    public boolean hasValidRequestMessageAuthenticator()
    {
        if(null == this.messageAuthenticatorAvp)
        {
            return true;
        }
        
        Array receivedAuthenticator = this.messageAuthenticatorAvp.getData();
        
        this.messageAuthenticatorAvp.setData(new ConstantArray((byte) 0, 16));
        
        Array wantedAuthenticator = this.computeMessageAuthenticator(this.authenticator);
        
        this.messageAuthenticatorAvp.setData(receivedAuthenticator);
        
        System.err.println("receivedAuthenticator="+receivedAuthenticator);
        System.err.println("wantedAuthenticator="+wantedAuthenticator);
        
        return receivedAuthenticator.equals(wantedAuthenticator);
    }
        
    public boolean hasValidResponseMessageAuthenticator(Array requestAuthenticator)
    {
        if(null == this.messageAuthenticatorAvp)
        {
            return true;
        }
        
        Array receivedAuthenticator = this.messageAuthenticatorAvp.getData();
        
        this.messageAuthenticatorAvp.setData(new ConstantArray((byte) 0, 16));
        
        Array wantedAuthenticator = this.computeMessageAuthenticator(requestAuthenticator);
        
        this.messageAuthenticatorAvp.setData(receivedAuthenticator);
        
        return receivedAuthenticator.equals(wantedAuthenticator);
    }

    public boolean hasValidResponseAuthenticator(Array requestAuthenticator)
    {
        return this.authenticator.equals(computeAuthenticator(requestAuthenticator));
    }

    private Array computeAuthenticator(Array inputAuthenticator)
    {
        this.assertSecretPresent();
        SupArray temp = new SupArray().addLast(this.header).addLast(inputAuthenticator);
        for(AVPBytes avp:avps)
        {
            temp.addLast(avp.getArray());
        }
        temp.addLast(this.secret);

        return new DigestArray(temp, "MD5");
    }
    
    private Array computeMessageAuthenticator(Array inputAuthenticator)
    {
        this.assertSecretPresent();
        SupArray temp = new SupArray().addLast(this.header).addLast(inputAuthenticator);
        for(AVPBytes avp:avps)
        {
            temp.addLast(avp.getArray());
        }
System.out.println("secret =" + secret + "\n" + "compute on "  + RadiusMessageUtils.toString(this));
        
        return new MacArray(temp, "HmacMD5", secret);
    }

    public Array getArray()
    {
        this.assertAuthenticatorPresent();
        SupArray array = new SupArray();
        array.addLast(this.header);
        array.addLast(this.authenticator);
        for(AVPBytes avp:avps)
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
