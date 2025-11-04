# 🚗 Car Wash & Gas Station Simulation (Java Multithreading)

## 📌 Overview
This Java project simulates a **Car Wash and Gas Station** using the **Producer-Consumer Problem** with a **Bounded Buffer Pattern**. It models a service station with:

- A fixed number of service bays (pumps)
- A waiting area (queue) with a limited capacity
- Car threads simulating arrivals
- Pump threads servicing cars concurrently

---

## 🧱 Components

| Class            | Description                                                                 |
|------------------|-----------------------------------------------------------------------------|
| `ServiceStation` | Starts the simulation, initializes shared data and thread pools             |
| `Car`            | Producer thread; simulates car arrival and waiting for service              |
| `Pump`           | Consumer thread; takes cars from the queue and processes them               |
| `Semaphore`      | Custom implementation to control access to shared resources                |

---

## 🔁 Simulation Flow

1. Cars arrive at the station and wait if the queue is full.
2. Pumps continuously check for available cars and service them.
3. Semaphores control:
   - Access to the queue (empty/full)
   - Mutex for safe queue access
   - Pump availability

---

## ✨ Example

**Sample Input**
- Waiting area capacity: `5`
- Number of service bays (pumps): `3`
- Cars arriving (order): `C1, C2, C3, C4, C5`
