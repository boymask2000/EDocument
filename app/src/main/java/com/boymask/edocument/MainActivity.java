package com.boymask.edocument;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.litetech.libs.restservicelib.RestService;


import java.io.IOException;

public class MainActivity extends AppCompatActivity implements NfcAdapter.ReaderCallback,RestService.CallBack {
    private NfcAdapter mAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter[] intentFiltersArray;
    private String[][] techListsArray;
    private TextView mText;

    private TextView cognome;
    private TextView nome;
    private TextView codfiscale;
    private TextView datanascita;
    private TextView sesso;
    private TextView validafrom;
    private TextView validato;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cognome = (TextView) findViewById(R.id.cognome);
        nome = (TextView) findViewById(R.id.nome);
        codfiscale = (TextView) findViewById(R.id.codfiscale);
        datanascita = (TextView) findViewById(R.id.datanascita);
        sesso = (TextView) findViewById(R.id.sesso);
        validafrom = (TextView) findViewById(R.id.validafrom);
        validato = (TextView) findViewById(R.id.validato);
        Button ok = (Button) findViewById(R.id.ok);
        ok.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i("CMD", "Pressed");
                sendToServer();
            }
        });


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
        intentFiltersArray = new IntentFilter[]{ndef};
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
        /*
        for (int i = 0; i < codes.length; i++) {
            Tag tag = getIntent().getParcelableExtra(codes[i]);
            System.out.println(codes[i]);
            if (tag != null) {
                publish("found " + codes[i]);
                publish(tag.toString());
                sendCommands(tag);
            }
        }
        */

    }


/*
    public void onPause() {
        super.onPause();
        mAdapter.disableForegroundDispatch(this);
    }

    public void onResume() {
        super.onResume();
        mAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListsArray);

    }
    */

    @Override
    public void onPause() {
        super.onPause();
        NfcAdapter.getDefaultAdapter(this).disableReaderMode(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        Bundle options = new Bundle();
        options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 500);

        mAdapter.enableReaderMode(this, this,

                NfcAdapter.FLAG_READER_NFC_BARCODE |
                        NfcAdapter.FLAG_READER_NFC_A |
                        NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, options);
    }

    public void onNewIntent(Intent intent) {
        System.out.println("Foreground dispatch Discovered tag with intent: " + intent);
        //   mText.setText("Discovered tag " + ++mCount + " with intent: " + intent);

        super.onNewIntent(intent);
        Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        //do something with tagFromIntent
        if (tagFromIntent != null) {
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>found " + intent);

            System.out.println("QQ:" + tagFromIntent.toString());
            //tagFromIntent.
        }
    }

    private void sendCommands(Tag tag) {
        IsoDep isoDep = IsoDep.get(tag);
        //   IsoDep isoDep = IsoDep.get((Tag) intent.getParcelableExtra("android.nfc.extra.TAG"));
        try {
            isoDep.connect();

            byte[] SELECT = {
                    (byte) 0x00, // CLA = 00 (first interindustry command set)
                    (byte) 0xA4, // INS = A4 (SELECT)
                    (byte) 0x04, // P1  = 04 (select file by DF name)
                    (byte) 0x0C, // P2  = 0C (first or only file; no FCI)
                    //(byte) 0x06, // Lc  = 6  (data/AID has 6 bytes)
                    //(byte) 0x31, (byte) 0x35, (byte) 0x38, (byte) 0x34, (byte) 0x35, (byte) 0x46 // AID = 15845F

                    (byte) 0x02,
                    (byte) 0x6E, (byte) 0x00

            };
            byte[] SELECT1 = new byte[]
                    {
                            (byte) 0x90, (byte) 0x5A, (byte) 0x00, (byte) 0x00, 3,  // SELECT
                            (byte) 0x5F, (byte) 0x84, (byte) 0x15, (byte) 0x00      // APPLICATION ID
                    };

            byte[] result = isoDep.transceive(SELECT);
            Log.i("SS", "SELECT: " + bin2hex(result));

            if (!(result[0] == (byte) 0x90 && result[1] == (byte) 0x00))
                throw new IOException("could not select application");

            byte[] GET_STRING = {
                    (byte) 0x00, // CLA Class
                    (byte) 0xB0, // INS Instruction
                    (byte) 0x00, // P1  Parameter 1
                    (byte) 0x00, // P2  Parameter 2
                    (byte) 0x04  // LE  maximal number of bytes expected in result
            };

            result = isoDep.transceive(GET_STRING);
            Log.i("SS", "GET_STRING: " + bin2hex(result));
        } catch (Throwable t) {
            t.printStackTrace();
        }
        if (isoDep != null) {
            try {
                isoDep.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static String bin2hex(byte[] bin) {
        String hexdigits = "0123456789ABCDEF";
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < bin.length; i++) {
            int value = bin[i] < 0 ? bin[i] + 256 : bin[i]; // signed -> unsigned
            buf.append(hexdigits.charAt(value / 16));
            buf.append(hexdigits.charAt(value % 16));
        }
        return buf.toString();
    }


    @Override
    public void onTagDiscovered(Tag tag) {
        final StringBuilder sb = new StringBuilder();


        handleIsoDep(tag);
        //     handleNfca( tag);
    }


    private void handleNfca(Tag tag) {
        System.out.println("****************************** handleNfca: ");
        NfcA nfcA = NfcA.get(tag);
        final StringBuilder sb = new StringBuilder();
        if (nfcA == null) return;
        try {
            nfcA.connect();
            nfcA.setTimeout(5000);
            byte[] ats = nfcA.transceive(new byte[]{(byte) 0xE0, (byte) 0xF0});
            String s = Protocol.parseProtocolParameters(sb, tag.getId(), nfcA.getSak(), nfcA.getAtqa(), ats);
            System.out.println("ZZ: " + s);
            nfcA.transceive(new byte[]{(byte) 0xC2});
            nfcA.close();
        } catch (IOException e) {
            //   sb.insert(0, "Test failed. IOException (did you keep the devices in range?)\n\n.");
        }
    }

    private void handleIsoDep(Tag tag) {

        System.out.println("****************************** handleIsoDep: ");
//            sendCommands(tag);

        IsoDep isoDep = IsoDep.get(tag);

        try {
            isoDep.connect();
            isoDep.setTimeout(5000);

//         execToIsodep( isoDep, "00A40400"); // SELECT OK
            execToIsodep(isoDep, "00A40000023F00"); // SELECT OK
            execToIsodep(isoDep, "00A40000021100"); // Select DF1
            execToIsodep(isoDep, "00A40000021102"); // Dati
            byte data[] = execToIsodep(isoDep, "00B0000000");  //Lettura
            String s = new String(data);
            Log.i("CMD", s);
            processOut(s);

            isoDep.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    CardData data;

    private void processOut(String s) {
        if (s == null || s.length() < 10) return;
        data = new CardData();
        String size = s.substring(0, 5);

        int i = 6;
        int field = 0;
        while (i < s.length()) {
            String lenX = s.substring(i, i + 2);
            int len = hex2Dec(lenX);

            i += 2;
            String val = s.substring(i, i + len);

            i += len;


            switch (field) {
                case 0:
                    break;
                case 1:
                    data.setDataInizioValidita(val);
                    break;
                case 2:
                    data.setDataFineValidita(val);
                    break;
                case 3:
                    data.setCognome(val);
                    break;
                case 4:
                    data.setNome(val);
                    break;
                case 5:
                    data.setDataNascita(val);
                    break;
                case 6:
                    data.setSex(val);
                    break;
                case 8:
                    data.setCodFiscale(val);
                    break;
            }
            if (field == 8) break;
            field++;
        }
        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                fillOut(data);

            }
        });


    }

    private void fillOut(CardData data) {

        cognome.setText(data.getCognome());
        nome.setText(data.getNome());
        sesso.setText(data.getSex());
        codfiscale.setText(data.getCodFiscale());
        datanascita.setText(data.getDataNascita());
        validafrom.setText(data.getDataInizioValidita());
        validato.setText(data.getDataFineValidita());
    }

    private void sendToServer() {
        String json = new Gson().toJson(data);

        String url = "http://192.168.1.128:8080/ServerTest/rest/user/register";
        RestService  restService = new RestService(MainActivity.this);
        //Executing call,Note it's Async call
        restService.execute(url,json,"post");
        //restService.execute(url);
    }

    @Override
    public void onResult(String s, String s1) {

    }



    private int hex2Dec(String hex) {

        return Integer.parseInt(hex, 16);
    }

    /*
    if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
    Tag tag = intent.getExtras().getParcelable(NfcAdapter.EXTRA_TAG);
    if (Arrays.asList(tag.getTechList()).contains("android.nfc.tech.IsoDep")) {
      SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
      String passportNumber = preferences.getString(KEY_PASSPORT_NUMBER, null);
      String expirationDate = convertDate(preferences.getString(KEY_EXPIRATION_DATE, null));
      String birthDate = convertDate(preferences.getString(KEY_BIRTH_DATE, null));
      if (passportNumber != null && !passportNumber.isEmpty()
          && expirationDate != null && !expirationDate.isEmpty()
          && birthDate != null && !birthDate.isEmpty()) {
        BACKeySpec bacKey = new BACKey(passportNumber, birthDate, expirationDate);
        new ReadTask(IsoDep.get(tag), bacKey).execute();
        mainLayout.setVisibility(View.GONE);
        loadingLayout.setVisibility(View.VISIBLE);
      } else {
        Snackbar.make(passportNumberView, R.string.error_input, Snackbar.LENGTH_SHORT).show();
      }
    }
  }
     */
    private byte[] execToIsodep(IsoDep isoDep, String c) {
        byte[] cmd = hexStringToByteArray(c);
        byte[] result = new byte[0];
        try {
            result = isoDep.transceive(cmd);

            String out = "Ok";
            if (!(result[0] == (byte) 0x90 && result[1] == (byte) 0x00))
                out = "Fail";

            Log.i("CMD", c + " --> " + "[" + result.length + "]" + bin2hex(result) + ". " + out);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;

    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len - 1; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}