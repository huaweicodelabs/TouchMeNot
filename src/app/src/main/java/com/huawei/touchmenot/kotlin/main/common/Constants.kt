package com.huawei.touchmenot.kotlin.main.common

object Constants {
    const val STR_NAME = "name"
    const val STR_HI = "Hi "
    const val STR_HELP = ", \n How can i help you?"
    const val STR_MY_NAME = ", \n       Introduce yourself \n       Say 'My name is .......'"
    const val STR_GUEST = "Guest"
    const val STR_PERMISSION_NEEDED = "Permission needed"
    const val STR_THIS_IS_NEEDED = "This permission is needed because of this and that"
    const val STR_CANCEL = "CANCEL"

    // Session.java
    const val PREF_NAME = "snow-intro-slider"
    const val IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch"
    const val STATUS_SUCCESS = true

    // CameraSourcePreview.java
    const val CAMERE_NOT_SOURCE = "Could not start camera source."
    const val STR_POTRAIT_MODE = "isPortraitMode returning false by default"

    //HomeActivity.java
    const val STR_CAMERA_PERMISSION = "This application needs camera permission."
    const val STR_RATING = "Rating"
    const val STR_READY_TO_SPEAK = "Ready to speak."
    const val STR_LENS_TYPE = "lensType"
    const val STR_RATED = "You had rated "
    const val STR_RATE_VALUE = "/5 for this application"
    const val STR_HAVE_RATED = "You have rated "
    const val STR_SHARING_EXPERIENCE = "/5 \nThanks for Sharing your Experience!!"
    const val STR_FAILED_ENGINE = "Failed to start lens engine."
    const val STR_DEAR = "Dear "
    const val STR_RATED_AS = ", \nYou have rated as "
    const val STR_FEED_BACK = ". Thank you for your valuable feedback. \uD83D\uDC4D"
    const val STRING_ON_PAUSE = "onPause start."
    const val STRING_ON_END = "onPause end."
    const val STR_EYE_BLINK_LOGIN = "Eye blink detected. Login successful"
    const val STR_EYE_BLINKED_CALLED = "Eye blinked called ---"
    const val STR_COLON = ":"
    const val STR_ERROR_MSG = "ERROR_MSG"
    const val STR_MESSAGE = "Message"
    const val STR_OK = "OK"
    const val STR_UTF_8 = "UTF-8"
    const val STR_CURRENT_TIME = "Current time => "
    const val STR_PROGRAM_START = "Create program start."
    const val STR_PROGRAM = "PROGRAM"
    const val STR_IN_POSITION = "inPosition"
    const val STR_IN_COLOR = "inColor"
    const val STR_IN_POINT_SIZE = "inPointSize"
    const val STR_MVP_MATRIX = "inMVPMatrix"
    const val STR_UPDATE_SKELETON = "Update hand skeleton lines data start."
    const val STR_GENDER_FEMALE = "Female"
    const val STR_GENDER_MALE = "Male"
    const val STR_PROGRAM_END = "Create program end."
    const val STR_SMILING = "Smiling"
    const val STR_NEUTRAL = "Neutral"
    const val STR_ANGRY = "Angry"
    const val STR_FEAR = "Fear"
    const val STR_SAD = "Sad"
    const val STR_DISGUST = "Disgust"
    const val STR_SURPRISE = "Surprise"
    const val ERR_DISPLAY_ROTATION = "SetDisplayRotationManage error, displayRotationManage is null!"
    const val ERR_SESSION = "set session error, arSession is null!"
    const val ERR_TEXT_VIEW = "Set text view error, textView is null!"
    const val EXCP_ARDEMOE = "Exception on the ArDemoRuntimeException!"
    const val EXCP_OPEN_GL = "Exception on the OpenGL thread"
    const val STR_HAND_RECONITION = "Hand Recognition Result"
    const val STR_ITEM = "Item = "
    const val Permission_Required = "CAMERA and AUDIO Services Permission required for this app"
    const val STR_WINDOWS_FOCUS = "onWindowFocusChanged"
    const val STR_ON_STATE = "onState"
    const val STR_RECOGNIZING_RESULTS = "onRecognizingResults"
    const val STR_ON_RESULTS = "onResults"
    const val INIT_ZERO = 0
    const val INIT_ONE = 1
    const val INIT_TWO = 2
    const val INIT_THREE = 3
    const val INIT_FOUR = 4
    const val INIT_FIVE = 5
    const val INIT_TEN = 10
    const val INIT_MINUS_ONE = -1
    const val RADIUS_10F = 10f
    const val RADIUS_40F = 40.0f
    const val RADIUS_300F = 300.0f
    const val STR_ON_ERROR = "onError"
    const val STR_ON_START_LISTENING = "onStartListening"
    const val STR_LISTENING_ALERT_MESSAGE = "Listening.... Please wait."
    const val STR_WEATHER = "today's weather"
    const val WEATHER_LAT = 12.426183
    const val WEATHER_LONG = 76.390479
    const val DELAY_MILLIS = 1000
    const val STR_OPEN_CAMERA = "open camera"
    const val STR_OPENING_CAMERA = " Opening Camera"
    const val STR_OPEN_GALLERY = "Open gallery"
    const val STR_OPENING_GALLERY = "Opening gallery"
    const val STR_WHAT_TIME = "what is time"
    const val STR_TIME_NOW = "time now"
    const val STR_NOW_TIME = ", now the time is "
    const val STR_STARTING_SPEECH = "onStartingOfSpeech"
    const val STR_MY_NAME_IS = "my name is"
    const val STR_DATE = "date"
    const val STR_IS_DATE = "is date"
    const val STR_NAME_SAVED = "Your name is saved"
    const val STR_EMPTY = ""
    const val STR_NEXT_LINE = ", \n"
    const val STR_WHAT_IS_NAME = "what is my name"
    const val STR_YOUR_NAME_IS = "Your name is "
    const val STR_SAY_MY_NAME = "Your name is not saved. Say My name is _______"
    const val STR_FEEDBACK = "feedback"
    const val STR_REDIRECT_FEEDBACK = ", \nRedirecting to feedback screen..."
    const val STR_SEARCH = "search"
    const val STR_OPEN_SETTING = "open setting"
    const val STR_NO_DATA_FOUND = ", \nNo data found. Please try again.."
    const val STR_BANGALORE = "Bangalore"
    const val STR_WEATHER_URL = "https://openweathermap.org/data/2.5/weather?q="
    const val STR_APP_ID = "&appid=439d4b804bc8187953eb36d2a8c26a02"
    const val STR_CANNOT_FIND_WEATHER = "cannot find weather"
    const val EXCEPTION_MSG = "Exception"
    const val STR_WEATER_MSG = "weather"
    const val STR_WEATHER_CONTENT = "weather content"
    const val STR_IT_WILL_BE = "It will be "
    const val STR_MAIN = "main"
    const val STR_TEMP_MAX = "temp_max"
    const val STR_TEMP_MIN = "temp_min"
    const val STR_FORCAST = "with a forcast high of "
    const val STR_LOW = "\u2103 and a low of "
    const val STR_NO_RESULT_FOUND = "No result found"
    const val STR_AGREE_MSG = "Please agree to install."
    const val STR_IS_INSTALL = "Is Install AR Engine Apk: "
    const val STR_UPDATE_APK = "Please update HuaweiARService.apk"
    const val STR_UPDATE_APP = "Please update this app"
    const val STR_CONFIGURATION_NOT_SUPPORTED = "The configuration is not supported by the device!"
    const val STR_STOP_AR_SESSION = "stopArSession start."
    const val STR_CREATING_SESSION_ERROR = "Creating session error"
    const val STR_WEATHER_IS = "Weather is "
    const val STR_TEMP = "temp"
    const val STR_LINE = "\n"
    const val STR_TEMPERATURE = "\n temperature is "
    const val STR_AUTHENICATION_ERROR = "Authentication Error"
    const val STR_PARSE_ERROR = "Parse Error"
    const val STR_NO_CONNECTION_ERROR = "No Connection Error"
    const val STR_NETWORK_ERROR = "Network Error"
    const val STR_TIMEOUT_ERROR = "Timeout Error"
    const val STR_SERVER_MAINTENANCE = "Server is under maintenance.Please try later."
    const val STR_CONTETNT_TYPE = "Content-Type"
    const val STR_APPLICATION_JSON = "application/json"
    const val RETRO_API_TIME_OUT = 30000
    const val STR_AR_SESSION = "stopArSession end."

    //Util.java
    const val STR_MYPREF = "mypref"
    const val STR_DATE_PATTERN = "dd-MMM-yyyy"
    const val STR_HOUR_PATTERN = "hh:mm a"
    const val STR_TODAY_DATE = "Today's date is "
    const val STR_SELECT_PICTURE = "Select Picture"
    const val STR_DESCRIPTION = "description"
    const val STR_HAND_SIGNAL = "handResult"
    const val STR_FPS = "FPS="
    const val CAMERA_PROJECTION = "Camera projection matrix: "
    const val STR_UPDATE_BOX = "Update hand box data start."
    const val STR_START_AD = "Start to load ad"
    const val STR_END_AD = "End to load ad"
    const val STR_PAUSED = "jump hasPaused: "
    const val STR_JUMP_APPLICATION = "jump into application"
    const val STR_ON_STOP = "SplashActivity onStop."
    const val STR_ON_RESTART = "SplashActivity onRestart."
    const val STR_ON_DESTROY = "SplashActivity onDestroy."
    const val STR_ON_PAUSE = "SplashActivity onPause."
    const val STR_ON_RESUME = "SplashActivity onResume."
    const val STR_ON_AD_LOADED = "SplashAdLoadListener onAdLoaded."
    const val STR_ON_AD_FAILED = "SplashAdLoadListener onAdFailedToLoad, errorCode: "
    const val STR_ON_AD_DISMISSED = "SplashAdLoadListener onAdDismissed."
    const val STR_ON_AD_SHOWED = "SplashAdDisplayListener onAdShowed."
    const val STR_ON_AD_CLICK = "SplashAdDisplayListener onAdClick."
    const val STR_VPOSITION = "vPosition"
    const val STR_VCOORD = "vCoord"
    const val STR_VMATRIX = "vMatrix"
    const val STR_VTEXTURE = "vTexture"
    const val STR_VCOOR_MATRIX = "vCoordMatrix"
    const val STR_COULD_NOT_LINK = "Could not link program:"
    const val STR_COMPILE_SHADER = "Could not compile shader:"
    const val STR_GLES = "GLES20 Error:"
    const val STR_GL_ERROR = "glError: "
    const val STR_SHOW_DIALOG = "show dialog."
    const val STR_EDC_DIALOG = "Show education dialog."
    const val STR_SHOW_APP_MARKET = "Show education showAppMarket123."
    const val STR_AR_ENGINE_CALLBACK = "arengine_install onClick."
    const val STR_AR_INSTALL_ACTIVITY = "Failed to launch ARInstallActivity"
    const val STR_NO_PERMISSION_MEDIA = "the target app has no permission of media"
    const val STR_TARGET_ACTIVITY_NOT_FOUND = "the target activity is not found: "
    const val STR_IS_POTRAIT_MODE = "isPortraitMode returning false by default"
    const val STR_GESTURE_HAND_POINT = "gesture.getGestureHandPointsNum()"
    const val STR_HAND_BOX_START = "Draw hand box start."
    const val STR_HAND_BOX_END = "Draw hand box end."
    const val STR_GESTURE_LENGTH = "gestureAction length:["
    const val STR_GESTURE_ = "gestureAction["
    const val STR_INIT_END = "Init end."
    const val STR_INIT_START = "Init start."
    const val STR_GESTURE_ACTION = "GestureAction:"
    const val STR_RATIO = "]:["
    const val STR_CLOSE_BRACKET = "]"
    const val STR_GESTURE_CENTER = "gestureCenter length:["
    const val STR_GESTURE_CENTER_ = "gestureCenter["
    const val STR_LINE_POINTS = "Skeleton skeleton line points num: "
    const val STR_LINEPOINTS = "Skeleton line points: "
    const val STR_UPDATE_LINES_DATA = "Update hand skeleton lines data end"
    const val STR_SKELETON_LINE_START = "Draw hand skeleton line start."
    const val STR_SKELETON_LINE_END = "Draw hand skeleton line end."
    const val STR_UPDATE = "update..."
    const val STR_DISPLAY_NULL = "updateArSessionDisplayGeometry mDisplay null!"
    const val STR_HAND_BOX_LENGTH = "GestureHandBox length:["
    const val STR_GESTURE_POINTS = "gesturePoints:"
    const val STR_GESTURE_POINTS_START = "gesturePoints["
    const val STR_THUBS_UP = "Thumbs Up"
    const val STR_THUMBS_DOWN = "Thumbs Down"
    const val STR_GESTURE_TYPE = "GestureType="
    const val STR_GESTURE_COORDINATE_SYSTEM = "GestureCoordinateSystem="
    const val STR_GESTURE_ORIENTATION = "gestureOrientation length:["
    const val STR_GESTURE_ORIENTATION_ = "gestureOrientation:"
    const val STR_GESTURE_OPEN_BRACES = "gestureOrientation["
    const val STR_HAND_TYPE = "Handtype="
    const val STR_SKELETON_COORDINATE = "SkeletonCoordinateSystem="
    const val STR_SKELETON_ARRAY = "HandskeletonArray length:["
    const val STR_SKELETON_ARRAY_LENGTH = "SkeletonArray.length:"
    const val STR_SKELETON = "SkeletonArray:"
    const val STR_SKELETON_CONNECTION = "HandSkeletonConnection length:["
    const val STR_SKELETON_CONNECTION_LENGTH = "handSkeletonConnection.length:"
    const val STR_HAND_SKELETON_CONNECTION = "handSkeletonConnection:"
    const val STR_IMG_TYPE = "image/*"
    const val STR_SKELETON_END = "Draw hand skeletons end."
    const val STR_UPDATE_SKELETON_END = "Update hand skeletons data end."
    const val STR_SKELETON_START = "Draw hand skeletons start."
    const val STR_UPDATE_HAND_SKELETONS_START = "Update hand skeletons data start."
    const val FLOAT_OF = 0.0f
    const val FLOAT_1F = 1.0f
    const val FLOAT_POINT_1F = 0.1f
    const val FLOAT_18F = 18.0f
    const val STR_ILLEGAL_EXTERNAL_INPUT = "onDrawFrame Illegal external input!"
    const val STR_ARHAND_SKELETON = "ARHand HandSkeletonNumber = "
    const val MAX_PROJECTION_MATRIX = 16
    const val STR_GL_ERROR_SHADER = "glError: Could not compile shader "
    const val STR_F = " F"
    const val RECORD_AUDIO_PERMISSION = "Application Required Audio Permission"
}