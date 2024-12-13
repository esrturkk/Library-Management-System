import javax.swing.*;
import java.awt.*;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class LibraryManagementSystem {

    public static void main(String[] args) {

        JFrame frame = new JFrame("Kütüphane Yönetim Sistemi");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);

        CardLayout cardLayout = new CardLayout();
        JPanel mainPanel = new JPanel(cardLayout);

        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new BorderLayout());

        JLabel appNameLabel = new JLabel("Kütüphane Yönetim Sistemi", SwingConstants.CENTER);
        appNameLabel.setFont(new Font("Arial", Font.BOLD, 24));
        loginPanel.add(appNameLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridLayout(3, 1, 10, 10));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        JTextField usernameField = new JTextField(5);
        usernameField.setToolTipText("Kullanıcı Adı");
        JPasswordField passwordField = new JPasswordField(5);
        passwordField.setToolTipText("Şifre");
        JButton loginButton = new JButton("Giriş Yap");

        centerPanel.add(usernameField);
        centerPanel.add(passwordField);
        centerPanel.add(loginButton);
        loginPanel.add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new GridLayout(2, 1));
        JLabel noAccountLabel = new JLabel("Hesabınız yok mu?", SwingConstants.CENTER);
        JButton registerButton = new JButton("Kayıt Ol");

        bottomPanel.add(noAccountLabel);
        bottomPanel.add(registerButton);
        loginPanel.add(bottomPanel, BorderLayout.SOUTH);

        JPanel adminPanel = new JPanel();
        adminPanel.setLayout(new GridLayout(5, 1, 10, 10));

        JLabel adminLabel = new JLabel("Admin Paneli", SwingConstants.CENTER);
        JButton addBookButton = new JButton("Kitap Ekle");
        JButton removeBookButton = new JButton("Kitap Sil");
        JButton backToLoginButton1 = new JButton("Çıkış");
        JLabel tokenLabel = new JLabel("", SwingConstants.CENTER);

        adminPanel.add(adminLabel);
        adminPanel.add(addBookButton);
        adminPanel.add(removeBookButton);
        adminPanel.add(backToLoginButton1);
        adminPanel.add(tokenLabel);

        JPanel userPanel = new JPanel();
        userPanel.setLayout(new GridLayout(5, 1, 10, 10));

        JLabel userLabel = new JLabel("Kullanıcı Paneli", SwingConstants.CENTER);
        JButton borrowBookButton = new JButton("Kitap Al");
        JButton returnBookButton = new JButton("Kitap İade Et");
        JButton viewBooksButton = new JButton("Aldığım Kitapları Görüntüle");
        JButton backToLoginButton2 = new JButton("Çıkış");

        userPanel.add(userLabel);
        userPanel.add(borrowBookButton);
        userPanel.add(returnBookButton);
        userPanel.add(viewBooksButton);
        userPanel.add(backToLoginButton2);

        JPanel registerPanel = new JPanel();
        registerPanel.setLayout(new GridLayout(4, 1, 10, 10));

        JLabel registerLabel = new JLabel("Kayıt Ol", SwingConstants.CENTER);
        JTextField newUsernameField = new JTextField(10);
        newUsernameField.setToolTipText("Yeni Kullanıcı Adı");
        JPasswordField newPasswordField = new JPasswordField(10);
        newPasswordField.setToolTipText("Yeni Şifre");
        JButton confirmRegisterButton = new JButton("Kaydol");

        registerPanel.add(registerLabel);
        registerPanel.add(newUsernameField);
        registerPanel.add(newPasswordField);
        registerPanel.add(confirmRegisterButton);

        mainPanel.add(loginPanel, "login");
        mainPanel.add(adminPanel, "admin");
        mainPanel.add(userPanel, "user");
        mainPanel.add(registerPanel, "register");

        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            try {
                URL url = new URL("http://localhost:8080/login");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json; utf-8");
                connection.setRequestProperty("Accept", "application/json");
                connection.setDoOutput(true);

                String jsonInputString = "{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}";

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int status = connection.getResponseCode();
                if (status == HttpURLConnection.HTTP_OK) {
                    try (Scanner scanner = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8)) {
                        String responseBody = scanner.useDelimiter("\\A").next();
                        if (responseBody.contains("\"status\":\"success\"")) {
                            String token = responseBody.replaceAll(".*\"token\":\"(.*?)\".*", "$1");
                            tokenLabel.setText("Token: " + token);
                            cardLayout.show(mainPanel, "admin");
                        } else if (responseBody.contains("\"status\":\"false\"")) {
                            JOptionPane.showMessageDialog(frame, "Giriş başarısız! Kullanıcı adı veya şifre hatalı.", "Hata", JOptionPane.ERROR_MESSAGE);
                        } else {
                            cardLayout.show(mainPanel, "user");
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "Sunucu hatası: " + status, "Hata", JOptionPane.ERROR_MESSAGE);
                }

                connection.disconnect();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Bir hata oluştu: " + ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
            }
        });

        registerButton.addActionListener(e -> cardLayout.show(mainPanel, "register"));
        confirmRegisterButton.addActionListener(e -> cardLayout.show(mainPanel, "login"));
        backToLoginButton1.addActionListener(e -> cardLayout.show(mainPanel, "login"));
        backToLoginButton2.addActionListener(e -> cardLayout.show(mainPanel, "login"));

        frame.add(mainPanel);
        frame.setVisible(true);
    }
}
