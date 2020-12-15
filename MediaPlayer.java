package com.hfad.mypodcastplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URL;

public class MediaPlayer extends AppCompatActivity {

    private ImageView imagePlayPause;
    private TextView textCurrentTime, textTotalDuration, textshowTitle;
    private SeekBar playerSeekBar;
    private android.media.MediaPlayer mediaPlayer;
    private Handler handler = new Handler();


    public static final String URLTEST = "Url";
    public static final String TITLE = "TITLE";
    private String UrlIntent;
    private String Title;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_player);

        imagePlayPause = findViewById(R.id.imagePlayPause);
        textCurrentTime = findViewById(R.id.textCurrentTime);
        textTotalDuration = findViewById(R.id.totalDuration);
        playerSeekBar = findViewById(R.id.seekBarPlayer);
        textshowTitle = findViewById(R.id.textshowTitle);
        mediaPlayer = new android.media.MediaPlayer();
        handler = new Handler();

        Intent intent = getIntent();
        UrlIntent = intent.getStringExtra(URLTEST);
        Log.d("Uri56", "Urlintent:  "  + UrlIntent);

        Title = intent.getStringExtra(TITLE);
        Log.d("Uri56","TitlePut" + Title);
        textshowTitle.setText(Title);





        playerSeekBar.setMax(100);
        imagePlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaPlayer.isPlaying())
                {
                    handler.removeCallbacks(updater);
                    mediaPlayer.pause();
                    imagePlayPause.setImageResource(R.drawable.ic_play);
                }
                else
                {
                    mediaPlayer.start();
                    imagePlayPause.setImageResource(R.drawable.ic_pause);
                    updateSeekBar();
                }
            }
        });

        prepareMediaPlayer();

        playerSeekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                SeekBar seekBar = (SeekBar) view;
                int playPosition = (mediaPlayer.getDuration() / 100) * seekBar.getProgress();
                mediaPlayer.seekTo(playPosition);
                textCurrentTime.setText(MillisecondsToTimer(mediaPlayer.getCurrentPosition()));
                return false;
            }
        });

        mediaPlayer.setOnBufferingUpdateListener(new android.media.MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(android.media.MediaPlayer mediaPlayer, int i) {
                playerSeekBar.setSecondaryProgress(i);
            }
        });
        mediaPlayer.setOnCompletionListener(new android.media.MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(android.media.MediaPlayer mediaPlayer) {
                playerSeekBar.setProgress(0);
                imagePlayPause.setImageResource(R.drawable.ic_play);
                textCurrentTime.setText(R.string.zero);
                textTotalDuration.setText(R.string.zero);
                mediaPlayer.reset();
                prepareMediaPlayer();
            }
        });

    }


    private void prepareMediaPlayer()
    {
        try{
            mediaPlayer.setDataSource(UrlIntent);
            mediaPlayer.prepare();
            textTotalDuration.setText(MillisecondsToTimer(mediaPlayer.getDuration()));
        }

        catch(Exception ex){
            Log.d("geturiExep", ex.toString() + UrlIntent);
            Toast.makeText(this, ex.getMessage()  , Toast.LENGTH_SHORT).show();
        }
    }

    private Runnable updater = new Runnable() {
        @Override
        public void run() {
            updateSeekBar();
            long currentDuration = mediaPlayer.getCurrentPosition();
            textCurrentTime.setText(MillisecondsToTimer(currentDuration));
        }
    };

    private void updateSeekBar(){
        if(mediaPlayer.isPlaying()){
            playerSeekBar.setProgress((int) (((float) mediaPlayer.getCurrentPosition() / mediaPlayer.getDuration()) * 100));
            handler.postDelayed(updater, 1000);
        }
    }

    private String MillisecondsToTimer(long milliseconds) {
        String timerString = "";
        String secondsString;

        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);

        if (hours > 0) {
            timerString = hours + ":";
        }
        if (seconds < 10) {
            secondsString = "0" + seconds;
        }else
            secondsString = "" + seconds;
        timerString = timerString + minutes + ":" + secondsString;
        return timerString;


    }

}