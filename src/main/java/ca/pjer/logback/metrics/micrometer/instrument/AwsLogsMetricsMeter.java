package ca.pjer.logback.metrics.micrometer.instrument;

import ca.pjer.logback.metrics.AwsLogsMetrics;
import ca.pjer.logback.metrics.AwsLogsMetricsHolder;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import software.amazon.awssdk.awscore.exception.AwsServiceException;

public class AwsLogsMetricsMeter implements MeterBinder, AwsLogsMetrics {
    private Counter lostCounter;
    private Counter batchRequeueFailedCounter;
    private Counter sentBytesCounter;
    private Counter eventsCounter;
    private Counter putLogCounter;
    private MeterRegistry registry;

    @Override
    public void bindTo(MeterRegistry registry) {
        this.registry = registry;

        this.lostCounter = Counter.builder("awslogs.lost")
                .description("Total lost log events")
                .register(registry);

        this.batchRequeueFailedCounter = Counter.builder("awslogs.failed.batch.requeue")
                .description("Total failed batch requeues")
                .register(registry);

        this.sentBytesCounter = Counter.builder("awslogs.sent.size")
                .description("Total size in bytes sent")
                .register(registry);

        this.eventsCounter = Counter.builder("awslogs.events")
                .description("Total log events")
                .register(registry);

        this.putLogCounter = Counter.builder("awslogs.putlog")
                .description("Total PutLog requests")
                .register(registry);

        AwsLogsMetricsHolder.set(this);
    }

    @Override
    public void incrementLostCount() {
        lostCounter.increment();
    }

    @Override
    public void incrementBatchRequeueFailed() {
        batchRequeueFailedCounter.increment();
    }

    @Override
    public void incrementFlushFailed(Throwable throwable) {
        Counter.builder("awslogs.failed.flush")
                .tags("type", getExceptionType(throwable))
                .description("Total failed flushes")
                .register(registry)
                .increment();
    }

    @Override
    public void incrementPutLogFailed(Throwable throwable) {
        Counter.builder("awslogs.failed.putlog")
                .tags("type", getExceptionType(throwable))
                .description("Total failed PutLog requests")
                .register(registry)
                .increment();
    }

    @Override
    public void incrementBatch(int batchSize) {
        sentBytesCounter.increment(batchSize);
    }

    @Override
    public void incrementLogEvents(int eventCount) {
        eventsCounter.increment(eventCount);
    }

    @Override
    public void incrementPutLog() {
        putLogCounter.increment();
    }

    private String getExceptionType(Throwable throwable) {
        String exceptionType = throwable.getClass().getSimpleName();
        if (throwable instanceof AwsServiceException) {
            AwsServiceException awsLogsException = (AwsServiceException) throwable;
            exceptionType = awsLogsException.awsErrorDetails().errorCode();
        }
        return exceptionType;
    }
}
