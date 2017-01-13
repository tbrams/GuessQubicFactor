package dk.brams.android.guessqubicfactor;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "TBR:";
    private static int mSecret = 0;
    private static int mGood=0;
    private static int mBad=0;
    private static boolean mNewHighScoreAnnounced=false;
    private Activity mActivity=this;
    private static SharedPreferences mSharedPrefs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        final EditText answerField = (EditText) findViewById(R.id.answer_text);
        final MyRunnable mRunnable = new MyRunnable(this);
        final ImageView imgView = (ImageView) findViewById(R.id.imageGrade);

        // Create a new challenge and fill in fields
        newQuestion(this);

        imgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String answer=answerField.getText().toString();
                if (!answer.equals("")) {
                    if (Integer.parseInt(answer) == mSecret) {
                        imgView.setBackgroundResource(R.mipmap.ic_thumbs_up);
                        mGood++;
                    } else {
                        imgView.setBackgroundResource(R.mipmap.ic_thumbs_down);

                        Snackbar.make(view, getString(R.string.correct_answer)+mSecret, Snackbar.LENGTH_LONG).show();
                        mBad++;
                    }

                    updateScore(mActivity);
                }

                // Execute the Runnable in 3 seconds
                mHandler.postDelayed(mRunnable, 3000);

            }
        });

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

        // set the question icon back
        ImageView img = (ImageView) activity.findViewById(R.id.imageGrade);
        img.setBackgroundResource(R.mipmap.ic_launcher);

        // Find a new secret number
        Random r = new Random();
        mSecret = r.nextInt(100-11)+11;

        // set the big number
        TextView number = (TextView) activity.findViewById(R.id.big_number_text);
        number.setText(Integer.toString(mSecret*mSecret*mSecret));

        // clear the previous answer
        EditText ans = (EditText) activity.findViewById(R.id.answer_text);
        ans.setText("");

        updateScore(activity);
    }


    private static void updateScore(Activity activity) {
        TextView scoreField = (TextView) activity.findViewById(R.id.text_score);
        int score = mGood*10-mBad*20;
        scoreField.setText(String.format("%d", score));

        int prevRecord = mSharedPrefs.getInt(activity.getString(R.string.highScore), 0);
        if (score>prevRecord) {
            // Make quick announcement
            Toast.makeText(activity, R.string.highScoreMsg, Toast.LENGTH_SHORT).show();

            // Write to preferences
            SharedPreferences.Editor editor = mSharedPrefs.edit();
            editor.putInt(activity.getString(R.string.highScore), score);
            editor.commit();
        }


    }


}
