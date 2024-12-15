import javax.swing.*;            // Swing UI bileşenleri için
import java.awt.*;               // Layout ve komponentler için
import java.io.*;                // Giriş/Çıkış işlemleri (dosya okuma/yazma vb.)
import java.net.HttpURLConnection; // HTTP bağlantısı için
import java.net.URL;             // URL sınıfı için
import java.nio.charset.StandardCharsets; // Karakter setleri için
import java.util.ArrayList;      // ArrayList sınıfı için
import java.util.List;           // List arayüzü için
import java.util.Scanner;        // Kullanıcıdan giriş almak için
              


public class LibraryManagementSystem {
    public static String finalToken = "";

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
        
        cardLayout.show(mainPanel, "login");
    }

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
        JButton viewMembersButton = new JButton("Tüm Üyeleri Listele");
        JButton addBookButton = new JButton("Kitap Ekle");
        JButton removeBookButton = new JButton("Kitap Sil");
        JButton backToLoginButton = new JButton("Çıkış");

        adminPanel.add(adminLabel);
        adminPanel.add(viewMembersButton);
        adminPanel.add(addBookButton);
        adminPanel.add(removeBookButton);
        adminPanel.add(backToLoginButton);
        
        viewMembersButton.addActionListener(e -> {
            // Admin giriş yaptıktan sonra aldığı token
            String token = finalToken;
            showAllMembers(frame, token);
        });

        addBookButton.addActionListener(e -> cardLayout.show(mainPanel, "addBook"));

        removeBookButton.addActionListener(e -> {
            try {
                // Kullanıcıdan silmek istediği kitabın ID'sini al
                String bookId = JOptionPane.showInputDialog(frame, "Silmek istediğiniz kitabın ID'sini girin:", 
                                                            "Kitap Sil", JOptionPane.QUESTION_MESSAGE);

                // Kullanıcı bir ID girdiyse silme işlemini başlat
                if (bookId != null && !bookId.trim().isEmpty()) {
                    deleteBook(frame, bookId.trim());
                } else {
                    JOptionPane.showMessageDialog(frame, "Kitap ID'si boş olamaz!", "Hata", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Bir hata oluştu: " + ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
            }
        });

        backToLoginButton.addActionListener(e -> cardLayout.show(mainPanel, "login"));

        return adminPanel;
    }
    
    public static void showAllMembers(JFrame frame, String token) {
        try {
            // API URL
            String urlString = "http://localhost:8080/api/members";
            URL url = new URL(urlString);

            // HTTP GET isteği oluştur
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Token ile kimlik doğrulama
            connection.setRequestProperty("Authorization", "Bearer " + token);

            // Yanıt kodunu kontrol et
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) { // 200 OK
                // Yanıt verisini oku
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // Yanıt verilerini JSON olarak al
                String responseData = response.toString();

                // Üyeleri JSON formatında çözümle
                List<String> members = parseMembers(responseData);

                // Üyeleri bir mesaj kutusunda göster
                StringBuilder message = new StringBuilder("Tüm Üyeler:\n\n");
                for (String member : members) {
                    message.append(member).append("\n\n"); // Üyeleri alt alta ve boşlukla göster
                }

                JOptionPane.showMessageDialog(frame, message.toString(), "Üyeler Listesi", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(frame, "Üyeler alınamadı. Yanıt Kodu: " + responseCode, "Hata", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Hata oluştu: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static List<String> parseMembers(String responseData) {
        List<String> members = new ArrayList<>();
        try {
            // JSON dizisini parçala
            String[] membersArray = responseData.substring(1, responseData.length() - 1).split("\\},\\{");

            for (String memberData : membersArray) {
                // JSON objesini parçalıyoruz
                String[] keyValuePairs = memberData.replace("{", "").replace("}", "").split(",");

                String memberId = "";
                String memberName = "";

                // Key-Value çiftlerini ayırarak üyelere ait bilgileri buluyoruz
                for (String pair : keyValuePairs) {
                    String[] keyValue = pair.split(":");
                    String key = keyValue[0].replace("\"", "").trim();
                    String value = keyValue[1].replace("\"", "").trim();

                    if (key.equals("id")) {
                        memberId = value;
                    } else if (key.equals("name")) {
                        memberName = value;
                    }
                }

                // Üyeyi listeye ekle
                members.add("ID: " + memberId + " - İsim: " + memberName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return members;
    }


    private static void deleteBook(JFrame frame, String bookId) {
        try {
            HttpURLConnection connection = createConnection("http://localhost:8080/api/books/" + bookId, "DELETE", finalToken);

            int status = connection.getResponseCode();

            // Başarı durumlarını kontrol et: 204 veya 200
            if (status == HttpURLConnection.HTTP_NO_CONTENT || status == HttpURLConnection.HTTP_OK) {
                JOptionPane.showMessageDialog(frame, "Kitap başarıyla silindi!", "Başarılı", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(frame, "Kitap silinemedi. Hata kodu: " + status, "Hata", JOptionPane.ERROR_MESSAGE);
            }

            connection.disconnect();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Bir hata oluştu: " + ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    
    private static JPanel createUserPanel(CardLayout cardLayout, JPanel mainPanel, JFrame frame) {
        JPanel userPanel = new JPanel();
        userPanel.setLayout(new GridLayout(6, 1, 10, 10));

        JLabel userLabel = new JLabel("Kullanıcı Paneli", SwingConstants.CENTER);
        JButton showAvailableBooks = new JButton("Kiralanabilir Kitaplar");
        JButton borrowBookButton = new JButton("Kitap Al");
        JButton returnBookButton = new JButton("Kitap İade Et");
        JButton viewBooksButton = new JButton("Aldığım Kitapları Görüntüle");
        JButton backToLoginButton = new JButton("Çıkış");

        userPanel.add(userLabel);
        userPanel.add(showAvailableBooks);
        userPanel.add(borrowBookButton);
        userPanel.add(returnBookButton);
        userPanel.add(viewBooksButton);
        userPanel.add(backToLoginButton);
        
        showAvailableBooks.addActionListener(e -> {
            // Kullanıcı giriş yaptıktan sonra aldığı token
            String token = finalToken;
            showAvailableBooks(frame, token);
        });

        
        borrowBookButton.addActionListener(e -> {
            // Kullanıcıdan kitap ID'sini al
            String bookId = JOptionPane.showInputDialog(frame, "Ödünç almak istediğiniz kitabın ID'sini girin:",
                                                        "Kitap Ödünç Al", JOptionPane.QUESTION_MESSAGE);

            if (bookId != null && !bookId.trim().isEmpty()) {
                // Kitap ödünç alma işlemini başlat
                borrowBook(frame, bookId.trim());
            } else {
                JOptionPane.showMessageDialog(frame, "Kitap ID'si boş olamaz!", "Hata", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        returnBookButton.addActionListener(e -> {
            // Kullanıcıdan kitap ID'sini alıyoruz
            String bookId = JOptionPane.showInputDialog(frame, "İade etmek istediğiniz kitabın ID'sini girin:", 
                                                         "Kitap İade Et", JOptionPane.QUESTION_MESSAGE);

            // Kullanıcı kitap ID'sini girmişse, kitap iade etme işlemi başlatılıyor
            if (bookId != null && !bookId.trim().isEmpty()) {
                // Token burada daha önce alınmış olmalı
                String token = finalToken;  // finalToken burada, giriş sonrası elde edilen token'ı temsil eder

                // Token ile iade işlemi başlatılıyor
                returnBook(frame, bookId.trim(), token);
            } else {
                // Kullanıcı kitap ID'sini boş bırakırsa hata mesajı gösteriyoruz
                JOptionPane.showMessageDialog(frame, "Kitap ID'si boş olamaz!", "Hata", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        viewBooksButton.addActionListener(e -> {
            // Kullanıcının ödünç aldığı kitapları görüntüle
            String token = finalToken;  // Kullanıcının giriş sonrası aldığı token
            viewMyBooks(frame, token, cardLayout, mainPanel);
        });




        backToLoginButton.addActionListener(e -> cardLayout.show(mainPanel, "login"));

        return userPanel;
    }
    
    public static void showAvailableBooks(JFrame frame, String token) {
        try {
            // API URL
            String urlString = "http://localhost:8080/api/books/available";
            URL url = new URL(urlString);

            // HTTP GET isteği oluştur
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Token ile kimlik doğrulama ekliyoruz
            connection.setRequestProperty("Authorization", "Bearer " + token);

            // Yanıt kodunu kontrol et
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) { // 200 OK
                // Yanıt verisini oku
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // Yanıt verilerini JSON olarak al
                String responseData = response.toString();

                // Kitapların listesini çözümle
                List<String> availableBooks = parseAvailableBooks(responseData);

                // Kitapları bir mesaj kutusunda göster
                StringBuilder message = new StringBuilder("Mevcut Kitaplar:\n\n");
                for (String book : availableBooks) {
                    message.append(book).append("\n\n"); // Kitapları alt alta ve boşlukla göster
                }

                JOptionPane.showMessageDialog(frame, message.toString(), "Kiralanabilir Kitaplar", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(frame, "Kitaplar alınamadı. Yanıt Kodu: " + responseCode, "Hata", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Hata oluştu: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private static List<String> parseAvailableBooks(String responseData) {
        List<String> books = new ArrayList<>();
        try {
            // JSON dizisini parçala
            String[] booksArray = responseData.substring(1, responseData.length() - 1).split("\\},\\{");

            for (String bookData : booksArray) {
                // JSON objesini parçalıyoruz
                String[] keyValuePairs = bookData.replace("{", "").replace("}", "").split(",");
                
                String bookId = "";
                String bookTitle = "";
                boolean isAvailable = false;

                // Key-Value çiftlerini ayırarak kitaba ait id, title ve is_available'ı buluyoruz
                for (String pair : keyValuePairs) {
                    String[] keyValue = pair.split(":");
                    String key = keyValue[0].replace("\"", "").trim();
                    String value = keyValue[1].replace("\"", "").trim();

                    if (key.equals("id")) {
                        bookId = value;
                    } else if (key.equals("title")) {
                        bookTitle = value;
                    } else if (key.equals("is_available")) {
                        isAvailable = Boolean.parseBoolean(value);
                    }
                }

                books.add("ID: " + bookId + " - Başlık: " + bookTitle);
                
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return books;
    }


    
    private static void borrowBook(JFrame frame, String bookId) {
        try {
            // Kitap ödünç almak için gerekli URL
            String urlString = "http://localhost:8080/api/books/loan";

            // JSON formatında gönderilecek veri
            String jsonInputString = String.format("{\"bookId\":\"%s\"}", bookId);

            // Sunucuya istek gönder
            HttpURLConnection connection = createConnection(urlString, "POST", finalToken);

            // JSON verisini sunucuya gönderiyoruz
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Yanıtı alıyoruz
            int status = connection.getResponseCode();
            if (status == HttpURLConnection.HTTP_OK) {
                // Başarılı ise kullanıcıya mesaj gösteriyoruz
                JOptionPane.showMessageDialog(frame, "Kitap başarıyla ödünç alındı!", "Başarılı", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Hata durumunda kullanıcıya hata mesajı gösteriyoruz
                JOptionPane.showMessageDialog(frame, "Kitap ödünç alınamadı. Hata: " + status, "Hata", JOptionPane.ERROR_MESSAGE);
            }

            // Bağlantıyı kapatıyoruz
            connection.disconnect();
        } catch (Exception ex) {
            // Hata durumunda kullanıcıya mesaj gösteriyoruz
            JOptionPane.showMessageDialog(frame, "Bir hata oluştu: " + ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private static void returnBook(JFrame frame, String bookId, String token) {
        try {
            // Kitap iade etmek için API endpoint: http://localhost:8080/api/books/return
            HttpURLConnection connection = createConnection("http://localhost:8080/api/books/return", "POST", token);

            // JSON verisini oluşturuyoruz (kitap ID'sini gönderiyoruz)
            String jsonInputString = "{\"bookId\": \"" + bookId + "\"}";

            // JSON verisini sunucuya gönderiyoruz
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Yanıtı alıyoruz
            int status = connection.getResponseCode();
            if (status == HttpURLConnection.HTTP_OK) {
                // Kitap başarıyla iade edildiğinde kullanıcıya bilgi veriyoruz
                JOptionPane.showMessageDialog(frame, "Kitap başarıyla iade edildi!", "Başarılı", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Hata durumunda kullanıcıya hata mesajı gösteriyoruz
                JOptionPane.showMessageDialog(frame, "Kitap iade edilemedi. Hata kodu: " + status, "Hata", JOptionPane.ERROR_MESSAGE);
            }

            connection.disconnect();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Bir hata oluştu: " + ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }
    
 
    public static void viewMyBooks(JFrame frame, String token, CardLayout cardLayout, JPanel mainPanel) {
        try {
            // API URL
            String urlString = "http://localhost:8080/api/books/my-books";
            URL url = new URL(urlString);

            // Bağlantıyı açıyoruz
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + token); // Token ile kimlik doğrulaması

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Response verisini okuma
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // JSON formatındaki yanıtı işleme
                String responseData = response.toString();
                List<String> books = parseBooks(responseData);

                // Yeni bir panel oluştur
                JPanel booksPanel = new JPanel();
                booksPanel.setLayout(new BoxLayout(booksPanel, BoxLayout.Y_AXIS));

                // Kitapları ekle
                for (String book : books) {
                    JLabel bookLabel = new JLabel(book);
                    booksPanel.add(bookLabel);
                    booksPanel.add(Box.createVerticalStrut(10)); // Boşluk eklemek için
                }

                // Geri dön butonu ekle
                JButton backButton = new JButton("Geri Dön");
                backButton.addActionListener(e -> {
                    cardLayout.show(mainPanel, "user"); // UserPanel'e geri dön
                });
                booksPanel.add(Box.createVerticalStrut(20)); // Butonun üstüne boşluk ekle
                booksPanel.add(backButton);

                // Main panelde bu yeni paneli göster
                mainPanel.add(booksPanel, "booksPanel");
                cardLayout.show(mainPanel, "booksPanel");

            } else {
                JOptionPane.showMessageDialog(frame, "Kitaplar alınamadı: " + responseCode);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Hata oluştu: " + e.getMessage());
        }
    }

    
    public static List<String> parseBooks(String responseData) {
        List<String> books = new ArrayList<>();

        // JSON verisini işleyip kitapları listeye ekliyoruz
        // "responseData" şu şekilde örnek bir JSON olabilir: [{"id":1,"title":"Kitap 1"}, {"id":2,"title":"Kitap 2"}]
        // Basitleştirilmiş JSON işleme
        String[] booksArray = responseData.substring(1, responseData.length() - 1).split("\\},\\{");

        for (String bookData : booksArray) {
            String[] keyValuePairs = bookData.replace("{", "").replace("}", "").split(",");
            String bookId = "";
            String bookTitle = "";

            for (String pair : keyValuePairs) {
                String[] keyValue = pair.split(":");
                String key = keyValue[0].replace("\"", "").trim();
                String value = keyValue[1].replace("\"", "").trim();

                if (key.equals("id")) {
                    bookId = value;
                } else if (key.equals("title")) {
                    bookTitle = value;
                }
            }

            // Kitap bilgilerini listeye ekliyoruz
            books.add("ID: " + bookId + " - Başlık: " + bookTitle);
        }

        return books;
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
        
        JButton backToLoginButton = new JButton("Geri");

     // GridBagConstraints ayarları
        gbc.gridx = 2;
        gbc.gridy = 5;
        registerPanel.add(backToLoginButton, gbc);


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
        
        backToLoginButton.addActionListener(e -> {
            // CardLayout kullanarak login paneline dön
            
            cardLayout.show(mainPanel, "login");
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

                    // Admin paneline dönüyoruz
                    cardLayout.show(mainPanel, "admin"); // "adminPanel" panel ismini doğru kontrol edin
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