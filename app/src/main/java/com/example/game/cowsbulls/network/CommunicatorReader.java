package com.example.game.cowsbulls.network;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

public class CommunicatorReader
{
    private boolean active;
    private CommunicatorReaderDelegate delegate;
    private BufferedReader reader;
    
    public CommunicatorReader(Socket socket, CommunicatorReaderDelegate delegate) throws IOException
    {
        this.active = false;
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        this.delegate = delegate;
    }
    
    public boolean isActive()
    {
        return active;
    }
    
    public BufferedReader getReader()
    {
        return reader;
    }
    
    public void begin()
    {
        if (!active)
        {
            active = true;
            
            onBegin();
        }
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
            this.reader.close();
        }
        catch (Exception e)
        {
            
        }
    }
    
    protected void onBegin()
    {
        // Start the reading stream loop in the background
        final CommunicatorReader reader = this;
        
        new Thread(new CommunicatorReaderLoop(reader)).start();
    }
    
    protected void onReceivedGreetings(String parameter)
    {
        delegate.greetingsMessageReceived(parameter);
    }
    
    protected void onPingReceived()
    {
        delegate.ping();
    }
    
    protected void onMessageReceived(String command, String parameter)
    {
        delegate.messageReceived(command, parameter);
    }
}

class CommunicatorReaderLoop implements Runnable
{
    CommunicatorMessage data;
    
    CommunicatorReader delegate;
    
    CommunicatorReaderLoop(CommunicatorReader delegate)
    {
        this.data = CommunicatorMessage.createReadMessage();
        this.delegate = delegate;
    }
    
    @Override
    public void run()
    {
        while (true)
        {
            try
            {
                TimeUnit.MILLISECONDS.sleep(10);
            }
            catch (InterruptedException e)
            {
                break;
            }
    
            if (!delegate.isActive())
            {
                break;
            }
            
            // If message is not fully written, read from socket
            if (!data.isFullyWritten())
            {
                // Read output from stream
                try
                {
                    char[] bytes = new char[CommunicatorMessage.MESSAGE_LENGTH];
                    int length = delegate.getReader().read(bytes, 0, CommunicatorMessage.MESSAGE_LENGTH);
        
                    if (length > 0)
                    {
                        StringBuilder buffer = new StringBuilder();
                        
                        for (int i = 0; i < bytes.length; i++)
                        {
                            buffer.append(bytes[i]);
                        }
                        
                        data.append(buffer.toString());
                        Log.v("Test", "RECEIVED: " + buffer.toString() + " length:" + String.valueOf(data.getDataBytesCount()));
                    }
                }
                catch (IOException e)
                {
        
                }
            }
            
            // Message was received
            if (data.isFullyWritten())
            {
                final String command = data.getCommand();
                final String parameter = data.getParameter();
                
                // On the main thread, evaluate the message received
                Handler mainLoop = new Handler(Looper.getMainLooper());
                
                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run()
                    {
                        // Upon receiving greetings, start the loop ping functionality
                        if (command.equals(CommunicatorCommands.GREETINGS))
                        {
                            delegate.onReceivedGreetings(parameter);
                            
                            return;
                        }
                        
                        // Ping received
                        if (command.equals(CommunicatorCommands.PING))
                        {
                            delegate.onPingReceived();
                            
                            return;
                        }
                        
                        // Message received
                        delegate.onMessageReceived(command, parameter);
                    }
                };
                
                // Reset
                data.clearFirstFilledMessage();
                
                // Repeat
                mainLoop.post(myRunnable);
            }
        }
    }
}
