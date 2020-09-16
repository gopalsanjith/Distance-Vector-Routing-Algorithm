import java.io.*;
import java.util.*;

public class Admin{
    private static Set<Integer> portSet = new HashSet<Integer>();
    
    public static void main(String[] args) {
       if(args.length != 1) { 
         if (args.length == 0) {
             System.out.println("Invalid request! Please provide the input file directory");
         } else if (args.length > 1) {
             System.out.println("Invalid request! More than 1 arguement is provided");
         }
         return;
       }
       readFilePath(args[0]);
    }
    
    private static void readFilePath(String directory) {

        // Creating the instance of the desired file path
         File path = new File(directory);

         if (!path.isDirectory()) {
             System.out.println("Given directoy path is Incorrect");
             return;
         }
         
         File router[] = path.listFiles();
         int size = router.length;
         int[] portNumber = new int[size];
         String allRouters = "";

         System.out.println("Provide port numbers to " + size +" Routers respectively");
         Scanner scanner = new Scanner(System.in);

         for (int i = 0; i < size; i++) {
             String routerName = router[i].getName();
             routerName = routerName.substring(0, routerName.indexOf(".dat"));
             System.out.println("Enter the portNumber number for Router: " + routerName);

             boolean status = true;

             while (status) {
                 try {
                     int number = Integer.parseInt(scanner.nextLine());

                     if (number <= 1024 || number >= 65536) {
                         throw new NumberFormatException();
                     }
                     if (portSet.contains(number)) {
                         throw new Exception();
                     }
                     portNumber[i] = number;
                     portSet.add(number);
                     status = false;
                 } catch (NumberFormatException e) {
                     System.out.println("Enter a valid Port number that is > 1024 && < 65536");
                     status = true;
                 } catch (Exception e) {
                     System.out.println("Port number is already taken. Please provide another port number");
                     status = true;
                 }
             }
             allRouters += " " + routerName + ":" + portNumber[i];
         }
         scanner.close();
         processBuilder(router,size,allRouters);
    }
    
    private static void processBuilder( File router[], int size, String allRouters) {
    	for (int i = 0; i < size; i++) {
            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", "start java MainRouter " + (i + 1) + " \""
                    + router[i].getParent().replace("\\", "/") + "\" " + size + allRouters);

            try {
                processBuilder.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Distance Vector Algorithm Started");
    }
}

