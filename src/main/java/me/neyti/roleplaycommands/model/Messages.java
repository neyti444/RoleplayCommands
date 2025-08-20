package me.neyti.roleplaycommands.model;

public final class Messages {

    // system
    public final String PLAYERS_ONLY;
    public final String COMMAND_DISABLED;
    public final String NO_PERMISSION;
    public final String NO_ARGUMENTS;
    public final String INVALID_NUMBER;
    public final String RELOADED;
    public final String USAGE_ERPC;

    // chat
    public final String ME;
    public final String DO;
    public final String WHISPER;
    public final String SHOUT;
    public final String TODO;
    public final String N; // /n non-rp

    // try
    public final String TRY_SUCCESS;
    public final String TRY_FAILURE;

    // roll
    public final String ROLL_DEFAULT;
    public final String ROLL_EXTENDED;

    // coin
    public final String COIN_HEADS;
    public final String COIN_TAILS;

    // dice
    public final String DICE;

    public Messages(
            String PLAYERS_ONLY, String COMMAND_DISABLED, String NO_PERMISSION, String NO_ARGUMENTS, String INVALID_NUMBER,
            String RELOADED, String USAGE_ERPC,
            String ME, String DO, String WHISPER, String SHOUT, String TODO, String N,
            String TRY_SUCCESS, String TRY_FAILURE,
            String ROLL_DEFAULT, String ROLL_EXTENDED,
            String COIN_HEADS, String COIN_TAILS,
            String DICE
    ) {
        this.PLAYERS_ONLY = PLAYERS_ONLY;
        this.COMMAND_DISABLED = COMMAND_DISABLED;
        this.NO_PERMISSION = NO_PERMISSION;
        this.NO_ARGUMENTS = NO_ARGUMENTS;
        this.INVALID_NUMBER = INVALID_NUMBER;
        this.RELOADED = RELOADED;
        this.USAGE_ERPC = USAGE_ERPC;

        this.ME = ME;
        this.DO = DO;
        this.WHISPER = WHISPER;
        this.SHOUT = SHOUT;
        this.TODO = TODO;
        this.N = N;

        this.TRY_SUCCESS = TRY_SUCCESS;
        this.TRY_FAILURE = TRY_FAILURE;

        this.ROLL_DEFAULT = ROLL_DEFAULT;
        this.ROLL_EXTENDED = ROLL_EXTENDED;

        this.COIN_HEADS = COIN_HEADS;
        this.COIN_TAILS = COIN_TAILS;

        this.DICE = DICE;
    }
}
