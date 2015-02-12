package ca.michael_cunningham.currencyconverter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import ca.michael_cunningham.currencyconverter.interfaces.*;
import ca.michael_cunningham.currencyconverter.tools.Font;

/**
 * MainActivity
 * ------------------------
 *
 * Main Activity Class for the Currency Converter Application
 *
 * @author  Michael Cunningham (www.michael-cunningham.ca)
 * @since   December 2nd, 2014
 * @version v1.0
 */
public class MainActivity extends Activity implements OnTaskStarted, OnTaskCompleted {

    // ------------------------------------------------------------------- global class constants
    static final int POPUP_CHANGE_DEFAULTS_SEND_CODE = 0;
    static final int POPUP_CHANGE_SETTINGS_SEND_CODE = 1;

    // ------------------------------------------------------------------- private global variables
    private YahooFinanceAPI     apiInterface;
    private ConnectivityManager objCManager;
    private SharedPreferences   shpPreferences;
    private String[]            aryCurrencies;
    private TextView            lblRate;
    private TextView            lblLastRefreshedValue;
    private TextView            lblAskValue;
    private TextView            lblBidValue;
    private TextView            lblDateValue;
    private TextView            lblTimeValue;
    private Spinner             spnCurrencyTo;
    private Spinner             spnCurrencyFrom;
    private Button              btnConvert;
    private Menu                mnuOptions;
    private boolean             isMobileDataAllowed;
    private Toast               tstToast;

    // ------------------------------------------------------------------- override methods

    /**
     * Constructs views, layout, objects and initializes variables and checks saves instance state
     * manually.
     *
     * @param siState - the saved instance state bundle from onSaveInstanceState()
     */
    @Override
    protected void onCreate(Bundle siState) {

        super.onCreate(siState);
        setContentView(R.layout.main);

        // initialize global objects
        objCManager    = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        shpPreferences = getSharedPreferences("StoredData", MODE_PRIVATE);
        aryCurrencies  = getResources().getStringArray(R.array.currency_codes);

        // initialize global views
        lblRate               = (TextView) findViewById(R.id.lblRate);
        lblLastRefreshedValue = (TextView) findViewById(R.id.lblLastRefreshedValue);
        lblAskValue           = (TextView) findViewById(R.id.lblAskValue);
        lblBidValue           = (TextView) findViewById(R.id.lblBidValue);
        lblDateValue          = (TextView) findViewById(R.id.lblDateValue);
        lblTimeValue          = (TextView) findViewById(R.id.lblTimeValue);
        spnCurrencyTo         = (Spinner)  findViewById(R.id.spnCurrencyTo);
        spnCurrencyFrom       = (Spinner)  findViewById(R.id.spnCurrencyFrom);
        btnConvert            = (Button)   findViewById(R.id.btnConvert);

        // initialize local views
        TextView lblLastRefreshed = (TextView) findViewById(R.id.lblLastRefreshed);

        lblLastRefreshed.setTypeface(Font.getTypeFace(this, "fonts/Roboto-Light.ttf"));
        lblLastRefreshedValue.setTypeface(Font.getTypeFace(this, "fonts/Roboto-Light.ttf"));
        lblRate.setTypeface(Font.getTypeFace(this, "fonts/Roboto-ThinItalic.ttf"));
        lblAskValue.setTypeface(Font.getTypeFace(this, "fonts/Roboto-ThinItalic.ttf"));
        lblBidValue.setTypeface(Font.getTypeFace(this, "fonts/Roboto-ThinItalic.ttf"));
        lblDateValue.setTypeface(Font.getTypeFace(this, "fonts/Roboto-ThinItalic.ttf"));
        lblTimeValue.setTypeface(Font.getTypeFace(this, "fonts/Roboto-ThinItalic.ttf"));

        // is mobile data allowed?
        isMobileDataAllowed = shpPreferences.getBoolean("swtRefreshOnlyOnWiFi", true);

        // set up array adapter for spinners
        ArrayAdapter aryAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, aryCurrencies);
        aryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // wire up event handlers and set array adapters
        btnConvert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickBtnConvert();
            }
        });
        spnCurrencyFrom.setAdapter(aryAdapter);
        spnCurrencyTo.setAdapter(aryAdapter);

        // fetch the default currency from and to options from shared preferences
        spnCurrencyFrom.setSelection(shpPreferences.getInt("defaultCurrencyFromIndex", 0));
        spnCurrencyTo.setSelection(shpPreferences.getInt("defaultCurrencyToIndex", 0));

        // initialize listener interfaces
        OnTaskCompleted onTaskCompleted = this;
        OnTaskStarted   onTaskStarted   = this;

        // pull out the converter object from the saved instance state if it's there
        if (siState != null) {

            apiInterface = siState.getParcelable("apiInterface");
            apiInterface.setListeners(onTaskCompleted, onTaskStarted);
            btnConvert.setEnabled(siState.getBoolean("btnConvertState"));

            lblRate.setText(siState.getString("lblRateValue"));
            lblDateValue.setText(siState.getString("lblDateValue"));
            lblTimeValue.setText(siState.getString("lblTimeValue"));
            lblAskValue.setText(siState.getString("lblAskValue"));
            lblBidValue.setText(siState.getString("lblBidValue"));
            lblLastRefreshedValue.setText(siState.getString("lblLastRefreshedValue"));

            if (apiInterface.isCacheValid()) {

                setViewsToLatestInfo();

            }/* else {

                lblRate.setText(siState.getString("lblRateValue"));
                lblDateValue.setText(siState.getString("lblDateValue"));
                lblTimeValue.setText(siState.getString("lblTimeValue"));
                lblAskValue.setText(siState.getString("lblAskValue"));
                lblBidValue.setText(siState.getString("lblBidValue"));
                lblLastRefreshedValue.setText(siState.getString("lblLastRefreshedValue"));

            }*/

        } else {

            apiInterface = new YahooFinanceAPI(this);
            apiInterface.setListeners(onTaskCompleted, onTaskStarted);
            apiInterface.setLastRefreshed(shpPreferences.getString("lblLastRefreshed", getResources().getString(R.string.lblLastRefreshedValue)));

            if (apiInterface.isCacheValid()) {
                onClickBtnConvert();
            } else {
                onClickMnuRefresh();
            }

        }
    }

    /**
     * Just before Activity death, such as screen orientation change, put items into
     * a saved instance state bundle object which will be preserved and loaded back either on
     * onRestoreInstanceState or manually through the onCreate method.
     *
     * @param siState - the saved instance state bundle
     */
    @Override
    protected void onSaveInstanceState(Bundle siState) {

        super.onSaveInstanceState(siState);
        siState.putParcelable("apiInterface", apiInterface);
        siState.putString("lblRateValue", lblRate.getText().toString());
        siState.putString("lblDateValue", lblDateValue.getText().toString());
        siState.putString("lblTimeValue", lblTimeValue.getText().toString());
        siState.putString("lblAskValue", lblAskValue.getText().toString());
        siState.putString("lblBidValue", lblBidValue.getText().toString());
        siState.putString("lblLastRefreshedValue", lblLastRefreshedValue.getText().toString());
        siState.putBoolean("btnConvertState", btnConvert.isEnabled());

    }

    /**
     * Creates the menu items and actionbar items
     *
     * @param menu - the menu object
     * @return     - true/false call to superclass
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        this.mnuOptions = menu;

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * When the user selects a menu item on the action bar
     *
     * @param item - the menu selected
     * @return - true/false, call to superclass
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {

            case (R.id.action_change_defaults) :
                displayPopup(id);
            break;

            case (R.id.action_change_settings) :
                displayPopup(id);
            break;

            case (R.id.action_refresh_data) :
                onClickMnuRefresh();
            break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Method run when a popup activity is closed/finished
     *
     * @param returnCode - which popup was closed
     * @param resultCode - if the popup was closed or cancelled
     * @param i          - the intent passed back from the popup activity
     */
    @Override
    public void onActivityResult(int returnCode, int resultCode, Intent i) {

        if (resultCode == RESULT_OK) {

            // construct editor to edit SharedPreferences object content
            SharedPreferences.Editor editor = shpPreferences.edit();

            switch (returnCode) {

                // the user saved and closed the defaults popup
                case POPUP_CHANGE_DEFAULTS_SEND_CODE :

                    editor.putInt("defaultCurrencyFromIndex", i.getIntExtra("spnCurrencyFromValue", 0));
                    editor.putInt("defaultCurrencyToIndex", i.getIntExtra("spnCurrencyToValue", 0));

                    spnCurrencyFrom.setSelection(i.getIntExtra("spnCurrencyFromValue", 0), true);
                    spnCurrencyTo.setSelection(i.getIntExtra("spnCurrencyToValue", 0), true);

                    if (apiInterface.isCacheValid()) {
                        onClickBtnConvert();
                    } else {
                        onClickMnuRefresh();
                    }

                break;

                // the user saved and closed the settings popup
                case POPUP_CHANGE_SETTINGS_SEND_CODE :

                    isMobileDataAllowed = i.getBooleanExtra("swtRefreshOnlyOnWiFi", true);
                    editor.putBoolean("swtRefreshOnlyOnWiFi", isMobileDataAllowed);

                break;
            }

            editor.apply();
        }
    }

    /**
     * Block of code which is run when the task has been marked as started in the JSON parser
     *
     * @param type - the type of task which has been started
     */
    @Override
    public void onTaskStarted(byte type) {

        if (type == YahooFinanceAPI.TASK_TYPE_REFRESH) {

            setRefreshActionButtonState(true);
            lblLastRefreshedValue.setText(getResources().getString(R.string.sharedLoading));

        }

        setViewsToLoading();
    }

    /**
     * Block of code which is run when the task has been marked as completed in the JSON parser
     *
     * @param type - the type of task which has been started
     */
    @Override
    public void onTaskCompleted(byte type) {

        switch (type) {
            case (YahooFinanceAPI.TASK_TYPE_REFRESH) :

                // construct editor to edit SharedPreferences object content
                SharedPreferences.Editor editor = shpPreferences.edit();

                setRefreshActionButtonState(false);
                onClickBtnConvert();

                editor.putString("lblLastRefreshed", apiInterface.getLastRefreshed());
                editor.apply();

            break;

            case (YahooFinanceAPI.TASK_TYPE_CONVERT) :
                setViewsToLatestInfo();
            break;
        }
    }

    // ------------------------------------------------------------------- protected event handlers
    /**
     * Event handler called when the user clicks btnConvert
     */
    protected void onClickBtnConvert() {

        //objJSONParser = apiInterface.getJSONParser();

        apiInterface.getJSONParser().execute(
                aryCurrencies[spnCurrencyFrom.getSelectedItemPosition()],
                aryCurrencies[spnCurrencyTo.getSelectedItemPosition()]
        );

    }

    /**
     * Event handler called when the user clicks the refresh button in the action bar
     */
    protected void onClickMnuRefresh() {

        if (isAllowedInternetAccess()) {
            apiInterface.invalidateCache();

            //objJSONParser = ;
            apiInterface.getJSONParser().execute(aryCurrencies);
        }

    }

    // ------------------------------------------------------------------- private methods
    /**
     * Displays a popup activity depending on the id number
     *
     * @param id - the id number corresponding to the menu item the user clicked
     */
    private void displayPopup(int id) {

        Intent intent       = new Intent();
        String intentAction = getPackageName();
        int    activityCode = -1;

        switch (id) {
            case (R.id.action_change_defaults) :

                intentAction += ".POPUP_CHANGE_DEFAULTS";
                activityCode  = POPUP_CHANGE_DEFAULTS_SEND_CODE;

                intent.putExtra("defaultCurrencyFromIndex", shpPreferences.getInt("defaultCurrencyFromIndex", 0));
                intent.putExtra("defaultCurrencyToIndex", shpPreferences.getInt("defaultCurrencyToIndex", 0));

            break;

            case (R.id.action_change_settings) :

                intentAction += ".POPUP_CHANGE_SETTINGS";
                activityCode  = POPUP_CHANGE_SETTINGS_SEND_CODE;

                intent.putExtra("refreshOnlyOnWiFi", shpPreferences.getBoolean("swtRefreshOnlyOnWiFi", true));

            break;
        }

        intent.setAction(intentAction);
        startActivityForResult(intent, activityCode);
    }

    /**
     * Sets the refresh icon to a loading state, or back to the original
     *
     * @param refreshing - true/false if the icon should be loading
     */
    private void setRefreshActionButtonState(boolean refreshing) {

        if (mnuOptions != null) {

            MenuItem refreshItem = mnuOptions.findItem(R.id.action_refresh_data);

            if (refreshItem != null) {

                if (refreshing) {
                    Log.d("michael", "MainActivity: setRefreshActionButtonState to loading");
                    refreshItem.setActionView(R.layout.actionbar_indeterminate_progress);
                } else {
                    refreshItem.setActionView(null);
                    Log.d("michael", "MainActivity: setRefreshActionButtonState to original");
                }

            }

        }
    }

    /**
     * If the application is allowed to connect to the internet, depending on the settings
     *
     * @return - true/false if the application is allowed to attempt a connection
     */
    private boolean isAllowedInternetAccess() {

        NetworkInfo objNetworkMobile = objCManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo objNetworkWiFi   = objCManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (isMobileDataAllowed) {
            if (objNetworkMobile.isConnected() || objNetworkWiFi.isConnected()) {
                return true;
            } else {
                showAToast(getResources().getString(R.string.notOnWiFiOrMobile));
            }

        } else {

            if (objNetworkWiFi.isConnected()) {
                return true;
            } else {
                showAToast(getResources().getString(R.string.notOnWiFi));
            }
        }

        return false;

    }

    /**
     * Sets the views on the main activity to a loading state
     */
    private void setViewsToLoading() {

        btnConvert.setEnabled(false);
        lblRate.setText(R.string.sharedLoading);
        lblDateValue.setText(R.string.sharedLoading);
        lblTimeValue.setText(R.string.sharedLoading);
        lblAskValue.setText(R.string.sharedLoading);
        lblBidValue.setText(R.string.sharedLoading);

    }

    /**
     * Sets the views to get the latest info from the API class
     */
    private void setViewsToLatestInfo() {

        btnConvert.setEnabled(true);
        lblRate.setText(apiInterface.getRate());
        lblDateValue.setText(apiInterface.getDate());
        lblTimeValue.setText(apiInterface.getTime());
        lblAskValue.setText(apiInterface.getAsk());
        lblBidValue.setText(apiInterface.getBid());
        lblLastRefreshedValue.setText(apiInterface.getLastRefreshed());

    }

    /**
     * Displays a toast to the user, but will not display a toast if one is currently being displayed
     *
     * @param msg - the content of the message shown
     */
    private void showAToast(String msg) {

        // if the isShown method does not return true, it throws an exception
        try {

            // just set the text if it's being shown, don't restart it
            tstToast.getView().isShown();
            tstToast.setText(msg);

        } catch (Exception e) {

            // if it's invisible... make a new toast :)
            tstToast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        }

        tstToast.show();

    }
}