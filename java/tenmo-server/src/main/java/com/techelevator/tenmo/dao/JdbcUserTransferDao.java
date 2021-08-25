package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.UserTransfer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcUserTransferDao implements UserTransferDao {

    private JdbcTemplate jdbcTemplate;
    @Autowired
    private AccountDao accountDao;

    public JdbcUserTransferDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    UserTransfer userTransfer = new UserTransfer();
    @Override
    public UserTransfer getUserTransferInfoById(long transferId) {
        UserTransfer userTransfer = null;
        String sql = "SELECT transfer_id, transfer_type_id, transfer_status_id, transfers.amount, \n" +
                "(SELECT username FROM transfers \n" +
                "JOIN accounts ON account_id = transfers.account_from \n" +
                "JOIN users ON accounts.user_id = users.user_id WHERE transfer_id = ?) AS from_username,\n" +
                "(SELECT users.username FROM transfers \n" +
                "JOIN accounts ON account_id = transfers.account_to\n" +
                "JOIN users ON accounts.user_id = users.user_id WHERE transfer_id = ?) AS to_username \n" +
                "FROM transfers WHERE transfer_id = ?;";
        SqlRowSet result = jdbcTemplate.queryForRowSet(sql, transferId, transferId, transferId);
        if (result.next()) {
            userTransfer = mapRowsToUserTransfer(result);
        }
        return userTransfer;
    }

    public List<UserTransfer> getUserTransferListByUserName(String userName) {
        List<UserTransfer> userTransfers = new ArrayList<>();
        String sql = "SELECT transfers.transfer_id, transfers.transfer_type_id, transfers.transfer_status_id, " +
                "transfers.transfer_type_id, transfers.amount, users.username FROM transfers\n" +
                "JOIN accounts ON account_id = transfers.account_from\n" +
                "JOIN users ON accounts.user_id = users.user_id\n" +
                "WHERE users.userName = ?";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, userName);
        while (results.next()) {
            sql = "SELECT username FROM transfers\n" +
                    "JOIN accounts ON account_id = transfers.account_to\n" +
                    "JOIN users ON accounts.user_id = users.user_id\n" +
                    "WHERE transfers.transfer_id= ?;";
            String toUserName = jdbcTemplate.queryForObject(sql, String.class, results.getLong("transfer_id"));
            userTransfers.add(mapRowsToUserTransfer(results, toUserName, false));
        }
        sql = "SELECT transfers.transfer_id, transfers.transfer_type_id, transfers.transfer_status_id, " +
                "transfers.transfer_type_id, transfers.amount, users.username FROM transfers\n" +
                "JOIN accounts ON account_id = transfers.account_to\n" +
                "JOIN users ON accounts.user_id = users.user_id\n" +
                "WHERE users.userName = ?";
        results = jdbcTemplate.queryForRowSet(sql, userName);
        while (results.next()) {
            sql = "SELECT username FROM transfers\n" +
                    "JOIN accounts ON account_id = transfers.account_from\n" +
                    "JOIN users ON accounts.user_id = users.user_id\n" +
                    "WHERE transfers.transfer_id= ?;";
            String fromUserName = jdbcTemplate.queryForObject(sql, String.class, results.getLong("transfer_id"));
            userTransfers.add(mapRowsToUserTransfer(results, fromUserName, true));
        }
        return userTransfers;
    }

    public List<UserTransfer> getPendingUserTransferList(String userName) {
        List<UserTransfer> userTransfers = new ArrayList<>();
        String sql = "SELECT transfers.transfer_id, transfers.transfer_type_id, transfers.transfer_status_id, " +
                "transfers.transfer_type_id, transfers.amount, users.username FROM transfers\n" +
                "JOIN accounts ON account_id = transfers.account_to\n" +
                "JOIN users ON accounts.user_id = users.user_id\n" +
                "WHERE users.userName = ? AND transfers.transfer_status_id = (SELECT transfer_status_id FROM " +
                "transfer_statuses WHERE transfer_status_desc = 'Pending');";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, userName);
        while (results.next()) {
            sql = "SELECT username FROM transfers\n" +
                    "JOIN accounts ON account_id = transfers.account_from\n" +
                    "JOIN users ON accounts.user_id = users.user_id\n" +
                    "WHERE transfers.transfer_id= ?;";
            String fromUserName = jdbcTemplate.queryForObject(sql, String.class, results.getLong("transfer_id"));
            userTransfers.add(mapRowsToUserTransfer(results, fromUserName, true));
        }
        return userTransfers;
    }

    public void createTransfer(UserTransfer userTransfer) throws RuntimeException {
        userTransfer.setTransferStatusId(getTransferStatusId(userTransfer.getTransferStatusDesc()));
        userTransfer.setTransferTypeId(getTransferTypeId(userTransfer.getTransferTypeDesc()));
        Long fromAccountNo = accountDao.getAccountByUserName(userTransfer.getAccountFromUserName()).getId();
        Long toAccountNo = accountDao.getAccountByUserName(userTransfer.getAccountToUserName()).getId();
        if (userTransfer.getTransferTypeDesc().equals("Send") &&
                accountDao.getBalanceByUserName(userTransfer.getAccountFromUserName()).subtract(userTransfer.getAmount()).compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException();
        }
        else {
            String sql = "INSERT INTO transfers (transfer_type_id, transfer_status_id, account_from, account_to, amount) " +
                    "VALUES (?,?,?,?,?) RETURNING transfer_id;";
            long id = jdbcTemplate.queryForObject(sql, long.class, userTransfer.getTransferTypeId(), userTransfer.getTransferStatusId(),
                    fromAccountNo, toAccountNo, userTransfer.getAmount());
            if (id == 0) throw new RuntimeException();
            else {
                if (userTransfer.getTransferTypeDesc().equals("Send") && userTransfer.getTransferStatusDesc().equals("Approved")) {
                    accountDao.updateAccountBalances(fromAccountNo, toAccountNo, userTransfer.getAmount());
                }

            }
        }

    }

    public void rejectRequest(long transferId) {
        UserTransfer userTransfer = getUserTransferInfoById(transferId);
        userTransfer.setTransferStatusDesc("Rejected");
        userTransfer.setTransferStatusId(getTransferStatusId(userTransfer.getTransferStatusDesc()));
        String sql = "UPDATE transfers SET transfer_status_id = ? WHERE transfer_id = ?";
        jdbcTemplate.update(sql, userTransfer.getTransferStatusId(), transferId);
    }

    public void acceptRequest(long transferId) throws RuntimeException {
        UserTransfer userTransfer = getUserTransferInfoById(transferId);
        long acceptingAccountNo = accountDao.getAccountByUserName(userTransfer.getAccountToUserName()).getId();
        long requestingAccountNo = accountDao.getAccountByUserName(userTransfer.getAccountFromUserName()).getId();
        if (accountDao.getBalanceByUserName(userTransfer.getAccountToUserName()).subtract(userTransfer.getAmount()).compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException();
        }
        else {
            userTransfer.setTransferStatusDesc("Approved");
            userTransfer.setTransferStatusId(getTransferStatusId(userTransfer.getTransferStatusDesc()));
            String sql = "UPDATE transfers SET transfer_status_id = ? WHERE transfer_id = ?";
            jdbcTemplate.update(sql, userTransfer.getTransferStatusId(), transferId);
            accountDao.updateAccountBalances(acceptingAccountNo, requestingAccountNo, userTransfer.getAmount());
        }
    }

    public long getTransferStatusId(String transferStatusDesc) {
        String sql = "SELECT transfer_status_id FROM transfer_statuses " +
                "WHERE transfer_status_desc = ?";
        return jdbcTemplate.queryForObject(sql, Long.class, transferStatusDesc);
    }

    public String getTransferStatusDesc(long transferStatusId) {
        String sql = "SELECT transfer_status_desc FROM transfer_statuses " +
                "WHERE transfer_status_id = ?";
        return jdbcTemplate.queryForObject(sql, String.class, transferStatusId);
    }

    public long getTransferTypeId(String transferTypeDesc) {
        String sql = "SELECT transfer_type_id FROM transfer_types " +
                "WHERE transfer_type_desc = ?";
        return jdbcTemplate.queryForObject(sql, Long.class, transferTypeDesc);
    }

    public String getTransferTypeDesc(long transferTypeId) {
        String sql = "SELECT transfer_type_desc FROM transfer_types " +
                "WHERE transfer_type_id = ?";
        return jdbcTemplate.queryForObject(sql, String.class, transferTypeId);
    }

    public UserTransfer mapRowsToUserTransfer(SqlRowSet rowSet) {
        UserTransfer userTransfer = new UserTransfer();
        userTransfer.setTransferId(rowSet.getLong("transfer_id"));
        userTransfer.setTransferStatusId(rowSet.getLong("transfer_status_id"));
        userTransfer.setTransferStatusDesc(getTransferStatusDesc(userTransfer.getTransferStatusId()));
        userTransfer.setTransferTypeId(rowSet.getLong("transfer_type_id"));
        userTransfer.setTransferTypeDesc(getTransferTypeDesc(userTransfer.getTransferTypeId()));
        userTransfer.setAccountFromUserName(rowSet.getString("from_username"));
        userTransfer.setAmount(rowSet.getBigDecimal("amount"));
        userTransfer.setAccountToUserName(rowSet.getString("to_username"));
        return userTransfer;
    }

    public UserTransfer mapRowsToUserTransfer(SqlRowSet rowSet, String otherName, boolean isFrom) {
        UserTransfer userTransfer = new UserTransfer();
        userTransfer.setTransferId(rowSet.getLong("transfer_id"));
        userTransfer.setTransferStatusId(rowSet.getLong("transfer_status_id"));
        userTransfer.setTransferStatusDesc(getTransferStatusDesc(userTransfer.getTransferStatusId()));
        userTransfer.setTransferTypeId(rowSet.getLong("transfer_type_id"));
        userTransfer.setTransferTypeDesc(getTransferTypeDesc(userTransfer.getTransferTypeId()));
        if (!isFrom) {
            userTransfer.setAccountFromUserName(rowSet.getString("username"));
            userTransfer.setAccountToUserName(otherName);
        }
        else {
            userTransfer.setAccountFromUserName(otherName);
            userTransfer.setAccountToUserName(rowSet.getString("username"));
        }
        userTransfer.setAmount(rowSet.getBigDecimal("amount"));
        return userTransfer;
    }

}
