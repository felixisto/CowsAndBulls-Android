package com.example.game.cowsbulls.network;

public interface Communicator
{
    void attachObserver(CommunicatorObserver observer, final String key);
    void detachObserver(final String key);
    
    void stop();
    
    void sendQuitMessage();
    void sendPlaySetupMessage(int guessLength, String turnToGo);
    void sendAlertPickedGuessWordMessage();
    void sendGuessMessage(String guess);
    void sendGuessIncorrectResponseMessage(String response);
    void sendGuessCorrectResponseMessage();
    void sendGameChatMessage(String chat);
    void sendGameNextMessage();
}
