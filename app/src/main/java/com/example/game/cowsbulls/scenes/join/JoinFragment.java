package com.example.game.cowsbulls.scenes.join;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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

public class JoinFragment extends Fragment implements JoinContract.View
{
    static public String CLIENT_LAST_HOST_ADDRESS_KEY = "CLIENT_LAST_HOST_ADDRESS_KEY";
    
    private JoinContract.Presenter presenter;
    
    private Button buttonConnect;
    private EditText fieldHostAddress;
    
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
        
        // Setup UI references
        this.buttonConnect = root.findViewById(R.id.buttonConnect);
        this.fieldHostAddress = root.findViewById(R.id.fieldHostAddress);
        
        // Setup UI
        buttonConnect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                presenter.tryConnectingToHost(fieldHostAddress.getText().toString());
            }
        });
        
        SharedPreferences preferences = getContext().getSharedPreferences(SharedPreferences.class.getClass().getSimpleName(), Context.MODE_PRIVATE);
        
        String lastHostAddress = preferences.getString(CLIENT_LAST_HOST_ADDRESS_KEY, "");
        
        if (lastHostAddress.length() > 0)
        {
            fieldHostAddress.setText(lastHostAddress);
        }
        
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
    public void connect(String hostAddress)
    {
        buttonConnect.setEnabled(false);
        fieldHostAddress.setEnabled(false);
        
        // Save the address, it will be used as default address next time the CLIENT screen starts
        SharedPreferences preferences = getContext().getSharedPreferences(SharedPreferences.class.getClass().getSimpleName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(CLIENT_LAST_HOST_ADDRESS_KEY, hostAddress);
        editor.commit();
    }
    
    @Override
    public void beginConnect()
    {
        
    }
    
    @Override
    public void connectionFailure(String error)
    {
        buttonConnect.setEnabled(true);
        fieldHostAddress.setEnabled(true);
        
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
        buttonConnect.setEnabled(true);
        fieldHostAddress.setEnabled(true);
        
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
