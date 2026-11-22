# CSA0929 – Programming in Java
## Assignment 3 – CO3 – Exceptions and Threads

| | |
|---|---|
| **Student** | Saai Vardhan D |
| **Register Number** | 192525205 |
| **Course** | CSA0929 – Programming in Java (SIMATS) |
| **Assignment** | 3 (CO3) |
| **Java** | openjdk version "21.0.12.1" 2026-08-18 |

## How to compile and run

Every program folder is self-contained. Open a terminal in the folder and run:

```bash
javac *.java
java <MainClass>
```

## Contents

| Folder | What it contains | Main class |
|---|---|---|
| `Q1_ATM_Exceptions` | ATM failure handling: custom checked exception and the uncaught case. | `Atm`, `Uncaught` |
| `Q1_ATM_Exceptions/compile_error_demo` | INTENTIONALLY do not compile: Multi = multi-catch of related exceptions; AtmBad = catch order (already caught). | `AtmBad`, `Multi` |
| `Q2_Exam_Portal/layers` | Exception propagation through layers with chaining. | `Layers` |
| `Q2_Exam_Portal/storage` | Storage-layer exception demo. | `StorageDemo` |
| `Q2_Exam_Portal/variant_layers` | Variant of the layers demo. | `Layers` |
| `Q2_Exam_Portal/variant_no_cause` | Same demo without exception chaining (shows lost cause). | `StorageDemo` |
| `Q3_Resource_Safety` | try-with-resources, close order, suppressed exceptions. | `ResourceDemo` |
| `Q4_Thread_Lifecycle` | Thread states and life-cycle experiments. | `Experiments`, `Scanners` |
| `Q5_Seat_Booking/unsynchronized` | Race condition (double booking). | `SeatBooking` |
| `Q5_Seat_Booking/synchronized` | Fixed with synchronized. | `SeatBooking` |
| `Q5_Seat_Booking/sleep_variant` | Race widened with sleep. | `SeatBooking` |
| `Q5_Seat_Booking/yield_variant` | Race with yield. | `SeatBooking` |
| `Q5_Seat_Booking/benchmark` | Cost of synchronisation. | `Bench` |
| `Q6_Deadlock/wallets` | Deadlock between two wallets and the lock-ordering fix. | `Wallets` |
| `Q6_Deadlock/token_counter_if` | wait() guarded by if (incorrect). | `TokenCounter` |
| `Q6_Deadlock/token_counter_while` | wait() guarded by while (correct). | `TokenCounter` |
| `Q6_Deadlock/token_counter_trace` | Traced wait/notify execution. | `TokenCounter` |
| `Q7_Result_Processor` | Concurrent fault-tolerant processor (ExecutorService, Callable, Future). | `ResultProcessor` |

Folders named `compile_error_demo` (if any) are deliberate: they hold code that is meant to fail compilation, as discussed in the report.

Only Java source (and the SQL script where the assignment needs one) is included. Output screenshots and the written analysis are in the report (`Assignment3` Word document).
