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
import gp.net.radius.exceptions.RadiusException;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 *
 * @author Gwenhael Pasquiers
 */
public class RadiusClient
{
    private RadiusSocket radiusSocket;
    private HashMap<String, RadiusClientContext> contexts;
    
    public RadiusClient(RadiusSocket radiusSocket)
    {
        this.radiusSocket = radiusSocket;
        this.contexts = new HashMap<String, RadiusClientContext>();
        final RadiusClient _this = this;
        Runnable runnable = new Runnable()
        {
            public void run()
            {
                try
                {
                    while(true)
                    {
                        RadiusMessage radiusMessage = _this.radiusSocket.receive();
                        String key = radiusMessage.getRemoteAddress().getAddress().getHostAddress() + ":" + radiusMessage.getRemoteAddress().getPort();
                        RadiusClientContext context = _this.contexts.get(key);
                        if(null != context)
                        {
                            context.endTransaction(radiusMessage);
                        }
                        else
                        {
                            System.err.println("DEBUG: silently discarding message (no context)");
                        }
                    }
                }
                catch(Exception exception)
                {
                    for(Entry<String, RadiusClientContext> entry:_this.contexts.entrySet())
                    {
                        entry.getValue().endAllTransactions(exception);
                    }
                    
                    _this.contexts.clear();
                }
            }
        };
        
        Thread thread = new Thread(runnable);
        thread.setDaemon(true);
        thread.start();
    }
    
    public RadiusMessage send(RadiusMessage request, RadiusClientRetransmissionParameters radiusClientRetransmissionParameters) throws RadiusException
    {
        return createTransaction(request, radiusClientRetransmissionParameters).waitResponse();
    }
    
    public RadiusClientTransaction createTransaction(RadiusMessage request, RadiusClientRetransmissionParameters radiusClientRetransmissionParameters) throws RadiusException
    {
        RadiusClientContext context;
        
        synchronized(contexts)
        {
            String key = request.getRemoteAddress().getAddress().getHostAddress() + ":" + request.getRemoteAddress().getPort();
            context = this.contexts.get(key);
            if(null == context)
            {
                context = new RadiusClientContext(this.radiusSocket);
                this.contexts.put(key, context);
            }
        }
        
        return context.createTransaction(request, radiusClientRetransmissionParameters);
    }
    
    public SocketAddress getLocalsocketAddress()
    {
        return this.radiusSocket.getLocalsocketAddress();
    }
}
