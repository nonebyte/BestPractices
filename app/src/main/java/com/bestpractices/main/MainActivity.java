package com.bestpractices.main;

import android.app.Activity;
import android.os.Bundle;

import com.bestpractices.R;
import com.bestpractices.base.ContextManager;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ContextManager.init(this);

        setContentView(R.layout.activity_main);
    }
}
