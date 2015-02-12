package ca.michael_cunningham.currencyconverter.popups;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import ca.michael_cunningham.currencyconverter.R;

/**
 * PopupChangeSettings
 * ------------------------
 *
 * Popup Activity with options to change settings
 *
 * @author  Michael Cunningham (www.michael-cunningham.ca)
 * @since   December 1st, 2014
 * @version v1.0
 */
public class PopupChangeSettings extends Activity {

    // ------------------------------------------------------------------- private global variables
    private Intent objIntent;
    private Switch swtRefreshOnlyOnWiFi;

    // ------------------------------------------------------------------- override methods
    /**
     * Constructs views, layout, objects and initializes variables and checks saves instance state
     * manually.
     *
     * @param siState - the saved instance state bundle from onSaveInstanceState()
     */
    @Override
    public void onCreate(Bundle siState) {

        super.onCreate(siState);
        setContentView(R.layout.popup_change_settings);

        // initialize global variables and objects
        objIntent            = getIntent();
        swtRefreshOnlyOnWiFi = (Switch) findViewById(R.id.swtRefreshOnlyOnWiFi);

        // initialize local variables and objects
        Button btnSaveClose = (Button) findViewById(R.id.btnSaveClose);
        Button btnCancel    = (Button) findViewById(R.id.btnCancel);

        // is the switch active/inactive according to the shared preferences?
        swtRefreshOnlyOnWiFi.setChecked(objIntent.getBooleanExtra("refreshOnlyOnWiFi", true));

        // wire up event handlers
        btnSaveClose.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onClickBtnSaveClose();
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onClickBtnCancel();
            }
        });

    }

    // ------------------------------------------------------------------- protected event handlers
    /**
     * Event handler called when user clicks btnSaveClose
     */
    protected void onClickBtnSaveClose() {

        objIntent.putExtra("swtRefreshOnlyOnWiFi", swtRefreshOnlyOnWiFi.isChecked());
        setResult(RESULT_OK, objIntent);
        finish();

    }

    /**
     * Event handler called when user clicks btnCancel
     */
    protected void onClickBtnCancel() {

        setResult(RESULT_CANCELED, objIntent);
        finish();

    }
}