package com.buddybank.mysuperbnak;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

class Customer {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty surname = new SimpleStringProperty();
    private final StringProperty email = new SimpleStringProperty();
    private final StringProperty phone = new SimpleStringProperty();
    private final StringProperty address = new SimpleStringProperty();
    private final ObservableList<Account> accounts = FXCollections.observableArrayList();

    public Customer(int id, String name, String surname, String email, String phone, String address) {
        this.id.set(id);
        this.name.set(name);
        this.surname.set(surname);
        this.email.set(email);
        this.phone.set(phone);
        this.address.set(address);
    }

    public int getId() {
        return id.get();
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public StringProperty nameProperty() {
        return name;
    }

    public String getSurname() {
        return surname.get();
    }

    public void setSurname(String surname) {
        this.surname.set(surname);
    }

    public StringProperty surnameProperty() {
        return surname;
    }

    public String getEmail() {
        return email.get();
    }

    public void setEmail(String email) {
        this.email.set(email);
    }

    public StringProperty emailProperty() {
        return email;
    }

    public String getPhone() {
        return phone.get();
    }

    public void setPhone(String phone) {
        this.phone.set(phone);
    }

    public StringProperty phoneProperty() {
        return phone;
    }

    public String getAddress() {
        return address.get();
    }

    public void setAddress(String address) {
        this.address.set(address);
    }

    public StringProperty addressProperty() {
        return address;
    }

    public ObservableList<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(ObservableList<Account> accounts) {
        this.accounts.setAll(accounts);
    }
}