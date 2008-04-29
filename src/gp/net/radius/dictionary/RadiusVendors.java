/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gp.net.radius.dictionary;

import gp.utils.exception.AssociationHashMapUniquenessException;
import gp.utils.map.AssociationHashMap;
import java.util.HashMap;

/**
 *
 * @author gege
 */
public class RadiusVendors
{
    
    private AssociationHashMap<Integer, String> vendorCodesAndNames;
    private HashMap<Integer, RadiusAttributes> vendorCodesAndAttributes;

    public RadiusVendors()
    {
        this.vendorCodesAndNames = new AssociationHashMap<Integer, String>();
        this.vendorCodesAndAttributes = new HashMap<Integer, RadiusAttributes>();
    }
    
    public void addVendor(String name, Integer code) throws AssociationHashMapUniquenessException
    {
        this.vendorCodesAndNames.put(code, name.toLowerCase());
        this.vendorCodesAndAttributes.put(code, new RadiusAttributes());
    }
    
    public RadiusAttributes getRadiusAttributes(Integer code)
    {
        return this.vendorCodesAndAttributes.get(code);
    }
    
    public String getVendorName(Integer code)
    {
        return this.vendorCodesAndNames.getRight(code);
    }

    public Integer getVendorCode(String name)
    {
        return this.vendorCodesAndNames.getLeft(name.toLowerCase());
    }
    
}
