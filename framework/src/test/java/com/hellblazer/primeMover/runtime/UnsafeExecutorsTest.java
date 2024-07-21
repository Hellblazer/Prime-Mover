package com.hellblazer.primeMover.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.jupiter.api.Test;

public class UnsafeExecutorsTest {
    private static String carrierThreadName() {
        var name = Thread.currentThread().toString();
        var index = name.lastIndexOf('@');
        if (index == -1) {
            throw new AssertionError();
        }
        return name.substring(index + 1);
    }

    @Test
    public void virtualThreadExecutorSingleThreadExecutor() throws InterruptedException {
        var executor = Executors.newSingleThreadExecutor();
        var virtualExecutor = UnsafeExecutors.virtualThreadExecutor(executor);
        var carrierThreadNames = new CopyOnWriteArraySet<String>();
        for (var i = 0; i < 10; i++) {
            virtualExecutor.execute(() -> carrierThreadNames.add(carrierThreadName()));
        }
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.DAYS);
        assertEquals(1, carrierThreadNames.size());
    }

    @Test
    void testVirtualThread() {
        Queue<Runnable> executor = new ArrayDeque<>();
        var virtualExecutor = UnsafeExecutors.virtualThreadExecutor(wrap(executor::add));

        Lock lock = new ReentrantLock();
        lock.lock();
        virtualExecutor.execute(lock::lock);
        assertEquals(1, executor.size(), "runnable for vthread has not been submitted");
        executor.poll().run();
        assertEquals(0, executor.size(), "vthread has not blocked");
        lock.unlock();
        assertEquals(1, executor.size(), "vthread is not schedulable");
        executor.poll().run();
        assertFalse(lock.tryLock(), "the virtual thread does not hold the lock");
    }

    private Executor wrap(Executor ex) {
        return r -> {
            System.out.println("Yes!");
            ex.execute(r);
        };
    }
}
