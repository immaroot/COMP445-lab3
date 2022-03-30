package ca.concordia.lab3.lib;

import java.net.URI;

public abstract class HttpResponse implements Writable {

    protected HttpResponse() {}

    public interface Builder {

        Builder httpVersion(String httpVersion);

        Builder request(HttpRequest request);

        Builder uri(URI uri);

        Builder statusCode(int statusCode);

        Builder reasonPhrase(String reasonPhrase);

        Builder addHeader(String name, String value);

        Builder addHeaders(HttpHeaders headers);

        Builder body(byte[] body);

        HttpResponse build();
    }

    public static Builder newBuilder() {
        return new HttpResponseBuilderImpl();
    }

    public abstract HttpRequest request();

    public abstract URI uri();

    public abstract String httpVersion();

    public abstract String reasonPhrase();

    public abstract int statusCode();

    public abstract boolean redirect();

    public abstract HttpHeaders headers();

    public abstract byte[] body();
}
