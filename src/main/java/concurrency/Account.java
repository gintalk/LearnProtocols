package concurrency;

import java.util.concurrent.atomic.AtomicInteger;

public class Account {
    private final AtomicInteger debitBalance;
    private final AtomicInteger creditBalance;

    public Account(int debitBalance, int creditBalance) {
        this.debitBalance = new AtomicInteger(debitBalance);
        this.creditBalance = new AtomicInteger(creditBalance);
    }

    void increaseBalance(int amount, boolean isCredit) {
        if (isCredit) {
            this.creditBalance.getAndAdd(amount);
        } else {
            this.debitBalance.getAndAdd(amount);
        }
    }

    public int getDebitBalance() {
        return this.debitBalance.get();
    }

    public int getCreditBalance() {
        return this.creditBalance.get();
    }
}