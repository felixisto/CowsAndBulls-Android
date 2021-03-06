package com.example.game.cowsbulls.scenes.host;

import com.example.game.cowsbulls.BaseView;
import com.example.game.cowsbulls.BasePresenter;

public interface HostContract
{
    interface Presenter extends BasePresenter
    {
        void goToGameSetupScreen();
        void leave();
    }
    
    interface View extends BaseView<Presenter>
    {
        void goToGameSetupScreen();
        
        void showDisconnected();
        void showOpponentQuit();
        
        void clientBeginConnect();
        void clientTimeout();
        
        void failedToStart(String error);
    }
}
