import java.io.*;
import java.net.*;
import java.util.*;
import java.text.SimpleDateFormat;

/**
 * Chat Server Application
 * This server handles all client connections and relays messages
 */
public class Server {
    // Server port number
    private static final int PORT = 5000;
    
    // List to keep connected clients
    private static Set<ClientHandler> clientHandlers = new HashSet<>();
    
    public static void main(String[] args) {
        System.out.println("🚀 Chat Server starting...");
        System.out.println("📡 Port: " + PORT);
        
        // Display all network IP addresses
        System.out.println("\n🌐 Server IP Addresses:");
        System.out.println("==================================");
        try {
            // Get localhost address
            InetAddress localhost = InetAddress.getLocalHost();
            System.out.println("💻 Local: " + localhost.getHostAddress());
            
            // Get all network interfaces
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                
                // Skip loopback and inactive interfaces
                if (iface.isLoopback() || !iface.isUp()) {
                    continue;
                }
                
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    
                    // Only show IPv4 addresses
                    if (addr instanceof java.net.Inet4Address) {
                        System.out.println("🌐 Network (" + iface.getDisplayName() + "): " + addr.getHostAddress());
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("⚠️ Could not determine IP addresses");
        }
        
        System.out.println("==================================");
        System.out.println("ℹ️ Use these IP addresses for remote connections");
        System.out.println("⏳ Waiting for clients...\n");
        
        try (ServerSocket serverSocket = new ServerSocket(PORT, 50, InetAddress.getByName("0.0.0.0"))) {
            
            // Continuously accept client connections
            while (true) {
                Socket socket = serverSocket.accept();
                
                // Create a handler thread for new client
                ClientHandler clientHandler = new ClientHandler(socket);
                clientHandlers.add(clientHandler);
                
                // Start thread
                Thread thread = new Thread(clientHandler);
                thread.start();
            }
            
        } catch (IOException e) {
            System.err.println("❌ Server Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * ClientHandler class - Separate thread for each client
     */
    private static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String username;
        
        public ClientHandler(Socket socket) {
            this.socket = socket;
        }
        
        @Override
        public void run() {
            try {
                // Setup input/output streams
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                
                // Get username from client
                username = in.readLine();
                
                System.out.println("✅ " + username + " has joined!");
                
                // Notify everyone that a new user has joined
                broadcastMessage("SERVER", username + " has joined the chat! 👋");
                
                // Receive messages from client
                String message;
                while ((message = in.readLine()) != null) {
                    if (message.equals("/quit")) {
                        break;
                    }
                    
                    // Log message (show simplified version for images)
                    if (message.startsWith("IMAGE:")) {
                        System.out.println(username + ": [sent an image 📷]");
                    } else if (message.startsWith("TEXT:")) {
                        System.out.println(username + ": " + message.substring(5));
                    } else {
                        System.out.println(username + ": " + message);
                    }
                    
                    // Broadcast message to everyone
                    broadcastMessage(username, message);
                }
                
            } catch (IOException e) {
                System.err.println("❌ Client Handler Error: " + e.getMessage());
            } finally {
                // Cleanup when closing connection
                closeConnection();
            }
        }
        
        /**
         * Method to send message to all clients
         */
        private void broadcastMessage(String sender, String message) {
            // Add current timestamp
            String timestamp = new SimpleDateFormat("hh:mm a").format(new Date());
            String formattedMessage = "[" + sender + " " + timestamp + "]: " + message;
            
            // Send to all connected clients
            for (ClientHandler client : clientHandlers) {
                if (client.out != null) {
                    client.out.println(formattedMessage);
                }
            }
        }
        
        /**
         * Close connection and cleanup resources
         */
        private void closeConnection() {
            try {
                // Remove from list
                clientHandlers.remove(this);
                
                if (username != null) {
                    System.out.println("❌ " + username + " has left the chat!");
                    broadcastMessage("SERVER", username + " has left the chat! 👋");
                }
                
                // Close resources
                if (in != null) in.close();
                if (out != null) out.close();
                if (socket != null) socket.close();
                
            } catch (IOException e) {
                System.err.println("❌ Close Error: " + e.getMessage());
            }
        }
    }
}
