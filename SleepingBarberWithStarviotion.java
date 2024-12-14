import java.util.concurrent.Semaphore;

class SleepingBarberWithStarviotion {
    // عدد الكراسي في منطقة الانتظار
    private static final int CHAIRS = 5;

    // Semaphore لإدارة الكراسي وعدد العملاء
    private final Semaphore waitingChairs = new Semaphore(CHAIRS);
    private final Semaphore barberReady = new Semaphore(0);
    private final Semaphore accessSeats = new Semaphore(1);

    private int waitingCustomers = 0;

    public static void main(String[] args) {
        SleepingBarberWithStarviotion barberShop = new SleepingBarberWithStarviotion();
        barberShop.startShop();
    }

    // تشغيل الصالون
    public void startShop() {
        // بدء تشغيل الحلاق
        Thread barberThread = new Thread(new Barber());
        barberThread.start();

        // إضافة العملاء
        for (int i = 1; i <= 10; i++) {
            new Thread(new Customer(i)).start();
            try {
                Thread.sleep((int) (Math.random() * 2000)); // وصول عشوائي للعملاء
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // فئة الحلاق
    class Barber implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    barberReady.acquire(); // انتظار العملاء
                    accessSeats.acquire(); // التحكم في الوصول إلى عدد الكراسي
                    waitingCustomers--; // تقليل عدد العملاء المنتظرين
                    System.out.println("Barber is cutting hair. Remaining customers: " + waitingCustomers);
                    accessSeats.release();
                    cutHair(); // قص الشعر
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private void cutHair() {
            try {
                Thread.sleep(3000); // وقت قص الشعر
                System.out.println("Barber finished cutting hair.");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // فئة العميل
    class Customer implements Runnable {
        private final int id;

        public Customer(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            try {
                accessSeats.acquire(); // التحكم في عدد الكراسي
                if (waitingCustomers < CHAIRS) {
                    waitingCustomers++; // إضافة العميل إلى الانتظار
                    System.out.println("Customer " + id + " is waiting. Total waiting: " + waitingCustomers);
                    barberReady.release(); // إشعار الحلاق بوجود عميل
                    accessSeats.release();
                    getHairCut(); // الحصول على قص الشعر
                } else {
                    System.out.println("Customer " + id + " left. No empty chairs.");
                    accessSeats.release();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void getHairCut() {
            try {
                Thread.sleep(3000); // وقت قص الشعر
                System.out.println("Customer " + id + " got a haircut.");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
