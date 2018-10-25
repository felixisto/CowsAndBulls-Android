package com.example.game.cowsbulls.scenes.join;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.example.game.cowsbulls.R;
import com.example.game.cowsbulls.network.Communicator;

public class JoinActivity extends AppCompatActivity
{
    private JoinPresenter presenter;
    
    private Communicator communicator;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_join_screen);
        
        JoinFragment fragment = (JoinFragment)getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        
        if (fragment == null)
        {
            fragment = JoinFragment.newInstance();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.contentFrame, fragment);
            transaction.commit();
        }
        
        presenter = new JoinPresenter(fragment);
        
        getSupportActionBar().setTitle("Join Game");
    }
    
    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        
        this.presenter.leave();
    }
}
