package com.example.game.cowsbulls.scenes.join;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.os.Bundle;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;

import com.example.game.cowsbulls.R;
import com.example.game.cowsbulls.scenes.gamesetup.GameSetupActivity;
import com.example.game.cowsbulls.network.Communicator;
import com.example.game.cowsbulls.shared.SharedResources;

public class JoinFragment extends Fragment implements JoinContract.View
{
    private JoinContract.Presenter presenter;
    
    public JoinFragment()
    {
        
    }
    
    public static JoinFragment newInstance()
    {
        return new JoinFragment();
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_join_screen, container, false);
        
        // Setup UI
        final Button connect = root.findViewById(R.id.buttonConnect);
        
        connect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
    
                if (getView() == null) {return;}
                
                final EditText fieldHost = getView().findViewById(R.id.fieldHostAddress);
                
                presenter.tryConnectingToHost(fieldHost.getText().toString());
                
                connect.setEnabled(false);
            }
        });
        
        return root;
    }
    
    @Override
    public void onResume()
    {
        super.onResume();
        presenter.start();
    }
    
    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
    }
    
    @Override
    public void onDetach()
    {
        super.onDetach();
    }
    
    // - View interface -
    
    @Override
    public void setPresenter(JoinContract.Presenter presenter)
    {
        this.presenter = presenter;
    }
    
    @Override
    public void goBack()
    {
        Log.v("JoinFragment", "Go back");
    }
    
    @Override
    public void goToGameSetupScreen()
    {
        Log.v("JoinFragment", "Go to Game Setup Screen");
        Intent intent = new Intent(getContext(), GameSetupActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
    
    @Override
    public void showDisconnected()
    {

    }
    
    @Override
    public void showOpponentQuit()
    {

    }
    
    @Override
    public void beginConnect()
    {
        
    }
    
    @Override
    public void connectionFailure(String error)
    {
        if (getView() == null) {return;}
        
        final Button connect = getView().findViewById(R.id.buttonConnect);
        connect.setEnabled(true);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("Could not find host server.");
        builder.setCancelable(true);
        
        builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        
        builder.create().show();
    }
    
    @Override
    public void timeout()
    {
        if (getView() == null) {return;}
        
        final Button connect = getView().findViewById(R.id.buttonConnect);
        connect.setEnabled(true);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("Connection timeout. Could not connect with server.");
        builder.setCancelable(true);
        
        builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        
        builder.create().show();
    }
}
