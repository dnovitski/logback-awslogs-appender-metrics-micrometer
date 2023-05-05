package ca.pjer.logback.metrics.micrometer.autoconfigure;

import ca.pjer.logback.AwsLogsAppender;
import ca.pjer.logback.metrics.AwsLogsMetrics;
import ca.pjer.logback.metrics.AwsLogsMetricsHolder;
import ca.pjer.logback.metrics.micrometer.instrument.AwsLogsMetricsMeter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotatedTypeMetadata;

@Configuration
@Conditional(AwsLogsMetricsAutoConfiguration.AwsLogsMetricsCondition.class)
@ConditionalOnClass({MeterRegistry.class, AwsLogsMetrics.class, AwsLogsAppender.class})
@ConditionalOnProperty(value = "management.metrics.binders.awslogs.enabled", matchIfMissing = true)
public class AwsLogsMetricsAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public AwsLogsMetricsMeter awsLogsMetrics() {
        return new AwsLogsMetricsMeter();
    }

    static class AwsLogsMetricsCondition extends SpringBootCondition {
        @Override
        public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
            ConditionMessage.Builder message = ConditionMessage.forCondition("AwsLogsMetricsCondition");
            if (AwsLogsMetricsHolder.isDesired()) {
                return ConditionOutcome.match(message.because("AwsLogsMetricsHolder indicated metrics are desired"));
            } else {
                return ConditionOutcome.noMatch(message.because("AwsLogsMetricsHolder did not indicate metrics desired"));
            }
        }
    }
}
