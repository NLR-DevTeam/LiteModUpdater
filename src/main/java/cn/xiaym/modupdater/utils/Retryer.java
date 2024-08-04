package cn.xiaym.modupdater.utils;

public class Retryer {
    public static void execute(UnsafeRunnable runnable, int maxRetries) throws Throwable {
        Throwable lastCause = null;
        for (int i = 0; i < maxRetries; i++) {
            try {
                runnable.run();
                return;
            } catch (Throwable th) {
                lastCause = th;
            }
        }

        if (lastCause != null) {
            throw lastCause;
        }
    }

    public static <T> T execute(UnsafeSupplier<T> supplier, int maxRetries) throws Throwable {
        Throwable lastCause = null;
        for (int i = 0; i < maxRetries; i++) {
            try {
                return supplier.get();
            } catch (Throwable th) {
                lastCause = th;
            }
        }

        if (lastCause != null) {
            throw lastCause;
        }

        throw new IllegalStateException("Impossible exception");
    }

    public static <T> T executeOrNull(UnsafeSupplier<T> supplier, int maxRetries) {
        try {
            return execute(supplier, maxRetries);
        } catch (Throwable th) {
            return null;
        }
    }

    public interface UnsafeRunnable {
        void run() throws Throwable;
    }

    public interface UnsafeSupplier<T> {
        T get() throws Throwable;
    }
}
