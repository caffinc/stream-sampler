package com.caffinc.researchgate.streamsampler.helper;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * This class stores the data written to the {@link PrintStream} into a {@link StringBuffer}
 *
 * @author Sriram
 */
class InspectableOutputStream extends OutputStream {
    private StringBuffer buffer = new StringBuffer();

    @Override
    public void write(int b) throws IOException {
        buffer.append((char) b);
    }

    /**
     * Extracts the {@code buffer} data, clears the {@code buffer} and returns the data
     *
     * @return Buffer data
     */
    String getBuffer() {
        String data = buffer.toString();
        buffer = new StringBuffer();
        return data;
    }
}

/**
 * This class allows {@link System#out} outputs to be inspected while redirecting it to the actual {@link System#out}
 * {@link PrintStream}.
 * It is hacky, and should not be used for anything more than testing.
 *
 * @author Sriram
 */
public class InspectablePrintStream extends PrintStream {
    private static final InspectableOutputStream oStream = new InspectableOutputStream();
    private static final PrintStream realPrintStream = System.out;
    private static final InspectablePrintStream mInstance = new InspectablePrintStream();

    /**
     * Default private constructor initializes the super ({@see PrintStream}) with an {@link InspectableOutputStream} and
     * replaces the {@link System#out} with {@code this}
     */
    private InspectablePrintStream() {
        super(oStream);
        System.setOut(this);
    }

    /**
     * Returns the singleton {@link InspectablePrintStream}
     *
     * @return singleton instance
     */
    public static InspectablePrintStream getPrintStream() {
        return mInstance;
    }

    @Override
    public void write(int b) {
        super.write(b);
        realPrintStream.write(b);
    }

    @Override
    public void write(byte[] buf, int off, int len) {
        super.write(buf, off, len);
        realPrintStream.write(buf, off, len);
    }

    @Override
    public void write(byte[] b) throws IOException {
        super.write(b);
        realPrintStream.write(b);
    }

    @Override
    public void flush() {
        super.flush();
        realPrintStream.flush();
    }

    /**
     * Returns the data from the underlying {@link OutputStream} buffer
     *
     * @return {@link System#out} data
     */
    public String getBuffer() {
        return oStream.getBuffer();
    }
}
