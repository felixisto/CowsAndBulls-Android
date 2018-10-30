package com.example.game.cowsbulls.game;

import java.util.ArrayList;

public class GameSession
{
    public static final String YOU_LABEL = "You";
    public static final String OPPONENT_LABEL = "Opponent";
    
    protected final boolean firstToGo;
    
    private int gameTurn;
    
    public final String guessWordAsString;
    private ArrayList<GuessCharacter> guessWord;
    public final int guessWordNumberOfCharacters;
    
    private boolean gameIsOver;
    
    private String log;
    
    private String lastGuessAttempt;
    
    public GameSession(boolean firstToGo, String guessWord)
    {
        this.firstToGo = firstToGo;
        
        this.gameTurn = 1;
        
        this.guessWordAsString = guessWord;
        
        this.guessWord = GuessCharacter.stringToGuessCharacters(guessWord);
        this.guessWordNumberOfCharacters = guessWord.length();
        
        this.log = "";
        
        this.gameIsOver = false;
        
        this.lastGuessAttempt = "";
    }
    
    public int getGameTurn()
    {
        return gameTurn;
    }
    
    public boolean isMyTurn()
    {
        if (firstToGo)
        {
            return gameTurn % 2 != 0;
        }
        else
        {
            return gameTurn % 2 == 0;
        }
    }
    
    public boolean isOpponentTurn()
    {
        return !isMyTurn();
    }
    
    public String getLog(String opponentName)
    {
        return log.replaceAll(OPPONENT_LABEL, opponentName);
    }
    
    public void endGame() throws GameError
    {
        if (gameIsOver)
        {
            throw GameError.BAD_LOGIC_GAME_IS_OVER;
        }
    
        gameIsOver = true;
    }
    
    public void guessAttempt(String guess)
    {
        lastGuessAttempt = guess;
    }
    
    public void opponentGuessResponse(String response) throws GameError
    {
        if (gameIsOver)
        {
            throw GameError.BAD_LOGIC_GAME_IS_OVER;
        }
        
        if (!isMyTurn())
        {
            throw GameError.BAD_LOGIC_WRONG_TURN;
        }
        
        gameTurn += 1;
    
        addGuessTextToLog(YOU_LABEL + " " + response);
    }
    
    public GuessResult opponentIsGuessing(String guessCharactersAsString) throws GameError
    {
        if (gameIsOver)
        {
            throw GameError.BAD_LOGIC_GAME_IS_OVER;
        }
    
        if (!isOpponentTurn())
        {
            throw GameError.BAD_LOGIC_WRONG_TURN;
        }
    
        if (guessCharactersAsString.length() != guessWordNumberOfCharacters)
        {
            throw GameError.BAD_LOGIC_INVALID_GUESS_CHARACTER_LENGTH;
        }
        
        gameTurn += 1;
    
        GuessResult guessResult = GuessCharacter.guessResult(guessWord, GuessCharacter.stringToGuessCharacters(guessCharactersAsString));
        
        addGuessTextToLog(OPPONENT_LABEL + " " + guessResult.messageWithGuess);
        
        if (guessResult.hasSuccessfullyGuessed())
        {
            gameIsOver = true;
        }
        
        return guessResult;
    }
    
    public void addMyChatTextToLog(String string)
    {
        String temp = YOU_LABEL + ": " + string + "\n";
        temp = temp.concat(log);
        log = temp;
    }
    
    public void addOpponentChatTextToLog(String string)
    {
        String temp = OPPONENT_LABEL + ": " + string + "\n";
        temp = temp.concat(log);
        log = temp;
    }
    
    private void addGuessTextToLog(String string)
    {
        String temp = String.valueOf(gameTurn-1) + ". " + string + "\n";
        temp = temp.concat(log);
        log = temp;
    }
    
    public String getLastGuessAttempt()
    {
        return lastGuessAttempt;
    }
}
