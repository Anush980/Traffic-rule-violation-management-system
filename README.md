# Traffic Rule Violation Management System

A **5th Semester Mini Project** built with Spring Boot that manages traffic rules, records violations, and tracks fines. Features separate dashboards for Admin and User roles with clean Bootstrap 5 UI.

---

## Tech Stack

| Technology | Purpose |
|---|---|
| **Spring Boot 4.1.0** | Backend framework |
| **Spring Security** | Authentication & role-based access control |
| **Spring Data JPA** | Database operations (ORM) |
| **H2 Database** | In-memory database (no installation needed) |
| **Thymeleaf** | Server-side HTML template engine |
| **Bootstrap 5** | Responsive UI styling |
| **Maven** | Build tool & dependency management |
| **Java 21** | Programming language |

---

## Prerequisites

Before running this project, make sure you have:

- **Java 21** (JDK) installed
  - Check: `java -version` (should show 21.x.x)
  - Download: https://adoptium.net/
- **Maven** (optional - Maven Wrapper is included)
  - The project includes `mvnw` so you don't need to install Maven separately

---

## How to Run

### Step 1: Open Terminal and navigate to project folder
```bash
cd /path/to/demo
```

### Step 2: Run the application
```bash
# On Mac/Linux:
./mvnw spring-boot:run

# On Windows:
mvnw.cmd spring-boot:run
```

### Step 3: Open in browser
```
http://localhost:8080
```

The application starts on **port 8080**. You'll see the login page.

### Step 4: Stop the application
Press `Ctrl + C` in the terminal.

---

## Default Login Credentials

| Role | Email | Password |
|---|---|---|
| **Admin** | admin@admin.com | admin123 |
| **User** | Register a new account | (your password) |

---

## How to Test All Features

### A. Admin Features

#### 1. Login as Admin
- Go to `http://localhost:8080`
- Enter: `admin@admin.com` / `admin123`
- You'll be redirected to the **Admin Dashboard**

#### 2. Admin Dashboard (`/admin/dashboard`)
- View total violations, pending fines, paid fines
- See total fine amount vs collected amount
- View registered users count and vehicles count
- See 5 most recent violations

#### 3. Manage Traffic Rules (`/admin/rules`)
- **View** all pre-loaded traffic rules (8 rules seeded on startup)
- **Add** a new rule: Enter rule name, description, and fine amount
- **Delete** a rule: Click the red "Delete" button

#### 4. Add a Violation (`/admin/violations/add`)
- Select a **vehicle** from dropdown (users must register vehicles first)
- Select which **rule** was violated
- Enter **location** and **description**
- Click "Record Violation"
- Note: You need at least one registered user with a vehicle first!

#### 5. View All Violations (`/admin/violations`)
- See every violation in the system
- Click **"Mark Paid"** to change status from PENDING to PAID
- See vehicle number, owner name, rule, fine, location, date, status

---

### B. User Features

#### 1. Register a New User
- Click "Register here" on the login page
- Fill in: Full Name, Email, Phone, Password
- Click "Register"
- You'll be redirected to login with a success message

#### 2. Login as User
- Enter your registered email and password
- You'll be redirected to the **User Dashboard**

#### 3. Add a Vehicle (`/user/vehicles`)
- Enter **Registration Number** (e.g., KA-01-AB-1234)
- Select **Vehicle Type** (CAR, BIKE, TRUCK, AUTO, BUS)
- Enter **Model** name (e.g., Honda City)
- Click "Add"

#### 4. User Dashboard (`/user/dashboard`)
- See your **total vehicles**, **total violations**, **total fines**, **paid amount**
- Yellow warning shows if you have **pending fines**
- See all your violations in a table

#### 5. View My Violations (`/user/violations`)
- Summary cards show: Total Fines, Paid, Pending amounts
- Table shows each violation with details

---

### C. Complete Demo Flow

Here's the recommended order to test everything:

1. **Login as Admin** (admin@admin.com / admin123)
2. **Check Dashboard** - All stats should be 0
3. **View Traffic Rules** - 8 pre-loaded rules visible
4. **Logout**
5. **Register** a new user (e.g., john@example.com)
6. **Login as the new user**
7. **Add a vehicle** (e.g., KA-01-AB-1234, CAR, Honda City)
8. **Add another vehicle** if you want
9. **Logout**
10. **Login as Admin again**
11. **Add a Violation** - Select John's vehicle + a rule + location
12. **Check Admin Dashboard** - Stats should update
13. **Logout**
14. **Login as User (John)**
15. **Check User Dashboard** - Violation should appear with fine amount
16. **View My Violations** - See the violation details
17. **Login as Admin** and **Mark as Paid**
18. **Login as User** - Paid amount should now show

---

## H2 Database Console

You can directly view/query the database tables:

1. Go to: `http://localhost:8080/h2-console`
2. Enter these settings:
   - **JDBC URL:** `jdbc:h2:mem:trafficdb`
   - **Username:** `sa`
   - **Password:** *(leave empty)*
3. Click **Connect**
4. You'll see tables: `USERS`, `VEHICLES`, `TRAFFIC_RULES`, `VIOLATIONS`
5. Try: `SELECT * FROM USERS;`

> Note: You must be logged out or use a different browser for the H2 console, as it requires the security exception configured in the app.

---

## Project Structure

```
demo/
├── src/main/java/com/example/demo/
│   ├── DemoApplication.java              # Main entry point
│   ├── model/                             # Entity classes (database tables)
│   │   ├── User.java                      # User table (id, name, email, password, role)
│   │   ├── Vehicle.java                   # Vehicle table (id, regNo, type, model, userId)
│   │   ├── TrafficRule.java               # Traffic rules table (id, name, description, fine)
│   │   └── Violation.java                 # Violations table (id, vehicleId, ruleId, date, status)
│   ├── repository/                        # Database query interfaces
│   │   ├── UserRepository.java            # User database operations
│   │   ├── VehicleRepository.java         # Vehicle database operations
│   │   ├── TrafficRuleRepository.java     # Traffic rule database operations
│   │   └── ViolationRepository.java       # Violation database operations + custom queries
│   ├── service/                           # Business logic layer
│   │   ├── CustomUserDetailsService.java  # Connects User entity to Spring Security
│   │   ├── TrafficRuleService.java        # Traffic rule CRUD operations
│   │   ├── VehicleService.java            # Vehicle management operations
│   │   └── ViolationService.java          # Violation CRUD + dashboard statistics
│   ├── controller/                        # HTTP request handlers
│   │   ├── AuthController.java            # Login & Registration (public)
│   │   ├── AdminController.java           # Admin pages (ADMIN role only)
│   │   └── UserController.java            # User pages (USER role only)
│   └── config/                            # Configuration classes
│       ├── SecurityConfig.java            # Spring Security rules (who can access what)
│       └── DataInitializer.java           # Seeds admin account + sample rules on startup
├── src/main/resources/
│   ├── application.properties             # Database, server, Thymeleaf settings
│   └── templates/                         # Thymeleaf HTML templates
│       ├── login.html                     # Login page
│       ├── register.html                  # Registration page
│       ├── layout.html                    # Shared layout (navbar + sidebar)
│       ├── admin/
│       │   ├── dashboard.html             # Admin dashboard with analytics
│       │   ├── rules.html                 # Traffic rules CRUD page
│       │   ├── violations.html            # All violations list
│       │   └── add-violation.html         # Record new violation form
│       └── user/
│           ├── dashboard.html             # User dashboard with stats
│           ├── vehicles.html              # My vehicles + add vehicle form
│           └── violations.html            # My violations list
└── pom.xml                                # Maven dependencies
```

---

## Architecture Diagram

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│   Browser    │────>│  Controller  │────>│   Service    │────>│  Repository  │
│  (HTML/CSS)  │<────│  (Handles    │<────│  (Business   │<────│  (Database   │
│              │     │   HTTP)      │     │   Logic)     │     │   Queries)   │
└──────────────┘     └──────────────┘     └──────────────┘     └──────┬───────┘
                            │                                         │
                            │                                         │
                     ┌──────▼───────┐                          ┌──────▼───────┐
                     │  Thymeleaf   │                          │  H2 Database │
                     │  Templates   │                          │  (In-Memory) │
                     │  (HTML)      │                          │              │
                     └──────────────┘                          └──────────────┘
```

---

## Database ER Diagram

```
    ┌─────────────┐        ┌─────────────┐        ┌──────────────┐
    │    USERS     │        │   VEHICLES   │        │  VIOLATIONS  │
    ├─────────────┤        ├─────────────┤        ├──────────────┤
    │ id (PK)     │◄──┐    │ id (PK)     │◄──┐    │ id (PK)      │
    │ fullName    │   │    │ regNumber   │   │    │ vehicle_id(FK)│──►VEHICLES
    │ email       │   └────│ user_id(FK) │   └────│ rule_id (FK) │──►TRAFFIC_RULES
    │ password    │        │ vehicleType │        │ violationDate│
    │ phone       │        │ model       │        │ location     │
    │ role        │        └─────────────┘        │ description  │
    │ createdAt   │                               │ status       │
    └─────────────┘        ┌──────────────┐       └──────────────┘
                           │ TRAFFIC_RULES │
                           ├──────────────┤
                           │ id (PK)      │
                           │ ruleName     │
                           │ description  │
                           │ fineAmount   │
                           └──────────────┘
```

---

## Important Notes

- **Data resets on restart** because H2 is in-memory. Admin account and sample rules are re-created automatically by `DataInitializer.java`.
- **Passwords are hashed** using BCrypt. The stored password is never the plain text.
- All files have **detailed comments** explaining every annotation, method, and concept for beginners.
- To switch to **MySQL/PostgreSQL**, change the datasource settings in `application.properties` and add the appropriate driver dependency in `pom.xml`.

---

## Troubleshooting

| Problem | Solution |
|---|---|
| Port 8080 already in use | Change `server.port=8081` in application.properties |
| `./mvnw` permission denied | Run `chmod +x mvnw` first |
| Java version error | Ensure Java 21 is installed: `java -version` |
| Login not working | Use exact credentials: `admin@admin.com` / `admin123` |
| No vehicles in "Add Violation" | Register a user and add a vehicle first |
