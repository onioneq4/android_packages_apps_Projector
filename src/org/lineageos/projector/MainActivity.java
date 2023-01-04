package org.lineageos.projector;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Switch sw = (Switch) findViewById(R.id.switch2);
        SeekBar sb = (SeekBar) findViewById(R.id.seekBar);
        TextView tv = (TextView) findViewById(R.id.textView);
        Spinner spn = (Spinner) findViewById(R.id.spinner);
        Spinner brspn = (Spinner) findViewById(R.id.spinner2);

        File path = new File("/sys/class/sec/sec_projector");
        File retval = new File(path, "retval");
        final File enable = new File(path, "proj_key");
        final File sharp = new File(path, "motor_verify");
        final File rotate = new File(path, "rotate_screen");
        final File brightness = new File(path, "brightness");

        String[] pos = {"Vertical", "Horizontal", "Vertical (flipped)", "Horizontal (flipped)"};
        String[] bright = {"High", "Medium", "Low"};

        ArrayAdapter<String> posadapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, pos);
        ArrayAdapter<String> briadapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, bright);

        try {
            FileInputStream fis = new FileInputStream(retval);
            char c = (char)fis.read();
            fis.close();
            if (c == '0') {
                sw.setChecked(false);
            } else {
                sw.setChecked(true);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                try {
                    FileOutputStream fos = new FileOutputStream(enable);
                    if (b == true) {
                        fos.write("1".getBytes());
                    } else {
                        fos.write("0".getBytes());
                    }
                    fos.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            FileOutputStream fos = null;

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                try {
                    fos = new FileOutputStream(sharp);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                try {
                    fos.write(String.valueOf(i).getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        spn.setAdapter(posadapter);
        brspn.setAdapter(briadapter);

        spn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    FileOutputStream fos = new FileOutputStream(rotate);
                    fos.write(String.valueOf(i).getBytes());
                    fos.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        brspn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    FileOutputStream fos = new FileOutputStream(brightness);
                    fos.write(String.valueOf(i+1).getBytes());
                    fos.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }
}
