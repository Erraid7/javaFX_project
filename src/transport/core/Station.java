package transport.core;

/**
 * Represents a station, which can also be suspended/reactivated.
 */
public class Station implements Suspendable {
    private final String name;
    private boolean suspended = false;

    public Station(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public void suspend() {
        suspended = true;
    }

    @Override
    public void reactivate() {
        suspended = false;
    }

    @Override
    public boolean isSuspended() {
        return suspended;
    }

    @Override
    public String getState() {
        return suspended ? "suspended" : "active";
    }

    @Override
    public String toString() {
        return "Station " + name + " [" + getState() + "]";
    }
}
