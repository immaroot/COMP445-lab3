package ca.concordia.lab3.lib;

import java.net.URI;
import java.util.Objects;

public class HttpRequestLine {
    String method;
    URI uri;
    String httpVersion;

    public HttpRequestLine(String method, URI uri, String httpVersion) {
        this.method      = method;
        this.uri         = uri;
        this.httpVersion = httpVersion;
    }

    public String getMethod() {
        return method;
    }

    public URI getUri() {
        return uri;
    }

    public String getHttpVersion() {
        return httpVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HttpRequestLine)) return false;
        HttpRequestLine that = (HttpRequestLine) o;
        return Objects.equals(getMethod(), that.getMethod()) && Objects.equals(getUri(), that.getUri()) && Objects.equals(getHttpVersion(), that.getHttpVersion());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMethod(), getUri(), getHttpVersion());
    }
}
