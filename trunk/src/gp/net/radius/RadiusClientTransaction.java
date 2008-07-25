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
import gp.utils.scheduler.Scheduler;
import gp.utils.scheduler.Task;
import java.util.concurrent.Semaphore;

/**
 *
 * @author Gwenhael Pasquiers
 */
public class RadiusClientTransaction
{
    private static Scheduler retransmissionsScheduler = new Scheduler(2);
    
    private RadiusMessage request;
    private RadiusSocket radiusSocket;
    private Semaphore semaphore;
    private RadiusClientContext radiusClientContext;
    private RadiusClientTransactionResult radiusClientTransactionResult;
    
    public RadiusClientTransaction(RadiusMessage request, RadiusSocket radiusSocket, RadiusClientContext radiusClientContext)
    {
        this.request = request;
        this.radiusSocket = radiusSocket;
        this.radiusClientContext = radiusClientContext;
        this.radiusClientTransactionResult = null;
        this.semaphore = new Semaphore(0);
        
        final RadiusClientTransaction _this = this;
        
        Task task = new Task()
        {
            private int counter = 0;
            
            @Override
            public void execute()
            {
                if(null != _this.radiusClientTransactionResult) return;
                
                try
                {
                    System.err.println("gogo");
                    _this.radiusSocket.send(_this.request);
                
                    // compute next retransmission delay
                    counter++;
                    if(counter > 4)
                    {
                        throw new Exception("timeout ! ! !");
                    }
                
                    // then schedule it
                    RadiusClientTransaction.retransmissionsScheduler.scheduleIn(this, 1000);
                }
                catch(Exception exception)
                {
                    _this.endTransaction(exception);
                }
            }
        };
        
        RadiusClientTransaction.retransmissionsScheduler.execute(task, false);
    }

    synchronized public void endTransaction(RadiusMessage response)
    {
        if(null != this.radiusClientTransactionResult) return;
        
        response.setSecret(this.request.getSecret());
        
        if(!response.hasValidResponseAuthenticator(this.request.getAuthenticator()))
        {
            System.err.println("DEBUG: silently discarding response (invalid identifier)");
            return;
        }
        
        this.radiusClientTransactionResult = new RadiusClientTransactionResult(this.request, response, null);
        this.radiusClientContext.removeTransaction(this.request.getIdentifier());
        this.semaphore.release();
    }

    synchronized public void endTransaction(Exception e)
    {
        if(null != this.radiusClientTransactionResult) return;
        this.radiusClientTransactionResult = new RadiusClientTransactionResult(this.request, null, new RadiusException("Exception in transaction", e));
        this.radiusClientContext.removeTransaction(this.request.getIdentifier());
        this.semaphore.release();
    }
    
    public RadiusMessage waitResponse() throws RadiusException
    {
        try
        {
            this.semaphore.acquire();
        }
        catch(Exception e)
        {
            throw new RadiusException(e);
        }
        
        if(null != this.radiusClientTransactionResult.exception)
        {
            throw this.radiusClientTransactionResult.exception;
        }
        else
        {
            return this.radiusClientTransactionResult.response;
        }
    }
}
