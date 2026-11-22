import java.lang.management.*;

public class Wallets {
    static class Account {
        final int id; int balance;
        Account(int id, int balance) { this.id = id; this.balance = balance; }
    }

    // DEADLOCK-PRONE: locks 'from' then 'to'
    static void transferUnsafe(Account from, Account to, int amt) throws InterruptedException {
        synchronized (from) {
            System.out.println(Thread.currentThread().getName() + " locked account " + from.id + ", wants " + to.id);
            Thread.sleep(100);                     // widen the window so both threads hold their first lock
            synchronized (to) {
                from.balance -= amt; to.balance += amt;
                System.out.println(Thread.currentThread().getName() + " transferred " + amt + " from " + from.id + " to " + to.id);
            }
        }
    }
    // FIXED: always lock the account with the smaller id first
    static void transferSafe(Account from, Account to, int amt) throws InterruptedException {
        Account first = from.id < to.id ? from : to, second = first == from ? to : from;
        synchronized (first) {
            System.out.println(Thread.currentThread().getName() + " locked account " + first.id + ", wants " + second.id);
            Thread.sleep(100);
            synchronized (second) {
                from.balance -= amt; to.balance += amt;
                System.out.println(Thread.currentThread().getName() + " transferred " + amt + " from " + from.id + " to " + to.id);
            }
        }
    }

    interface Transfer { void go(Account f, Account t, int a) throws InterruptedException; }

    public static void main(String[] args) throws Exception {
        boolean safe = args[0].equals("safe");
        Transfer tr = safe ? Wallets::transferSafe : Wallets::transferUnsafe;
        Account a1 = new Account(1, 1000), a2 = new Account(2, 1000);
        Thread t1 = new Thread(() -> { try { tr.go(a1, a2, 100); } catch (InterruptedException e) { } }, "T1(1->2)");
        Thread t2 = new Thread(() -> { try { tr.go(a2, a1, 50);  } catch (InterruptedException e) { } }, "T2(2->1)");
        System.out.println("Version: " + (safe ? "FIXED (lock ordering by id)" : "DEADLOCK-PRONE (lock from, then to)"));
        t1.start(); t2.start();
        t1.join(2000); t2.join(2000);
        ThreadMXBean mx = ManagementFactory.getThreadMXBean();
        long[] ids = mx.findDeadlockedThreads();
        if (ids == null) {
            System.out.println("findDeadlockedThreads() = null  -> no deadlock");
            System.out.println("Final balances: acct1=" + a1.balance + " acct2=" + a2.balance + " (expected 950 / 1050)");
        } else {
            System.out.println("findDeadlockedThreads() found " + ids.length + " threads:");
            for (ThreadInfo ti : mx.getThreadInfo(ids))
                System.out.println("  " + ti.getThreadName() + " state=" + ti.getThreadState() + " waiting for lock " + ti.getLockName() + " held by " + ti.getLockOwnerName());
            System.out.println("Final balances: acct1=" + a1.balance + " acct2=" + a2.balance + " (transfers never completed)");
            Runtime.getRuntime().halt(1);
        }
    }
}
