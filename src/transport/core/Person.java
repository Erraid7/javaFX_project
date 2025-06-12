// src/transport/core/Person.java
package transport.core;

import java.time.LocalDate;

/**
 * Abstract base class for all persons in the system.
 */
public abstract class Person {
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private boolean handicap;

    public Person(String firstName, String lastName, LocalDate dateOfBirth, boolean handicap) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.handicap = handicap;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public boolean hasHandicap() {
        return handicap;
    }

    @Override
    public String toString() {
        return String.format("%s %s", firstName, lastName);
    }
}
