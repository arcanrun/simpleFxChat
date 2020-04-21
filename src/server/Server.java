package server;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Vector;

public class Server {
    public static final int PORT = 8189;
    private Socket socket;
    private Vector<ActiveClient> activeClients;
    private AuthService authService;


    public Server() {
        activeClients = new Vector<>();
        authService = new AuthService();
        authService.connect();
        try (ServerSocket server = new ServerSocket(PORT);) {
            System.out.println("server has been started");
            while (true) {
                System.out.println("waiting for activeClients...");
                socket = server.accept();
                System.out.println("ActiveClient :" + socket.getInetAddress() + " " + socket.getPort() + " has been conected");
                new ActiveClient(socket, this, authService);
            }


        } catch (IOException e) {

            e.printStackTrace();
        } finally {
            authService.disconnect();
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendListOfActiveClient() {
       StringBuilder activeClientsString = new StringBuilder("/activeclients±");
        for (ActiveClient a: activeClients) {
            activeClientsString.append(a.getLogin()+" ");
        }

            broadCastMsg(activeClientsString.toString().trim());
    }

    public void subscribe(ActiveClient activeClient) {

        activeClients.add(activeClient);
        sendListOfActiveClient();
    }

    public boolean isAlreadyLogin(ActiveClient activeClient) {
        for (ActiveClient o : activeClients
        ) {
            if (o.getLogin().equals(activeClient.getLogin())) {
                return true;
            }
        }
        return false;
    }

    public void unsubscribe(ActiveClient activeClient) {
        activeClients.remove(activeClient);
        System.out.println("ActiveClient: " + "has been disconnected");
        sendListOfActiveClient();
    }

    public void broadCastMsg(String msg) {
        for (ActiveClient o : activeClients) {
            o.sendMsg(msg);
        }
    }

    public void privateMessage(String whom, String msg, ActiveClient from) {

        for (ActiveClient o : activeClients) {
            if (o.getLogin().equals(whom)) {
                o.sendMsg(from.getLogin() + ": " + msg);
            }

        }
        from.sendMsg(from.getLogin() + ">" + whom + ": " + msg);

    }

}
