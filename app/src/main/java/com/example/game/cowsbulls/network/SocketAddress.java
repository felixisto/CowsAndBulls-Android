package com.example.game.cowsbulls.network;

import java.net.Socket;

public class SocketAddress
{
    static String getFrom(Socket socket)
    {
        if (socket == null)
        {
            return "Unknown";
        }
        
        String address = socket.getRemoteSocketAddress().toString();
        
        if (address.length() == 0)
        {
            return "Unknown";
        }
        
        int start = 0;
        int end = address.length();
        
        for (int e = 0; e < address.length(); e++)
        {
            char c = address.charAt(e);
            
            if (Character.isDigit(c) || Character.isLetter(c))
            {
                start = e;
                break;
            }
        }
        
        for (int e = start; e < address.length(); e++)
        {
            char c = address.charAt(e);
            
            if (c == ':')
            {
                end = e;
                break;
            }
        }
        
        return address.substring(start, end);
    }
}
