package com.example.game.cowsbulls.scenes.gamesetup;

import com.example.game.cowsbulls.BaseView;
import com.example.game.cowsbulls.BasePresenter;
import com.example.game.cowsbulls.network.Communicator;

public interface GameSetupContract
{
    interface Presenter extends BasePresenter
    {
        void goToGameSessionScreen();
        void leave();
        
        void didSelectGuessWordCharacterCount(int number);
        void didSelectTurnToGo(String turnToGo);
        
        void pickCurrentPlaySetupAndSendToOpponent();
        void playSetupMismatchesOpponentPlayerSetup();
        void playSetupMatchesOpponentPlayerSetup();
    }
    
    interface View extends BaseView<Presenter>
    {
        void goToGameSessionScreen(int guessWordLength, String turnToGo);
        void updateOpponentPlaySetup(int guessWordLength, String turnToGo);
        void playSetupMatch();
        void playSetupMismatch();
        void updateConnectionData(String playerName, String playerAddress);
        
        void lostConnectionAttemptingToReconnect();
        void reconnected();
        
        void leave();
        void opponentQuit();
        void disconnected();
    }
}
