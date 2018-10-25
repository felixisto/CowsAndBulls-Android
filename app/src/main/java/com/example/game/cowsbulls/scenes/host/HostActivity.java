package com.example.game.cowsbulls.scenes.host;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.example.game.cowsbulls.R;
import com.example.game.cowsbulls.network.Communicator;

public class HostActivity extends AppCompatActivity 
{
    private HostPresenter presenter;
    
    private Communicator communicator;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_host_screen);
        
        HostFragment fragment = (HostFragment)getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        
        if (fragment == null)
        {
            fragment = HostFragment.newInstance();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.contentFrame, fragment);
            transaction.commit();
        }
        
        presenter = new HostPresenter(fragment);
    
        getSupportActionBar().setTitle("Host Game");
    }
    
    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        
        this.presenter.leave();
    }
}
