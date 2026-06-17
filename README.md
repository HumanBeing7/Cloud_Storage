# Cloud Storage REST API ☁️

A robust, secure backend REST API for a cloud file storage platform. Built with Spring Boot, this application currently features a production-grade authentication layer using JSON Web Tokens (JWT), secure cryptographic password hashing, and PostgreSQL persistence.

## 🚀 Current Features (Phases 1-3 Complete)

* **User Registration:** Secure user onboarding with BCrypt password hashing.
* **Stateless Authentication:** Custom Spring Security implementation without relying on default auto-configurations.
* **JWT Issuance:** Cryptographically signed 256-bit Base64 JSON Web Tokens generated upon successful login.
* **Secure Secrets Management:** Master keys and mathematical variables are isolated from the codebase using environment variables (`.env`).
* **Relational Persistence:** Robust data storage using Spring Data JPA and PostgreSQL.

## 🛠️ Tech Stack

* **Language:** Java 17+
* **Framework:** Spring Boot 3.x
* **Security:** Spring Security, BCrypt
* **Tokens:** JJWT (Java JWT)
* **Database:** PostgreSQL, Spring Data JPA
* **Configuration:** `spring-dotenv`
* **Build Tool:** Maven

---

## ⚙️ Getting Started

### Prerequisites
* Java Development Kit (JDK) 17 or higher
* PostgreSQL installed and running
* Maven

### 1. Database Setup
Create a new PostgreSQL database for the application. Update your `application.properties` with your specific database URL, username, and password.

### 2. Environment Variables (Crucial)
This application uses a `.env` file to manage cryptographic secrets. **Do not commit this file to version control.**

Create a `.env` file in the root directory (next to `pom.xml`) and add the following variables:

```env
# Must be a valid, 256-bit Base64 encoded string without spaces or brackets
JWT_SECRET=YourSuperSecretBase64EncodedKeyHere
# Expiration time in milliseconds (e.g., 86400000 for 24 hours)
JWT_EXPIRATION_TIME=86400000
