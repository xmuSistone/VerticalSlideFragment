package com.stone.verticalslide;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.stone.verticalslide.library.DragLayout;


public class MainActivity extends AppCompatActivity {

    DragLayout mDragLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportFragmentManager().beginTransaction().add(R.id.frame1, new Fragment1())
                .add(R.id.frame2, new Fragment2()).commit();
        mDragLayout = (DragLayout) findViewById(R.id.drag_layout);
        mDragLayout.setOnPageSwitchListener(new DragLayout.OnPageSwitchListener() {
            @Override
            public void onSwitchPageToOne() {
                Toast.makeText(MainActivity.this, "onSwitchPageToOne", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSwitchPageToTwo() {
                Toast.makeText(MainActivity.this, "onSwitchPageToTwo", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
