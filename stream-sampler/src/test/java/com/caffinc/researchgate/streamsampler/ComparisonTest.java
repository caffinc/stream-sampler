package com.caffinc.researchgate.streamsampler;

import com.caffinc.researchgate.streamsampler.helper.InspectablePrintStream;
import com.caffinc.researchgate.streamsampler.helper.StringInputStream;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests the {@link StreamSampler} class
 * <p>
 * Note:
 * The functionality of the {@link StreamSampler#main(String[])} method is not tested here as it requires the application
 * to shutdown to trigger the {@code ShutdownHook}. See {@link Runtime#addShutdownHook(Thread)}.
 *
 * @author Sriram
 */
public class ComparisonTest {
    private static final InspectablePrintStream printStream = InspectablePrintStream.getPrintStream();

    private static final Logger LOG = LoggerFactory.getLogger(StreamSampler.class);

    private String pattern = "THEQUICKBROWNFOXJUMPSOVERTHELAZYDOGöäÄÜÖß";

    /**
     * Compares the speeds of {@link StreamSampler#sample} and {@link StreamSampler#fastSample}, proving that the Fast
     * Approximation algorithm is indeed faster.
     *
     * @throws Exception {@link java.io.IOException} thrown by the passed {@link java.io.InputStream}'s read method
     */
    @Test
    public void testSampleSpeed() throws Exception {
        double naiveTime = 0;
        double fastApproximationTime = 0;
        int nRuns = 20;

        int seed = 0;
        int expectedSize = 10;

        LOG.info("Slow test running, expect delays of up to 2 minutes (Processor dependent)");

        for (int i = 1; i <= nRuns; i++) {
            long startTime = System.nanoTime();
            new StreamSampler(seed).sample(new StringInputStream(pattern, expectedSize * 10000000), expectedSize);
            naiveTime += System.nanoTime() - startTime;
            if (i % 2 == 0) {
                LOG.info("Completed {}/{} runs for Naive approach, average {}ns", i, nRuns, naiveTime / i);
            }
        }

        for (int i = 1; i <= nRuns; i++) {
            long startTime = System.nanoTime();
            new StreamSampler(seed).fastSample(new StringInputStream(pattern, expectedSize * 10000000), expectedSize);
            fastApproximationTime += System.nanoTime() - startTime;
            if (i % 2 == 0) {
                LOG.info("Completed {}/{} runs for Fast Approximation approach, average {}ns", i, nRuns, fastApproximationTime / i);
            }
        }

        LOG.info("Speedup of {}% observed", (naiveTime - fastApproximationTime) * 100 / fastApproximationTime);

        Assert.assertTrue("Fast Approximation should be faster than Naive approach", fastApproximationTime < naiveTime);
    }
}