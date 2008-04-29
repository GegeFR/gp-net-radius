/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gp.net.radius.data;

import gp.utils.array.impl.Array;
import gp.utils.array.impl.Integer32Array;
import gp.utils.array.impl.SubArray;
import gp.utils.array.impl.SupArray;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author gege
 */
public class VendorDataCollection
{
    private List<BytesAVP> bytesAVPs;
    
    private Integer32Array vendorId;
    
    public VendorDataCollection(int vendorId, List<BytesAVP> vendorDatas)
    {
        this.vendorId = new Integer32Array(0);
        this.setVendorId(vendorId);
        this.bytesAVPs = new LinkedList<BytesAVP>();
        this.bytesAVPs.addAll(vendorDatas);
    }
    
    public VendorDataCollection(Array data)
    {
        this.bytesAVPs = new LinkedList<BytesAVP>();

        this.vendorId = new Integer32Array(new SubArray(data, 0, 4));
        
        int offset = 4;
        while(offset < data.length)
        {
            BytesAVP bytesAVP = new BytesAVP(new SubArray(data, offset));
            this.bytesAVPs.add(bytesAVP);
            offset += bytesAVP.getLength();
        }
    }
    
    public int getVendorId()
    {
        return this.vendorId.getValue(); 
    }
    
    public void setVendorId(int value)
    {
        this.vendorId.setValue(value);
    }
    
    public List<BytesAVP> getBytesAVPs()
    {
        return Collections.unmodifiableList(bytesAVPs);
    }
            
    public Array getArray()
    {
        SupArray array = new SupArray();
        array.addFirst(this.vendorId);
        for(BytesAVP bytesAVP:this.bytesAVPs)
        {
            array.addLast(bytesAVP.getArray());
        }
        return array;
    }

}
