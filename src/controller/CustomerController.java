package controller;

import model.Customer;
import model.enums.AccountStatus;
import model.enums.CustTier;
import repository.CustomerRepository;

public class CustomerController {
    private final CustomerRepository customerRepo;

    /**
     * Default constructor using the production file path.
     */
    public CustomerController() {
        this.customerRepo = new CustomerRepository("data/customers.csv");
    }

    /**
     * Constructor for testing or custom file paths.
     * @param filePath Path to the customers CSV file
     */
    public CustomerController(String filePath) {
        this.customerRepo = new CustomerRepository(filePath);
    }

    /**
     * Registers a new customer after checking for duplicate email.
     * Generates a new ID in the format CXXXXX.
     * Default tier is BRONZE, status is APPROVED.
     * 
     * @param name Customer name
     * @param email Customer email
     * @param password Customer password
     * @return The registered Customer object
     * @throws IllegalArgumentException if the email is already registered or inputs are invalid
     */
    public Customer register(String name, String email, String password) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty!");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty!");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty!");
        }

        // Check duplicate email
        if (customerRepo.getByEmail(email) != null) {
            throw new IllegalArgumentException("Email '" + email + "' is already registered!");
        }

        // Generate next ID
        int maxNum = 0;
        for (Customer c : customerRepo.getAll()) {
            String id = c.getId();
            if (id != null && id.startsWith("C")) {
                try {
                    int num = Integer.parseInt(id.substring(1));
                    if (num > maxNum) {
                        maxNum = num;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        String newId = String.format("C%05d", maxNum + 1);

        Customer newCust = new Customer(newId, name.trim(), email.trim(), password, AccountStatus.APPROVED, CustTier.BRONZE);
        customerRepo.add(newCust);
        return newCust;
    }

    /**
     * Authenticates a customer.
     * Only allows logging in if credentials are correct and status is APPROVED.
     * 
     * @param email Customer email
     * @param password Customer password
     * @return The Customer object if login is successful, null otherwise
     */
    public Customer login(String email, String password) {
        if (email == null || password == null) {
            return null;
        }
        Customer c = customerRepo.authenticate(email, password);
        if (c != null && c.getStatus() == AccountStatus.APPROVED) {
            return c;
        }
        return null;
    }

    /**
     * Gets the customer repo (useful for tests/admin view).
     * @return CustomerRepository
     */
    public CustomerRepository getCustomerRepo() {
        return customerRepo;
    }
}
