package lt.vtmc.ems.zwaclaw.myflagquiz;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.Set;

public class MainActivity extends AppCompatActivity {

    /*Preference keys*/
    public static final String CHOICES = "pref_numberOfChoices";
    public static final String REGIONS = "pref_regionsToInclude";
    /*Application is running?*/
    private boolean phoneDevice = true;
    /*Preferences changed?*/
    private boolean preferencesChanged = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        /*Defaults prerefences for SharedPrefences object*/
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        /*Register listener object for SharedPreferences*/
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(preferenceChangeListener);
        /*Get device screen size*/
        int screenSize = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        /*If screen size is tablet size ...*/
        if (screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE || screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE) {
            phoneDevice = false;
        }
        /*If screen size is phone size ...*/
        if (phoneDevice) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @Override
    protected void onStart(){
        super.onStart();

        if (preferencesChanged) {
            MainActivityFragment quizFragment = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.quizFragment);
            quizFragment.updateGuessRows(PreferenceManager.getDefaultSharedPreferences(this));
            quizFragment.updateRegions(PreferenceManager.getDefaultSharedPreferences(this));
            quizFragment.resetQuiz();
            preferencesChanged = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /*Get information about screen orientation*/
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT){
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_main, menu);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent preferencesIntent = new Intent(this, SettingsActivity.class);
        startActivity(preferencesIntent);
        return super.onOptionsItemSelected(item);
    }


    /*Listener object for SharedPreferences*/
    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            /*User change application preferences*/
            preferencesChanged = true;
            /*MainActivityFragment initialization*/
            MainActivityFragment quizFragment = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.quizFragment);
            /*Conditional statement for changed preferences*/
            if (key.equals(CHOICES)) {
                /*Button rows prefences update*/
                quizFragment.updateGuessRows(sharedPreferences);
                /*Reset Quiz*/
                quizFragment.resetQuiz();
            } else if (key.equals(REGIONS)) {
                /*Get selected regions list*/
                Set<String> regions = sharedPreferences.getStringSet(REGIONS, null);
                /*If select more than one region ...*/
                if (regions != null && regions.size() > 0) {
                    quizFragment.updateRegions(sharedPreferences);
                    quizFragment.resetQuiz();
                }
                /*If not selected any region ...*/
                else {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    regions.add(getString(R.string.default_region));
                    editor.putStringSet(REGIONS, regions);
                    editor.apply();
                    Toast.makeText(MainActivity.this, R.string.default_region_message, Toast.LENGTH_SHORT).show();
                }

                /*User information: Quiz reset!*/
                Toast.makeText(MainActivity.this, R.string.reset_quiz, Toast.LENGTH_SHORT).show();
            }
        }
    };
}
