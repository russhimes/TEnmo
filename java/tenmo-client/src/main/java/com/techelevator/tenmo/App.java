package com.techelevator.tenmo;

//TESTING FORKING IN GIT, PLEASE IGNORE IF THIS ENDS UP IN MAIN

import com.techelevator.tenmo.model.*;
import com.techelevator.tenmo.services.AuthenticationService;
import com.techelevator.tenmo.services.AuthenticationServiceException;
import com.techelevator.tenmo.services.UserService;
import com.techelevator.tenmo.services.UserServiceException;
import com.techelevator.view.ConsoleService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

public class App {

private static final String API_BASE_URL = "http://localhost:8080/";
    
    private static final String MENU_OPTION_EXIT = "Exit";
    private static final String LOGIN_MENU_OPTION_REGISTER = "Register";
	private static final String LOGIN_MENU_OPTION_LOGIN = "Login";
	private static final String[] LOGIN_MENU_OPTIONS = { LOGIN_MENU_OPTION_REGISTER, LOGIN_MENU_OPTION_LOGIN, MENU_OPTION_EXIT };
	private static final String MAIN_MENU_OPTION_VIEW_BALANCE = "View your current balance";
	private static final String MAIN_MENU_OPTION_SEND_BUCKS = "Send TE bucks";
	private static final String MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS = "View your past transfers";
	private static final String MAIN_MENU_OPTION_REQUEST_BUCKS = "Request TE bucks";
	private static final String MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS = "View your pending requests";
	private static final String MAIN_MENU_OPTION_LOGIN = "Login as different user";
	private static final String[] MAIN_MENU_OPTIONS = { MAIN_MENU_OPTION_VIEW_BALANCE, MAIN_MENU_OPTION_SEND_BUCKS, MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS, MAIN_MENU_OPTION_REQUEST_BUCKS, MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS, MAIN_MENU_OPTION_LOGIN, MENU_OPTION_EXIT };
	
    private AuthenticatedUser currentUser;
    private ConsoleService console;
    private AuthenticationService authenticationService;
    private UserService userService;
    private RestTemplate restTemplate;

    public static void main(String[] args) {
    	App app = new App(new ConsoleService(System.in, System.out), new AuthenticationService(API_BASE_URL), new UserService(API_BASE_URL));
    	app.run();
    }

    public App(ConsoleService console, AuthenticationService authenticationService, UserService userService) {
		this.console = console;
		this.authenticationService = authenticationService;
		this.userService = userService;
		this.restTemplate = new RestTemplate();
	}

	public void run() {
		System.out.println("*********************");
		System.out.println("* Welcome to TEnmo! *");
		System.out.println("*********************");
		
		registerAndLogin();
		mainMenu();
	}

	private void mainMenu() {
		while(true) {
			String choice = (String)console.getChoiceFromOptions(MAIN_MENU_OPTIONS);
			if(MAIN_MENU_OPTION_VIEW_BALANCE.equals(choice)) {
				viewCurrentBalance();
			} else if(MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS.equals(choice)) {
				try {
					viewTransferHistory();
				} catch (UserServiceException e) {
					e.printStackTrace();
				}
			} else if(MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS.equals(choice)) {
				try {
					viewPendingRequests();
				} catch (UserServiceException e) {
					e.printStackTrace();
				}
			} else if(MAIN_MENU_OPTION_SEND_BUCKS.equals(choice)) {
				sendBucks();
			} else if(MAIN_MENU_OPTION_REQUEST_BUCKS.equals(choice)) {
				requestBucks();
			} else if(MAIN_MENU_OPTION_LOGIN.equals(choice)) {
				login();
			} else {
				// the only other option on the main menu is to exit
				exitProgram();
			}
		}
	}

	private void viewCurrentBalance() {

		try {
			System.out.println("Your current account balance is: $" +userService.getBalance());
		} catch (UserServiceException e) {
			System.out.println(e.getMessage());
		}
	}

	private void viewTransferHistory() throws UserServiceException {
		UserTransfer[] userTransfers = null;

		try {
			userTransfers = userService.listTransfers();
		} catch (UserServiceException e) {
			System.out.println(e.getMessage());
		}

			System.out.println("-------------------------------------------\n" +
				"Transfers\n" +
				"ID\t\t\tFrom/To  \t\t\tAmount\n" +
				"-------------------------------------------\n");
		for(UserTransfer userTransfer : userTransfers) {
			System.out.print(userTransfer.getTransferId() + "        ");
			if (userTransfer.getAccountFromUserName().equals(currentUser.getUser().getUsername())) {
				if (userTransfer.getAccountToUserName().length() < 4)
					System.out.print("To:    " + userTransfer.getAccountToUserName() + "\t\t\t$");
				else System.out.print("To:    " + userTransfer.getAccountToUserName() + "\t\t$");
			} else {
				if (userTransfer.getAccountFromUserName().length() < 4) {
					System.out.print("From:  " + userTransfer.getAccountFromUserName() + "\t\t\t$");
				}
				else System.out.print("From:  " + userTransfer.getAccountFromUserName() + "\t\t$");

			}
			System.out.println(userTransfer.getAmount());
		}
		boolean isGoodInput = false;
		while (!isGoodInput) {
			System.out.print("Please enter transfer ID to view details (0 to cancel): ");
			Scanner inputScanner = new Scanner(System.in);
			try {
				long transferId = Long.parseLong(inputScanner.nextLine());
				if(transferId == 0){
					mainMenu();
				}
				UserTransfer userTransfer = null;
				for (UserTransfer option : userTransfers) {
					if (option.getTransferId() == transferId) {
						userTransfer = option;
						break;
					}
				}
				if (userTransfer == null) throw new UserServiceException("No message");
				System.out.println("--------------------------------------------\n" +
						"Transfer Details\n" +
						"--------------------------------------------");
				System.out.println("Id: " + userTransfer.getTransferId());
				System.out.println("From: " + userTransfer.getAccountFromUserName());
				System.out.println("To: " + userTransfer.getAccountToUserName());
				System.out.println("Type: " + userTransfer.getTransferTypeDesc());
				System.out.println("Status: " + userTransfer.getTransferStatusDesc());
				System.out.println("Amount: $" + userTransfer.getAmount());
				isGoodInput = true;
			} catch (NumberFormatException e) {
				System.out.println("Please enter a valid transfer ID");
			} catch (UserServiceException e) {
				if (e.getMessage().contains("No message")) System.out.println("Please enter a valid transfer ID");
				else {
					System.out.println(e.getMessage());
					mainMenu();
				}
			}
		}

    }

	private void viewPendingRequests() throws UserServiceException {
		UserTransfer[] pendingTransfers = null;
		long transferId = 0;
		UserTransfer userTransfer = null;
		try {
			pendingTransfers = userService.listPendingTransfers();
		} catch (UserServiceException e) {
			System.out.println(e.getMessage());
			mainMenu();
		}
		System.out.println("-------------------------------------------\n" +
				"Transfers\n" +
				"ID\t\t\tFrom  \t\t\tAmount\n" +
				"-------------------------------------------");
		for(UserTransfer option : pendingTransfers) {
			System.out.print(option.getTransferId() + "        ");
			if (option.getAccountFromUserName().length() < 4) {
				System.out.print("From:  " + option.getAccountFromUserName() + "\t\t\t$");
			} else System.out.print("From:  " + option.getAccountFromUserName() + "\t\t$");
			System.out.println(option.getAmount());
		}
		boolean isGoodInput = false;
		while (!isGoodInput) {
			System.out.print("\nPlease enter transfer ID to view details (0 to cancel): ");
			Scanner inputScanner = new Scanner(System.in);
			try {
				transferId = Long.parseLong(inputScanner.nextLine());
				if (transferId == 0) {
					mainMenu();
				}
				for (UserTransfer option : pendingTransfers)  {
					if (option.getTransferId() == transferId) {
						userTransfer = option;
						break;
					}
				}
				if (userTransfer == null) throw new UserServiceException("No message");
				System.out.println("--------------------------------------------\n" +
						"Transfer Details\n" +
						"--------------------------------------------");
				System.out.println("Id: " + userTransfer.getTransferId());
				System.out.println("From: " + userTransfer.getAccountFromUserName());
				System.out.println("To: " + userTransfer.getAccountToUserName());
				System.out.println("Type: " + userTransfer.getTransferTypeDesc());
				System.out.println("Status: " + userTransfer.getTransferStatusDesc());
				System.out.println("Amount: $" + userTransfer.getAmount());
				isGoodInput = true;
			} catch (NumberFormatException e) {
				System.out.println("Please enter a valid transfer ID");
			} catch (UserServiceException e) {
				if (e.getMessage().contains("No message")) System.out.println("Please enter a valid transfer ID");
				else {
					System.out.println(e.getMessage());
					mainMenu();
				}
			}
		}
		isGoodInput = false;
		while (!isGoodInput) {
			System.out.print("1: Approve\n" +
						"2: Reject\n" +
						"0: Don't approve or reject\n" +
						"---------\n" +
						"Please choose an option: ");

			try {
				Scanner inputScanner = new Scanner(System.in);
				int choice = Integer.parseInt(inputScanner.nextLine());
				if (choice == 1) {
					userService.acceptTransfer(transferId);
					isGoodInput = true;
				}
				else if (choice == 2) {
					userService.rejectTransfer(transferId);
					isGoodInput = true;
				}
				else if (choice == 0) break;
				else System.out.println("Please enter a valid choice");
			} catch (UserServiceException e) {
				if (e.getMessage().contains("No message")) {
					System.out.println(e.getMessage());
					System.out.println("Please enter a valid choice");
				}
				else {
					System.out.println(e.getMessage());
					mainMenu();
				}
		}
		}
	}

	private void sendBucks() {
		boolean isGoodInput = false;
		while (!isGoodInput) {
			try {
				System.out.println("-------------------------------------------\n" +
						"Users\n" +
						"ID          Name\n" +
						"-------------------------------------------");
				User[] users = userService.getUsers();
				User choice = null;
				for (User user : users) {
					System.out.println(user.getId() + "        " + user.getUsername());
				}
				System.out.print("----------\n\n" +
						"Enter ID of user you are sending to (0 to cancel): ");
				Scanner inputScanner = new Scanner(System.in);
				long receivingId = Long.parseLong(inputScanner.nextLine());
				if(receivingId == 0){
					mainMenu();
				}
				for (User user : users) {
					if (user.getId() == receivingId) {
						choice = user;
						System.out.println(choice.getUsername());
						break;
					}
				}
				if (choice == null) {
					throw new UserServiceException("No message");
				}
				System.out.print("Enter amount: ");
				BigDecimal amount = new BigDecimal(inputScanner.nextLine());
				userService.createTransfer(currentUser.getUser().getUsername(), choice.getUsername(), amount, "Send");
				isGoodInput = true;
			} catch (UserServiceException e) {
				if (e.getMessage().contains("No message")) System.out.println("Please provide a userId");
				else {
					System.out.println(e.getMessage());
					mainMenu();
				}
			} catch (NumberFormatException e) {
				System.out.println("Please provide a userId");
			}
		}
	}

	private void requestBucks() {
		boolean isGoodInput = false;
		while (!isGoodInput) {
			try {
				System.out.println("-------------------------------------------\n" +
						"Users\n" +
						"ID          Name\n" +
						"-------------------------------------------");
				User[] users = userService.getUsers();
				for (User user : users) {
					System.out.println(user.getId() + "        " + user.getUsername());
				}
				System.out.print("----------\n\n" +
						"Enter ID of user you are requesting from (0 to cancel): ");
				Scanner inputScanner = new Scanner(System.in);
				long receivingId = Long.parseLong(inputScanner.nextLine());
				if(receivingId == 0){
					mainMenu();
				}
				User choice = null;
				for (User user : users) {
					if (user.getId() == receivingId) {
						choice = user;
						break;
					}
				}
				if (choice == null) throw new UserServiceException("No message");
				System.out.print("Enter amount: ");
				BigDecimal amount = new BigDecimal(inputScanner.nextLine());
				userService.createTransfer(currentUser.getUser().getUsername(), choice.getUsername(), amount, "Request");
				isGoodInput = true;
			} catch (UserServiceException e) {
				if (e.getMessage().contains("No message")) System.out.println("Please provide a userId");
				else {
					System.out.println(e.getMessage());
					mainMenu();
				}
			} catch (NumberFormatException e) {
				System.out.println("Please provide a userId");
			}
		}
	}


	
	private void exitProgram() {
		System.exit(0);
	}

	private void registerAndLogin() {
		while(!isAuthenticated()) {
			String choice = (String)console.getChoiceFromOptions(LOGIN_MENU_OPTIONS);
			if (LOGIN_MENU_OPTION_LOGIN.equals(choice)) {
				login();
			} else if (LOGIN_MENU_OPTION_REGISTER.equals(choice)) {
				register();
			} else {
				// the only other option on the login menu is to exit
				exitProgram();
			}
		}
	}

	private boolean isAuthenticated() {
		return currentUser != null;
	}

	private void register() {
		System.out.println("Please register a new user account");
		boolean isRegistered = false;
        while (!isRegistered) //will keep looping until user is registered
        {
            UserCredentials credentials = collectUserCredentials();
            try {
            	authenticationService.register(credentials);
            	isRegistered = true;
            	System.out.println("Registration successful. You can now login.");
            } catch(AuthenticationServiceException e) {
            	System.out.println("REGISTRATION ERROR: "+e.getMessage());
				System.out.println("Please attempt to register again.");
            }
        }
	}

	private void login() {
		System.out.println("Please log in");
		currentUser = null;
		while (currentUser == null) //will keep looping until user is logged in
		{
			UserCredentials credentials = collectUserCredentials();
		    try {
				currentUser = authenticationService.login(credentials);
				userService.setAuthToken(currentUser.getToken());
			} catch (AuthenticationServiceException e) {
				System.out.println("LOGIN ERROR: "+e.getMessage());
				System.out.println("Please attempt to login again.");
			}
		}
	}
	
	private UserCredentials collectUserCredentials() {
		String username = console.getUserInput("Username");
		String password = console.getUserInput("Password");
		return new UserCredentials(username, password);
	}
}
