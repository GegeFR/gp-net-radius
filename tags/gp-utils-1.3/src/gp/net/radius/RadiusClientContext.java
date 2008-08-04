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
import gp.net.radius.data.RadiusMessageUtils;
import gp.net.radius.exceptions.RadiusException;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;

/**
 * A RadiusClientContext is associated to a specific remote host:port.<br/>
 * It contains a list of available identifiers for this remote host:port and
 * the list of pending transactions.
 * @author Gwenhael Pasquiers
 */
public class RadiusClientContext {

    private HashMap<Integer, RadiusClientTransaction> transactions;
    private LinkedBlockingQueue<Integer> identifiers;
    private RadiusSocket radiusSocket;
    
    public RadiusClientContext(RadiusSocket radiusSocket)
    {
        this.radiusSocket = radiusSocket;
        this.transactions = new HashMap<Integer, RadiusClientTransaction>();
        
        /*
         * Init the stak of identifiers.
         */
        this.identifiers = new LinkedBlockingQueue<Integer>();
        for(int i=0; i<256; i++) this.identifiers.offer(i);
    }
    
    public RadiusClientTransaction createTransaction(RadiusMessage request,RadiusClientRetransmissionParameters radiusClientRetransmissionParameters) throws RadiusException
    {
        Integer identifier = this.identifiers.poll();
        
        if(null == identifier) throw new RadiusException("No more identifier available");
        
        request.setIdentifier(identifier);
        request.computeRequestAuthenticator();
        if(1 == request.getCode()) request.encodeUserPasswordAvps();
        
        RadiusClientTransaction radiusClientTransaction = new RadiusClientTransaction(request, this.radiusSocket, this, radiusClientRetransmissionParameters);
        
        this.transactions.put(identifier, radiusClientTransaction);
        
        return radiusClientTransaction;
    }
    
    public void endTransaction(RadiusMessage response)
    {
        RadiusClientTransaction radiusClientTransaction = this.transactions.get(response.getIdentifier());
        if(null != radiusClientTransaction)
        {
            radiusClientTransaction.endTransaction(response);
        }
        else
        {
            if(RadiusLogger.logger.isLoggable(Level.WARNING)) RadiusLogger.logger.log(Level.WARNING, "silently discarding response (unknown transaction)\n" + RadiusMessageUtils.toString(response));
        }
    }
    
    public void endAllTransactions(Exception exception)
    {
        for(int i=0; i<256; i++)
        {
            RadiusClientTransaction radiusClientTransaction = this.transactions.get(i);
            if(null != radiusClientTransaction) radiusClientTransaction.endTransaction(exception);
        }
    }
    
    public void removeTransaction(Integer identifier)
    {
        if(null != this.transactions.remove(identifier))
        {
            this.identifiers.offer(identifier);
        }
    }
}
