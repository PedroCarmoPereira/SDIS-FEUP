import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

class TestApp {

    private TestApp() {
    }

    public static void main(String[] args) {

        if (args.length < 3 || args.length > 5) {
            System.out.println("Input error.");
            System.out.println("Usage: TestApp <peer_ap> <sub_protocol> <opnd_1> <opnd_2>");
            System.out.println("<peer_ap> => <IP address>:<port number> <Peer Id>");
            System.out.println("if <IP address> == null => <IP address> = localhost");
            return;
        }
        int port;
        String host;

        String[] arrOfStr = args[0].split(":", 0);
        if (arrOfStr.length == 1) {
            host = "localhost";
            port = Integer.parseInt(arrOfStr[0]);
        } else {
            if (arrOfStr[0] == "") {
                host = "localhost";
                port = Integer.parseInt(arrOfStr[1]);
            } else {
                host = arrOfStr[0];
                port = Integer.parseInt(arrOfStr[1]);
            }

        }

        String peerId = args[1];
        String operation = args[2].toUpperCase();
        if (!operation.equals("BACKUP") && !operation.equals("RESTORE") && !operation.equals("DELETE")
                && !operation.equals("RECLAIM") && !operation.equals("STATE")) {
            System.out.println("Input error.");
            System.out.println("Usage: TestApp <IP address>:<port number> <Peer Id> <sub_protocol> <opnd_1> <opnd_2>");
            System.out.println("if <IP address> == null => <IP address> = localhost");
            System.out.println("Available protocols are:");
            System.out.println("BACKUP");
            System.out.println("RESTORE");
            System.out.println("DELETE");
            System.out.println("RECLAIM");
            System.out.println("STATE");
            return;
        }

        try {
            Registry registry = LocateRegistry.getRegistry(host, port);
            RMI peer = (RMI) registry.lookup(peerId);

            switch (operation) {
                case "BACKUP":
                    peer.backup(args[3], Integer.parseInt(args[4]));
                    break;

                case "RESTORE":
                    break;

                case "DELETE":
                    break;

                case "RECLAIM":
                    break;

                case "STATE":
                    break;

                default:
                    System.out.println("Input error.");
                    System.out.println("Usage: TestApp <IP address>:<port number> <Peer Id> <sub_protocol> <opnd_1> <opnd_2>");
                    System.out.println("if <IP address> == null => <IP address> = localhost");
                    System.out.println("Available protocols are:");
                    System.out.println("BACKUP");
                    System.out.println("RESTORE");
                    System.out.println("DELETE");
                    System.out.println("RECLAIM");
                    System.out.println("STATE");
            }
        } catch (Exception e) {
            System.err.println("TestApp exception: " + e.toString());
            e.printStackTrace();
        }

    }
}