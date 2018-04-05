package com.example.sinchpstn;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.sinch.android.rtc.AudioController;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CallActivity extends AppCompatActivity implements View.OnClickListener
{
    private static final int REQ_CODE_SPEECH_INPUT = 100;
    private static final String[] INITIAL_PERMS = { Manifest.permission.RECORD_AUDIO};
    private static final int INITIAL_REQUEST = 1337;
    private static AudioController audioController;

    Spinner sourceLangSpinner;
    Spinner targetLangSpinner;
    String sourceLanguage;
    String targetLanguage;
    FloatingActionButton speakerButton;
    TextView callState;
    Button callButton, buttonOne, buttonTwo, buttonThree, buttonFour, buttonFive, buttonSix, buttonSeven, buttonEight, buttonNine, buttonZero;

    TextToSpeech textToSpeech;
    StrictMode.ThreadPolicy policy;
    SinchClient sinchClient;
    Call call;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(Manifest.permission.RECORD_AUDIO))
        {
            requestPermissions(INITIAL_PERMS, INITIAL_REQUEST);
        }

        sourceLangSpinner = findViewById(R.id.sourceLangSpinner);
        targetLangSpinner = findViewById(R.id.targetLangSpinner);
        init();

        sinchClient = Sinch.getSinchClientBuilder()
                .context(this)
                .userId("userA")
                .applicationKey("e813dcc6-5341-4718-97eb-e075ddb24cea")
                .applicationSecret("5y6voQiAJ0ihhPYv3ZqVkA==")
                .environmentHost("clientapi.sinch.com")
                .build();

        // To receive calls: sinchClient.addSinchClientListener()?
        sinchClient.setSupportCalling(true);
        sinchClient.start();

        callState = findViewById(R.id.callState);
        callButton = findViewById(R.id.callButton); callButton.setOnClickListener(this);
        buttonOne = findViewById(R.id.buttonOne); buttonOne.setOnClickListener(this);
        buttonTwo = findViewById(R.id.buttonTwo); buttonTwo.setOnClickListener(this);
        buttonThree = findViewById(R.id.buttonThree); buttonThree.setOnClickListener(this);
        buttonFour = findViewById(R.id.buttonFour); buttonFour.setOnClickListener(this);
        buttonFive = findViewById(R.id.buttonFive); buttonFive.setOnClickListener(this);
        buttonSix = findViewById(R.id.buttonSix); buttonSix.setOnClickListener(this);
        buttonSeven = findViewById(R.id.buttonSeven); buttonSeven.setOnClickListener(this);
        buttonEight = findViewById(R.id.buttonEight); buttonEight.setOnClickListener(this);
        buttonNine = findViewById(R.id.buttonNine); buttonNine.setOnClickListener(this);
        buttonZero = findViewById(R.id.buttonZero); buttonZero.setOnClickListener(this);
        speakerButton = findViewById(R.id.speakerButton); speakerButton.setOnClickListener(this);
    }

    private void init()
    {
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener()
        {
            @Override
            public void onInit(int i)
            {

            }
        });

        policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.languages, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sourceLangSpinner.setAdapter(adapter);
        targetLangSpinner.setAdapter(adapter);

        sourceLangSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
            {
                sourceLanguage = sourceLangSpinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView)
            {
                sourceLanguage = "en";
            }
        });

        targetLangSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
            {
                targetLanguage = targetLangSpinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView)
            {
                targetLanguage = "en";
            }
        });
    }

    @Override
    public void onClick(View v)
    {
        StringBuilder sb = new StringBuilder("+1");
        audioController = sinchClient.getAudioController();

        switch (v.getId())
        {
            case R.id.buttonOne: callState.append("1"); break;
            case R.id.buttonTwo: callState.append("2"); break;
            case R.id.buttonThree: callState.append("3"); break;
            case R.id.buttonFour: callState.append("4"); break;
            case R.id.buttonFive: callState.append("5"); break;
            case R.id.buttonSix: callState.append("6"); break;
            case R.id.buttonSeven: callState.append("7"); break;
            case R.id.buttonEight: callState.append("8"); break;
            case R.id.buttonNine: callState.append("9"); break;
            case R.id.buttonZero: callState.append("0"); break;
            case R.id.callButton:
                if (call == null)
                {
                    sb.append(callState.getText().toString());
                    callButton.setText(R.string.hangup);
                    call = sinchClient.getCallClient().callPhoneNumber(sb.toString());
                    call.addCallListener(new SinchCallListener());
                }
                else
                {
                    call.hangup();
                }
                break;
            case R.id.speakerButton:
                //audioController.mute();
                //new VoiceInputTask(this).execute();
                recorder();
                break;
        }
    }

    private class VoiceInputTask extends AsyncTask<String, Void, Void>
    {
        Context context;

        private VoiceInputTask(Context context)
        {
            this.context = context.getApplicationContext();
        }

        @Override
        protected Void doInBackground(String... strings)
        {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Recording...");

            try
            {
                context.startActivity(intent);
                //startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
            }
            catch (ActivityNotFoundException ex)
            {
                Log.d("DEBUG - startVoiceInput", Log.getStackTraceString(ex));
            }

            return null;
        }
    }

    public void recorder()
    {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Recording...");

        try
        {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        }
        catch (ActivityNotFoundException ex)
        {
            Log.d("DEBUG - startVoiceInput", Log.getStackTraceString(ex));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode)
        {
            case REQ_CODE_SPEECH_INPUT:
            {
                if (resultCode == RESULT_OK && data != null)
                {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    try
                    {
                        String translatedText = translateViaGoogleApi(result.get(0), targetLanguage);
                        audioController.unmute();
                        textToSpeech(translatedText, Locale.forLanguageTag(targetLanguage));
                    }
                    catch (Exception ex)
                    {
                        Log.d("DEBUG - onActivityRes", Log.getStackTraceString(ex));
                    }
                }

                break;
            }
        }
    }

    private String translateViaGoogleApi(String translatedText, String targetLang) throws Exception
    {
        String http = "https://translation.googleapis.com/language/translate/v2";
        String keyStr = "AIzaSyAEqLlN2J8odMo4dX2etif68rPgScXvihA";
        String text = URLEncoder.encode(translatedText, "UTF-8").replaceAll("\\+", "%20");
        String sourceLang = sourceLanguage;
        String urlStr = http + "?" + "key=" + keyStr + "=&q=" + text + "&source=" + sourceLang + "&target=" + targetLang;
        // https://translation.googleapis.com/language/translate/v2?key=AIzaSyAEqLlN2J8odMo4dX2etif68rPgScXvihA=&q=hello%20world&source=en&target=de

        URL url = new URL(urlStr);
        URLConnection connection = url.openConnection();
        InputStream in = connection.getInputStream();
        OutputStream out = copyStream(in);

        String[] tokenizedByLine = out.toString().split("\n");
        String[] tokenizedByColon = tokenizedByLine[4].split(":");

        return tokenizedByColon[1].replace("\"", "");
        // {
        //  "data":{
        //   "translations":[
        //    {
        //     "translatedText":"Hallo Test-Test"
        //    }
        //   ]
        //  }
        // }
    }

    public static OutputStream copyStream(InputStream in) throws IOException
    {
        OutputStream out = new ByteArrayOutputStream(1024);
        byte[] chunk= new byte[1024];
        int nbyte;

        while ((nbyte = in.read(chunk)) > -1)
        {
            out.write(chunk, 0, nbyte);
        }

        in.close();
        return out;
    }

    private void textToSpeech(String text, Locale loc)
    {
        textToSpeech.setLanguage(loc);
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "");
    }

    private class SinchCallListener implements CallListener
    {
        @Override
        public void onCallEnded(Call endedCall)
        {
            call = null;
            callButton.setText(R.string.call);
            setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
        }

        @Override
        public void onCallEstablished(Call establishedCall)
        {
            setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        }

        @Override
        public void onCallProgressing(Call progressingCall)
        {
            // intentionally empty
        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> pushPairs)
        {
            // intentionally empty
        }
    }
}
