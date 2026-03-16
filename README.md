## Project Overview

This project implements four thread-safe sorted data structures in Java, each using a different synchronization strategy:

| Data Structure | Code | Strategy |
|---|---|---|
| `CoarseGrainedList` | `cgl` | Sorted linked list, single global lock |
| `FineGrainedList` | `fgl` | Sorted linked list, per-node hand-over-hand locking |
| `CoarseGrainedTree` | `cgt` | Binary search tree, single global lock |
| `FineGrainedTree` | `fgt` | Binary search tree, per-node locking |

All four implement the `Sorted<T>` interface:

| Method | Description |
|---|---|
| `add(T t)` | Insert element in sorted order (duplicates tracked via counter) |
| `remove(T t)` | Remove one occurrence of an element |
| `removeLowest()` | Remove and return the smallest element |
| `toArrayList()` | Return all elements as a sorted `ArrayList` |

## File Structure

```
src/data_structures/
├── Sorted.java                       # Shared interface
├── WorkerThread.java                 # Thread for performance benchmarking
├── Main.java / Main2.java           # Entry points for benchmarking
├── DoRuns.java                       # Benchmark orchestration
│
├── implementation/
│   ├── CoarseGrainedList.java        # cgl implementation
│   ├── FineGrainedList.java          # fgl implementation
│   ├── CoarseGrainedTree.java        # cgt implementation
│   └── FineGrainedTree.java          # fgt implementation
│
└── tests/
    ├── UnitTestRunner.java           # Test entry point
    ├── SequentialTestsInteger.java   # Single-threaded tests (Integer)
    ├── SequentialTestsString.java    # Single-threaded tests (String)
    └── ThreadedTestsInteger.java     # Multi-threaded stress tests

report/                               # Directory for the PDF report
```

## Building

```bash
./gradlew build
```

## Running the Tests

```bash
bin/test_data_structures <variant>
```

`<variant>` is one of: `cgl` | `fgl` | `cgt` | `fgt`

**Example:**

```bash
bin/test_data_structures cgl
```

This runs **21 tests** per variant:
- 10 sequential Integer tests
- 8 sequential String tests
- 3 multi-threaded stress tests (each repeated 10,000 times)

## Running the Benchmark

```bash
bin/run_data_structures <variant> <threads> <items> <work_us>
```

| Argument | Description |
|---|---|
| `variant` | Data structure to use: `cgl`, `fgl`, `cgt`, `fgt` |
| `threads` | Number of worker threads |
| `items` | Number of items each thread adds and then removes |
| `work_us` | Microseconds of simulated CPU work per remove (Amdahl's law) |

**Example:**

```bash
bin/run_data_structures cgl 2 10000 10
```

Benchmarks the coarse-grained list with 2 threads, each adding and removing 10,000 items, with 10 microseconds of work per removal.