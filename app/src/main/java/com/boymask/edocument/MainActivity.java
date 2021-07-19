package com.boymask.edocument;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcF;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private NfcAdapter mAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter[] intentFiltersArray;
    private String[][] techListsArray;
    private TextView mText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mText = (TextView) findViewById(R.id.text1);
        mText.setText("Scan a tag");


        mAdapter = NfcAdapter.getDefaultAdapter(this);

        pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndef.addDataType("*/*");    /* Handles all MIME based dispatches.
                                       You should specify only the ones that you need. */
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }
        intentFiltersArray = new IntentFilter[]{ndef,};
        check();
        techListsArray = new String[][]{new String[]{NfcF.class.getName()}};


        Tag tag = getIntent().getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) {
            return;
        }


    }

    private void check() {
        String codes[] = {
                NfcAdapter.EXTRA_TAG, //
                NfcAdapter.EXTRA_ADAPTER_STATE, //
                NfcAdapter.EXTRA_DATA, //
                NfcAdapter.EXTRA_AID, //
                NfcAdapter.EXTRA_ID, //
                NfcAdapter.EXTRA_NDEF_MESSAGES, //
                NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, //
                NfcAdapter.EXTRA_PREFERRED_PAYMENT_CHANGED_REASON, //
                NfcAdapter.EXTRA_SECURE_ELEMENT_NAME //

        };
        for (int i = 0; i < codes.length; i++) {
            Tag tag = getIntent().getParcelableExtra(codes[i]);
            System.out.println(codes[i]);
            if (tag != null) {
                System.out.println("found " + codes[i]);
            }
        }
    }

    public void onPause() {
        super.onPause();
        mAdapter.disableForegroundDispatch(this);
    }

    public void onResume() {
        super.onResume();
        mAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListsArray);

    }

    public void onNewIntent(Intent intent) {
        System.out.println("Foreground dispatch Discovered tag with intent: " + intent);
        //   mText.setText("Discovered tag " + ++mCount + " with intent: " + intent);

        super.onNewIntent(intent);
        Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        //do something with tagFromIntent
        if (tagFromIntent != null) {
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>found " + intent);
        }
    }

}