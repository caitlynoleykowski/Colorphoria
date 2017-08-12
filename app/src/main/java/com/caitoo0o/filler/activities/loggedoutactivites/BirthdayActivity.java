package com.caitoo0o.filler.activities.loggedoutactivites;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.caitoo0o.filler.R;
import com.caitoo0o.filler.utils.Utils;

public class BirthdayActivity extends AppCompatActivity {

    NumberPicker numberPickerMonth;
    NumberPicker numberPickerDay;
    NumberPicker numberPickerYear;
    TextView birthdayTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_birthday);

        numberPickerMonth = (NumberPicker) findViewById(R.id.number_picker_month);
        numberPickerDay = (NumberPicker) findViewById(R.id.number_picker_day);
        numberPickerYear = (NumberPicker) findViewById(R.id.number_picker_year);

        birthdayTextView = (TextView) findViewById(R.id.birthday_text_view);

        numberPickerMonth.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                updateBirthdayTextView();
            }
        });
        numberPickerDay.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                updateBirthdayTextView();
            }
        });
        numberPickerYear.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                updateBirthdayTextView();
            }
        });
    }

    private void updateBirthdayTextView() {
        String birthday;
        String month = Integer.toString(numberPickerMonth.getValue());
        String day = Integer.toString(numberPickerDay.getValue());
        String year = Integer.toString(numberPickerYear.getValue());
        birthday = month + "/" + day + "/" + year;

        birthdayTextView.setText(birthday);
    }

    private Birthday getBirthday() {
        int month = numberPickerMonth.getValue();
        int day = numberPickerDay.getValue();
        int year = numberPickerYear.getValue();

        return new Birthday(month, day, year);
    }

    public void onContinue(View v) {
        if (getBirthday().isGreaterThanOrEqualThirteenYearsAgo()) {
            startActivity(new Intent(BirthdayActivity.this, TermsActivity.class));
        } else {
            Utils.showConfirmDialog(
                    this,
                    "Sorry, you're not eligible to use Filler :/",
                    "Ok",
                    "Ok",
                    new Utils.ConfirmDialogHandler() {
                        @Override
                        public void onPositiveButton(Dialog confirmDialog) {
                            confirmDialog.dismiss();
                        }

                        @Override
                        public void onNegativeButton(Dialog confirmDialog) {
                            confirmDialog.dismiss();
                        }
                    },
                    false);
        }
    }

    private class Birthday {
        private int month;
        private int day;
        private int year;

        Birthday(int month, int day, int year) {
            this.month = month;
            this.day = day;
            this.year = year;
        }

        // TODO: implement this
        public boolean isGreaterThanOrEqualThirteenYearsAgo() {
            return true;
        }
    }
}
