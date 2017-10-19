package com.caffinc.researchgate.streamsampler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Random;

/**
 * <h1>StreamSampler</h1>
 * <p>
 * This class samples incoming streams of data based on the
 * <a href="https://en.wikipedia.org/wiki/Reservoir_sampling">Reservoir Sampling algorithm</a>.
 *
 * @author Sriram
 */
public class StreamSampler {
    private static final Logger LOG = LoggerFactory.getLogger(StreamSampler.class);

    private Random random;

    /**
     * Default constructor which initializes a random stream sampler
     */
    public StreamSampler() {
        this.random = new Random();
    }

    /**
     * Initializes a random stream sampler with a seed for reproducibility
     *
     * @param seed Seed for the random state
     */
    public StreamSampler(int seed) {
        this.random = new Random(seed);
    }

    /**
     * Main method executed when this application is started from the commandline.
     * Utilizes a {@link Runtime#addShutdownHook(Thread)} to monitor process shutdown to print the sample to the console.
     *
     * @param args Command line arguments specifying the streamSize
     * @throws IOException Thrown by the underlying {@link InputStream}
     */
    public static void main(String[] args) throws IOException {
        try {
            if (args.length != 1) {
                throw new IllegalArgumentException("Too few or too many arguments passed");
            }
            int sampleSize = Integer.parseInt(args[0]);
            final char[] sample = new char[sampleSize];
            // Register a ShutdownHook to print the final sample
            Runtime.getRuntime().addShutdownHook(new Thread(() -> LOG.info(new String(sample))));
            new StreamSampler().fastSample(System.in, sample);
        } catch (IllegalArgumentException e) {
            LOG.error("Error: {}", e.getLocalizedMessage());
            LOG.error(getUsageMessage());
        }
    }

    /**
     * Performs a Reservoir Sampling of the {@code stream}. If the input contains less than {@code streamSize}
     * characters, then the sample will contain all of the characters from the input.
     *
     * @param stream     Stream to read data from
     * @param sampleSize Size to sample from the stream
     * @return Sampled String containing at max {@code sampleSize} characters
     * @throws IOException Thrown by the underlying {@link StreamSampler#sample(InputStream, char[])} method
     */
    public String sample(InputStream stream, int sampleSize) throws IOException {
        if (sampleSize <= 0) {
            throw new IllegalArgumentException("Sample size must be positive");
        }
        char[] sample = new char[sampleSize];
        long count = sample(stream, sample);
        return new String(sample, 0, (int) Math.min(sampleSize, count));
    }

    /**
     * Performs a Reservoir Sampling of the {@code stream}. If the input contains less than {@code sample.length}
     * characters, then the sample will contain all of the characters from the input.
     * <p>
     * Note: This method has bad side-effects, i.e. it modifies the {@code sample} parameter's contents, which is
     * unfortunately required to allow for Ctrl+C behavior
     *
     * @param stream Stream to read data from
     * @param sample Character buffer to hold the sample from the stream
     * @return Number of characters read from the stream
     * @throws IOException Thrown by the passed {@link InputStream}'s read method
     */
    private long sample(InputStream stream, char[] sample) throws IOException {
        int sampleSize = sample.length;
        long count = 0;
        int length;
        char[] buffer = new char[1000];
        BufferedReader br = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
        while ((length = br.read(buffer)) > 0) {
            for (int i = 0; i < length; i++) {
                char character = buffer[i];
                if (count < sampleSize) {
                    sample[(int) count] = character;
                } else {
                    long randomPosition = Math.abs(random.nextLong() % (count + 1));
                    if (randomPosition < sampleSize) {
                        sample[(int) randomPosition] = character;
                    }
                }
                count++;
            }
        }
        return count;
    }

    /**
     * Performs a Fast Approximate Reservoir Sampling of the {@code stream}. Speed improvements are noticeable when
     * the input is several orders of magnitude larger than the {@code sampleSize}.
     * <p>
     * Based on <a href="http://erikerlandson.github.io/blog/2015/11/20/very-fast-reservoir-sampling/">Erik Erlandson's blog</a>.
     *
     * @param stream     Stream to read data from
     * @param sampleSize Size to sample from the stream
     * @return Sampled String containing at max {@code sampleSize} characters
     * @throws IOException Thrown by the passed {@link InputStream}'s read method
     */
    public String fastSample(InputStream stream, int sampleSize) throws IOException {
        if (sampleSize <= 0) {
            throw new IllegalArgumentException("Sample size must be positive");
        }
        char[] sample = new char[sampleSize];
        long count = fastSample(stream, sample);
        return new String(sample, 0, (int) Math.min(sampleSize, count));
    }


    /**
     * Performs a Fast Approximate Reservoir Sampling of the {@code stream}. Speed improvements are noticeable when
     * the input is several orders of magnitude larger than the {@code sampleSize}.
     * <p>
     * Based on <a href="http://erikerlandson.github.io/blog/2015/11/20/very-fast-reservoir-sampling/">Erik Erlandson's blog</a>.
     *
     * @param stream Stream to read data from
     * @param sample Character buffer to hold the sample from the stream
     * @return Number of characters read from the stream
     * @throws IOException Thrown by the passed {@link InputStream}'s read method
     */
    private long fastSample(InputStream stream, char[] sample) throws IOException {
        int sampleSize = sample.length;
        int threshold = 4 * sampleSize;
        long skip = -1;
        long count = 0;
        int length;
        char[] buffer = new char[1000];
        BufferedReader br = new BufferedReader(new InputStreamReader(stream));
        while ((length = br.read(buffer)) > 0) {
            for (int i = 0; i < length; i++) {
                char character = buffer[i];
                if (count < sampleSize) {
                    sample[(int) count] = character;
                } else {
                    if (count < threshold) {
                        // Up to a Threshold, do the naive way
                        long randomPosition = Math.abs(random.nextLong() % (count + 1));
                        if (randomPosition < sampleSize) {
                            sample[(int) randomPosition] = character;
                        }
                    } else {
                        // After the Threshold, instead of finding the probability of the character going into the sample,
                        // compute the number of characters to skip before picking one and putting it in the sample
                        if (skip == -1) {
                            double probability = sampleSize / (double) count;
                            double rf = random.nextDouble();
                            skip = ((Double) Math.floor(Math.log(rf) / Math.log(1 - probability))).intValue();
                        } else if (skip == 0) {
                            int randomPosition = random.nextInt(sampleSize);
                            sample[randomPosition] = character;
                            skip = -1;
                        }

                        // There is a skip required, skip up to the min(skip, remaining buffer length)
                        if (skip > 0) {
                            count += Math.min(skip - 1, length - i - 1L);
                            if (skip < length - i - 1) {
                                // Skip the remaining characters in skip and sample the next character
                                i += skip - 1;
                                skip = 0;
                            } else {
                                // Skip all the remaining characters in this buffer and reduce that amount from the skip
                                skip -= length - i;
                                i = length;
                            }
                        }
                    }
                }
                count++;
            }
        }
        return count;
    }

    /**
     * Returns a help message showing usage
     *
     * @return Help message
     */
    private static String getUsageMessage() {
        return "StreamSampler Usage:\n" +
                "===================\n" +
                "cat abc.txt | java -jar stream-sampler.jar n\n" +
                "This samples \"n\" characters from the piped input";
    }
}
