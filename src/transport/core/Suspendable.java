package transport.core;

/**
 * Common interface for entities that can be suspended/reactivated.
 */
public interface Suspendable {
    void suspend();
    void reactivate();
    boolean isSuspended();
    String getState();  // "active" or "suspended"
}
