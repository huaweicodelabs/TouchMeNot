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
package com.huawei.touchmenot.kotlin.main

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.opengl.GLSurfaceView
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.speech.RecognizerIntent
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.huawei.hiar.ARConfigBase
import com.huawei.hiar.AREnginesApk
import com.huawei.hiar.ARHandTrackingConfig
import com.huawei.hiar.ARSession
import com.huawei.hiar.exceptions.*
import com.huawei.hms.mlsdk.asr.MLAsrConstants
import com.huawei.hms.mlsdk.asr.MLAsrListener
import com.huawei.hms.mlsdk.asr.MLAsrRecognizer
import com.huawei.hms.mlsdk.common.MLApplication
import com.huawei.hms.site.api.SearchResultListener
import com.huawei.hms.site.api.SearchServiceFactory
import com.huawei.hms.site.api.model.*
import com.huawei.touchmenot.kotlin.hms.hand.LiveHandKeyPointAnalyseActivity
import com.huawei.touchmenot.kotlin.main.adapter.CustomAdapter
import com.huawei.touchmenot.kotlin.main.common.*
import com.huawei.touchmenot.kotlin.main.common.PermissionManager.onResume
import com.huawei.touchmenot.kotlin.main.handar.HandRenderManager
import com.huawei.touchmenot.kotlin.main.model.SpeechData
import com.huawei.touchmenot.kotlin.main.model.TmnSingleton.Companion.getInstance
import com.huawei.touchmenot.kotlin.main.model.WebURLConstants
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.ArrayList
import com.huawei.touchmenot.R

class HomeActivity : AppCompatActivity() {
    var mobileArray = arrayOf("Android", "IPhone", "WindowsMobile", "Blackberry",
            "WebOS", "Ubuntu", "Windows7", "Max OS X")
    private var list = ArrayList<SpeechData>()
    var count = 0

    // private WaveView mWaveView;
    private var mSpeechRecognizer: MLAsrRecognizer? = null
    private var mTextView: TextView? = null
    private var handRenderMangreTextView: TextView? = null
    private var recyclerView: RecyclerView? = null
    private var mAdapter: CustomAdapter? = null
    private var layoutManager: LinearLayoutManager? = null
    private var waveLineView: CustomWaveView? = null
    private var imgView: ImageView? = null
    private var userName: String? = null
    private var mArSession: ARSession? = null
    private var mSurfaceView: GLSurfaceView? = null
    private var mHandRenderManager: HandRenderManager? = null
    private var mDisplayRotationManager: DisplayRotationManager? = null
    private var message: String? = null
    private var isRemindInstall = false
    var up: Button? = null
    var down: Button? = null
    var currentPosition = 0
    var visibleItemCount = 5

    //AR Scrolling related code ends
    private var TEMP_MESSAGE = ""
    private val INIT_EIGHT = 8
    private val DEPTH_SIZE = 16
    private val STR_LAT = "?lat="
    private val STR_LONG = "&lon="
    private val STR_CNT = "&cnt=10&appid="

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        Utils.getInstance(this)
        waveLineView = findViewById(R.id.waveLineView)
        mTextView = findViewById(R.id.mTextView)
        handRenderMangreTextView = findViewById(R.id.handRendereMangerTextView)
        imgView = findViewById(R.id.imgView)
        recyclerView = findViewById(R.id.recyclerView)

        //AR Scrolling related code starts
        mSurfaceView = findViewById(R.id.handSurfaceview)
        instance = this
        mDisplayRotationManager = DisplayRotationManager(this)

        // Keep the OpenGL ES running context.
        mSurfaceView?.setPreserveEGLContextOnPause(true)

        // Set the OpenGLES version.
        mSurfaceView?.setEGLContextClientVersion(2)

        // Set the EGL configuration chooser, including for the
        // number of bits of the color buffer and the number of depth bits.
        mSurfaceView?.setEGLConfigChooser(INIT_EIGHT, INIT_EIGHT, INIT_EIGHT, INIT_EIGHT, DEPTH_SIZE, Constants.INIT_ZERO)
        mHandRenderManager = HandRenderManager(this)
        mHandRenderManager!!.setDisplayRotationManage(mDisplayRotationManager)
        mHandRenderManager!!.setTextView(handRenderMangreTextView)
        mSurfaceView?.setRenderer(mHandRenderManager)
        mSurfaceView?.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY)
        arEngineAbilityCheck()
        // AR Scrolling related code ends
        Glide.with(this)
                .load(R.drawable.mic2)
                .centerCrop()
                .placeholder(R.drawable.mic2)
                .into(imgView!!)
        val speech = findViewById<Button>(R.id.speech)
        speech.setOnClickListener {
        }
        list = getList()
        loadRecyclerView()
        userName = Utils.getValue(Constants.STR_NAME)
        if (userName != null && !userName!!.isEmpty()) {
            val speechData = SpeechData()
            speechData.response = Constants.STR_HI + userName + Constants.STR_HELP
            mAdapter!!.add(speechData, recyclerView!!)
        } else {
            userName = Constants.STR_GUEST
            val speechData = SpeechData()
            speechData.response = Constants.STR_HI + userName + Constants.STR_MY_NAME
            mAdapter!!.add(speechData, recyclerView!!)
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            Handler().postDelayed({ startASR() }, Constants.DELAY_MILLIS.toLong())
        } else {
            requestAudioPermission()
        }
    }

    private fun requestAudioPermission() {
        val permissions = arrayOf(Manifest.permission.RECORD_AUDIO)
        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE)
            return
        } else ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE)
    }

    private fun showDialogOK(message: String) {
        androidx.appcompat.app.AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton(Constants.STR_OK) { dialogInterface, i -> requestAudioPermission() }
                .setNegativeButton(Constants.STR_CANCEL) { dialogInterface, i -> quitApplication() }
                .create()
                .show()
    }

    //Handling callback
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CODE -> {
                if (grantResults.size > Constants.INIT_ZERO
                        && grantResults[Constants.INIT_ZERO] == PackageManager.PERMISSION_GRANTED) {
                    startASR()
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    showDialogOK(Constants.RECORD_AUDIO_PERMISSION)
                }
                return
            }
        }
    }

    var handler = Handler(Handler.Callback { message ->
        when (message.what) {
            HANDLE_CODE -> {
                val data = message.data.getString(HANDLE_KEY)
                mTextView!!.text = data
                val speechData: SpeechData? = null
                if (data!!.length > Constants.INIT_ZERO) {
                    operation(data.trim { it <= ' ' }.toLowerCase())
                }
                Log.d(TAG, data)
                mSpeechRecognizer!!.destroy()
                startASR()
            }
            else -> {
            }
        }
        false
    })

    private fun displayResult(str: String?) {
        val msg = Message()
        val data = Bundle()
        data.putString(HANDLE_KEY, str)
        msg.data = data
        msg.what = HANDLE_CODE
        handler.sendMessage(msg)
    }

    fun startScroll(view: View?) {
        if (count < mAdapter!!.itemCount) {
            recyclerView!!.layoutManager!!.scrollToPosition(count)
            if (count == Constants.INIT_ZERO) count = mAdapter!!.itemCount - Constants.INIT_ONE
            count--
        }
    }

    private fun setImage(value: Int) {
        if (value == Constants.INIT_ZERO) {
            Glide.with(applicationContext)
                    .load(R.drawable.mic2)
                    .centerCrop()
                    .placeholder(R.drawable.mic2)
                    .into(imgView!!)
        } else {
            Glide.with(applicationContext)
                    .load(R.drawable.mic1)
                    .centerCrop()
                    .placeholder(R.drawable.mic1)
                    .into(imgView!!)
        }
    }

    private fun getList(): ArrayList<SpeechData> {
        val temp = ArrayList<SpeechData>()
        var data: SpeechData? = null
        for (str in mobileArray) {
            data = SpeechData()
            data.request = str
            temp.add(data)
        }
        return temp
    }

    private fun loadRecyclerView() {
        recyclerView!!.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
        recyclerView!!.layoutManager = layoutManager
        recyclerView!!.itemAnimator = DefaultItemAnimator()
        val input: List<SpeechData> = ArrayList()
        mAdapter = CustomAdapter(input as ArrayList<SpeechData>)
        recyclerView!!.adapter = mAdapter
    }

    private fun callDialog() {
        Toast.makeText(this@HomeActivity, Constants.STR_EYE_BLINK_LOGIN, Toast.LENGTH_SHORT).show()
    }

    private fun startASR() {
        MLApplication.getInstance().apiKey = API_KEY
        mSpeechRecognizer = MLAsrRecognizer.createAsrRecognizer(this@HomeActivity)
        // Set the ASR result listener callback. You can obtain the ASR result or result code from the listener.
        mSpeechRecognizer?.setAsrListener(SpeechRecognitionListener())
        // Set parameters and start the audio device.
        val intentSdk = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH) // Set the language that can be recognized to English. If this parameter is not set, English is recognized by default. Example: "zh": Chinese; "en-US": English; "fr-FR": French
                .putExtra(MLAsrConstants.LANGUAGE, R.string.US_English) // Set to return the recognition result along with the speech. If you ignore the setting, this mode is used by default. Options are as follows:
                // MLAsrConstants.FEATURE_WORDFLUX: Recognizes and returns texts through onRecognizingResults.
                // MLAsrConstants.FEATURE_ALLINONE: After the recognition is complete, texts are returned through onResults.
                .putExtra(MLAsrConstants.FEATURE, MLAsrConstants.FEATURE_ALLINONE)
        // Start speech recognition.
        mSpeechRecognizer?.startRecognizing(intentSdk)
        mTextView!!.text = Constants.STR_READY_TO_SPEAK
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE) {
            val rating: String? = Utils.getValue(Constants.STR_RATING)
            val speechData = SpeechData()
            speechData.response = Constants.STR_DEAR + userName + Constants.STR_RATED_AS + rating + Constants.STR_FEED_BACK
            mAdapter!!.add(speechData, recyclerView!!)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onPause() {
        Log.d(TAG, Constants.STRING_ON_PAUSE)
        super.onPause()
        if (mArSession != null) {
            mDisplayRotationManager!!.unregisterDisplayListener()
            mSurfaceView!!.onPause()
            mArSession!!.pause()
        }
        Log.d(TAG, Constants.STRING_ON_END)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onResume() {
        super.onResume()
        ////AR scrolling related code starts
        var exception: Exception? = null
        message = null
        if (mArSession == null) {
            try {
                if (!arEngineAbilityCheck()) {
                    finish()
                    return
                }
                mArSession = ARSession(this)
                val config = ARHandTrackingConfig(mArSession)
                config.cameraLensFacing = ARConfigBase.CameraLensFacing.FRONT
                config.powerMode = ARConfigBase.PowerMode.ULTRA_POWER_SAVING
                val item = ARConfigBase.ENABLE_DEPTH.toLong()
                config.enableItem = item
                mArSession!!.configure(config)
                mHandRenderManager!!.setArSession(mArSession)
                Log.d(TAG, Constants.STR_ITEM + config.enableItem)
            } catch (capturedException: Exception) {
                exception = capturedException
                setMessageWhenError(capturedException)
            }
            if (message != null) {
                stopArSession(exception)
                return
            }
        }
        try {
            mArSession!!.resume()
        } catch (e: ARCameraNotAvailableException) {
            Toast.makeText(this, R.string.Camera_Fail, Toast.LENGTH_LONG).show()
            mArSession = null
            return
        }
        mDisplayRotationManager!!.registerDisplayListener()
        mSurfaceView!!.onResume()
        //AR scrolling related code ends
        onResume(this)
        startASR()
    }

    fun start() {
        val dm = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(dm)
    }

    fun startSpeech() {
        waveLineView!!.post { waveLineView!!.start() }
    }

    fun endSpeech() {
        waveLineView!!.clearAnimation()
    }

    fun pauseSpeech() {}
    override fun onWindowFocusChanged(isHasFocus: Boolean) {
        Log.d(TAG, Constants.STR_WINDOWS_FOCUS)
        super.onWindowFocusChanged(isHasFocus)
        if (isHasFocus) {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //AR scrolling related codes start
        if (mArSession != null) {
            mArSession!!.stop()
            mArSession = null
        }
        //AR scrolling related codes ends
        if (mSpeechRecognizer != null) {
            mSpeechRecognizer!!.destroy()
        }
    }

    protected inner class SpeechRecognitionListener : MLAsrListener {
        override fun onStartingOfSpeech() {
            Log.d(TAG, Constants.STR_STARTING_SPEECH)
            start()
            mTextView!!.setTextColor(resources.getColor(R.color.color_red_card))
            mTextView!!.text = Constants.STR_LISTENING_ALERT_MESSAGE
            startSpeech()
        }

        override fun onVoiceDataReceived(data: ByteArray, energy: Float, bundle: Bundle) {

            // Return the original PCM stream and audio power to the user.
        }

        override fun onState(i: Int, bundle: Bundle) {
            // Notify the app status change.
            Log.d(TAG, Constants.STR_ON_STATE)
        }

        override fun onRecognizingResults(partialResults: Bundle) {
            // Receive the recognized text from MLAsrRecognizer.
            Log.d(TAG, Constants.STR_RECOGNIZING_RESULTS)
        }

        override fun onResults(results: Bundle) {
            Log.d(TAG, Constants.STR_ON_RESULTS)
            endSpeech()
            setImage(Constants.INIT_ZERO)
            // Text data of ASR.
            val data = results.getString(MLAsrRecognizer.RESULTS_RECOGNIZED)
            mTextView!!.text = data
            displayResult(data)
            Log.d(TAG, data)
            endSpeech()
        }

        override fun onError(error: Int, errorMessage: String) {
            Log.d(TAG, Constants.STR_ON_ERROR)
            // Called when an error occurs in recognition.
            mTextView!!.text = error.toString() + errorMessage
            Toast.makeText(this@HomeActivity, error.toString() + errorMessage, Toast.LENGTH_SHORT).show()
            pauseSpeech()
            endSpeech()
            mSpeechRecognizer!!.destroy()
            startASR()
        }

        override fun onStartListening() {
            Log.d(TAG, Constants.STR_ON_START_LISTENING)
            // The recorder starts to receive speech.
            setImage(Constants.INIT_ONE)
            mTextView!!.text = Constants.STR_LISTENING_ALERT_MESSAGE
            startSpeech()
        }
    }

    private fun operation(data: String) {
        var speechData: SpeechData? = null
        speechData = SpeechData()
        speechData.request = data
        mAdapter!!.add(speechData, recyclerView!!)
        if (data.contains(Constants.STR_WEATHER)) {
            getWeatherReport(Constants.WEATHER_LAT, Constants.WEATHER_LONG)
        } else if (data.equals(Constants.STR_OPEN_CAMERA, ignoreCase = true)) {
            speechData = SpeechData()
            speechData.response = userName + Constants.STR_OPENING_CAMERA
            mAdapter!!.add(speechData, recyclerView!!)
            val responseHandler = Handler()
            responseHandler.postDelayed({ Utils.getInstance(this@HomeActivity)!!.openCamera() }, Constants.DELAY_MILLIS.toLong())
        } else if (data.equals(Constants.STR_OPEN_GALLERY, ignoreCase = true)) {
            speechData = SpeechData()
            speechData.response = userName + Constants.STR_OPENING_CAMERA
            mAdapter!!.add(speechData, recyclerView!!)
            val responseHandler = Handler()
            responseHandler.postDelayed({ Utils.getInstance(this@HomeActivity)!!.openGallery() }, Constants.DELAY_MILLIS.toLong())
        } else if (data.contains(Constants.STR_TIME_NOW) || data.contains(Constants.STR_WHAT_TIME)) {
            val responseHandler = Handler()
            responseHandler.postDelayed({
                val speechData = SpeechData()
                speechData.response = Constants.STR_DEAR + userName + Constants.STR_NOW_TIME + Utils.getInstance(this@HomeActivity)?.todayTime
                mAdapter!!.add(speechData, recyclerView!!)
            }, Constants.DELAY_MILLIS.toLong())
        } else if (data.contains(Constants.STR_DATE) || data.contains(Constants.STR_IS_DATE)) {
            val responseHandler = Handler()
            responseHandler.postDelayed({
                val speechData = SpeechData()
                speechData.response = Constants.STR_DEAR + userName + Constants.STR_NEXT_LINE + Utils.getInstance(this@HomeActivity)?.todayDate
                mAdapter!!.add(speechData, recyclerView!!)
            }, Constants.DELAY_MILLIS.toLong())
        } else if (data.contains(Constants.STR_MY_NAME_IS)) {
            val responseHandler = Handler()
            responseHandler.postDelayed({
                Utils.setValue(Constants.STR_NAME, data.replace(Constants.STR_MY_NAME_IS, Constants.STR_EMPTY))
                val speechData = SpeechData()
                speechData.response = Constants.STR_NAME_SAVED
                userName = Utils.getValue(Constants.STR_NAME)
                mAdapter!!.add(speechData, recyclerView!!)
            }, Constants.DELAY_MILLIS.toLong())
        } else if (data.contains(Constants.STR_MY_NAME_IS)) {
            val responseHandler = Handler()
            responseHandler.postDelayed({
                val speechData = SpeechData()
                val name: String? = Utils.getValue(Constants.STR_NAME)
                if (name != null) {
                    speechData.response = Constants.STR_YOUR_NAME_IS + name
                } else {
                    speechData.response = Constants.STR_SAY_MY_NAME
                }
                mAdapter!!.add(speechData, recyclerView!!)
            }, Constants.DELAY_MILLIS.toLong())
        } else if (data.contains(Constants.STR_FEEDBACK)) {
            speechData = SpeechData()
            speechData.response = Constants.STR_DEAR + userName + Constants.STR_REDIRECT_FEEDBACK
            mAdapter!!.add(speechData, recyclerView!!)
            val responseHandler = Handler()
            responseHandler.postDelayed({ startActivityForResult(Intent(this@HomeActivity, LiveHandKeyPointAnalyseActivity::class.java), REQUEST_CODE) }, Constants.DELAY_MILLIS.toLong())
        } else if (data.contains(Constants.STR_SEARCH)) {
            searchSitePlaces(data.replace(Constants.STR_SEARCH, "").trim { it <= ' ' })
        } else if (data.equals(Constants.STR_OPEN_SETTING, ignoreCase = true)) {
            startActivity(Intent(this@HomeActivity, SettingActivity::class.java))
        } else {
            speechData = SpeechData()
            speechData.response = Constants.STR_DEAR + userName + Constants.STR_NO_DATA_FOUND
            mAdapter!!.add(speechData, recyclerView!!)
        }
    }

    private var siteId: String? = null
    private fun searchSitePlaces(searchText: String) {
        val textSearchRequest = TextSearchRequest()
        textSearchRequest.query = searchText
        try {
            val searchService = SearchServiceFactory.create(this@HomeActivity, URLEncoder.encode(API_KEY, Constants.STR_UTF_8))
            searchService.textSearch(textSearchRequest, object : SearchResultListener<TextSearchResponse?> {
                override fun onSearchResult(textSearchResponse: TextSearchResponse?) {
                    if (textSearchResponse != null && textSearchResponse.sites != null) {
                        for (site in textSearchResponse.sites) {
                            siteId = site.siteId
                            break
                        }
                        val request = DetailSearchRequest()
                        request.siteId = siteId
                        val resultListener: SearchResultListener<DetailSearchResponse?> = object : SearchResultListener<DetailSearchResponse?> {
                            override fun onSearchResult(result: DetailSearchResponse?) {
                                var site: Site
                                if (result == null || result.site.also { site = it } == null) {
                                    return
                                }
                                site = result.site
                                val speechData = SpeechData()
                                speechData.requestType = 2
                                speechData.response = site.name
                                speechData.siteResponse = site
                                mAdapter!!.add(speechData, recyclerView!!)
                                Log.d(TAG, String.format("siteId: '%s', name: %s\r\n", site.siteId, site.name))
                            }

                            override fun onSearchError(status: SearchStatus?) {
                                TEMP_MESSAGE = Constants.STR_ERROR_MSG + status?.errorCode + " " + status?.errorMessage
                                Log.d(TAG, TEMP_MESSAGE)
                            }
                        }
                        // Call the place detail search API.
                        searchService.detailSearch(request, resultListener)
                    } else {
                        val speechData = SpeechData()
                        speechData.response = Constants.STR_NO_RESULT_FOUND
                        mAdapter!!.add(speechData, recyclerView!!)
                    }
                }

                override fun onSearchError(searchStatus: SearchStatus) {
                    val `val` = Gson().toJson(searchStatus)
                    Log.d(TAG, `val`)
                    Toast.makeText(this@HomeActivity, `val`, Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: UnsupportedEncodingException) {
            ExceptionHandling().PrintExceptionInfo(Constants.EXCEPTION_MSG, e)
        }
    }

    /**
     * Check whether HUAWEI AR Engine server (com.huawei.arengine.service) is installed on
     * * the current device. If not, redirect the user to HUAWEI AppGallery for installation.
     *
     * @return boolean
     */
    private fun arEngineAbilityCheck(): Boolean {
        val isInstallArEngineApk = AREnginesApk.isAREngineApkReady(this)
        if (!isInstallArEngineApk && isRemindInstall) {
            Toast.makeText(this, Constants.STR_AGREE_MSG, Toast.LENGTH_LONG).show()
            finish()
        }
        Log.d(TAG, Constants.STR_IS_INSTALL + isInstallArEngineApk)
        if (!isInstallArEngineApk) {
            startActivity(Intent(this, ConnectAppMarketActivity::class.java))
            isRemindInstall = true
        }
        return AREnginesApk.isAREngineApkReady(this)
    }

    private fun setMessageWhenError(catchException: Exception) {
        if (catchException is ARUnavailableServiceNotInstalledException) {
            startActivity(Intent(this, ConnectAppMarketActivity::class.java))
        } else if (catchException is ARUnavailableServiceApkTooOldException) {
            message = Constants.STR_UPDATE_APK
        } else if (catchException is ARUnavailableClientSdkTooOldException) {
            message = Constants.STR_UPDATE_APP
        } else if (catchException is ARUnSupportedConfigurationException) {
            message = Constants.STR_CONFIGURATION_NOT_SUPPORTED
        } else {
            message = Constants.EXCEPTION_MSG
        }
    }

    /**
     * Stop the ARSession and display exception information when an unrecoverable exception occurs.
     *
     * @param exception Exception occurred
     */
    private fun stopArSession(exception: Exception?) {
        Log.d(TAG, Constants.STR_STOP_AR_SESSION)
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        Log.d(TAG, Constants.STR_CREATING_SESSION_ERROR, exception)
        if (mArSession != null) {
            mArSession!!.stop()
            mArSession = null
        }
        Log.d(TAG, Constants.STR_STOP_AR_SESSION)
    }

    private fun getWeatherReport(lat: Double, lng: Double) {
        val weatherRequest: StringRequest = object : StringRequest(Method.GET, WebURLConstants.WEATHER + STR_LAT + lat + STR_LONG + lng + STR_CNT + WebURLConstants.WEATHER_API_KEY, Response.Listener { response ->
            try {
                val responseFromServer = JSONObject(response)
                val weatherArray = responseFromServer.getJSONArray(Constants.STR_WEATER_MSG)
                val weather = weatherArray.getJSONObject(Constants.INIT_ZERO)
                var mDescription = weather.getString(Constants.STR_DESCRIPTION)
                mDescription = Constants.STR_WEATHER_IS + mDescription
                val main = responseFromServer.getJSONObject(Constants.STR_MAIN)
                var mTemp = main.getString(Constants.STR_TEMP)
                mTemp = mTemp + Constants.STR_F
                val speechData = SpeechData()
                speechData.response = Constants.STR_MAIN + userName + Constants.STR_LINE + mDescription + Constants.STR_TEMPERATURE + mTemp
                mAdapter!!.add(speechData, recyclerView!!)
            } catch (e: JSONException) {
                ExceptionHandling().PrintExceptionInfo(Constants.EXCEPTION_MSG, e)
            }
        }, Response.ErrorListener { error ->
            if (error.networkResponse != null) {
                parseVolleyError(error)
            }
            if (error is ServerError) {
                Log.d(Constants.STR_ERROR_MSG, error.toString())
            } else if (error is AuthFailureError) {
                Log.d(Constants.STR_ERROR_MSG, Constants.STR_AUTHENICATION_ERROR)
            } else if (error is ParseError) {
                Log.d(Constants.STR_ERROR_MSG, Constants.STR_PARSE_ERROR)
            } else if (error is NoConnectionError) {
                Toast.makeText(this@HomeActivity, Constants.STR_SERVER_MAINTENANCE, Toast.LENGTH_LONG).show()
                Log.d(Constants.STR_ERROR_MSG, Constants.STR_NO_CONNECTION_ERROR)
            } else if (error is NetworkError) {
                Log.d(Constants.STR_ERROR_MSG, Constants.STR_NETWORK_ERROR)
            } else if (error is TimeoutError) {
                Log.d(Constants.STR_ERROR_MSG, Constants.STR_TIMEOUT_ERROR)
            } else {
            }
        }) {
            override fun getHeaders(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params[Constants.STR_CONTETNT_TYPE] = Constants.STR_APPLICATION_JSON
                return params
            }
        }
        weatherRequest.retryPolicy = DefaultRetryPolicy(Constants.RETRO_API_TIME_OUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        getInstance(applicationContext)!!.addtorequestqueue(weatherRequest)
    }

    fun parseVolleyError(error: VolleyError) {
        try {
            val responseBody = String(error.networkResponse.data, Charset.forName(Constants.STR_UTF_8))
            val data = JSONObject(responseBody)
            val message = data.getString(Constants.STR_ERROR_MSG)
            Toast.makeText(this@HomeActivity, message, Toast.LENGTH_LONG).show()
            val loginErrorBuilder = AlertDialog.Builder(this@HomeActivity)
            loginErrorBuilder.setTitle(Constants.STR_ERROR_MSG)
            loginErrorBuilder.setMessage(message)
            loginErrorBuilder.setPositiveButton(Constants.STR_OK) { dialogInterface, i -> dialogInterface.dismiss() }
            loginErrorBuilder.show()
        } catch (e: JSONException) {
            Log.d(TAG, Constants.STR_ERROR_MSG, e)
        }
    }

    fun scrollUpMethod() {
        recyclerView!!.smoothScrollToPosition(Constants.INIT_ZERO)
    }

    fun scrollDownMethod() {
        recyclerView!!.smoothScrollToPosition(list.size - Constants.INIT_ONE)
    }

    fun updateList(values: List<SpeechData?>?) {
        list.clear()
        list.addAll(values as  ArrayList<SpeechData>)
    }

    private fun quitApplication() {
        finish()
        System.exit(0)
    }

    companion object {
        val TAG = HomeActivity::class.java.simpleName
        const val API_KEY = "ASR_API_KEY"
        private const val AUDIO_PERMISSION_CODE = 1
        private const val HANDLE_KEY = "text"
        private const val HANDLE_CODE = 0

        //AR Scrolling related code starts
        var instance: HomeActivity? = null
            private set
        private const val REQUEST_ID_MULTIPLE_PERMISSIONS = 1
        private const val REQUEST_CODE = 100
        private const val PERMISSION_REQUEST_CODE = 200
    }
}