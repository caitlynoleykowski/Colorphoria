package com.caitoo0o.filler.reusableuserinterface;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.NumberPicker;

/**
 * Created by caitoo0o on 8/12/2017.
 */
public class ConfigurableNumberPicker extends NumberPicker {

    public ConfigurableNumberPicker(Context context) {
        super(context);
    }

    public ConfigurableNumberPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        processAttributeSet(attrs);
    }

    public ConfigurableNumberPicker(Context context, AttributeSet attrs, int style) {
        super(context, attrs, style);
        processAttributeSet(attrs);
    }
    private void processAttributeSet(AttributeSet attrs) {
        // Get parameters from xml and set them in this NumberPicker object
        this.setMinValue(attrs.getAttributeIntValue(null, "minimum", 0));
        this.setMaxValue(attrs.getAttributeIntValue(null, "maximum", 0));
        this.setValue(attrs.getAttributeIntValue(null, "default_value", 0));
    }
}
