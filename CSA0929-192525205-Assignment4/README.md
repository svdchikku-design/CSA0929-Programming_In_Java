# CSA0929 – Programming in Java
## Assignment 4 – CO4 – Event-Driven GUI Design

| | |
|---|---|
| **Student** | Saai Vardhan D |
| **Register Number** | 192525205 |
| **Course** | CSA0929 – Programming in Java (SIMATS) |
| **Assignment** | 4 (CO4) |
| **Java** | openjdk version "21.0.12.1" 2026-08-18 |

## How to compile and run

Every program folder is self-contained. Open a terminal in the folder and run:

```bash
javac *.java
java <MainClass>
```

GUI programs need a desktop (or `xvfb-run` on a headless Linux machine).

## Contents

| Folder | What it contains | Main class |
|---|---|---|
| `Task1_Fee_Desk` | Repaired fee window: Swing view + validating service + SwingWorker. | (library classes) |
| `Task3_Library_Search` | Library search screen with layout managers. | `LibrarySearch` |
| `Task4_Drawing_Pad/original` | Faulty original (AWT). | `DrawPad` |
| `Task4_Drawing_Pad/fix1` | First repair. | `DrawPad` |
| `Task4_Drawing_Pad/final` | Final repaired version. | `DrawPad` |
| `Task4_Drawing_Pad/compile_error_demo` | INTENTIONALLY does not compile: misspelt mouseclicked with @Override. | `DrawPad` |
| `Task4_Drawing_Pad/experiment_bad_nov` | Forensic experiment variant (no validation call). | `DrawPad` |
| `Task5_Currency_Converter` | Currency converter screen. | `Converter` |
| `Task5_Handler_Styles/A_frame_implements_v1` | Frame implements ActionListener. | `A` |
| `Task5_Handler_Styles/A_frame_implements_v2` | Variant 2. | `A` |
| `Task5_Handler_Styles/B_named_inner_v1` | Named inner class. | `B` |
| `Task5_Handler_Styles/B_named_inner_v2` | Variant 2. | `B` |
| `Task5_Handler_Styles/C_anonymous_inner_v1` | Anonymous inner class. | `C` |
| `Task5_Handler_Styles/C_anonymous_inner_v2` | Variant 2. | `C` |
| `Task5_Handler_Styles/D_lambda_v1` | Lambda handlers. | `D` |
| `Task5_Handler_Styles/D_lambda_v2` | Variant 2. | `D` |
| `Task5_Handler_Styles/style_comparison` | Shared helper for the style comparison. | (library classes) |
| `Task6_Registration_App` | Layered desktop app: GUI → service → DAO (in-memory and JDBC). schema.sql creates the MySQL table. | `RegistrationApp`, `Task6Demo` |
| `Benchmarks` | Event-handling cost measurements. | `Bench4` |

Folders named `compile_error_demo` (if any) are deliberate: they hold code that is meant to fail compilation, as discussed in the report.

Only Java source (and the SQL script where the assignment needs one) is included. Output screenshots and the written analysis are in the report (`Assignment4` Word document).
