import java.util.Queue;

public class Pump extends Thread {

    private int pumpId;
    private Queue<Car> queue;
    private Semaphore empty;
    private Semaphore full;
    private Semaphore mutex;
    private Semaphore pumps;

    public Pump(int pumpId, Queue<Car> queue, Semaphore empty, Semaphore full, Semaphore mutex, Semaphore pumps) {
        this.pumpId = pumpId;
        this.queue = queue;
        this.empty = empty;
        this.full = full;
        this.mutex = mutex;
        this.pumps = pumps;
    }

    // ===== Thread Run Method =====
    @Override
    public void run() {
        try {
            while (true) {
                pumps.waitSemaphore();
                full.waitSemaphore();
                mutex.waitSemaphore();
                Car car = queue.poll();
                System.out.println("Pump " + pumpId + " is servicing Car " + car.getCarId() + ".");
                mutex.signal();
                Thread.sleep(1000); // Simulate time to start servicing
                System.out.println("Pump " + pumpId + " has finished servicing Car " + car.getCarId() + ".");
                empty.signal();
                pumps.signal();

                if (Thread.interrupted()) {
                    break;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}