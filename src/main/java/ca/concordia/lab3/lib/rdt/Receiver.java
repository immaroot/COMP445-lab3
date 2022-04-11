package ca.concordia.lab3.lib.rdt;

import java.io.IOException;
import java.math.BigInteger;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

public class Receiver {

    private ArrayBlockingQueue<Packet> window;
    private final ArrayList<Packet> segments;
    private final int MAX_WINDOW_SIZE = 3;
    private final int MAX_SEQUENCE_NUMBER = 10;
    private final Inet4Address address;
    private final Inet4Address peerAddress;
    private final int port;
    private final int peerPort;
    private int expectedSequence = 0;

    public Receiver(Inet4Address address, Inet4Address peerAddress, int port, int peerPort) {
        this.address     = address;
        this.peerAddress = peerAddress;
        this.port        = port;
        this.peerPort = peerPort;
        this.segments = new ArrayList<>();
    }

    public byte[] receive() {
        int size = 0;
        boolean finished = false;
        try (DatagramSocket socket = new DatagramSocket(null)) {
            socket.bind(new InetSocketAddress(port));
            Packet packet;
            do {
                packet = receivePacket(socket);
                if (segments.contains(packet)) {
                    sendAck(packet.getSequenceNumber().intValue(), peerAddress, peerPort, socket);
                    continue;
                }
                if (packet.getType() == Packet.Type.DATA && packet.getData().length == 0) {
                    finished = true;
                }

                segments.add(packet);
                size += packet.getData().length;
                sendAck(packet.getSequenceNumber().intValue(), peerAddress, peerPort, socket);
            } while (!finished);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return getBytes(segments, size);
    }

    private Packet receivePacket(DatagramSocket socket) throws IOException {
        Packet packet;
        byte[]  buffer = new byte[1024];
        DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
        socket.receive(incoming);
        byte[] bytes = Arrays.copyOfRange(buffer,0 , incoming.getLength());
        packet = Packet.Builder.getPacketBuilder().parseBytes(bytes);
        System.out.println("Received packet: " + packet);
        return packet;
    }

    private void sendAck(int intValue, Inet4Address address, int port, DatagramSocket socket) throws IOException {
        Packet.Builder builder = Packet.Builder.getPacketBuilder();
        Packet ack = builder
                .setType(Packet.Type.ACK)
                .setState(Packet.State.SENT)
                .setSequenceNumber(BigInteger.valueOf(intValue))
                .setPeerAddress(address)
                .setPeerPort(port)
                .setData(new byte[0])
                .createPacket();
        sendPacket(socket,ack);
    }

    public void sendPacket(DatagramSocket socket, Packet packet) throws IOException {
        DatagramPacket datagramPacket = new DatagramPacket(packet.getBytes(), packet.getBytes().length);
        socket.connect(peerAddress,peerPort);
        socket.send(datagramPacket);
        socket.disconnect();
        System.out.println("Sent packet: " + packet);
    }

    private byte[] getBytes(List<Packet> segments, int size) {
        ByteBuffer bytes = ByteBuffer.allocate(size);
        for (Packet packet : segments) {
            bytes.put(packet.getData());
        }
        return bytes.array();
    }

    public static void main(String[] args) throws UnknownHostException {
        Receiver receiver = new Receiver( (Inet4Address)Inet4Address.getLocalHost(),(Inet4Address)Inet4Address.getLocalHost(),5000, 8080);

        byte[] data = receiver.receive();

        System.out.println("Message size is : " + data.length);

        System.out.println(new String(data));
    }
}
