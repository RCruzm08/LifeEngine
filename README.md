# LifeEngine

LifeEngine is an interactive evolutionary ecosystem simulator built with a Spring Boot backend and a standalone HTML5 canvas frontend. The project models creatures, food, biomes, climate pressure, mutation, population dynamics and visible ecological events on a living map.

The current interface is designed to be watched as a real-time ecosystem: terrain is rendered with visible biomes, creatures are drawn as organic forms, weather systems move across the map and ecological interventions can be triggered from the control panel.

## Highlights

- Real-time ecosystem simulation with herbivores, predators, omnivores, insects and emergent proto-humans.
- Visible biome map with forest, plains, desert, water, wetland, tundra and highland regions.
- Organic canvas rendering with textured terrain, creature variants, movement trails and heatmap overlays.
- Climate systems including rain, heat waves, cold fronts and visible hurricanes.
- Ecological events such as bloom, oasis creation and wildfire propagation.
- Day and night lighting cycle with atmospheric overlays, clouds, sun/moon and vignette.
- Ecosystem dashboard with health, biomass, dominant biome, active light phase and biome distribution.
- REST endpoints and WebSocket broadcasting for server-driven simulation state.

## Tech Stack

- Java 21
- Spring Boot 3.2
- Spring Web and WebSocket
- HTML5 Canvas
- Vanilla JavaScript
- Maven

## Running the Project

### Spring Boot App

From the project directory:

```powershell
cd LifeEngine
.\run.ps1
```

Then open:

```text
http://localhost:8080
```

### Manual Build

```bash
mvn clean package
java -jar target/life-engine-2.0.0.jar
```

### Standalone Frontend Preview

The frontend is self-contained and can run without the backend. Open:

```text
src/main/resources/static/index.html
```

If you are using a browser that blocks local file access, serve the folder with any static server and open the page through `localhost`.

## Controls

The right-side control panel allows direct intervention in the ecosystem:

- Start/Pause and Reset control the simulation loop.
- Extinction removes predator pressure from the world.
- Drought, Glacial and Heat change environmental pressure.
- Hurricane creates a visible storm system on the map.
- Rain refills water and cools the environment.
- Bloom injects a plant growth event.
- Wildfire creates a spreading fire that damages plants and fauna.
- Oasis adds new water and reshapes the surrounding biome.
- Heatmap highlights high-traffic creature paths.
- Biomes toggles biome labels and visual reference.
- Trails toggles creature movement trails.
- Save exports a JSON snapshot of the current simulation.

## Simulation Model

LifeEngine models each world tick as a combination of:

- plant growth and seeding;
- creature aging, energy use and hydration;
- predator/prey behavior;
- water seeking and drinking;
- climate and biome stress;
- mutation and reproduction;
- ecological events such as fire, bloom and storms.

Creatures carry evolvable traits:

| Trait | Purpose |
| --- | --- |
| `speed` | Movement range and energy cost |
| `perception` | Search radius for food, water and threats |
| `aggression` | Hunting and omnivore pressure |
| `mutationRate` | Chance of genetic variation during reproduction |
| `heatTolerance` | Resistance to high-temperature environments |
| `coldTolerance` | Resistance to cold environments |
| `intelligence` | Enables more advanced behavior and proto-human emergence |

## Backend Architecture

```text
src/main/java/com/lifeengine/
├── engine/
│   └── SimulationEngine.java
├── model/
│   ├── Biome.java
│   ├── Creature.java
│   ├── CreatureType.java
│   └── World.java
└── websocket/
    ├── SimulationController.java
    ├── SimulationWebSocketHandler.java
    └── WebSocketConfig.java
```

### Main Responsibilities

- `World` owns the simulation state: biome grid, creatures, food, heatmap and ecological actions.
- `Creature` contains movement, reproduction, mutation and survival behavior.
- `Biome` defines environmental modifiers such as speed, food availability and heat stress.
- `SimulationEngine` schedules ticks, computes statistics and broadcasts state.
- `SimulationController` exposes REST controls.
- `SimulationWebSocketHandler` broadcasts JSON state to connected clients.

## REST API

Base path:

```text
/api/sim
```

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/start` | Start simulation |
| `POST` | `/pause` | Pause simulation |
| `POST` | `/reset` | Reset and restart simulation |
| `POST` | `/crisis` | Trigger predator extinction event |
| `POST` | `/bloom` | Trigger plant bloom event |
| `POST` | `/oasis` | Add a new oasis to the biome map |
| `POST` | `/wildfire` | Trigger a wildfire event |
| `POST` | `/speed` | Update tick delay |
| `POST` | `/params` | Update food, reproduction and mutation parameters |
| `GET` | `/snapshot` | Export current simulation snapshot |
| `GET` | `/status` | Return running status and current cycle |

WebSocket stream:

```text
/ws/simulation
```

The WebSocket payload includes population counts, averages, creatures, food coordinates, biome map, biome summary, heatmap and event log entries.

## Frontend Structure

The primary UI lives in:

```text
src/main/resources/static/index.html
```

It includes:

- the simulation canvas;
- the standalone JavaScript simulation;
- chart rendering;
- control panel interactions;
- terrain, weather, light and creature drawing routines.

This file is intentionally self-contained so the experience can run both through Spring Boot and as a standalone browser preview.

## Development

Recommended verification before committing:

```bash
mvn -q -DskipTests package
```

For frontend-only changes, also open the static page and verify:

- the map renders without a blank canvas;
- controls update the event log and dashboard;
- storms, fire, bloom and oasis effects are visible;
- browser console has no JavaScript errors.

## Repository

GitHub:

```text
https://github.com/RCruzm08/LifeEngine
```

## License

No license file is currently included. Add one before distributing or accepting external contributions.
