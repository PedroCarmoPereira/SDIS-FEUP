import java.util.*;
import java.io.IOException;
import java.net.*;

class Server extends Thread
{ 
    private MulticastSocket socket;
    private Map<String, String> dns_map = new HashMap<String, String>();
    public Map<String, String> getDNSMap(){return this.dns_map;}

    public Server(int port) throws SocketException, IOException {socket = new MulticastSocket(port);}

    public String[] lookup(String dns_name){
        String [] ret = {dns_name, null};
        if (dns_map.get(dns_name) != null) ret[1] = this.dns_map.get(dns_name);
        return ret;
    }

    public int register(String dns_name, String ip_addr){
        if(this.lookup(dns_name)[1] != null) return -1;
        this.dns_map.put(dns_name, ip_addr); 
        return this.dns_map.size();
    }


    public void service() throws IOException{
        while (true) {
            byte[] buff = new byte[512];
            DatagramPacket request = new DatagramPacket(buff, buff.length);
            socket.receive(request);
            
            //for(int i = 0; i < request.getData().length; i++) System.out.println("RECEIVING: " +  request.getData()[i]);

            String req_data = new String(request.getData());
            String[] params = req_data.split(" ");
            System.out.print("Server: ");
            System.out.print(params[0] + " ");
            System.out.print(params[1] + " ");
            
            if (!(params.length > 2)){
                System.out.println();
                params[1] = params[1].substring(0, params[1].indexOf('\0'));
            }
            else {
                System.out.println(params[2]);
                params[2] = params[2].substring(0, params[2].indexOf('\0'));
            }
            byte[] buffer;
            if (params[0].equals("register")){
                int k = this.register(params[1], params[2]);
                String s = k + "";
                buffer = s.getBytes();
            }

            //TODO: VER O QUE SE PASSA COM PARAMS[1] QUE NÃO FUNCIONA COM O LOOKUP :P
            else if (params[0].equals("lookup")){
                String [] res = this.lookup(params[1]);
                if (res[1] != null) {
                    buffer = res[1].getBytes();
                }
                else buffer = "NOT_FOUND".getBytes();
            }

            else buffer = "".getBytes();

            InetAddress clientAddress = request.getAddress();
            int clientPort = request.getPort();
 
            DatagramPacket response = new DatagramPacket(buffer, buffer.length, clientAddress, clientPort);
            socket.send(response);
        }
    }

    public static void main(String args[]) {
        
        if(args.length < 1){
            System.out.println("Usage: java Server <portno>");
            return;
        }
        
        int port;
        try{ port = Integer.parseInt(args[0]);}
        catch (NumberFormatException ex){
            System.out.println("Invalid port number");
            return;
        }

        Server self;
        try {
            self = new Server(port);
            self.service();
        } catch (SocketException ex) {
            System.out.println("Socket error: " + ex.getMessage());
            return;
        } catch (IOException ex){
            System.out.println("IO error: " + ex.getMessage());
            return;
        }

    } 
} 