package net.woggioni.jwo.internal;

import net.woggioni.jwo.Bucket;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongBinaryOperator;
import java.util.function.LongUnaryOperator;

import static net.woggioni.jwo.LongMath.ceilDiv;

public class LocalBucket implements Bucket {
    private final long maxCapacity;
    private final long fillAmount;
    private final long fillPeriod;

    private final AtomicLong availableTokens;
    private final AtomicLong lastFill;

    public LocalBucket(final long maxCapacity, final long fillAmount, final long fillPeriod) {
        this(maxCapacity, fillAmount, fillPeriod, maxCapacity);
    }

    public LocalBucket(final long maxCapacity, final long fillAmount, final long fillPeriod, final long initialAmount) {
        this(maxCapacity, fillAmount, fillPeriod, initialAmount, System.nanoTime());
    }

    public LocalBucket(final long maxCapacity, final long fillAmount, final long fillPeriod, final long initialAmount, final long currentTimestamp) {
        if (maxCapacity <= 0 || fillAmount <= 0 || fillPeriod <= 0) {
            throw new IllegalArgumentException("maxCapacity, fillAmount and fillPeriod must all be positive");
        }
        this.maxCapacity = maxCapacity;
        this.fillAmount = fillAmount;
        this.fillPeriod = fillPeriod;
        this.availableTokens = new AtomicLong(initialAmount);
        this.lastFill = new AtomicLong(currentTimestamp);
    }

    private long getTokenPrivate(final long nTokens, final long now, final long previousFillTime, final long currentFillTime) {
        final LongBinaryOperator tickCalculator = (lf, currentTimestamp) -> (currentTimestamp - lf) / fillPeriod;
        if (currentFillTime != previousFillTime) {
            final long ticks = tickCalculator.applyAsLong(previousFillTime, now);
            final LongUnaryOperator filledAmountCalculator = currentTokens -> Math.min(currentTokens + ticks * fillAmount, maxCapacity);
            final LongUnaryOperator tokenCalculator = currentTokens -> {
                final long filledAmount = filledAmountCalculator.applyAsLong(currentTokens);
                if (filledAmount >= nTokens) {
                    return filledAmount - nTokens;
                } else {
                    return filledAmount;
                }
            };
            final long previousTokenAmount = availableTokens.getAndUpdate(tokenCalculator);
            final long filledAmount = filledAmountCalculator.applyAsLong(previousTokenAmount);
            return filledAmount;
        } else {
            final long previousTokenAmount = availableTokens.getAndUpdate(n -> {
                if (n >= nTokens) {
                    return n - nTokens;
                } else {
                    return n;
                }
            });
            return previousTokenAmount;
        }
    }


    @Override
    public boolean removeTokens(final long nTokens) {
        return removeTokens(nTokens, System.nanoTime());
    }

    @Override
    public boolean removeTokens(final long nTokens, final long now) {
        if(nTokens > maxCapacity) throw new IllegalArgumentException("The requested number of tokens exceeds the bucket max capacity");
        final LongBinaryOperator tickCalculator = (lf, currentTimestamp) -> (currentTimestamp - lf) / fillPeriod;
        final LongBinaryOperator timestampCalculator = (lf, currentTimestamp) -> lf + tickCalculator.applyAsLong(lf, currentTimestamp) * fillPeriod;
        final long previousFillTime = lastFill.getAndAccumulate(now, timestampCalculator);
        final long currentFillTime = timestampCalculator.applyAsLong(previousFillTime, now);
        final long result = getTokenPrivate(nTokens, now, previousFillTime, currentFillTime);
        return result >= nTokens;
    }

    @Override
    public long removeTokensWithEstimate(final long nTokens) {
        return removeTokensWithEstimate(nTokens, System.nanoTime());
    }

    @Override
    public long removeTokensWithEstimate(final long nTokens, final long now) {
        if(nTokens > maxCapacity) throw new IllegalArgumentException("The requested number of tokens exceeds the bucket max capacity");
        final LongBinaryOperator tickCalculator = (lf, currentTimestamp) -> (currentTimestamp - lf) / fillPeriod;
        final LongBinaryOperator timestampCalculator = (lf, currentTimestamp) -> lf + tickCalculator.applyAsLong(lf, currentTimestamp) * fillPeriod;
        final long previousFillTime = lastFill.getAndAccumulate(now, timestampCalculator);
        final long currentFillTime = timestampCalculator.applyAsLong(previousFillTime, now);
        final long previousTokenAmount = getTokenPrivate(nTokens, now, previousFillTime, currentFillTime);
        if(previousTokenAmount >= nTokens) {
            return -1;
        } else {
            return Math.max(ceilDiv((nTokens - previousTokenAmount) * fillPeriod, fillAmount), currentFillTime + fillPeriod - now);
        }
    }
}
