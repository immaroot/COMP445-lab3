package ca.concordia.lab3.lib;

import java.net.URI;
import java.util.Objects;

public class HttpResponseBuilderImpl implements HttpResponse.Builder {

    private String httpVersion;
    private HttpRequest request;
    private URI uri;
    private int statusCode;
    private boolean redirect;
    private String reasonPhrase;
    private HttpHeaders headers = HttpHeaders.createHttpHeaders();
    private byte[] body;


    @Override
    public HttpResponse.Builder httpVersion(String httpVersion) {
        this.httpVersion = httpVersion;
        return this;
    }

    @Override
    public HttpResponse.Builder request(HttpRequest request) {
        this.request = request;
        return this;
    }

    @Override
    public HttpResponse.Builder uri(URI uri) {
        Objects.requireNonNull(uri);
        this.uri = uri;
        return this;
    }

    @Override
    public HttpResponse.Builder statusCode(int statusCode) {
        if (statusCode >= 300 && statusCode < 400) redirect = true;
        this.statusCode = statusCode;
        return this;
    }

    @Override
    public HttpResponse.Builder reasonPhrase(String reasonPhrase) {
        this.reasonPhrase = reasonPhrase;
        return this;
    }

    @Override
    public HttpResponse.Builder addHeader(String name, String value) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(value);
        headers.addHeader(name, value);
        return this;
    }

    @Override
    public HttpResponse.Builder addHeaders(HttpHeaders headers) {
        this.headers = headers;
        return this;
    }

    @Override
    public HttpResponse.Builder body(byte[] body) {
        this.body = body;
        return this;
    }

    @Override
    public HttpResponse build() {
        return new HttpResponseImpl(httpVersion, request, uri, statusCode, redirect, reasonPhrase, headers, body);
    }
}
