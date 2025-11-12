import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

/**
 * Chat Client Application with Swing GUI
 * Users can chat using this client application
 */
public class Client extends JFrame {
    // Server connection details
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 5000;
    
    // Network components
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    
    // UI Components
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private String username;
    
    /**
     * Constructor - Setup UI
     */
    public Client() {
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
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chatArea.setBackground(Color.WHITE);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setMargin(new Insets(10, 10, 10, 10));
        
        // Add scrollbar
        JScrollPane scrollPane = new JScrollPane(chatArea);
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
        
        // Send button
        sendButton = new JButton("📤 Send");
        sendButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        sendButton.setBackground(new Color(70, 130, 180));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.setBorderPainted(false);
        sendButton.setPreferredSize(new Dimension(100, 40));
        sendButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add hover effect
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
        
        // Add components to bottom panel
        bottomPanel.add(messageField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);
        
        // Add all components to main panel
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        // Add main panel to frame
        add(mainPanel);
    }
    
    /**
     * Establish connection with server
     */
    private void connectToServer() {
        try {
            // Create socket connection
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            
            // Setup input/output streams
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            // Send username to server
            out.println(username);
            
            // Create separate thread to receive messages
            Thread messageReceiver = new Thread(new MessageReceiver());
            messageReceiver.start();
            
            // Connection successful message
            appendToChat("✅ Connected to server!\n");
            
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
            out.println(message);
            
            // Clear input field
            messageField.setText("");
            
            // Return focus
            messageField.requestFocus();
        }
    }
    
    /**
     * Append message to chat area
     */
    private void appendToChat(String message) {
        chatArea.append(message + "\n");
        
        // Automatically scroll to bottom
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
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
                    SwingUtilities.invokeLater(() -> appendToChat(msg));
                }
                
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> {
                    appendToChat("\n❌ Disconnected from server!");
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
