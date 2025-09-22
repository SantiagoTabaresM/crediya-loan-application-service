package co.com.pragma.sqs.sender.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "adapter.sqs")
public record SQSSenderProperties(
     String region,
     String notificationQueueUrl,
     String debtCapacityQueueUrl,
     String incrementApprovedLoanReport,
     String endpoint
){
}
