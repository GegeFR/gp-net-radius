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

package gp.net.radius.dictionary;

import gp.utils.map.AssociationHashMapUniquenessException;
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
