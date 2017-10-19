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
 * Tests the {@link StreamSampler#fastSample(InputStream, int)} method
 * <p>
 * Note:
 * The functionality of the {@link StreamSampler#main(String[])} method is not tested here as it requires the application
 * to shutdown to trigger the {@code ShutdownHook}. See {@link Runtime#addShutdownHook(Thread)}.
 *
 * @author Sriram
 */
public class FastStreamSamplerTest {
    private static final InspectablePrintStream printStream = InspectablePrintStream.getPrintStream();

    private static final Logger LOG = LoggerFactory.getLogger(StreamSampler.class);

    private String pattern = "THEQUICKBROWNFOXJUMPSOVERTHELAZYDOGöäÄÜÖß";

    /**
     * Tests {@link StreamSampler#fastSample(InputStream, int)} when a {@link java.io.InputStream} is passed to it
     *
     * @throws Exception {@link java.io.IOException} thrown by the passed {@link java.io.InputStream}'s read method
     */
    @Test
    public void testFastSample() throws Exception {
        int expectedSize = 10;
        String sample = new StreamSampler().fastSample(new RandomInputStream(0, 20), expectedSize);
        Assert.assertEquals("Sample should have " + expectedSize + " characters", expectedSize, sample.length());
    }

    /**
     * Tests that the {@link StreamSampler#fastSample(InputStream, int)} actually samples the provided input
     *
     * @throws Exception {@link java.io.IOException} thrown by the passed {@link java.io.InputStream}'s read method
     */
    @Test
    public void testFastSampleKnownString() throws Exception {
        int expectedSize = pattern.length();
        String sample = new StreamSampler().fastSample(new StringInputStream(pattern, pattern.length()), expectedSize);

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
     * Tests that the {@link StreamSampler#fastSample(InputStream, int)} samples up to available input if {@code sampleSize} exceeds stream size
     *
     * @throws Exception {@link java.io.IOException} thrown by the passed {@link java.io.InputStream}'s read method
     */
    @Test
    public void testFastSampleWithExcessSize() throws Exception {
        int sampleSize = pattern.length() + 10;
        String sample = new StreamSampler().fastSample(new StringInputStream(pattern, pattern.length()), sampleSize);
        Assert.assertEquals("Sample should have original pattern", pattern, sample);
    }


    /**
     * Tests that the {@link StreamSampler#fastSample(InputStream, int)} throws {@link IllegalArgumentException} for 0 {@code sampleSize}
     *
     * @throws Exception {@link java.io.IOException} thrown by the passed {@link java.io.InputStream}'s read method
     */
    @Test(expected = IllegalArgumentException.class)
    public void testFastSampleWithZeroSampleSize() throws Exception {
        int sampleSize = 0;
        new StreamSampler().fastSample(new StringInputStream(pattern, pattern.length()), sampleSize);
    }

    /**
     * Tests that the {@link StreamSampler#fastSample(InputStream, int)} throws {@link IllegalArgumentException} for negative {@code sampleSize}
     *
     * @throws Exception {@link java.io.IOException} thrown by the passed {@link java.io.InputStream}'s read method
     */
    @Test(expected = IllegalArgumentException.class)
    public void testFastSampleWithNegativeSampleSize() throws Exception {
        int sampleSize = -10;
        new StreamSampler().fastSample(new StringInputStream(pattern, pattern.length()), sampleSize);
    }


    /**
     * Tests that the {@link StreamSampler#fastSample(InputStream, int)} samples correctly when seeded
     *
     * @throws Exception {@link java.io.IOException} thrown by the passed {@link java.io.InputStream}'s read method
     */
    @Test
    public void testFastSampleWithSeed() throws Exception {
        int seed = 0;
        int expectedSize = 10;
        String sample = new StreamSampler(seed).fastSample(new StringInputStream(pattern, expectedSize * 10), expectedSize);
        Assert.assertEquals("Sample should only have characters from the provided pattern", "DNGGYTDäOE", sample);
    }


    /**
     * Tests that the {@link StreamSampler#fastSample(InputStream, int)} samples large streams correctly when seeded
     *
     * @throws Exception {@link java.io.IOException} thrown by the passed {@link java.io.InputStream}'s read method
     */
    @Test
    public void testFastSampleForLargeInputWithSeed() throws Exception {
        int seed = 0;
        int expectedSize = 10;
        String sample = new StreamSampler(seed).fastSample(new StringInputStream(pattern, expectedSize * 100000), expectedSize);
        Assert.assertEquals("Sample should only have expected characters for the seed", "HTKLöOöQVE", sample);
    }


    /**
     * Tests the {@link StreamSampler#main} behavior when the command line argument is missing or several
     *
     * @throws Exception Not thrown in the code
     */
    @Test
    public void testMainWithMissingSizeArgument() throws Exception {
        // Clear the buffer
        printStream.getBuffer();
        String expectedError = "Error: Too few or too many arguments passed\n" +
                "StreamSampler Usage:\n" +
                "===================\n" +
                "cat abc.txt | java -jar stream-sampler.jar n\n" +
                "This samples \"n\" characters from the piped input\n";
        StreamSampler.main(new String[]{});
        String actualError = printStream.getBuffer();
        Assert.assertEquals("Error and usage message should be displayed when sample size is not present in the arguments",
                expectedError, actualError);
    }
}