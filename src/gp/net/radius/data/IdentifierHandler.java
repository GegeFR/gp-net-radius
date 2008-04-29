/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gp.net.radius.data;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author gege
 */
public class IdentifierHandler
{
    private List<Integer> freeIdentifiers;
    private Set<Integer> usedIdentifiers;
    
    public IdentifierHandler()
    {
        this.freeIdentifiers = new LinkedList<Integer>();
        this.usedIdentifiers = new HashSet<Integer>();
        
        for(int i=1; i<256; i++)
        {
            this.freeIdentifiers.add(i);
        }
    }
    
    synchronized public int getIdentifier() throws InterruptedException
    {
        if(this.freeIdentifiers.size() == 0)
        {
            throw new RuntimeException("No more free identifiers");
        }
        int value = this.freeIdentifiers.remove(0);
        this.usedIdentifiers.add(value);
        return value;
    }
    
    synchronized public boolean freeIdentifier(int value)
    {
        if(this.usedIdentifiers.remove(value))
        {
            this.freeIdentifiers.add(value);
            return true;
        }
        else
        {
            return false;
        }
    }
    
}
