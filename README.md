# Stream Sampler
## Strategy
The Stream Sampler performs random sampling on a Stream of unbound data. It runs in O(n) time and requires O(k) space where k is the sample size. It implements the Reservoir Sampling algorithm, and a Fast Approximate algorithm as well (with a claimed O(log n) for bounded data).

## Code Structure
The code in this directory follows Maven structure:

```
├── pom.xml
└── src
    ├── main
    │   ├── java
    │   │   └── com
    │   │       └── caffinc
    │   │           └── researchgate
    │   │               └── streamsampler
    │   │                   └── StreamSampler.java
    │   └── resources
    │       └── log4j.properties
    └── test
        ├── java
        │   └── com
        │       └── caffinc
        │           └── researchgate
        │               └── streamsampler
        │                   ├── ComparisonTest.java
        │                   ├── FastStreamSamplerTest.java
        │                   ├── helper
        │                   │   ├── InspectablePrintStream.java
        │                   │   ├── RandomInputStream.java
        │                   │   └── StringInputStream.java
        │                   └── NaiveStreamSamplerTest.java
        └── resources
            └── log4j.properties

```

## Build and Test
You can build it by using:
```
mvn clean package
```
This will run the tests as well.

To skip tests, please run:
```
mvn clean package -DskipTests
```

The above step(s) produces the `stream-sampler.jar` in the target/ folder. 

### Note
There is a test called `testSampleSpeed` which compares the performance of the `sample` and `fastSample` methods which takes some time to run (On my i7 6500U it takes about 140 seconds) as the methods are single-threaded. It might be advisable to skip tests for repeated builds.
The main method cannot be unit tested for the positive scenario as it utilizes a Shutdown Hook to print the output which cannot be tested within JUnit.

## Usage

### Command Line
You can run the Stream Sampler using the following command:
```
cat xyz.txt | java -jar stream-sampler.jar 5
```
This will sample 5 characters out of the xyz.txt file.  You can also just run the jar using:
```
java -jar stream-sampler.jar 5
```
This will read from the console input until *Ctrl+C* is pressed.

### In code
The StreamSampler class exposes two member methods:

#### 1. Naive Sampling
```
public String sample(InputStream stream, int sampleSize)
```
This method samples the given InputStream and returns a maximum of `sampleSize` samples, and a minimum of `min(stream size, sampleSize)` samples. It uses the naive **Reservoir Sampling algorithm**.

#### 2. Fast Approximation
```
public String fastSample(InputStream stream, int sampleSize)
```

This method is similar to the one above, except it uses **Fast Approximation** to perform lesser computations, which provides additional performance boost as the random values do not have to be calculated for every element in the stream.

### Note:
There is a constructor of the StreamSampler class which accepts a seed integer which can be used to ensure reproducibility.
The code uses BufferedReader to read from the InputStream. This is faster for reading piped text as input is very fast when piped, but slower when the InputStream is populated in the code. Which is better depends on the usage pattern.

## Benchmarks
The application took `8.14819s (123 MB/s)` against an input of `1GB` to sample 5 characters:

```
$ dd if=/dev/urandom count=1000 bs=1MB | base64 | java -jar stream-sampler.jar 5
1000+0 records in
1000+0 records out
1000000000 bytes (1.0 GB, 954 MiB) copied, 8.14819 s, 123 MB/s
3Dj8E
```

Compare this to a fast grep operation which takes 7.99957s (125 MB/s), which is very similar:
```
$ dd if=/dev/urandom count=1000 bs=1MB | base64 | grep " "
1000+0 records in
1000+0 records out
1000000000 bytes (1.0 GB, 954 MiB) copied, 7.99957 s, 125 MB/s
```

## Assumptions
1. Input will only be in the form of `InputStreams`, be it `System.in` or otherwise.
2. While the code can accept very large amounts of data, it has been tested only with around a few hundred megabytes.
3. In order to support very large amounts of data, computations that could use `random.nextInt()` use `random.nextLong()`, which is much slower. If the sample space is guaranteed to be within Integer bounds, this can be reverted to make things faster. Alternatively a faster pseudo-random generator could be used to generate these numbers, but this has not been explored.
4. To keep the command-line simple, only one parameter indicating the size of the sample is accepted. This means that the system will only perform fast sampling on the data (default) when called from the command-line. There can be more comprehensive command-line parameters made available to support more fine-grained operations.
5. The core code only spans one class (`StreamSampler`) and as such can be built and executed directly without `Maven` (with some minor rewrite), but `Maven` brings a lot of organizational niceties which is why it is used here.
6. As the data is available as a single stream, multi-threaded operations have not been performed. If the input can be split into multiple chunks, a distributed or parallel method could compute individual samples from chunks and collect and merge them all together in the end.
7. `Loggers` have been used to write to `System.out` instead of writing to `System.out` directly. This gives a bit more control over where the program output is redirected to, although this might be unnecessary.
8. There is a newline printed to the console after the output, which may make piping the output to downstream processes from the command line a bit annoying, but as this is a business decision which has pros and cons for either approach, the newline has been left in.
