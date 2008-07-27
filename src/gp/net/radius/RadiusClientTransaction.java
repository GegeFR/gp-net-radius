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
    private RadiusClientRetransmissionParameters parameters;

    public RadiusClientTransaction(RadiusMessage request, RadiusSocket radiusSocket, RadiusClientContext radiusClientContext, RadiusClientRetransmissionParameters radiusClientRetransmissionParameters)
    {
        this.request = request;
        this.radiusSocket = radiusSocket;
        this.radiusClientContext = radiusClientContext;
        this.radiusClientTransactionResult = null;
        this.semaphore = new Semaphore(0);
        this.parameters = radiusClientRetransmissionParameters;
        
        final RadiusClientTransaction _this = this;
        
        Task task = new Task()
        {
            private long initialDate = System.currentTimeMillis();
            private long RT;
            private long RC = 0;
            
            @Override
            public void execute()
            {
                long now = System.currentTimeMillis();
                
                if(null != _this.radiusClientTransactionResult) return;
                
                if(0 != _this.parameters.MRD && (now - this.initialDate) > _this.parameters.MRD)
                    _this.endTransaction(new RadiusException("Timeout: Maximum Retransmission Duration reached: " +  _this.parameters.MRD + " ms"));

                if(0 != _this.parameters.MRC && this.RC > _this.parameters.MRC)
                    _this.endTransaction(new RadiusException("Timeout: Maximum Retransmission Count reached: " + _this.parameters.MRC));
                
                try
                {
                    _this.radiusSocket.send(_this.request);
                
                    // compute next retransmission delay
                    this.RC++;
                    double RAND = ((Math.random() - 0.5) / 5);
                    if(1 == this.RC)                                                             // if first retransmission
                        this.RT = (long) (_this.parameters.IRT + _this.parameters.IRT*RAND);     //   RT = IRT + RAND*IRT
                    else                                                                         // else
                        this.RT = (long) (2*this.RT + this.RT*RAND);                             //   RT = 2*RTprev + RAND*RTprev
                    

                    if(this.RT > _this.parameters.MRT)                                           // if (RT > MRT)
                        this.RT = (long) (_this.parameters.MRT + _this.parameters.MRT * RAND);   //   RT = MRT + RAND*MRT

                    if(0 != _this.parameters.MRD && (now + this.RT) > (this.initialDate + _this.parameters.MRD))
                        this.RT = this.initialDate + _this.parameters.MRD - now;
                    
                    // then schedule it
                    RadiusClientTransaction.retransmissionsScheduler.scheduleIn(this, this.RT);
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
        this.radiusClientTransactionResult = new RadiusClientTransactionResult(this.request, null, new RadiusException("Exception in transaction of remote " + this.request.getRemoteAddress() + " and id " + this.request.getIdentifier(), e));
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
