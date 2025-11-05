import java.util.Queue;

public class Car extends Thread {

    private final int carId;
    private final Queue<Car> queue;
    private final Semaphore empty;
    private final Semaphore full;
    private final Semaphore mutex;
    private final ServiceStationGUI gui;

    public Car(int carId, Queue<Car> queue, Semaphore empty, Semaphore full, Semaphore mutex, ServiceStationGUI gui) {
        this.carId = carId;
        this.queue = queue;
        this.empty = empty;
        this.full = full;
        this.mutex = mutex;
        this.gui = gui;
    }

    public int getCarId() {
        return carId;
    }

    @Override
    public void run() {
        try {
            empty.waitSemaphore();
            mutex.waitSemaphore();

            queue.add(this);
            gui.updateQueueDisplay(queue);
            gui.appendLog("Car " + carId + " joined queue.");

            mutex.signal();
            full.signal();

        } catch (InterruptedException e) {
            interrupt();
        }
    }
}
