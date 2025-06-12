package transport.core;

import java.time.LocalDate;
import java.time.Period;

public class PersonalCard extends TransportPass {
    private final String ownerFullName;
    private final LocalDate birthDate;
    private final boolean isHandicapped;
    private final boolean isEmployee;
    private CardType cardType;

    public PersonalCard(Person person, boolean isEmployee, PaymentMethod paymentMethod) {
        super(paymentMethod);
        this.ownerFullName = person.getFirstName() + " " + person.getLastName();
        this.birthDate = person.getDateOfBirth();
        this.isHandicapped = person.hasHandicap();
        this.isEmployee = isEmployee;
        setCardTypeAndPrice();
    }

    public PersonalCard(int id, Person person, boolean isEmployee, PaymentMethod paymentMethod, LocalDate purchaseDate) {
        super(id, paymentMethod, purchaseDate);
        this.ownerFullName = person.getFirstName() + " " + person.getLastName();
        this.birthDate = person.getDateOfBirth();
        this.isHandicapped = person.hasHandicap();
        this.isEmployee = isEmployee;
        setCardTypeAndPrice();
    }

    private void setCardTypeAndPrice() {
        int age = Period.between(birthDate, LocalDate.now()).getYears();
        if (isHandicapped) {
            cardType = CardType.SOLIDARITY;
            price = 5000 * 0.5;
        } else if (isEmployee) {
            cardType = CardType.PARTNER;
            price = 5000 * 0.6;
        } else if (age < 25) {
            cardType = CardType.JUNIOR;
            price = 5000 * 0.7;
        } else if (age > 65) {
            cardType = CardType.SENIOR;
            price = 5000 * 0.75;
        }  else {
            throw new ReductionImpossibleException("No discount applies for this user.");
        }
    }

    public CardType getCardType() {
        return cardType;
    }

    @Override
    public  String getOwnerFullName() {
        return ownerFullName;
    }

    public boolean isEmployee() {
        return isEmployee;
    }

    @Override
    public boolean isValid(LocalDate date) {
        if (Period.between(purchaseDate, date).getYears() < 1) {
            return true;
        }
        throw new InvalidPassException("Card #" + id + " has expired.");
    }

    @Override
    public String toString() {
        return super.toString() + " | Owner: " + ownerFullName + " | Type: " + cardType;
    }
}
