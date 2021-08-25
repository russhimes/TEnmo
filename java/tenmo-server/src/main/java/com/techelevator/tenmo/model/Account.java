package com.techelevator.tenmo.model;

import java.math.BigDecimal;

public class Account {

    private long id;
    private long userId;
    private BigDecimal balance;
    //private List<Transfer> transfers;

    public Account(long id, long userId, BigDecimal balance) {
        this.id = id;
        this.userId = userId;
        this.balance = balance;
        //this.transfers = new ArrayList<>();
    }

    public long getId() {
        return id;
    }

    public long getUserId() {
        return userId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void addAmount(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }

    public void subtractAmount(BigDecimal amount) {
        this.balance = this.balance.subtract(amount);
    }
}
