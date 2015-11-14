package com.gamicarts.hipsterbirdfree;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import com.gamicarts.hipsterbirdfree.game.GameHandler;
import com.gamicarts.hipsterbirdfree.game.GameView;
import com.gamicarts.hipsterbirdfree.game.state.GameState;
import com.gamicarts.hipsterbirdfree.game.task.Game;
import com.google.android.gms.ads.*;
import com.google.android.gms.plus.model.people.Person;

import java.util.HashMap;

public class HipsterBirdFree extends Activity {

    public static AssetManager ASSET_MANAGER = null;
    public static Context CONTEXT = null;
    public static Resources RESOURCES = null;

    public static int SCREEN_WIDTH;
    public static int SCREEN_HEIGHT;
    public static int STANDARD_SCREEN_WIDTH = 800;
    public static int STANDARD_SCREEN_HEIGHT = 480;
    public static Typeface HOBO;

    public static GameHandler GAME_HANDLER;

    public static AudioManager AUDIO_MANAGER;
    public static MediaPlayer GAMIC_THEME;
    public static MediaPlayer GAME_SONG;
    public static SoundPool soundPool; //Want to eventually re-name static variables to lower case
    public static HashMap<Integer, Integer> soundPoolMap = new HashMap<Integer, Integer>();

    private GameView gameView;
    private RelativeLayout.LayoutParams layoutParams;
    private RelativeLayout relativeLayout;
    private InterstitialAd interstitial;
    private AdRequest adRequest;

    private boolean active;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        active = true;

        //Basic set up of application
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //Need resources to allocate images
        RESOURCES = getResources();

        //Need asset manager to open files
        ASSET_MANAGER = getAssets();

        //Needed to write files
        CONTEXT = this;

        //Grabbing screen dimensions
        Display display = getWindowManager().getDefaultDisplay();
        SCREEN_WIDTH = display.getWidth(); //Deprecated
        SCREEN_HEIGHT = display.getHeight();

        HOBO = Typeface.createFromAsset(ASSET_MANAGER, "fonts/Hobo.ttf");

        AUDIO_MANAGER = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        GAMIC_THEME = MediaPlayer.create(CONTEXT, R.raw.gamictheme);
        GAME_SONG = MediaPlayer.create(CONTEXT, R.raw.gamesong);

        soundPool = new SoundPool(13, AudioManager.STREAM_MUSIC, 0);

        soundPoolMap.put(R.raw.powerup, soundPool.load(this, R.raw.powerup, 1));
        soundPoolMap.put(R.raw.hit, soundPool.load(this, R.raw.hit, 2));
        soundPoolMap.put(R.raw.button, soundPool.load(this, R.raw.button, 3));
        soundPoolMap.put(R.raw.point, soundPool.load(this, R.raw.point, 4));

        adRequest = new AdRequest.Builder().build();
        interstitial = new InterstitialAd(this);
        interstitial.setAdUnitId("ca-app-pub-9120681913103393/5316034468");

        interstitial.loadAd(adRequest);

        if (GAME_HANDLER == null) {
            GAME_HANDLER = new GameHandler();
            GAME_HANDLER.setActive(true);
            GAME_HANDLER.start();
        }

        //Setting up layout for game
        gameView = new GameView(this, GAME_HANDLER);
        gameView.getHolder().addCallback(gameView);
        relativeLayout = new RelativeLayout(CONTEXT);
        layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        relativeLayout.addView(gameView);

        adHandler.start();

        setContentView(relativeLayout);
    }

    @Override
    public void onResume() {
        super.onResume();

        this.gameView = new GameView(this, GAME_HANDLER);
        active = true;
    }

    @Override
    public void onDestroy() {
        active = false;

        super.onDestroy();
    }

    private Thread adHandler = new Thread() {

        private boolean shown = false;

        @Override
        public void run() {
            Runnable adViewRunnable = new Runnable() {

                @Override
                public void run() {
                    if (gameView != null && GAME_HANDLER != null && GAME_HANDLER.getGameTask() instanceof Game) {
                        Game game = (Game) GAME_HANDLER.getGameTask();

                        if (game.currentState == GameState.LOST && !shown) {
                            interstitial.show();

                            shown = true;
                        } else if (game.currentState != GameState.LOST && shown) {
                            shown = false;

                            interstitial.loadAd(adRequest);
                        }
                    }
                }

            };

            while (active) {
                runOnUiThread(adViewRunnable);

                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    };

}
