package transport.core;

import java.io.Serializable;
import java.time.LocalDate;

public abstract class TransportPass implements Serializable {
    public static int nextId = 1;
    protected final int id;
    protected final LocalDate purchaseDate;
    protected double price;
    protected PaymentMethod paymentMethod;

    protected TransportPass (int id, PaymentMethod paymentMethod, LocalDate purchaseDate) {
        this.id = id;
        this.purchaseDate = purchaseDate;
        this.paymentMethod = paymentMethod;
    }

    public TransportPass(PaymentMethod paymentMethod) {
        this.id = nextId++;
        this.purchaseDate = LocalDate.now();
        this.paymentMethod = paymentMethod;
    }

    public int getId() { return id; }
    public LocalDate getPurchaseDate() { return purchaseDate; }
    public double getPrice() { return price; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }

    public abstract boolean isValid(LocalDate date) throws InvalidPassException;

    @Override
    public String toString() {
        return "Pass #" + id + " | Purchased: " + purchaseDate + " | Price: " + price + " DA | Paid by: " + paymentMethod;
    }

    public LocalDate getExpiryDate() {
        return purchaseDate;
    }


    public  String getOwnerFullName() {
        return "";
    }
}
