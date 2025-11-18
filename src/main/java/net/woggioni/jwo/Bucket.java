package net.woggioni.jwo;

import net.woggioni.jwo.internal.LocalBucket;

import java.time.Duration;

/**
 * This class implements the <a href="https://en.wikipedia.org/wiki/Token_bucket">token bucket algorithm</a>
 * for throttling
 *
 * @see <a href="https://en.wikipedia.org/wiki/Token_bucket">token bucket algorithm</a>
 */
public interface Bucket {
    /**
     * Tries to consume nTokens from the bucket, returning true if the operation was successful, false otherwise.
     * When this method returns false no tokens are actually consumed and the user is advised to retry after sometime
     *
     * @param nTokens numberOfTokens to consume
     * @return false if the bucket did not contain enough token for the operation to complete,
     * true otherwise (in the latter case nTokens are actually consumed)
     */
    default boolean removeTokens(final long nTokens) {
        return removeTokens(nTokens, System.nanoTime());
    }

    /**
     * Tries to consume nTokens from the bucket, returning true if the operation was successful, false otherwise.
     * When this method returns false no tokens are actually consumed and the user is advised to retry after sometime
     *
     * @param nTokens numberOfTokens to consume
     * @param currentTimestamp timestamp used to compute how much time has elapsed since the last bucket refill
     * @return false if the bucket did not contain enough token for the operation to complete,
     * true otherwise (in the latter case nTokens are actually consumed)
     */
    boolean removeTokens(final long nTokens, final long currentTimestamp);

    /**
     * Tries to consume nTokens from the bucket, returning -1 if the operation was successful or the amount of time
     * to wait before calling the method again that guarantees a positive outcome
     *
     * @param nTokens numberOfTokens to consume
     * @return -1 if the bucket did not contain enough token for the operation to complete,
     * a positive number of nanoseconds to wait that guarantees that the next
     * {@link #removeTokensWithEstimate(long)} or {@link #removeTokens(long)} invocation with the same {@param nTokens} parameter
     * will succeed
     */
    default long removeTokensWithEstimate(final long nTokens) {
        return removeTokensWithEstimate(nTokens, System.nanoTime());
    }

    /**
     * Tries to consume nTokens from the bucket, returning -1 if the operation was successful or the amount of time
     * to wait before calling the method again that guarantees a positive outcome
     *
     * @param nTokens numberOfTokens to consume
     * @param currentTimestamp timestamp used to compute how much time has elapsed since the last bucket refill
     * @return -1 if the bucket did not contain enough token for the operation to complete,
     * a positive number of nanoseconds to wait that guarantees that the next
     * {@link #removeTokensWithEstimate(long)} or {@link #removeTokens(long)} invocation with the same {@param nTokens} parameter
     * will succeed
     */
    long removeTokensWithEstimate(final long nTokens, final long currentTimestamp);

    static Bucket local(final long maxCapacity, final long fillAmount, final Duration fillPeriod) {
        return local(maxCapacity, fillAmount, fillPeriod, maxCapacity);
    }

    static Bucket local(final long maxCapacity, final long fillAmount, final Duration fillPeriod, final long initialAmount) {
        return new LocalBucket(maxCapacity, fillAmount, fillPeriod.toNanos(), initialAmount);
    }
}

