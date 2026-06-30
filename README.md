# Dots & Boxes

A full-stack implementation of the classic **Dots & Boxes** game with a browser-based interface, REST API, and an AI opponent powered by Minimax with alpha-beta pruning.

Players take turns drawing lines between adjacent dots. Completing a box awards a point and grants an extra move. The player with the most completed boxes wins.

<img width="619" height="1032" alt="image" src="https://github.com/user-attachments/assets/90ffcd7e-9c73-419c-838d-681d74848a32" />


## Project Context

This project was originally developed as a **Computer Practicum** assignment.

Its purpose was to combine full-stack web development with adversarial-search algorithms in one practical application: a Java Spring Boot backend, a JavaScript Canvas frontend, Docker-based local environment, and an AI opponent based on Minimax.

## Features

* Play against an AI opponent.
* Interactive board rendered with HTML Canvas.
* Configurable board size from `2×2` to `6×6`.
* Three AI difficulty levels:

  * Easy — Minimax depth 2;
  * Medium — Minimax depth 3;
  * Hard — Minimax depth 4.
* Automatic detection and scoring of completed boxes.
* Extra turn after completing one or more boxes.
* Game restart and board resizing.
* Board-size preference stored in browser `localStorage`.
* AI calculation status while the move is being processed.
* AI diagnostics panel showing:

  * selected search depth;
  * evaluated root moves;
  * selected move;
  * calculation time;
  * visited nodes;
  * alpha-beta pruning cutoffs.
* REST API for game state, player moves, AI moves, restarting, and board resizing.
* Docker Compose setup for launching frontend and backend together.

## Tech Stack

### Backend

* Java 17
* Spring Boot
* Maven
* REST API
* Minimax
* Alpha-beta pruning

### Frontend

* HTML
* CSS
* Vanilla JavaScript
* HTML Canvas
* LocalStorage

### Infrastructure

* Docker
* Docker Compose
* Node.js 20
* Maven with Eclipse Temurin 17

## Run with Docker

### Prerequisites

* Git
* Docker Desktop
* Docker Compose

### Start the Application

```bash
git clone https://github.com/koliruslik/dotsAndBoxes.git
cd dotsAndBoxes
docker compose up --build
```

Open the frontend:

```text
http://localhost:3000
```

The backend API runs at:

```text
http://localhost:8080
```

To stop the application:

```bash
docker compose down
```

## Run Without Docker

### Prerequisites

* Java 17
* Node.js and npm

### Backend

Open a terminal in the project root:

```bash
cd backend
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

The backend starts at:

```text
http://localhost:8080
```

### Frontend

Open a second terminal:

```bash
cd frontend
npx serve -s . -l 3000
```

On Windows PowerShell, script execution policies may block `npx`. Use:

```powershell
npx.cmd serve -s . -l 3000
```

Alternatively, temporarily allow scripts only for the current PowerShell session:

```powershell
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
npx serve -s . -l 3000
```

Open:

```text
http://localhost:3000
```

## Game Rules

1. Players take turns drawing one line between two neighboring dots.
2. When a player completes the fourth side of a box, they claim that box.
3. Completing a box grants another turn.
4. When every box is claimed, the player with the highest score wins.
5. The AI controls Player 2.

## API Endpoints

| Method | Endpoint     | Description                                  |
| ------ | ------------ | -------------------------------------------- |
| `GET`  | `/get-state` | Returns the current game state               |
| `POST` | `/make-move` | Applies a human player move                  |
| `POST` | `/ai-move`   | Calculates and applies the AI move           |
| `POST` | `/restart`   | Clears the board and starts a new game       |
| `POST` | `/set-size`  | Changes board dimensions and resets the game |

## AI Approach

The AI uses **Minimax with alpha-beta pruning**.

For every available move, the AI simulates the resulting game state and evaluates future positions up to the selected depth. Alpha-beta pruning reduces the number of branches that need to be explored.

The evaluation function considers:

* the difference between AI and opponent scores;
* moves that may create a dangerous third side of a box;
* tactical opportunities to complete boxes;
* the current number of claimed squares.

The frontend displays search diagnostics after every AI turn, making the decision process visible during gameplay.

## Project Structure

```text
dotsAndBoxes/
├── backend/
│   ├── src/main/java/
│   │   └── com/ruslan/backend/
│   │       ├── aiPlayer/
│   │       │   ├── Evaluator.java
│   │       │   ├── MiniMax.java
│   │       │   └── MoveGenerator.java
│   │       ├── controllers/
│   │       │   ├── GameController.java
│   │       │   └── GameState.java
│   │       ├── BackendApplication.java
│   │       └── WebConfig.java
│   ├── Dockerfile
│   └── pom.xml
├── frontend/
│   ├── css/
│   ├── js/
│   │   └── app.js
│   ├── index.html
│   └── Dockerfile
└── docker-compose.yml
```

## Current Limitations and Improvement Directions

This is an educational project, not a production-ready multiplayer service. The following areas are known opportunities for improvement.

### Application Structure

`GameController` currently handles HTTP endpoints, keeps the game state, and coordinates core game flow. A cleaner architecture would separate these responsibilities into dedicated layers:

* controller layer for HTTP communication;
* service layer for game use cases;
* domain layer for rules and state transitions;
* repository layer for game persistence.

### Shared In-Memory State

The backend currently stores one game state in memory. This means all browser clients connected to the same backend instance share the same board.

A production-ready version should introduce:

* separate game sessions;
* unique game identifiers;
* persistent storage;
* player-specific state and authentication.

### Server-Side Move Validation

The frontend prevents most invalid clicks, but the backend should not rely on the client for rule enforcement.

The `/make-move` endpoint should validate:

* whether it is the player’s turn;
* whether coordinates are within board bounds;
* whether a line connects adjacent dots;
* whether a line already exists;
* whether the game has already ended.

### Client–Server Board Synchronization

The frontend stores board dimensions in `localStorage`, while a newly started backend initializes a default board state.

After restarting the backend, the frontend and backend can temporarily use different board sizes until they are explicitly synchronized. A better initialization flow would request or set the board size before gameplay begins.

### AI Evaluation Improvements

The AI already uses Minimax and alpha-beta pruning, but its evaluation function can be improved further.

Potential improvements:

* make all heuristics explicitly player-aware;
* improve move ordering before search;
* introduce iterative deepening;
* add a time limit for AI calculations;
* cache evaluated positions;
* profile search performance on different board sizes.

### Request Handling and AI Responsiveness

AI calculation currently runs as part of the API request. At higher board sizes and search depths, this can delay the backend response.

A more scalable version could use:

* background tasks;
* cancellation support;
* time budgets;
* asynchronous job handling;
* WebSocket updates for AI progress.

### Tests and Continuous Integration

The project would benefit from automated coverage for:

* line validation;
* box-completion rules;
* extra-turn logic;
* winner calculation;
* board resizing;
* AI move generation;
* evaluation heuristics.

A CI workflow could automatically build the frontend and backend, run tests, and validate Docker configuration on every pull request.

### Deployment Configuration

The frontend API URL and backend CORS configuration are currently intended for local development.

For deployment, these should be configured through environment variables rather than fixed `localhost` values.

## Educational Goal

This project was created to practice:

* Java and Spring Boot development;
* REST API design;
* HTML Canvas rendering;
* JavaScript frontend state management;
* Docker-based development environments;
* Minimax and alpha-beta pruning;
* structuring a small full-stack application.

It represents an intermediate learning project and a foundation for future improvements in backend architecture, automated testing, AI design, and deployment practices.
