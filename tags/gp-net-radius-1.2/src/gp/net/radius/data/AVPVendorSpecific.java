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
import gp.utils.arrays.Array;
import gp.utils.arrays.Integer32Array;
import gp.utils.arrays.SupArray;

/**
 *
 * @author Gwenhael Pasquiers
 */
public class AVPVendorSpecific extends AVPBytes
{
    private Integer32Array vendorId;
    
    private Array vendorData;
    
    public AVPVendorSpecific(int code, int vendorId, Array vendorData)
    {
        super(code);
        this.vendorId = new Integer32Array(vendorId);
        this.vendorData = vendorData;
        super.setData(new SupArray().addLast(this.vendorData).addLast(this.vendorData));
    }

    public AVPVendorSpecific(AVPBytes bytesAVP) throws RadiusException
    {
        super(bytesAVP);
        this.vendorId = new Integer32Array(super.getData().subArray(0, 4));
        this.vendorData = super.getData().subArray(4);
        
        if(super.getLength() < 6)
        {
            throw new RadiusException("Invalid length for VendorSpecific AVP (" + super.getLength() + ") minimum is 6: 2 header bytes and 4 VendorId bytes.");
        }
    }
    
    public Array getVendorData()
    {
        return this.vendorData;
    }
    
    public int getVendorId()
    {
        return this.vendorId.getValue();
    }
}
