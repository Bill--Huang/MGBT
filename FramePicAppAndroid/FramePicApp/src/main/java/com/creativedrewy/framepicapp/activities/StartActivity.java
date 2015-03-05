package com.creativedrewy.framepicapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

import com.creativedrewy.framepicapp.R;

/**
 * Initial activity for the whole application
 */
public class StartActivity extends Activity {
    private Button _startMasterButton;
    private Button _startPicTakerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.start_activity_layout);

        _startMasterButton = (Button) findViewById(R.id.startMasterButton);
        _startMasterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(StartActivity.this, SystemMasterActivity.class));
            }
        });

        _startPicTakerButton = (Button) findViewById(R.id.startPicTakerButton);
        _startPicTakerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(StartActivity.this, PicTakerActivity.class));
            }
        });
    }
    
}
