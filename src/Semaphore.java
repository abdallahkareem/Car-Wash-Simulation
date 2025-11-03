public class Semaphore {
    private int value;

    public Semaphore(int initial) {
    }

    public synchronized void waitSemaphore() throws InterruptedException {
        value--;
        if (value < 0)
        try {
            wait();
        } catch (Exception e) {

        }
    }

    public synchronized void signal() {
        value++;
        if (value <= 0) {
            notifyAll();
        }
    }
}