package App;

import java.awt.BorderLayout;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import javax.swing.*;

public class Client {

    public static void main(String[] args) {
        JFrame frame = new JFrame("Calculator Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 200);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JTextField num1Field = new JTextField();
        JTextField num2Field = new JTextField();
        JTextField operatorField = new JTextField();

        JButton calculateButton = new JButton("Calculate");

        calculateButton.addActionListener(e -> {
            try {
                int num1 = Integer.parseInt(num1Field.getText());
                int num2 = Integer.parseInt(num2Field.getText());
                char operator = operatorField.getText().charAt(0);

                Socket s = new Socket("localhost", 1113);
                OutputStream os = s.getOutputStream();
                InputStream is = s.getInputStream();

                os.write(num1);
                os.write(num2);
                os.write((char) operator);

                int result = is.read();
                JOptionPane.showMessageDialog(frame, "Result received from server: " + result, "Result", JOptionPane.INFORMATION_MESSAGE);

                s.close();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(new JLabel("Enter the first number:"));
        panel.add(num1Field);
        panel.add(new JLabel("Enter the second number:"));
        panel.add(num2Field);
        panel.add(new JLabel("Enter the operator (+, -, *, /):"));
        panel.add(operatorField);
        panel.add(calculateButton);

        frame.getContentPane().add(BorderLayout.CENTER, panel);
        frame.setVisible(true);
    }
}
