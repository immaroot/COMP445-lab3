package ca.concordia.lab3.lib;

import java.io.IOException;
import java.io.OutputStream;

public interface Writable {

    void writeTo(OutputStream out) throws IOException;

    String getRaw() throws IOException;
}
