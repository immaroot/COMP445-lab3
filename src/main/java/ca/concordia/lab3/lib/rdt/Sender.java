package ca.concordia.lab3.lib.rdt;

import java.io.IOException;
import java.math.BigInteger;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class Sender {

    private final ArrayBlockingQueue<Packet> window;
    private LinkedList<Packet> segments;
    private final ConcurrentHashMap<Integer, Boolean> acks;
    private final int MAX_WINDOW_SIZE = 1;
    private final int MAX_DATA_SIZE = 1024 - 11;
    private final int MAX_SEQUENCE_NUMBER = 10;
    private final Inet4Address address;
    private final Inet4Address peerAddress;
    private final int port;
    private final int peerPort;
    private int expectedSequence = 0;
    private final Timer timoutTimer;

    public Sender(Inet4Address address, Inet4Address peerAddress, int port, int peerPort) {
        this.address     = address;
        this.port        = port;
        this.peerAddress = peerAddress;
        this.peerPort    = peerPort;
        this.window      = new ArrayBlockingQueue<>(MAX_WINDOW_SIZE);
        this.segments    = new LinkedList<>();
        this.acks        = new ConcurrentHashMap<>();
        this.timoutTimer = new Timer();
    }

    public void send(byte[] message) throws InterruptedException {

        segments = getPackets(message);
        segments.forEach(packet -> acks.put(packet.getSequenceNumber().intValue(), false));

        try (DatagramSocket socket = new DatagramSocket(null)) {

            socket.bind(new InetSocketAddress(port));
            socket.connect(peerAddress, peerPort);

            boolean finished = false;

            while (!finished) {

                while (window.remainingCapacity() > 0 && !segments.isEmpty()) {
                    window.put(segments.poll());
                }

                for (Packet packet : window) {
                    if (!acks.get(packet.getSequenceNumber().intValue()))
                        sendPacket(socket, packet);
                }
                Packet incoming = receivePacket(socket);
                if (incoming.getType() == Packet.Type.ACK) {
                    ackPacket(incoming);
                    adjustWindow();
                }
                if (acks.entrySet().stream().allMatch(Map.Entry::getValue)) {
                    finished = true;
                }
            }
            timoutTimer.cancel();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void ackPacket(Packet packet) {
        acks.put(packet.getSequenceNumber().intValue(), true);
    }

    private void adjustWindow() {
        Packet packet = window.peek();
        assert packet != null;
        if (acks.get(packet.getSequenceNumber().intValue())) {
            window.poll();
        }
    }

    private LinkedList<Packet> getPackets(byte[] message) {

        LinkedList<Packet> packets = new LinkedList<>();
        ByteBuffer buffer = ByteBuffer.wrap(message);
        Packet.Builder builder = Packet.Builder.getPacketBuilder();
        long sequence = 0;

        while (buffer.hasRemaining()) {
            int segmentSize = Math.min(buffer.limit() - buffer.position(), MAX_DATA_SIZE);
            byte[] segment = new byte[segmentSize];
            buffer.get(segment);
            packets.add(builder
                    .setType(Packet.Type.DATA)
                    .setSequenceNumber(BigInteger.valueOf(sequence++))
                    .setPeerAddress(peerAddress)
                    .setPeerPort(peerPort)
                    .setData(segment)
                    .createPacket());
        }

        packets.add(builder.setType(Packet.Type.DATA)
                .setSequenceNumber(BigInteger.valueOf(sequence))
                .setPeerAddress(peerAddress)
                .setPeerPort(peerPort)
                .setData(new byte[0])
                .createPacket());

        return packets;
    }

    public void handShake() {
        Packet.Builder builder = Packet.Builder.getPacketBuilder();
        Packet packet = builder.setType(Packet.Type.SYN)
                .setSequenceNumber(BigInteger.valueOf(1))
                .setPeerAddress(address)
                .setPeerPort(port)
                .setData(new byte[0])
                .createPacket();

        try (DatagramSocket socket = new DatagramSocket()) {
            DatagramPacket outgoingPacket = new DatagramPacket(packet.getBytes(),packet.getBytes().length);
            DatagramPacket incomingPacket = new DatagramPacket(new byte[1024], 1024);
            Packet receivedPacket;

            socket.connect(address,port);
            socket.send(outgoingPacket);
            expectedSequence++;

            socket.receive(incomingPacket);
            receivedPacket = builder.parseBytes(incomingPacket.getData());


            assert receivedPacket.getType() == Packet.Type.SYN_ACK;
            assert receivedPacket.getSequenceNumber().intValue() == expectedSequence;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendPacket(DatagramSocket socket, Packet packet) throws IOException {
        DatagramPacket datagramPacket = new DatagramPacket(packet.getBytes(), packet.getBytes().length);
        socket.send(datagramPacket);
        timoutTimer.schedule(new TimoutTimer(socket, packet), 5000);
        System.out.println("Sent packet: " + packet);
    }

    private Packet receivePacket(DatagramSocket socket) throws IOException {
        byte[]  buffer = new byte[1024];
        DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
        socket.receive(datagramPacket);
        Packet packet = Packet.Builder.getPacketBuilder().parseBytes(datagramPacket.getData());
        System.out.println("Received packet: " + packet);
        return packet;
    }


    public static void main(String[] args) throws UnknownHostException, InterruptedException {
        Sender sender = new Sender((Inet4Address) Inet4Address.getLocalHost(), (Inet4Address)Inet4Address.getLocalHost(), 8080, 5000);

        String message = "Debitis et eaque eum. Suscipit eos qui occaecati asperiores qui inventore quas. Vero id dolores impedit reprehenderit. Voluptatem autem et nam doloribus explicabo. Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum. Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum. Odit non aut sapiente eligendi. Quia quia rerum. Quis dolorum perspiciatis quam. Dolore culpa maiores aliquid eius. Ea rerum dolores enim nulla omnis animi.";
        byte[] data = message.getBytes(StandardCharsets.UTF_8);

        System.out.println("Message size is : " + message.length());
        sender.send(data);
    }

    public class TimoutTimer extends TimerTask {

        DatagramSocket socket;
        Packet packet;

        public TimoutTimer(DatagramSocket socket, Packet packet) {
            this.socket = socket;
            this.packet = packet;
        }

        @Override
        public void run() {
            if (!acks.get(packet.getSequenceNumber().intValue())) {
                try {
                    System.out.println("Resending packet seq: " + packet.getSequenceNumber());
                    sendPacket(socket,packet);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
