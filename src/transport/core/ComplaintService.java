package transport.core;

import java.util.*;

/**
 * Manages submission, listing, and resolution of complaints.
 * Suspends any target that accumulates >3 complaints.
 */
public class ComplaintService {
    private static final int THRESHOLD = 3;
    private final List<Complaint> complaints = new ArrayList<>();
    private boolean autoSave = false;  // Flag to control auto-saving

    /** Submit a new complaint. */
    public void submit(Complaint c) {
        complaints.add(c);
        long count = complaints.stream()
                .filter(comp -> comp.getTarget().equals(c.getTarget()))
                .count();
        if (count > THRESHOLD) {
            c.getTarget().suspend();
        }

        // Only save if autoSave is enabled
        if (autoSave) {
            try {
                AppState.getInstance().saveComplaints();
            } catch (Exception e) {
                System.err.println("Error saving complaints: " + e.getMessage());
            }
        }
    }

    /** Resolve a complaint: remove it and possibly reactivate the target. */
    public void resolve(Complaint c) {
        complaints.remove(c);
        long count = complaints.stream()
                .filter(comp -> comp.getTarget().equals(c.getTarget()))
                .count();
        if (count <= THRESHOLD) {
            c.getTarget().reactivate();
        }

        // Only save if autoSave is enabled
        if (autoSave) {
            try {
                AppState.getInstance().saveComplaints();
            } catch (Exception e) {
                System.err.println("Error saving complaints: " + e.getMessage());
            }
        }
    }

    /** Enable or disable auto-saving */
    public void setAutoSave(boolean autoSave) {
        this.autoSave = autoSave;
    }

    /** Add complaints in bulk without triggering auto-save */
    public void addAll(List<Complaint> complaintsToAdd) {
        complaints.addAll(complaintsToAdd);
    }

    /** List all complaints. */
    public List<Complaint> listAll() {
        return Collections.unmodifiableList(complaints);
    }

    /** List complaints by reporter. */
    public List<Complaint> listByReporter(Person p) {
        return complaints.stream()
                .filter(c -> c.getReporter().equals(p))
                .toList();
    }

    /** List complaints by target (station or vehicle). */
    public List<Complaint> listByTarget(Suspendable s) {
        return complaints.stream()
                .filter(c -> c.getTarget().equals(s))
                .toList();
    }
}