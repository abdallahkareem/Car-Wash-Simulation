import java.util.Queue;

public class Pump extends Thread {

    private int pumpId;
    private Queue<Car> queue;
    private Semaphore empty;
    private Semaphore full;
    private Semaphore mutex;
    private Semaphore pumps;

    public Pump(int pumpId, Queue<Car> queue, Semaphore empty, Semaphore full, Semaphore mutex, Semaphore pumps) {
    }

    // ===== Thread Run Method =====
    @Override
    public void run() {
        // Consumer logic (to be implemented)
    }
}