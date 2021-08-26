/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.huawei.touchmenot.java.main;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.huawei.hiar.ARConfigBase;
import com.huawei.hiar.AREnginesApk;
import com.huawei.hiar.ARHandTrackingConfig;
import com.huawei.hiar.ARSession;
import com.huawei.hiar.exceptions.ARCameraNotAvailableException;
import com.huawei.hiar.exceptions.ARUnSupportedConfigurationException;
import com.huawei.hiar.exceptions.ARUnavailableClientSdkTooOldException;
import com.huawei.hiar.exceptions.ARUnavailableServiceApkTooOldException;
import com.huawei.hiar.exceptions.ARUnavailableServiceNotInstalledException;
import com.huawei.hms.mlsdk.asr.MLAsrConstants;
import com.huawei.hms.mlsdk.asr.MLAsrListener;
import com.huawei.hms.mlsdk.asr.MLAsrRecognizer;
import com.huawei.hms.mlsdk.common.MLApplication;
import com.huawei.hms.site.api.SearchResultListener;
import com.huawei.hms.site.api.SearchService;
import com.huawei.hms.site.api.SearchServiceFactory;
import com.huawei.hms.site.api.model.DetailSearchRequest;
import com.huawei.hms.site.api.model.DetailSearchResponse;
import com.huawei.hms.site.api.model.SearchStatus;
import com.huawei.hms.site.api.model.Site;
import com.huawei.hms.site.api.model.TextSearchRequest;
import com.huawei.hms.site.api.model.TextSearchResponse;
import com.huawei.touchmenot.R;
import com.huawei.touchmenot.java.hms.hand.LiveHandKeyPointAnalyseActivity;
import com.huawei.touchmenot.java.main.adapter.CustomAdapter;
import com.huawei.touchmenot.java.main.common.ConnectAppMarketActivity;
import com.huawei.touchmenot.java.main.common.Constants;
import com.huawei.touchmenot.java.main.common.CustomWaveView;
import com.huawei.touchmenot.java.main.common.DisplayRotationManager;
import com.huawei.touchmenot.java.main.common.ExceptionHandling;
import com.huawei.touchmenot.java.main.common.PermissionManager;
import com.huawei.touchmenot.java.main.handar.HandRenderManager;
import com.huawei.touchmenot.java.main.model.SpeechData;
import com.huawei.touchmenot.java.main.model.TmnSingleton;
import com.huawei.touchmenot.java.main.model.WebURLConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@SuppressWarnings("ALL")
public class HomeActivity extends AppCompatActivity {
    public static final String TAG = HomeActivity.class.getSimpleName();
    public static final String API_KEY = "ASR_API_KEY";
    String[] mobileArray = {"Android", "IPhone", "WindowsMobile", "Blackberry",
            "WebOS", "Ubuntu", "Windows7", "Max OS X"};
    private ArrayList<SpeechData> list = new ArrayList<>();
    private static final int AUDIO_PERMISSION_CODE = 1;
    int count = 0;
    // private WaveView mWaveView;
    private MLAsrRecognizer mSpeechRecognizer;
    private TextView mTextView;
    private TextView handRenderMangreTextView;

    private RecyclerView recyclerView;
    private CustomAdapter mAdapter;
    private LinearLayoutManager layoutManager;

    private CustomWaveView waveLineView;
    private static final String HANDLE_KEY = "text";
    private static final int HANDLE_CODE = 0;

    private ImageView imgView;
    private String userName;

    //AR Scrolling related code starts
    private static HomeActivity instance;

    private ARSession mArSession;

    private GLSurfaceView mSurfaceView;

    private HandRenderManager mHandRenderManager;

    private DisplayRotationManager mDisplayRotationManager;

    private String message = null;

    private boolean isRemindInstall = false;

    Button up, down;
    int currentPosition = 0;
    int visibleItemCount = 5;
    //AR Scrolling related code ends

    private String TEMP_MESSAGE = "";
    private static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    private static final int REQUEST_CODE = 100;
    private int INIT_EIGHT = 8, DEPTH_SIZE = 16;

    private String STR_LAT = "?lat=", STR_LONG = "&lon=", STR_CNT = "&cnt=10&appid=";

    private static final int PERMISSION_REQUEST_CODE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_java);

        waveLineView = findViewById(R.id.waveLineView);
        mTextView = findViewById(R.id.mTextView);
        handRenderMangreTextView = findViewById(R.id.handRendereMangerTextView);
        imgView = findViewById(R.id.imgView);
        recyclerView = findViewById(R.id.recyclerView);

        //AR Scrolling related code starts
        mSurfaceView = findViewById(R.id.handSurfaceview);
        instance = this;
        mDisplayRotationManager = new DisplayRotationManager(this);

        // Keep the OpenGL ES running context.
        mSurfaceView.setPreserveEGLContextOnPause(true);

        // Set the OpenGLES version.
        mSurfaceView.setEGLContextClientVersion(2);

        // Set the EGL configuration chooser, including for the
        // number of bits of the color buffer and the number of depth bits.
        mSurfaceView.setEGLConfigChooser(INIT_EIGHT, INIT_EIGHT, INIT_EIGHT, INIT_EIGHT, DEPTH_SIZE, Constants.INIT_ZERO);
        mHandRenderManager = new HandRenderManager(this);
        mHandRenderManager.setDisplayRotationManage(mDisplayRotationManager);
        mHandRenderManager.setTextView(handRenderMangreTextView);

        mSurfaceView.setRenderer(mHandRenderManager);
        mSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        arEngineAbilityCheck();
        // AR Scrolling related code ends

        Glide.with(this)
                .load(R.drawable.mic2)
                .centerCrop()
                .placeholder(R.drawable.mic2)
                .into(imgView);

        Button speech = findViewById(R.id.speech);
        speech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
        list = getList();
        loadRecyclerView();

        userName = Utils.getInstance(this).getValue(Constants.STR_NAME);
        if (userName != null && !userName.isEmpty()) {
            SpeechData speechData = new SpeechData();
            speechData.setResponse(Constants.STR_HI + userName + Constants.STR_HELP);
            mAdapter.add(speechData, recyclerView);
        } else {
            userName = Constants.STR_GUEST;
            SpeechData speechData = new SpeechData();
            speechData.setResponse(Constants.STR_HI + userName + Constants.STR_MY_NAME);
            mAdapter.add(speechData, recyclerView);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startASR();
                }
            }, Constants.DELAY_MILLIS);
        } else {
            requestAudioPermission();
        }
    }

    private void requestAudioPermission() {
        final String[] permissions = new String[]{Manifest.permission.RECORD_AUDIO};
        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE);
            return;
        } else
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE);
    }


    private void showDialogOK(String message) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton(Constants.STR_OK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        requestAudioPermission();
                    }
                })
                .setNegativeButton(Constants.STR_CANCEL, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        quitApplication();
                    }
                })
                .create()
                .show();
    }


    //Handling callback
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE: {
                if (grantResults.length > Constants.INIT_ZERO
                        && grantResults[Constants.INIT_ZERO] == PackageManager.PERMISSION_GRANTED) {
                    startASR();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    showDialogOK(Constants.RECORD_AUDIO_PERMISSION);
                }
                return;
            }
        }
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what) {
                case HANDLE_CODE:
                    String data = message.getData().getString(HANDLE_KEY);
                    mTextView.setText(data);
                    SpeechData speechData = null;
                    if (data.length() > Constants.INIT_ZERO) {
                        operation(data.trim().toLowerCase());
                    }
                    Log.d(TAG, data);
                    mSpeechRecognizer.destroy();
                    startASR();
                    break;
                default:
                    break;
            }
            return false;
        }
    });

    private void displayResult(String str) {
        Message msg = new Message();
        Bundle data = new Bundle();
        data.putString(HANDLE_KEY, str);
        msg.setData(data);
        msg.what = HANDLE_CODE;
        handler.sendMessage(msg);
    }


    public void startScroll(View view) {
        if (count < mAdapter.getItemCount()) {
            recyclerView.getLayoutManager().scrollToPosition(count);
            if (count == Constants.INIT_ZERO)
                count = mAdapter.getItemCount() - Constants.INIT_ONE;
            count--;
        }
    }

    private void setImage(int value) {
        if (value == Constants.INIT_ZERO) {
            Glide.with(this)
                    .load(R.drawable.mic2)
                    .centerCrop()
                    .placeholder(R.drawable.mic2)
                    .into(imgView);
        } else {
            Glide.with(this)
                    .load(R.drawable.mic1)
                    .centerCrop()
                    .placeholder(R.drawable.mic1)
                    .into(imgView);
        }
    }

    private ArrayList<SpeechData> getList() {
        ArrayList<SpeechData> temp = new ArrayList<>();
        SpeechData data = null;
        for (String str : mobileArray) {
            data = new SpeechData();
            data.setRequest(str);
            temp.add(data);
        }
        return temp;
    }

    private void loadRecyclerView() {
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        List<SpeechData> input = new ArrayList<>();
        mAdapter = new CustomAdapter(input);
        recyclerView.setAdapter(mAdapter);
    }

    private void callDialog() {
        Toast.makeText(HomeActivity.this, Constants.STR_EYE_BLINK_LOGIN, Toast.LENGTH_SHORT).show();
    }

    private void startASR() {
        MLApplication.getInstance().setApiKey(API_KEY);
        mSpeechRecognizer = MLAsrRecognizer.createAsrRecognizer(HomeActivity.this);
        // Set the ASR result listener callback. You can obtain the ASR result or result code from the listener.
        mSpeechRecognizer.setAsrListener(new SpeechRecognitionListener());
        // Set parameters and start the audio device.
        Intent intentSdk = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                // Set the language that can be recognized to English. If this parameter is not set, English is recognized by default. Example: "zh": Chinese; "en-US": English; "fr-FR": French
                .putExtra(MLAsrConstants.LANGUAGE, R.string.US_English)
                // Set to return the recognition result along with the speech. If you ignore the setting, this mode is used by default. Options are as follows:
                // MLAsrConstants.FEATURE_WORDFLUX: Recognizes and returns texts through onRecognizingResults.
                // MLAsrConstants.FEATURE_ALLINONE: After the recognition is complete, texts are returned through onResults.
                .putExtra(MLAsrConstants.FEATURE, MLAsrConstants.FEATURE_ALLINONE);
        // Start speech recognition.
        mSpeechRecognizer.startRecognizing(intentSdk);
        mTextView.setText(Constants.STR_READY_TO_SPEAK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            String rating = Utils.getInstance(this).getValue(Constants.STR_RATING);
            SpeechData speechData = new SpeechData();
            speechData.setResponse(Constants.STR_DEAR + userName + Constants.STR_RATED_AS + rating + Constants.STR_FEED_BACK);
            mAdapter.add(speechData, recyclerView);
        }
    }

    @Override
    protected void onPause() {
        Log.d(TAG, Constants.STRING_ON_PAUSE);
        super.onPause();
        Log.d(TAG, Constants.STRING_ON_END);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ////AR scrolling related code starts
        Exception exception = null;
        message = null;
        if (mArSession == null) {
            try {
                if (!arEngineAbilityCheck()) {
                    finish();
                    return;
                }
                mArSession = new ARSession(this);
                ARHandTrackingConfig config = new ARHandTrackingConfig(mArSession);
                config.setCameraLensFacing(ARConfigBase.CameraLensFacing.FRONT);
                config.setPowerMode(ARConfigBase.PowerMode.ULTRA_POWER_SAVING);

                long item = ARConfigBase.ENABLE_DEPTH;
                config.setEnableItem(item);
                mArSession.configure(config);
                mHandRenderManager.setArSession(mArSession);
                Log.d(TAG, Constants.STR_ITEM + config.getEnableItem());
            } catch (Exception capturedException) {
                exception = capturedException;
                setMessageWhenError(capturedException);
            }
            if (message != null) {
                stopArSession(exception);
                return;
            }
        }
        try {
            mArSession.resume();
        } catch (ARCameraNotAvailableException e) {
            Toast.makeText(this, R.string.Camera_Fail, Toast.LENGTH_LONG).show();
            mArSession = null;
            return;
        }
        mDisplayRotationManager.registerDisplayListener();
        mSurfaceView.onResume();
        //AR scrolling related code ends
        PermissionManager.onResume(this);
        startASR();
    }

    public void start() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
    }

    public void startSpeech() {
        waveLineView.post(new Runnable() {
            @Override
            public void run() {
                waveLineView.start();
            }
        });
    }


    public void endSpeech() {
        waveLineView.clearAnimation();
    }


    public void pauseSpeech() {
    }

    @Override
    public void onWindowFocusChanged(boolean isHasFocus) {
        Log.d(TAG, Constants.STR_WINDOWS_FOCUS);
        super.onWindowFocusChanged(isHasFocus);
        if (isHasFocus) {
            getWindow().getDecorView()
                    .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //AR scrolling related codes start
        if (mArSession != null) {
            mArSession.stop();
            mArSession = null;
        }
        //AR scrolling related codes ends
        if (mSpeechRecognizer != null) {
            mSpeechRecognizer.destroy();
        }
    }

    protected class SpeechRecognitionListener implements MLAsrListener {
        @Override
        public void onStartingOfSpeech() {
            Log.d(TAG, Constants.STR_STARTING_SPEECH);
            start();
            mTextView.setTextColor(getResources().getColor(R.color.color_red_card));
            mTextView.setText(Constants.STR_LISTENING_ALERT_MESSAGE);
            startSpeech();
        }

        @Override
        public void onVoiceDataReceived(byte[] data, float energy, Bundle bundle) {
            // Return the original PCM stream and audio power to the user.

        }

        @Override
        public void onState(int i, Bundle bundle) {
            // Notify the app status change.
            Log.d(TAG, Constants.STR_ON_STATE);
        }

        @Override
        public void onRecognizingResults(Bundle partialResults) {
            // Receive the recognized text from MLAsrRecognizer.
            Log.d(TAG, Constants.STR_RECOGNIZING_RESULTS);
        }

        @Override
        public void onResults(Bundle results) {
            Log.d(TAG, Constants.STR_ON_RESULTS);
            endSpeech();
            setImage(Constants.INIT_ZERO);
            // Text data of ASR.
            String data = results.getString(MLAsrRecognizer.RESULTS_RECOGNIZED);
            mTextView.setText(data);
            displayResult(data);
            Log.d(TAG, data);
            endSpeech();
        }

        @Override
        public void onError(int error, String errorMessage) {
            Log.d(TAG, Constants.STR_ON_ERROR);
            // Called when an error occurs in recognition.
            mTextView.setText(error + errorMessage);
            Toast.makeText(HomeActivity.this, error + errorMessage, Toast.LENGTH_SHORT).show();
            pauseSpeech();
            endSpeech();
            mSpeechRecognizer.destroy();
            startASR();
        }

        @Override
        public void onStartListening() {
            Log.d(TAG, Constants.STR_ON_START_LISTENING);
            // The recorder starts to receive speech.
            setImage(Constants.INIT_ONE);
            mTextView.setText(Constants.STR_LISTENING_ALERT_MESSAGE);
            startSpeech();
        }
    }

    private void operation(String data) {
        SpeechData speechData = null;
        speechData = new SpeechData();
        speechData.setRequest(data);
        mAdapter.add(speechData, recyclerView);
        if (data.contains(Constants.STR_WEATHER)) {
            getWeatherReport(Constants.WEATHER_LAT, Constants.WEATHER_LONG);
        } else if (data.equalsIgnoreCase(Constants.STR_OPEN_CAMERA)) {
            speechData = new SpeechData();
            speechData.setResponse(userName + Constants.STR_OPENING_CAMERA);
            mAdapter.add(speechData, recyclerView);
            Handler responseHandler = new Handler();
            responseHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Utils.getInstance(HomeActivity.this).openCamera();
                }
            }, Constants.DELAY_MILLIS);
        } else if (data.equalsIgnoreCase(Constants.STR_OPEN_GALLERY)) {
            speechData = new SpeechData();
            speechData.setResponse(userName + Constants.STR_OPENING_CAMERA);
            mAdapter.add(speechData, recyclerView);
            Handler responseHandler = new Handler();
            responseHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Utils.getInstance(HomeActivity.this).openGallery();
                }
            }, Constants.DELAY_MILLIS);
        } else if (data.contains(Constants.STR_TIME_NOW) || data.contains(Constants.STR_WHAT_TIME)) {
            Handler responseHandler = new Handler();
            responseHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    SpeechData speechData = new SpeechData();
                    speechData.setResponse(Constants.STR_DEAR + userName + Constants.STR_NOW_TIME + Utils.getInstance(HomeActivity.this).getTodayTime());
                    mAdapter.add(speechData, recyclerView);
                }
            }, Constants.DELAY_MILLIS);
        } else if (data.contains(Constants.STR_DATE) || data.contains(Constants.STR_IS_DATE)) {
            Handler responseHandler = new Handler();
            responseHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    SpeechData speechData = new SpeechData();
                    speechData.setResponse(Constants.STR_DEAR + userName + Constants.STR_NEXT_LINE + Utils.getInstance(HomeActivity.this).getTodayDate());
                    mAdapter.add(speechData, recyclerView);
                }
            }, Constants.DELAY_MILLIS);
        } else if (data.contains(Constants.STR_MY_NAME_IS)) {
            Handler responseHandler = new Handler();
            responseHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Utils.getInstance(HomeActivity.this).setValue(Constants.STR_NAME, data.replace(Constants.STR_MY_NAME_IS, Constants.STR_EMPTY));
                    SpeechData speechData = new SpeechData();
                    speechData.setResponse(Constants.STR_NAME_SAVED);
                    userName = Utils.getInstance(HomeActivity.this).getValue(Constants.STR_NAME);
                    mAdapter.add(speechData, recyclerView);
                }
            }, Constants.DELAY_MILLIS);
        } else if (data.contains(Constants.STR_MY_NAME_IS)) {
            Handler responseHandler = new Handler();
            responseHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    SpeechData speechData = new SpeechData();
                    String name = Utils.getInstance(HomeActivity.this).getValue(Constants.STR_NAME);
                    if (name != null) {
                        speechData.setResponse(Constants.STR_YOUR_NAME_IS + name);
                    } else {
                        speechData.setResponse(Constants.STR_SAY_MY_NAME);
                    }
                    mAdapter.add(speechData, recyclerView);
                }
            }, Constants.DELAY_MILLIS);
        } else if (data.contains(Constants.STR_FEEDBACK)) {
            speechData = new SpeechData();
            speechData.setResponse(Constants.STR_DEAR + userName + Constants.STR_REDIRECT_FEEDBACK);
            mAdapter.add(speechData, recyclerView);
            Handler responseHandler = new Handler();
            responseHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivityForResult(new Intent(HomeActivity.this, LiveHandKeyPointAnalyseActivity.class), REQUEST_CODE);
                }
            }, Constants.DELAY_MILLIS);
        } else if (data.contains(Constants.STR_SEARCH)) {
            searchSitePlaces(data.replace(Constants.STR_SEARCH, "").trim());
        } else if (data.equalsIgnoreCase(Constants.STR_OPEN_SETTING)) {
            startActivity(new Intent(HomeActivity.this, SettingActivity.class));
        } else {
            speechData = new SpeechData();
            speechData.setResponse(Constants.STR_DEAR + userName + Constants.STR_NO_DATA_FOUND);
            mAdapter.add(speechData, recyclerView);
        }
    }

    private String siteId;

    private void searchSitePlaces(String searchText) {
        TextSearchRequest textSearchRequest = new TextSearchRequest();
        textSearchRequest.setQuery(searchText);
        try {
            SearchService searchService = SearchServiceFactory.create(HomeActivity.this, URLEncoder.encode(API_KEY, Constants.STR_UTF_8));
            searchService.textSearch(textSearchRequest, new SearchResultListener<TextSearchResponse>() {
                @Override
                public void onSearchResult(TextSearchResponse textSearchResponse) {
                    if (textSearchResponse != null && textSearchResponse.getSites() != null) {
                        for (Site site : textSearchResponse.getSites()) {
                            siteId = site.getSiteId();
                            break;
                        }

                        DetailSearchRequest request = new DetailSearchRequest();
                        request.setSiteId(siteId);
                        SearchResultListener<DetailSearchResponse> resultListener = new SearchResultListener<DetailSearchResponse>() {
                            @Override
                            public void onSearchResult(DetailSearchResponse result) {
                                Site site;
                                if (result == null || (site = result.getSite()) == null) {
                                    return;
                                }
                                site = result.getSite();
                                SpeechData speechData = new SpeechData();
                                speechData.setRequestType(2);
                                speechData.setResponse(site.getName());
                                speechData.setSiteResponse(site);
                                mAdapter.add(speechData, recyclerView);
                                Log.d(TAG, String.format("siteId: '%s', name: %s\r\n", site.getSiteId(), site.getName()));
                            }

                            @Override
                            public void onSearchError(SearchStatus status) {
                                TEMP_MESSAGE = Constants.STR_ERROR_MSG + status.getErrorCode() + " " + status.getErrorMessage();
                                Log.d(TAG, TEMP_MESSAGE);
                            }
                        };
                        // Call the place detail search API.
                        searchService.detailSearch(request, resultListener);
                    } else {
                        SpeechData speechData = new SpeechData();
                        speechData.setResponse(Constants.STR_NO_RESULT_FOUND);
                        mAdapter.add(speechData, recyclerView);
                    }
                }

                @Override
                public void onSearchError(SearchStatus searchStatus) {
                    String val = new Gson().toJson(searchStatus);
                    Log.d(TAG, val);
                    Toast.makeText(HomeActivity.this, val, Toast.LENGTH_SHORT).show();
                }
            });

        } catch (UnsupportedEncodingException e) {
            new ExceptionHandling().PrintExceptionInfo(Constants.EXCEPTION_MSG, e);
        }
    }

    /**
     * Check whether HUAWEI AR Engine server (com.huawei.arengine.service) is installed on
     * * the current device. If not, redirect the user to HUAWEI AppGallery for installation.
     *
     * @return boolean
     */
    private boolean arEngineAbilityCheck() {
        boolean isInstallArEngineApk = AREnginesApk.isAREngineApkReady(this);
        if (!isInstallArEngineApk && isRemindInstall) {
            Toast.makeText(this, Constants.STR_AGREE_MSG, Toast.LENGTH_LONG).show();
            finish();
        }
        Log.d(TAG, Constants.STR_IS_INSTALL + isInstallArEngineApk);
        if (!isInstallArEngineApk) {
            startActivity(new Intent(this, ConnectAppMarketActivity.class));
            isRemindInstall = true;
        }
        return AREnginesApk.isAREngineApkReady(this);
    }

    private void setMessageWhenError(Exception catchException) {
        if (catchException instanceof ARUnavailableServiceNotInstalledException) {
            startActivity(new Intent(this, ConnectAppMarketActivity.class));
        } else if (catchException instanceof ARUnavailableServiceApkTooOldException) {
            message = Constants.STR_UPDATE_APK;
        } else if (catchException instanceof ARUnavailableClientSdkTooOldException) {
            message = Constants.STR_UPDATE_APP;
        } else if (catchException instanceof ARUnSupportedConfigurationException) {
            message = Constants.STR_CONFIGURATION_NOT_SUPPORTED;
        } else {
            message = Constants.EXCEPTION_MSG;
        }
    }

    /**
     * Stop the ARSession and display exception information when an unrecoverable exception occurs.
     *
     * @param exception Exception occurred
     */
    private void stopArSession(Exception exception) {
        Log.d(TAG, Constants.STR_STOP_AR_SESSION);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.d(TAG, Constants.STR_CREATING_SESSION_ERROR, exception);
        if (mArSession != null) {
            mArSession.stop();
            mArSession = null;
        }
        Log.d(TAG, Constants.STR_STOP_AR_SESSION);
    }

    private void getWeatherReport(double lat, double lng) {
        StringRequest weatherRequest = new StringRequest(Request.Method.GET, WebURLConstants.WEATHER + STR_LAT + lat + STR_LONG + lng + STR_CNT + WebURLConstants.WEATHER_API_KEY, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject responseFromServer = new JSONObject(response);
                    JSONArray weatherArray = responseFromServer.getJSONArray(Constants.STR_WEATER_MSG);
                    JSONObject weather = weatherArray.getJSONObject(Constants.INIT_ZERO);
                    String mDescription = weather.getString(Constants.STR_DESCRIPTION);
                    mDescription = Constants.STR_WEATHER_IS + mDescription;
                    JSONObject main = responseFromServer.getJSONObject(Constants.STR_MAIN);
                    String mTemp = main.getString(Constants.STR_TEMP);
                    mTemp = mTemp + Constants.STR_F;
                    SpeechData speechData = new SpeechData();
                    speechData.setResponse(Constants.STR_MAIN + userName + Constants.STR_LINE + mDescription + Constants.STR_TEMPERATURE + mTemp);
                    mAdapter.add(speechData, recyclerView);
                } catch (JSONException e) {
                    new ExceptionHandling().PrintExceptionInfo(Constants.EXCEPTION_MSG, e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse != null) {
                    parseVolleyError(error);
                }
                if (error instanceof ServerError) {
                    Log.d(Constants.STR_ERROR_MSG, String.valueOf(error));
                } else if (error instanceof AuthFailureError) {
                    Log.d(Constants.STR_ERROR_MSG, Constants.STR_AUTHENICATION_ERROR);
                } else if (error instanceof ParseError) {
                    Log.d(Constants.STR_ERROR_MSG, Constants.STR_PARSE_ERROR);
                } else if (error instanceof NoConnectionError) {
                    Toast.makeText(HomeActivity.this, Constants.STR_SERVER_MAINTENANCE, Toast.LENGTH_LONG).show();
                    Log.d(Constants.STR_ERROR_MSG, Constants.STR_NO_CONNECTION_ERROR);
                } else if (error instanceof NetworkError) {
                    Log.d(Constants.STR_ERROR_MSG, Constants.STR_NETWORK_ERROR);
                } else if (error instanceof TimeoutError) {
                    Log.d(Constants.STR_ERROR_MSG, Constants.STR_TIMEOUT_ERROR);
                } else {
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put(Constants.STR_CONTETNT_TYPE, Constants.STR_APPLICATION_JSON);
                return params;
            }
        };
        weatherRequest.setRetryPolicy(new DefaultRetryPolicy(Constants.RETRO_API_TIME_OUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        TmnSingleton.getInstance(getApplicationContext()).addtorequestqueue(weatherRequest);
    }

    public void parseVolleyError(VolleyError error) {
        try {
            String responseBody = new String(error.networkResponse.data, Charset.forName(Constants.STR_UTF_8));
            JSONObject data = new JSONObject(responseBody);
            String message = data.getString(Constants.STR_ERROR_MSG);
            Toast.makeText(HomeActivity.this, message, Toast.LENGTH_LONG).show();
            android.app.AlertDialog.Builder loginErrorBuilder = new android.app.AlertDialog.Builder(HomeActivity.this);
            loginErrorBuilder.setTitle(Constants.STR_ERROR_MSG);
            loginErrorBuilder.setMessage(message);
            loginErrorBuilder.setPositiveButton(Constants.STR_OK, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            loginErrorBuilder.show();
        } catch (JSONException e) {
            Log.d(TAG, Constants.STR_ERROR_MSG, e);
        }
    }

    public static HomeActivity getInstance() {
        return instance;
    }

    public void scrollUpMethod() {
        recyclerView.smoothScrollToPosition(Constants.INIT_ZERO);
    }

    public void scrollDownMethod() {
        recyclerView.smoothScrollToPosition(list.size() - Constants.INIT_ONE);
    }

    public void updateList(List<SpeechData> values) {
        list.clear();
        list.addAll(values);
    }

    private void quitApplication() {
        HomeActivity.this.finish();
        System.exit(0);
    }

}