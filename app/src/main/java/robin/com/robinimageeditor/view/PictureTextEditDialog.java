package robin.com.robinimageeditor.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import robin.com.robinimageeditor.R;
import robin.com.robinimageeditor.data.share.InputTextSharableData;

public class PictureTextEditDialog extends Dialog {

    public interface OnClickSaveListener {

        void onSave(InputTextSharableData inputTextSharableData);
    }


    private static final String TAG = "PictureTextEditDialog";

    private EditText etInput;

    private OnClickSaveListener mOnClickSaveListener;

    private int mTextColor = 0;
    private String mTextInputId = null;

    private InputTextSharableData mInputTextSharableData;

    private PictureColorGroup pcgColors;

    private Context mContext;

    public PictureTextEditDialog(Context context, InputTextSharableData inputTextSharableData, OnClickSaveListener onClickSaveListener) {
        super(context, R.style.InputTextDialog);
        setContentView(R.layout.editor_text_input_activity);
        mContext = context;
        mInputTextSharableData = inputTextSharableData;
        mOnClickSaveListener = onClickSaveListener;
        Window window = getWindow();
        if (window != null) {
            window.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            window.setFlags(LayoutParams.FLAG_FULLSCREEN, LayoutParams.FLAG_FULLSCREEN);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pcgColors = (PictureColorGroup) findViewById(R.id.pcgColors);
        etInput = (EditText) findViewById(R.id.etInput);

        initListener();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mInputTextSharableData != null) {
            mTextColor = mInputTextSharableData.getColor();
            etInput.setTextColor(mTextColor);

            mTextInputId = mInputTextSharableData.getId();
            String text = mInputTextSharableData.getText();
            if (text == null) {
                text = "";
            }
            etInput.setText(text);
            Selection.setSelection(etInput.getText(), text.length());

            for (int i = 0; i < pcgColors.getChildCount(); i++) {
                PictureColorRadio child = (PictureColorRadio) pcgColors.getChildAt(i);
                if (child.getColor() == mInputTextSharableData.getColor()) {
                    child.setChecked(true);
                    break;
                }
            }
        } else {
            mTextColor = pcgColors.getCheckColor();
        }
    }

    public void setInputTextSharableData(InputTextSharableData inputTextSharableData) {
        this.mInputTextSharableData = inputTextSharableData;
    }

    private void clearData() {
        etInput.setText("");
        mInputTextSharableData = null;
        mTextInputId = null;
        dismiss();
    }

    private void initListener() {
        findViewById(R.id.tvCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearData();
            }
        });
        findViewById(R.id.tvComplete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = etInput.getText().toString().trim();
                if (!TextUtils.isEmpty(text) && mOnClickSaveListener != null) {
                    mOnClickSaveListener.onSave(new InputTextSharableData(mTextInputId, text, mTextColor));
                }
                clearData();
            }
        });

        pcgColors.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                mTextColor = pcgColors.getCheckColor();
                etInput.setTextColor(mTextColor);
            }
        });

        etInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 100) {
                    Toast.makeText(mContext, "最多编辑100个文字", Toast.LENGTH_SHORT).show();
                    etInput.setText(s.subSequence(0, 100));
                    etInput.setSelection(100);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
}
