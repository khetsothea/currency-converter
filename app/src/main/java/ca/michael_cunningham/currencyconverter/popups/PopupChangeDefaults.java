package ca.michael_cunningham.currencyconverter.popups;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import ca.michael_cunningham.currencyconverter.R;

/**
 * PopupChangeDefaults
 * ------------------------
 *
 * Popup Activity with options to change default settings
 *
 * @author  Michael Cunningham (www.michael-cunningham.ca)
 * @since   December 1st, 2014
 * @version v1.0
 */
public class PopupChangeDefaults extends Activity {

    // ------------------------------------------------------------------- private global variables
    private Intent  objIntent;
    private Spinner spnCurrencyFrom;
    private Spinner spnCurrencyTo;

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
        setContentView(R.layout.popup_change_defaults);

        // initialize global variables and objects
        objIntent       = getIntent();
        spnCurrencyFrom = (Spinner) findViewById(R.id.spnCurrencyFrom);
        spnCurrencyTo   = (Spinner) findViewById(R.id.spnCurrencyTo);

        // initialize local variables and objects
        String[] aryCurrencies = getResources().getStringArray(R.array.currency_codes);
        Button   btnSaveClose  = (Button)  findViewById(R.id.btnSaveClose);
        Button   btnCancel     = (Button)  findViewById(R.id.btnCancel);

        // construct array adapter for spinners
        ArrayAdapter aryAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, aryCurrencies);
        aryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // populate the spinners
        spnCurrencyTo.setAdapter(aryAdapter);
        spnCurrencyFrom.setAdapter(aryAdapter);

        // set the spinners to their default places
        spnCurrencyTo.setSelection(objIntent.getIntExtra("defaultCurrencyToIndex", 0));
        spnCurrencyFrom.setSelection(objIntent.getIntExtra("defaultCurrencyFromIndex", 0));

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
     * Event handler called when the user clicks btnClose
     */
    protected void onClickBtnSaveClose() {

        // store the positions of the selections in the intent
        objIntent.putExtra("spnCurrencyFromValue", spnCurrencyFrom.getSelectedItemPosition());
        objIntent.putExtra("spnCurrencyToValue", spnCurrencyTo.getSelectedItemPosition());

        setResult(RESULT_OK, objIntent);
        finish();

    }

    /**
     * Event handler called when the user clicks btnCancel
     */
    protected void onClickBtnCancel() {

        setResult(RESULT_CANCELED, objIntent);
        finish();

    }
}