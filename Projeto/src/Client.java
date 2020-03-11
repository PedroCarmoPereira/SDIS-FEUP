import java.io.*;
import java.net.*;
import java.util.Scanner;

class Client {

    public static void main(String[] args) {
        int operation = 0;
        Boolean goodInput = false;
        System.out.println("1. Backup file");
        System.out.println("2. Restore file");
        System.out.println("3. Delete file");
        System.out.println("4. Manage local service storage");
        System.out.println("5. Retrieve local service state information");

        while(!goodInput){
            try{
                System.out.print("\nChoose your operation:");
                Scanner in = new Scanner(System.in);
                operation = in.nextInt();
                if(operation > 0 && operation < 6)
                    goodInput = true;
                else
                    System.out.println("Input error. Insert an integer between 1 and 5.");
            }
            catch(Exception ex){
                System.out.println("Input error. Insert an integer.");
            }
        }

        if(operation==1)
            new Backup();
    }
}