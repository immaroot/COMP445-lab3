package ca.concordia.lab3.lib;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.nio.channels.SocketChannel;

public class HttpClient {

    private HttpClient() {
    }

    public HttpResponse send(HttpRequest request) {

        SocketAddress endpoint;
        HttpResponse response = null;

        //Do some validation
        if (request.uri().getHost() == null) {
            throw new IllegalArgumentException("The URL was not entered.");
        }

        //Get correct port
        int port = request.uri().getPort();
        if (port == -1) {
            switch (request.uri().getScheme()) {
                case "http" : port = 80; break;
                case "https" : port = 443; break;
                default: throw new IllegalArgumentException("Cannot determine the port number");
            }
        }

        endpoint = new InetSocketAddress(request.uri().getHost(), port);

        try (SocketChannel socketChannel = SocketChannel.open()) {

            socketChannel.bind(socketChannel.getLocalAddress());
            socketChannel.connect(endpoint);

            request.writeTo(socketChannel.socket().getOutputStream());

            response = getHttpResponse(request, socketChannel);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return response;
    }

    private HttpResponse getHttpResponse(HttpRequest request, SocketChannel socketChannel) throws IOException {

        HttpResponseParser responseParser = HttpResponseParser.createHttpResponseParser();
        responseParser.parse(socketChannel.socket().getInputStream());

        HttpResponse.Builder builder = HttpResponse.newBuilder();

        builder.request(request)
                .uri(request.uri())
                .httpVersion(responseParser.getHttpVersion())
                .statusCode(responseParser.getStatusCode())
                .reasonPhrase(responseParser.getReasonPhrase())
                .addHeaders(responseParser.getHeaders())
                .body(responseParser.getBody());
        return  builder.build();
    }

    public static HttpClient newClient() {
        return new HttpClient();
    }

    public HttpRequest getRedirectRequest(HttpResponse response) {
        assert response.redirect();
        assert !response.headers().getValue("Location").isEmpty();
        URI location = URI.create(response.headers().getValue("Location"));
        return HttpRequest.newBuilder().GET().uri(location).build();
    }

    public static void main(String[] args) {
        HttpClient client = new HttpClient();
    }
}
