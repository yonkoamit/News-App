package com.example.android.newsapp;

import android.text.TextUtils;
import android.util.JsonReader;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 *Helper Method related to requesting and recieving technology news data from Guardian API
 */
public class QueryUtils {
    public static final String LOG_TAG = QueryUtils.class.getSimpleName();
    /**
     * Query the Guardian API and return a list of News objects.
     */
    public static List<News> fetchNewsData(String requestUrl)
    {   // create URL object
        URL url=createUrl(requestUrl);

        // Perform http request to the URL to get a json response back
        String jsonResponse=null;
        try {
            jsonResponse=makeHttpRequest(url);
        } catch (IOException e)
        {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }
        // Extract relevant fields from the JSON response and create a list of {@link News}s
        List<News> news=extractFeatureFromJson(jsonResponse);
        return news;

    }

    /**
     * Returns new URL object from the given string URL.
     */
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL ", e);
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private static String makeHttpRequest(URL url)throws IOException{
        String jsonResponse="";

        // If the URL is null return early
        if(url==null) {
            return jsonResponse;
        }
        HttpURLConnection urlConnection=null;
        InputStream inputStream=null;
        try{
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();


            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the news JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // Closing the input stream could throw an IOException, which is why
                // the makeHttpRequest(URL url) method signature specifies than an IOException
                // could be thrown.
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    public static List<News> extractFeatureFromJson(String NewsJSON){
        if(TextUtils.isEmpty(NewsJSON))
        {
            return null;
        }

        // Create an empty ArrayList that we can start adding News to
        List<News> news=new ArrayList<>();
        // Try to parse the SAMPLE_JSON_RESPONSE. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {
            JSONObject response=new JSONObject(NewsJSON);
            JSONObject basejsonresponse=response.getJSONObject("response");
            JSONArray NewsArray=basejsonresponse.getJSONArray("results");
            for(int i=0;i<NewsArray.length();i++)
            {
                JSONObject obj=NewsArray.getJSONObject(i);
                String webTitle=obj.getString("webTitle");
                String webUrl=obj.getString("webUrl");
                String webPublicationDate=obj.getString("webPublicationDate");
                String section=obj.getString("sectionName");

                // Extract the JSONArray associated with the key called "tags",
                String author ="";
                if (obj.has("tags")) {
                    JSONArray authorArray = obj.getJSONArray("tags");
                    if (authorArray != null && authorArray.length() >= 0) {
                        for (int j = 0; j < authorArray.length(); j++) {
                            JSONObject object = authorArray.getJSONObject(j);
                            author = object.getString("webTitle");
                        }
                    }
                }
                else author = "Author NA";

                News technology=new News(webTitle,webUrl,webPublicationDate,section,author);
                news.add(technology);
            }
        } catch (JSONException e)
        {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e("QueryUtils", "Problem parsing the News JSON results", e);
        }

        // Return list of news
        return news;
    }


}
