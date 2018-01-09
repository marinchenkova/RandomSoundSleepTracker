package marinchenko.name.randomsoundsleeptracker;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import marinchenko.name.randomsoundsleeptracker.util.DynamicToneGen;


public class MainActivity extends Activity {

    private final static String START = "START";
    private final static String STOP = "STOP";

    private final DynamicToneGen toneGen = new DynamicToneGen();

    private boolean isPlaying = false;
    private boolean randomDelay = false;
    private int freqHz = 300;
    private int durationMs = 1000;
    private int delayMs = 1000;
    private double ratio = 0.2;
    private int curve = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setProgress(this.curve);
        ((TextView) findViewById(R.id.seekBarValue)).setText(String.valueOf(this.curve));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setCurve(progress);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void setCurve(final int value) {
        this.curve = value;
        ((TextView) findViewById(R.id.seekBarValue)).setText(String.valueOf(this.curve));
    }

    public void buttonClick(View view) {
        this.isPlaying = !this.isPlaying;
        this.freqHz = (int) parseEditText(this.freqHz, (EditText) findViewById(R.id.freqText));
        this.durationMs = (int) parseEditText(this.durationMs, (EditText) findViewById(R.id.durationText));
        this.delayMs = (int) parseEditText(this.delayMs, (EditText) findViewById(R.id.delayText));
        this.ratio = parseEditText(this.ratio, (EditText) findViewById(R.id.ratioText));

        if (this.isPlaying) {
            if (!randomDelay) toneGen.setBlockTrue();
            toneGen.setRandom(randomDelay);
            toneGen.play(this.freqHz,
                    this.curve,
                    this.durationMs,
                    this.delayMs,
                    this.ratio);
        } else {
            toneGen.setRandom(false);
        }

        final String text = this.isPlaying ? STOP : START;
        final Button button = (Button) view;
        button.setText(text);
    }

    public void checkBoxRandomClick(View view) {
        this.randomDelay = ((CheckBox) view).isChecked();
    }

    private double parseEditText(final double def,
                              final EditText editText) {
        final String text = editText.getText().toString();
        return text.equals("") ? (int) def : Double.parseDouble(text);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    InputMethodManager imm = (InputMethodManager)
                            getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }
}
