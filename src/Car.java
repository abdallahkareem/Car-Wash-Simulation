import java.util.Queue;

public class Car extends Thread {

    private int carId;
    private Queue<Car> queue;
    private Semaphore empty;
    private Semaphore full;
    private Semaphore mutex;

    public Car(int carId, Queue<Car> queue, Semaphore empty, Semaphore full, Semaphore mutex) {
        this.carId = carId;
        this.queue = queue;
        this.empty = empty;
        this.full = full;
        this.mutex = mutex;
    }

    public int getCarId() {
        return carId;
    }
    // ===== Thread Run Method =====
    @Override
    public void run() {
        try {
            empty.waitSemaphore();
            mutex.waitSemaphore();
            queue.add(this);
            System.out.println("Car " + carId + " has entered the queue.");
            mutex.signal();
            full.signal();
        }catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}