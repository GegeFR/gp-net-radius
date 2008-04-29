/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gp.net.radius.dictionary;

import gp.utils.exception.AssociationHashMapUniquenessException;
import gp.utils.map.AssociationHashMap;

/**
 *
 * @author gege
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
