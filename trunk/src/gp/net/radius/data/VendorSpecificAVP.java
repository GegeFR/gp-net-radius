/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
