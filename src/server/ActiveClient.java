package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

public class ActiveClient {
    private DataInputStream in;
    private DataOutputStream out;
    private Socket socket;
    private Server server;
    private AuthService authService;
    private String login;


    public ActiveClient(Socket socket, Server server, AuthService authService) {
        this.authService = authService;
        this.server = server;
        try {
            this.socket = socket;
            out = new DataOutputStream(this.socket.getOutputStream());
            in = new DataInputStream(this.socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        new Thread(() -> {
            try {
                while (true) {
                    String msg = in.readUTF();
                    if (msg.startsWith("/auth")) {
                        String[] tokens = msg.split("±");
                        if (tokens.length < 3) {
                            out.writeUTF("Wrong numbers of arguments!");
                            continue;
                        }
                        login = authService.loginUserByLoginAndPass(tokens[1], tokens[2]);
                        if (login != null) {
                            if (server.isAlreadyLogin(this)) {
                                out.writeUTF("This user is already has been started session!");
                                continue;
                            }
                            sendMsg("/authorizedsuccess");
                            server.subscribe(this);
                            server.broadCastMsg(login + " has been authorized");
                            break;
                        } else {
                            out.writeUTF("User not found!");
                        }
                    }
                }
                while (true) {
                    String msg = in.readUTF().trim();
                    if (msg.equals("/end")) {
                        break;
                    }
                    if(msg.startsWith("/w±")){
                        String[] tokens = msg.split("±", 3);
                        System.out.println(Arrays.asList(tokens));
//                        if(tokens.length < 3){
//                            continue;
//                        }

                        server.privateMessage(tokens[1], tokens[2], this);
                        continue;
                    }
                    server.broadCastMsg(login + ": " + msg);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                server.unsubscribe(this);
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                server.unsubscribe(this);
            }
        }
        ).

                start();

    }

    public String getLogin() {
        return login;
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
