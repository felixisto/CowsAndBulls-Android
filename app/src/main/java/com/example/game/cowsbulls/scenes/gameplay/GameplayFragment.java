package com.example.game.cowsbulls.scenes.gameplay;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.game.cowsbulls.R;
import com.example.game.cowsbulls.scenes.main.MainActivity;
import com.chaos.view.PinView;

public class GameplayFragment extends Fragment implements GameplayContract.View
{
    private GameplayContract.Presenter presenter;
    
    private View layoutBase;
    private TextView labelInfo;
    private TextView labelYourGuessWord;
    private Button buttonGuess;
    private Button buttonChat;
    private TextView labelTurn;
    private View scrollLog;
    private TextView labelScrollLog;
    private TextView labelStatus;
    
    private View layoutGuess;
    private Button buttonGuessCancel;
    private TextView labelGuessOpponentWord;
    private PinView pinentry;
    
    private View layoutChat;
    private Button buttonChatCancel;
    private TextView labelChat;
    private EditText fieldChat;
    
    private View layoutOutcome;
    private TextView labelOutcome;
    
    public GameplayFragment()
    {
        
    }
    
    public static GameplayFragment newInstance()
    {
        return new GameplayFragment();
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_gameplay_screen, container, false);
        
        // Setup UI
        layoutBase = root.findViewById(R.id.layoutBase);
        labelInfo = root.findViewById(R.id.labelInfo);
        labelYourGuessWord = root.findViewById(R.id.labelYourGuessWord);
        buttonGuess = root.findViewById(R.id.buttonGuess);
        buttonChat = root.findViewById(R.id.buttonChat);
        labelTurn = root.findViewById(R.id.labelTurn);
        scrollLog = root.findViewById(R.id.scrollLog);
        labelScrollLog = root.findViewById(R.id.labelScrollLog);
        labelStatus = root.findViewById(R.id.labelStatus);
        
        layoutGuess = root.findViewById(R.id.layoutGuess);
        buttonGuessCancel = root.findViewById(R.id.buttonGuessCancel);
        labelGuessOpponentWord = root.findViewById(R.id.labelGuessOpponentWord);
        pinentry = root.findViewById(R.id.pinentry);
        
        layoutChat = root.findViewById(R.id.layoutChat);
        buttonChatCancel = root.findViewById(R.id.buttonChatCancel);
        labelChat = root.findViewById(R.id.labelChat);
        fieldChat = root.findViewById(R.id.fieldChat);
        
        layoutOutcome = root.findViewById(R.id.layoutOutcome);
        labelOutcome = root.findViewById(R.id.labelOutcome);
        
        buttonGuess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) 
            {
                // Show pincode layout, hide everything else
                layoutBase.setVisibility(View.INVISIBLE);
                layoutGuess.setVisibility(View.VISIBLE);
            }
        });
        
        pinentry.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                if (s.length() == pinentry.getItemCount())
                {
                    // Hide keyboard, lose focus
                    InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(pinentry.getWindowToken(), 0);
                    
                    // Alert presenter
                    presenter.guess(s.toString());
                    
                    // Hide pincode layout, show everything else
                    layoutBase.setVisibility(View.VISIBLE);
                    layoutGuess.setVisibility(View.INVISIBLE);
                    
                    // Reset pincode text
                    pinentry.setText("");
                }
            }
        
            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        buttonGuessCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                // Hide keyboard, lose focus
                InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(pinentry.getWindowToken(), 0);
                
                // Reset pincode text
                pinentry.setText("");
                
                // Hide pincode layout, show everything else
                layoutBase.setVisibility(View.VISIBLE);
                layoutGuess.setVisibility(View.INVISIBLE);
            }
        });
        
        buttonChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                // Show chat layout, hide everything else
                layoutBase.setVisibility(View.INVISIBLE);
                layoutChat.setVisibility(View.VISIBLE);
            }
        });
        
        buttonChatCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                // Reset chat field
                InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(fieldChat.getWindowToken(), 0);
                
                // Reset chat field
                fieldChat.setText("");
                
                // Hide chat layout, show everything else
                layoutBase.setVisibility(View.VISIBLE);
                layoutChat.setVisibility(View.INVISIBLE);
            }
        });
    
        fieldChat.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) 
            {
                if (actionId == EditorInfo.IME_ACTION_DONE)
                {
                    // Send chat message
                    presenter.chat(fieldChat.getText().toString());
                    
                    // Hide keyboard, lose focus
                    InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(fieldChat.getWindowToken(), 0);
                    
                    // Reset chat field
                    fieldChat.setText("");
                    
                    // Hide chat layout, show everything else
                    layoutBase.setVisibility(View.VISIBLE);
                    layoutChat.setVisibility(View.INVISIBLE);
                    
                    return true;
                }
                
                return false;
            }
        });
        
        layoutOutcome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) 
            {
                presenter.goBack();
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
    public void setPresenter(GameplayContract.Presenter presenter)
    {
        this.presenter = presenter;
    }
    
    @Override
    public void goBack()
    {
        Log.v("GameplayFragment", "Go back");
        
        getActivity().finish();
    }
    
    @Override
    public void victory(String opponentGuessWord)
    {
        pinentry.setEnabled(false);
        
        // Hide base layout
        layoutBase.setVisibility(View.INVISIBLE);
        
        // Show outcome layout
        layoutOutcome.setVisibility(View.VISIBLE);
        layoutOutcome.setBackgroundColor(Color.GREEN);
        labelOutcome.setText("YOU WIN!\nYou guessed " + opponentGuessWord);
    }
    
    @Override
    public void defeat(String myGuessWord)
    {
        pinentry.setEnabled(false);
        
        // Hide base layout
        layoutBase.setVisibility(View.INVISIBLE);
        
        // Show outcome layout
        layoutOutcome.setVisibility(View.VISIBLE);
        layoutOutcome.setBackgroundColor(Color.RED);
        labelOutcome.setText("YOU LOSE!\nOpponent guessed " + myGuessWord);
    }
    
    @Override
    public void setupUI(int guessCharacters, String myGuessWord, boolean firstToGo)
    {
        pinentry.setItemCount(guessCharacters);
        
        labelYourGuessWord.setText("My guess word: " + myGuessWord);
        
        setCurrentTurnValue(1, firstToGo);
    }
    
    @Override
    public void setCurrentTurnValue(int turn, boolean myTurn)
    {
        labelTurn.setText("Turn: " + String.valueOf(turn));
        
        if (myTurn)
        {
            buttonGuess.setEnabled(true);
            
            labelStatus.setText("It's your turn");
            labelStatus.setTextColor(Color.GREEN);
        }
        else
        {
            buttonGuess.setEnabled(false);
            
            labelStatus.setText("It's the opponents turn");
            labelStatus.setTextColor(Color.RED);
        }
    }
    
    @Override
    public void updateLog(String string)
    {
        labelScrollLog.setText(string);
    }
    
    @Override
    public void updateConnectionData(String playerName, String playerAddress)
    {
        labelInfo.setText("Opponent: " + playerName + " (" + playerAddress + ")");
    }
    
    @Override
    public void invalidGuessAttempt(String error)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(error);
        builder.setCancelable(true);
    
        builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
    
                pinentry.setText("");
            }
        });
    
        builder.create().show();
    }
    
    @Override
    public void lostConnectionAttemptingToReconnect()
    {
        FragmentActivity activity = getActivity();
        
        if (activity == null) {return;}
        
        Window window = activity.getWindow();
        
        if (window == null) {return;}
        
        View decorView = window.getDecorView();
        
        if (decorView == null) {return;}
        
        View rootView = decorView.findViewById(android.R.id.content);
        
        Snackbar snackbar = Snackbar.make(rootView, "Lost connection, attempting to reconnect...", Snackbar.LENGTH_INDEFINITE);
        
        snackbar.show();
    }
    
    @Override
    public void reconnected()
    {
        FragmentActivity activity = getActivity();
        
        if (activity == null) {return;}
        
        Window window = activity.getWindow();
        
        if (window == null) {return;}
        
        View decorView = window.getDecorView();
        
        View rootView = decorView.findViewById(android.R.id.content);
        
        Snackbar snackbar = Snackbar.make(rootView, "Reconnected!", 5000);
        
        snackbar.show();
    }
    
    @Override
    public void leave()
    {
        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
    
    @Override
    public void opponentQuit()
    {
        
    }
    
    @Override
    public void disconnected()
    {
        
    }
}
