package com.caffinc.researchgate.streamsampler;

import com.caffinc.researchgate.streamsampler.helper.InspectablePrintStream;
import com.caffinc.researchgate.streamsampler.helper.RandomInputStream;
import com.caffinc.researchgate.streamsampler.helper.StringInputStream;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * Tests the {@link StreamSampler#sample(InputStream, int)} method
 *
 * @author Sriram
 */
public class NaiveStreamSamplerTest {
    private static final InspectablePrintStream printStream = InspectablePrintStream.getPrintStream();

    private static final Logger LOG = LoggerFactory.getLogger(StreamSampler.class);

    private String pattern = "THEQUICKBROWNFOXJUMPSOVERTHELAZYDOGöäÄÜÖß";

    /**
     * Tests {@link StreamSampler#sample(InputStream, int)} when a {@link java.io.InputStream} is passed to it
     *
     * @throws Exception {@link java.io.IOException} thrown by the passed {@link java.io.InputStream}'s read method
     */
    @Test
    public void testSample() throws Exception {
        int expectedSize = 10;
        String sample = new StreamSampler().sample(new RandomInputStream(0, 20), expectedSize);
        Assert.assertEquals("Sample should have " + expectedSize + " characters", expectedSize, sample.length());
    }

    /**
     * Tests that the {@link StreamSampler#sample(InputStream, int)} actually samples the provided input
     *
     * @throws Exception {@link java.io.IOException} thrown by the passed {@link java.io.InputStream}'s read method
     */
    @Test
    public void testSampleKnownString() throws Exception {
        int expectedSize = pattern.length();
        String sample = new StreamSampler().sample(new StringInputStream(pattern, pattern.length()), expectedSize);

        Set<Character> patternSet = new HashSet<>();
        for (Character c : pattern.toCharArray()) {
            patternSet.add(c);
        }

        boolean flag = true;
        for (Character c : sample.toCharArray()) {
            if (!patternSet.contains(c)) {
                flag = false;
                break;
            }
        }

        Assert.assertTrue("Sample should only have characters from the provided pattern", flag);
    }

    /**
     * Tests that the {@link StreamSampler#sample(InputStream, int)} samples up to available input if {@code sampleSize} exceeds stream size
     *
     * @throws Exception {@link java.io.IOException} thrown by the passed {@link java.io.InputStream}'s read method
     */
    @Test
    public void testSampleWithExcessSize() throws Exception {
        int sampleSize = pattern.length() + 10;
        String sample = new StreamSampler().sample(new StringInputStream(pattern, pattern.length()), sampleSize);
        Assert.assertEquals("Sample should have original pattern", pattern, sample);
    }


    /**
     * Tests that the {@link StreamSampler#sample(InputStream, int)} throws {@link IllegalArgumentException} for 0 {@code sampleSize}
     *
     * @throws Exception {@link java.io.IOException} thrown by the passed {@link java.io.InputStream}'s read method
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSampleWithZeroSampleSize() throws Exception {
        int sampleSize = 0;
        new StreamSampler().sample(new StringInputStream(pattern, pattern.length()), sampleSize);
    }

    /**
     * Tests that the {@link StreamSampler#sample(InputStream, int)} throws {@link IllegalArgumentException} for negative {@code sampleSize}
     *
     * @throws Exception {@link java.io.IOException} thrown by the passed {@link java.io.InputStream}'s read method
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSampleWithNegativeSampleSize() throws Exception {
        int sampleSize = -10;
        new StreamSampler().sample(new StringInputStream(pattern, pattern.length()), sampleSize);
    }


    /**
     * Tests that the {@link StreamSampler#sample(InputStream, int)} samples correctly when seeded
     *
     * @throws Exception {@link java.io.IOException} thrown by the passed {@link java.io.InputStream}'s read method
     */
    @Test
    public void testSampleWithSeed() throws Exception {
        int seed = 0;
        int expectedSize = 10;
        String sample = new StreamSampler(seed).sample(new StringInputStream(pattern, expectedSize * 10), expectedSize);
        Assert.assertEquals("Sample should only have characters from the provided pattern", "FHVBYRßFOX", sample);
    }


    /**
     * Tests that the {@link StreamSampler#sample(InputStream, int)} samples large streams correctly when seeded
     *
     * @throws Exception {@link java.io.IOException} thrown by the passed {@link java.io.InputStream}'s read method
     */
    @Test
    public void testSampleForLargeInputWithSeed() throws Exception {
        int seed = 0;
        int expectedSize = 10;
        String sample = new StreamSampler(seed).sample(new StringInputStream(pattern, expectedSize * 100000), expectedSize);
        Assert.assertEquals("Sample should only have expected characters for the seed", "BXVÜHÖEHGX", sample);
    }

}