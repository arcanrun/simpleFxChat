package client;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import server.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;


public class Controller implements Initializable {

    private final int DEFAULT_PORT = Server.PORT;
    private final String DEAFULT_IP = "localhost";
    private ObservableList<String> activeClientsList;
    private String SEND_TO;

    private DataInputStream in;
    private DataOutputStream out;
    private Socket socket;
    private boolean isAuthorized;
    private Alert alert;

    @FXML
    private TextField inputField;

    @FXML
    private Button sendBtn;

    @FXML
    private TextArea chatArea;

    @FXML
    private TextField inputLogin;

    @FXML
    private TextField loginPassword;

    @FXML
    private Button loginBtn;

    @FXML
    private HBox bottomPanel;

    @FXML
    private VBox loginPanel;

    @FXML
    private HBox workingArea;

    @FXML
    private ListView<String> activeClientsListView;

    @FXML
    private HBox sayToLabel;

    @FXML
    private Button closeSayToBtn;

    public void setAuthorized(boolean isAuthorized) {
        this.isAuthorized = isAuthorized;
        if (isAuthorized) {
            loginPanel.setVisible(false);
            loginPanel.setManaged(false);
            workingArea.setVisible(true);
            workingArea.setManaged(true);

        } else {
            loginPanel.setVisible(true);
            loginPanel.setManaged(true);
            workingArea.setVisible(false);
            workingArea.setManaged(false);
        }
    }

    public void closePrivateMessage() {
        SEND_TO = "/toall ";
        activeClientsListView.getSelectionModel().clearSelection();
        sayToLabel.setManaged(false);
        sayToLabel.setVisible(false);
//        activeClientsListView.getItems().removeAll(activeClientsListView.getSelectionModel().getSelectedItems());
    }


    public void sendMsg() {
        String msg = inputField.getText().trim();
        System.out.println(SEND_TO);
        System.out.println(msg);
        if (msg.isEmpty()) return;
        if (SEND_TO.startsWith("/w ")) {
            String[] tokens = SEND_TO.split(" ");

            try {
                out.writeUTF("/w±" + tokens[1] + "±" + msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
            inputField.clear();
        } else if (SEND_TO.startsWith("/toall ")) {
            try {
                out.writeUTF(msg);
                inputField.clear();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    public void connect() {
        try {
            socket = new Socket(DEAFULT_IP, DEFAULT_PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            Thread t = new Thread(() -> {
                try {
                    while (true) {
                        String msg = in.readUTF();
                        if (msg.equals("/authorizedsuccess")) {
                            setAuthorized(true);
                            break;
                        } else {
//                                chatArea.appendText(msg + "\n");
                            Platform.runLater(() -> {
                                alert.setTitle("Information");
                                alert.setHeaderText(null);
                                alert.setContentText(msg);
                                alert.setX(500);
                                alert.setY(500);
                                alert.showAndWait();

                            });

                        }
                    }
                    while (true) {
                        String msg = in.readUTF();
                        if (msg.startsWith("/activeclients")) {
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    String[] activeClients = msg.split("±")[1].split(" ");
                                    activeClientsList.setAll(activeClients);
                                }
                            });

                        } else {
                            chatArea.appendText(msg.trim() + "\n");

                        }
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            });
            t.setDaemon(true);
            t.start();
        } catch (
                IOException e) {
            e.printStackTrace();
        }

    }

    public void login(ActionEvent actionEvent) {
        if (socket == null || socket.isClosed()) {
            connect();
        }
        try {
            out.writeUTF("/auth±" + inputLogin.getText() + "±" + loginPassword.getText());
            loginPassword.clear();
            inputLogin.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        SEND_TO = "/toall ";
        alert = new Alert(Alert.AlertType.INFORMATION);
        activeClientsListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        activeClientsList = FXCollections.observableArrayList();
        activeClientsListView.setItems(activeClientsList);

        activeClientsListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if(newValue == null){
                    return;
                }
                SEND_TO = "/w " + newValue;
                closeSayToBtn.setText(">>> " + newValue);
                sayToLabel.setManaged(true);
                sayToLabel.setVisible(true);
            }
        });
    }
}
