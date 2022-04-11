package ca.concordia.lab3.lib.rdt;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PacketTest {

    @Test
    void testPacketCreation() throws UnknownHostException {
        Packet.Builder builder = Packet.Builder.getPacketBuilder();

        String data = "hello world";

        Packet packet = builder.setSequenceNumber(BigInteger.valueOf(1))
                .setPeerAddress((Inet4Address) Inet4Address.getByName("1.1.1.1"))
                .setType(Packet.Type.ACK)
                .setPeerPort(101)
                .setData(data.getBytes(StandardCharsets.UTF_8))
                .createPacket();

        System.out.println(packet);
        assertEquals(1, packet.getSequenceNumber().intValue());
        assertEquals(InetAddress.getByName("1.1.1.1"), packet.getPeerAddress());
        assertEquals(Packet.Type.ACK, packet.getType());
        assertEquals(101, packet.getPeerPort());
        assertArrayEquals(data.getBytes(StandardCharsets.UTF_8), packet.getData());

        System.out.println(Arrays.toString(packet.getBytes()));

        Packet packet2 = builder.parseBytes(packet.getBytes());

        System.out.println(packet2);
    }

    @Test
    void testSorting() throws UnknownHostException {

        int port = 5000;
        Inet4Address address = (Inet4Address) Inet4Address.getLocalHost();

        Packet packet1 = Packet.Builder.getPacketBuilder()
                .setSequenceNumber(BigInteger.valueOf(1))
                .setType(Packet.Type.DATA)
                .setState(Packet.State.READY)
                .setPeerAddress(address)
                .setPeerPort(port)
                .setData(new byte[0])
                .createPacket();

        Packet packet2 = Packet.Builder.getPacketBuilder()
                .setSequenceNumber(BigInteger.valueOf(2))
                .setType(Packet.Type.DATA)
                .setState(Packet.State.READY)
                .setPeerAddress(address)
                .setPeerPort(port)
                .setData(new byte[0])
                .createPacket();

        Packet packet3 = Packet.Builder.getPacketBuilder()
                .setSequenceNumber(BigInteger.valueOf(3))
                .setType(Packet.Type.DATA)
                .setState(Packet.State.READY)
                .setPeerAddress(address)
                .setPeerPort(port)
                .setData(new byte[0])
                .createPacket();

        List<Packet> packets = new ArrayList<>();

        packets.add(packet3);
        packets.add(packet1);
        packets.add(packet2);

        System.out.println("Before sorting: ");
        packets.forEach(System.out::println);

        Collections.sort(packets);

        System.out.println("After sorting: ");
        packets.forEach(System.out::println);

        assertEquals(1, packets.get(0).getSequenceNumber().intValue());
        assertEquals(2, packets.get(1).getSequenceNumber().intValue());
        assertEquals(3, packets.get(2).getSequenceNumber().intValue());
    }
}