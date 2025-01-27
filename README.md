# Multithreading-Locks-TTAS-Alock-MCS-TAS-CLH-Backoff-SpinSleepLock

This project was completed as part of my multiprocessor programming course. It focuses on implementing and comparing the performance of different synchronization locks.

**Locks Implemented**

MCS Lock: A queue-based lock that is scalable and fair.
TTAS Lock: Reduces contention with a test-and-test-and-set approach.
TAS Lock: A simple spinlock using a test-and-set mechanism.
ALock: Uses an array to implement a queue-based lock.
CLH Lock: A scalable, queue-based lock with local spinning.
**Features**

Implementation of multiple locks in a modular way.
Performance testing under various contention levels.
Comparison of locks based on waiting time, throughput, and scalability
