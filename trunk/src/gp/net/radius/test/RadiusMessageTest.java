/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gp.net.radius.test;

import gp.net.radius.data.RadiusMessage;
import gp.net.radius.data.StringAVP;
import gp.utils.array.impl.DefaultArray;
import java.util.Arrays;

/**
 *
 * @author gege
 */
public class RadiusMessageTest
{

    static public void main(String... args)
    {
        try
        {
            long timestamp = System.currentTimeMillis();
            long nombre = 1;
            long dataVolume = 0;
            for (long i = 0; i < nombre; i++)
            {
                RadiusMessage message = new RadiusMessage();
                message.setCode(1);
                message.setIdentifier(22);

                message.addAVP(new StringAVP(1, "1234", "UTF-8"));
                message.addAVP(new StringAVP(2, "5678", "UTF-8"));
                message.addAVP(new StringAVP(1, "1234", "UTF-8"));
                message.addAVP(new StringAVP(1, "1234", "UTF-8"));
                message.addAVP(new StringAVP(1, "1234", "UTF-8"));
                message.addAVP(new StringAVP(1, "1234", "UTF-8"));
                message.addAVP(new StringAVP(1, "1234", "UTF-8"));
                message.addAVP(new StringAVP(1, "1234", "UTF-8"));

                message.setSecret(new DefaultArray("secret".getBytes()));
                message.computeRequestAuthenticator();
                if (message.getCode() == 1)
                {
                    message.encodeUserPasswordAvps();
                }
                dataVolume += message.getArray().getBytes().length;
                System.out.println(message.getArray());

            }

            long timestampEnd = System.currentTimeMillis();


            System.out.println("rate =" + ((1000 * nombre) / (timestampEnd - timestamp)) + " encode per second");
            System.out.println("rate =" + (1000 * dataVolume / (timestampEnd - timestamp)) + " bytes per second");
            System.out.println("rate =" + ((1000 * dataVolume / (timestampEnd - timestamp)) / 1024.0) + " kB per second");
            System.out.println("rate =" + ((1000 * dataVolume / (timestampEnd - timestamp)) / (1024.0 * 1024.0)) + " mB per second");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
