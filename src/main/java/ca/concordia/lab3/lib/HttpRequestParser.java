package ca.concordia.lab3.lib;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Scanner;

public class HttpRequestParser {

    private final HttpRequest.Builder builder;

    public HttpRequestParser() {
        this.builder = HttpRequest.newBuilder();
    }

    public static HttpRequestParser createHttpRequestParser() {
        return new HttpRequestParser();
    }

    public  HttpRequest parse(InputStream in) throws IOException, URISyntaxException {
        HttpRequestLine requestLine = parseRequestLine(in);
        HttpHeaders headers = parseHeaders(in);
        byte[] body;

        if (requestLine.method.equalsIgnoreCase("POST")) {
            if (!headers.hasValue("Content-Length")) {
                throw new IllegalStateException("Expected a body with a POST request.");
            }
            int contentLength = Integer.parseInt(headers.getValue("Content-Length"));
            body = new byte[contentLength];

            if (in.read(body) <= 0) {
                throw new IllegalStateException("Something Wong");
            }
            return builder
                    .POST(body)
                    .uri(requestLine.getUri())
                    .addHeaders(headers)
                    .build();
        } else if (requestLine.method.equalsIgnoreCase("GET")) {
            return builder
                    .GET()
                    .uri(requestLine.getUri())
                    .addHeaders(headers)
                    .build();
        } else {
            throw new IllegalStateException("Method not supported");
        }
    }


    HttpHeaders parseHeaders(InputStream in) throws IOException {
        HttpHeaders headers = HttpHeaders.createHttpHeaders();
        while (true) {
            String line = readLine(in);
            if (line.isEmpty()) break;
            String name = line.split(":")[0].strip();
            String value = line.split(":", 2)[1].strip();
            headers.addHeader(name, value);
        }
        return headers;
    }

    HttpRequestLine parseRequestLine(InputStream in) throws IOException, URISyntaxException {
        String line = readLine(in);
        Scanner scanner = new Scanner(line).useDelimiter("\\h+");
        String method = parseMethod(scanner);
        String uri = parseUri(scanner);
        String httpVersion = parseHttpVersion(scanner);

        HttpRequestLineBuilder builder = HttpRequestLineBuilder
                .createHttpRequestLineBuilder();
        builder.setMethod(method)
                .setUri(new URI(uri))
                .setHttpVersion(httpVersion);
        return builder.createHttpRequestLine();
    }

    String parseMethod(Scanner lineScanner) {
        assert lineScanner.hasNext("(GET|POST|HEAD)") : "Was expecting regex \"(GET|POST|HEAD)\"";
        return lineScanner.next("(GET|POST|HEAD)");
    }

    String parseUri(Scanner lineScanner) {
        String uri = lineScanner.next();
        assert isValidURI(uri) : "The URI was not valid.";
        return uri;
    }

    private boolean isValidURI(String uri) {
        try {
            URI.create(uri);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    String parseHttpVersion(Scanner lineScanner) {
        assert lineScanner.hasNext("HTTP/\\d*.\\d*") : "Was expecting regex \"HTTP/\\d*.\\d*\"";
        return lineScanner.next("HTTP/\\d*.\\d*");
    }

    String readLine(InputStream in) throws IOException {
        int b;
        StringBuilder sb = new StringBuilder();
        while ((b = in.read()) >= 0) {
            if (b == '\r') {
                int next = in.read();
                if (next == '\n') {
                    break;
                }
            }
            sb.append((char) b);
        }
        return sb.toString();
    }
}