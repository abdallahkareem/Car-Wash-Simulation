import java.util.Queue;

public class Pump extends Thread {

    private final int pumpId;
    private final Queue<Car> queue;
    private final Semaphore empty;
    private final Semaphore full;
    private final Semaphore mutex;
    private final Semaphore pumps;
    private final ServiceStationGUI gui;

    private volatile boolean running = true;

    public Pump(int pumpId, Queue<Car> queue, Semaphore empty, Semaphore full, Semaphore mutex, Semaphore pumps, ServiceStationGUI gui) {
        this.pumpId = pumpId;
        this.queue = queue;
        this.empty = empty;
        this.full = full;
        this.mutex = mutex;
        this.pumps = pumps;
        this.gui = gui;
    }

    public void stopRunning() {
        running = false;
        interrupt();
    }

    @Override
    public void run() {
        while (running) {
            try {
                pumps.waitSemaphore();
                full.waitSemaphore();
                mutex.waitSemaphore();

                if (!running) break;

                Car car = queue.poll();
                gui.updateQueueDisplay(queue);
                gui.updatePumpStatus(pumpId, true, "Car " + car.getCarId());

                mutex.signal();

                gui.appendLog("Pump " + pumpId + " servicing Car " + car.getCarId());
                Thread.sleep(gui.getServiceTimeMillis());

                gui.updatePumpStatus(pumpId, false, "");
                gui.appendLog("Pump " + pumpId + " finished Car " + car.getCarId());

                empty.signal();
                pumps.signal();

            } catch (InterruptedException e) {
                break;
            }
        }
    }
}
