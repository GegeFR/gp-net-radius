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
