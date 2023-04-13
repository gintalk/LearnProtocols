package concurrency;

import java.util.concurrent.Phaser;

public class LearnConcurrency {

    private static final Phaser PHASER = new Phaser(1);

    public static void main(String[] args) {
        Account acc = new Account(100, 100);

        PHASER.register();
        new Thread(() -> {
            System.out.println("Thread 1 chạy nha");
            for (int i = 0; i < 10000; i++) {
                acc.increaseBalance(1, true);
            }
            System.out.println("Thread 1 chạy xong");
            PHASER.arriveAndAwaitAdvance();
        }).start();

        PHASER.register();
        new Thread(() -> {
            System.out.println("Thread 2 chạy nha");
            for (int i=0; i<10000; i++){
                acc.increaseBalance(1, true);
            }
            System.out.println("Thread 2 chạy xong");
            PHASER.arriveAndAwaitAdvance();
        }).start();

        PHASER.arriveAndAwaitAdvance();

        System.out.println(acc.getDebitBalance());
        System.out.println(acc.getCreditBalance());
    }
}
