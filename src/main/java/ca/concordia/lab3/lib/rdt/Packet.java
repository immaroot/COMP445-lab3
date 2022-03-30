package ca.concordia.lab3.lib.rdt;

import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Optional;

public class Packet {

    enum Type {

        ACK(1),
        NAK(2),
        SYN(3),
        SYN_ACK(4);

        private final int value;

        Type(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static Optional<Type> valueOf(int value) {
            return Arrays.stream(values())
                    .filter(packetType -> packetType.getValue() == value)
                    .findFirst();
        }
    }

    private final Type type;
    private final BigInteger sequenceNumber;
    private final Inet4Address peerAddress;
    private final int peerPort;
    private final byte[] data;

    public Packet(Type type, BigInteger sequenceNumber, Inet4Address peerAddress, int peerPort, byte[] data) {

        //Should not total more than 1024 bytes.
        assert data.length + 11 <=1024;

        this.type           = type;
        this.sequenceNumber = sequenceNumber;
        this.peerAddress    = peerAddress;
        this.peerPort       = peerPort;
        this.data           = data;
    }

    public Type getType() {
        return type;
    }

    public BigInteger getSequenceNumber() {
        return sequenceNumber;
    }

    public Inet4Address getPeerAddress() {
        return peerAddress;
    }

    public int getPeerPort() {
        return peerPort;
    }

    public byte[] getData() {
        return data;
    }

    public byte[] getBytes() {

        ByteBuffer byteBuffer = ByteBuffer.allocate(data.length + 11);

        byte[] sequenceBytes = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(sequenceNumber.intValue()).array();

        byteBuffer.put((byte) type.getValue());
        byteBuffer.put(sequenceBytes);
        byteBuffer.put(peerAddress.getAddress());
        byteBuffer.put((byte) (peerPort / 256));
        byteBuffer.put((byte) (peerPort % 256));
        byteBuffer.put(data);

        return byteBuffer.array();
    }

    @Override
    public String toString() {
        return "Packet{" +
                "type=" + type +
                ", sequenceNumber=" + sequenceNumber +
                ", peerAddress=" + peerAddress +
                ", peerPort=" + peerPort +
                ", data=" + Arrays.toString(data) +
                '}';
    }

    interface Builder {

        Packet.Builder setType(Packet.Type type);

        Packet.Builder setSequenceNumber(BigInteger sequenceNumber);

        Packet.Builder setPeerAddress(Inet4Address peerAddress);

        Packet.Builder setPeerPort(int peerPort);

        Packet.Builder setData(byte[] data);

        Packet parseBytes(byte[] packet) throws UnknownHostException;

        Packet createPacket();

        static Packet.Builder getPacketBuilder() {
            return new PacketBuilderImpl();
        }
    }
}
