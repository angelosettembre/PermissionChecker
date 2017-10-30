/* Copyright (C) 2016 Julian Andres Klode <jak@jak-linux.org>
 *
 * Derived from AdBuster:
 * Copyright (C) 2016 Daniel Brodie <dbrodie@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * Contributions shall also be provided under any later versions of the
 * GPL.
 */
package com.example.angiopasqui.permissionchecker.privacyLeaks;

import android.content.Context;
import android.util.Log;

import com.example.angiopasqui.permissionchecker.privacyLeaks.db.RuleDatabase;

import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.Pcaps;
import org.pcap4j.packet.*;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Flags;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Rcode;
import org.xbill.DNS.SOARecord;
import org.xbill.DNS.Section;
import org.xbill.DNS.TextParseException;

import java.io.EOFException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeoutException;

/**
 * Creates and parses packets, and sends packets to a remote socket or the device using

 */
public class DnsPacketProxy {
    private final EventLoop eventLoop;
    final RuleDatabase ruleDatabase;
    private static String TAG = "DnsPacketProxy";
    ArrayList<InetAddress> upstreamDnsServers = new ArrayList<>();
    private static final int NEGATIVE_CACHE_TTL_SECONDS = 5;
    private static final SOARecord NEGATIVE_CACHE_SOA_RECORD;

    static {
        try {
            // Let's use a guaranteed invalid hostname here, clients are not supposed to use
            // our fake values, the whole thing just exists for negative caching.
            Name name = new Name("dns66.dns66.invalid.");
            NEGATIVE_CACHE_SOA_RECORD = new SOARecord(name, DClass.IN, NEGATIVE_CACHE_TTL_SECONDS,
                    name, name, 0, 0, 0, 0, NEGATIVE_CACHE_TTL_SECONDS);
        } catch (TextParseException e) {
            throw new RuntimeException(e);
        }
    }

    public DnsPacketProxy(EventLoop eventLoop) {
        this.eventLoop = eventLoop;
        this.ruleDatabase = RuleDatabase.getInstance();
    }

    void initialize(Context context, ArrayList<InetAddress> upstreamDnsServers) throws InterruptedException {  //9
        Log.i("DEBUG", "metodo initialize DnsPacketProxy");

        ruleDatabase.initialize(context);
        this.upstreamDnsServers = upstreamDnsServers;
    }

    /**
     * Handles a responsePayload from an upstream DNS server
     *
     * @param requestPacket   The original request packet
     * @param responsePayload The payload of the response
     */
    void handleDnsResponse(IpPacket requestPacket, byte[] responsePayload) {
        UdpPacket udpOutPacket = (UdpPacket) requestPacket.getPayload();
        UdpPacket.Builder payLoadBuilder = new UdpPacket.Builder(udpOutPacket)
                .srcPort(udpOutPacket.getHeader().getDstPort())
                .dstPort(udpOutPacket.getHeader().getSrcPort())
                .srcAddr(requestPacket.getHeader().getDstAddr())
                .dstAddr(requestPacket.getHeader().getSrcAddr())
                .correctChecksumAtBuild(true)
                .correctLengthAtBuild(true)
                .payloadBuilder(
                        new UnknownPacket.Builder()
                                .rawData(responsePayload)
                );


        IpPacket ipOutPacket;
        if (requestPacket instanceof IpV4Packet) {
            ipOutPacket = new IpV4Packet.Builder((IpV4Packet) requestPacket)
                    .srcAddr((Inet4Address) requestPacket.getHeader().getDstAddr())
                    .dstAddr((Inet4Address) requestPacket.getHeader().getSrcAddr())
                    .correctChecksumAtBuild(true)
                    .correctLengthAtBuild(true)
                    .payloadBuilder(payLoadBuilder)
                    .build();

        } else {
            ipOutPacket = new IpV6Packet.Builder((IpV6Packet) requestPacket)
                    .srcAddr((Inet6Address) requestPacket.getHeader().getDstAddr())
                    .dstAddr((Inet6Address) requestPacket.getHeader().getSrcAddr())
                    .correctLengthAtBuild(true)
                    .payloadBuilder(payLoadBuilder)
                    .build();
        }

        eventLoop.queueDeviceWrite(ipOutPacket);
    }


    /**
     * Handles a DNS request, by either blocking it or forwarding it to the remote location.
     * Lettura del pacchetto
     * @param packetData The packet data to read
     * @throws VpnNetworkException If some network error occurred
     */
    //25
    String handleDnsRequest(byte[] packetData) throws VpnNetworkException, PcapNativeException, EOFException, TimeoutException, NotOpenException {
        Log.d("DEBUG","metodo handleDnsRequest DnsPacketProxy");

        IpPacket parsedPacket = null;
        try {

            parsedPacket = (IpPacket) IpSelector.newPacket(packetData, 0, packetData.length);  //Chiamata metodo per creare un nuovo pacchetto della libreria pcap4j

        } catch (Exception e) {
            Log.i(TAG, "handleDnsRequest: Discarding invalid IP packet", e);
            return "";
        }

        if (!(parsedPacket.getPayload() instanceof UdpPacket)) {
            Log.i(TAG, "handleDnsRequest: Discarding unknown packet type " + parsedPacket.getPayload());
            return "";
        }

        InetAddress destAddr = translateDestinationAdress(parsedPacket);    //Chiamata metodo per tradurre l'indirizzo di destinazione del pacchetto

        //PcapNetworkInterface nif = Pcaps.getDevByAddress(destAddr);
        //PcapHandle handle = nif.openLive(65536, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, 10);
        //org.pcap4j.packet.Packet packet = handle.getNextPacketEx();
        /*try{

            TCPPacket tpacket = new TCPPacket(0,packetData);
            System.out.println("PACCHETTO TCP "+tpacket.toString());


        }catch (Exception e ){
            e.printStackTrace();
        }*/


        try {
            TcpPacket tcp = parsedPacket.get(TcpPacket.class);
            System.out.println("PACCHETTO tcp " + tcp.toString());
        }catch (Exception e){
            Log.d("DEBUG","ECCEZIONEEEEEEEEEE NON E' UN PACCHETTO TCP");
        }

        UdpPacket udp = parsedPacket.get(UdpPacket.class);

        System.out.println("PACCHETTO UDP "+udp.toString());


        if (destAddr == null)
            return "";

        UdpPacket parsedUdp = (UdpPacket) parsedPacket.getPayload();


        if (parsedUdp.getPayload() == null) {
            Log.i(TAG, "handleDnsRequest: Sending UDP packet without payload: " + parsedUdp);

            // Let's be nice to Firefox. Firefox uses an empty UDP packet to
            // the gateway to reduce the RTT. For further details, please see
            // https://bugzilla.mozilla.org/show_bug.cgi?id=888268
            DatagramPacket outPacket = new DatagramPacket(new byte[0], 0, 0 /* length */, destAddr, parsedUdp.getHeader().getDstPort().valueAsInt());
            eventLoop.forwardPacket(outPacket, null);
            return "";
        }

        byte[] dnsRawData = (parsedUdp).getPayload().getRawData();
        Message dnsMsg;
        try {
            dnsMsg = new Message(dnsRawData);
        } catch (IOException e) {
            Log.i(TAG, "handleDnsRequest: Discarding non-DNS or invalid packet", e);
            return "";
        }
        if (dnsMsg.getQuestion() == null) {
            Log.i(TAG, "handleDnsRequest: Discarding DNS packet with no query " + dnsMsg);
            return "";
        }

        String dnsQueryName = dnsMsg.getQuestion().getName().toString(true);
        Log.d("DEBUG","dnsQueryName "+dnsQueryName);
        if (!ruleDatabase.isBlocked(dnsQueryName.toLowerCase(Locale.ENGLISH))) {        //Si controlla l'host è bloccato
            Log.i(TAG, "handleDnsRequest: DNS Name " + dnsQueryName + " Allowed, sending to " + destAddr);
            DatagramPacket outPacket = new DatagramPacket(dnsRawData, 0, dnsRawData.length, destAddr, parsedUdp.getHeader().getDstPort().valueAsInt());
            eventLoop.forwardPacket(outPacket, parsedPacket);
        } else {
            Log.i(TAG, "handleDnsRequest: DNS Name " + dnsQueryName + " Blocked!");
            dnsMsg.getHeader().setFlag(Flags.QR);
            dnsMsg.getHeader().setRcode(Rcode.NOERROR);
            dnsMsg.addRecord(NEGATIVE_CACHE_SOA_RECORD, Section.AUTHORITY);
            handleDnsResponse(parsedPacket, dnsMsg.toWire());
        }
        return dnsQueryName;
    }

    /**
     * Traduzione dell'indirizzo di destinazione del pacchetto in un uno reale
     * Translates the destination address in the packet to the real one. In
     * case address translation is not used, this just returns the original one.
     *
     * @param parsedPacket Packet to get destination address for.
     * @return The translated address or null on failure.
     */
    private InetAddress translateDestinationAdress(IpPacket parsedPacket) {

        Log.d("DEBUG","metodo translateDestinationAddress");

        InetAddress destAddr = null;
        if (upstreamDnsServers.size() > 0) {
            byte[] addr = parsedPacket.getHeader().getDstAddr().getAddress();
            int index = addr[addr.length - 1] - 2;

            try {
                destAddr = upstreamDnsServers.get(index);
            } catch (Exception e) {
                Log.e(TAG, "handleDnsRequest: Cannot handle packets to" + parsedPacket.getHeader().getDstAddr().getHostAddress(), e);
                return null;
            }
            Log.d(TAG, String.format("handleDnsRequest: Incoming packet to %s AKA %d AKA %s", parsedPacket.getHeader().getDstAddr().getHostAddress(), index, destAddr));

        } else {
            destAddr = parsedPacket.getHeader().getDstAddr();
            Log.d(TAG, String.format("handleDnsRequest: Incoming packet to %s - is upstream", parsedPacket.getHeader().getDstAddr().getHostAddress()));
        }
        return destAddr;
    }


    interface EventLoop {

        void forwardPacket(DatagramPacket packet, IpPacket requestPacket) throws VpnNetworkException;

        /**
         * Write an IP packet to the local TUN device
         *
         * @param packet The packet to write (a response to a DNS request)
         */
        void queueDeviceWrite(IpPacket packet);
    }
}
