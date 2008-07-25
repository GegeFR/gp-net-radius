/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gp.net.radius.test;

import gp.net.radius.data.RadiusMessageUtils;
import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;

/**
 *
 * @author Gwenhael Pasquiers
 */
public class RadiusMessageUtilsTest
{

    static public void main(String[] args)
    {
        System.out.println(RadiusMessageUtils.generateUniqueAuthenticator());
        long nombre = 1;
        
        byte[] auth = {(byte) 0x0f, (byte) 0x40, (byte) 0x3f, (byte) 0x94, (byte) 0x73, (byte) 0x97, (byte) 0x80, (byte) 0x57, (byte) 0xbd, (byte) 0x83, (byte) 0xd5, (byte) 0xcb, (byte) 0x98, (byte) 0xf4, (byte) 0x22, (byte) 0x7a};
        Array authenticator = new DefaultArray(auth);

        Array secret = new DefaultArray("xyzzy5461".getBytes());
        Array userPassword = new DefaultArray("arctangent".getBytes());

        {
            long timestamp = System.currentTimeMillis();
            for (long i = 0; i < nombre; i++)
            {
                try
                {
                    System.out.println(new String(userPassword.getBytes()));
                    Array encoded = RadiusMessageUtils.encodeUserPassword(authenticator, secret, userPassword);
                    encoded.getBytes();
                    System.out.println(encoded);
                    
                    Array decoded = RadiusMessageUtils.decodeUserPassword(authenticator, secret, encoded);
                    System.out.println(decoded);
                    System.out.println(new String(decoded.getBytes()));
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            long timestampEnd = System.currentTimeMillis();
            System.out.println("processUserPassword rate =" + ((1000 * nombre) / (timestampEnd - timestamp)) + " compute per seconde");
        }

    }
}
