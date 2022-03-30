package ca.concordia.lab3.lib;

import java.net.URI;

public class HttpRequestLineBuilder {
    private String method;
    private URI uri;
    private String httpVersion;

    private HttpRequestLineBuilder() {
    }

    public static HttpRequestLineBuilder createHttpRequestLineBuilder() {
        return new HttpRequestLineBuilder();
    }

    public HttpRequestLineBuilder setMethod(String method) {
        this.method = method;
        return this;
    }

    public HttpRequestLineBuilder setUri(URI uri) {
        this.uri = uri;
        return this;
    }

    public HttpRequestLineBuilder setHttpVersion(String httpVersion) {
        this.httpVersion = httpVersion;
        return this;
    }

    public HttpRequestLine createHttpRequestLine() {
        return new HttpRequestLine(method, uri, httpVersion);
    }
}