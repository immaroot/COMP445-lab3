package ca.concordia.lab3.lib.rdt;

import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.Arrays;

public class PacketBuilderImpl implements Packet.Builder {
    private Packet.Type type;
    private Packet.State state;
    private BigInteger sequenceNumber;
    private Inet4Address peerAddress;
    private int peerPort;
    private byte[] data;

    @Override
    public PacketBuilderImpl setType(Packet.Type type) {
        this.type = type;
        return this;
    }

    @Override
    public Packet.Builder setState(Packet.State state) {
        this.state = state;
        return this;
    }

    @Override
    public PacketBuilderImpl setSequenceNumber(BigInteger sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
        return this;
    }

    @Override
    public PacketBuilderImpl setPeerAddress(Inet4Address peerAddress) {
        this.peerAddress = peerAddress;
        return this;
    }

    @Override
    public PacketBuilderImpl setPeerPort(int peerPort) {
        this.peerPort = peerPort;
        return this;
    }

    @Override
    public PacketBuilderImpl setData(byte[] data) {
        this.data = data;
        return this;
    }

    @Override
    public Packet parseBytes(byte[] packet) throws UnknownHostException {
        assert Packet.Type.valueOf(packet[0]).isPresent();
        return setType(Packet.Type.valueOf(packet[0]).get())
                .setSequenceNumber(new BigInteger(Arrays.copyOfRange(packet, 1, 5)))
                .setPeerAddress((Inet4Address) Inet4Address.getByAddress(Arrays.copyOfRange(packet,5, 9)))
                .setPeerPort((int) packet[9] * 256 + (int) packet[10])
                .setData(Arrays.copyOfRange(packet, 11, packet.length))
                .createPacket();
    }

    @Override
    public Packet createPacket() {
        return new Packet(type, state, sequenceNumber, peerAddress, peerPort, data);
    }

}