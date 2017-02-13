package xyz.sayangoswami.urbandictionary;

import android.app.IntentService;
import android.content.Intent;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Sayan on 28/08/16.
 */
public class VoteService extends IntentService {
    public VoteService(){
        super("VoteService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        String url = intent.getStringExtra("url");

        try {
            URL mURL = new URL(url);

            HttpURLConnection connection = (HttpURLConnection) mURL.openConnection();
            InputStream in = connection.getInputStream();
        } catch (MalformedURLException ignored) {
        } catch (IOException ignored) {
        }

    }
}
