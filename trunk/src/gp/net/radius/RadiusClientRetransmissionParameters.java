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

/**
 *
 * @author gege
 */
public class RadiusClientRetransmissionParameters
{
    /*
     *    The retransmission behavior is controlled and described by the
     *    following variables:
     * 
     *         RT     Retransmission timeout
     *         IRT    Initial retransmission time  (default 2 seconds)
     *         MRC    Maximum retransmission count (default 5 attempts)
     *         MRT    Maximum retransmission time (default 16 seconds)
     *         MRD    Maximum retransmission duration (default 30 seconds)
     *         RAND   Randomization factor
     */
    final public long IRT;
    final public long MRC;
    final public long MRT;
    final public long MRD;
    
    public RadiusClientRetransmissionParameters()
    {
        this(2000 ,2 , 16000, 30000);
    }
    
    public RadiusClientRetransmissionParameters(long IRT, long MRC, long MRT, long MRD)
    {
        this.IRT = IRT;
        this.MRC = MRC;
        this.MRT = MRT;
        this.MRD = MRD;
    }
}
