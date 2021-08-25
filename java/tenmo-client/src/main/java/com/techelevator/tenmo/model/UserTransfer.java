package com.techelevator.tenmo.model;

import java.math.BigDecimal;

public class UserTransfer {

    private long transferId;
    private long transferStatusId;
    private String transferStatusDesc;
    private long transferTypeId;
    private String transferTypeDesc;
    private String accountFromUserName;
    private String accountToUserName;
    private BigDecimal amount;

    public void setTransferStatusDesc(String transferStatusDesc) {
        this.transferStatusDesc = transferStatusDesc;
    }

    public void setTransferTypeDesc(String transferTypeDesc) {
        this.transferTypeDesc = transferTypeDesc;
    }

    public void setTransferId(long transferId) {
        this.transferId = transferId;
    }

    public void setTransferStatusId(long transferStatusId) {
        this.transferStatusId = transferStatusId;
    }

    public void setTransferTypeId(long transferTypeId) {
        this.transferTypeId = transferTypeId;
    }

    public void setAccountFromUserName(String accountFromUserName) {
        this.accountFromUserName = accountFromUserName;
    }

    public void setAccountToUserName(String accountToUserName) {
        this.accountToUserName = accountToUserName;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getTransferStatusDesc() {
        return transferStatusDesc;
    }

    public String getTransferTypeDesc() {
        return transferTypeDesc;
    }

    public long getTransferId() {
        return transferId;
    }

    public long getTransferStatusId() {
        return transferStatusId;
    }

    public long getTransferTypeId() {
        return transferTypeId;
    }

    public String getAccountFromUserName() {
        return accountFromUserName;
    }

    public String getAccountToUserName() {
        return accountToUserName;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}