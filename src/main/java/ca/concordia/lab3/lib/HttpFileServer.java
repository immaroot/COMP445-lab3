package ca.concordia.lab3.lib;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.file.Path;

public class HttpFileServer implements Runnable {

    private final int       PORT;
    private final String    HOST;
    private final Path      BASE_PATH;
    private final boolean   verbose;

    public HttpFileServer(int PORT, String HOST, Path BASE_PATH, boolean verbose) {
        this.PORT      = PORT;
        this.HOST      = HOST;
        this.BASE_PATH = BASE_PATH;
        this.verbose   = verbose;
    }

    @Override
    public void run() {

        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {

            serverSocketChannel.socket().bind(new InetSocketAddress(PORT));

            System.out.println("Listening on port: " + PORT);
            System.out.println("Address is: http://" + HOST + ":" + PORT);
            System.out.println("Serving files at: " + BASE_PATH.toAbsolutePath());

            while (true) {
                HttpFileServerHandler handler =
                        new HttpFileServerHandler(serverSocketChannel.accept(), this, verbose);
                Thread thread = new Thread(handler);
                thread.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getPORT() {
        return PORT;
    }

    public String getHOST() {
        return HOST;
    }

    public Path getBASE_PATH() {
        return BASE_PATH;
    }
}
