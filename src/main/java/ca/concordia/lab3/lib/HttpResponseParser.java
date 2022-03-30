package ca.concordia.lab3.lib;

import java.io.InputStream;
import java.util.Scanner;
import java.util.regex.Pattern;

public class HttpResponseParser {

    Scanner scanner;

    private String httpVersion;
    private int statusCode;
    private String reasonPhrase;
    private HttpHeaders headers;
    private byte[] body;

    private HttpResponseParser() {
    }

    public static HttpResponseParser createHttpResponseParser() {
        return new HttpResponseParser();
    }

    public void parse(InputStream inputStream) {
        scanner = new Scanner(inputStream).useDelimiter("\\r\\n");
        parseResponse();
    }

    private void parseResponse() {
        parseStatusLine();
        int contentLength = parseHeaders();
        if (contentLength > 0) {
            parseBody(contentLength);
        }
    }

    private void parseBody(int contentLength) {
        body = scanner.next(Pattern.compile(".*", Pattern.DOTALL)).getBytes();
        assert body.length == contentLength;
    }

    protected void parseStatusLine() {
        String line = scanner.next();
        Scanner lineScanner = new Scanner(line);
        httpVersion = getHttpVersion(lineScanner);
        statusCode = getStatusCode(lineScanner);
        reasonPhrase = getReasonPhrase(lineScanner);
    }

    private String getHttpVersion(Scanner lineScanner) {
        assert lineScanner.hasNext("HTTP/\\d*.\\d*") : "Was expecting regex \"HTTP/\\d*.\\d*\"";
        String httpVersion = lineScanner.next("HTTP/\\d*.\\d*");
        lineScanner.skip("\\h");
        return httpVersion;
    }

    private int getStatusCode(Scanner lineScanner) {
        assert lineScanner.hasNext("\\d{3}") : "Was expecting regex \"\\d{3}\"";
        int statusCode = Integer.parseInt(lineScanner.next("\\d{3}"));
        lineScanner.skip("\\h");
        return statusCode;
    }

    private String getReasonPhrase(Scanner lineScanner) {
        assert lineScanner.hasNext("[ \\w]+") : "Was expecting regex \"[ \\w]+\"";
        return lineScanner.next();
    }

    private int parseHeaders() {
        int contentLength = 0;
        headers = HttpHeaders.createHttpHeaders();
        while (scanner.hasNext()) {
            String line = scanner.next();
            if (line.isEmpty()) break;
            String name = line.split(":")[0].strip();
            String value = line.split(":", 2)[1].strip();
            if (name.equals("Content-Length")) contentLength = Integer.parseInt(value);
            headers.addHeader(name, value);
        }
        return contentLength;
    }

    public String getHttpVersion() {
        return httpVersion;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getReasonPhrase() {
        return reasonPhrase;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    public byte[] getBody() {
        return body;
    }
}
