package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.UserTransfer;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;

public interface UserTransferDao {

    UserTransfer getUserTransferInfoById(long transferId);

    List<UserTransfer> getUserTransferListByUserName(String userName);

    List<UserTransfer> getPendingUserTransferList(String userName);

    String getTransferStatusDesc(long transferStatusId);

    String getTransferTypeDesc(long transferTypeId);

    long getTransferTypeId(String transferTypeDesc);

    long getTransferStatusId(String transferStatusDesc);

    void createTransfer(UserTransfer userTransfer);

    void rejectRequest(long transferId);

    void acceptRequest(long transferId);

}
