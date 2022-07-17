package uz.unidev.puzzle15.ui;

import static java.lang.Math.abs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import uz.unidev.puzzle15.Coordinate;
import uz.unidev.puzzle15.MyBounceInterpolator;
import uz.unidev.puzzle15.R;

public class GameActivity extends AppCompatActivity {

    ViewGroup group;

    private TextView textScore;
    private TextView tvBest;
    private Chronometer textTime;
    private Button[][] buttons;
    private ArrayList<Integer> numbers;
    private Coordinate emptyCoordinate;
    private int score;
    private int bestScore = 0;

    MediaPlayer mediaPlayer;

    private long pauseTime;
    private boolean isStarted;

    private MediaPlayer clickMP;
    private boolean soundButtonStatus = true;

    private Dialog dialog;

    private SharedPreferences sharedPreferences;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        dialog = new Dialog(this);

        loadViews();
        loadButtons();

        preferences = getSharedPreferences("app_name", Context.MODE_PRIVATE);
        editor = preferences.edit();

        sharedPreferences = getSharedPreferences("best", Context.MODE_PRIVATE);

        getBestScoreFromSharedPreferences();

        if (savedInstanceState == null) {
            isStarted = preferences.getBoolean("is_playing", false);
            if (isStarted) {
                score = preferences.getInt("score", 0);
                pauseTime = preferences.getLong("pause_time", 0);
                List<String> numbersList = new ArrayList<>();
                String numbersText = preferences.getString("numbers", "1#2#3#4#5#6#7#8#9#10#11#12#13#14#15##");
                String[] numbersArray = numbersText.split("#");
                for (int i = 0; i < numbersArray.length; i++) {
                    numbersList.add(numbersArray[i]);
                }
                textScore.setText(String.valueOf(score));
                loadSavedNumbers(numbersList);
            } else {
                initNumbers();
                loadNumbersToButtons();
            }
        }
        setSoundButtonStatus();
    }

    private void getBestScoreFromSharedPreferences() {
        if (sharedPreferences.getInt("best_score", 0) != 0) {
            bestScore = sharedPreferences.getInt("best_score", 0);
            tvBest.setText(String.valueOf(bestScore));
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d("RRR", "RESTORED");
        score = savedInstanceState.getInt("score", 0);
        pauseTime = savedInstanceState.getLong("pause_time", 0);
        isStarted = savedInstanceState.getBoolean("is_started");
        List<String> numbersList = savedInstanceState.getStringArrayList("numbers");

        textScore.setText(String.valueOf(score));

        loadSavedNumbers(numbersList);

    }

    private void loadSavedNumbers(List<String> numbers) {
        for (int i = 0; i < numbers.size(); i++) {
            if (numbers.get(i).equals("")) {
                emptyCoordinate = new Coordinate(i % 4, i / 4);
            }
            buttons[i % 4][i / 4].setText(numbers.get(i));
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt("score", score);
        outState.putLong("pause_time", pauseTime);
        outState.putBoolean("is_started", isStarted);
        ArrayList<String> numbers = new ArrayList<>();
        for (int i = 0; i < group.getChildCount(); i++) {
            numbers.add(((Button) group.getChildAt(i)).getText().toString());
        }
        outState.putStringArrayList("numbers", numbers);
        super.onSaveInstanceState(outState);
    }

    private void loadViews() {
        tvBest = findViewById(R.id.tv_best);
        group = findViewById(R.id.group_items);
        textScore = findViewById(R.id.tv_score);
        textTime = findViewById(R.id.tv_time);
        findViewById(R.id.iv_back).setOnClickListener(view -> {
            didTapButton(findViewById(R.id.iv_back));
            onBackPressed();
        });
        findViewById(R.id.btn_restart).setOnClickListener(view -> onRestartGame());
        findViewById(R.id.iv_sound).setOnClickListener(this::onSoundButtonClick);
    }

    private void setSoundButtonStatus() {
        ImageView ivSound = findViewById(R.id.iv_sound);
        if (!soundButtonStatus) {
            soundButtonStatus = true;
            ivSound.setImageResource(R.drawable.not_sound);
            pause();
        } else {
            soundButtonStatus = false;
            ivSound.setImageResource(R.drawable.sound);
            play();
        }
    }

    private void onSoundButtonClick(View view) {
        didTapButton(findViewById(R.id.iv_sound));
        ImageView ivSound = (ImageView) view;
        if (!soundButtonStatus) {
            soundButtonStatus = true;
            ivSound.setImageResource(R.drawable.not_sound);
            mediaPlayer.pause();
        } else {
            soundButtonStatus = false;
            ivSound.setImageResource(R.drawable.sound);
            mediaPlayer.start();
        }
    }

    private void loadButtons() {
        ViewGroup group = findViewById(R.id.group_items);
        int count = group.getChildCount();
        int size = (int) Math.sqrt(count);
        buttons = new Button[size][size];
        for (int i = 0; i < group.getChildCount(); i++) {
            View view = group.getChildAt(i);
            Button button = (Button) view;
            button.setOnClickListener(this::onButtonClick);
            int y = i / size;
            int x = i % size;
            button.setTag(new Coordinate(x, y));
            buttons[y][x] = button;
        }
    }

    private void initNumbers() {
        numbers = new ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            numbers.add(i);
        }
    }

    private void loadNumbersToButtons() {
        shuffle();
        for (int i = 0; i < buttons.length; i++) {
            for (int j = 0; j < buttons.length; j++) {
                int index = i * 4 + j;
                if (index < 15) {
                    buttons[i][j].setText(String.valueOf(numbers.get(index)));
                }
            }
        }
        buttons[3][3].setVisibility(View.VISIBLE);
        buttons[3][3].setText("");
        emptyCoordinate = new Coordinate(3, 3);
        score = 0;
        textScore.setText(String.valueOf(score));
        isStarted = true;
        textTime.setBase(SystemClock.elapsedRealtime());
        textTime.start();
    }

    public void didTapButton(View view) {
        final Animation myAnim = AnimationUtils.loadAnimation(this, R.anim.bounce);
        MyBounceInterpolator interpolator = new MyBounceInterpolator(0.2, 20);
        myAnim.setInterpolator(interpolator);
        view.startAnimation(myAnim);
    }

    private void onRestartGame() {
        didTapButton(findViewById(R.id.btn_restart));
        loadNumbersToButtons();
    }

    private void onButtonClick(View view) {

        playClickSound();

        Button button = (Button) view;
        Coordinate c = (Coordinate) button.getTag();

        int eX = emptyCoordinate.getX();
        int eY = emptyCoordinate.getY();
        int dX = abs(c.getX() - eX);
        int dY = abs(c.getY() - eY);
        if (dX + dY == 1) {
            score++;
            textScore.setText(String.valueOf(score));
            buttons[eY][eX].setText(button.getText());
            button.setText("");
            emptyCoordinate = c;
//            buttons[c.getY()][c.getX()].setVisibility(View.INVISIBLE);
//            buttons[eY][eX].setVisibility(View.VISIBLE);
            if (isWin()) {
                saveBestScore();
                textTime.stop();
                openWinDialog(score, String.valueOf(textTime.getText()));
            }
        }
    }

    private void saveBestScore() {
        if (tvBest.getText().toString().equals("-")) {
            sharedPreferences.edit().putInt("best_score", score).apply();
            bestScore = sharedPreferences.getInt("best_score", 0);
        } else {
            int min = Math.min(score, sharedPreferences.getInt("best_score", 0));
            sharedPreferences.edit().putInt("best_score", min).apply();
            bestScore = min;
        }
        tvBest.setText(String.valueOf(bestScore));
    }

    private boolean isWin() {
        if (!(emptyCoordinate.getX() == 3 && emptyCoordinate.getY() == 3)) return false;
        for (int i = 0; i < 15; i++) {
            String s = buttons[i / 4][i % 4].getText().toString();
            if (!s.equals(String.valueOf(i + 1))) return false;
        }
        return true;
    }

    private void shuffle() {
        numbers.remove(Integer.valueOf(0));
        Collections.shuffle(numbers);
        if (isSolvable(numbers)) ;
        else shuffle();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isStarted) {
            textTime.setBase(SystemClock.elapsedRealtime() + pauseTime);
            textTime.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        pauseTime = textTime.getBase() - SystemClock.elapsedRealtime();
        textTime.stop();

        putDataToSharedPreference();
    }

    private void putDataToSharedPreference() {

        editor.putBoolean("is_playing", true);

        editor.putInt("score", score);
        editor.putLong("pause_time", pauseTime);

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < group.getChildCount() - 1; i++) {
            builder.append(((Button) group.getChildAt(i)).getText().toString()).append("#");
        }
        builder.append(((Button) group.getChildAt(15)).getText().toString());
        editor.putString("numbers", builder.toString());

        editor.putBoolean("is_music_on", true);
        editor.apply();
    }

    public boolean isSolvable(ArrayList<Integer> puzzle) {
        Log.d("TTT", "Check is solvable");
        numbers.add(0);
        int parity = 0;
        int gridWidth = (int) Math.sqrt(puzzle.size());
        int row = 0; // the current row we are on
        int blankRow = 0; // the row with the blank tile

        for (int i = 0; i < puzzle.size(); i++) {
            if (i % gridWidth == 0) { // advance to next row
                row++;
            }
            if (puzzle.get(i) == 0) { // the blank tile
                blankRow = row; // save the row on which encountered
                continue;
            }
            for (int j = i + 1; j < puzzle.size(); j++) {
                if (puzzle.get(i) > puzzle.get(j) && puzzle.get(j) != 0) {
                    parity++;
                }
            }
        }

        if (gridWidth % 2 == 0) { // even grid
            if (blankRow % 2 == 0) { // blank on odd row; counting from bottom
                return parity % 2 == 0;
            } else { // blank on even row; counting from bottom
                return parity % 2 != 0;
            }
        } else { // odd grid
            return parity % 2 == 0;
        }
    }

    private void playClickSound() {
        if (clickMP != null) {
            clickMP.release();
        }
        clickMP = MediaPlayer.create(this, R.raw.click);
        clickMP.start();
    }

    @SuppressLint("SetTextI18n")
    private void openWinDialog(Integer moves, String time) {
        dialog.setContentView(R.layout.win_layout_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable((Color.TRANSPARENT)));
        ImageView btnClose = dialog.findViewById(R.id.btnClose);
        TextView tvMoves = dialog.findViewById(R.id.tv_moves);
        TextView tvTime = dialog.findViewById(R.id.tv_time);
        CardView cardView = dialog.findViewById(R.id.card_view);
        tvMoves.setText("Moves: " + moves);
        tvTime.setText("Time: " + time);
        dialog.show();
        textTime.stop();
        btnClose.setOnClickListener(view -> {
            dialog.dismiss();
            onRestartGame();
        });
    }

    public void play() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.bg_music);
            mediaPlayer.setOnCompletionListener(mediaPlayer -> stopPlayer());
        }
        mediaPlayer.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopPlayer();
    }

    public void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    public void stop() {
        stopPlayer();
    }

    private void stopPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
            Toast.makeText(this, "MediaPlayer released !", Toast.LENGTH_SHORT).show();
        }
    }
}