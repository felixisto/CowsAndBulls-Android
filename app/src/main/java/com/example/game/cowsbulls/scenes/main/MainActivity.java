package com.example.game.cowsbulls.scenes.main;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentActivity;

import com.example.game.cowsbulls.R;

public class MainActivity extends FragmentActivity 
{
    private MainPresenter presenter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_main_screen);
        
        MainFragment fragment = (MainFragment)getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        
        if (fragment == null)
        {
            fragment = MainFragment.newInstance();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.contentFrame, fragment);
            transaction.commit();
        }
        
        presenter = new MainPresenter(fragment);
    }
    
    public void setPresenter(MainPresenter presenter)
    {
        this.presenter = presenter;
    }
}
