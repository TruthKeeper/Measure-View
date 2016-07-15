package com.tk.measureview.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.tk.measureview.R;
import com.tk.measureview.view.MeasureView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.edittext1)
    EditText edittext1;
    @Bind(R.id.edittext2)
    EditText edittext2;
    @Bind(R.id.checkbox)
    CheckBox checkbox;
    @Bind(R.id.btn_init)
    Button btnInit;
    @Bind(R.id.measureView)
    MeasureView measureView;
    @Bind(R.id.ll_value)
    LinearLayout llValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.btn_init, R.id.btn_do})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_init:
                String m = edittext1.getText().toString().trim();
                String s = edittext2.getText().toString().trim();
                if (!TextUtils.isEmpty(m)) {
                    measureView.setmMeasure(Integer.parseInt(m));
                }
                if (!TextUtils.isEmpty(s)) {
                    measureView.setmPathSize(Integer.parseInt(s));
                }
                measureView.clearContent();
                measureView.setLineOn(checkbox.isChecked());
                llValue.removeAllViews();
                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                p.setMargins(0, 10, 0, 0);
                SeekBar seekBar;
                for (int i = 0; i < measureView.getmMeasure(); i++) {
                    seekBar = new SeekBar(this);
                    seekBar.setMax(10);
                    llValue.addView(seekBar, p);
                }
                break;
            case R.id.btn_do:
                float[] value = new float[measureView.getmMeasure()];
                for (int i = 0; i < llValue.getChildCount(); i++) {
                    value[i] = ((SeekBar) llValue.getChildAt(i)).getProgress() / 10f;
                }
                measureView.clearContent();
                measureView.setValue(value);
                Toast.makeText(this, String.format("百分之%f", measureView.getValue() * 100), Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
