package com.caffinc.researchgate.streamsampler.helper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

/**
 * Generates a random stream of input
 *
 * @author Sriram
 */
public class RandomInputStream extends InputStream {
    private Random random;
    private int streamSize;

    /**
     * Initializes the {@link RandomInputStream}
     *
     * @param seed       Seed for the {@link Random} object used to generate random values
     * @param streamSize Number of characters to generate
     */
    public RandomInputStream(int seed, int streamSize) {
        this.random = new Random(seed);
        this.streamSize = streamSize;
    }

    /**
     * Initializes the {@link RandomInputStream} with a default {@code seed} of 0
     *
     * @param streamSize Number of characters to generate
     */
    public RandomInputStream(int streamSize) {
        this(0, streamSize);
    }

    /**
     * Generates up to {@code streamSize} random numbers within the range 65-90 inclusive (A-Z)
     *
     * @return Random integer if this stream has generated less than {@code streamSize} integers, -1 otherwise
     * @throws IOException Not thrown by this implementation
     */
    @Override
    public int read() throws IOException {
        if (this.streamSize > 0) {
            this.streamSize--;
            return 65 + this.random.nextInt(26);
        } else {
            return -1;
        }
    }
}
