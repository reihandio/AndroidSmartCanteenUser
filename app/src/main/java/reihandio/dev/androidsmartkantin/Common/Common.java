package reihandio.dev.androidsmartkantin.Common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.format.DateFormat;

import java.util.Calendar;
import java.util.Locale;

import reihandio.dev.androidsmartkantin.Model.User;
import reihandio.dev.androidsmartkantin.Remote.APIService;
import reihandio.dev.androidsmartkantin.Remote.RetrofitClient;

public class Common {

    public static User currentUser;

    private static final String fcmUrl = "https://fcm.googleapis.com/";

    public static APIService getFCMService()
    {
        return RetrofitClient.getClient(fcmUrl).create(APIService.class);
    }

    public static String convertCodeToStatus(String status) {
        if(status.equals("0"))
            return "Rincian Dikirim";
        else if(status.equals("1"))
            return "Diproses";
        else
            return "Dikirim";

    }

    public static final String USER_KEY = "User";
    public static final String PWD_KEY = "Password";
    public static final String DELETE = "Delete";
    public static boolean isConnectedToInternet(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if(connectivityManager != null)
        {
            NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
            if(info != null)
            {
                for(int i=0;i<info.length;i++)
                {
                    if(info[i].getState() == NetworkInfo.State.CONNECTED)
                        return true;
                }
            }
        }
        return false;
    }

    public static String getDate (long time)
    {
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(time);
        StringBuilder date = new StringBuilder(DateFormat.format("dd-MM-yyyy HH:mm"
                ,calendar)
                .toString());
        return date.toString();
    }
}
