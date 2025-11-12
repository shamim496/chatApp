import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.Base64;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

/**
 * Chat Client Application with Swing GUI
 * Users can chat using this client application
 */
public class Client extends JFrame {
    // Server connection details
    private String serverHost;
    private static final int SERVER_PORT = 5000;
    
    // Network components
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    
    // UI Components
    private JTextPane chatPane;
    private StyledDocument chatDoc;
    private JTextField messageField;
    private JButton sendButton;
    private JButton imageButton;
    private String username;
    
    // Message styles
    private Style systemStyle;
    private Style ownMessageStyle;
    private Style otherMessageStyle;
    private Style serverStyle;
    private Style timestampStyle;
    
    /**
     * Constructor - Setup UI
     */
    public Client() {
        // Show dialog to get server IP address
        serverHost = JOptionPane.showInputDialog(
            null,
            "Enter Server IP Address:\n(Use 'localhost' for same computer)",
            "Connect to Server",
            JOptionPane.QUESTION_MESSAGE
        );
        
        // If user cancels or empty, use localhost
        if (serverHost == null || serverHost.trim().isEmpty()) {
            serverHost = "localhost";
        }
        serverHost = serverHost.trim();
        
        // Show dialog to get username
        username = JOptionPane.showInputDialog(
            this,
            "Enter your name:",
            "Chat Login",
            JOptionPane.QUESTION_MESSAGE
        );
        
        // If user cancels or provides empty name
        if (username == null || username.trim().isEmpty()) {
            username = "User" + (int)(Math.random() * 1000);
        }
        
        // Window setup
        setTitle("💬 Chat Application - " + username);
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center on screen
        
        // Create UI
        createUI();
        
        // Connect to server
        connectToServer();
        
        // Handle window close event
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                disconnect();
            }
        });
    }
    
    /**
     * Create UI Components
     */
    private void createUI() {
        // Main panel with BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(new Color(240, 240, 245));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // ============ Header Panel ============
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(70, 130, 180));
        headerPanel.setPreferredSize(new Dimension(0, 50));
        
        JLabel headerLabel = new JLabel("💬 Chat Room", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);
        
        // ============ Chat Area ============
        chatPane = new JTextPane();
        chatPane.setEditable(false);
        chatPane.setBackground(new Color(245, 245, 250));
        chatPane.setMargin(new Insets(10, 10, 10, 10));
        
        // Setup styled document
        chatDoc = chatPane.getStyledDocument();
        initializeStyles();
        
        // Add scrollbar
        JScrollPane scrollPane = new JScrollPane(chatPane);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        
        // ============ Bottom Panel (Message Input + Send Button) ============
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 0));
        bottomPanel.setBackground(new Color(240, 240, 245));
        bottomPanel.setPreferredSize(new Dimension(0, 60));
        
        // Message input field
        messageField = new JTextField();
        messageField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        messageField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 180, 180), 1),
            new EmptyBorder(5, 10, 5, 10)
        ));
        
        // Image/Photo button
        imageButton = new JButton("📁 Upload");
        imageButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        imageButton.setBackground(new Color(76, 175, 80));
        imageButton.setForeground(Color.WHITE);
        imageButton.setFocusPainted(false);
        imageButton.setBorderPainted(false);
        imageButton.setPreferredSize(new Dimension(95, 40));
        imageButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        imageButton.setToolTipText("Upload and send image");
        
        // Image button hover effect
        imageButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                imageButton.setBackground(new Color(66, 150, 70));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                imageButton.setBackground(new Color(76, 175, 80));
            }
        });
        
        // Image button click event
        imageButton.addActionListener(e -> selectAndSendImage());
        
        // Send button
        sendButton = new JButton("📤 Send");
        sendButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        sendButton.setBackground(new Color(70, 130, 180));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.setBorderPainted(false);
        sendButton.setPreferredSize(new Dimension(95, 40));
        sendButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Send button hover effect
        sendButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                sendButton.setBackground(new Color(60, 110, 160));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                sendButton.setBackground(new Color(70, 130, 180));
            }
        });
        
        // Send button click event
        sendButton.addActionListener(e -> sendMessage());
        
        // Send message on Enter key press
        messageField.addActionListener(e -> sendMessage());
        
        // Button panel for image and send buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonPanel.setBackground(new Color(240, 240, 245));
        buttonPanel.add(imageButton);
        buttonPanel.add(sendButton);
        
        // Add components to bottom panel
        bottomPanel.add(messageField, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);
        
        // Add all components to main panel
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        // Add main panel to frame
        add(mainPanel);
    }
    
    /**
     * Initialize message styles with different colors
     */
    private void initializeStyles() {
        // System messages (connections, errors)
        systemStyle = chatPane.addStyle("System", null);
        StyleConstants.setForeground(systemStyle, new Color(46, 125, 50));
        StyleConstants.setFontSize(systemStyle, 13);
        StyleConstants.setItalic(systemStyle, true);
        
        // Own messages
        ownMessageStyle = chatPane.addStyle("Own", null);
        StyleConstants.setForeground(ownMessageStyle, new Color(13, 71, 161));
        StyleConstants.setFontSize(ownMessageStyle, 14);
        StyleConstants.setBold(ownMessageStyle, true);
        
        // Other users' messages
        otherMessageStyle = chatPane.addStyle("Other", null);
        StyleConstants.setForeground(otherMessageStyle, new Color(74, 20, 140));
        StyleConstants.setFontSize(otherMessageStyle, 14);
        StyleConstants.setBold(otherMessageStyle, true);
        
        // Server messages
        serverStyle = chatPane.addStyle("Server", null);
        StyleConstants.setForeground(serverStyle, new Color(211, 47, 47));
        StyleConstants.setFontSize(serverStyle, 13);
        StyleConstants.setBold(serverStyle, true);
        
        // Timestamp
        timestampStyle = chatPane.addStyle("Timestamp", null);
        StyleConstants.setForeground(timestampStyle, new Color(117, 117, 117));
        StyleConstants.setFontSize(timestampStyle, 11);
    }
    
    /**
     * Establish connection with server
     */
    private void connectToServer() {
        try {
            // Create socket connection
            appendSystemMessage("⏳ Connecting to " + serverHost + ":" + SERVER_PORT + "...");
            socket = new Socket(serverHost, SERVER_PORT);
            
            // Setup input/output streams
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            // Send username to server
            out.println(username);
            
            // Create separate thread to receive messages
            Thread messageReceiver = new Thread(new MessageReceiver());
            messageReceiver.start();
            
            // Connection successful message
            appendSystemMessage("✅ Connected to server!");
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                this,
                "❌ Failed to connect to server!\nMake sure the server is running.",
                "Connection Error",
                JOptionPane.ERROR_MESSAGE
            );
            System.exit(1);
        }
    }
    
    /**
     * Method to send message
     */
    private void sendMessage() {
        String message = messageField.getText().trim();
        
        // Check for empty message
        if (!message.isEmpty() && out != null) {
            // Send message to server
            out.println("TEXT:" + message);
            
            // Clear input field
            messageField.setText("");
            
            // Return focus
            messageField.requestFocus();
        }
    }
    
    /**
     * Select and send image
     */
    private void selectAndSendImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Image");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Image Files", "jpg", "jpeg", "png", "gif", "bmp"));
        
        int result = fileChooser.showOpenDialog(this);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            
            try {
                // Read image
                BufferedImage image = ImageIO.read(selectedFile);
                
                if (image == null) {
                    JOptionPane.showMessageDialog(this, 
                        "Invalid image file!", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Resize if too large (max 400px width)
                int maxWidth = 400;
                if (image.getWidth() > maxWidth) {
                    int newHeight = (int)((double)maxWidth / image.getWidth() * image.getHeight());
                    Image scaledImage = image.getScaledInstance(maxWidth, newHeight, Image.SCALE_SMOOTH);
                    BufferedImage resizedImage = new BufferedImage(maxWidth, newHeight, BufferedImage.TYPE_INT_RGB);
                    Graphics2D g = resizedImage.createGraphics();
                    g.drawImage(scaledImage, 0, 0, null);
                    g.dispose();
                    image = resizedImage;
                }
                
                // Convert to Base64
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(image, "jpg", baos);
                byte[] imageBytes = baos.toByteArray();
                String base64Image = Base64.getEncoder().encodeToString(imageBytes);
                
                // Send image data
                if (out != null) {
                    out.println("IMAGE:" + base64Image);
                    appendSystemMessage("📷 Image sent!");
                }
                
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, 
                    "Error loading image: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Append system message (connections, errors)
     */
    private void appendSystemMessage(String message) {
        try {
            chatDoc.insertString(chatDoc.getLength(), "\n" + message + "\n", systemStyle);
            chatPane.setCaretPosition(chatDoc.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Append formatted chat message with username and timestamp
     */
    private void appendChatMessage(String fullMessage) {
        try {
            // Parse message format: [USERNAME TIMESTAMP]: MESSAGE
            if (fullMessage.startsWith("[SERVER")) {
                // Server announcement
                chatDoc.insertString(chatDoc.getLength(), "\n📢 ", null);
                
                int endBracket = fullMessage.indexOf("]");
                String content = fullMessage.substring(endBracket + 2).trim();
                chatDoc.insertString(chatDoc.getLength(), content, serverStyle);
                chatDoc.insertString(chatDoc.getLength(), "\n", null);
                
            } else if (fullMessage.startsWith("[")) {
                // Regular user message
                int endBracket = fullMessage.indexOf("]");
                String headerPart = fullMessage.substring(1, endBracket);
                String messagePart = fullMessage.substring(endBracket + 2).trim();
                
                // Split username and timestamp
                int lastSpace = headerPart.lastIndexOf(" ");
                String sender = headerPart.substring(0, lastSpace);
                String timestamp = headerPart.substring(lastSpace + 1);
                
                // Add newline for spacing
                chatDoc.insertString(chatDoc.getLength(), "\n", null);
                
                // Choose style based on sender
                Style nameStyle = sender.equals(username) ? ownMessageStyle : otherMessageStyle;
                
                // Add sender name with emoji
                String emoji = sender.equals(username) ? "💙 " : "💜 ";
                chatDoc.insertString(chatDoc.getLength(), emoji + sender, nameStyle);
                
                // Add timestamp
                chatDoc.insertString(chatDoc.getLength(), " • " + timestamp, timestampStyle);
                
                // Check if message is an image or text
                if (messagePart.startsWith("IMAGE:")) {
                    // Display image
                    String base64Image = messagePart.substring(6);
                    displayImage(base64Image);
                } else if (messagePart.startsWith("TEXT:")) {
                    // Display text message
                    String textContent = messagePart.substring(5);
                    Style messageTextStyle = chatPane.addStyle("MessageText", null);
                    StyleConstants.setForeground(messageTextStyle, new Color(33, 33, 33));
                    StyleConstants.setFontSize(messageTextStyle, 14);
                    chatDoc.insertString(chatDoc.getLength(), "\n  " + textContent + "\n", messageTextStyle);
                } else {
                    // Legacy format without prefix
                    Style messageTextStyle = chatPane.addStyle("MessageText", null);
                    StyleConstants.setForeground(messageTextStyle, new Color(33, 33, 33));
                    StyleConstants.setFontSize(messageTextStyle, 14);
                    chatDoc.insertString(chatDoc.getLength(), "\n  " + messagePart + "\n", messageTextStyle);
                }
                
            } else {
                // Fallback for any other format
                chatDoc.insertString(chatDoc.getLength(), fullMessage + "\n", null);
            }
            
            // Auto-scroll to bottom
            chatPane.setCaretPosition(chatDoc.getLength());
            
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Display image in chat
     */
    private void displayImage(String base64Image) {
        try {
            // Decode Base64
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            
            if (image != null) {
                // Insert newline and image
                chatDoc.insertString(chatDoc.getLength(), "\n  ", null);
                
                // Insert image as icon
                ImageIcon imageIcon = new ImageIcon(image);
                Style imageStyle = chatPane.addStyle("ImageStyle", null);
                StyleConstants.setIcon(imageStyle, imageIcon);
                chatDoc.insertString(chatDoc.getLength(), " ", imageStyle);
                
                chatDoc.insertString(chatDoc.getLength(), "\n", null);
            }
            
        } catch (Exception e) {
            try {
                Style errorStyle = chatPane.addStyle("Error", null);
                StyleConstants.setForeground(errorStyle, Color.RED);
                chatDoc.insertString(chatDoc.getLength(), "\n  [Error displaying image]\n", errorStyle);
            } catch (BadLocationException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        }
    }
    
    /**
     * Disconnect from server
     */
    private void disconnect() {
        try {
            if (out != null) {
                out.println("/quit");
            }
            
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
            
        } catch (IOException e) {
            System.err.println("❌ Disconnect Error: " + e.getMessage());
        }
    }
    
    /**
     * MessageReceiver class - To receive messages from server
     */
    private class MessageReceiver implements Runnable {
        @Override
        public void run() {
            try {
                String message;
                
                // Continuously receive messages from server
                while ((message = in.readLine()) != null) {
                    final String msg = message;
                    
                    // Use SwingUtilities to update UI
                    SwingUtilities.invokeLater(() -> appendChatMessage(msg));
                }
                
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> {
                    appendSystemMessage("❌ Disconnected from server!");
                });
            }
        }
    }
    
    /**
     * Main method - Start application
     */
    public static void main(String[] args) {
        // Set Look and Feel (System default)
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Create and show client window
        SwingUtilities.invokeLater(() -> {
            Client client = new Client();
            client.setVisible(true);
        });
    }
}
