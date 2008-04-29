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
        this.vendorDataCollection = new VendorDataCollection(bytesAVP.getData());
    }
    
    public VendorDataCollection getVendorDataCollection()
    {
        return this.vendorDataCollection;
    }
}
