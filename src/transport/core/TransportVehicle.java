package transport.core;

/**
 * Represents a public transport vehicle (bus, tram, etc.).
 * Currently only tracks an identifier and suspension state.
 */
public class TransportVehicle implements Suspendable {
    private final String id;
    private boolean suspended = false;

    public TransportVehicle(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
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
        return "Vehicle " + id + " [" + getState() + "]";
    }
}
