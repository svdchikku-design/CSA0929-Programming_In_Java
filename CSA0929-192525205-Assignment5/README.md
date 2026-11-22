# CSA0929 – Programming in Java
## Assignment 5 – CO5 – Data Persistence Engineering: Files and Databases

| | |
|---|---|
| **Student** | Saai Vardhan D |
| **Register Number** | 192525205 |
| **Course** | CSA0929 – Programming in Java (SIMATS) |
| **Assignment** | 5 (CO5) |
| **Java** | openjdk version "21.0.12.1" 2026-08-18 |

## How to compile and run

Every program folder is self-contained. Open a terminal in the folder and run:

```bash
javac *.java
java <MainClass>
```

Programs that use MySQL (`B2`, `B3`/`B4` loaders) need MySQL and the Connector/J driver on the class path; run `B2_Adversarial_Login/setup.sql` first and set the URL, user and password at the top of each program. Programs that need no database run as they are.

## Contents

| Folder | What it contains | Main class |
|---|---|---|
| `A4_Serialization/v1` | Baseline: save and load, transient fields. | `Main` |
| `A4_Serialization/v2_no_serialVersionUID` | Phone field added, no serialVersionUID → InvalidClassException. | `Main` |
| `A4_Serialization/v1_with_serialVersionUID` | Version 1 with serialVersionUID = 1L. | `Main` |
| `A4_Serialization/v2_with_serialVersionUID` | Version 2 reads the old file; new field defaults to null. | `Main` |
| `A4_Serialization/non_serializable_field` | Non-transient Connection field → NotSerializableException. | `Main` |
| `B1_File_Store_Measurements` | Given PassStore (TODO 1 completed), line-count buffering, find-growth, damaged file, serialization vs CSV. | `AdversarialFile`, `FindGrowth`, `LineCount`, `PassStore`, `SerBench` |
| `B1_Full_Store_7_Fields` | Task A1 design (7 fields) with tests; RandomAccessFile demo; validation cost. | `PassStore`, `PassStoreFull`, `RafDemo`, `ValidateBench` |
| `B2_Adversarial_Login` | Unsafe vs PreparedStatement login (MySQL). setup.sql creates the test database. | `LoginDemo` |
| `B3_Batch_Loader` | PassLoader (TODO 1–3 completed) and a mock-Connection test of commit/rollback. | `MockTest` |
| `B4_Migration` | Migration with failure report, DatabaseMetaData report, load benchmark. | `LoadBench`, `MetaDemo`, `MigrationTool` |

Folders named `compile_error_demo` (if any) are deliberate: they hold code that is meant to fail compilation, as discussed in the report.

Only Java source (and the SQL script where the assignment needs one) is included. Output screenshots and the written analysis are in the report (`Assignment5` Word document).
