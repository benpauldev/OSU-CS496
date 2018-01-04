package com.example.benjaminfondell.androidui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.content.Intent;
import android.database.AbstractCursor;
import android.database.CharArrayBuffer;
import android.database.Cursor;

/**
 * Created by benjaminfondell on 10/27/17.
 */

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView GridViewItem = (TextView) findViewById(R.id.Layout3);
        GridViewItem.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GridViewActivity.class);
                startActivity(intent);
            }
        });

        TextView HorizontalViewItem = (TextView) findViewById(R.id.Layout1);
        HorizontalViewItem.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, HorizontalViewActivity.class);
                startActivity(intent);
            }
        });

        TextView VerticalViewItem = (TextView) findViewById(R.id.Layout2);
        VerticalViewItem.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, VerticalViewActivity.class);
                startActivity(intent);
            }
        });

        TextView RelativeViewItem = (TextView) findViewById(R.id.Layout4);
        RelativeViewItem.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RelativeViewActivity.class);
                startActivity(intent);
            }
        });
    }

}
