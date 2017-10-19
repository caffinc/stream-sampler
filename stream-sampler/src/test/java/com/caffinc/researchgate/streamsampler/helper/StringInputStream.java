package com.caffinc.researchgate.streamsampler.helper;

import java.io.IOException;
import java.io.InputStream;

/**
 * Iterates through the provided pattern and returns characters one by one, cyclically, up to the limit specified
 *
 * @author Sriram
 */
public class StringInputStream extends InputStream {
    private byte[] pattern;
    private int limit;
    private int position;
    private boolean halfChar = false;


    /**
     * Initializes the {@link StringInputStream} with a pattern and a limit
     *
     * @param pattern Pattern to return
     * @param limit   Limit of characters to produce
     */
    public StringInputStream(String pattern, int limit) {
        this.pattern = pattern.getBytes();
        this.limit = limit;
        this.position = 0;
    }

    /**
     * Iterates through the {@code pattern} and returns characters one by one, circling back to the start when the position
     * reaches the end of the {@code pattern}. Returns -1 when this method has been called {@code limit} times.
     *
     * @return Next character in the {@code pattern}
     * @throws IOException Not thrown by this implementation
     */
    @Override
    public int read() throws IOException {
        if (this.limit > 0) {
            int b = this.pattern[this.position];
            if (b < 0) {
                halfChar = !halfChar;
            }
            if (!halfChar) {
                this.limit--;
            }
            this.position = (this.position + 1) % this.pattern.length;
            return b;
        } else {
            return -1;
        }
    }
}
