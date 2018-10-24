package com.example.game.cowsbulls.network;

public interface CommunicatorObserver
{
    // Connection status
    void lostConnectionAttemptingToReconnect();
    void reconnect();
    void opponentQuit();
    void disconnect(String error);
    
    // Host/Client scenes
    void beginConnect();
    void formallyConnected(CommunicatorInitialConnection data);
    void failedToConnect();
    void timeout();
    
    // Game Setup scene
    void opponentPickedPlaySetup(int guessWordLength, String turnToGo);
    
    // Game Pick Word scene
    void opponentPickedPlaySession();
    
    // Game Play scene
    void opponentGuess(String guess);
    void incorrectGuessResponse(String response);
    void correctGuessResponse();
}
