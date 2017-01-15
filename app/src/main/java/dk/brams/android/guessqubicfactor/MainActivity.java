package dk.brams.android.guessqubicfactor;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "TBR:";
    private final Activity mActivity=this;

    private static int sSecret = 0;
    private static int sGood =0;
    private static int sBad =0;
    private static SharedPreferences sSharedPreferences;
    private EditText mAnswerField;
    private ImageView mImgView;
    private Animation mAnimation;
    private static MyRunnable sRunnable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sRunnable = new MyRunnable(this);

        mImgView = (ImageView) findViewById(R.id.imageGrade);
        mAnimation = new AlphaAnimation(0.00f, 1.00f);
        mAnimation.setDuration(100);
        mAnimation.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                Log.d(TAG, "onAnimationStart: ");
                mAnimation.setRepeatMode(0);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                Log.d(TAG, "onAnimationRepeat: ");
                animation.cancel();

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Log.d(TAG, "onAnimationEnd: trying to stop animation");
                mImgView.setAnimation(null);
            }
        });

        mImgView.setAnimation(mAnimation);

        mAnswerField = (EditText) findViewById(R.id.answer_text);
        mAnswerField.setOnKeyListener(new View.OnKeyListener()
        {
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                if (event.getAction() == KeyEvent.ACTION_DOWN)
                {
                    switch (keyCode)
                    {
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_ENTER:
                            processAnswer(v);
                            return true;
                        default:
                            break;
                    }
                }
                return false;
            }
        });

        // Create a new challenge and fill in fields
        newQuestion(this);

        mImgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                processAnswer(view);
            }
        });

    }


    private void processAnswer(View view) {
        String answer= mAnswerField.getText().toString();
        if (!answer.equals("")) {
            if (Integer.parseInt(answer) == sSecret) {
                mImgView.setBackgroundResource(R.mipmap.ic_thumbs_up);
                sGood++;
            } else {
                mImgView.setBackgroundResource(R.mipmap.ic_thumbs_down);

                Snackbar.make(view, getString(R.string.correct_answer)+ sSecret, Snackbar.LENGTH_LONG).show();
                sBad++;
            }
            mImgView.startAnimation(mAnimation);
            updateScore(mActivity);
        }

        // Execute the Runnable in 3 seconds
        mHandler.postDelayed(sRunnable, 3000);
    }



    // The following is best practise when using timers in Android
    // See http://stackoverflow.com/a/3039718/1682139
    private static class MyHandler extends Handler {}
    private final MyHandler mHandler = new MyHandler();

    public static class MyRunnable implements Runnable {
        private final WeakReference<Activity> mActivity;

        public MyRunnable(Activity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void run() {
            Activity activity = mActivity.get();
            if (activity != null) {
                newQuestion(activity);
            }
        }
    }


    private static void newQuestion(Activity activity){

        // set the answer icon back
        ImageView imgView = (ImageView) activity.findViewById(R.id.imageGrade);
        imgView.setAnimation(null);
        imgView.setBackgroundResource(R.mipmap.ic_launcher);


        // Find a new secret number
        Random r = new Random();
        sSecret = r.nextInt(100-11)+11;

        // set the big number
        TextView number = (TextView) activity.findViewById(R.id.big_number_text);
        number.setText(String.format(Locale.ENGLISH, "%d", sSecret * sSecret * sSecret));

        // clear the previous answer
        EditText ans = (EditText) activity.findViewById(R.id.answer_text);
        ans.setText("");

        updateScore(activity);
    }


    private static void updateScore(Activity activity) {
        TextView scoreField = (TextView) activity.findViewById(R.id.text_score);
        int score = sGood *10- sBad *20;
        scoreField.setText(String.format(Locale.ENGLISH, "%d", score));

        int prevRecord = sSharedPreferences.getInt(activity.getString(R.string.highScore), 0);
        if (score>prevRecord) {
            // Make quick announcement
            Toast.makeText(activity, R.string.highScoreMsg, Toast.LENGTH_SHORT).show();

            // Write to preferences
            SharedPreferences.Editor editor = sSharedPreferences.edit();
            editor.putInt(activity.getString(R.string.highScore), score);
            editor.apply();
        }


    }


}
