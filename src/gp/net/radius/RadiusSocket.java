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

package gp.net.radius;

import gp.net.radius.data.RadiusMessage;
import gp.utils.array.impl.ReadOnlyDefaultArray;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

/**
 *
 * @author gege
 */
public class RadiusSocket
{
    private DatagramSocket socket;
    
    private int bufferSize;
    
    public RadiusSocket() throws SocketException
    {
        this("0.0.0.0", 0);
    }

    public RadiusSocket(String localhost) throws SocketException
    {
        this(localhost, 0);
    }

    public RadiusSocket(int port) throws SocketException
    {
        this("0.0.0.0", port);
    }

    public RadiusSocket(String localhost, int port) throws SocketException
    {
        this.bufferSize = 4 * 1024;
        this.socket = new DatagramSocket(new InetSocketAddress(localhost, port));
    }

    public void setBufferSize(int size)
    {
        this.bufferSize = size;
    }
    
    public int getPort()
    {
        return this.socket.getLocalPort();
    }
    
    public void send(RadiusMessage radiusMessage) throws SocketException, IOException
    {
        radiusMessage.setLocalAddress((InetSocketAddress) this.socket.getLocalSocketAddress());
        
        if(null == radiusMessage.getRemoteAddress())
        {
            throw new SocketException("Can't send message, DstSocketAddress is null");
        }
        
        byte[] buffer = radiusMessage.getArray().getBytes();
        DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length, radiusMessage.getRemoteAddress());
        this.socket.send(datagramPacket);
    }

    public void send(RadiusMessage radiusMessage, String remoteHost, int remotePort) throws SocketException, IOException
    {
        radiusMessage.setLocalAddress((InetSocketAddress) this.socket.getLocalSocketAddress());
        radiusMessage.setRemoteAddress(new InetSocketAddress(remoteHost, remotePort));
        byte[] buffer = radiusMessage.getArray().getBytes();
        DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length, radiusMessage.getRemoteAddress());
        this.socket.send(datagramPacket);
    }
    
    public RadiusMessage receive() throws IOException
    {
        byte[] buffer = new byte[this.bufferSize];
        DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
        
        socket.receive(datagramPacket);

        RadiusMessage radiusMessage = new RadiusMessage(new ReadOnlyDefaultArray(datagramPacket.getData()));
        radiusMessage.setRemoteAddress((InetSocketAddress) datagramPacket.getSocketAddress());
        radiusMessage.setLocalAddress((InetSocketAddress) this.socket.getLocalSocketAddress());

        return radiusMessage;
    }
    
    public void close()
    {
        this.socket.close();
    }
    
    public boolean isOpen()
    {
        return !this.socket.isClosed();
    }
}
