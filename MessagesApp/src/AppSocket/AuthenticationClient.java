package AppSocket;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;

public class AuthenticationClient {

    public static void main(String[] args) {
    	AuthenticationApp authenticationApp = new AuthenticationApp();
    	authenticationApp.setVisible(true);

    }
}

class AuthenticationApp extends JFrame {

    private static final long serialVersionUID = 1L;

    private JTextField signUpUsernameField, signUpFirstNameField, signUpLastNameField, signUpPasswordField;
    private JTextField signInUsernameField, signInPasswordField;
    private CardLayout cardLayout = new CardLayout();
    private JPanel cardPanel = new JPanel(cardLayout);

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
        try (Socket socket = new Socket("localhost", 12345);
             ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream input = new ObjectInputStream(socket.getInputStream())) {

            output.writeObject("SIGN_UP");
            output.writeObject(signUpUsernameField.getText());
            output.writeObject(signUpFirstNameField.getText());
            output.writeObject(signUpLastNameField.getText());
            output.writeObject(signUpPasswordField.getText());

            // Implement the sign-up logic here if needed...

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void signIn() {
        try (Socket socket = new Socket("localhost", 12345);
             ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream input = new ObjectInputStream(socket.getInputStream())) {

            output.writeObject("SIGN_IN");
            output.writeObject(signInUsernameField.getText());
            output.writeObject(signInPasswordField.getText());

            // Read the server response
            try {
                @SuppressWarnings("unchecked")
                ArrayList<ArrayList<Object>> messages = (ArrayList<ArrayList<Object>>) input.readObject();
                displayMessages(messages);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Replace the displayMessages method with the following
private void displayMessages(ArrayList<ArrayList<Object>> messages) {
    JFrame messagesFrame = new JFrame("Messages");
    messagesFrame.setSize(800, 400);
    messagesFrame.setLocationRelativeTo(null);
    messagesFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    String[] columnNames = {"ID", "Topic", "Message"};

    Object[][] messagesArray = new Object[messages.size()][];
    for (int i = 0; i < messages.size(); i++) {
        ArrayList<Object> row = messages.get(i);
        messagesArray[i] = row.toArray(new Object[0]);
    }

    DefaultTableModel tableModel = new DefaultTableModel(messagesArray, columnNames) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false; // Make all cells non-editable
        }
    };

    JTable messagesTable = new JTable(tableModel);
    JScrollPane scrollPane = new JScrollPane(messagesTable);
    messagesFrame.getContentPane().add(scrollPane, BorderLayout.CENTER);

    messagesFrame.setVisible(true);
}

}
