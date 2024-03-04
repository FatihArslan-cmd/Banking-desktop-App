package com.buddybank.mysuperbnak;

import javafx.beans.property.*;

class Account {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty type = new SimpleStringProperty();
    private final DoubleProperty balance = new SimpleDoubleProperty();
    private final IntegerProperty customerId = new SimpleIntegerProperty();

    public Account(int id, String type, double balance, int customerId) {
        this.id.set(id);
        this.type.set(type);
        this.balance.set(balance);
        this.customerId.set(customerId);
    }

    public int getId() {
        return id.get();
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public String getType() {
        return type.get();
    }

    public void setType(String type) {
        this.type.set(type);
    }

    public StringProperty typeProperty() {
        return type;
    }

    public double getBalance() {
        return balance.get();
    }

    public void setBalance(double balance) {
        this.balance.set(balance);
    }

    public DoubleProperty balanceProperty() {
        return balance;
    }

    public int getCustomerId() {
        return customerId.get();
    }

    public void setCustomerId(int customerId) {
        this.customerId.set(customerId);
    }

    public IntegerProperty customerIdProperty() {
        return customerId;
    }
}