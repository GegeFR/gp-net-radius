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
import gp.utils.scheduler.Scheduler;
import gp.utils.scheduler.Task;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;

/**
 *
 * @author Gwenhael Pasquiers
 */
public class RadiusClientTransaction
{
    private static Scheduler retransmissionsScheduler = new Scheduler(2);
    
    private RadiusMessage request;
    private RadiusMessage response;
    private Exception exception;
    
    private RadiusSocket radiusSocket;
    private Semaphore semaphore;
    private RadiusClientContext radiusClientContext;
    private RadiusClientTransactionResult radiusClientTransactionResult;
    private RadiusClientRetransmissionParameters parameters;

    private long RC;
    
    public RadiusClientTransaction(RadiusMessage request, RadiusSocket radiusSocket, RadiusClientContext radiusClientContext, RadiusClientRetransmissionParameters radiusClientRetransmissionParameters)
    {
        this.request = request;
        this.response = null;
        this.exception = null;
        
        this.radiusSocket = radiusSocket;
        this.radiusClientContext = radiusClientContext;
        this.radiusClientTransactionResult = null;
        this.semaphore = new Semaphore(0);
        this.parameters = radiusClientRetransmissionParameters;
        this.RC = 0;
        
        final RadiusClientTransaction _this = this;
        
        Task task = new Task()
        {
            private long initialDate = System.currentTimeMillis();
            private long RT;
            private long RC = 0;
            
            public void execute()
            {
                long now = System.currentTimeMillis();
                
                if(null != _this.radiusClientTransactionResult)
                {
                    if(RadiusLogger.logger.isLoggable(Level.FINE)) RadiusLogger.logger.log(Level.FINE, "stop retransmitting, radiusClientTransactionResult is not null");
                    return;
                }
                
                if(0 != _this.parameters.MRD && (now - this.initialDate) > _this.parameters.MRD)
                {
                    _this.endTransaction(new RadiusException("Timeout: Maximum Retransmission Duration reached: " +  _this.parameters.MRD + " ms"));
                    return;
                }

                if(0 != _this.parameters.MRC && this.RC > _this.parameters.MRC)
                {
                    _this.endTransaction(new RadiusException("Timeout: Maximum Retransmission Count reached: " + _this.parameters.MRC));
                    return;
                }
                
                try
                {
                    // compute next retransmission delay
                    this.RC++;
                    _this.RC = this.RC;

                    double RAND = ((Math.random() - 0.5) / 5);
                    if(1 == this.RC)                                                             // if first retransmission
                        this.RT = (long) (_this.parameters.IRT + _this.parameters.IRT*RAND);     //   RT = IRT + RAND*IRT
                    else                                                                         // else
                        this.RT = (long) (2*this.RT + this.RT*RAND);                             //   RT = 2*RTprev + RAND*RTprev
                    

                    if(this.RT > _this.parameters.MRT)                                           // if (RT > MRT)
                        this.RT = (long) (_this.parameters.MRT + _this.parameters.MRT * RAND);   //   RT = MRT + RAND*MRT

                    if(0 != _this.parameters.MRD && (now + this.RT) > (this.initialDate + _this.parameters.MRD))
                        this.RT = this.initialDate + _this.parameters.MRD - now;

                    //send the current retransmission
                    _this.radiusSocket.send(_this.request);
                    if(RadiusLogger.logger.isLoggable(Level.WARNING)) RadiusLogger.logger.log(Level.WARNING, "sent retransmission after "+ (now - this.initialDate) +"ms for request\n" + RadiusMessageUtils.toString(_this.request));

                    // then schedule the next retransmission
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
        
        this.response = response;
        
        response.setSecret(this.request.getSecret());
        
        if(!response.hasValidResponseAuthenticator(this.request.getAuthenticator()))
        {
            if(RadiusLogger.logger.isLoggable(Level.WARNING)) RadiusLogger.logger.log(Level.WARNING, "silently discarding response (invalid header authenticator field)\n" + RadiusMessageUtils.toString(response));
            return;
        }
        
        if(!response.hasValidResponseMessageAuthenticator(this.request.getAuthenticator()))
        {
            if(RadiusLogger.logger.isLoggable(Level.WARNING)) RadiusLogger.logger.log(Level.WARNING, "silently discarding response (invalid Message-Authenticator AVP)\n" + RadiusMessageUtils.toString(response));
            return;
        }

        this.radiusClientTransactionResult = new RadiusClientTransactionResult(this.request, response, null);
        this.radiusClientContext.removeTransaction(this.request.getIdentifier());
        
        if(RadiusLogger.logger.isLoggable(Level.FINE)) RadiusLogger.logger.log(Level.FINE, "ending transaction with reponse\n" + RadiusMessageUtils.toString(response) + "\nfor request\n" + RadiusMessageUtils.toString(request));

        this.semaphore.release();
    }

    synchronized public void endTransaction(Exception e)
    {
        if(null != this.radiusClientTransactionResult) return;
        
        this.exception = e;
        
        if(RadiusLogger.logger.isLoggable(Level.WARNING)) RadiusLogger.logger.log(Level.WARNING, "ending transaction with exception", e);

        this.radiusClientTransactionResult = new RadiusClientTransactionResult(this.request, null, new RadiusException("Exception in transaction of remote " + this.request.getRemoteAddress() + " and id " + this.request.getIdentifier(), e));
        this.radiusClientContext.removeTransaction(this.request.getIdentifier());
        this.semaphore.release();
    }
    
    public RadiusMessage waitResponse() throws RadiusException
    {
        return this.waitResponse(true);
    }

    public RadiusMessage waitResponse(boolean throwsExceptions) throws RadiusException
    {
        try
        {
            this.semaphore.acquire();
        }
        catch(Exception e)
        {
            this.radiusClientTransactionResult = new RadiusClientTransactionResult(this.request, null, new RadiusException("Unexpected exception in transaction of remote " + this.request.getRemoteAddress() + " and id " + this.request.getIdentifier(), e));
            this.exception = e;
        }
        
        if(throwsExceptions && null != this.radiusClientTransactionResult.exception)
        {
            throw this.radiusClientTransactionResult.exception;
        }
        else
        {
            return this.radiusClientTransactionResult.response;
        }
    }
    
    public long getRetransmissionCount()
    {
        return this.RC;
    }
    
    public RadiusMessage getRequest()
    {
        return this.request;
    }

    public RadiusMessage getResponse()
    {
        return this.response;
    }

    public Exception getException()
    {
        return this.exception;
    }
    
    
}
