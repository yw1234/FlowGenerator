package main;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jnetpcap.Pcap;
import org.jnetpcap.PcapIf;
import org.jnetpcap.packet.JMemoryPacket;
import org.jnetpcap.packet.JPacket;
import org.jnetpcap.protocol.JProtocol;
import org.jnetpcap.protocol.lan.Ethernet;
import org.jnetpcap.protocol.network.Ip4;

public class RawPacketSender {
    private static Logger logger = Logger.getLogger(RawPacketSender.class.getName());

    private Pcap pcap = null;
    private int headerLength = getHeaderLength();
    
    public RawPacketSender() throws UnknownHostException, SocketException {
        //Destination MAC address needs to be configured. This can be retrieved using ARP, but it's not easy
        try {
            pcap = createPcap();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to start pcap library.", e);
        }
    }
    /*
    public void sendPacket(URI destination, byte[] packet)
            throws IOException {
        int port = destination.getPort();
        InetAddress address = InetAddress.getByName(destination.getHost());
        byte[] destinationAddress = address.getAddress();
        sendPacket(destinationAddress, port, packet);
    }*/
    

    private Pcap createPcap() throws IOException {
        PcapIf device = getPcapDevice();
        if (device == null) {
        	System.out.println("Device is NULL");
            return null;
        }
        //sourceMacAddress = device.getHardwareAddress();  //Use device's MAC address as the source address
        StringBuilder errorBuffer = new StringBuilder();
        int snapLen = 64 * 1024;
        int flags = Pcap.MODE_NON_PROMISCUOUS;
        int timeout = 10 * 1000;
        Pcap pcap = Pcap.openLive(device.getName(), snapLen, flags, timeout, errorBuffer);
        if (logger.isLoggable(Level.INFO)) {
            logger.info(String.format("Pcap starts for device %s successfully.", device));
        }
        return pcap;
    }

    private PcapIf getPcapDevice() {
        List<PcapIf> allDevs = new ArrayList<PcapIf>();
        StringBuilder errorBuffer = new StringBuilder();
        int r = Pcap.findAllDevs(allDevs, errorBuffer);
        if (r == Pcap.NOT_OK || allDevs.isEmpty()) {
            logger.log(Level.SEVERE, String.format("Can't read list of devices, error is %s",
                    errorBuffer.toString()));
            return null;
        }
        for (PcapIf device : allDevs) {
            if (device.getName().matches("(.*)eth0")) {
                return device;
            }
        }
        return allDevs.get(0);
    }

    private int getHeaderLength() {
        return 14 + 20; //Ethernet header + IP v4 header
    }

    void sendPacket(String[] addTuple, byte[] data) throws IOException {
        int dataLength = data.length;
        int packetSize = headerLength + dataLength;
        JPacket packet = new JMemoryPacket(packetSize);
        packet.order(ByteOrder.BIG_ENDIAN);
        packet.setUShort(12, 0x0800);
        packet.scan(JProtocol.ETHERNET_ID);
        Ethernet ethernet = packet.getHeader(new Ethernet());
        byte[] sourceMacAddress = hexStringToByteArray(convertMAC(addTuple[2]));
        byte[] destinationMacAddress = hexStringToByteArray(convertMAC(addTuple[3]));
        ethernet.source(sourceMacAddress);
        ethernet.destination(destinationMacAddress);
        ethernet.checksum(ethernet.calculateChecksum());

        //IP v4 packet
        packet.setUByte(14, 0x40 | 0x05);
        packet.scan(JProtocol.ETHERNET_ID);
        Ip4 ip4 = packet.getHeader(new Ip4());
        ip4.type(Ip4.Ip4Type.IP);
        ip4.length(packetSize - ethernet.size());
        byte[] sourceAddress = InetAddress.getByName(addTuple[0]).getAddress();
        byte[] destinationAddress = InetAddress.getByName(addTuple[1]).getAddress();
        ip4.source(sourceAddress);
        ip4.destination(destinationAddress);
        ip4.ttl(32);
        ip4.flags(0);
        ip4.offset(0);
        ip4.checksum(ip4.calculateChecksum());

        //UDP packet
        //packet.scan(JProtocol.ETHERNET_ID);
        //Udp udp = packet.getHeader(new Udp());
        //udp.source(UDP_SOURCE_PORT);
        //udp.destination(port);
        //udp.length(packetSize - ethernet.size() - ip4.size());
        //udp.checksum(udp.calculateChecksum());
        packet.setByteArray(headerLength, data);
        packet.scan(Ethernet.ID);
        
        if (pcap.sendPacket(packet) != Pcap.OK) {
            throw new IOException(String.format(
                    "Failed to send packet with error: %s", pcap.getErr()));
        }else {
        	System.out.println("Packet Sent Successfully");
        }
    }
    
    private String convertMAC(String s){
    	String[] strs = new String[4];
    	strs = s.split(":");
    	StringBuilder sb = new StringBuilder();
    	for(String str: strs){
    		sb.append(str);
    	}
    	return sb.toString();
    }
    
    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character
                    .digit(s.charAt(i + 1), 16));
        }
        return data;
    }

}