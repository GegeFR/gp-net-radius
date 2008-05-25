/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gp.net.radius.test;

import gp.net.radius.RadiusSocket;
import gp.net.radius.data.BytesAVP;
import gp.net.radius.data.RadiusMessage;
import gp.utils.array.impl.DefaultArray;
import java.net.InetSocketAddress;

/**
 *
 * @author gege
 */
public class RadiusSocketTest
{

    static public void main(String... args)
    {
        try
        {
            RadiusSocket client = new RadiusSocket();

            RadiusSocket server = new RadiusSocket(12345);

            long nombre = 100000;

            long timestamp = System.currentTimeMillis();
            for (long i = 0; i < nombre; i++)
            {

                RadiusMessage requestSent = new RadiusMessage();
                requestSent.setCode(1);
                requestSent.setIdentifier(22);

                BytesAVP avp;
                //VendorData vendorData;

                avp = new BytesAVP();
                avp.setType(2);
                avp.setData(new DefaultArray("0123456789012345".getBytes()));
                requestSent.addAVP(avp);
                requestSent.setSecret(new DefaultArray("totosecret".getBytes()));
                requestSent.computeRequestAuthenticator();


                //System.out.println("\nunencrypted");
                //System.out.println(requestSent.getArray());

                requestSent.encodeUserPasswordAvps();

                requestSent.setRemoteAddress(new InetSocketAddress("127.0.0.1", 12345));

                //System.out.println("\nencrypted");
                //System.out.println(requestSent.getArray());

                client.send(requestSent);

                //System.out.println("\nsent");
                //System.out.println(requestSent.getArray());

                RadiusMessage requestReceived = server.receive();

//            System.out.println("\nreceived");
//            System.out.println(requestReceived.getArray());

                requestReceived.setSecret(new DefaultArray("totosecret".getBytes()));

                requestReceived.decodeUserPasswordAvps();

                //System.out.println("requestReceived.hasValidRequestAuthenticator() ... " + requestReceived.hasValidRequestAuthenticator());

                RadiusMessage responseSent = new RadiusMessage();
                responseSent.setCode(2);
                avp = new BytesAVP();
                avp.setType(3);
                avp.setData(new DefaultArray("0123456789012345".getBytes()));
                responseSent.addAVP(avp);
                responseSent.setSecret(new DefaultArray("totosecret".getBytes()));
                responseSent.computeResponseAuthenticator(requestReceived.getAuthenticator());
                responseSent.setRemoteAddress(requestReceived.getRemoteAddress());


                server.send(responseSent);

                RadiusMessage responseReceived = client.receive();

                responseReceived.setSecret(new DefaultArray("totosecret".getBytes()));
                responseReceived.hasValidResponseAuthenticator(requestSent.getAuthenticator());
//System.out.println("responseReceived.hasValidResponseAuthenticator() ... " + responseReceived.hasValidResponseAuthenticator(requestSent.getAuthenticator()));
//
//            
//            System.out.println("\ndecrypted");
//            System.out.println(requestReceived.getArray());

            }

            long timestampEnd = System.currentTimeMillis();


            System.out.println("rate =" + ((1000 * nombre) / (timestampEnd - timestamp)) + " encode per seconde");
        }
        catch (Exception e)
        {
            e.printStackTrace(System.out);
        }
    }
}
