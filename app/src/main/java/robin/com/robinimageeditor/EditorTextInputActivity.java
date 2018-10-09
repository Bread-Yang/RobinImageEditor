package robin.com.robinimageeditor;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import robin.com.robinimageeditor.bean.InputTextData;
import robin.com.robinimageeditor.util.AdjustResizeInFullScreen;
import robin.com.robinimageeditor.view.PictureColorGroup;
import robin.com.robinimageeditor.view.PictureColorRadio;

/**
 * Created by Robin Yang on 1/15/18.
 */

public class EditorTextInputActivity extends AppCompatActivity {

    private static int sCheckedId = -1;

    private static String EXTRA_CODE = "extra_input";
    private final int RESULT_CODE = 301;

    private int mTextColor = 0;
    private String mTextInputId = null;

    private EditText etInput;
//    private ColorSeekBar colorBarInput;
    private PictureColorGroup pcgColors;
    private TextView tvCancelInput;
    private TextView tvConfirmInput;

    public static Intent intent(Context context, InputTextData data) {
        Intent intent = new Intent(context, EditorTextInputActivity.class);
        intent.putExtra(EXTRA_CODE, data);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editor_text_input_activity);
        AdjustResizeInFullScreen.assistActivity(this);
        initData();
        initListener();
    }

    private void initData() {
        etInput = findViewById(R.id.etInput);
//        colorBarInput = findViewById(R.id.colorBarInput);
        tvCancelInput = findViewById(R.id.tvCancel);
        tvConfirmInput = findViewById(R.id.tvComplete);
        pcgColors = findViewById(R.id.pcgColors);

        final InputTextData readyData = (InputTextData) getIntent().getSerializableExtra(EXTRA_CODE);
        if (readyData != null) {
            mTextColor = readyData.getColor();
            etInput.setTextColor(mTextColor);

            mTextInputId = readyData.getId();
            String text = readyData.getText();
            if (text == null) {
                text = "";
            }
            etInput.setText(text);

            for (int i = 0; i < pcgColors.getChildCount(); i++) {
                PictureColorRadio child = (PictureColorRadio) pcgColors.getChildAt(i);
                if (child.getColor() == readyData.getColor()) {
                    child.setChecked(true);
                    break;
                }
            }

//            colorBarInput.setOnInitDoneListener(new ColorSeekBar.OnInitDoneListener() {
//
//                @Override
//                public void done() {
//                    int position = 8;
//                    if (readyData.getColor() != 0) {
//                        position = colorBarInput.getColorIndexPosition(readyData.getColor());
//                    }
//                    colorBarInput.setColorBarPosition(position);
//                }
//            });
        } else {
            mTextColor = pcgColors.getCheckColor();
        }
    }

    private void initListener() {
        tvCancelInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        tvConfirmInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = etInput.getText().toString().trim();
                if (TextUtils.isEmpty(text)) {
                    finish();
                } else {
                    Intent intent = new Intent();
                    intent.putExtra(String.valueOf(RESULT_CODE), new InputTextData(mTextInputId, text.toString(), mTextColor));
                    setResult(RESULT_CODE, intent);
                    finish();
                }
            }
        });

        pcgColors.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                sCheckedId = group.getCheckedRadioButtonId();
                int color = pcgColors.getCheckColor();
                etInput.setTextColor(color);
                mTextColor = color;
            }
        });
//        colorBarInput.setOnColorChangeListener(new ColorSeekBar.OnColorChangeListener() {
//
//            @Override
//            public void onColorChangeListener(int colorBarPosition, int alphaBarPosition, int color) {
//                etInput.setTextColor(color);
//                mTextColor = color;
//            }
//        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.animation_top_to_bottom);
    }
}
