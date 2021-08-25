package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.User;
import com.techelevator.tenmo.model.UserTransfer;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

public class UserService {

    private String BASE_URL;
    private RestTemplate restTemplate = new RestTemplate();
    private String AUTH_TOKEN;

    public UserService(String url) {
        BASE_URL = url+"account";
    }

    public void setAuthToken(String token) { AUTH_TOKEN = token; }

    public BigDecimal getBalance() throws UserServiceException {
        try {
            return restTemplate.exchange(BASE_URL + "/balance", HttpMethod.GET, makeAuthEntity(), BigDecimal.class).getBody();
        } catch (RestClientResponseException e) {
            throw new UserServiceException(e.getRawStatusCode() + " : " + e.getResponseBodyAsString());
        }
    }

    public UserTransfer[] listTransfers() throws UserServiceException {
        UserTransfer[] userTransfers = null;
        try {
            return restTemplate.exchange(BASE_URL + "/transfer", HttpMethod.GET, makeAuthEntity(), UserTransfer[].class).getBody();
        } catch (RestClientResponseException e) {
            throw new UserServiceException(e.getRawStatusCode() + " : " + e.getResponseBodyAsString());
        }
    }

    public UserTransfer[] listPendingTransfers() throws UserServiceException {
        try {
            return restTemplate.exchange(BASE_URL + "/transfer/pending", HttpMethod.GET, makeAuthEntity(), UserTransfer[].class).getBody();
        } catch (RestClientResponseException e) {
            throw new UserServiceException(e.getRawStatusCode() + " : " + e.getResponseBodyAsString());
        }
    }

    public UserTransfer getUserTransferById(long id) throws UserServiceException {
        try {
            return restTemplate.exchange(BASE_URL + "/transfer/" + id, HttpMethod.GET, makeAuthEntity(), UserTransfer.class).getBody();
        } catch (RestClientResponseException e) {
            throw new UserServiceException(e.getRawStatusCode() + " : " + e.getResponseBodyAsString());
        }
    }

    public void acceptTransfer(long id) throws UserServiceException {
        try {
            restTemplate.exchange(BASE_URL + "/transfer/" + id +"/accept", HttpMethod.POST, makeAuthEntity(), Long.class).getBody();
        } catch (RestClientResponseException e) {
            throw new UserServiceException(e.getRawStatusCode() + " : " + e.getResponseBodyAsString());
        }
    }

    public void rejectTransfer(long id) throws UserServiceException {
        try {
            restTemplate.exchange(BASE_URL + "/transfer/" + id +"/reject", HttpMethod.POST, makeAuthEntity(), Long.class).getBody();
        } catch (RestClientResponseException e) {
            throw new UserServiceException(e.getRawStatusCode() + " : " + e.getResponseBodyAsString());
        }
    }


    public User[] getUsers() throws UserServiceException {
        User[] users = null;
        try {
            return restTemplate.exchange(BASE_URL + "/users", HttpMethod.GET, makeAuthEntity(), User[].class).getBody();
        } catch (RestClientResponseException e) {
            throw new UserServiceException(e.getRawStatusCode() + " : " + e.getResponseBodyAsString());
        }
    }

    public UserTransfer createTransfer(String sendingUserName, String receivingUserName, BigDecimal amount, String transferType) throws UserServiceException {
        UserTransfer userTransfer = new UserTransfer();

        userTransfer.setAccountFromUserName(sendingUserName);
        userTransfer.setAccountToUserName(receivingUserName);
        userTransfer.setAmount(amount);
        userTransfer.setTransferTypeDesc(transferType);
        if (transferType.equals("Send")) userTransfer.setTransferStatusDesc("Approved");
        else userTransfer.setTransferStatusDesc("Pending");

        try {
            HttpEntity<UserTransfer> userTransferEntity = makeUserTransferEntity(userTransfer);
            userTransfer = restTemplate.exchange(BASE_URL + "/transfer/new", HttpMethod.POST, userTransferEntity, UserTransfer.class).getBody();
        } catch (RestClientResponseException e) {
            throw new UserServiceException(e.getRawStatusCode() + " : " + e.getResponseBodyAsString());
        }
        return userTransfer;
    }

    private HttpEntity<String> makeStatusId(String string) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(AUTH_TOKEN);
        HttpEntity<String> entity = new HttpEntity<>(string, headers);
        if (entity == null) System.out.println("Entity is Null");
        return entity;
    }

        private HttpEntity<UserTransfer> makeUserTransferEntity(UserTransfer userTransfer) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(AUTH_TOKEN);
        HttpEntity<UserTransfer> entity = new HttpEntity<>(userTransfer, headers);
        if (entity == null) System.out.println("Entity is Null");
        return entity;
    }

    private HttpEntity makeAuthEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(AUTH_TOKEN);
        HttpEntity entity = new HttpEntity<>(headers);
        return entity;
    }
}
