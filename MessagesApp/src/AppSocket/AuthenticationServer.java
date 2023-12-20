package AppSocket;

import java.io.*;
import java.net.*;
import java.sql.*;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;

public class AuthenticationServer {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/messageapp";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            System.out.println("Server is running and listening on port 12345...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                handleClient(clientSocket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream());
             ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream())) {

            String action = (String) input.readObject();

            if ("SIGN_UP".equals(action)) {
                signUp(input);
            } else if ("SIGN_IN".equals(action)) {
                signIn(input, output);
            }

        } catch (IOException | ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    private static void signUp(ObjectInputStream input) throws IOException, SQLException {
        try {
            String username = (String) input.readObject();
            String firstName = (String) input.readObject();
            String lastName = (String) input.readObject();
            String password = (String) input.readObject();

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
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void signIn(ObjectInputStream input, ObjectOutputStream output) throws IOException, SQLException {
        try {
            String username = (String) input.readObject();
            String password = (String) input.readObject();

            try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                String query = "SELECT id FROM users WHERE username = ? AND password = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                    preparedStatement.setString(1, username);
                    preparedStatement.setString(2, password);
                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        if (resultSet.next()) {
                            int userId = resultSet.getInt("id");
                            System.out.println("Sign In: User authenticated. User ID: " + userId);
                            sendMessages(output);
                        } else {
                            System.out.println("Sign In: Authentication failed");
                        }
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void sendMessages(ObjectOutputStream output) throws IOException, SQLException {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "SELECT id, topic, message FROM messages";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query);
                 ResultSet resultSet = preparedStatement.executeQuery()) {

                ArrayList<ArrayList<Object>> messages = new ArrayList<>();

                while (resultSet.next()) {
                    int messageId = resultSet.getInt("id");
                    String topic = resultSet.getString("topic");
                    String message = resultSet.getString("message");

                    ArrayList<Object> messageRow = new ArrayList<>();
                    messageRow.add(messageId);
                    messageRow.add(topic);
                    messageRow.add(message);

                    messages.add(messageRow);
                }

                output.writeObject(messages);
            }
        }
    }
}
