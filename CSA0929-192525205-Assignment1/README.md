# CSA0929 – Programming in Java
## Assignment 1 – CO1 – Object-Oriented Design: Smart Parking System

| | |
|---|---|
| **Student** | Saai Vardhan D |
| **Register Number** | 192525205 |
| **Course** | CSA0929 – Programming in Java (SIMATS) |
| **Assignment** | 1 (CO1) |
| **Java** | openjdk version "21.0.12.1" 2026-08-18 |

## How to compile and run

Every program folder is self-contained. Open a terminal in the folder and run:

```bash
javac *.java
java <MainClass>
```

The parking system uses packages, so build it from `ParkingSystem`:

```bash
cd ParkingSystem
javac -d out $(find src -name "*.java")
java -cp out parking.app.Main
```

## Contents

| Folder | What it contains | Main class |
|---|---|---|
| `ParkingSystem/src/parking` | Complete parking system (packages model / service / ext / app): vehicles, passes, tickets, factory, extensions. Tasks 4–12. | `parking.app.Main` |
| `Experiments/FeeDemo` | Fee calculation through the `Chargeable` interface (Tasks 7–9). | `FeeDemo` |
| `Experiments/ParkingFacility_2D_Array` | Slot grid as a 2-D array (Task 2 – data representation). | `ParkingFacility` |
| `Experiments/CompBreak` | Measurement experiment used in the design analysis. | `CompBreak` |
| `Experiments/TicketFind` | Array vs ArrayList vs HashMap ticket look-up timing. | `TicketFind` |

Folders named `compile_error_demo` (if any) are deliberate: they hold code that is meant to fail compilation, as discussed in the report.

Only Java source (and the SQL script where the assignment needs one) is included. Output screenshots and the written analysis are in the report (`Assignment1` Word document).
