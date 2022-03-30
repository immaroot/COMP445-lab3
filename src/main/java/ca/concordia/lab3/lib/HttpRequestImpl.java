package ca.concordia.lab3.lib;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public class HttpRequestImpl extends HttpRequest {

    private final URI uri;
    private final static String HTTP_VERSION = "HTTP/1.0";
    private final HttpHeaders headers;
    private final String method;
    private final byte[] body;

    public HttpRequestImpl(URI uri,
                           HttpHeaders headers,
                           String method,
                           byte[] body) {
        this.uri     = uri;
        this.headers = headers;
        this.method  = method;
        if (body != null && body.length != 0) {
            this.body = body;
            this.headers.addHeader("Content-Length", String.valueOf(body.length));
        } else {
            this.body = null;
        }
    }

    @Override
    public URI uri() {
        return this.uri;
    }

    @Override
    public HttpHeaders headers() {
        return this.headers;
    }

    @Override
    public String method() {
        return this.method;
    }

    @Override
    public byte[] body() {
        return this.body;
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        out.write(method().getBytes(StandardCharsets.UTF_8));
        out.write(' ');
        out.write(uri().toString().getBytes(StandardCharsets.UTF_8));
        out.write(' ');
        out.write(HTTP_VERSION.getBytes(StandardCharsets.UTF_8));
        out.write('\r');
        out.write('\n');
        headers().writeTo(out);
        out.write('\r');
        out.write('\n');
        if (body() == null) {
            return;
        }
        out.write(body);
        out.write('\r');
        out.write('\n');
    }

    @Override
    public String getRaw() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeTo(out);
        return out.toString(StandardCharsets.UTF_8);
    }

    @Override
    public String toString() {
        return "HttpRequestImpl{" +
                "uri=" + uri +
                ", headers=" + headers +
                ", method='" + method + '\'' +
                ", body='" + body + '\'' +
                '}';
    }
}
