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
    private List<VendorData> vendorDatas;
    
    private Integer32Array vendorId;
    
    public VendorDataCollection(int vendorId, List<VendorData> vendorDatas)
    {
        this.vendorId = new Integer32Array(0);
        this.setVendorId(vendorId);
        this.vendorDatas = new LinkedList<VendorData>();
        this.vendorDatas.addAll(vendorDatas);
    }
    
    public VendorDataCollection(Array data)
    {
        this.vendorDatas = new LinkedList<VendorData>();

        this.vendorId = new Integer32Array(new SubArray(data, 0, 4));
        
        int offset = 4;
        while(offset < data.length)
        {
            VendorData vendorData = new VendorData(new SubArray(data, offset));
            this.vendorDatas.add(vendorData);
            offset += vendorData.getLength();
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
    
    public List<VendorData> getvendorDatas()
    {
        return Collections.unmodifiableList(vendorDatas);
    }
            
    public Array getArray()
    {
        SupArray array = new SupArray();
        array.addFirst(this.vendorId);
        for(VendorData vendorData:this.vendorDatas)
        {
            array.addLast(vendorData.getArray());
        }
        return array;
    }

}
