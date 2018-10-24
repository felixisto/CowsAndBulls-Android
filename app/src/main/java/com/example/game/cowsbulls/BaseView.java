package com.example.game.cowsbulls;

public interface BaseView<T>
{
    void setPresenter(T presenter);
    
    void goBack();
}
