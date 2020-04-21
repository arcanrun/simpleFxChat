package server;

import sun.misc.Cleaner;

import java.sql.*;

public class AuthService {
    private Connection connection;
    private PreparedStatement loginAndPassStmnt;

    public void connect() {
   
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:chatDb.db");
            this.loginAndPassStmnt = connection.prepareStatement("SELECT login, password from users WHERE login = ? AND password = ?");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }


    public String loginUserByLoginAndPass(String login, String password) {
        try {
            loginAndPassStmnt.setString(1, login);
            loginAndPassStmnt.setString(2, password);
            ResultSet rs = loginAndPassStmnt.executeQuery();
            while (rs.next()) {
                return  rs.getString(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;

    }




    public void disconnect() {
        try {
            connection.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
