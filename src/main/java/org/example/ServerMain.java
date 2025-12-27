package org.example;

import org.example.db.DBConnection;
import org.example.server.Server;

public class ServerMain {
    public static void main(String[] args){
        try {
            DBConnection.init();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        Server server = new Server(50, 50, 300);
        server.start();
    }
}
