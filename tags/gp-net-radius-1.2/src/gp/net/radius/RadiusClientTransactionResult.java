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

/**
 * A RadiusClientTransactionResult is a mean of stocking the result of a transaction.<br/>
 * It always contains the request and can contain a response or an exception,
 * depending on the success (or not) of this transaction.
 * @author Gwenhael Pasquiers
 */
public class RadiusClientTransactionResult
{
    public final RadiusMessage request;
    public final RadiusMessage response;
    public final RadiusException exception;
    
    public RadiusClientTransactionResult(RadiusMessage request, RadiusMessage response, RadiusException exception)
    {
        this.request = request;
        this.response = response;
        this.exception = exception;
    }
}
