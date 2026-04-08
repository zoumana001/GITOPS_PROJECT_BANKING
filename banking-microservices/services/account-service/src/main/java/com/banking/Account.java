package com.banking;

public class Account {
    private String id;
    private String owner;
    private String type; // SAVINGS, CHECKING
    private Double balance;
    private String status;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Double getBalance() { return balance; }
    public void setBalance(Double balance) { this.balance = balance; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}