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

/**
 *
 * @author gege
 */
public class VendorSpecificAVP extends BytesAVP
{
    private VendorDataCollection vendorDataCollection;
    
    public VendorSpecificAVP(int code, VendorDataCollection vendorDataCollection)
    {
        super(code, vendorDataCollection.getArray());
        this.vendorDataCollection = vendorDataCollection;
    }

    public VendorSpecificAVP(BytesAVP bytesAVP)
    {
        super(bytesAVP);
        // we do not initialize vendorDataCollection because the data of this AVP
        // might be a vendor specific format (and not the RFC recommanded format)
        this.vendorDataCollection = null;
    }
    
    public VendorDataCollection getVendorDataCollection()
    {
        if(null != this.vendorDataCollection)
        {
            return this.vendorDataCollection;
        }
        else
        {
            //TODO should maybe thrown some exception
            return null;
        }
    }
}
