import java.util.*;
import java.util.concurrent.*;

public class ServiceStation {

    private Queue<Car> queue;
    private Semaphore empty;
    private Semaphore full;
    private Semaphore mutex;
    private Semaphore pumps;

    private List<Pump> pumpThreads;

    private int numPumps;
    private int queueSize;

    public ServiceStation(int numPumps, int queueSize) {
        // Initialize all shared resources here
    }

    public void startSimulation() {
        // Start pumps and car threads
    }

    // ===== Main Entry Point =====
    public static void main(String[] args) {
        // Create and start ServiceStation
    }
}