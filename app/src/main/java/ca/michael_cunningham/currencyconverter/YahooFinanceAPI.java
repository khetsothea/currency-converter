package ca.michael_cunningham.currencyconverter;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import ca.michael_cunningham.currencyconverter.interfaces.*;
import ca.michael_cunningham.currencyconverter.tools.Cache;

/**
 * YahooFinanceAPI
 * ------------------------
 *
 * A type of interface for the JSONParser inner-class used to get/set and manage the JSONParser
 *
 * @author  Michael Cunningham (www.michael-cunningham.ca)
 * @since   December 2nd, 2014
 * @version v1.0
 */
public class YahooFinanceAPI implements Parcelable {

    // ------------------------------------------------------------------- private class constants
    static final String URL_PREPEND       = "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.xchange%20where%20pair%20in%20(";
    static final String URL_APPEND        = ")&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=";
    static final String JSON_CACHE_FILE   = "cache.json";
    static final byte   TASK_TYPE_REFRESH = 0;
    static final byte   TASK_TYPE_CONVERT = 1;

    // ------------------------------------------------------------------- private global variables
    private OnTaskStarted   iListenerTaskStarted;
    private OnTaskCompleted iListenerTaskCompleted;
    private String          strRawJsonData;
    private String[]        aryJsonTags;
    private String[]        aryConvertedData;
    private String          strLastRefreshed;
    private Context         ctxContext;
    private boolean         cacheValid;

    // ------------------------------------------------------------------- constructors

    /**
     * Constructs a new YahooFinanceAPI object
     *
     * @param ctxContext - the context from the activity which is using this class
     */
    public YahooFinanceAPI(Context ctxContext) {

        this.aryJsonTags      = ctxContext.getResources().getStringArray(R.array.json_tags);
        this.aryConvertedData = new String[aryJsonTags.length];
        this.cacheValid       = false;

        if (Cache.fileExists(ctxContext, JSON_CACHE_FILE)) {
            strRawJsonData = (String) Cache.loadFile(ctxContext, JSON_CACHE_FILE);
            cacheValid = true;
        }

        this.ctxContext = ctxContext;

    }

    // ------------------------------------------------------------------- get methods

    /**
     * Get a new JSONParser object to execute
     *
     * @return - new JSONParser object
     */
    public JSONParser getJSONParser() {
        return new JSONParser();
    }

    /**
     * Get whether the cache is valid or not
     *
     * @return - true/false if the cache is valid or not
     */
    public boolean isCacheValid() {
        return cacheValid;
    }

    /**
     * Get the Rate value from the parsed JSON
     *
     * @return - the rate value
     */
    public String getRate() {
        return aryConvertedData[0];
    }

    /**
     * Get the Date value from the parsed JSON
     *
     * @return - the date value
     */
    public String getDate() {
        return aryConvertedData[1];
    }

    /**
     * Get the Time value from the parsed JSON
     *
     * @return - the time value
     */
    public String getTime() {
        return aryConvertedData[2];
    }

    /**
     * Get the Ask value from the parsed JSON
     *
     * @return - the ask value
     */
    public String getAsk() {
        return aryConvertedData[3];
    }

    /**
     * Get the Bid value from the parsed JSON
     *
     * @return - the bid value
     */
    public String getBid() {
        return aryConvertedData[4];
    }

    /**
     * Get the last refreshed value from the parsed JSON on refresh
     *
     * @return - the last refreshed value
     */
    public String getLastRefreshed() {
        return strLastRefreshed;
    }

    /**
     * Set the last refreshed value, usually used on initial application run
     *
     * @param strLastRefreshed - the saved string of the value
     */
    public void setLastRefreshed(String strLastRefreshed) {
        this.strLastRefreshed = strLastRefreshed;
    }

    // ------------------------------------------------------------------- public methods

    /**
     * Set the cache validity to false, invalidating the cache
     */
    public void invalidateCache() {
        this.cacheValid = false;
    }

    /**
     * Set the interface listeners for the JSONParser
     *
     * @param onTaskCompleted - the interface called when the task has been completed
     * @param onTaskStarted   - the interface called when the task has been started
     */
    public void setListeners(OnTaskCompleted onTaskCompleted, OnTaskStarted onTaskStarted) {
        this.iListenerTaskCompleted = onTaskCompleted;
        this.iListenerTaskStarted = onTaskStarted;
    }

    // ------------------------------------------------------------------- protected inner class
    /**
     * JSONParser
     * ------------------------
     *
     * Pulls JSON data from Yahoo's public finance API and parses the JSON data
     *
     * @author  Michael Cunningham (www.michael-cunningham.ca)
     * @since   December 2nd, 2014
     * @version v1.0
     */
    protected class JSONParser extends AsyncTask<String, Void, String[]> {

        /**
         * Run before the task has started
         */
        @Override
        protected void onPreExecute() {

            if (!cacheValid) {
                iListenerTaskStarted.onTaskStarted(TASK_TYPE_REFRESH);
            } else {
                iListenerTaskStarted.onTaskStarted(TASK_TYPE_CONVERT);
            }

        }

        /**
         * The core functionality of the task at hand
         *
         * @param parameters - URL parameters, different depending on cache validity
         * @return           - either null or a string array, depending on cache validity
         */
        @Override
        protected String[] doInBackground(String... parameters) {

            if (!cacheValid) {

                try {

                    /* get all possibilities that the user could select out of the two spinners */
                    /* Yahoo's Finance API does not support more than 2 possible combinations of currencies */
                    int    parametersLength = parameters.length;
                    String strParameter     = "";

                    for (int i = 0; i < parametersLength; i++) {
                        for (int x = 0; x < parametersLength; x++) {

                            strParameter += "%22" + parameters[i] + parameters[x];

                            if ((i == (parametersLength - 1)) && (x == (parametersLength - 1))) {
                                strParameter += "%22";
                            } else {
                                strParameter += "%22%2C%20";
                            }

                        }
                    }

                    // initialize http objects
                    HttpClient objHttpClient = new DefaultHttpClient();
                    HttpPost   objHttpPost   = new HttpPost(URL_PREPEND + strParameter + URL_APPEND);

                    // fetch the data and save to global variable
                    HttpResponse objHttpResponse = objHttpClient.execute(objHttpPost);
                                 strRawJsonData  = EntityUtils.toString(objHttpResponse.getEntity(), "UTF-8");

                    try {

                        // save the created date :)
                        JSONObject allJsonData      = new JSONObject(strRawJsonData);
                        JSONObject childQuery       = allJsonData.getJSONObject("query");
                                   strLastRefreshed = childQuery.getString("created");

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Cache.saveFile(strRawJsonData, ctxContext, JSON_CACHE_FILE);

                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {

                String[] aryReturn = new String[aryJsonTags.length];

                try {

                    /* after executing this task with a valid cache, we can simply feed in two currency strings */
                    String strParameter = "";

                    for (String parameter : parameters) {
                        strParameter += parameter;
                    }

                    // we need to traverse through the JSON data structure to get to the data :)
                    JSONObject allJsonData  = new JSONObject(strRawJsonData);
                    JSONObject childQuery   = allJsonData.getJSONObject("query");
                    JSONObject childResults = childQuery.getJSONObject("results");
                    JSONArray  childRate    = childResults.getJSONArray("rate");

                    for (int i = 0; i < childRate.length(); i++) {

                        JSONObject row = childRate.getJSONObject(i);
                        String     id  = row.getString("id");

                        if (id.contentEquals(strParameter)) {

                            // stuff all the values from the json data into the return array
                            for (int x = 0; x < aryJsonTags.length; x++) {
                                aryReturn[x] = row.getString(aryJsonTags[x]);
                            }

                        }
                    }

                    return aryReturn;

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        /**
         * Run after the core task at hand
         * @param aryReturn - the object returned from doInBackground()
         */
        @Override
        protected void onPostExecute(String[] aryReturn) {

            aryConvertedData = aryReturn;

            if (!cacheValid) {

                cacheValid = true;
                iListenerTaskCompleted.onTaskCompleted(TASK_TYPE_REFRESH);

            } else {
                iListenerTaskCompleted.onTaskCompleted(TASK_TYPE_CONVERT);
            }
        }
    }

    // ------------------------------------------------------------------- parcelable methods
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel d, int flags) {}
}