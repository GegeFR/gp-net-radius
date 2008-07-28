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

/**
 *
 * @author Gwenhael Pasquiers
 */
public class RadiusValues
{
    
    private AssociationHashMap<Integer, String> valuesCodesAndNames;

    public RadiusValues()
    {
        valuesCodesAndNames = new AssociationHashMap<Integer, String>();
    }
    
    public void addValue(String name, Integer code) throws AssociationHashMapUniquenessException
    {
        this.valuesCodesAndNames.put(code, name.toLowerCase());
    }
    
    public String getValueName(Integer code)
    {
        return this.valuesCodesAndNames.getRight(code);
    }

    public Integer getValueCode(String name)
    {
        return this.valuesCodesAndNames.getLeft(name.toLowerCase());
    }
    
}
