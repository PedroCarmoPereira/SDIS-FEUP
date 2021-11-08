import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

class TestApp {

    private TestApp() {
    }

    public static void printUsage() {
        System.out.println("Usage: TestApp <peer_ap> <sub_protocol> <opnd_1> <opnd_2>");
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

        if (args.length < 3 || args.length > 5) {
            System.out.println("Input error." + args.length);
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
            Registry registry = LocateRegistry.getRegistry(host, port);
            System.out.println("Looking for register @" + host);
            RMI peer = (RMI) registry.lookup(peerId);
            int r;
            switch (operation) {
                case "BACKUP":
                    r = peer.backup(args[3], Integer.parseInt(args[4]));
                    if(r == 0) System.out.println("BACKUP SUCESSFULL");
                    else System.out.println("BACKUP FAILED WITH ERROR CODE: " + r);
                    break;

                case "RESTORE":
                    r = peer.restore(args[3]);
                    if(r == 0) System.out.println("RESTORE SUCESSFULL");
                    else System.out.println("RESTORE FAILED WITH ERROR CODE: " + r);
                    break;

                case "DELETE":
                    r = peer.delete(args[3]);
                    if(r == 0) System.out.println("DELETE SUCESSFULL");
                    else System.out.println("DELETE FAILED WITH ERROR CODE: " + r);
                    break;

                case "STATE":
                    System.out.println(peer.state());
                    break;

                case "RECLAIM":
                    r = peer.reclaim((long)Integer.parseInt(args[3]));
                    if(r == 0) System.out.println("RECLAIM SUCESSFULL");
                    else System.out.println("RECLAIM FAILED WITH ERROR CODE: " + r);
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