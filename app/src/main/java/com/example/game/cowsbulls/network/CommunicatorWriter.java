package com.example.game.cowsbulls.network;

import android.os.Handler;
import android.os.Looper;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

public class CommunicatorWriter
{
    boolean active;
    CommunicatorWriterDelegate delegate;
    PrintWriter writer;
    
    public CommunicatorWriter(Socket socket, CommunicatorWriterDelegate delegate) throws IOException
    {
        this.active = false;
        this.delegate = delegate;
        this.writer = new PrintWriter(socket.getOutputStream(), false);
    }
    
    public void begin()
    {
        if (active)
        {
            return;
        }
        
        active = true;
        
        // Start the ping loop in the background
        final CommunicatorWriter writer = this;
        
        new Thread(new CommunicatorWriterPingLoop(writer)).start();
    }
    
    public void stop()
    {
        if (!active)
        {
            return;
        }
        
        active = false;
        
        try
        {
            this.writer.close();
        }
        catch (Exception e)
        {
            
        }
    }
    
    public void send(String data)
    {
        if (!active)
        {
            return;
        }
        
        new Thread(new CommunicatorWriterSendMessage(writer, data)).start();
    }
    
    public void sendPing()
    {
        if (!active)
        {
            return;
        }
        
        CommunicatorMessage pingMessage = CommunicatorMessage.createWriteMessage(CommunicatorCommands.PING);
        send(pingMessage.getData());
    }
    
    public void refreshPing()
    {
        if (!active)
        {
            return;
        }
        
        // On the main thread
        Handler mainLoop = new Handler(Looper.getMainLooper());
        
        Runnable myRunnable = new Runnable() {
            @Override
            public void run()
            {
                delegate.pingRefresh();
            }
        };
        
        mainLoop.post(myRunnable);
    }
}

class CommunicatorWriterSendMessage implements Runnable
{
    PrintWriter writer;
    String data;
    
    CommunicatorWriterSendMessage(PrintWriter writer, String data)
    {
        this.writer = writer;
        this.data = data;
    }
    
    @Override
    public void run()
    {
        writer.print(data);
        writer.flush();
    }
}

class CommunicatorWriterPingLoop implements Runnable
{
    static final int INTERVAL_IN_MS = 50;
    
    CommunicatorWriter delegate;
    
    CommunicatorWriterPingLoop(CommunicatorWriter delegate)
    {
        this.delegate = delegate;
    }
    
    @Override
    public void run()
    {
        while (true) 
        {
            try 
            {
                TimeUnit.MILLISECONDS.sleep(INTERVAL_IN_MS);
            } 
            catch (InterruptedException e) 
            {
                break;
            }
            
            if (!delegate.active) 
            {
                break;
            }
            
            // Ping other end
            delegate.sendPing();
            
            // Refresh ping
            delegate.refreshPing();
        }
    }
}