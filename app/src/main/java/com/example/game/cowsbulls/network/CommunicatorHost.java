package com.example.game.cowsbulls.network;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.game.cowsbulls.utilities.UserName;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class CommunicatorHost implements Communicator, CommunicatorReaderDelegate, CommunicatorWriterDelegate
{
    static final int PORT = 1337;
    static final int CONNECTION_TIMEOUT_IN_SECONDS = 10;
    static final int BEGIN_CONNECTION_TIMEOUT_IN_SECONDS = 3;
    static final double PING_DELAY_MINIMUM = 0.4;
    static final double PING_TIMEOUT = 10.0;
    
    private HashMap<String, CommunicatorObserverValue> observers;
    private CommunicatorReader reader;
    private CommunicatorWriter writer;
    private ServerSocket server;
    private Socket client;
    
    private boolean isConnectedToClient;
    
    private Date lastPingFromClient;
    private boolean lastPingRetryingToConnect;
    
    public CommunicatorHost()
    {
        this.observers = new HashMap<String, CommunicatorObserverValue>();
        this.isConnectedToClient = false;
        this.lastPingFromClient = null;
        this.lastPingRetryingToConnect = false;
    }
    
    public boolean hasServerRunning() {return server != null;}
    
    public boolean isConnectedToClient()
    {
        return isConnectedToClient;
    }
    
    public void reset()
    {
        if (reader != null)
        {
            reader.stop();
        }
        
        if (writer != null)
        {
            writer.stop();
        }
        
        if (server != null) {try {server.close();} catch (Exception e) {}}
        if (client != null) {try {client.close();} catch (Exception e) {}}
        
        server = null;
        client = null;
        reader = null;
        writer = null;
        
        isConnectedToClient = false;
        
        lastPingFromClient = null;
        lastPingRetryingToConnect = false;
    }
    
    public void destroy()
    {
        reset();
        
        observers.clear();
    }
    
    public void start()
    {
        Log.v("CommunicatorHost", "Starting server...");
        
        try {
            server = new ServerSocket(PORT);
    
            try { server.setSoTimeout(100); } catch (Exception e) {}
            
            Log.v("CommunicatorHost", "Server started.");
            
            new Thread(new CommunicatorHostConnection(this, server)).start();
        }
        catch (Exception e)
        {
            Log.v("CommunicatorHost", "Server failed to start!");
        }
    }
    
    protected void onBeginConnection(Socket client)
    {
        String clientAddress = SocketAddress.getFrom(client);
        Log.v("CommunicatorHost", "Beginning new connection with a client (" +  clientAddress + ") " + (new Date()).toString() + ". Sending greetings to client, waiting to be greeted back...");
        
        this.client = client;
        
        // Start writer
        try
        {
            reader = new CommunicatorReader(client, this);
            reader.begin();
            
            writer = new CommunicatorWriter(client, this);
            writer.begin();
            
            lastPingFromClient = new Date();
            
            // Success!
            // Alert the observers in the main thread
            Handler mainLoop = new Handler(Looper.getMainLooper());
            
            Runnable myRunnable = new Runnable() {
                @Override
                public void run()
                {
                    for (CommunicatorObserverValue observerValue : observers.values())
                    {
                        if (observerValue.value() != null)
                        {
                            observerValue.value().beginConnect();
                        }
                    }
                }
            };
            
            mainLoop.post(myRunnable);
            
            // Start the timeout check in a background thread
            // If a formal connection is not established in @BEGIN_CONNECTION_TIMEOUT_IN_SECONDS seconds, terminate connection
            new Thread(new CommunicatorHostTimeoutConnection(this)).start();
        }
        catch (Exception e)
        {
            Log.v("CommunicatorHost", "Failed to open read or write stream, error: " + e.toString());
        
            onTimeout();
        
            return;
        }
        
        // Send greetings to client
        String userName = UserName.get();
        
        CommunicatorMessage message = CommunicatorMessage.createWriteMessage(CommunicatorCommands.GREETINGS, userName);
        writer.send(message.getData());
    }
    
    protected void onTimeout()
    {
        Log.v("CommunicatorHost", "Failed to connect properly with client.");
        
        reset();
        
        // Alert the observers in the main thread
        Handler mainLoop = new Handler(Looper.getMainLooper());
        
        Runnable myRunnable = new Runnable() {
            @Override
            public void run()
            {
                for (CommunicatorObserverValue observerValue : observers.values())
                {
                    if (observerValue.value() != null)
                    {
                        observerValue.value().timeout();
                    }
                }
            }
        };
        
        mainLoop.post(myRunnable);
    }
    
    protected void onConnected(final String parameter)
    {
        Log.v("CommunicatorHost", "Received greetings from client on " + (new Date()).toString());
        
        this.isConnectedToClient = true;
        
        // Alert the observers in the main thread
        String clientAddress = SocketAddress.getFrom(client);
        final CommunicatorInitialConnection data = new CommunicatorInitialConnection(new Date(), clientAddress, parameter);
        
        Handler mainLoop = new Handler(Looper.getMainLooper());
        
        Runnable myRunnable = new Runnable() {
            @Override
            public void run()
            {
                for (CommunicatorObserverValue observerValue : observers.values())
                {
                    if (observerValue.value() != null)
                    {
                        observerValue.value().formallyConnected(data);
                    }
                }
            }
        };
        
        mainLoop.post(myRunnable);
    }
    
    protected void onClientQuit()
    {
        Log.v("CommunicatorHost", "Client quit on " + (new Date()).toString());
        
        reset();
        
        // Alert the observers in the main thread
        final CommunicatorHost communicator = this;
        
        Handler mainLoop = new Handler(Looper.getMainLooper());
        
        Runnable myRunnable = new Runnable() {
            @Override
            public void run()
            {
                for (CommunicatorObserverValue observerValue : observers.values())
                {
                    if (observerValue.value() != null)
                    {
                        observerValue.value().opponentQuit();
                    }
                }
                
                communicator.destroy();
            }
        };
        
        mainLoop.post(myRunnable);
    }
    
    protected void onDisconnected()
    {
        Log.v("CommunicatorHost", "Client disconnected on " + (new Date()).toString());
        
        reset();
        
        // Alert the observers in the main thread
        final CommunicatorHost communicator = this;
        
        Handler mainLoop = new Handler(Looper.getMainLooper());
        
        Runnable myRunnable = new Runnable() {
            @Override
            public void run()
            {
                for (CommunicatorObserverValue observerValue : observers.values())
                {
                    if (observerValue.value() != null)
                    {
                        observerValue.value().disconnect("Disconnected");
                    }
                }
                
                communicator.destroy();
            }
        };
        
        mainLoop.post(myRunnable);
    }
    
    protected void onLostConnectionAttemptingToReconnect()
    {
        Log.v("CommunicatorHost", "Lost connection with client on " + (new Date()).toString() + ", attempting to reconnect...");
        
        lastPingRetryingToConnect = true;
        
        // Alert the observers in the main thread
        Handler mainLoop = new Handler(Looper.getMainLooper());
        
        Runnable myRunnable = new Runnable() {
            @Override
            public void run()
            {
                for (CommunicatorObserverValue observerValue : observers.values())
                {
                    if (observerValue.value() != null)
                    {
                        observerValue.value().lostConnectionAttemptingToReconnect();
                    }
                }
            }
        };
        
        mainLoop.post(myRunnable);
    }
    
    protected void onReconnect()
    {
        Log.v("CommunicatorHost", "Reconnected with client on " + (new Date()).toString());
        
        lastPingRetryingToConnect = false;
        
        // Alert the observers in the main thread
        Handler mainLoop = new Handler(Looper.getMainLooper());
        
        Runnable myRunnable = new Runnable() {
            @Override
            public void run()
            {
                for (CommunicatorObserverValue observerValue : observers.values())
                {
                    if (observerValue.value() != null)
                    {
                        observerValue.value().reconnect();
                    }
                }
            }
        };
        
        mainLoop.post(myRunnable);
    }
    
    // - Communicator interface -
    
    @Override
    public void attachObserver(CommunicatorObserver observer, final String key)
    {
        observers.put(key, new CommunicatorObserverValue(observer));
    }
    
    @Override
    public void detachObserver(String key)
    {
        observers.remove(key);
    }
    
    @Override
    public void stop()
    {
        sendQuitMessage();
        
        reset();
    }
    
    @Override
    public void sendQuitMessage()
    {
        Log.v("CommunicatorHost", "Sending quit message to client");
        
        CommunicatorMessage message = CommunicatorMessage.createWriteMessage(CommunicatorCommands.QUIT);
        writer.send(message.getData());
    }
    
    @Override
    public void sendPlaySetupMessage(int guessLength, String turnToGo)
    {
        Log.v("CommunicatorHost", "Sending play setup message to client");
        
        CommunicatorMessage message = CommunicatorMessage.createWriteMessage(CommunicatorCommands.PLAYSETUP, String.valueOf(guessLength), turnToGo);
        writer.send(message.getData());
    }
    
    @Override
    public void sendAlertPickedGuessWordMessage()
    {
        Log.v("CommunicatorHost", "Sending alert picked guess word message to client");
        
        CommunicatorMessage message = CommunicatorMessage.createWriteMessage(CommunicatorCommands.READYTOPLAY);
        writer.send(message.getData());
    }
    
    @Override
    public void sendGuessMessage(String guess)
    {
        Log.v("CommunicatorHost", "Sending guess message to client");
        
        CommunicatorMessage message = CommunicatorMessage.createWriteMessage(CommunicatorCommands.GAMEGUESS, guess);
        writer.send(message.getData());
    }
    
    @Override
    public void sendGuessIncorrectResponseMessage(String response)
    {
        Log.v("CommunicatorHost", "Sending guess response message to client");
        
        CommunicatorMessage message = CommunicatorMessage.createWriteMessage(CommunicatorCommands.GAMEGUESSRESPONSE, response);
        writer.send(message.getData());
    }
    
    @Override
    public void sendGuessCorrectResponseMessage()
    {
        Log.v("CommunicatorHost", "Sending guess correct message to client");
        
        CommunicatorMessage message = CommunicatorMessage.createWriteMessage(CommunicatorCommands.GAMECORRECTGUESS);
        writer.send(message.getData());
    }
    
    // - CommunicatorReaderDelegate interface -
    
    @Override
    public void ping()
    {
        lastPingFromClient = new Date();
        
        if (lastPingRetryingToConnect)
        {
            onReconnect();
        }
        
        lastPingRetryingToConnect = false;
    }
    
    @Override
    public void greetingsMessageReceived(String parameter)
    {
        if (!isConnectedToClient)
        {
            onConnected(parameter);
        }
    }
    
    @Override
    public void messageReceived(String command, final String parameter)
    {
        if (!isConnectedToClient)
        {
            return;
        }
        
        switch (command)
        {
            case CommunicatorCommands.QUIT:
                onClientQuit();
                break;
            case CommunicatorCommands.PLAYSETUP: {
                String[] parameters = parameter.split(" ");
                
                if (parameters.length != 2)
                {
                    return;
                }
                
                final String param1 = parameters[0];
                final String param2 = parameters[1];
                final int wordLength = Integer.parseInt(param1);
                
                // Alert the observers in the main thread
                Handler mainLoop = new Handler(Looper.getMainLooper());
                
                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run()
                    {
                        for (CommunicatorObserverValue observerValue : observers.values())
                        {
                            if (observerValue.value() != null)
                            {
                                observerValue.value().opponentPickedPlaySetup(wordLength, param2);
                            }
                        }
                    }
                };
                
                mainLoop.post(myRunnable);
                break; }
            case CommunicatorCommands.READYTOPLAY: {
                // Alert the observers in the main thread
                Handler mainLoop = new Handler(Looper.getMainLooper());
                
                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run()
                    {
                        for (CommunicatorObserverValue observerValue : observers.values())
                        {
                            if (observerValue.value() != null)
                            {
                                observerValue.value().opponentPickedPlaySession();
                            }
                        }
                    }
                };
                
                mainLoop.post(myRunnable);
                break; }
            case CommunicatorCommands.GAMEGUESS: {
                // Alert the observers in the main thread
                Handler mainLoop = new Handler(Looper.getMainLooper());
                
                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run()
                    {
                        for (CommunicatorObserverValue observerValue : observers.values())
                        {
                            if (observerValue.value() != null)
                            {
                                observerValue.value().opponentGuess(parameter);
                            }
                        }
                    }
                };
                
                mainLoop.post(myRunnable);
                break; }
            case CommunicatorCommands.GAMEGUESSRESPONSE: {
                // Alert the observers in the main thread
                Handler mainLoop = new Handler(Looper.getMainLooper());
                
                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run()
                    {
                        for (CommunicatorObserverValue observerValue : observers.values())
                        {
                            if (observerValue.value() != null)
                            {
                                observerValue.value().incorrectGuessResponse(parameter);
                            }
                        }
                    }
                };
                
                mainLoop.post(myRunnable);
                break; }
            case CommunicatorCommands.GAMECORRECTGUESS: {
                // Alert the observers in the main thread
                Handler mainLoop = new Handler(Looper.getMainLooper());
                
                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run()
                    {
                        for (CommunicatorObserverValue observerValue : observers.values())
                        {
                            if (observerValue.value() != null)
                            {
                                observerValue.value().correctGuessResponse();
                            }
                        }
                    }
                };
                
                mainLoop.post(myRunnable);
                break; }
        }
    }
    
    // - CommunicatorWriterDelegate interface -
    
    @Override
    public void pingRefresh()
    {
        if (!isConnectedToClient)
        {
            return;
        }
        
        // Check if server has been pinging back
        if (lastPingFromClient != null)
        {
            Date currentDate = new Date();
            
            double timeElapsedSinceLastPing = (currentDate.getTime() - lastPingFromClient.getTime()) / 1000.0;
            
            boolean noPingReceivedShort = timeElapsedSinceLastPing >= PING_DELAY_MINIMUM;
            
            // Lost connection
            if (noPingReceivedShort)
            {
                boolean pingTimeout = timeElapsedSinceLastPing >= PING_TIMEOUT;
            
                // Timeout, end the connection
                if (pingTimeout)
                {
                    onDisconnected();
                    return;
                }
                else
                {
                    // Try to reconnect
                    if (!lastPingRetryingToConnect)
                    {
                        onLostConnectionAttemptingToReconnect();
                    }
                }
            }
        }
        else
        {
            Log.v("CommunicatorHost", "Communicator last ping date was not initialized properly");
            
            onDisconnected();
        }
    }
}

class CommunicatorHostConnection implements Runnable
{
    CommunicatorHost delegate;
    ServerSocket server;
    
    CommunicatorHostConnection(CommunicatorHost delegate, ServerSocket server)
    {
        this.delegate = delegate;
        this.server = server;
    }
    
    @Override
    public void run()
    {
        while (true)
        {
            if (!delegate.hasServerRunning())
            {
                return;
            }
            
            if (delegate.isConnectedToClient())
            {
                return;
            }
            
            try {
                Socket socket = server.accept();
                
                // Success!
                delegate.onBeginConnection(socket);
            }
            catch (Exception e)
            {
                
            }
            
            try {TimeUnit.SECONDS.sleep(1);} catch (Exception dummy) {}
        }
    }
}

class CommunicatorHostTimeoutConnection implements Runnable
{
    CommunicatorHost delegate;
    
    CommunicatorHostTimeoutConnection(CommunicatorHost delegate)
    {
        this.delegate = delegate;
    }
    
    @Override
    public void run()
    {
        try
        {
            TimeUnit.SECONDS.sleep(CommunicatorClient.BEGIN_CONNECTION_TIMEOUT_IN_SECONDS);
        }
        catch (Exception e)
        {
            return;
        }
        
        if (delegate.hasServerRunning() && !delegate.isConnectedToClient())
        {
            Log.v("CommunicatorHost", "Connection timeout, did not receive greetings from client!");
            
            delegate.onTimeout();
        }
    }
}
