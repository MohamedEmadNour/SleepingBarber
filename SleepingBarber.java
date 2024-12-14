import java.util.concurrent.Semaphore;

public class SleepingBarber {
    // Number of barbers, chairs, and customers
    private static final int NUM_BARBERS = 3;
    private static final int NUM_CHAIRS = 5;

    // Semaphores for synchronization
    private static final Semaphore customers = new Semaphore(0);
    private static final Semaphore barbers = new Semaphore(0);
    private static final Semaphore accessWaitingChairs = new Semaphore(1);

    // Track available chairs
    private static int waitingCustomers = 0;

    public static void main(String[] args) {
        // Create barber threads
        for (int i = 1; i <= NUM_BARBERS; i++) {
            new Thread(new Barber(i)).start();
        }

        // Create customer threads (simulation of customers arriving randomly)
        for (int i = 1; i <= 20; i++) { // Number of customers
            try {
                Thread.sleep((int) (Math.random() * 2000)); // Random arrival time
                new Thread(new Customer(i)).start();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    static class Barber implements Runnable {
        private final int barberId;

        public Barber(int id) {
            this.barberId = id;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    customers.acquire(); // Wait for a customer
                    accessWaitingChairs.acquire(); // Access waiting chairs
                    waitingCustomers--; // Customer leaves waiting chair
                    System.out.println("Barber " + barberId + " is taking a customer. Waiting customers: " + waitingCustomers);
                    accessWaitingChairs.release(); // Release waiting chairs
                    barbers.release(); // Barber is now busy
                    cutHair();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private void cutHair() {
            try {
                System.out.println("Barber " + barberId + " is cutting hair.");
                Thread.sleep((int) (Math.random() * 3000)); // Simulate haircut time
                System.out.println("Barber " + barberId + " has finished cutting hair.");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    static class Customer implements Runnable {
        private final int customerId;

        public Customer(int id) {
            this.customerId = id;
        }

        @Override
        public void run() {
            try {
                accessWaitingChairs.acquire(); // Try to get a chair
                if (waitingCustomers < NUM_CHAIRS) {
                    waitingCustomers++;
                    System.out.println("Customer " + customerId + " is waiting. Waiting customers: " + waitingCustomers);
                    customers.release(); // Notify a barber
                    accessWaitingChairs.release(); // Release access to waiting chairs
                    barbers.acquire(); // Wait for a barber
                    getHaircut();
                } else {
                    System.out.println("Customer " + customerId + " leaves as no chairs are available.");
                    accessWaitingChairs.release(); // Release access to waiting chairs
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void getHaircut() {
            System.out.println("Customer " + customerId + " is getting a haircut.");
        }
    }
}
