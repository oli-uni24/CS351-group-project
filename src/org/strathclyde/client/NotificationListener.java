package org.strathclyde.client;

import java.io.BufferedReader;
import java.io.IOException;

public class NotificationListener implements Runnable {
    private final BufferedReader serverIn;

    public NotificationListener(BufferedReader serverIn){
        this.serverIn = serverIn;
    }

    @Override
    public void run() {
        try{
            String serverLine;
            while((serverLine = serverIn.readLine()) != null){
                System.out.println(serverLine);
            }
        } catch (IOException e){
            //connection closed, thread ends
        }
    }
}
