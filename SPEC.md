# Merchant API Technical Specification

## Overview

A RESTful API service providing external integration points for merchants to initiate payments, manage transactions, and retrieve status. Built with OpenAPI specification for auto-generated documentation and client SDKs.

## Key Features

- Endpoints for payment creation, status queries, refunds, and webhooks.
- Support for multiple payment methods (cards, wallets) via proxy routing.
- Rate limiting, authentication (API keys/JWT), and sandbox mode.
- OpenAPI 3.0 spec auto-generated on build.
- Event-driven webhooks for transaction updates.

## Tech Stack

- Language: Java
- Framework: Spring Boot
- Database: PostgreSQL
- API: OpenAPI/Swagger integration.
- Security: TLS, JWT auth, input validation.
- Deployment: Dockerized microservice in EKS with Linkerd mTLS.

## Requirements

- PCI-compliant (no card data storage; tokenization).
- High availability with canary deployments.
- Prometheus metrics and structured logging.
