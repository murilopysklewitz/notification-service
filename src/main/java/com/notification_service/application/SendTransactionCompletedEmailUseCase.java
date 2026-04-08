package com.notification_service.application;

import com.notification_service.domain.TransactionCompletedEvent;
import org.springframework.stereotype.Service;

@Service
public class SendTransactionCompletedEmailUseCase {
    private final EmailSender emailSender;

    public SendTransactionCompletedEmailUseCase(EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    public void execute(TransactionCompletedEvent event){
        String sourceTo = event.getSourceEmail();
        String destinationTo = event.getDestinationEmail();
        String subject = "Transaction completed";
        String body = "Id Transaction:"
                + event.getTransactionId()
                + "\n" + "amount: "
                + event.getAmount()
                + "\n"
                + "Data:"
                + event.getCompletedAt()
                + "\n"
                + "Transaction type: "
                + event.getType();
        emailSender.send(sourceTo, subject, body);
        emailSender.send(destinationTo, subject, body);
    }
}
