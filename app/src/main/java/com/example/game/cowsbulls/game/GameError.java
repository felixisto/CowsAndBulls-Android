package com.example.game.cowsbulls.game;

public class GameError extends Exception
{
    public static final GameError BAD_LOGIC = new GameError("BAD LOGIC");
    public static final GameError BAD_LOGIC_GAME_IS_OVER = new GameError("BAD LOGIC - GAME IS OVER");
    public static final GameError BAD_LOGIC_WRONG_TURN = new GameError("BAD LOGIC - WRONG TURN");
    public static final GameError BAD_LOGIC_INVALID_GUESS_CHARACTER_LENGTH = new GameError("BAD LOGIC - INVALID GUESS CHARACTER LENGTH");
    
    public GameError(String description)
    {
        super(description);
    }
}
