/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gp.net.radius.test;

import gp.net.radius.data.BytesAVP;
import gp.net.radius.data.RadiusMessage;
import gp.net.radius.data.VendorData;
import gp.utils.array.impl.DefaultArray;

/**
 *
 * @author gege
 */
public class RadiusMessageTest
{

    static public void main(String... args)
    {
        long timestamp = System.currentTimeMillis();
        long nombre = 1;
        long dataVolume = 0;
        for (long i = 0; i < nombre; i++)
        {
            RadiusMessage message = new RadiusMessage();
            message.setCode(1);
            message.setIdentifier(22);

            BytesAVP avp;
            VendorData vendorData;

            avp = new BytesAVP();
            avp.setType(1);
            avp.setData(new DefaultArray("1234".getBytes()));
            message.addAVP(avp);

            avp = new BytesAVP();
            avp.setType(2);
            avp.setData(new DefaultArray("5678".getBytes()));
            message.addAVP(avp);

            avp = new BytesAVP();
            avp.setType(1);
            avp.setData(new DefaultArray("1234".getBytes()));
            message.addAVP(avp);
            avp = new BytesAVP();
            avp.setType(1);
            avp.setData(new DefaultArray("1234".getBytes()));
            message.addAVP(avp);
            avp = new BytesAVP();
            avp.setType(1);
            avp.setData(new DefaultArray("1234".getBytes()));
            message.addAVP(avp);
            avp = new BytesAVP();
            avp.setType(1);
            avp.setData(new DefaultArray("1234".getBytes()));
            message.addAVP(avp);
            avp = new BytesAVP();
            avp.setType(1);
            avp.setData(new DefaultArray("1234".getBytes()));
            message.addAVP(avp);

            
            message.setSecret(new DefaultArray("secret".getBytes()));
            message.computeRequestAuthenticator();
            if(message.getCode() == 1) message.encodeUserPasswordAvps();
            dataVolume += message.getArray().getBytes().length;
            //System.out.println(Arrays.toString(message.getArray().getBytes()));

            //Message rcvMessage = new Message("toto", new DefaultArray(message.getArray().getBytes()));
        //System.out.println(Arrays.toString(rcvMessage.getArray().getBytes()));
        }

        long timestampEnd = System.currentTimeMillis();


        System.out.println("rate =" + ((1000 * nombre) / (timestampEnd - timestamp)) + " encode per seconde");
        System.out.println("rate =" + (1000 * dataVolume / (timestampEnd - timestamp)) + " bits per seconde");
        System.out.println("rate =" + ((1000 * dataVolume / (timestampEnd - timestamp))/1024.0) + " kbits per seconde");
        System.out.println("rate =" + ((1000 * dataVolume / (timestampEnd - timestamp))/(1024.0*1024.0)) + " mbits per seconde");
    }
}
