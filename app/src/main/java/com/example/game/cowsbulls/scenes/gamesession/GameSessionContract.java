package com.example.game.cowsbulls.scenes.gamesession;

import com.example.game.cowsbulls.BasePresenter;
import com.example.game.cowsbulls.BaseView;
import com.example.game.cowsbulls.game.GameSession;

public interface GameSessionContract
{
    interface Presenter extends BasePresenter
    {
        void goToGameplayScreen(String guessWord);
        void leave();
        
        void prepareForNewGame();
    }
    
    interface View extends BaseView<GameSessionContract.Presenter>
    {
        void goToGameplayScreen();
        void invalidGuessWord(String error);
        void setOpponentStatus(String status);
        void updateEnterXCharacterWord(int length);
        void updateConnectionData(String playerName, String playerAddress);
        
        void lostConnectionAttemptingToReconnect();
        void reconnected();
        
        void leave();
        void opponentQuit();
        void disconnected();
    }
}
