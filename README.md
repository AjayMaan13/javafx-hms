# 🍁 Maple Leaf Hotel — Hotel Reservation System

A desktop **Hotel Reservation System** built with JavaFX and a real Hibernate/JPA database.
It has two sides that live in **one window**: a self-serve **guest kiosk** for booking rooms,
and a staff **admin back-office** for managing reservations, billing, loyalty, and reporting.

> **APD545 — Final Milestone (M3) · Group project.**
> Built collaboratively as a team.

---

## ✨ Features

**Guest kiosk**
- Step-by-step booking: occupancy → dates → room type → add-ons → guest details → estimate → confirm
- Live price estimate (room rate × nights, weekend pricing, tax, and itemised add-ons)
- Instant on-screen confirmation, saved as a real database record
- Loyalty enrolment during booking
- Guest feedback with star ratings

**Admin back-office**
- Secure staff login with **BCrypt**-hashed passwords and **role-based** permissions (Admin / Manager)
- Reservation dashboard with real search and filters
- Create / edit / cancel reservations, with double-booking checks
- Billing, payments and refunds (one signed amount: `+` payment, `−` refund)
- Checkout that is blocked until the bill is settled, and **frees the room** when done
- Role-capped discounts (Admin ≤ 15%, Manager ≤ 30%) — over-cap requests are rejected
- Loyalty accounts: earn on payment, redeem for money off (with a safe cap)
- Waitlist that is **automatically notified** when a matching room frees up
- Feedback viewer with sentiment and average rating
- Reports (Revenue, Occupancy, Activity Log) exported to **CSV / TXT / PDF**
- Rotating log file + a database **audit trail** of admin actions

---

## 🛠️ Technologies

| Area | Technology |
|---|---|
| Language | Java 17 |
| User interface | JavaFX + FXML + CSS |
| Persistence (ORM) | Hibernate / Jakarta Persistence (JPA) |
| Database | H2 (file-based) |
| Password hashing | jBCrypt |
| PDF export | OpenPDF |
| Build tool | Maven (with wrapper) |
| Testing | JUnit 5 — **62 passing tests** |

---

## 🧱 Architecture

A clean **3-tier / MVC** design — each layer only talks to the one next to it:

```
Controllers  →  Services  →  Repositories  →  EntityManager  →  H2
 (JavaFX)       (rules)       (data access)      (Hibernate)
```

- **MVC** — FXML views, controller classes, and JPA entity models.
- **Dependency Injection** — a single composition root, `AppConfig`, builds every service and
  repository once and hands them out. Nothing constructs its own dependencies.
- **ORM** — Hibernate/JPA maps Java objects to database rows (no hand-written SQL).
- **Single window** — a `SceneRouter` owns one Stage and swaps between the kiosk and admin views
  ("Staff Login" to switch, "Back to Kiosk" to return).

### Design patterns (GoF)

| Pattern | Where |
|---|---|
| **Singleton** | `PersistenceManager` (one `EntityManagerFactory`), `LoggerService` |
| **Factory** | `RoomFactory` — builds rooms by type |
| **Strategy** | `PricingStrategy` → `StandardPricingStrategy` / `WeekendPricingStrategy` |
| **Decorator** | `WifiDecorator`, `BreakfastDecorator`, `ParkingDecorator`, `SpaDecorator` |
| **Observer** | `RoomAvailabilityPublisher` → `WaitlistSubscriber` |

---

## 📂 Project structure

```
src/main/java/com/hotel/
├── app/          Main entry point, AppConfig (DI), SceneRouter
├── config/       PricingPolicy, DiscountPolicy
├── controller/   kiosk + admin JavaFX controllers
├── events/       Observer pattern (publisher / subscriber)
├── model/        JPA entities + enums
├── repository/   Data-access layer (BaseRepository + per-entity)
├── security/     AuthService, BCryptPasswordHasher
├── service/      Business logic (pricing, billing, loyalty, reporting…)
└── util/         PersistenceManager, LoggerService, exporters, DataSeeder
src/main/resources/fxml/   kiosk + admin FXML views, app.css
src/test/java/com/hotel/   JUnit 5 tests
docs/                      Project documentation, screenshots, video script
```

---

## ▶️ Running the app

Requires **JDK 17+**. From the project root:

```bash
./mvnw clean javafx:run
```

The app opens on the guest kiosk. To reach the admin side, click **Staff Login** in the header.

**Seeded staff logins**

| Username | Password | Role |
|---|---|---|
| `admin` | `admin123` | Admin |
| `manager` | `manager123` | Manager |

The database, logs, and exports are created on first run (`data/`, `logs/`, `exports/`).
Delete those folders any time for a clean slate.

## 🧪 Tests

```bash
./mvnw clean test
```

---

*Maple Leaf Hotel · JavaFX · Hibernate/JPA · H2 — a group project for APD545.*
