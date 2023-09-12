package ru.mirea.likhomanov.timeservice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class SocketUtils {
    public static BufferedReader getReader(Socket s) throws IOException {
        return (new BufferedReader(new InputStreamReader(s.getInputStream())));
    }
}
