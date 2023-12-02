package com.svk.window;

import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    private GLESView glesView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // hide action bar or title bar
        getSupportActionBar().hide();

        // full screen
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        // hiding system bars and ime
        WindowInsetsControllerCompat windowInsetsControllerCompat = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsControllerCompat.hide(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());

        // forced landscape orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // setting background color
        getWindow().getDecorView().setBackgroundColor(Color.rgb(0, 0, 0));

        glesView = new GLESView(this);
        setContentView(glesView);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}