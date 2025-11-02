import java.util.Queue;

public class Car extends Thread {

    private int carId;
    private Queue<Car> queue;
    private Semaphore empty;
    private Semaphore full;
    private Semaphore mutex;

    public Car(int carId, Queue<Car> queue, Semaphore empty, Semaphore full, Semaphore mutex) {
    }

    // ===== Thread Run Method =====
    @Override
    public void run() {
        // Producer logic (to be implemented)
    }
}