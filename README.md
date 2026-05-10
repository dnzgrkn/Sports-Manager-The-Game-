# Sports Manager — The Game

> A sport-agnostic management simulation game built with Java & JavaFX.
> Manage your team through a full league season, set tactics, play matches, and compete for the championship.

---

## 🎮 Supported Sports

| Sport | Periods | Squad | Season | Post-Season |
|---|---|---|---|---|
| Football | 2 Halves (45 min) | 11 starters | 38 weeks | — |
| Basketball | 4 Quarters (10 min) | 5 starters | 38 weeks | Top 8 Playoff |

---

## 👥 Team

| Name | Student ID |
|---|---|
| Deniz Gürkan | 20230602032 |
| Birsu Hacıkadiroğlu | 20220602039 |
| Oğuz Ünlüoğlu | 20240602077 |
| Pelin Çinici | 20240602023 |

CE 216 Fundamentals of Programming — Izmir University of Economics, Spring 2026

---

## 🏗️ Architecture

The application is built on a four-layer architecture:

```
com.sportsmanager.core        → Interfaces & abstract classes (Sport-agnostic)
com.sportsmanager.sports.*    → Sport-specific implementations (Football, Basketball)
com.sportsmanager.app         → Game orchestration & session management
com.sportsmanager.ui          → JavaFX screens & controllers
```

**Design Patterns Used:**
- **Template Method** — AbstractMatch.play() defines the fixed match lifecycle
- **Strategy** — StandingsComparator provides pluggable tiebreaker rules per sport
- **Observer** — MatchEventBus decouples match simulation from the UI
- **Singleton** — GameSession and SportRegistry manage shared application state
- **Open/Closed** — Adding a new sport requires zero changes to existing classes

---

## 🚀 Quick Start

### Prerequisites
- Java JDK/JRE 21 or later
- Apache Maven 3.9 or later

### Run from Source
```bash
mvn clean javafx:run
```

### Run Tests
```bash
mvn clean test
```
38 unit tests, 0 failures.

### Build Deployable JAR
```bash
mvn clean package -DskipTests
java -jar target/Sports-Manager-1.0-SNAPSHOT.jar
```

### Run with BAT (Windows)
```
Double-click run.bat in the setup/ folder
```

---

## 📁 Project Structure

```
Sports-Manager/
├── src/
│   ├── main/
│   │   ├── java/com/sportsmanager/
│   │   │   ├── core/           → Sport, Player, AbstractMatch, League, Team...
│   │   │   ├── sports/
│   │   │   │   ├── football/   → FootballSport, FootballMatch, FootballPlayer...
│   │   │   │   └── basketball/ → BasketballSport, BasketballMatch, BasketballPlayer...
│   │   │   ├── app/            → GameSession, LeagueOrchestrator, SaveLoadService...
│   │   │   └── ui/             → SceneNavigator, controllers/
│   │   └── resources/
│   │       ├── fxml/           → JavaFX layout files
│   │       └── css/            → Dark theme stylesheet
│   └── test/
│       └── java/com/sportsmanager/
│           ├── core/           → PlayerTest, TeamTest, LeagueTest
│           └── sports/         → FootballPlayerTest, BasketballMatchTest...
├── setup/                      → Deployable JAR + run.bat
├── docs/                       → Final report, user manual
├── pom.xml
└── run.bat
```

---

## 🎯 Key Features

- **Sport-agnostic framework** — new sports can be added with zero core changes
- **Tick-based match simulation** — possession, shots, goals, injuries per minute
- **Live match screen** — real-time event log, score, minute counter, possession bar
- **Formation diagram** — interactive tactic canvas updates on selection
- **Player attributes** — FIFA-style ratings with position-weighted formulas
- **Injury system** — players miss matches, recover week by week
- **Training** — improve squad attributes once per week
- **Save / Load** — Gson-serialized .smg save files
- **Basketball playoffs** — top 8 single-elimination bracket after regular season

---

## 🛠️ Tech Stack

| Technology | Version | Purpose |
|---|---|---|
| Java | 21 | Core language |
| JavaFX | 21 | GUI framework |
| Maven | 3.9 | Build tool |
| JUnit 5 | 5.10 | Unit testing |
| Gson | 2.10.1 | Save file serialization |
| maven-shade-plugin | 3.5.0 | Fat JAR packaging |

---

## 📄 Deliverables

- setup/ — Fat JAR + BAT launcher
- docs/FinalReport.pdf — Design changes, OO principles, post-mortem
- docs/UserManual.pdf — Complete user guide for both sports
- GitHub Release v1.0-m3 — Tagged final submission

---

## 📜 License

CE 216 Academic Project — Izmir University of Economics, Spring 2026
