package transport.core;

import java.time.LocalDate;

public class Ticket extends TransportPass {
    private boolean used;
    private final String ownerFullName;

    public Ticket(PaymentMethod paymentMethod, Person person) {
        super(paymentMethod);
        this.price = 50;
        this.ownerFullName = person.getFirstName() + " " + person.getLastName();
    }

    public Ticket(int id, PaymentMethod paymentMethod, LocalDate purchaseDate, Person person) {
        super(id, paymentMethod, purchaseDate);
        this.price = 50;
        this.ownerFullName = person.getFirstName() + " " + person.getLastName();
    }

    @Override
    public boolean isValid(LocalDate date) {
        if (date.equals(purchaseDate)) {
            return true;
        }
        throw new InvalidPassException("Ticket #" + id + " is only valid on the purchase date: " + purchaseDate);
    }

    @Override
    public String toString() {
        return super.toString() + " | Owner: " + ownerFullName;
    }

    @Override
    public  String getOwnerFullName() {
        return ownerFullName;
    }

    public void markAsUsed() {
        this.used = true;
    }

    public boolean isUsed() {
        return used;
    }
}
