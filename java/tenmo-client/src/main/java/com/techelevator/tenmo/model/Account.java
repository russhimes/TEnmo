package com.techelevator.tenmo.model;

import java.math.BigDecimal;

public class Account {

    private long id;
    private long userId;
    private BigDecimal balance;

    public BigDecimal getBalance() {
        return balance;
    }
    public long getUserId(){return userId;}
}

