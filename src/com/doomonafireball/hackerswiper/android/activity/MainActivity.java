package com.doomonafireball.hackerswiper.android.activity;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.doomonafireball.hackerswiper.android.R;
import com.doomonafireball.hackerswiper.android.util.Typer;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.MotionEventCompat;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.ClickableSpan;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ScrollView;
import android.widget.TextView;

import roboguice.inject.InjectView;

public class MainActivity extends RoboSherlockFragmentActivity implements View.OnTouchListener {

    private static final String TYPER_INDEX = "MainActivity_TyperIndex";

    @InjectView(R.id.text) private TextView text;
    @InjectView(R.id.scroll_view) private ScrollView scrollView;

    private Typer mTyper;
    private int mTouchSlop = 16;

    private int motionX = 0;
    private int motionY = 0;
    private boolean hasScrolled = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        text.append("@" + Build.MODEL.replaceAll("\\s+", "").toLowerCase().trim() + ": ");

        mTyper = new Typer(this);
        mTouchSlop = ViewConfiguration.get(this).getScaledTouchSlop();

        scrollView.setOnTouchListener(this);

        // TODO Set window padding
        int leftRightPadding = getResources().getDimensionPixelSize(R.dimen.default_padding);
        SystemBarTintManager.SystemBarConfig config = new SystemBarTintManager(this).getConfig();
        text.setPadding(leftRightPadding, config.getPixelInsetTop(false) + leftRightPadding,
                config.getPixelInsetRight() + leftRightPadding,
                config.getPixelInsetBottom() + leftRightPadding);
    }

    private boolean checkTouchSlop(MotionEvent event) {
        boolean isMove = false;
        if (Math.abs(event.getY() - motionY) >= mTouchSlop) {
            isMove = true;
        } else if (Math.abs(event.getX() - motionX) >= mTouchSlop) {
            isMove = true;
        }
        motionX = Math.round(event.getX());
        motionY = Math.round(event.getY());
        return isMove;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        int action = MotionEventCompat.getActionMasked(event);

        switch (action) {
            case (MotionEvent.ACTION_DOWN):
                motionX = Math.round(event.getX());
                motionY = Math.round(event.getY());
                hasScrolled = false;
                return super.onTouchEvent(event);
            case (MotionEvent.ACTION_MOVE):
                if (Math.abs(event.getY() - motionY) > Math.abs(event.getX() - motionX)) {
                    // User scrolled vertically
                    hasScrolled = true;
                    return super.onTouchEvent(event);
                } else if (checkTouchSlop(event)) {
                    hasScrolled = true;
                    setTyperText();
                }
                return true;
            case MotionEvent.ACTION_UP:
                if (!hasScrolled) {
                    setTyperText();
                    return true;
                }
                return super.onTouchEvent(event);
            default:
                return super.onTouchEvent(event);
        }
    }

    private void setTyperText() {
        text.append(mTyper.getTextPortion());
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.activity_main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.get_hacker_swiper_pro:
                Intent hsProIntent = new Intent(Intent.ACTION_VIEW);
                hsProIntent.setData(Uri.parse(
                        "http://play.google.com/store/apps/details?id=com.doomonafireball.hackerswiperpro.android"));
                startActivity(hsProIntent);
                return true;
            case R.id.about:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.about_hacker_swiper);

                final TextView aboutText = new TextView(this);
                int padding = getResources().getDimensionPixelSize(R.dimen.default_padding);
                aboutText.setPadding(padding, padding, padding, padding);
                String appVersionName = "x.x";
                int appVersionCode = 0;
                try {
                    appVersionName = getApplication().getPackageManager()
                            .getPackageInfo(getApplication().getPackageName(), 0).versionName;
                    appVersionCode = getApplication().getPackageManager()
                            .getPackageInfo(getApplication().getPackageName(), 0).versionCode;

                } catch (PackageManager.NameNotFoundException e) {
                    //Failed
                }
                aboutText.setText(Html.fromHtml(getString(R.string.about_hacker_swiper_text)));
                aboutText.append(
                        String.format(getString(R.string.version_dynamic), appVersionName + " b" + appVersionCode));
                aboutText.append("\n");
                SpannableString openSourceLink = makeLinkSpan(getString(R.string.open_source_licenses),
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showOpenSourceDialog();
                            }
                        });
                aboutText.append(openSourceLink);
                aboutText.append("\n");
                aboutText.setFocusable(false);
                makeLinksFocusable(aboutText);

                builder.setView(aboutText);
                builder.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        int typerIndex = savedInstanceState.getInt(TYPER_INDEX);

        if (mTyper == null) {
            mTyper = new Typer(this);
        }

        mTyper.setIndex(typerIndex);

        text.setText("");
        text.append("@" + Build.MODEL.replaceAll("\\s+", "").toLowerCase().trim() + ": ");
        text.append(mTyper.getTextPortion(typerIndex));

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt(TYPER_INDEX, mTyper.getIndex());
    }

    private void showOpenSourceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.open_source_licenses);
        String[] apacheProjects = getResources().getStringArray(R.array.apache_licensed_projects);
        StringBuilder sb = new StringBuilder();
        sb.append(getResources().getString(R.string.licenses_header));
        for (String project : apacheProjects) {
            sb.append("\u2022 ").append(project).append("\n");
        }
        sb.append("\n").append(getResources().getString(R.string.licenses_subheader));
        sb.append(getResources().getString(R.string.apache_2_0_license));
        builder.setMessage(sb.toString());
        builder.setPositiveButton(getString(R.string.close), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setCancelable(true);
        builder.setIcon(android.R.drawable.ic_menu_info_details);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private SpannableString makeLinkSpan(CharSequence text, View.OnClickListener listener) {
        SpannableString link = new SpannableString(text);
        link.setSpan(new ClickableString(listener), 0, text.length(),
                SpannableString.SPAN_INCLUSIVE_EXCLUSIVE);
        return link;
    }

    private void makeLinksFocusable(TextView tv) {
        MovementMethod m = tv.getMovementMethod();
        if ((m == null) || !(m instanceof LinkMovementMethod)) {
            if (tv.getLinksClickable()) {
                tv.setMovementMethod(LinkMovementMethod.getInstance());
            }
        }
    }

    private static class ClickableString extends ClickableSpan {

        private View.OnClickListener mListener;

        public ClickableString(View.OnClickListener listener) {
            mListener = listener;
        }

        @Override
        public void onClick(View v) {
            mListener.onClick(v);
        }
    }
}

