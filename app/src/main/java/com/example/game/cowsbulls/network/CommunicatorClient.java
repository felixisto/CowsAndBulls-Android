package com.example.game.cowsbulls.network;

import android.os.Looper;
import android.util.Log;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import android.os.Handler;

import com.example.game.cowsbulls.utilities.UserName;

public class CommunicatorClient implements Communicator, CommunicatorReaderDelegate, CommunicatorWriterDelegate
{
    static final int PORT = 1337;
    static final int CONNECTION_TIMEOUT_IN_SECONDS = 10;
    static final int BEGIN_CONNECTION_TIMEOUT_IN_SECONDS = 3;
    static final double PING_DELAY_MINIMUM = 0.4;
    static final double PING_TIMEOUT = 10.0;
    
    private HashMap<String, CommunicatorObserverValue> observers;
    private CommunicatorReader reader;
    private CommunicatorWriter writer;
    private Socket socket;
    private String hostAddress;
    
    private boolean isConnectedToServer;
    
    private Date lastPingFromServer;
    private boolean lastPingRetryingToConnect;
    
    public CommunicatorClient()
    {
        this.observers = new HashMap<String, CommunicatorObserverValue>();
        this.isConnectedToServer = false;
        this.lastPingFromServer = null;
        this.lastPingRetryingToConnect = false;
    }
    
    public boolean hasOpenSocket() {return socket != null;}
    
    public boolean isConnectedToServer()
    {
        return isConnectedToServer;
    }
    
    public HashMap<String, CommunicatorObserverValue> getObservers()
    {
        return observers;
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
        
        if (socket != null)
        {
            try {socket.close();} catch (Exception dummy) {}
        }
        
        socket = null;
        reader = null;
        writer = null;
        
        isConnectedToServer = false;
        
        lastPingFromServer = null;
        lastPingRetryingToConnect = false;
    }
    
    public void destroy()
    {
        reset();
        
        observers.clear();
    }
    
    public void start(final String host)
    {
        Log.v("CommunicatorClient", "Attempting to connect to " + host + "...");
        
        socket = new Socket();
        hostAddress = host;
        
        // Attempt to start connection
        final CommunicatorClient communicator = this;
        
        new Thread(new CommunicatorClientConnection(communicator, host)).start();
    }
    
    protected void onBeginConnection(Socket socket, String host)
    {
        Log.v("CommunicatorClient", "Beginning new connection with server on " + (new Date()).toString());
        
        this.socket = socket;
        
        try {
            reader = new CommunicatorReader(socket, this);
            reader.begin();
            
            hostAddress = host;
            
            lastPingFromServer = new Date();
            
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
            // If a formal connection is not established in @CommunicatorHostBeginConnectTimeout seconds, terminate connection
            final CommunicatorClient communicator = this;
            
            new Thread(new CommunicatorClientTimeoutConnection(communicator)).start();
        }
        catch (final Exception e)
        {
            Log.v("CommunicatorClient", "Failed to open read stream, error: " + e.toString());
            
            onTimeout();
            
            return;
        }
    }
    
    protected void onFailureConnection(String host)
    {
        Log.v("CommunicatorClient", "Failed to start, could not find host.");
    
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
                        observerValue.value().failedToConnect();
                    }
                }
            }
        };
    
        mainLoop.post(myRunnable);
    }
    
    protected void onTimeout()
    {
        Log.v("CommunicatorClient", "Failed to connect properly with host.");
        
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
        Log.v("CommunicatorClient", "Received greetings, sending greetings message to server on " + (new Date()).toString());
        
        this.isConnectedToServer = true;
        
        // Start writer
        try
        {
            writer = new CommunicatorWriter(socket, this);
            writer.begin();
        }
        catch (Exception e)
        {
            Log.v("CommunicatorClient", "Failed to open write stream, error: " + e.toString());
            
            onTimeout();
            
            return;
        }
        
        // Send greetings BACK to server
        String userName = UserName.get();
        
        CommunicatorMessage message = CommunicatorMessage.createWriteMessage(CommunicatorCommands.GREETINGS, userName);
        writer.send(message.getData());
        
        // Alert the observers in the main thread
        final CommunicatorInitialConnection data = new CommunicatorInitialConnection(new Date(), hostAddress, parameter);
        
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
    
    protected void onServerQuit()
    {
        Log.v("CommunicatorClient", "Server quit on " + (new Date()).toString());
        
        reset();
        
        // Alert the observers in the main thread
        final CommunicatorClient communicator = this;
        
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
        Log.v("CommunicatorClient", "Server disconnected on " + (new Date()).toString());
    
        reset();
    
        // Alert the observers in the main thread
        final CommunicatorClient communicator = this;
        
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
        Log.v("CommunicatorClient", "Lost connection with server on " + (new Date()).toString() + ", attempting to reconnect...");
        
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
        Log.v("CommunicatorClient", "Reconnected with server on " + (new Date()).toString());
        
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
    public void attachObserver(final CommunicatorObserver observer, final String key)
    {
        // Attach in the main thread
        Handler mainLoop = new Handler(Looper.getMainLooper());
        
        Runnable myRunnable = new Runnable() {
            @Override
            public void run()
            {
                observers.put(key, new CommunicatorObserverValue(observer));
            }
        };
        
        mainLoop.post(myRunnable);
    }
    
    @Override
    public void detachObserver(final String key)
    {
        // Detach in the main thread
        Handler mainLoop = new Handler(Looper.getMainLooper());
        
        Runnable myRunnable = new Runnable() {
            @Override
            public void run()
            {
                observers.remove(key);
            }
        };
        
        mainLoop.post(myRunnable);
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
        Log.v("CommunicatorClient", "Sending quit message to server");
        
        CommunicatorMessage message = CommunicatorMessage.createWriteMessage(CommunicatorCommands.QUIT);
        writer.send(message.getData());
    }
    
    @Override
    public void sendPlaySetupMessage(int guessLength, String turnToGo)
    {
        Log.v("CommunicatorClient", "Sending play setup message to server");
        
        CommunicatorMessage message = CommunicatorMessage.createWriteMessage(CommunicatorCommands.PLAYSETUP, String.valueOf(guessLength), turnToGo);
        writer.send(message.getData());
    }
    
    @Override
    public void sendAlertPickedGuessWordMessage()
    {
        Log.v("CommunicatorClient", "Sending alert picked guess word message to server");
    
        CommunicatorMessage message = CommunicatorMessage.createWriteMessage(CommunicatorCommands.READYTOPLAY);
        writer.send(message.getData());
    }
    
    @Override
    public void sendGuessMessage(String guess)
    {
        Log.v("CommunicatorClient", "Sending guess message to server");
    
        CommunicatorMessage message = CommunicatorMessage.createWriteMessage(CommunicatorCommands.GAMEGUESS, guess);
        writer.send(message.getData());
    }
    
    @Override
    public void sendGuessIncorrectResponseMessage(String response)
    {
        Log.v("CommunicatorClient", "Sending guess response message to server");
    
        CommunicatorMessage message = CommunicatorMessage.createWriteMessage(CommunicatorCommands.GAMEGUESSRESPONSE, response);
        writer.send(message.getData());
    }
    
    @Override
    public void sendGuessCorrectResponseMessage()
    {
        Log.v("CommunicatorClient", "Sending guess correct message to server");
    
        CommunicatorMessage message = CommunicatorMessage.createWriteMessage(CommunicatorCommands.GAMECORRECTGUESS);
        writer.send(message.getData());
    }
    
    // - CommunicatorReaderDelegate interface -
    
    @Override
    public void ping()
    {
        lastPingFromServer = new Date();
        
        if (lastPingRetryingToConnect)
        {
            onReconnect();
        }
    
        lastPingRetryingToConnect = false;
    }
    
    @Override
    public void greetingsMessageReceived(String parameter)
    {
        if (!isConnectedToServer)
        {
            onConnected(parameter);
        }
    }
    
    @Override
    public void messageReceived(String command, final String parameter)
    {
        if (!isConnectedToServer)
        {
            return;
        }
        
        switch (command)
        {
            case CommunicatorCommands.QUIT:
                onServerQuit();
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
        if (!isConnectedToServer)
        {
            return;
        }
        
        // Check if server has been pinging back
        if (lastPingFromServer != null)
        {
            Date currentDate = new Date();
            
            double timeElapsedSinceLastPing = (currentDate.getTime() - lastPingFromServer.getTime()) / 1000.0;
            
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
            Log.v("CommunicatorClient", "Communicator last ping date was not initialized properly");
            
            onDisconnected();
        }
    }
}

class CommunicatorClientConnection implements Runnable
{
    CommunicatorClient delegate;
    Socket socket;
    String host;
    
    CommunicatorClientConnection(CommunicatorClient delegate, String host)
    {
        this.delegate = delegate;
        this.host = host;
    }
    
    @Override
    public void run()
    {
        for (int i = 0; i < CommunicatorClient.CONNECTION_TIMEOUT_IN_SECONDS; i++)
        {
            if (!delegate.hasOpenSocket())
            {
                return;
            }
            
            try {
                socket = new Socket();
                try { socket.setSoTimeout(100); } catch (Exception e) {}
                socket.connect(new InetSocketAddress(host, CommunicatorClient.PORT));
                
                // Success!
                delegate.onBeginConnection(socket, host);
                
                return;
            }
            catch (Exception e)
            {
                
            }
            
            try {TimeUnit.SECONDS.sleep(1);} catch (Exception dummy) {}
        }
        
        // Failure!
        delegate.onFailureConnection(host);
    }
}

class CommunicatorClientTimeoutConnection implements Runnable
{
    CommunicatorClient delegate;
    
    CommunicatorClientTimeoutConnection(CommunicatorClient delegate)
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
        
        if (delegate.hasOpenSocket() && !delegate.isConnectedToServer())
        {
            Log.v("CommunicatorClient", "Connection timeout, did not receive greetings from server!");
            
            delegate.onTimeout();
        }
    }
}

