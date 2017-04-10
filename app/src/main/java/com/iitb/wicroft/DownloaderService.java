package com.iitb.wicroft;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.drm.DefaultDrmSessionManager;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.drm.FrameworkMediaDrm;
import com.google.android.exoplayer2.drm.HttpMediaDrmCallback;
import com.google.android.exoplayer2.drm.UnsupportedDrmException;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer;
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil;
import com.google.android.exoplayer2.source.BehindLiveWindowException;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.DebugTextViewHelper;
import com.google.android.exoplayer2.ui.PlaybackControlView;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;
import com.google.android.exoplayer2.Format;

/**
 * Created by swinky on 3/7/16.
 */
//called by alarm receiver to start serving next download event
public class DownloaderService extends IntentService implements ExoPlayer.EventListener,
        PlaybackControlView.VisibilityListener{

    Handler uiHandler;

    Handler h;
    static boolean countedThisStallBefore=false;
    Calendar start;
    long startTime;

    Calendar buffering_start;
    long buffering_startTime;



    int numberOfStalls;
    Intent intent;
    public static final String DRM_SCHEME_UUID_EXTRA = "drm_scheme_uuid";
    public static final String DRM_LICENSE_URL = "drm_license_url";
    public static final String DRM_KEY_REQUEST_PROPERTIES = "drm_key_request_properties";
    public static final String PREFER_EXTENSION_DECODERS = "prefer_extension_decoders";

    public static final String ACTION_VIEW = "com.google.android.exoplayer.demo.action.VIEW";
    public static final String EXTENSION_EXTRA = "extension";

    public static final String ACTION_VIEW_LIST =
            "com.google.android.exoplayer.demo.action.VIEW_LIST";
    public static final String URI_LIST_EXTRA = "uri_list";
    public static final String EXTENSION_LIST_EXTRA = "extension_list";

    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    private static final CookieManager DEFAULT_COOKIE_MANAGER;
    static {
        DEFAULT_COOKIE_MANAGER = new CookieManager();
        DEFAULT_COOKIE_MANAGER.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    }

    private Handler mainHandler;
    private EventLogger eventLogger;
    private SimpleExoPlayerView simpleExoPlayerView;
    private LinearLayout debugRootView;
    private TextView debugTextView;
    private Button retryButton;

    private DataSource.Factory mediaDataSourceFactory;
    private SimpleExoPlayer player;
    private DefaultTrackSelector trackSelector;
    // private TrackSelectionHelper trackSelectionHelper;
    private DebugTextViewHelper debugViewHelper;
    private boolean needRetrySource;

    private boolean shouldAutoPlay;
    private int resumeWindow;
    private long resumePosition;

    public DownloaderService() {
        super("DownloaderService");
        // TODO Auto-generated constructor stub
    }

    Intent getIntent(){
        return this.intent;
    }


    public void onCreate(){
        super.onCreate();
       uiHandler = new Handler(); // This makes the handler attached to UI Thread
        h = new Handler();
    }

    @Override
    public void onLoadingChanged(boolean isLoading) {
        // Do nothing.
    }

    @Override
    public void onPositionDiscontinuity() {
        if (needRetrySource) {
            // This will only occur if the user has performed a seek whilst in the error state. Update the
            // resume position so that if the user then retries, playback will resume from the position to
            // which they seeked.
            updateResumePosition();
        }
    }




    @Override
    public void onVisibilityChange(int visibility) {
        debugRootView.setVisibility(visibility);
    }




    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

        Log.d("playerStateChanged", playbackState + " " + ExoPlayer.STATE_ENDED );
        if(playbackState == ExoPlayer.STATE_READY){

             buffering_startTime=0;
             Log.d("METRICS state_ready","player started");



                if(countedThisStallBefore==false) {
                    start = Utils.getServerCalendarInstance();
                    startTime = start.getTimeInMillis();
                    numberOfStalls = 0;
                }

            countedThisStallBefore=false;
        }

        if (playbackState == ExoPlayer.STATE_ENDED) {
            //showControls();

            Calendar end = Utils.getServerCalendarInstance();
            long endTime = end.getTimeInMillis();
            String startTimeFormatted =  Utils.sdf.format(start.getTime());
            String endTimeFormatted =  Utils.sdf.format(end.getTime());
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
            Log.d("logging METRICS","tofile: "+ MainActivity.logfilename+ " number_of_stalls: " + numberOfStalls);
            Threads.writeToLogFile(MainActivity.logfilename, "Start_Time:" + startTimeFormatted + " End_time:" + endTimeFormatted +
                    " IP:" + Utils.getIP() + " " +
                    "MAC:" + Utils.getMACAddress() + " " +
                    "RSSI:" + MainActivity.rssi + "dBm " +
                    "BSSID:" + MainActivity.bssid + " " +
                    "SSID:" + MainActivity.ssid + " " +
                    "LINK_SPEED:" + MainActivity.linkSpeed + "Mbps " + " Total_Playback_Time:" + (endTime - startTime) +  " Number_Of_Stalls:"+numberOfStalls +" Video_Length:"+player.getDuration()+" Buffering_Time:"+((endTime-startTime)-player.getDuration()) +
                    " Average_Bitrate:"+ BANDWIDTH_METER.getBitrateEstimate());

        }
        if((playbackState== ExoPlayer.STATE_BUFFERING || playbackState==ExoPlayer.STATE_IDLE) && playWhenReady==true){
            if(startTime>0 && countedThisStallBefore==false) {
                numberOfStalls++;
                countedThisStallBefore=true;
                Log.d("METRICS","idle or buffering ");
            }

             buffering_start = Utils.getServerCalendarInstance();
             buffering_startTime = buffering_start.getTimeInMillis();


        }
        //updateButtonVisibilities();
    }

    private static boolean isBehindLiveWindow(ExoPlaybackException e) {
        if (e.type != ExoPlaybackException.TYPE_SOURCE) {
            return false;
        }
        Throwable cause = e.getSourceException();
        while (cause != null) {
            if (cause instanceof BehindLiveWindowException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {
        // Do nothing.
    }

    @Override
    public void onPlayerError(ExoPlaybackException e) {
        String errorString = null;
        if (e.type == ExoPlaybackException.TYPE_RENDERER) {
            Exception cause = e.getRendererException();
            if (cause instanceof MediaCodecRenderer.DecoderInitializationException) {
                // Special case for decoder initialization failures.
                MediaCodecRenderer.DecoderInitializationException decoderInitializationException =
                        (MediaCodecRenderer.DecoderInitializationException) cause;
                if (decoderInitializationException.decoderName == null) {
                    if (decoderInitializationException.getCause() instanceof MediaCodecUtil.DecoderQueryException) {
                        errorString = getString(R.string.error_querying_decoders);
                    } else if (decoderInitializationException.secureDecoderRequired) {
                        errorString = getString(R.string.error_no_secure_decoder,
                                decoderInitializationException.mimeType);
                    } else {
                        errorString = getString(R.string.error_no_decoder,
                                decoderInitializationException.mimeType);
                    }
                } else {
                    errorString = getString(R.string.error_instantiating_decoder,
                            decoderInitializationException.decoderName);
                }
            }
        }
        if (errorString != null) {
            //showToast(errorString);
        }
        needRetrySource = true;
        if (isBehindLiveWindow(e)) {
            clearResumePosition();
            initializePlayer();
        } else {
            updateResumePosition();
            //updateButtonVisibilities();
            //showControls();
        }
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
        //updateButtonVisibilities();
        MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
        if (mappedTrackInfo != null) {
            if (mappedTrackInfo.getTrackTypeRendererSupport(C.TRACK_TYPE_VIDEO)
                    == MappingTrackSelector.MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
                //showToast(R.string.error_unsupported_video);
            }
            if (mappedTrackInfo.getTrackTypeRendererSupport(C.TRACK_TYPE_AUDIO)
                    == MappingTrackSelector.MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
                //showToast(R.string.error_unsupported_audio);
            }
        }
    }

    private void initializePlayer() {
        Intent intent = getIntent();
        boolean needNewPlayer = player == null;
        if (needNewPlayer) {
            boolean preferExtensionDecoders = intent.getBooleanExtra(PREFER_EXTENSION_DECODERS, false);
            UUID drmSchemeUuid = intent.hasExtra(DRM_SCHEME_UUID_EXTRA)
                    ? UUID.fromString(intent.getStringExtra(DRM_SCHEME_UUID_EXTRA)) : null;
            DrmSessionManager<FrameworkMediaCrypto> drmSessionManager = null;
            if (drmSchemeUuid != null) {
                String drmLicenseUrl = intent.getStringExtra(DRM_LICENSE_URL);
                String[] keyRequestPropertiesArray = intent.getStringArrayExtra(DRM_KEY_REQUEST_PROPERTIES);
                try {
                    drmSessionManager = buildDrmSessionManager(drmSchemeUuid, drmLicenseUrl,
                            keyRequestPropertiesArray);
                } catch (UnsupportedDrmException e) {
                    int errorStringId = Util.SDK_INT < 18 ? R.string.error_drm_not_supported
                            : (e.reason == UnsupportedDrmException.REASON_UNSUPPORTED_SCHEME
                            ? R.string.error_drm_unsupported_scheme : R.string.error_drm_unknown);
                    //showToast(errorStringId);
                    return;
                }
            }

            @SimpleExoPlayer.ExtensionRendererMode int extensionRendererMode =
                    ((DemoApplication) getApplication()).useExtensionRenderers()
                            ? (preferExtensionDecoders ? SimpleExoPlayer.EXTENSION_RENDERER_MODE_PREFER
                            : SimpleExoPlayer.EXTENSION_RENDERER_MODE_ON)
                            : SimpleExoPlayer.EXTENSION_RENDERER_MODE_OFF;
            TrackSelection.Factory videoTrackSelectionFactory =
                    new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
            trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

            //trackSelectionHelper = new TrackSelectionHelper(trackSelector, videoTrackSelectionFactory);
            //player = ExoPlayerFactory.newSimpleInstance(this, trackSelector, new DefaultLoadControl(),
                    //drmSessionManager, extensionRendererMode;
            player = ExoPlayerFactory.newSimpleInstance(this,trackSelector,new DefaultLoadControl());
            player.addListener(this);

            Log.d("renderers"," "+player.getRendererCount());

            int c=player.getRendererCount();
            for(int i=0;i<c;i++){
                Log.d("renderer", " "+i+" "+trackSelector.getRendererDisabled(i));
                if(i==0 && !trackSelector.getRendererDisabled(i)){

                    trackSelector.setRendererDisabled(i,true);
                }

            }


            eventLogger = new EventLogger(trackSelector);
            player.addListener(eventLogger);
            player.setAudioDebugListener(eventLogger);
            player.setVideoDebugListener(eventLogger);
            player.setMetadataOutput(eventLogger);
            player.setVolume(0);
            //simpleExoPlayerView.setPlayer(player);
            player.setPlayWhenReady(shouldAutoPlay);
            //debugViewHelper = new DebugTextViewHelper(player, debugTextView);
            //debugViewHelper.start();
        }
        if (needNewPlayer || needRetrySource) {
            String action = intent.getAction();
            Log.d("in intialise player",intent.getData().toString());
            Uri[] uris;
            String[] extensions;
            if (ACTION_VIEW.equals(action)) {
                uris = new Uri[] {intent.getData()};
                extensions = new String[] {intent.getStringExtra(EXTENSION_EXTRA)};
            } else if (ACTION_VIEW_LIST.equals(action)) {
                String[] uriStrings = intent.getStringArrayExtra(URI_LIST_EXTRA);
                uris = new Uri[uriStrings.length];
                for (int i = 0; i < uriStrings.length; i++) {
                    uris[i] = Uri.parse(uriStrings[i]);
                }
                extensions = intent.getStringArrayExtra(EXTENSION_LIST_EXTRA);
                if (extensions == null) {
                    extensions = new String[uriStrings.length];
                }
            } else {
                //showToast(getString(R.string.unexpected_intent_action, action));
                return;
            }
//      if (Util.maybeRequestReadExternalStoragePermission(this, uris)) {
//        // The player will be reinitialized if the permission is granted.
//        return;
//      }
            MediaSource[] mediaSources = new MediaSource[uris.length];
            for (int i = 0; i < uris.length; i++) {
                mediaSources[i] = buildMediaSource(uris[i], extensions[i]);
            }
            MediaSource mediaSource = mediaSources.length == 1 ? mediaSources[0]
                    : new ConcatenatingMediaSource(mediaSources);
            boolean haveResumePosition = resumeWindow != C.INDEX_UNSET;
            if (haveResumePosition) {
                player.seekTo(resumeWindow, resumePosition);
            }
            player.prepare(mediaSource, !haveResumePosition, false);
            needRetrySource = false;
            //updateButtonVisibilities();
        }
    }

    private MediaSource buildMediaSource(Uri uri, String overrideExtension) {
        int type = TextUtils.isEmpty(overrideExtension) ? Util.inferContentType(uri)
                : Util.inferContentType("." + overrideExtension);
        switch (type) {
            case C.TYPE_SS:
                return new SsMediaSource(uri, buildDataSourceFactory(false),
                        new DefaultSsChunkSource.Factory(mediaDataSourceFactory), mainHandler, eventLogger);
            case C.TYPE_DASH:
                return new DashMediaSource(uri, buildDataSourceFactory(false),
                        new DefaultDashChunkSource.Factory(mediaDataSourceFactory), mainHandler, eventLogger);
            case C.TYPE_HLS:
                return new HlsMediaSource(uri, mediaDataSourceFactory, mainHandler, eventLogger);
            case C.TYPE_OTHER:
                return new ExtractorMediaSource(uri, mediaDataSourceFactory, new DefaultExtractorsFactory(),
                        mainHandler, eventLogger);
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
    }

    private DrmSessionManager<FrameworkMediaCrypto> buildDrmSessionManager(UUID uuid,
                                                                           String licenseUrl, String[] keyRequestPropertiesArray) throws UnsupportedDrmException {
        if (Util.SDK_INT < 18) {
            return null;
        }
        HttpMediaDrmCallback drmCallback = new HttpMediaDrmCallback(licenseUrl,
                buildHttpDataSourceFactory(false));
        if (keyRequestPropertiesArray != null) {
            for (int i = 0; i < keyRequestPropertiesArray.length - 1; i += 2) {
                drmCallback.setKeyRequestProperty(keyRequestPropertiesArray[i],
                        keyRequestPropertiesArray[i + 1]);
            }
        }
        return new DefaultDrmSessionManager<>(uuid,
                FrameworkMediaDrm.newInstance(uuid), drmCallback, null, mainHandler, eventLogger);
    }

    /**
     * Returns a new DataSource factory.
     *
     * @param useBandwidthMeter Whether to set {@link #BANDWIDTH_METER} as a listener to the new
     *     DataSource factory.
     * @return A new DataSource factory.
     */
    private DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
        return ((DemoApplication) getApplication())
                .buildDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
    }

    /**
     * Returns a new HttpDataSource factory.
     *
     * @param useBandwidthMeter Whether to set {@link #BANDWIDTH_METER} as a listener to the new
     *     DataSource factory.
     * @return A new HttpDataSource factory.
     */
    private HttpDataSource.Factory buildHttpDataSourceFactory(boolean useBandwidthMeter) {
        return ((DemoApplication) getApplication())
                .buildHttpDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
    }

    private void releasePlayer() {
        if (player != null) {
            debugViewHelper.stop();
            debugViewHelper = null;
            shouldAutoPlay = player.getPlayWhenReady();
            updateResumePosition();
            player.release();
            player = null;
            trackSelector = null;
            //trackSelectionHelper = null;
            eventLogger = null;
        }
    }

    private void updateResumePosition() {
        resumeWindow = player.getCurrentWindowIndex();
        resumePosition = player.isCurrentWindowSeekable() ? Math.max(0, player.getCurrentPosition())
                : C.TIME_UNSET;
    }

    private void clearResumePosition() {
        resumeWindow = C.INDEX_UNSET;
        resumePosition = C.TIME_UNSET;
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        this.intent=intent;
        startTime=0;
        String msg=" DownloaderService : ";
        if(!MainActivity.running){
            msg+=" entered. But experiment not running";
            return;
        }

        if(MainActivity.load == null){
            msg+=" load null";
            return;
        }

        //Log.d(Constants.LOGTAG, "DownloaderService : just entered");
        Bundle bundle = intent.getExtras();
        final int eventid = bundle.getInt("eventid");
        Log.d(" Downloader Service ", "Event id = "+eventid);
        final RequestEvent event = MainActivity.load.events.get(eventid-1);

        //final RequestEvent e = MainActivity.load.events.get(eventid);

        msg +=" Handling event " + eventid + "in a thread ... ";

        boolean webviewon = true;
        if(event.mode == DownloadMode.SOCKET){

            Runnable r = new Runnable() {
                public void run() {
                    Threads.HandleEvent(eventid, getApplicationContext());
                }
            };

            Thread t = new Thread(r);

            t.start();
        }
        else if(event.mode == DownloadMode.WEBVIEW){


            //final String url = event.url;

            msg+= "HandleEvent : just entered thread";


            uiHandler.post(new Runnable() {

                @Override
                public void run() {

                    WebView webview = new WebView(getApplicationContext());
                    webview.setWebViewClient(new MyBrowser(eventid, event.url,getApplicationContext()));
                    // WebSettings settings = webview.getSettings();
                    //settings.setJavaScriptEnabled(true);
                    //settings.setJavaScriptEnabled(false);
                    //settings.setPluginsEnabled(false);

                    MainActivity.webViewMap.put(eventid, webview);
                    webview.loadUrl(event.url + "##" + event.postDataSize + "##" + event.type);


                }
            });

        }
        else if(event.mode == DownloadMode.EXO){



            MainActivity.logfilename = "" + MainActivity.load.loadid;

            msg+= "HandleEvent : just entered thread";
            this.intent.setData(Uri.parse(event.url));
            Log.d("Before thread",event.url);
            MainActivity.logfilename = "" + MainActivity.load.loadid;



            final int delay = 2000; //milliseconds
            h.postDelayed(new Runnable(){
                public void run(){
                    //do something

                    Calendar buffer_end = Utils.getServerCalendarInstance();
                    long buffer_endTime = buffer_end.getTimeInMillis();


                    if(player.getPlaybackState()==ExoPlayer.STATE_BUFFERING || player.getPlaybackState()==ExoPlayer.STATE_IDLE){
                        if(buffer_endTime-buffering_startTime>5000){
                            //Log.d("metrics","restart player");
                            initializePlayer();
                        }
                    }
                    if(player.getPlaybackState()==ExoPlayer.STATE_ENDED){
                        //Log.d("metrics","exit");
                    }
                    h.postDelayed(this, delay);
                }
            }, delay);






            uiHandler.post(new Runnable() {

                @Override
                public void run() {

                    Calendar start = Utils.getServerCalendarInstance();
                    long startTime = start.getTimeInMillis();
                    shouldAutoPlay = true;

                    clearResumePosition();
                    mediaDataSourceFactory = buildDataSourceFactory(true);
                    mainHandler = new Handler();
                    if (CookieHandler.getDefault() != DEFAULT_COOKIE_MANAGER) {
                        CookieHandler.setDefault(DEFAULT_COOKIE_MANAGER);
                    }



                    initializePlayer();

                }


            });





        }
        else{
            msg+= "Incorrect Download mode specified";
        }

        Log.d(Constants.LOGTAG, msg);
        if(MainActivity.debugging_on) {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");

            Threads.writeToLogFile(MainActivity.debugfilename, format1.format(cal.getTime()) + " " + Utils.sdf.format(cal.getTime()) + msg );
        }

        EventAlarmReceiver.completeWakefulIntent(intent);
    }
}