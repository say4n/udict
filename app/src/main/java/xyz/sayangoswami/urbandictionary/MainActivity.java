package xyz.sayangoswami.urbandictionary;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    /**
     * Needed for using vector drawables on buildTools > v23.2.0 for support on below API 19
     */
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }


    private List<Definitions> definitionsList = new ArrayList<>();
    private DefinitionsAdapter mAdapter;

    private ArrayList<String> suggestions;
    private ArrayAdapter<String> mArrayAdapter;


    private FirebaseAnalytics mFirebaseAnalytics;
    RecyclerView.LayoutManager mLayoutManager;
    RecyclerView recyclerView;
    AppCompatAutoCompleteTextView mEditText;
    Button go;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);

        // Vector icons
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        //Analytics
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // Recycler Part
        recyclerView = (RecyclerView) findViewById(R.id.definition_placeholder);
        mAdapter = new DefinitionsAdapter(definitionsList);
        mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);


        //I/O part

        mEditText = (AppCompatAutoCompleteTextView) findViewById(R.id.text);

        go = (Button) findViewById(R.id.search_go);


        go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mAdapter.clearData();

                String keyword = mEditText.getText().toString().replaceAll("^\\s+|\\s+$", "");
                Log.i("WTF",keyword);

                mEditText.dismissDropDown();

                // Hide keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                if(!isInternetAvailable()){
                    RelativeLayout parentLayout = (RelativeLayout) findViewById(R.id.parent);
                    final Snackbar mSnack = Snackbar.make(parentLayout, "No internet connectivity :-(", Snackbar.LENGTH_LONG);
                    mSnack.setAction("Dismiss", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mSnack.dismiss();
                                }
                            });
                    mSnack.show();

                }
                else if(!keyword.equals("") && !keyword.equals(" ")){

                    processRequest(keyword);

                }
            }
        });

        mEditText.setOnEditorActionListener(new AppCompatEditText.OnEditorActionListener(){
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                        actionId == EditorInfo.IME_ACTION_DONE ||
                        keyEvent.getAction() == KeyEvent.ACTION_DOWN &&
                                keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) {

                    mAdapter.clearData();

                    String keyword = mEditText.getText().toString().replaceAll("^\\s+|\\s+$", "");
                    Log.i("WTF",keyword);

                    mEditText.dismissDropDown();

                    if(!isInternetAvailable()){
                        RelativeLayout parentLayout = (RelativeLayout) findViewById(R.id.parent);
                        final Snackbar mSnack = Snackbar.make(parentLayout, "No internet connectivity :-(", Snackbar.LENGTH_LONG);
                        mSnack.setAction("Dismiss", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mSnack.dismiss();
                            }
                        });
                        mSnack.show();

                    }
                    else if(!keyword.equals("") && !keyword.equals(" ")){

                        processRequest(keyword);

                    }
                }

                return false;
            }
        });

        mEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // get autocomplete suggestions and set them
                String typedText = mEditText.getText().toString().replaceAll("^\\s+|\\s+$", "");
                new AutoCompleteSuggestionTask().execute(typedText);
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {}

        });


        suggestions= new ArrayList<>();
        mArrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, suggestions);
        mEditText.setAdapter(mArrayAdapter);

        //Log.v("Firebase Token", FirebaseInstanceId.getInstance().getToken());


    } // End of onCreate


    /**
     * Proper auto completion implemented now!
     */
    class AutoCompleteSuggestionTask extends AsyncTask<String, Void, ArrayList<String>> {

        @Override
        protected ArrayList<String> doInBackground(String... strings) {
            String input = strings[0];
            ArrayList<String> suggest = new ArrayList<>();
            try {
                suggest = getSuggestions(input);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return suggest;
        }

        @Override
        protected void onPostExecute(ArrayList<String> suggestions){
            super.onPostExecute(suggestions);
            mArrayAdapter.clear();
            mArrayAdapter.addAll(suggestions);
            mArrayAdapter.notifyDataSetChanged();
            mEditText.showDropDown();
        }
    }

    ArrayList<String> getSuggestions(String word) throws IOException, JSONException {

        URL url = new URL("http://api.urbandictionary.com/v0/autocomplete?term="
                + URLEncoder.encode(word, "UTF-8"));

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        InputStream in = connection.getInputStream();
        BufferedReader streamReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        StringBuilder responseStrBuilder = new StringBuilder();

        String inputStr;
        while ((inputStr = streamReader.readLine()) != null)
            responseStrBuilder.append(inputStr);

        JSONArray array = new JSONArray(responseStrBuilder.toString());

        suggestions.clear();
        
        for(int i = 0; i < array.length(); i++){
            suggestions.add(array.getString(i));
        }

        return suggestions;
    }


    /**
     * get definitions from UD in another thread (non blocking)
     */
    class DownloadTask extends AsyncTask<String, Void, ArrayList<Definitions>> {

        @Override
        protected ArrayList<Definitions> doInBackground(String... params) {
            String word = params[0];
            return getDefs(word);
        }

        @Override
        protected void onPostExecute(ArrayList<Definitions> defs) {
            super.onPostExecute(defs);

            definitionsList.clear();
            definitionsList.addAll(defs);
            mAdapter.notifyDataSetChanged();

            RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.definition_placeholder);
            ProgressBar mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);

            mRecyclerView.smoothScrollToPosition(0);
            mLayoutManager.scrollToPosition(0);

            mRecyclerView.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);



            if(defs.size() == 0){

                // If the word isn't on UD database, add it using browser.
                RelativeLayout parentLayout = (RelativeLayout) findViewById(R.id.parent);

                final String mQuery = mEditText.getText().toString().replaceAll("^\\s+|\\s+$", "");

                final Snackbar mSnack = Snackbar.make(parentLayout, "Whoa we couldn't find \"" + mQuery + "\" !", Snackbar.LENGTH_LONG);
                mSnack.setAction("Add definition", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String mUrl = "http://www.urbandictionary.com/define.php?term=" + mQuery;

                        FirebaseAnalytics(mQuery + " <-- Adding definition",FirebaseAnalytics.Param.VALUE);

                        Intent mAdd= new Intent(Intent.ACTION_VIEW, Uri.parse(mUrl));
                        startActivity(Intent.createChooser(mAdd,"Add the definition by choosing a browser"));

                    }
                });
                mSnack.show();
            }

        }

    }


    /**
     * Returns array of definitions after getting them from UD
     * Used inside AsyncTask
     */
    private ArrayList<Definitions> getDefs(String word) {
        ArrayList<Definitions> definitions = new ArrayList<>();

        try {
            URL url = new URL("http://api.urbandictionary.com/v0/define?term="
                    + URLEncoder.encode(word, "UTF-8"));

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            InputStream in = connection.getInputStream();
            BufferedReader streamReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            StringBuilder responseStrBuilder = new StringBuilder();

            String inputStr;
            while ((inputStr = streamReader.readLine()) != null)
                responseStrBuilder.append(inputStr);

            JSONObject jsonObject = new JSONObject(responseStrBuilder.toString());
            JSONArray array = jsonObject.getJSONArray("list");

            for (int i = 0; i < array.length(); ++i) {
                JSONObject object = array.getJSONObject(i);
                Definitions tmp = new Definitions();

                // capitalizes the first word of each definition
                String mDefinition = object.getString("definition").substring(0, 1).toUpperCase() +
                        object.getString("definition").substring(1);

                tmp.setDefinition(mDefinition);
                tmp.setAuthor("\nby " + object.getString("author"));
                tmp.setThumb_up(object.getInt("thumbs_up"));
                tmp.setThumb_down(object.getInt("thumbs_down"));
                tmp.setId(object.getInt("defid"));
                tmp.setWord(word);
                definitions.add(tmp);
            }

            in.close();
        } catch (MalformedURLException ignored) {
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return definitions;
    }


    /**
     * Navigation drawer handle back button while open
     */
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    /**
     * Nav drawer items
     */
    @SuppressLint("SetTextI18n")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_share) {

            Intent mShare = new Intent();
            mShare.setAction(Intent.ACTION_SEND);
            mShare.putExtra(Intent.EXTRA_TEXT, "Check out this brand spanking new Urban Dictionary " +
                    "client @ http://sayan98.github.io/urbandictionary !!");
            mShare.setType("text/plain");

            startActivity(Intent.createChooser(mShare,"Spread the love using .."));

        } else if (id == R.id.about) {

            Intent launchAbout = new Intent(getBaseContext(), AboutActivity.class);
            startActivity(launchAbout);

        } else if (id == R.id.changelog){

            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Changelog");

            WebView wv = new WebView(this);
            String text = getResources().getString(R.string.changes);
            wv.loadData(text, "text/html", "utf-8");

            alert.setView(wv);

            alert.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });

            AlertDialog dialog = alert.create();
            dialog.show();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    /**
     * checks if device is online
     * @return boolean isInternetAvailable
     */
    private boolean isInternetAvailable() {

        // Does what it says it does.

        ConnectivityManager cm =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();

    }


    /**
     * for hiding and showing of views !
     */
    private void processRequest(String keyword){

        // Just another module

        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.definition_placeholder);
        ProgressBar mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mRecyclerView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);

        try {
            String encoded_keyword = URLEncoder.encode(keyword, "UTF-8");
            new DownloadTask().execute(encoded_keyword);
            FirebaseAnalytics(keyword,FirebaseAnalytics.Param.SEARCH_TERM);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }


    /**
     * self explanatory
     */
    private void FirebaseAnalytics(String description,String param){

        //Analytics

        Bundle anal = new Bundle();
        anal.putString(param,description);
        anal.putString(FirebaseAnalytics.Param.CONTENT_TYPE,"text");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SEARCH,anal);
    }

}
// End of MainActivity