import java.net.*;
import java.util.*;
import java.io.*;

public class MainRouter {

    public static double[] Vector_Routers;
    public static String[] Hop_list;
    public static DatagramSocket Socket_router;
    public static String[] Neighbours;
    public static File File_router;
    public static int Count_of_Routers = 1;
    public static double[][] Router_Vectors;
    public static String[] Router_Nodes;
    public static int[] Ports_router;
    public static int router_ID;


    public static void setParametres(int len, String[] args, int id, String parent) {

        Router_Vectors = new double[len][len];
        Ports_router = new int[len];
        Router_Nodes = new String[len];

        for (int i = 0; i < len; i++) {
            Arrays.fill(Router_Vectors[i], Double.MAX_VALUE);
            Router_Vectors[i][i] = 0.0;
            String[] temp = args[i + 3].split(":");
            Router_Nodes[i] = temp[0];
            Ports_router[i] = Integer.parseInt(temp[1]);
        }

        router_ID = id;
        Vector_Routers = new double[len];
        Hop_list = new String[len];
        Arrays.fill(Vector_Routers, Double.MAX_VALUE);
        Vector_Routers[router_ID - 1] = 0.0;
        File_router = new File(parent + "/" + Router_Nodes[router_ID - 1] + ".dat");
        try {
            Socket_router = new DatagramSocket(Ports_router[router_ID - 1]);
        } catch (SocketException e) {

            e.printStackTrace();
        }
        
        System.out.println("Router " + Router_Nodes[router_ID - 1] + " is Working..!");

    }

    public synchronized static void updateNetworks(String[] vector, int port_number) {

        int index = 0;
        int ports_Length=Ports_router.length;
        while(index < ports_Length)
        {
            if (Ports_router[index] == port_number)
            {
                break;
            }
            index++;
        }
        if (index == ports_Length)
        {
            return;
        }
        for (int i = 0; i < vector.length; i++)
        {
            Router_Vectors[index][i] = Double.parseDouble(vector[i]);
        }
    }
	
	public static void distanceVectorAlgorithn() {
        for (int i = 0; i < Neighbours.length; i++) {
            int ind = indexFinder(Neighbours[i]);
            for (int j = 0; j < Vector_Routers.length; j++) {
                if (j == router_ID - 1) {
                    continue;
                } else if (i == 0) {

                    Vector_Routers[j] = Router_Vectors[router_ID - 1][ind] + Router_Vectors[ind][j];
                    Hop_list[j] = Neighbours[i];
                } else {

                    if (Vector_Routers[j] > Router_Vectors[router_ID - 1][ind] + Router_Vectors[ind][j]) {
                        Hop_list[j] = Neighbours[i];
                        Vector_Routers[j] = Router_Vectors[router_ID - 1][ind] + Router_Vectors[ind][j];
                    }

                }
            }
        }


    }

    public static void broadCast() {

        try {
            for (int i = 0; i < Neighbours.length; i++) {
                String data = "";
                for (int j = 0; j < Vector_Routers.length; j++) {
                    if (Neighbours[i].equals(Hop_list[j])) {
                        data = data + Double.MAX_VALUE + ":";
                    } else {
                        data += Vector_Routers[j] + ":";
                    }
                }
                DatagramPacket packet = new DatagramPacket(data.getBytes(), data.getBytes().length);
                packet.setAddress(InetAddress.getByName("localhost"));
                packet.setPort(Ports_router[indexFinder(Neighbours[i])]);
                Socket_router.send(packet);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void output() {
        System.out.println("> Output Number is " + Count_of_Routers++);
        System.out.println();
        String src = Router_Nodes[router_ID - 1];
        for (int i = 0; i < Vector_Routers.length; i++) {
            if (i != (router_ID - 1)) {
                String dest = Router_Nodes[i];
                if (Vector_Routers[i] == Double.MAX_VALUE) {
                    System.out.println("shortest path from " + src + "to" + dest + ": " + " no route found");
                } else {
                    System.out.println("shortest path from " + src + "to" + dest + " : the next hop is " + Hop_list[i]
                            + " and its cost is " + Vector_Routers[i]);
                }
            }
        }
    }

    public static void readInput() {
        boolean status = true;
        while (status) {
            try {
                String controltype = "u";
                byte[] data = new byte[1024];
                int size = data.length;
                DatagramPacket packet = new DatagramPacket(data, size);
                Socket_router.receive(packet);
                int length = packet.getLength();
                String vector = new String(packet.getData(), 0, length);
                MainThread updateThread = new MainThread(controltype, vector, packet.getPort());
                updateThread.start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void read() {
        try {
            Arrays.fill(Router_Vectors[router_ID - 1], Double.MAX_VALUE);
            Router_Vectors[router_ID - 1][router_ID - 1] = 0.0;
            BufferedReader br = new BufferedReader(new FileReader(File_router));
            int length = Integer.parseInt(br.readLine());
            Neighbours = new String[length];
            for (int i = 0; i < length; i++) {

                String[] temp = br.readLine().split(" ");
                int ind = indexFinder(temp[0]);
                Neighbours[i] = temp[0];

                if (Count_of_Routers == 1) {
                    Hop_list[ind] = temp[0];
                    Vector_Routers[ind] = Double.parseDouble(temp[1]);
                } else {

                }
                Router_Vectors[router_ID - 1][ind] = Double.parseDouble(temp[1]);

            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int indexFinder(String temp) {
        int pos = -1;
        for (int i = 0; i < Router_Nodes.length; i++) {
            if (Router_Nodes[i].equals(temp)) {
                pos = i;
                break;
            }
        }
        return pos;
    }

    public static void main(String args[]) {
        int total = Integer.parseInt(args[2]);
        int currentNumber = Integer.parseInt(args[0]);
        String path = args[1];
        setParametres(total, args, currentNumber, path);

        MainThread readThread = new MainThread("r");
        readThread.start();

        MainThread writeThread = new MainThread("w");
        writeThread.start();

        while (true)
            ;
    }
}

class MainThread extends Thread {
    private String controltype;
    private String vector;
    private int port_number;

    public MainThread(String controltype, String vector, int port) {
        this.controltype = controltype;
        this.vector = vector;
        this.port_number = port;
    }

    public MainThread(String controltype) {
        this.controltype = controltype;
    }

    public void run() {

        if (controltype.equalsIgnoreCase("r")) {
            MainRouter.readInput();

        } else if (controltype.equalsIgnoreCase("w")) {

            while (true) {
                try {

                    MainRouter.read();
                    MainRouter.output();
                    MainRouter.broadCast();

                    Thread.sleep(6000);

                    MainRouter.distanceVectorAlgorithn();

                    Thread.sleep(9000);

                } catch (Exception e) {
                }
            }
        } else if (controltype.equalsIgnoreCase("u")) {

            MainRouter.updateNetworks(vector.split(":"), port_number);
        }
    }
}

