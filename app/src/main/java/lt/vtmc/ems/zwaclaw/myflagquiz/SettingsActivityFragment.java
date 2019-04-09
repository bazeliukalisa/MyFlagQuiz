package lt.vtmc.ems.zwaclaw.myflagquiz;

import android.preference.PreferenceFragment;
import android.os.Bundle;


public class SettingsActivityFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle bundle){
        super.onCreate(bundle);
        addPreferencesFromResource(R.xml.preferences);
    }
}
