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

import gp.utils.exception.AssociationHashMapUniquenessException;
import gp.utils.map.AssociationHashMap;
import java.util.HashMap;

/**
 *
 * @author gege
 */
public class RadiusAttributes
{
    
    private AssociationHashMap<Integer, String> attributesCodesAndNames;
    private HashMap<Integer, String> attributesCodesAndTypes;
    private HashMap<Integer, RadiusValues> attributesRadiusValues;

    public RadiusAttributes()
    {
        attributesCodesAndNames = new AssociationHashMap<Integer, String>();
        attributesCodesAndTypes = new HashMap<Integer, String>();
        attributesRadiusValues  = new HashMap<Integer, RadiusValues>();
    }
    
    public void addAttribute(String name, Integer code, String type) throws AssociationHashMapUniquenessException
    {
        this.attributesCodesAndTypes.put(code, type);
        this.attributesCodesAndNames.put(code, name.toLowerCase());
        this.attributesRadiusValues.put(code, new RadiusValues());
    }
    
    public RadiusValues getAttributeRadiusValues(Integer code)
    {
        return this.attributesRadiusValues.get(code);
    }

    public String getAttributeName(Integer code)
    {
        return this.attributesCodesAndNames.getRight(code);
    }

    public String getAttributeType(Integer code)
    {
        return this.attributesCodesAndTypes.get(code);
    }
    
    public Integer getAttributeCode(String name)
    {
        return this.attributesCodesAndNames.getLeft(name.toLowerCase());
    }
    
}
