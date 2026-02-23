
This includes:
	•	Security
	•	Idempotency
	•	Seat locking
	•	Virtual queue
	•	Discount strategy
	•	Refund flow
	•	Payment integration
	•	Ticket service integration
	•	Logging
	•	Role-based access
	•	Cancellation
	•	Delete order
	•	CQRS awareness
	•	Scalability considerations

Booking Service

Overview

The Booking Service is the core transactional service of the Movie Ticket Platform.

It is responsible for:
	•	Seat reservation
	•	Booking lifecycle management
	•	Payment integration
	•	Refund handling
	•	Virtual queue control
	•	Discount application
	•	Idempotent request handling
	•	Secure role-based access

This service ensures transactional integrity and consistency in a distributed microservice architecture.



Responsibilities
	•	Create booking
	•	Validate show availability
	•	Lock seats
	•	Apply discount strategies
	•	Process payment
	•	Confirm booking
	•	Handle payment failure
	•	Cancel booking
	•	Process refund
	•	Release seats
	•	Enforce idempotency
	•	Maintain booking lifecycle state



Technology Stack
	•	Spring Boot
	•	Spring Security
	•	JPA / Hibernate
	•	MySQL
	•	JWT Authentication
	•	Virtual Queue (Semaphore-based)
	•	Strategy Pattern (Discount)
	•	Structured Logging
	•	REST API



Architecture Design

Microservice Pattern
	•	Stateless
	•	Isolated business logic
	•	Integrates with:
	•	Identity Service (JWT validation)
	•	Theatre Service (show validation)
	•	Payment Service (charge/refund)



Booking Lifecycle

Booking Status Flow:

PENDING → CONFIRMED → REFUNDED
          ↓
         FAILED

Flow Description
	1.	Booking request received
	2.	Idempotency check performed
	3.	Virtual queue entry validated
	4.	Show validated via Theatre Service
	5.	Seats locked
	6.	Discount applied
	7.	Payment processed
	8.	Status updated:
	•	CHARGED → CONFIRMED
	•	FAILED → FAILED
	9.	Seats released if payment fails



Security Model
	•	JWT-based authentication
	•	Role-based authorization
	•	CUSTOMER role required for booking
	•	ADMIN allowed for query and monitoring
	•	Stateless session management
	•	Method-level security enforcement



Idempotency

The service prevents duplicate bookings using:
	•	Idempotency key
	•	Unique request tracking
	•	Booking lookup before processing

If the same request is sent twice:
	•	Original result is returned
	•	No duplicate booking created
	•	No duplicate payment charged

This ensures safe retries in distributed systems.



Seat Locking Strategy

Seat locking is implemented to prevent double booking.

Mechanism:
	•	SeatLock entity per show
	•	Atomic lock check
	•	Lock release on failure or cancellation
	•	Soft locking before payment confirmation

Optimized for:
	•	Concurrent user traffic
	•	Data consistency
	•	Preventing race conditions

Future plan:
	•	Replace DB locking with Redis distributed lock for high scale



Virtual Booking Queue

Implemented using:
	•	Semaphore-based concurrency control
	•	Maximum concurrent booking limit
	•	Request rejection if capacity exceeded

Purpose:
	•	Prevent system overload
	•	Avoid database contention
	•	Improve stability during peak traffic

Future enhancement:
	•	Distributed queue using Redis or Kafka



Discount Strategy

Implemented using Strategy Pattern.

Current strategies:
	•	Third ticket discount
	•	Time-based discount (e.g., afternoon shows)

Execution flow:
	1.	Base price calculated
	2.	Discount strategies applied sequentially
	3.	Final amount computed

This allows:
	•	Easy addition of new discount rules
	•	Clean separation of pricing logic



Payment Integration

Booking Service integrates with Payment Service.

Charge Flow:
	•	Send bookingId, userId, amount
	•	Receive:
	•	Status
	•	Transaction ID

If CHARGED:
	•	Booking → CONFIRMED

If FAILED:
	•	Booking → FAILED
	•	Seats released



Refund Flow

Refund occurs during:
	•	Booking cancellation
	•	Confirmation failure recovery

Steps:
	1.	Refund request sent to Payment Service
	2.	Transaction status updated
	3.	Booking status → REFUNDED
	4.	Seats released

Refund operation is idempotent.



Ticket / Theatre Service Integration

Before booking:
	•	Show retrieved using showId
	•	Show validated:
	•	Active
	•	Not expired
	•	Available

Booking depends on show validity.



Delete / Cancel Order

Cancel Booking
	•	Allowed only if status = CONFIRMED
	•	Payment refund triggered
	•	Seats released
	•	Status updated to REFUNDED

Delete Order

Soft delete approach:
	•	Status updated
	•	Data retained for audit
	•	No physical deletion



Structured Logging

The service uses structured logging for observability.

Logs include:
	•	Booking ID
	•	User ID
	•	Payment status
	•	Seat details
	•	Transaction ID

Example:

BOOKING_CREATED bookingId=5
PAYMENT_RESPONSE bookingId=5 status=CHARGED txId=12
BOOKING_CONFIRMED bookingId=5

Benefits:
	•	Easier debugging
	•	Production monitoring
	•	Log aggregation compatibility
	•	Traceability across services

Future plan:
	•	Add correlation ID
	•	Distributed tracing integration



Database Design

Tables:
	•	booking
	•	seat_lock

Key principles:
	•	Strong consistency for write operations
	•	Transactional integrity
	•	Soft delete instead of hard delete

Future scaling plan:
	•	Read replicas
	•	Partitioning by showId
	•	Redis-based locking



CAP Theorem Consideration

In distributed deployment:
	•	Prioritize Consistency and Partition Tolerance
	•	Strong consistency for seat locking
	•	Eventual consistency acceptable for read queries

Tradeoff chosen for transactional correctness.



Scalability Strategy
	•	Horizontal scaling via Kubernetes replicas
	•	CPU-based HPA
	•	Virtual queue to control concurrency
	•	Future distributed locking via Redis



Deployment

Docker

docker build -t booking-service .
docker run -p 8083:8080 booking-service




Kubernetes
	•	Deployment with multiple replicas
	•	ClusterIP internal communication
	•	HPA enabled
	•	Integrated via API Gateway



Failure Handling
	•	Payment failure → booking FAILED
	•	Confirmation failure → automatic refund
	•	Seat release on failure
	•	Idempotent retry safety



Future Improvements
	•	Redis distributed locking
	•	Kafka-based event-driven architecture
	•	Saga pattern for distributed transactions
	•	Centralized logging (ELK)
	•	Prometheus monitoring
	•	Circuit breaker implementation
	•	Dead-letter queue for failed payments



Architectural Role

The Booking Service is the core transactional engine of the system.

It guarantees:
	•	Seat consistency
	•	Payment integrity
	•	Controlled concurrency
	•	Secure access
	•	Scalable deployment

It coordinates between Theatre Service and Payment Service to complete the booking workflow reliably.

