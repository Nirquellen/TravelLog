package com.example.dragonmaster.knihajazd02.view;

import android.content.Context;
import android.support.design.widget.TextInputLayout;
import android.util.AttributeSet;
import android.view.ViewParent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

/**
 * Created by Dragon Master on 6.4.2018.
 */

public class AutoCompleteTextInput extends  android.support.v7.widget.AppCompatAutoCompleteTextView {

    public AutoCompleteTextInput(Context context) {
        super(context);
    }

    public AutoCompleteTextInput(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoCompleteTextInput(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        final InputConnection ic = super.onCreateInputConnection(outAttrs);
        if (ic != null && outAttrs.hintText == null) {
            final ViewParent parent = getParent();
            if (parent instanceof TextInputLayout) {
                outAttrs.hintText = ((TextInputLayout) parent).getHint();
            }
        }
        return ic;
    }

}
