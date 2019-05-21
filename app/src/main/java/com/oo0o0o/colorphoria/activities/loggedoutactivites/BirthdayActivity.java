package com.oo0o0o.colorphoria.activities.loggedoutactivites;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.oo0o0o.colorphoria.R;
import com.oo0o0o.colorphoria.activities.BaseColorphoriaActivity;
import com.oo0o0o.colorphoria.utils.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;
import java.util.Date;

public class BirthdayActivity extends BaseColorphoriaActivity {

    private static final String TAG = BirthdayActivity.class.getName();

    public static final String BIRTHDAY_STATE_KEY = "BIRTHDAY_STATE_KEY";
    public static final String STARTED_FROM_SIGNUP = "STARTED_FROM_SIGNUP";

    NumberPicker numberPickerMonth;
    NumberPicker numberPickerDay;
    NumberPicker numberPickerYear;
    TextView birthdayTextView;

    boolean startedFromSignup = true;
    boolean startingAnotherActivity = false;

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

        updateBirthdayTextView();

        if (savedInstanceState != null) {
            startedFromSignup = savedInstanceState.getBoolean(BirthdayActivity.STARTED_FROM_SIGNUP);
        } else if (getIntent().hasExtra(BirthdayActivity.STARTED_FROM_SIGNUP)) {
            startedFromSignup = getIntent().getBooleanExtra(BirthdayActivity.STARTED_FROM_SIGNUP, true);
        }
    }

    @Override
    public void onResume() {
        startingAnotherActivity = false;

        // If started from a google sign in but has since been signed out, go back to login
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (!startedFromSignup && auth.getCurrentUser() == null) {
            startActivity(new Intent(BirthdayActivity.this, LoginActivity.class));
            finish();
        }

        super.onResume();
    }

    @Override
    public void onBackPressed() {
        signOut();
        super.onBackPressed();
    }

    @Override
    public void onStop() {
        if (!startingAnotherActivity) {
            signOut();
        }
        super.onStop();
    }

    private void setupDisplayedDateToCurrentDate() {

    }

    private void updateBirthdayTextView() {
        String birthday;
        String month = String.format("%02d", numberPickerMonth.getValue());
        String day = String.format("%02d", numberPickerDay.getValue());
        String year = String.format("%02d", numberPickerYear.getValue());
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
            Intent termsActivityIntent = new Intent(BirthdayActivity.this, TermsActivity.class);
            termsActivityIntent.putExtra(BirthdayActivity.BIRTHDAY_STATE_KEY, getBirthday().getBirthdayDate());
            termsActivityIntent.putExtra(BirthdayActivity.STARTED_FROM_SIGNUP, startedFromSignup);
            startingAnotherActivity = true;
            startActivity(termsActivityIntent);
        } else {
            signOut();
            Utils.showAlertDialog(
                    this,
                    getString(R.string.birthday_error_not_eligible),
                    getString(android.R.string.ok),
                    new Utils.AlertDialogHandler() {
                        @Override
                        public void onPositiveButton(Dialog confirmDialog) {
                            confirmDialog.dismiss();
                            startActivity(new Intent(BirthdayActivity.this, LoginActivity.class));
                        }
                    },
                    false);
        }
    }

    private void signOut() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            auth.signOut();
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

        public Date getBirthdayDate() {
            Calendar c = Calendar.getInstance();
            c.set(year, month - 1, day);
            Date birthdayDate = c.getTime();
            return birthdayDate;
        }

        /* Returns true if this Birthday would make someone 13 years old or older
         *
         */
        public boolean isGreaterThanOrEqualThirteenYearsAgo() {
            Calendar c = Calendar.getInstance();

            int currentYear = c.get(Calendar.YEAR);
            int currentMonth = c.get(Calendar.MONTH);
            int currentDay = c.get(Calendar.DAY_OF_MONTH);

            int thirteenYearsAgoYear = currentYear - 13;
            c.set(thirteenYearsAgoYear, currentMonth, currentDay, 0, 0);
            Date thirteenYearsAgo = c.getTime();

            c.set(year, month -1, day);
            Date birthday = c.getTime();

            if (birthday.compareTo(thirteenYearsAgo) < 0) { // Already 13 or older
                return true;
            } else if (birthday.compareTo(thirteenYearsAgo) == 0) { // Turned 13 today
                return true;
            } else { // Not 13 yet
                return false;
            }
        }
    }
}
