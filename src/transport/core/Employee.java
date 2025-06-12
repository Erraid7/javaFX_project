// src/transport/core/Employee.java
package transport.core;

import java.time.LocalDate;

/**
 * Represents an employee at the station or as a driver.
 */
public class Employee extends Person {
    private String matricule;
    private FunctionType function;

    public Employee(String firstName, String lastName, LocalDate dateOfBirth, boolean handicap,
                    String matricule, FunctionType function) {
        super(firstName, lastName, dateOfBirth, handicap);
        this.matricule = matricule;
        this.function = function;
    }

    public String getMatricule() {
        return matricule;
    }

    public FunctionType getFunction() {
        return function;
    }
}
