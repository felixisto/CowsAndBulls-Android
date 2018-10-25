package com.example.game.cowsbulls.scenes.join;

import com.example.game.cowsbulls.BaseView;
import com.example.game.cowsbulls.BasePresenter;

public interface JoinContract
{
    interface Presenter extends BasePresenter
    {
        void tryConnectingToHost(String host);
        void leave();
    }
    
    interface View extends BaseView<Presenter>
    {
        void goToGameSetupScreen();
        
        void showDisconnected();
        void showOpponentQuit();
        
        void beginConnect();
        void connectionFailure(String error);
        void timeout();
    }
}
