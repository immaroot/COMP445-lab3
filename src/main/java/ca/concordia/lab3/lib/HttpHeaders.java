package ca.concordia.lab3.lib;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class HttpHeaders implements Writable {

    private final Map<String, List<String>> headers;

    private HttpHeaders() {
        this.headers = new TreeMap<>();
    }

    public static HttpHeaders createHttpHeaders() {
        return new HttpHeaders();
    }

    public void addHeader(String name, String value) {
        headers.computeIfAbsent(name.toLowerCase(), n -> new ArrayList<>()).add(value);
    }

    public String getValue(String headerName) {
        return headers.get(headerName.toLowerCase()).get(0);
    }

    public boolean hasValue(String headerName) {
        return headers.containsKey(headerName.toLowerCase());
    }

    @Override
    public String toString() {
        return "HttpHeaders{" +
                "headers=" + headers +
                '}';
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        StringBuilder sb = new StringBuilder();
        headers.forEach(
                (headerName,values) -> sb.append(headerName)
                        .append(": ")
                        .append(String.join(" ", values))
                        .append("\r\n"));
        out.write(sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String getRaw() {
        StringBuilder sb = new StringBuilder();
        headers.forEach(
                (headerName,values) -> sb.append(headerName)
                        .append(": ")
                        .append(String.join(" ", values))
                        .append("\r\n"));
        return sb.toString();
    }
}
