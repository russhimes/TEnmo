package com.techelevator.tenmo.model;

import java.math.BigDecimal;

public class Transfer {

    private long transfer_id;
    private String transfer_type_desc;
    private String transfer_status_desc;
    private long account_from;
    private long account_to;
    private BigDecimal amount;

    private long transfer_type_id;
    private long transfer_status_id;




    public Transfer(long transfer_id, String transfer_type_desc, String transfer_status_desc, long account_from, long account_to, BigDecimal amount) {
        this.transfer_id = transfer_id;
        this.transfer_type_desc = transfer_type_desc;
        this.transfer_status_desc = transfer_status_desc;
        this.account_from = account_from;
        this.account_to = account_to;
        this.amount = amount;


    }

    public Transfer(){
    }

    public long getTransfer_id() {
        return transfer_id;
    }

    public String getTransfer_type_desc() {
        return transfer_type_desc;
    }

    public String getTransfer_status_desc() {
        return transfer_status_desc;
    }

    public long getTransfer_type_id() {
        return transfer_type_id;
    }

    public long getTransfer_status_id() {
        return transfer_status_id;
    }

    public long getAccount_from() {
        return account_from;
    }

    public long getAccount_to() {
        return account_to;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setTransfer_id(long transfer_id) {
        this.transfer_id = transfer_id;
    }

    public void setTransfer_type_desc(String transfer_type_desc) {
        this.transfer_type_desc = transfer_type_desc;
    }

    public void setTransfer_status_desc(String transfer_status_desc) {
        this.transfer_status_desc = transfer_status_desc;
    }

    public void setAccount_from(long account_from) {
        this.account_from = account_from;
    }

    public void setAccount_to(long account_to) {
        this.account_to = account_to;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setTransfer_type_id(long transfer_type_id) {
        this.transfer_type_id = transfer_type_id;
    }

    public void setTransfer_status_id(long transfer_status_id) {this.transfer_status_id = transfer_status_id;}

    //================================================================================================================



}