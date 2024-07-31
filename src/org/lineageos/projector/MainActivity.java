package org.lineageos.projector;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.ToggleButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

public class MainActivity extends Activity {
    File motorVerify;
    File currentOrientation;
    Spinner brightnessSpinner;
    ToggleButton toggleRotate;
    ToggleButton toggleTouches;
    SeekBar focusBar;
    Button dimBtn;
    Switch switchEnable;
    int retvalValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String[] brightStrings = {getResources().getString(R.string.high),
                                    getResources().getString(R.string.medium),
                                    getResources().getString(R.string.low)};

        final String[] patternStrings = {getResources().getString(R.string.checker),
                                    getResources().getString(R.string.white),
                                    getResources().getString(R.string.black),
                                    getResources().getString(R.string.red),
                                    getResources().getString(R.string.green),
                                    getResources().getString(R.string.blue),
                                    getResources().getString(R.string.stripes)};
        ArrayAdapter<String> brightAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, brightStrings);

        final File path = new File("/sys/class/sec/sec_projector");
        final File retval = new File(path, "retval");
        final File projKey = new File(path, "proj_key");
        final File brightness = new File(path, "brightness");
        final File rotate = new File(path, "rotate_screen");
        currentOrientation = new File(path, "screen_direction");
        motorVerify = new File(path, "motor_verify");
        final File projectionVerify = new File(path, "projection_verify");

        switchEnable = (Switch) findViewById(R.id.switchEnable);
        Button patternBtn = (Button) findViewById(R.id.patternButton);
        brightnessSpinner = (Spinner) findViewById(R.id.spinner);
        toggleRotate = (ToggleButton) findViewById(R.id.toggleRotate);
        toggleTouches = (ToggleButton) findViewById(R.id.toggleTouches);
        focusBar = (SeekBar) findViewById(R.id.seekBar);
        dimBtn = (Button) findViewById(R.id.dimButton);

        focusBar.setEnabled(false);
        brightnessSpinner.setEnabled(false);

        try {
            FileInputStream fis = new FileInputStream(retval);
            Scanner scanner = new Scanner(fis);
            retvalValue = scanner.nextInt();
            fis.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (retvalValue==10) {
            initUI();
            uiUnlock(true, false);
        } else if(retvalValue>=300) {
            initUI();
            uiUnlock(true, false);
            focusBar.setProgress(360-retvalValue);
        } else if (retvalValue>10) {
            initUI();
            uiUnlock(true, true);
        } else {
            switchEnable.setChecked(false);
        }

        try {
            if (Settings.System.getInt(getApplicationContext().getContentResolver(), "show_touches") == 1) {
                toggleTouches.setChecked(true);
            }
        } catch (Settings.SettingNotFoundException ignored) { }

        switchEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    writeValue(projKey, "1");
                    uiUnlock(true, false);
                } else {
                    writeValue(projKey, "0");
                    toggleTouches.setChecked(false);
                    uiUnlock(false, false);
                }
            }
        });

        focusBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                new setFocus().execute(60-i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        brightnessSpinner.setAdapter(brightAdapter);
        brightnessSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                writeValue(brightness, String.valueOf(i+1));
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        toggleRotate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    writeValue(rotate, "3");
                } else {
                    writeValue(rotate, "0");
                }
            }
        });

        toggleTouches.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    Settings.System.putInt(getApplicationContext().getContentResolver(), "show_touches", 1);
                } else {
                    Settings.System.putInt(getApplicationContext().getContentResolver(), "show_touches", 0);
                }
            }
        });

        patternBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setItems(patternStrings, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switchEnable.setChecked(true);
                        if (i<3) {
                            writeValue(projectionVerify, String.valueOf(i));
                        } else if (i<6) {
                            writeValue(projectionVerify, String.valueOf(i+1));
                        } else {
                            writeValue(projectionVerify, String.valueOf(i+2));
                        }
                        uiUnlock(true, true);
                    }
                });
                builder.show();
            }
        });

        dimBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Settings.System.putInt(getApplicationContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 1);
            }
        });
    }
    private void writeValue(File f, String s) {
        try {
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(s.getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void uiUnlock(boolean lock, boolean isPattern) {
        focusBar.setEnabled(lock);
        brightnessSpinner.setEnabled(lock);
        if (!isPattern) {
            toggleRotate.setEnabled(lock);
            toggleTouches.setEnabled(lock);
            dimBtn.setEnabled(lock);
        } else {
            toggleRotate.setEnabled(false);
            toggleTouches.setEnabled(false);
            dimBtn.setEnabled(false);
        }
    }
    private void initUI() {
        switchEnable.setChecked(true);
        char c;
        try {
            FileInputStream fis = new FileInputStream(currentOrientation);
            c = (char) fis.read();
            fis.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (c == '0') {
            toggleRotate.setChecked(false);
        } else {
            toggleRotate.setChecked(true);
        }
    }
    public class setFocus extends AsyncTask<Integer, Void, Void> {
        @Override
        protected Void doInBackground(Integer... params) {
            writeValue(motorVerify, String.valueOf(params[0]));
            return null;
        }
    }
}