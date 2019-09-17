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

import gp.utils.map.AssociationHashMap;
import java.util.HashMap;

/**
 *
 * @author Gwenhael Pasquiers
 */
public class RadiusCodes
{
    
    private AssociationHashMap<Integer, String> vendorCodesAndNames;
    private HashMap<Integer, RadiusAttributes> vendorCodesAndAttributes;

    public RadiusCodes()
    {
        this.vendorCodesAndNames = new AssociationHashMap<Integer, String>();
        this.vendorCodesAndAttributes = new HashMap<Integer, RadiusAttributes>();
        
        this.addCode("access-request", 1);
        this.addCode("access-accept", 2);
        this.addCode("access-reject", 3);
        this.addCode("access-challenge", 11);
        this.addCode("accounting-request", 4);
        this.addCode("accounting-response", 5);
    }
    
    public void addCode(String name, Integer code)
    {
        this.vendorCodesAndNames.put(code, name.toLowerCase());
        this.vendorCodesAndAttributes.put(code, new RadiusAttributes());
    }
    
    public String getName(Integer code)
    {
        return this.vendorCodesAndNames.getRight(code);
    }

    public Integer getCode(String name)
    {
        return this.vendorCodesAndNames.getLeft(name.toLowerCase());
    }
    
}
