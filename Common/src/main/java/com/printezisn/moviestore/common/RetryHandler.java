package com.printezisn.moviestore.common;

import java.util.Random;
import java.util.concurrent.Callable;
import java.util.function.Function;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

/**
 * Retry handler for operations
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RetryHandler {

    @Builder.Default
    private int maxRetries = 5;

    @Builder.Default
    private int delay = 1000;

    @Builder.Default
    private boolean useExponentialBackOff = false;

    @Builder.Default
    private int jitter = 0;

    /**
     * Runs an operation and retries if an exception is thrown
     * 
     * @param operation
     *            The operation to run
     * @param condition
     *            The condition that indicates whether an exception is retriable or
     *            not
     * @return The result of the operation
     * @throws Exception
     *             Exception thrown by the operation
     */
    public <T> T run(final Callable<T> operation, final Function<Throwable, Boolean> condition) throws Exception {
        int retry = 0;
        final Random random = new Random();

        while (true) {
            try {
                return operation.call();
            }
            catch (final Exception | AssertionError ex) {
                retry++;
                if (!condition.apply(ex) || retry >= maxRetries) {
                    throw ex;
                }

                final double totalBackOff = useExponentialBackOff ? (Math.pow(2, retry - 1) * delay) : delay;
                final long totalDelay = (long) totalBackOff + random.nextInt(jitter);

                Thread.sleep(totalDelay);
            }
        }
    }
}
