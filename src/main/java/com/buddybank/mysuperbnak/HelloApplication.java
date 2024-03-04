package com.buddybank.mysuperbnak;

import javafx.application.Application;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class HelloApplication extends Application {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/deneme";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "12345678";

    private Stage primaryStage;
    private Scene customerScene;
    private Scene accountScene;
    private VBox customerPage;
    private VBox accountPage;
    private Customer currentCustomer;
    private Account currentAccount;
    private TableView<Customer> customerTable;
    private TableView<Account> accountTable;

    private ObservableList<Customer> customers = FXCollections.observableArrayList();
    private ObservableList<Account> accounts = FXCollections.observableArrayList();
    private Map<Integer, ObservableList<Account>> customerAccountsMap = new HashMap<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        // Veritabanı bağlantısını aç
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // Migration işlemlerini gerçekleştir
            migrateDatabase(connection);

            // Müşteri ve hesap verilerini yükle
            loadCustomersFromDatabase(connection);
            loadAccountsFromDatabase(connection);

            // Sayfaları oluştur
            createCustomerPage();
            createAccountPage();

            // Ana sayfayı göster
            primaryStage.setScene(customerScene);
            primaryStage.setTitle("Customer Management System");
            primaryStage.show();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database connection error!");
        }
    }

    private void migrateDatabase(Connection connection) throws SQLException {
        // Migration işlemleri burada gerçekleştirilecek
        // Örneğin, gerekli tabloların oluşturulması gibi
        String createCustomerTableSQL = "CREATE TABLE IF NOT EXISTS customers (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "name VARCHAR(255)," +
                "surname VARCHAR(255)," +
                "email VARCHAR(255)," +
                "phone VARCHAR(20)," +
                "address VARCHAR(255)" +
                ")";
        String createAccountTableSQL = "CREATE TABLE IF NOT EXISTS accounts (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "type VARCHAR(50)," +
                "balance DOUBLE," +
                "customer_id INT," +
                "FOREIGN KEY (customer_id) REFERENCES customers(id)" +
                ")";
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(createCustomerTableSQL);
            statement.executeUpdate(createAccountTableSQL);
        }
    }

    private void loadCustomersFromDatabase(Connection connection) throws SQLException {
        String selectCustomersSQL = "SELECT * FROM customers";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(selectCustomersSQL)) {
            while (resultSet.next()) {
                Customer customer = new Customer(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getString("surname"),
                        resultSet.getString("email"),
                        resultSet.getString("phone"),
                        resultSet.getString("address")
                );
                customers.add(customer);
            }
        }
    }

    private void loadAccountsFromDatabase(Connection connection) throws SQLException {
        String selectAccountsSQL = "SELECT * FROM accounts";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(selectAccountsSQL)) {
            while (resultSet.next()) {
                Account account = new Account(
                        resultSet.getInt("id"),
                        resultSet.getString("type"),
                        resultSet.getDouble("balance"),
                        resultSet.getInt("customer_id")
                );
                accounts.add(account);
                ObservableList<Account> customerAccounts = customerAccountsMap.get(account.getCustomerId());
                if (customerAccounts != null) {
                    customerAccounts.add(account);
                } else {
                    customerAccounts = FXCollections.observableArrayList();
                    customerAccounts.add(account);
                    customerAccountsMap.put(account.getCustomerId(), customerAccounts);
                }
            }
        }
    }

    private void createCustomerPage() {
        customerPage = new VBox(10);
        customerPage.setPadding(new Insets(10));

        // Input alanları
        TextField idField = new TextField();
        idField.setPromptText("ID");
        TextField nameField = new TextField();
        nameField.setPromptText("Name");
        TextField surnameField = new TextField();
        surnameField.setPromptText("Surname");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone");
        TextField addressField = new TextField();
        addressField.setPromptText("Address");

        // Fetch button
        Button fetchButton = new Button("Fetch");
        fetchButton.setOnAction(e -> {
            if (!idField.getText().isEmpty()) {
                int id;
                try {
                    id = Integer.parseInt(idField.getText());
                } catch (NumberFormatException ex) {
                    showAlert("Please enter a valid customer ID.");
                    return;
                }
                currentCustomer = customers.stream()
                        .filter(c -> c.getId() == id)
                        .findFirst()
                        .orElse(null);
                if (currentCustomer != null) {
                    nameField.setText(currentCustomer.getName());
                    surnameField.setText(currentCustomer.getSurname());
                    emailField.setText(currentCustomer.getEmail());
                    phoneField.setText(currentCustomer.getPhone());
                    addressField.setText(currentCustomer.getAddress());
                    accountTable.setItems(currentCustomer.getAccounts());
                } else {
                    clearCustomerFields();
                }
            } else {
                showAlert("Please enter a valid customer ID.");
            }
        });

        // Add, Update, Delete buttons
        Button addButton = new Button("Add");
        addButton.setOnAction(e -> addCustomer(nameField.getText(), surnameField.getText(), emailField.getText(), phoneField.getText(), addressField.getText()));

        Button updateButton = new Button("Update");
        updateButton.setOnAction(e -> updateCustomer(idField.getText(), nameField.getText(), surnameField.getText(), emailField.getText(), phoneField.getText(), addressField.getText()));

        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(e -> deleteCustomer(idField.getText()));

        // Show All button
        Button showAllButton = new Button("Show All");
        showAllButton.setOnAction(e -> customerTable.setItems(customers));

        // Tablo
        customerTable = new TableView<>(customers);
        TableColumn<Customer, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        TableColumn<Customer, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        TableColumn<Customer, String> surnameCol = new TableColumn<>("Surname");
        surnameCol.setCellValueFactory(cellData -> cellData.getValue().surnameProperty());
        TableColumn<Customer, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(cellData -> cellData.getValue().emailProperty());
        TableColumn<Customer, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(cellData -> cellData.getValue().phoneProperty());
        TableColumn<Customer, String> addressCol = new TableColumn<>("Address");
        addressCol.setCellValueFactory(cellData -> cellData.getValue().addressProperty());
        customerTable.getColumns().addAll(idCol, nameCol, surnameCol, emailCol, phoneCol, addressCol);

        // Navigasyon alanı
        HBox navigationBar = createNavigationBar(true);
        customerPage.getChildren().addAll(
                navigationBar,
                new Label("Customer Information"),
                new HBox(10, idField, fetchButton),
                nameField,
                surnameField,
                emailField,
                phoneField,
                addressField,
                new HBox(10, addButton, updateButton, deleteButton, showAllButton),
                customerTable
        );

        customerScene = new Scene(new BorderPane(customerPage), 800, 400);
    }

    private HBox createNavigationBar(boolean isCustomerPage) {
        HBox navigationBar = new HBox(10);
        Button customersButton = new Button("Customers");
        Button accountsButton = new Button("Accounts");
        if (isCustomerPage) {
            customersButton.setDisable(true);
            customersButton.setStyle("-fx-background-color: #cccccc;");
            customersButton.setOnAction(e -> primaryStage.setScene(customerScene));
            accountsButton.setOnAction(e -> primaryStage.setScene(accountScene));
        } else {
            accountsButton.setDisable(true);
            accountsButton.setStyle("-fx-background-color: #cccccc;");
            accountsButton.setOnAction(e -> primaryStage.setScene(accountScene));
            customersButton.setOnAction(e -> primaryStage.setScene(customerScene));
        }
        navigationBar.getChildren().addAll(customersButton, accountsButton);
        return navigationBar;
    }

    private void createAccountPage() {
        accountPage = new VBox(10);
        accountPage.setPadding(new Insets(10));

        // Input alanları
        TextField accountIdField = new TextField();
        accountIdField.setPromptText("Account ID");
        TextField accountTypeField = new TextField();
        accountTypeField.setPromptText("Account Type");
        TextField balanceField = new TextField();
        balanceField.setPromptText("Balance");
        TextField customerIdField = new TextField();
        customerIdField.setPromptText("Customer ID");

        // Fetch button
        Button fetchButton = new Button("Fetch");
        fetchButton.setOnAction(e -> {
            if (!accountIdField.getText().isEmpty()) {
                int id;
                try {
                    id = Integer.parseInt(accountIdField.getText());
                } catch (NumberFormatException ex) {
                    showAlert("Please enter a valid account ID.");
                    return;
                }
                currentAccount = accounts.stream()
                        .filter(a -> a.getId() == id)
                        .findFirst()
                        .orElse(null);
                if (currentAccount != null) {
                    accountTypeField.setText(currentAccount.getType());
                    balanceField.setText(String.valueOf(currentAccount.getBalance()));
                    customerIdField.setText(String.valueOf(currentAccount.getCustomerId()));
                    currentCustomer = customers.stream()
                            .filter(c -> c.getId() == currentAccount.getCustomerId())
                            .findFirst()
                            .orElse(null);
                    if (currentCustomer != null) {
                        customerTable.getSelectionModel().select(currentCustomer);
                        accountTable.setItems(currentCustomer.getAccounts());
                    }
                } else {
                    clearAccountFields();
                }
            } else {
                showAlert("Please enter a valid account ID.");
            }
        });

        // Add, Update, Delete buttons
        Button addButton = new Button("Add");
        addButton.setOnAction(e -> addAccount(accountTypeField.getText(), Double.parseDouble(balanceField.getText()), Integer.parseInt(customerIdField.getText())));

        Button updateButton = new Button("Update");
        updateButton.setOnAction(e -> updateAccount(Integer.parseInt(accountIdField.getText()), accountTypeField.getText(), Double.parseDouble(balanceField.getText()), Integer.parseInt(customerIdField.getText())));

        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(e -> deleteAccount(Integer.parseInt(accountIdField.getText())));

        // Show All button
        Button showAllButton = new Button("Show All");
        showAllButton.setOnAction(e -> accountTable.setItems(accounts));

        // Tablo
        accountTable = new TableView<>();
        TableColumn<Account, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        TableColumn<Account, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
        TableColumn<Account, Double> balanceCol = new TableColumn<>("Balance");
        balanceCol.setCellValueFactory(cellData -> cellData.getValue().balanceProperty().asObject());
        accountTable.getColumns().addAll(idCol, typeCol, balanceCol);

        // Navigasyon alanı
        HBox navigationBar = createNavigationBar(false);
        accountPage.getChildren().addAll(
                navigationBar,
                new Label("Account Information"),
                new HBox(10, accountIdField, fetchButton),
                accountTypeField,
                balanceField,
                customerIdField,
                new HBox(10, addButton, updateButton, deleteButton, showAllButton),
                accountTable
        );

        accountScene = new Scene(new BorderPane(accountPage), 600, 400);
    }

    private void clearCustomerFields() {
        currentCustomer = null;
        customerTable.getSelectionModel().clearSelection();
        customerTable.refresh();
    }

    private void clearAccountFields() {
        currentAccount = null;
        accountTable.getSelectionModel().clearSelection();
        accountTable.refresh();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void addCustomer(String name, String surname, String email, String phone, String address) {
        // Basit bir validasyon ekleyelim
        if (!name.matches("[a-zA-Z]+") || !surname.matches("[a-zA-Z]+")) {
            showAlert("Name and surname should contain only letters.");
            return;
        }

        if (!email.matches("\\b[\\w.%-]+@[-.\\w]+\\.[A-Za-z]{2,4}\\b")) {
            showAlert("Please enter a valid email address.");
            return;
        }

        if (!phone.matches("\\d+")) {
            showAlert("Phone number should contain only digits.");
            return;
        }

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String insertCustomerSQL = "INSERT INTO customers (name, surname, email, phone, address) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(insertCustomerSQL, Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, name);
                statement.setString(2, surname);
                statement.setString(3, email);
                statement.setString(4, phone);
                statement.setString(5, address);
                int affectedRows = statement.executeUpdate();
                if (affectedRows == 0) {
                    showAlert("Creating customer failed, no rows affected.");
                    return;
                }
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int customerId = generatedKeys.getInt(1);
                        Customer newCustomer = new Customer(customerId, name, surname, email, phone, address);
                        customers.add(newCustomer);
                        ObservableList<Account> newCustomerAccounts = FXCollections.observableArrayList();
                        customerAccountsMap.put(customerId, newCustomerAccounts);
                        newCustomer.setAccounts(newCustomerAccounts);
                    } else {
                        showAlert("Creating customer failed, no ID obtained.");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database error occurred while adding customer.");
        }
    }

    private void updateCustomer(String id, String name, String surname, String email, String phone, String address) {
        // Basit bir validasyon ekleyelim
        if (!id.isEmpty()) {
            int customerId;
            try {
                customerId = Integer.parseInt(id);
            } catch (NumberFormatException ex) {
                showAlert("Please enter a valid customer ID.");
                return;
            }

            if (!name.matches("[a-zA-Z]+") || !surname.matches("[a-zA-Z]+")) {
                showAlert("Name and surname should contain only letters.");
                return;
            }

            if (!email.matches("\\b[\\w.%-]+@[-.\\w]+\\.[A-Za-z]{2,4}\\b")) {
                showAlert("Please enter a valid email address.");
                return;
            }

            if (!phone.matches("\\d+")) {
                showAlert("Phone number should contain only digits.");
                return;
            }

            try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                String updateCustomerSQL = "UPDATE customers SET name=?, surname=?, email=?, phone=?, address=? WHERE id=?";
                try (PreparedStatement statement = connection.prepareStatement(updateCustomerSQL)) {
                    statement.setString(1, name);
                    statement.setString(2, surname);
                    statement.setString(3, email);
                    statement.setString(4, phone);
                    statement.setString(5, address);
                    statement.setInt(6, customerId);
                    int affectedRows = statement.executeUpdate();
                    if (affectedRows == 0) {
                        showAlert("Customer not found!");
                        return;
                    }
                    Customer customerToUpdate = customers.stream()
                            .filter(c -> c.getId() == customerId)
                            .findFirst()
                            .orElse(null);
                    if (customerToUpdate != null) {
                        customerToUpdate.setName(name);
                        customerToUpdate.setSurname(surname);
                        customerToUpdate.setEmail(email);
                        customerToUpdate.setPhone(phone);
                        customerToUpdate.setAddress(address);
                        customerTable.refresh();
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Database error occurred while updating customer.");
            }
        } else {
            showAlert("Please enter a valid customer ID.");
        }
    }

    private void deleteCustomer(String id) {
        // Basit bir validasyon ekleyelim
        if (!id.isEmpty()) {
            int customerId;
            try {
                customerId = Integer.parseInt(id);
            } catch (NumberFormatException ex) {
                showAlert("Please enter a valid customer ID.");
                return;
            }

            try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                String deleteCustomerSQL = "DELETE FROM customers WHERE id=?";
                try (PreparedStatement statement = connection.prepareStatement(deleteCustomerSQL)) {
                    statement.setInt(1, customerId);
                    int affectedRows = statement.executeUpdate();
                    if (affectedRows == 0) {
                        showAlert("Customer not found!");
                        return;
                    }
                    Customer customerToDelete = customers.stream()
                            .filter(c -> c.getId() == customerId)
                            .findFirst()
                            .orElse(null);
                    if (customerToDelete != null) {
                        customers.remove(customerToDelete);
                        customerAccountsMap.remove(customerId);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Database error occurred while deleting customer.");
            }
        } else {
            showAlert("Please enter a valid customer ID.");
        }
    }

    private void addAccount(String type, double balance, int customerId) {
        // Basit bir validasyon ekleyelim
        if (customers.stream().noneMatch(c -> c.getId() == customerId)) {
            showAlert("Customer with given ID does not exist.");
            return;
        }

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String insertAccountSQL = "INSERT INTO accounts (type, balance, customer_id) VALUES (?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(insertAccountSQL, Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, type);
                statement.setDouble(2, balance);
                statement.setInt(3, customerId);
                int affectedRows = statement.executeUpdate();
                if (affectedRows == 0) {
                    showAlert("Creating account failed, no rows affected.");
                    return;
                }
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int accountId = generatedKeys.getInt(1);
                        Account newAccount = new Account(accountId, type, balance, customerId);
                        accounts.add(newAccount);
                        ObservableList<Account> customerAccounts = customerAccountsMap.get(customerId);
                        if (customerAccounts != null) {
                            customerAccounts.add(newAccount);
                        } else {
                            customerAccounts = FXCollections.observableArrayList();
                            customerAccounts.add(newAccount);
                            customerAccountsMap.put(customerId, customerAccounts);
                        }
                        currentCustomer = customers.stream()
                                .filter(c -> c.getId() == customerId)
                                .findFirst()
                                .orElse(null);
                        if (currentCustomer != null) {
                            currentCustomer.getAccounts().add(newAccount);
                            accountTable.setItems(currentCustomer.getAccounts());
                        }
                    } else {
                        showAlert("Creating account failed, no ID obtained.");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database error occurred while adding account.");
        }
    }

    private void updateAccount(int id, String type, double balance, int customerId) {
        // Basit bir validasyon ekleyelim
        if (!customers.stream().anyMatch(c -> c.getId() == customerId)) {
            showAlert("Customer with given ID does not exist.");
            return;
        }

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String updateAccountSQL = "UPDATE accounts SET type=?, balance=?, customer_id=? WHERE id=?";
            try (PreparedStatement statement = connection.prepareStatement(updateAccountSQL)) {
                statement.setString(1, type);
                statement.setDouble(2, balance);
                statement.setInt(3, customerId);
                statement.setInt(4, id);
                int affectedRows = statement.executeUpdate();
                if (affectedRows == 0) {
                    showAlert("Account not found!");
                    return;
                }
                Account accountToUpdate = accounts.stream()
                        .filter(a -> a.getId() == id)
                        .findFirst()
                        .orElse(null);
                if (accountToUpdate != null) {
                    accountToUpdate.setType(type);
                    accountToUpdate.setBalance(balance);
                    accountToUpdate.setCustomerId(customerId);
                    accountTable.refresh();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database error occurred while updating account.");
        }
    }

    private void deleteAccount(int id) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String deleteAccountSQL = "DELETE FROM accounts WHERE id=?";
            try (PreparedStatement statement = connection.prepareStatement(deleteAccountSQL)) {
                statement.setInt(1, id);
                int affectedRows = statement.executeUpdate();
                if (affectedRows == 0) {
                    showAlert("Account not found!");
                    return;
                }
                Account accountToDelete = accounts.stream()
                        .filter(a -> a.getId() == id)
                        .findFirst()
                        .orElse(null);
                if (accountToDelete != null) {
                    accounts.remove(accountToDelete);
                    ObservableList<Account> customerAccounts = customerAccountsMap.get(accountToDelete.getCustomerId());
                    if (customerAccounts != null) {
                        customerAccounts.remove(accountToDelete);
                    }
                    currentCustomer = customers.stream()
                            .filter(c -> c.getId() == accountToDelete.getCustomerId())
                            .findFirst()
                            .orElse(null);
                    if (currentCustomer != null) {
                        currentCustomer.getAccounts().remove(accountToDelete);
                        accountTable.setItems(currentCustomer.getAccounts());
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database error occurred while deleting account.");
        }
    }
}

