package uz.unidev.puzzle15.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;

import uz.unidev.puzzle15.R;

public class MainActivity extends AppCompatActivity {

    private ViewGroup playBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        playBtn = findViewById(R.id.play_now);
        playBtn.setOnClickListener(view -> {
            startActivity(new Intent(this, GameActivity.class));
        });
    }
}