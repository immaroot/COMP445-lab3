package ca.concordia.lab3.lib;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HttpFileServerHandler implements Runnable {

    private final HttpFileServer server;
    private final SocketChannel socket;
    private final boolean verbose;

    public HttpFileServerHandler(SocketChannel socket, HttpFileServer httpFileServer, boolean verbose) {
        this.socket = socket;
        this.server = httpFileServer;
        this.verbose = verbose;
    }

    @Override
    public void run() {

        HttpRequestParser requestParser = HttpRequestParser.createHttpRequestParser();

        try {
            InputStream in = socket.socket().getInputStream();
            OutputStream out = socket.socket().getOutputStream();

            HttpRequest request = requestParser.parse(in);
            HttpResponse response;

            if (verbose) {
                System.out.println(request.getRaw());
            }

            switch (request.method()) {
                case "GET":
                    response = doGet(request);
                    break;
                case "POST":
                    response = doPost(request);
                    break;
                default:
                    response = methodNotValid(request);
                    break;
            }

            if (verbose) {
                System.out.println(response.getRaw());
            }

            response.writeTo(out);
            out.flush();
            out.close();

        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private HttpResponse doGet(HttpRequest request) throws IOException {
        Path requestedFilePath = Path.of(server.getBASE_PATH() + request.uri().getPath());
        HttpResponse.Builder builder = HttpResponse.newBuilder();

        if (request.uri().getPath().contains("./") || request.uri().getPath().contains("../")) {
            System.out.println("Some bad request!!!");
            return builder.request(request)
                    .uri(request.uri())
                    .httpVersion("HTTP/1.0")
                    .reasonPhrase("Bad Request")
                    .statusCode(400).build();

        } else if (request.uri().getPath().equals("/")) {
            List<Path> paths = listFiles();
            String document = createHtmlDocument(paths);

            return builder.request(request)
                    .uri(request.uri())
                    .httpVersion("HTTP/1.0")
                    .reasonPhrase("OK")
                    .statusCode(200)
                    .addHeader("Server", "httpfs")
                    .addHeader("Host", "localhost:8181")
                    .addHeader("Content-Type", "text/html; charset=UTF-8")
                    .addHeader("Content-Length", String.valueOf(document.length()))
                    .body(document.getBytes(StandardCharsets.UTF_8)).build();

        } else if (Files.exists(requestedFilePath) && !Files.isDirectory(requestedFilePath)) {

            File file = requestedFilePath.toFile();
            String mimeType = URLConnection.guessContentTypeFromName(file.getName());

            return builder.request(request)
                    .uri(request.uri())
                    .httpVersion("HTTP/1.0")
                    .reasonPhrase("OK")
                    .statusCode(200)
                    .addHeader("Content-Type", mimeType)
                    .addHeader("Content-Length", String.valueOf(file.length()))
                    .addHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"")
                    .body(Files.readAllBytes(file.toPath())).build();
        } else {
            return builder.request(request)
                    .uri(request.uri())
                    .httpVersion("HTTP/1.1")
                    .reasonPhrase("Not Found")
                    .statusCode(404)
                    .addHeader("Content-Type", "text/html")
                    .addHeader("Content-Length", "0")
                    .addHeader("User-Agent", "httpfs").build();
        }
    }

    private String createHtmlDocument(List<Path> paths) {
        StringBuilder sb = new StringBuilder();

        sb.append(htmlHeader);

        paths.forEach(f -> sb.append("<a href=\"http://")
                .append(server.getHOST())
                .append(":")
                .append(server.getPORT())
                .append("/")
                .append(server.getBASE_PATH().relativize(f))
                .append("\">")
                .append(server.getBASE_PATH().relativize(f))
                .append("</a> <br>\n"));

        sb.append(htmlFooter);

        return sb.toString();
    }

    private HttpResponse doPost(HttpRequest request) throws IOException {
        Path requestedPath = Path.of(server.getBASE_PATH() + request.uri().getPath());
        File file = new File(String.valueOf(requestedPath));

        if (request.headers().getValue("Content-Type").startsWith("text")) {
            try (FileWriter fileWriter = new FileWriter(file)) {
                fileWriter.write(new String(request.body()));
                fileWriter.flush();
            }
        } else {
            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                outputStream.write(request.body());
                outputStream.flush();
            }
        }

        return HttpResponse.newBuilder()
                .request(request)
                .httpVersion("HTTP/1.0")
                .statusCode(200)
                .reasonPhrase("OK").build();
    }

    private HttpResponse methodNotValid(HttpRequest request) {
        return HttpResponse.newBuilder()
                .request(request)
                .httpVersion("HTTP/1.0")
                .statusCode(400)
                .reasonPhrase("Bad Request").build();
    }

    private List<Path> listFiles() {

        List<Path> result = null;
        try (Stream<Path> walk = Files.walk(server.getBASE_PATH())) {
            result = walk.filter(Files::isRegularFile).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private final static String htmlHeader =
            "<html>\n" +
            "<body>\n";

    private final static String htmlFooter =
            "</body>\n" +
            "</html>\n";
}
