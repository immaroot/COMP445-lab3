package ca.concordia.lab3.lib.rdt;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

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
}