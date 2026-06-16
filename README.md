# 🤖 CodeScan AI — AI-Powered Code Review Platform

> A full-stack final year project: Spring Boot + React + MySQL + OpenAI API

---

## 📋 Table of Contents
- [Project Overview](#project-overview)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Project Structure](#project-structure)
- [Setup Guide](#setup-guide)
- [API Reference](#api-reference)
- [Features](#features)

---

## Project Overview

CodeScan AI is an intelligent code review platform where users can:
- Upload/paste code in multiple languages
- Get AI-powered analysis (bugs, complexity, style, best practices)
- View a complexity score and overall code rating
- Track review history with a personal dashboard
- See detailed issue breakdowns with fix suggestions

---

## Tech Stack

| Layer     | Technology                        |
|-----------|-----------------------------------|
| Backend   | Java 17, Spring Boot 3.2, Maven   |
| Frontend  | React 18, React Router, Recharts  |
| Database  | MySQL 8.x                         |
| Auth      | JWT (JSON Web Tokens)             |
| AI Engine | OpenAI GPT-3.5-turbo API          |
| Styling   | Pure CSS with CSS Variables       |

---

## Prerequisites

Install these before starting:

| Tool       | Version   | Download                          |
|------------|-----------|-----------------------------------|
| Java (JDK) | 17+       | https://adoptium.net/             |
| Maven      | 3.8+      | https://maven.apache.org/         |
| Node.js    | 18+       | https://nodejs.org/               |
| MySQL      | 8.x       | https://dev.mysql.com/downloads/  |
| Git        | Any       | https://git-scm.com/              |

---

## Project Structure

```
ai-code-review/
├── backend/                        ← Spring Boot application
│   ├── pom.xml                     ← Maven dependencies
│   └── src/main/
│       ├── java/com/codereview/
│       │   ├── CodeReviewApplication.java   ← Main entry point
│       │   ├── config/
│       │   │   ├── SecurityConfig.java      ← JWT + CORS security
│       │   │   └── GlobalExceptionHandler.java
│       │   ├── controller/
│       │   │   ├── AuthController.java      ← /api/auth/*
│       │   │   └── CodeReviewController.java ← /api/reviews/*
│       │   ├── dto/
│       │   │   └── DTOs.java               ← Request/Response objects
│       │   ├── model/
│       │   │   ├── User.java               ← User entity
│       │   │   ├── CodeReview.java         ← Review entity
│       │   │   └── ReviewIssue.java        ← Issue entity
│       │   ├── repository/
│       │   │   ├── UserRepository.java
│       │   │   ├── CodeReviewRepository.java
│       │   │   └── ReviewIssueRepository.java
│       │   ├── security/
│       │   │   ├── JwtUtil.java            ← Token generation/validation
│       │   │   └── JwtAuthFilter.java      ← Request filter
│       │   └── service/
│       │       ├── AuthService.java        ← Register/Login logic
│       │       ├── CodeReviewService.java  ← Core review logic
│       │       ├── CustomUserDetailsService.java
│       │       └── OpenAIService.java      ← AI analysis + fallback
│       └── resources/
│           └── application.properties     ← Config (DB, JWT, OpenAI)
│
├── frontend/                       ← React application
│   ├── package.json
│   ├── public/
│   │   └── index.html
│   └── src/
│       ├── App.js                  ← Routes
│       ├── index.js                ← Entry point
│       ├── index.css               ← Global styles + CSS variables
│       ├── context/
│       │   └── AuthContext.js      ← Global auth state
│       ├── services/
│       │   └── api.js              ← Axios API calls
│       ├── components/layout/
│       │   ├── Layout.js           ← Sidebar + shell
│       │   └── Layout.css
│       └── pages/
│           ├── LoginPage.js        ← Login form
│           ├── RegisterPage.js     ← Register form
│           ├── AuthPages.css
│           ├── DashboardPage.js    ← Stats + charts
│           ├── DashboardPage.css
│           ├── NewReviewPage.js    ← Submit code form
│           ├── NewReviewPage.css
│           ├── ReviewDetailPage.js ← Full AI results
│           ├── ReviewDetailPage.css
│           ├── HistoryPage.js      ← All reviews table
│           └── HistoryPage.css
│
└── database/
    └── schema.sql                  ← MySQL schema + sample data
```

---

## Setup Guide

### Step 1 — Clone / Extract the project

```bash
cd Desktop
# If from ZIP:
unzip ai-code-review.zip
cd ai-code-review
```

---

### Step 2 — MySQL Database Setup

Open MySQL Workbench or run in terminal:

```bash
mysql -u root -p
```

Then paste or run the schema file:

```sql
source /path/to/ai-code-review/database/schema.sql;
```

Or in MySQL Workbench: File → Open SQL Script → select `database/schema.sql` → Run

---

### Step 3 — Configure the Backend

Open `backend/src/main/resources/application.properties` and update:

```properties
# Your MySQL password
spring.datasource.password=YOUR_MYSQL_PASSWORD_HERE

# Your OpenAI API key (get from https://platform.openai.com/api-keys)
# Leave as-is to use the built-in rule-based fallback (no API key needed for demo)
openai.api.key=YOUR_OPENAI_API_KEY_HERE

# JWT secret — change this in production!
app.jwt.secret=my_super_secret_key_change_this_in_production_abc123xyz
```

> **Note:** The app has a built-in rule-based code analyzer as fallback. Even without an OpenAI key, the platform will work and detect bugs, TODOs, style issues, and complexity automatically.

---

### Step 4 — Run the Backend

```bash
cd backend
mvn clean install -DskipTests
mvn spring-boot:run
```

Backend starts at: **http://localhost:8080**

You should see:
```
Started CodeReviewApplication in X.XXX seconds
```

---

### Step 5 — Run the Frontend

Open a **new terminal**:

```bash
cd frontend
npm install
npm start
```

Frontend starts at: **http://localhost:3000**

The browser will open automatically.

---

### Step 6 — Use the App

1. Go to http://localhost:3000
2. Click **Create one** to register
3. Or use demo account: `demo` / `password123`
4. Click **New Review**, paste code, select language, click **Analyze Code**
5. View detailed results: rating, bugs, complexity, suggestions

---

## API Reference

### Auth Endpoints

| Method | URL                   | Body                                      | Description    |
|--------|-----------------------|-------------------------------------------|----------------|
| POST   | /api/auth/register    | `{username, email, password, fullName}`   | Register user  |
| POST   | /api/auth/login       | `{username, password}`                    | Login user     |

### Review Endpoints (requires Bearer token)

| Method | URL                   | Body / Params                             | Description          |
|--------|-----------------------|-------------------------------------------|----------------------|
| POST   | /api/reviews          | `{title, language, code}`                 | Submit code review   |
| GET    | /api/reviews          | —                                         | Get all user reviews |
| GET    | /api/reviews/{id}     | —                                         | Get review details   |
| DELETE | /api/reviews/{id}     | —                                         | Delete a review      |
| GET    | /api/reviews/dashboard| —                                         | Get dashboard stats  |

---

## Features

### AI Analysis Includes:
- **Overall Rating** (1–10 with visual circle gauge)
- **Complexity Score** (1–100) and Level (LOW / MEDIUM / HIGH / VERY_HIGH)
- **Bug Detection** — NullPointerExceptions, division by zero, etc.
- **Code Smells** — TODOs, long methods, broad catches
- **Security Issues** — hardcoded passwords, SQL injection patterns
- **Performance** — inefficient loops, unnecessary operations
- **Style** — naming conventions, line length
- **Best Practices** — logging, error handling

### Dashboard:
- Total reviews, average rating, languages used, bugs found
- Bar chart of language breakdown (Recharts)
- Recent reviews quick-access list

### History:
- Searchable and filterable review table
- Language filter pills
- One-click delete

---

## Common Issues

**Port 8080 already in use:**
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Mac/Linux
lsof -ti:8080 | xargs kill
```

**MySQL connection refused:**
- Make sure MySQL service is running
- Check username/password in `application.properties`

**npm install fails:**
- Use Node.js 18+ (`node --version`)
- Delete `node_modules` and run `npm install` again

---

## Presented By

**Final Year Project — [Your College Name]**
**Student Name:** ___________________
**Roll Number:** ___________________
**Guide:** ___________________
**Batch:** 2024–25
