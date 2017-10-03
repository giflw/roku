package com.itquasar.multiverse.roku;/* Send a multicast SSDP M-SEARCH request on multiple threads to
 * find the addresses of all Rokus on the network.
 * device. For more info go to: 
 * http://sdkdocs.roku.com/display/sdkdoc/External+Control+Guide 
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

/* we extend the Thread class to that we can call multiple instances */
class Scan {

    /* these class variables are shared by all threads */
    private static final Logger LOGGER = LoggerFactory.getLogger(Scan.class);

    /**
     * Scan the local area network for a single Roku device
     *
     * @return the IP Address of the first found Roku device
     */
    public Collection<String> scanForRokus() {
        Set<String> addresses = new TreeSet<>();
        try {
            String multicastIp = "239.255.255.250";
            int port = 1900;
            /* our M-SEARCH data as a byte array */
            String MSEARCH = "M-SEARCH * HTTP/1.1\n" +
                    "Host: 239.255.255.250:1900\n" +
                    "Man: \"ssdp:discover\"\n" +
                    "ST: roku:ecp\n";
            byte[] sendData = MSEARCH.getBytes();

            /* create a packet from our data destined for 239.255.255.250:1900 */
            DatagramPacket sendPacket = new DatagramPacket(
                    sendData,
                    sendData.length,
                    InetAddress.getByName(multicastIp),
                    port
            );
            /* send packet to the socket we're creating */
            DatagramSocket clientSocket = new DatagramSocket();
            clientSocket.send(sendPacket);

            clientSocket.setSoTimeout(1 * 1000);
            int lastSize = -1;
            while (lastSize != addresses.size()) {
                addresses.add(receive(clientSocket));
                lastSize = addresses.size();
            }
        } catch (Exception ex) {
            LOGGER.error("Error scanning for Roku", ex);
        }
        return addresses;
    }

    public String receive(DatagramSocket datagramSocket) throws IOException {
        /* create byte arrays to hold our response data */
        byte[] receiveData = new byte[1024];
        /* recieve response and store in our receivePacket */
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        datagramSocket.receive(receivePacket);

        /* get the response as a string */
        String response = new String(receivePacket.getData());

        /* close the socket */
        datagramSocket.close();

        /* parse the IP from the response */
        /* the response should contain a line like:
            Location:  http://192.168.1.9:8060/
            and we're only interested in the address -- not the port.
            So we find the line, then split it at the http:// and the : to get the address.
         */
        response = response.toLowerCase();
        String address = response.split("location:")[1].split("\n")[0].split("http://")[1].split(":")[0].trim();
        /* return the address */
        return address;
    }
}
