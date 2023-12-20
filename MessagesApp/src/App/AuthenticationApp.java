package App;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import javax.swing.table.DefaultTableModel;


public class AuthenticationApp extends JFrame {

    private JTextField signUpUsernameField, signUpFirstNameField, signUpLastNameField, signUpPasswordField;
    private JTextField signInUsernameField, signInPasswordField;
    private CardLayout cardLayout = new CardLayout();
    private JPanel cardPanel = new JPanel(cardLayout);

    private static final String DB_URL = "jdbc:mysql://localhost:3306/messageapp";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    public AuthenticationApp() {
        setTitle("Authentication App");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JButton signUpButton = new JButton("Sign Up");
        JButton signInButton = new JButton("Sign In");

        cardPanel.add(createSignUpPanel(), "SIGN_UP");
        cardPanel.add(createSignInPanel(), "SIGN_IN");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(signUpButton);
        buttonPanel.add(signInButton);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.add(buttonPanel);
        contentPanel.add(cardPanel);

        setLayout(new BorderLayout());
        add(contentPanel, BorderLayout.CENTER);

        signUpButton.addActionListener(e -> switchToPanel("SIGN_UP"));
        signInButton.addActionListener(e -> switchToPanel("SIGN_IN"));

        setVisible(true);
    }

    private JPanel createSignUpPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        signUpUsernameField = addTextField(panel, "Username:");
        signUpFirstNameField = addTextField(panel, "First Name:");
        signUpLastNameField = addTextField(panel, "Last Name:");
        signUpPasswordField = addTextField(panel, "Password:");

        return panel;
    }

    private JPanel createSignInPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        signInUsernameField = addTextField(panel, "Username:");
        signInPasswordField = addTextField(panel, "Password:");

        return panel;
    }

    private JTextField addTextField(JPanel panel, String labelText) {
        JPanel fieldPanel = new JPanel();
        JLabel label = new JLabel(labelText);
        JTextField textField = new JTextField(15);
        textField.setPreferredSize(new Dimension(200, 25));
        fieldPanel.add(label);
        fieldPanel.add(textField);
        panel.add(fieldPanel);
        return textField;
    }

    private void switchToPanel(String panelName) {
        cardLayout.show(cardPanel, panelName);
        if ("SIGN_UP".equals(panelName)) {
            signUp();
        } else if ("SIGN_IN".equals(panelName)) {
            signIn();
        }
    }

    private void signUp() {
        String username = signUpUsernameField.getText();
        String firstName = signUpFirstNameField.getText();
        String lastName = signUpLastNameField.getText();
        String password = signUpPasswordField.getText();

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "INSERT INTO users (username, firstname, lastname, password) VALUES (?, ?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, firstName);
                preparedStatement.setString(3, lastName);
                preparedStatement.setString(4, password);
                int rowsAffected = preparedStatement.executeUpdate();
                System.out.println(rowsAffected > 0 ? "Sign Up: User added to the database" : "Sign Up: Failed to add user");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void signIn() {
        String username = signInUsernameField.getText();
        String password = signInPasswordField.getText();

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "SELECT id FROM users WHERE username = ? AND password = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, password);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        int userId = resultSet.getInt("id");
                        System.out.println("Sign In: User authenticated. User ID: " + userId);
                        openMessagesFrame(userId);
                    } else {
                        System.out.println("Sign In: Authentication failed");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
        private void openMessagesFrame(int userId) {
        JFrame messagesFrame = new JFrame("Messages");
        messagesFrame.setSize(600, 400);
        messagesFrame.setLocationRelativeTo(null);
        messagesFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "SELECT id, topic, message FROM messages";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query);
                 ResultSet resultSet = preparedStatement.executeQuery()) {

                // Create a JTable to display messages
                String[] columnNames = {"ID", "Topic", "Message"};
                DefaultTableModel tableModel = new DefaultTableModel(null, columnNames) {
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false; // Make all cells non-editable
                    }
                };
                JTable messagesTable = new JTable(tableModel);

                // Add messages from the database to the table
                while (resultSet.next()) {
                    int messageId = resultSet.getInt("id");
                    String topic = resultSet.getString("topic");
                    String message = resultSet.getString("message");
                    tableModel.addRow(new Object[]{messageId, topic, message});
                }

                JScrollPane scrollPane = new JScrollPane(messagesTable);
                messagesFrame.getContentPane().add(scrollPane, BorderLayout.CENTER);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        messagesFrame.setVisible(true);
    }
    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(AuthenticationApp::new);
    }
}
