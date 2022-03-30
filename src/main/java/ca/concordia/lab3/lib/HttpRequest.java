package ca.concordia.lab3.lib;

import java.net.URI;

public abstract class HttpRequest implements Writable {

    protected HttpRequest() {}

    public interface Builder {

        Builder uri(URI uri);

        Builder addHeader(String name, String value);

        Builder addHeaders(HttpHeaders headers);

        Builder GET();

        Builder POST(byte[] body);

        HttpRequest build();
    }

    public static Builder newBuilder() {
        return new HttpRequestBuilderImpl();
    }

    public static Builder newBuilder(URI uri) {
        return new HttpRequestBuilderImpl(uri);
    }

    public abstract URI uri();

    public abstract HttpHeaders headers();

    public abstract String method();

    public abstract byte[] body();
}
