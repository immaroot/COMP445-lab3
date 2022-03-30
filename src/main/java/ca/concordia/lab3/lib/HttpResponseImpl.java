package ca.concordia.lab3.lib;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class HttpResponseImpl extends HttpResponse {

    private final HttpRequest request;
    private final URI uri;
    private final String httpVersion;
    private final int statusCode;
    private final boolean redirect;
    private final String reasonPhrase;
    private final HttpHeaders headers;
    private final byte[] body;

    public HttpResponseImpl(String httpVersion,
                            HttpRequest request,
                            URI uri,
                            int statusCode,
                            boolean redirect, String reasonPhrase,
                            HttpHeaders headers,
                            byte[] body) {
        this.httpVersion  = httpVersion;
        this.request      = request;
        this.uri          = uri;
        this.statusCode   = statusCode;
        this.redirect     = redirect;
        this.reasonPhrase = reasonPhrase;
        this.headers      = headers;
        this.body         = body;
    }

    @Override
    public HttpRequest request() {
        return this.request;
    }

    @Override
    public URI uri() {
        return this.uri;
    }

    @Override
    public String httpVersion() {
        return this.httpVersion;
    }

    @Override
    public String reasonPhrase() {
        return this.reasonPhrase;
    }

    @Override
    public int statusCode() {
        return this.statusCode;
    }

    @Override
    public boolean redirect() {
        return this.redirect;
    }

    @Override
    public HttpHeaders headers() {
        return this.headers;
    }

    @Override
    public byte[] body() {
        return this.body;
    }

    @Override
    public String getRaw() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeTo(out);
        return out.toString(StandardCharsets.UTF_8);
    }

    @Override
    public String toString() {
        return "HttpResponseImpl{" +
                "request=" + request +
                ", uri=" + uri +
                ", httpVersion='" + httpVersion + '\'' +
                ", statusCode=" + statusCode +
                ", reasonPhrase='" + reasonPhrase + '\'' +
                ", headers=" + headers +
                ", body='" + Arrays.toString(body) + '\'' +
                '}';
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        out.write(httpVersion.getBytes(StandardCharsets.UTF_8));
        out.write(' ');
        out.write(String.valueOf(statusCode()).getBytes(StandardCharsets.UTF_8));
        out.write(' ');
        out.write(reasonPhrase().getBytes(StandardCharsets.UTF_8));
        out.write('\r');
        out.write('\n');
        if (headers != null) {
            headers().writeTo(out);
        }
        out.write('\r');
        out.write('\n');
        if (body() == null) {
            return;
        }
        out.write(body);
    }
}
