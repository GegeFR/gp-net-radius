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

package gp.net.radius.exceptions;

/**
 *
 * @author Gwenhael Pasquiers
 */
public class RadiusException extends Exception
{
    public RadiusException(){super();}
    public RadiusException(Exception e){super(e);}
    public RadiusException(String message){super(message);}
    public RadiusException(String message, Exception e){super(message, e);}
}
