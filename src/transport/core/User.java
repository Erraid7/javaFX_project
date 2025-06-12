// src/transport/core/User.java
package transport.core;

import java.time.LocalDate;

/**
 * Represents a passenger user.
 */
public class User extends Person {
    public User(String firstName, String lastName, LocalDate dateOfBirth, boolean handicap) {
        super(firstName, lastName, dateOfBirth, handicap);
    }
}