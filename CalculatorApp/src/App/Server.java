package App;


import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(1113);
            System.out.println("Server started. Waiting for clients...");
            while(true) {
            	Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected!");
                int number1 = clientSocket.getInputStream().read();
                int number2 = clientSocket.getInputStream().read();
                char Operator = (char) clientSocket.getInputStream().read();

                int result = calculate(number1, number2, Operator);

                OutputStream os = clientSocket.getOutputStream();
                os.write(result);
                clientSocket.close();
                serverSocket.close();
            }
            

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int calculate(int operand1, int operand2, char operator) {
        switch (operator) {
            case '+':
                return operand1 + operand2;
            case '-':
                return operand1 - operand2;
            case '*':
                return operand1 * operand2;
            case '/':
                return operand1 / operand2;
            default:
                throw new IllegalArgumentException("Unsupported operator: " + operator);
        }
    }
}

