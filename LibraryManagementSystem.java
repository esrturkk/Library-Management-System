import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class LibraryManagementSystem {
    private static String finalToken = "";


    public static void main(String[] args) {
        JFrame frame = new JFrame("Kütüphane Yönetim Sistemi");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);

        CardLayout cardLayout = new CardLayout();
        JPanel mainPanel = new JPanel(cardLayout);

        JPanel loginPanel = createLoginPanel(cardLayout, mainPanel, frame);
        JPanel adminPanel = createAdminPanel(frame, cardLayout, mainPanel);
        JPanel userPanel = createUserPanel(cardLayout, mainPanel, frame);
        JPanel registerPanel = createRegisterPanel(cardLayout, mainPanel, frame);
        JPanel addBookPanel = createAddBookPanel(cardLayout, mainPanel, frame);

        mainPanel.add(loginPanel, "login");
        mainPanel.add(adminPanel, "admin");
        mainPanel.add(userPanel, "user");
        mainPanel.add(registerPanel, "register");
        mainPanel.add(addBookPanel, "addBook");

        frame.add(mainPanel);
        frame.setVisible(true);
    }

    // Helper methods
    private static HttpURLConnection createConnection(String urlString, String method, String token) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setRequestProperty("Content-Type", "application/json; utf-8");
        connection.setRequestProperty("Accept", "application/json");
        if (token != null) {
            connection.setRequestProperty("Authorization", "Bearer " + token); // Token'ı başlığa ekliyoruz
        }
        connection.setDoOutput(true);
        return connection;
    }

    private static JPanel createLoginPanel(CardLayout cardLayout, JPanel mainPanel, JFrame frame) {
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
        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            try {
                HttpURLConnection connection = createConnection("http://localhost:8080/login", "POST", null);

                String jsonInputString = "{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}";
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int status = connection.getResponseCode();
                if (status == HttpURLConnection.HTTP_OK) {
                    try (Scanner scanner = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8)) {
                        String responseBody = scanner.useDelimiter("\\A").next();
                        // Token'ı JSON yanıtından çıkarıyoruz
                        if (responseBody.contains("\"status\":\"success\"")) {
                            // Token'ı çıkarıyoruz
                            finalToken = responseBody.substring(responseBody.indexOf("\"token\":\"") + 9, responseBody.indexOf("\"}", responseBody.indexOf("\"token\":\"")));
                            System.out.println("Token alındı: " + finalToken);

                            // Admin veya kullanıcı ekranını gösteriyoruz
                            if (username.equals("admin")) {
                                cardLayout.show(mainPanel, "admin");
                            } else {
                                cardLayout.show(mainPanel, "user");
                            }

                        } else {
                            JOptionPane.showMessageDialog(frame, "Giriş başarısız! Kullanıcı adı veya şifre hatalı.", "Hata", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "Sunucu hatası: " + status, "Hata", JOptionPane.ERROR_MESSAGE);
                }

                connection.disconnect();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Bir hata oluştu: " + ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
            }
        });


        registerButton.addActionListener(e -> cardLayout.show(mainPanel, "register"));

        return loginPanel;
    }

    private static JPanel createAdminPanel(JFrame frame, CardLayout cardLayout, JPanel mainPanel) {
        JPanel adminPanel = new JPanel();
        adminPanel.setLayout(new GridLayout(5, 1, 10, 10));

        JLabel adminLabel = new JLabel("Admin Paneli", SwingConstants.CENTER);
        JButton addBookButton = new JButton("Kitap Ekle");
        JButton removeBookButton = new JButton("Kitap Sil");
        JButton backToLoginButton = new JButton("Çıkış");
        JLabel tokenLabel = new JLabel("", SwingConstants.CENTER);

        adminPanel.add(adminLabel);
        adminPanel.add(addBookButton);
        adminPanel.add(removeBookButton);
        adminPanel.add(backToLoginButton);
        adminPanel.add(tokenLabel);

        addBookButton.addActionListener(e -> cardLayout.show(mainPanel, "addBook"));
        backToLoginButton.addActionListener(e -> cardLayout.show(mainPanel, "login"));

        return adminPanel;
    }

    private static JPanel createUserPanel(CardLayout cardLayout, JPanel mainPanel, JFrame frame) {
        JPanel userPanel = new JPanel();
        userPanel.setLayout(new GridLayout(5, 1, 10, 10));

        JLabel userLabel = new JLabel("Kullanıcı Paneli", SwingConstants.CENTER);
        JButton borrowBookButton = new JButton("Kitap Al");
        JButton returnBookButton = new JButton("Kitap İade Et");
        JButton viewBooksButton = new JButton("Aldığım Kitapları Görüntüle");
        JButton backToLoginButton = new JButton("Çıkış");

        userPanel.add(userLabel);
        userPanel.add(borrowBookButton);
        userPanel.add(returnBookButton);
        userPanel.add(viewBooksButton);
        userPanel.add(backToLoginButton);

        backToLoginButton.addActionListener(e -> cardLayout.show(mainPanel, "login"));

        return userPanel;
    }

    private static JPanel createRegisterPanel(CardLayout cardLayout, JPanel mainPanel, JFrame frame) {
        JPanel registerPanel = new JPanel();
        registerPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;

        // İsim
        JLabel nameLabel = new JLabel("İsim:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        registerPanel.add(nameLabel, gbc);

        JTextField nameField = new JTextField(10);
        gbc.gridx = 1;
        gbc.gridy = 0;
        registerPanel.add(nameField, gbc);

        // Soyisim
        JLabel surnameLabel = new JLabel("Soyisim:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        registerPanel.add(surnameLabel, gbc);

        JTextField surnameField = new JTextField(10);
        gbc.gridx = 1;
        gbc.gridy = 1;
        registerPanel.add(surnameField, gbc);

        // Kullanıcı adı
        JLabel usernameLabel = new JLabel("Kullanıcı Adı:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        registerPanel.add(usernameLabel, gbc);

        JTextField newUsernameField = new JTextField(10);
        gbc.gridx = 1;
        gbc.gridy = 2;
        registerPanel.add(newUsernameField, gbc);

        // Şifre
        JLabel passwordLabel = new JLabel("Şifre:");
        gbc.gridx = 0;
        gbc.gridy = 3;
        registerPanel.add(passwordLabel, gbc);

        JPasswordField newPasswordField = new JPasswordField(10);
        gbc.gridx = 1;
        gbc.gridy = 3;
        registerPanel.add(newPasswordField, gbc);

        // E-posta
        JLabel emailLabel = new JLabel("E-posta:");
        gbc.gridx = 0;
        gbc.gridy = 4;
        registerPanel.add(emailLabel, gbc);

        JTextField emailField = new JTextField(10);
        gbc.gridx = 1;
        gbc.gridy = 4;
        registerPanel.add(emailField, gbc);

        // Kaydol
        JButton confirmRegisterButton = new JButton("Kaydol");
        gbc.gridx = 1;
        gbc.gridy = 5;
        registerPanel.add(confirmRegisterButton, gbc);

        confirmRegisterButton.addActionListener(e -> {
            String name = nameField.getText();
            String surname = surnameField.getText();
            String newUsername = newUsernameField.getText();
            String newPassword = new String(newPasswordField.getPassword());
            String email = emailField.getText();

            try {
                HttpURLConnection connection = createConnection("http://localhost:8080/register", "POST", null);

                String jsonInputString = "{\"name\": \"" + name + "\", \"surname\": \"" + surname + "\", " +
                        "\"username\": \"" + newUsername + "\", \"password\": \"" + newPassword + "\", \"email\": \"" + email + "\"}";
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int status = connection.getResponseCode();
                if (status == HttpURLConnection.HTTP_OK) {
                    JOptionPane.showMessageDialog(frame, "Kayıt başarılı. Giriş yapabilirsiniz.", "Başarılı", JOptionPane.INFORMATION_MESSAGE);
                    cardLayout.show(mainPanel, "login");
                } else {
                    JOptionPane.showMessageDialog(frame, "Kayıt hatası: " + status, "Hata", JOptionPane.ERROR_MESSAGE);
                }

                connection.disconnect();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Bir hata oluştu: " + ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
            }
        });

        return registerPanel;
    }

    private static JPanel createAddBookPanel(CardLayout cardLayout, JPanel mainPanel, JFrame frame) {
        JPanel addBookPanel = new JPanel();
        addBookPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;

        // ISBN
        JLabel isbnLabel = new JLabel("ISBN:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        addBookPanel.add(isbnLabel, gbc);

        JTextField isbnField = new JTextField(10);
        gbc.gridx = 1;
        gbc.gridy = 0;
        addBookPanel.add(isbnField, gbc);

        // Kitap Adı
        JLabel titleLabel = new JLabel("Kitap Adı:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        addBookPanel.add(titleLabel, gbc);

        JTextField titleField = new JTextField(10);
        gbc.gridx = 1;
        gbc.gridy = 1;
        addBookPanel.add(titleField, gbc);

        // Yazar
        JLabel authorLabel = new JLabel("Yazar:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        addBookPanel.add(authorLabel, gbc);

        JTextField authorField = new JTextField(10);
        gbc.gridx = 1;
        gbc.gridy = 2;
        addBookPanel.add(authorField, gbc);

        // Yayın Tarihi
        JLabel publishedDateLabel = new JLabel("Yayın Tarihi:");
        gbc.gridx = 0;
        gbc.gridy = 3;
        addBookPanel.add(publishedDateLabel, gbc);

        JTextField publishedDateField = new JTextField(10);
        gbc.gridx = 1;
        gbc.gridy = 3;
        addBookPanel.add(publishedDateField, gbc);

        // Kitap Ekle
        JButton addButton = new JButton("Kitap Ekle");
        gbc.gridx = 1;
        gbc.gridy = 4;
        addBookPanel.add(addButton, gbc);

     // Kitap Ekle butonuna tıklandığında çalışacak kod:
        addButton.addActionListener(e -> {
            // Formdan kitap bilgilerini alıyoruz
            String isbn = isbnField.getText();
            String title = titleField.getText();
            String author = authorField.getText();
            String publishedDate = publishedDateField.getText();

            // JSON formatında kitap verisini oluşturuyoruz
            String jsonInputString = String.format("{\"isbn\":\"%s\",\"title\":\"%s\",\"author\":\"%s\",\"publishedDate\":\"%s\"}",
                    isbn, title, author, publishedDate);

            try {
                // Token'ı kullanarak sunucuya bağlanıyoruz
                HttpURLConnection connection = createConnection("http://localhost:8080/api/books/create", "POST", finalToken);

                // JSON verisini sunucuya gönderiyoruz
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                // Yanıtı alıyoruz
                int status = connection.getResponseCode();
                if (status == HttpURLConnection.HTTP_OK) {
                    // Başarılı ise kullanıcıya mesaj gösteriyoruz
                    JOptionPane.showMessageDialog(frame, "Kitap başarıyla eklendi!", "Başarılı", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    // Hata durumunda kullanıcıya hata mesajı gösteriyoruz
                    JOptionPane.showMessageDialog(frame, "Kitap eklenemedi. Hata: " + status, "Hata", JOptionPane.ERROR_MESSAGE);
                }

                // Bağlantıyı kapatıyoruz
                connection.disconnect();
            } catch (Exception ex) {
                // Hata durumunda kullanıcıya mesaj gösteriyoruz
                JOptionPane.showMessageDialog(frame, "Bir hata oluştu: " + ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
            }
        });

        return addBookPanel;
    }
}
