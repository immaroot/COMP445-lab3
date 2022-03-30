package ca.concordia.lab3.lib;

import java.net.URI;
import java.util.Objects;

public class HttpRequestBuilderImpl implements HttpRequest.Builder {

    private URI uri;
    private HttpHeaders headers = HttpHeaders.createHttpHeaders();
    private String method;
    private byte[] body;

    public HttpRequestBuilderImpl() {
        this.method = "GET"; //default
    }

    public HttpRequestBuilderImpl(URI uri) {
        Objects.requireNonNull(uri);
        this.uri = uri;
        this.method = "GET"; //default
    }

    @Override
    public HttpRequest.Builder uri(URI uri) {
        Objects.requireNonNull(uri);
        this.uri = uri;
        return this;
    }

    @Override
    public HttpRequest.Builder addHeader(String name, String value) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(value);
        headers.addHeader(name, value);
        return this;
    }

    @Override
    public HttpRequest.Builder addHeaders(HttpHeaders headers) {
        Objects.requireNonNull(headers);
        this.headers = headers;
        return this;
    }

    @Override
    public HttpRequest.Builder GET() {
        this.method = "GET";
        return this;
    }

    @Override
    public HttpRequest.Builder POST(byte[] body) {
        this.method = "POST";
        this.body = body;
        return this;
    }

    @Override
    public HttpRequest build() {
        if (uri == null) {
            throw new IllegalStateException("uri is null");
        }
        assert method != null;
        return new HttpRequestImpl(uri, headers, method, body);
    }
}
