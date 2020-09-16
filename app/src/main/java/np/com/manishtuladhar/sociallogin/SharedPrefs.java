package np.com.manishtuladhar.sociallogin;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.HashMap;
import java.util.Map;

public class SharedPrefs {

    SharedPreferences sharedPreferences;

    public static SharedPrefs instance = null;

    public static SharedPrefs getInstance()
    {
        if(instance == null)
        {
            synchronized (SharedPrefs.class)
            {
                instance = new SharedPrefs();

            }
        }
        return instance;
    }

    public void saveUserData(Context context, String displayName, String email,String userImage)
    {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("displayName",displayName);
        editor.putString("email",email);
        editor.putString("userImage",userImage);
        editor.apply();
    }


    public HashMap<String, String> getUserData(Context context)
    {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        HashMap<String,String> userData= new HashMap<String,String>();
        String displayName = sharedPreferences.getString("displayName","Test Name");
        String email = sharedPreferences.getString("email","test@gmail.com");
        String userImage = sharedPreferences.getString("userImage",
                "https://cdn.icon-icons.com/icons2/1736/PNG/512/4043260-avatar-male-man-portrait_113269.png");
        userData.put("displayName",displayName);
        userData.put("email",email);
        userData.put("userImage",userImage);
       return userData;
    }
}
