package com.bank.brewdreamwelcome;

import java.util.Date;

/**
 * Customer model for the bank management system.
 * Updated to support database IDs, account IDs, and ID card numbers.
 */
public class Customer {
    private int id;
    private String accountId;
    private String idCardNumber;
    private String name;
    private String email;
    private String phone;
    private String address;
    private Date createdAt;

    /**
     * Primary constructor for database entities.
     */
    public Customer(int id, String accountId, String idCardNumber, String name, String email, String phone,
            String address) {
        this.id = id;
        this.accountId = accountId;
        this.idCardNumber = idCardNumber;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.createdAt = new Date();
    }

    // Getters and Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getIdCardNumber() {
        return idCardNumber;
    }

    public void setIdCardNumber(String idCardNumber) {
        this.idCardNumber = idCardNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return name + " (" + accountId + ")";
    }
}
