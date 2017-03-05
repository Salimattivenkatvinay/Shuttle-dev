package com.doomonafireball.betterpickers.hmspicker;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.simplecity.amp_library.R;


public class HmsPicker extends LinearLayout implements Button.OnClickListener, Button.OnLongClickListener {

    protected int mInputSize = 3;
    protected final Button mNumbers[] = new Button[10];
    protected int mInput[] = new int[mInputSize];
    protected int mInputPointer = -1;
    protected ImageButton mDelete;
    protected Button mLeft, mRight;
    protected HmsView mEnteredHms;
    protected final Context mContext;

    private TextView mHoursLabel, mMinutesLabel; //mSecondsLabel;
    private Button mSetButton;

    protected View mDivider;
    private ColorStateList mTextColor;
    private int mKeyBackgroundResId;
    private int mButtonBackgroundResId;
    private int mDividerColor;
    private int mDeleteDrawableSrcResId;
    private int mTheme = -1;

    public HmsPicker(Context context) {
        this(context, null);
    }

    public HmsPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        LayoutInflater layoutInflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(getLayoutId(), this);

        // Init defaults
        mTextColor = getResources().getColorStateList(R.color.dialog_text_color_holo_dark);
        mKeyBackgroundResId = R.drawable.key_background_dark;
        mButtonBackgroundResId = R.drawable.button_background_dark;
        mDividerColor = getResources().getColor(R.color.default_divider_color_dark);
        mDeleteDrawableSrcResId = R.drawable.ic_backspace_dark;
    }

    protected int getLayoutId() {
        return R.layout.hms_picker_view;
    }

    public void setTheme(int themeResId) {
        mTheme = themeResId;
        if (mTheme != -1) {
            TypedArray a = getContext().obtainStyledAttributes(themeResId, R.styleable.BetterPickersDialogFragment);
            mTextColor = a.getColorStateList(R.styleable.BetterPickersDialogFragment_bpTextColor);
            mKeyBackgroundResId = a.getResourceId(R.styleable.BetterPickersDialogFragment_bpKeyBackground,
                    mKeyBackgroundResId);
            mButtonBackgroundResId = a.getResourceId(R.styleable.BetterPickersDialogFragment_bpButtonBackground,
                    mButtonBackgroundResId);
            mDividerColor = a.getColor(R.styleable.BetterPickersDialogFragment_bpDividerColor, mDividerColor);
            mDeleteDrawableSrcResId = a.getResourceId(R.styleable.BetterPickersDialogFragment_bpDeleteIcon,
                    mDeleteDrawableSrcResId);
            a.recycle();
        }

        restyleViews();
    }

    private void restyleViews() {
        for (Button number : mNumbers) {
            if (number != null) {
                number.setTextColor(mTextColor);
                number.setBackgroundResource(mKeyBackgroundResId);
            }
        }
        if (mDivider != null) {
            mDivider.setBackgroundColor(mDividerColor);
        }
        if (mHoursLabel != null) {
            mHoursLabel.setTextColor(mTextColor);
            mHoursLabel.setBackgroundResource(mKeyBackgroundResId);
        }
        if (mMinutesLabel != null) {
            mMinutesLabel.setTextColor(mTextColor);
            mMinutesLabel.setBackgroundResource(mKeyBackgroundResId);
        }

        if (mDelete != null) {
            mDelete.setBackgroundResource(mButtonBackgroundResId);
            mDelete.setImageDrawable(getResources().getDrawable(mDeleteDrawableSrcResId));
        }
        if (mEnteredHms != null) {
            mEnteredHms.setTheme(mTheme);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        View v1 = findViewById(R.id.first);
        View v2 = findViewById(R.id.second);
        View v3 = findViewById(R.id.third);
        View v4 = findViewById(R.id.fourth);
        mEnteredHms = (HmsView) findViewById(R.id.hms_text);
        mDelete = (ImageButton) findViewById(R.id.delete);
        mDelete.setOnClickListener(this);
        mDelete.setOnLongClickListener(this);

        mNumbers[1] = (Button) v1.findViewById(R.id.key_left);
        mNumbers[2] = (Button) v1.findViewById(R.id.key_middle);
        mNumbers[3] = (Button) v1.findViewById(R.id.key_right);

        mNumbers[4] = (Button) v2.findViewById(R.id.key_left);
        mNumbers[5] = (Button) v2.findViewById(R.id.key_middle);
        mNumbers[6] = (Button) v2.findViewById(R.id.key_right);

        mNumbers[7] = (Button) v3.findViewById(R.id.key_left);
        mNumbers[8] = (Button) v3.findViewById(R.id.key_middle);
        mNumbers[9] = (Button) v3.findViewById(R.id.key_right);

        mLeft = (Button) v4.findViewById(R.id.key_left);
        mNumbers[0] = (Button) v4.findViewById(R.id.key_middle);
        mRight = (Button) v4.findViewById(R.id.key_right);
        setLeftRightEnabled(false);

        for (int i = 0; i < 10; i++) {
            mNumbers[i].setOnClickListener(this);
            mNumbers[i].setText(String.format("%d", i));
            mNumbers[i].setTag(R.id.numbers_key, i);
        }
        updateHms();

        mHoursLabel = (TextView) findViewById(R.id.hours_label);
        mMinutesLabel = (TextView) findViewById(R.id.minutes_label);
        //mSecondsLabel = (TextView) findViewById(R.id.seconds_label);
        mDivider = findViewById(R.id.divider);

        restyleViews();
        updateKeypad();
    }

    public void updateDeleteButton() {
        boolean enabled = mInputPointer != -1;
        if (mDelete != null) {
            mDelete.setEnabled(enabled);
        }
    }

    @Override
    public void onClick(View v) {
        v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        doOnClick(v);
        updateDeleteButton();
    }

    protected void doOnClick(View v) {
        Integer val = (Integer) v.getTag(R.id.numbers_key);
        // A number was pressed
        if (val != null) {
            addClickedNumber(val);
        } else if (v == mDelete) {
            if (mInputPointer >= 0) {
                for (int i = 0; i < mInputPointer; i++) {
                    mInput[i] = mInput[i + 1];
                }
                mInput[mInputPointer] = 0;
                mInputPointer--;
            }
        }
        updateKeypad();
    }

    @Override
    public boolean onLongClick(View v) {
        v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        if (v == mDelete) {
            mDelete.setPressed(false);

            reset();
            updateKeypad();
            return true;
        }
        return false;
    }

    public void reset() {
        for (int i = 0; i < mInputSize; i++) {
            mInput[i] = 0;
        }
        mInputPointer = -1;
        updateHms();
    }

    private void updateKeypad() {
        // Update the h:m:s
        updateHms();
        // enable/disable the "set" key
        enableSetButton();
        // Update the backspace button
        updateDeleteButton();

    }

    // Update the time displayed in the picker:
    // Special cases:
    protected void updateHms() {
        // Put "-" in digits that was not entered by passing -1
        // Hide digit by passing -2 (for highest hours digit only);

        int hours1 = -1;
        mEnteredHms.setTime(mInput[2], mInput[1], mInput[0]//, mInput[1], mInput[0]
        		);
    }

    private void addClickedNumber(int val) {
        if (mInputPointer < mInputSize - 1) {
            for (int i = mInputPointer; i >= 0; i--) {
                mInput[i + 1] = mInput[i];
            }
            mInputPointer++;
            mInput[0] = val;
        }
    }

    // Enable/disable the set key
    private void enableSetButton() {
        if (mSetButton == null) {
            return;
        }

        // Nothing entered - disable
        if (mInputPointer == -1) {
            mSetButton.setEnabled(false);
            return;
        }

        mSetButton.setEnabled(mInputPointer >= 0);
    }

    public void setSetButton(Button b) {
        mSetButton = b;
        enableSetButton();
    }

    public int getHours() {
        return mInput[2];
    }

    public int getMinutes() {
        return mInput[1] * 10 + mInput[0];
    }

//    public int getSeconds() {
//        return mInput[1] * 10 + mInput[0];
//    }

    @Override
    public Parcelable onSaveInstanceState() {
        final Parcelable parcel = super.onSaveInstanceState();
        final SavedState state = new SavedState(parcel);
        state.mInput = mInput;
        state.mInputPointer = mInputPointer;
        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        final SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());

        mInputPointer = savedState.mInputPointer;
        mInput = savedState.mInput;
        if (mInput == null) {
            mInput = new int[mInputSize];
            mInputPointer = -1;
        }
        updateKeypad();
    }

    private static class SavedState extends BaseSavedState {

        int mInputPointer;
        int[] mInput;
        int mAmPmState;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        SavedState(Parcel in) {
            super(in);
            mInputPointer = in.readInt();
            in.readIntArray(mInput);
            mAmPmState = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(mInputPointer);
            dest.writeIntArray(mInput);
            dest.writeInt(mAmPmState);
        }

        public static final Creator<SavedState> CREATOR
                = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    public int getTime() {
        return mInput[2] * 3600 + mInput[1] * 600 + mInput[0] * 60  // +  mInput[1] * 10 + mInput[0]
        		;
    }

    public void saveEntryState(Bundle outState, String key) {
        outState.putIntArray(key, mInput);
    }

    public void restoreEntryState(Bundle inState, String key) {
        int[] input = inState.getIntArray(key);
        if (input != null && mInputSize == input.length) {
            for (int i = 0; i < mInputSize; i++) {
                mInput[i] = input[i];
                if (mInput[i] != 0) {
                    mInputPointer = i;
                }
            }
            updateHms();
        }
    }

    protected void setLeftRightEnabled(boolean enabled) {
        mLeft.setEnabled(enabled);
        mRight.setEnabled(enabled);
        if (!enabled) {
            mLeft.setContentDescription(null);
            mRight.setContentDescription(null);
        }
    }
}
