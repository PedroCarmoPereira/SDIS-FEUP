import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

class TestApp {

    public static RMI peer = null;

    private TestApp() {
    }

    public static void printUsage() {
        System.out.println("Usage: TestApp <peer_ap> <sub_protocol> <file>");
        System.out.println("<peer_ap> => <IP address>:<port number> <Peer Id>");
        System.out.println("if <IP address> == null => <IP address> = localhost");
        System.out.println("Available protocols are:");
        System.out.println("BACKUP");
        System.out.println("RESTORE");
        System.out.println("DELETE");
        System.out.println("RECLAIM");
        System.out.println("STATE");
    }

    public static void main(String[] args) {

        if (args.length < 3 || args.length > 4) {
            System.out.println("Input error.");
            printUsage();
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
                && !operation.equals("RECLAIM") && !operation.equals("STATE") ) {
            System.out.println("Input error.");
            printUsage();
            return;
        }

        try {
            final Registry registry = LocateRegistry.getRegistry(host, port);
            System.out.println("Looking for register @" + host);
            TestApp.peer = (RMI) registry.lookup(peerId);

            switch (operation) {
                case "BACKUP":
                    peer.backup(args[3]);
                    break;

                case "RESTORE":
                    peer.restore(args[3]);
                    break;

                case "DELETE":
                    peer.delete(args[3]);
                    break;

                case "STATE":
                    break;

                case "RECLAIM":
                    break;
                default:
                    System.out.println("Input error.");
                    printUsage();
            }
        } catch (Exception e) {
            System.err.println("TestApp exception: " + e.toString());
            e.printStackTrace();
        }

    }
}