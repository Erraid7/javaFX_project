package transport.core;

import java.time.LocalDate;

/**
 * A complaint raised by a user or employee against a station or vehicle.
 */
public class Complaint implements Comparable<Complaint> {
    public static int counter = 1;
    private final int id;
    private final LocalDate date;
    private final Person reporter;
    private final ComplaintType type;
    private final Suspendable target;
    private final String description;
    private final GravityLevel gravity;

    public Complaint(Person reporter,
                     ComplaintType type,
                     Suspendable target,
                     String description,
                     GravityLevel gravity,
                     LocalDate date) {
        this.id = counter++;
        this.reporter = reporter;
        this.type = type;
        this.target = target;
        this.description = description;
        this.gravity = gravity;
        this.date = date;
    }

    public Complaint(int id,Person reporter,
                     ComplaintType type,
                     Suspendable target,
                     String description,
                     GravityLevel gravity,
                     LocalDate date) {
        this.id = id;
        this.reporter = reporter;
        this.type = type;
        this.target = target;
        this.description = description;
        this.gravity = gravity;
        this.date = date;
    }

    public int getId() { return id; }
    public LocalDate getDate() { return date; }
    public Person getReporter() { return reporter; }
    public ComplaintType getType() { return type; }
    public Suspendable getTarget() { return target; }
    public String getDescription() { return description; }
    public GravityLevel getGravity() { return gravity; }

    @Override
    public int compareTo(Complaint o) {
        return Integer.compare(this.id, o.id);
    }

    @Override
    public String toString() {
        return String.format(
                "Complaint #%d [%s on %s]\n Reporter: %s\n Target: %s\n Gravity: %s\n Desc: %s\n",
                id, type, date, reporter, target, gravity, description);
    }

    public String getReporterFullName () {
        return reporter.getFirstName() + " " + reporter.getLastName();
    }
}
