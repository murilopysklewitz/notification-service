package com.notification_service.infra.messaging;

import com.notification_service.application.SendTransactionCompletedEmailUseCase;
import com.notification_service.domain.TransactionCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class TransactionCompletedConsumer {

    private static final Logger log = LoggerFactory.getLogger(TransactionCompletedConsumer.class);
    private final SendTransactionCompletedEmailUseCase sendTransactionCompletedEmailUseCase;

    public TransactionCompletedConsumer(SendTransactionCompletedEmailUseCase sendTransactionCompletedEmailUseCase) {
        this.sendTransactionCompletedEmailUseCase = sendTransactionCompletedEmailUseCase;

    }

    @RabbitListener(queues = "transaction.queue")
    public void handle(TransactionCompletedEvent event){
        log.info("Event received: {}", event.getTransactionId());
        sendTransactionCompletedEmailUseCase.execute(event);

   }

}
