package ru.dtrunin.singletontest;

import android.app.Application;
import android.os.ConditionVariable;
import android.os.SystemClock;
import android.test.ApplicationTestCase;
import android.util.Log;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by dtrunin on 11.04.2015.
 */
public class SingletonTest extends ApplicationTestCase<Application> {

    public SingletonTest() {
        super(Application.class);
    }


    static class SynchronizedSingleton {
        private static SynchronizedSingleton instance;

        synchronized static SynchronizedSingleton getInstance() {
            if (instance == null) {
                instance = new SynchronizedSingleton();
            }
            return instance;
        }

        static void reset() {
            instance = null;
        }
    }

    static class DoubleCheckedLocking {
        private static volatile DoubleCheckedLocking instance;

        static DoubleCheckedLocking getInstance() {
            if (instance == null) {
                synchronized (DoubleCheckedLocking.class) {
                    if (instance == null) {
                        instance = new DoubleCheckedLocking();
                    }
                }
            }
            return instance;
        }

        static void reset() {
            instance = null;
        }
    }

    static class CompareAndSet {
        private static AtomicReference<CompareAndSet> instance = new AtomicReference<>();

        static CompareAndSet getInstance() {
            while (true) {
                CompareAndSet obj = instance.get();
                if (obj != null) {
                    return obj;
                }
                obj = new CompareAndSet();
                if (instance.compareAndSet(null, obj)) {
                    return obj;
                }
            }
        }

        static void reset() {
            instance.set(null);
        }
    }

    static class HolderClass {
        private static class Holder {
            static HolderClass instance = new HolderClass();
        }

        static HolderClass getInstance() {
            return Holder.instance;
        }
    }

    enum EnumSingleton {
        INSTANCE;

        static EnumSingleton getInstance() {
            return INSTANCE;
        }
    }


    public void testSingleThreadDCL() {
        DoubleCheckedLocking.reset();
        testSingleThread("testSingleThreadDCL", new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 10000000; i++) {
                    DoubleCheckedLocking obj = DoubleCheckedLocking.getInstance();
                }
            }
        });
    }

    public void testSingleThreadCAS() {
        CompareAndSet.reset();
        testSingleThread("testSingleThreadCAS", new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 10000000; i++) {
                    CompareAndSet obj = CompareAndSet.getInstance();
                }
            }
        });
    }

    public void testSingleThreadHolder() {
        testSingleThread("testSingleThreadHolder", new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 10000000; i++) {
                    HolderClass obj = HolderClass.getInstance();
                }
            }
        });
    }

    public void testSingleThreadEnum() {
        testSingleThread("testSingleThreadEnum", new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 10000000; i++) {
                    EnumSingleton obj = EnumSingleton.getInstance();
                }
            }
        });
    }

    public void testSingleThreadSynchronized() {
        testSingleThread("testSingleThreadSynchronized", new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 10000000; i++) {
                    SynchronizedSingleton obj = SynchronizedSingleton.getInstance();
                }
            }
        });
    }

    public void testParallelDCL() {
        DoubleCheckedLocking.reset();
        testParallel("testParallelDCL", new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 100000; i++) {
                    DoubleCheckedLocking obj = DoubleCheckedLocking.getInstance();
                }
            }
        });
    }

    public void testParallelCAS() {
        CompareAndSet.reset();
        testParallel("testParallelCAS", new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 100000; i++) {
                    CompareAndSet obj = CompareAndSet.getInstance();
                }
            }
        });
    }

    public void testParallelHolder() {
        testParallel("testParallelHolder", new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 100000; i++) {
                    HolderClass obj = HolderClass.getInstance();
                }
            }
        });
    }

    public void testParallelEnum() {
        testParallel("testParallelEnum", new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 100000; i++) {
                    EnumSingleton obj = EnumSingleton.getInstance();
                }
            }
        });
    }

    public void testParallelSynchronized() {
        testParallel("testParallelSynchronized", new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 100000; i++) {
                    SynchronizedSingleton obj = SynchronizedSingleton.getInstance();
                }
            }
        });
    }

    private static void testSingleThread(String testName,
                                         final Runnable workerRunnable) {
        final long startTime = SystemClock.currentThreadTimeMillis();
        workerRunnable.run();
        Log.i(LOG_TAG, testName + ": finished in "
                + (SystemClock.currentThreadTimeMillis() - startTime) + " ms");
    }

    private static void testParallel(String testName,
                                     final Runnable workerRunnable) {
        final ConditionVariable startCondition = new ConditionVariable();
        final CountDownLatch startLatch = new CountDownLatch(100);
        final CountDownLatch finishLatch = new CountDownLatch(100);
        final AtomicLong totalTime = new AtomicLong(0);
        final int threadCount = 100;

        for (int i = 0; i < threadCount; i++) {
            new Thread() {
                @Override
                public void run() {
                    startLatch.countDown();
                    startCondition.block();

                    final long startTime = SystemClock.currentThreadTimeMillis();
                    workerRunnable.run();
                    final long time = SystemClock.currentThreadTimeMillis() - startTime;
                    totalTime.addAndGet(time);

                    finishLatch.countDown();
                }
            }.start();
        }

        try {
            startLatch.await();
        } catch (InterruptedException e) {
            fail("Interrupted");
            return;
        }

        startCondition.open();
        try {
            finishLatch.await();
            Log.i(LOG_TAG, testName + ": finished in " + totalTime.get() + " ms");

        } catch (InterruptedException e) {
            Log.e(LOG_TAG, testName + " <<< interrupted");
            fail("Interrupted");
            return;
        }
    }

    private static final String LOG_TAG = "SingletonTest";

}
