/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gp.net.radius.dictionary;

import gp.utils.exception.AssociationHashMapUniquenessException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

/**
 *
 * @author gege
 */
public class RadiusDictionary
{

    private RadiusVendors radiusVendors;
    private final String defaultVendorName = "Base";
    private final Integer defaultVendorCode = -1;

    public RadiusDictionary(File file) throws FileNotFoundException, IOException, AssociationHashMapUniquenessException
    {
        this.radiusVendors = new RadiusVendors();
        this.radiusVendors.addVendor(this.defaultVendorName, this.defaultVendorCode);

        this.parseFile(file);
    }

    public RadiusVendors getRadiusVendors()
    {
        return this.radiusVendors;
    }

    private void parseFile(File file) throws FileNotFoundException, IOException, AssociationHashMapUniquenessException
    {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

        String line;

        String currentVendorName = this.defaultVendorName;
        Integer currentVendorCode = this.defaultVendorCode;

        LinkedList<File> includes = new LinkedList<File>();

        while (null != (line = bufferedReader.readLine()))
        {
            if ((line.length() == 0) || line.startsWith("#"))
            {
                continue;
            }

            line = this.cleanLine(line);

            if (line.toUpperCase().startsWith("$INCLUDE"))
            {
                includes.addLast(new File(file.getParent() + File.separator + line.split(" ")[1]));
            }
            else if (line.toUpperCase().startsWith("END-VENDOR"))
            {
                currentVendorName = this.defaultVendorName;
                currentVendorCode = this.defaultVendorCode;
            }
            else if (line.toUpperCase().startsWith("VENDOR"))
            {
                String[] splitted = line.split(" ");
                currentVendorName = splitted[1];
                currentVendorCode = Integer.valueOf(splitted[2]);
                this.radiusVendors.addVendor(currentVendorName, currentVendorCode);

            }
            else if (line.toUpperCase().startsWith("ATTRIBUTE"))
            {
                String[] splitted = line.split(" ");
                this.radiusVendors.getRadiusAttributes(currentVendorCode).addAttribute(splitted[1], Integer.valueOf(splitted[2]), splitted[3]);
            }
            else if (line.toUpperCase().startsWith("VALUE"))
            {
                String[] splitted = line.split(" ");
                Integer currentAttributeCode = this.radiusVendors.getRadiusAttributes(currentVendorCode).getAttributeCode(splitted[1]);

                Integer vendorCodeToUse;

                if (null == currentAttributeCode)
                {
                    vendorCodeToUse = new Integer(-1);
                    currentAttributeCode = this.radiusVendors.getRadiusAttributes(vendorCodeToUse).getAttributeCode(splitted[1]);
                }
                else
                {
                    vendorCodeToUse = currentVendorCode;
                }

                Integer currentValueValue;

                if (splitted[3].startsWith("0x"))
                {
                    currentValueValue = Integer.valueOf(splitted[3].substring(2), 16);
                }
                else
                {
                    currentValueValue = Integer.valueOf(splitted[3]);
                }

                this.radiusVendors.getRadiusAttributes(vendorCodeToUse).getAttributeRadiusValues(currentAttributeCode).addValue(splitted[2], currentValueValue);
            }
        }

        bufferedReader.close();

        for (File include : includes)
        {
            this.parseFile(include);
        }
    }

    static public void main(String... args)
    {
        try
        {
            RadiusDictionary dic = new RadiusDictionary(new File("./res/radius/dictionary"));
            Integer vcode = dic.getRadiusVendors().getVendorCode("Ascend");
            Integer acode = dic.getRadiusVendors().getRadiusAttributes(vcode).getAttributeCode("Ascend-Service-Type");
            Integer value = dic.getRadiusVendors().getRadiusAttributes(vcode).getAttributeRadiusValues(acode).getValueCode("Ascend-Service-Type-NetToNet");
            System.out.println(value);
        }
        catch (Exception ex)
        {
            ex.printStackTrace(System.out);
        }
    }

    private String cleanLine(String line)
    {
        line = line.trim().replace("\t", " ");

        String newLine = line.replace("  ", " ");

        while (line.length() != newLine.length())
        {
            line = newLine;
            newLine = line.replace("  ", " ");
        }

        return line;
    }
}
