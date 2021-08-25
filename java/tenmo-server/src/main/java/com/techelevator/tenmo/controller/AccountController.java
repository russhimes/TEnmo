package com.techelevator.tenmo.controller;


import com.techelevator.tenmo.dao.AccountDao;

import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.dao.UserTransferDao;
import com.techelevator.tenmo.model.User;
import com.techelevator.tenmo.model.UserTransfer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@RestController
@PreAuthorize("isAuthenticated()")
@RequestMapping(path = "/account")
public class AccountController {

    @Autowired
    AccountDao accountDao;
    @Autowired
    UserDao userDao;
    @Autowired
    UserTransferDao userTransferDao;

    @RequestMapping(path = "/balance", method = RequestMethod.GET)
    public BigDecimal getBalance(Principal principal) {return accountDao.getBalanceByUserName(principal.getName());}

    @RequestMapping(path = "/users", method = RequestMethod.GET)
    public List<User> listUsers(){
        return userDao.findAll();
    }


    @RequestMapping(path = "/transfer/{id}/reject", method = RequestMethod.POST)
    public void rejectRequest(@PathVariable("id") Long id){
        userTransferDao.rejectRequest(id);
    }

    @RequestMapping(path = "/transfer/{id}/accept", method = RequestMethod.POST)
    public void acceptRequest(@PathVariable("id") Long id) {
        userTransferDao.acceptRequest(id);
    }

    @RequestMapping(path = "/transfer/new")
    public void createRequestTransfer(@RequestBody UserTransfer userTransfer) {
        userTransferDao.createTransfer(userTransfer);
    }

    @RequestMapping(path = "/transfer", method = RequestMethod.GET)
    public List<UserTransfer> listTransfers(Principal principal) {
        return userTransferDao.getUserTransferListByUserName(principal.getName());
    }

    @RequestMapping(path = "/transfer/pending", method = RequestMethod.GET)
    public List<UserTransfer> listPendingTransfers(Principal principal) {
        return userTransferDao.getPendingUserTransferList(principal.getName());
    }

    @RequestMapping(path = "/transfer/{id}", method = RequestMethod.GET)
    public UserTransfer transfer(@PathVariable("id") long id) {
        return userTransferDao.getUserTransferInfoById(id);
    }

}
