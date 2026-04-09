# 📧 Notification Service

A lightweight Spring Boot microservice that listens to banking transaction events via RabbitMQ and sends email notifications to both parties involved in a completed transaction.

---

## 📐 Architecture

```
src/main/java/com/notification_service/
├── application/
│   ├── EmailSender.java                        # Port (interface)
│   ├── SmtpEmailService.java                   # SMTP implementation of EmailSender
│   └── SendTransactionCompletedEmailUseCase.java
├── domain/
│   └── TransactionCompletedEvent.java          # Event domain model
└── infra/
    └── messaging/
        ├── RabbitMQConfiguration.java          # Queue/converter bean setup
        └── TransactionCompletedConsumer.java   # RabbitMQ listener
```

The service follows a clean, minimal architecture: the domain defines the event model, the application layer owns the use case and the email port, and the infrastructure wires RabbitMQ and SMTP together.

---

## 🚀 How It Works

```
[SecureBankingApi]
      │
      │  Publishes TransactionCompletedEvent
      ▼
[RabbitMQ]  exchange: transaction.exchange
            routing key: transaction.completed
            queue: transaction.queue
      │
      ▼
[Notification Service — TransactionCompletedConsumer]
      │
      ▼
[SendTransactionCompletedEmailUseCase]
      │
      ├── emailSender.send(sourceEmail, subject, body)
      └── emailSender.send(destinationEmail, subject, body)
```

On every consumed event, two emails are sent: one to the **source account holder** and one to the **destination account holder**.

---

## 📨 Email Content

Subject: `Transaction completed`

Body:
```
Id Transaction: <transactionId>
amount: <amount>
Data: <completedAt>
Transaction type: <type>
```

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot |
| Messaging | RabbitMQ (Spring AMQP) |
| Email | Spring Mail (JavaMailSender / SMTP) |
| Build | Maven |
| Containerization | Docker / Docker Compose |

---

## ⚙️ Configuration

### `application.yml`

```yaml
server:
  port: 8081

spring:
  rabbitmq:
    host: rabbitmq
    port: 5672
    username: ${RABBITMQ_USERNAME}
    password: ${RABBITMQ_PASSWORD}
  jackson:
    deserialization:
      fail-on-unknown-properties: false
```

You must also configure SMTP via environment variables or an additional profile. Example for Gmail:

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
    properties:
      mail.smtp.auth: true
      mail.smtp.starttls.enable: true
```

### Required environment variables

```env
RABBITMQ_USERNAME=admin
RABBITMQ_PASSWORD=admin

MAIL_USERNAME=your@email.com
MAIL_PASSWORD=your_app_password
```

---

## 🐳 Running with Docker Compose

The service is included in the shared `docker-compose.yml` alongside RabbitMQ:

```yaml
services:
  rabbitmq:
    image: rabbitmq:3-management
    ports:
      - "5672:5672"
      - "15672:15672"
    environment:
      RABBITMQ_DEFAULT_USER: admin
      RABBITMQ_DEFAULT_PASS: admin

  notification-service:
    build: ./
    ports:
      - "3001:8081"
    depends_on:
      - rabbitmq
```

```bash
docker-compose up --build
```

The service will be available on port `3001` and will start listening to the `transaction.queue` automatically.

---

## 🔌 RabbitMQ Contract

| Property | Value |
|---|---|
| Queue | `transaction.queue` |
| Message format | JSON (deserialized via `JacksonJsonMessageConverter`) |
| Listener annotation | `@RabbitListener(queues = "transaction.queue")` |
| Payload class | `TransactionCompletedEvent` |

### `TransactionCompletedEvent` fields

| Field | Type | Description |
|---|---|---|
| `sourceEmail` | `String` | Email of the sender |
| `destinationEmail` | `String` | Email of the receiver |
| `transactionId` | `UUID` | Unique transaction ID |
| `sourceUserId` | `UUID` | Sender's user ID |
| `destinationUserId` | `UUID` | Receiver's user ID |
| `amount` | `BigDecimal` | Transaction amount |
| `type` | `String` | `TRANSFER`, `DEPOSIT`, or `WITHDRAWAL` |
| `completedAt` | `LocalDateTime` | Completion timestamp |

Unknown JSON fields are ignored (`fail-on-unknown-properties: false`), making the consumer resilient to future additions in the event payload.

---

## 🔁 Relationship with SecureBankingApi

This service is a consumer of events produced by **SecureBankingApi**. Both services communicate exclusively through RabbitMQ — there is no direct HTTP dependency between them.

```
SecureBankingApi  ──(RabbitMQ)──▶  notification-service
```

They share the same Docker network (`secure_api_bank`) and connect to the same RabbitMQ instance.