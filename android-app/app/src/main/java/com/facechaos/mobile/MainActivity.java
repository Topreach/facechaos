package com.facechaos.mobile;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.os.Environment;
import android.widget.Switch;
import android.widget.VideoView;
import android.widget.MediaController;
import android.content.ContentValues;
import android.content.ClipData;
import android.provider.MediaStore;
import java.io.*;
import java.util.Locale;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ScrollView;
import android.graphics.drawable.GradientDrawable;

/** Camera games with local face, head and sound controls, plus optional silent video recording. */
public final class MainActivity extends Activity {
    private SelfieCamera camera;
    private GameAudio gameAudio;
    private android.media.AudioTrack animalGuideTrack;
    private MiniGame animalGuideOwner;
    private MicrophoneInput microphone;
    private CountrySpeech countrySpeech;
    private boolean quizVoice=true;
    private long nextCountrySpeech;
    private int speechGeneration;
    private FaceRecognition faceRecognition;
    private boolean faceChoice=true,faceLab,reverseHead;
    private int faceLabAction=FaceSignals.NONE;
    private int faceGeneration;
    private long lastFaceResult;
    private TextView faceCheckText;
    private final int[] faceTrialCounts=new int[6];
    private float faceSensitivity=1;
    private final Runnable faceFrame=new Runnable(){public void run(){
        long frameStart=SystemClock.uptimeMillis();
        if(!resumed||faceRecognition==null||!faceLab&&(paused||!gameVisible()||overlay.game.finished||!overlay.game.faceMode))return;
        if(camera.hasFreshAnalysisFrame()&&preview.isAvailable()&&faceRecognition.canAccept()){
            faceRecognition.expectedAction=faceLab?faceLabAction:FaceGameInput.expected(overlay.game);
            float frameScale=Math.min(480f/Math.max(1,preview.getWidth()),1040f/Math.max(1,preview.getHeight()));
            int width=Math.max(1,Math.round(preview.getWidth()*frameScale)),height=Math.max(1,Math.round(preview.getHeight()*frameScale));
            Bitmap frame=preview.getBitmap(width,height);
            if(frame!=null&&!faceRecognition.offer(frame,SystemClock.uptimeMillis()))frame.recycle();
        }
        if((!camera.hasFreshAnalysisFrame()||SystemClock.uptimeMillis()-lastFaceResult>1100)&&gameVisible()&&overlay.game.faceMode){overlay.game.faceReady=false;overlay.game.faceStatus="Finding your face - look at the camera";overlay.invalidate();}
        ui.postAtTime(this,Math.max(SystemClock.uptimeMillis()+1,frameStart+60));
    }};
    private boolean useVoiceMicrophone, pendingInputSwitch, inputSwitchPaused;
    private int micGeneration, pendingSoundMode=3;
    private GameOverlay overlay;
    private TextView status;
    private Button permission;
    private Button playMenu;
    private LinearLayout actions;
    private boolean resumed;
    private boolean paused;
    private boolean awaitingStart=true;
    private int gameGeneration, permissionGeneration;
    private AlertDialog switchConfirmation;
    private TextureView preview;
    private CaptureButton captureButton;
    private TextView captureLabel, recordState;
    private Switch recordSwitch;
    private Button gamesButton;
    private final Handler ui = new Handler(Looper.getMainLooper());
    private VideoEncoder encoder;
    private Bitmap videoFrame;
    private boolean preparingVideo, finishingVideo, handledResult;
    private File lastClip, exportClip;
    private Runnable afterVideo;
    private long recordingStart, pauseStart, pausedTime;
    private int recordedWidth, recordedHeight;
    private final Runnable tick = new Runnable(){public void run(){
        if(!resumed)return;
        if(overlay.getVisibility()==View.VISIBLE && overlay.game.finished && !handledResult){
            handledResult=true;
            if(!overlay.game.stopped){android.content.SharedPreferences ratings=getSharedPreferences("ratings",0);ratings.edit().putInt("completed_rounds",ratings.getInt("completed_rounds",0)+1).apply();}
            VideoEncoder owner=encoder;
            if(owner!=null)ui.postDelayed(()->{if(encoder==owner)stopVideo(null);},1000);
        }
        if(gameVisible()&&overlay.game.finished){stopMicrophone();if(!faceLab)stopFaceRecognition();stopCountrySpeech();}
        if(gameVisible()&&overlay.game.mode==14&&countrySpeech!=null&&!overlay.game.quizListening){stopCountrySpeech();nextCountrySpeech=SystemClock.uptimeMillis()+1400;}
        if(gameVisible()&&overlay.game.mode==14&&!paused&&!overlay.game.finished&&overlay.game.countdown<=0&&!overlay.game.resolved&&quizVoice&&countrySpeech==null&&SystemClock.uptimeMillis()>=nextCountrySpeech)startCountrySpeech();
        if(gameAudio!=null)gameAudio.update(overlay.game,gameVisible()&&!awaitingStart&&!paused);
        updateCaptureControls();ui.postDelayed(this,100);
    }};
    private final Runnable captureFrame = new Runnable(){public void run(){
        long frameStart=SystemClock.uptimeMillis();
        if(!resumed||encoder==null||preparingVideo||finishingVideo||paused)return;
        if(!camera.hasFreshFrame()){stopVideo(null);Toast.makeText(MainActivity.this,"Camera feed stopped. Recording finished.",Toast.LENGTH_LONG).show();return;}
        if(encoder.available()&&preview.isAvailable()){
            try{
                if(preview.getBitmap(videoFrame)==null)throw new IllegalStateException("No camera frame");
                if(overlay.getVisibility()==View.VISIBLE){
                    Canvas canvas=new Canvas(videoFrame);canvas.save();
                    canvas.scale(videoFrame.getWidth()/(float)preview.getWidth(),videoFrame.getHeight()/(float)preview.getHeight());
                    canvas.translate(overlay.getLeft()-preview.getLeft(),overlay.getTop()-preview.getTop());
                    overlay.draw(canvas);canvas.restore();
                }
                encoder.submit(videoFrame,System.nanoTime());
            }catch(Exception error){stopVideo(null);Toast.makeText(MainActivity.this,"Camera recording interrupted",Toast.LENGTH_LONG).show();}
        }
        ui.postAtTime(this,Math.max(SystemClock.uptimeMillis()+1,frameStart+42));
    }};

    @Override protected void attachBaseContext(android.content.Context base){super.attachBaseContext(AppLanguage.wrap(base));}
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        reverseHead=getSharedPreferences("controls",0).getBoolean("reverse_head",false);faceChoice=getSharedPreferences("controls",0).getBoolean("face",true);faceSensitivity=getSharedPreferences("controls",0).getFloat("face_sensitivity",1);
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.rgb(23, 62, 66));
        preview = new TextureView(this);
        root.addView(preview, new FrameLayout.LayoutParams(-1, -1));
        overlay = new GameOverlay(this);
        overlay.setVisibility(View.GONE);
        root.addView(overlay, new FrameLayout.LayoutParams(-1, -1));
        LinearLayout controls = new LinearLayout(this);
        controls.setOrientation(LinearLayout.VERTICAL);
        controls.setPadding(20, 12, 20, 12);
        controls.setBackgroundColor(Color.TRANSPARENT);
        status = new TextView(this);
        status.setTextColor(Color.WHITE);
        status.setTextSize(14);
        status.setVisibility(View.GONE);
        controls.addView(status);
        actions = new LinearLayout(this);
        actions.setVisibility(View.GONE);
        actions.setOrientation(LinearLayout.VERTICAL);
        controls.addView(actions);
        permission = new Button(this);
        permission.setText(AppText.t(MainActivity.this,"Enable camera"));
        permission.setVisibility(View.GONE);
        permission.setOnClickListener(v -> {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) camera.start();
            else if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) requestCamera();
            else startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName())));
        });
        controls.addView(permission);
        recordSwitch = new Switch(this);
        recordSwitch.setText(AppText.t(MainActivity.this,"Record video (silent)"));recordSwitch.setTextColor(Color.WHITE);recordSwitch.setTextSize(13);
        recordSwitch.setOnCheckedChangeListener((button,checked)->updateCaptureControls());
        controls.addView(recordSwitch,new LinearLayout.LayoutParams(-1,dp(42)));
        recordState = new TextView(this);recordState.setTextColor(Color.WHITE);recordState.setTextSize(13);recordState.setGravity(Gravity.CENTER);
        recordState.setVisibility(View.GONE);controls.addView(recordState);
        captureButton = new CaptureButton(this);
        captureButton.setOnClickListener(v -> primaryCaptureAction());
        int diameter = Math.round(80 * getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams recordLayout = new LinearLayout.LayoutParams(diameter, diameter);
        recordLayout.gravity = Gravity.CENTER_HORIZONTAL;
        recordLayout.topMargin = Math.round(8 * getResources().getDisplayMetrics().density);
        LinearLayout cameraBar = new LinearLayout(this);
        cameraBar.setGravity(Gravity.CENTER_VERTICAL);
        gamesButton = new Button(this);
        Button games = gamesButton;
        games.setText(AppText.t(MainActivity.this,"\uD83C\uDFAE  Games")); games.setTextColor(0xFF173E42); games.setAllCaps(false);
        games.setTextSize(15);games.setTypeface(null,android.graphics.Typeface.BOLD);
        GradientDrawable gameBadge=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{0xFFFFE68B,0xFFF6BE48});
        gameBadge.setCornerRadius(dp(24));gameBadge.setStroke(dp(2),0xFFFFFFFF);games.setBackground(gameBadge);games.setBackgroundTintList(null);
        games.setContentDescription("Choose a game");
        games.setOnClickListener(v -> showGames());
        cameraBar.addView(games, new LinearLayout.LayoutParams(0, -2, 1));
        LinearLayout captureColumn=new LinearLayout(this);captureColumn.setOrientation(LinearLayout.VERTICAL);captureColumn.setGravity(Gravity.CENTER);
        captureColumn.addView(captureButton,recordLayout);
        captureLabel=new TextView(this);captureLabel.setTextColor(Color.WHITE);captureLabel.setTextSize(12);captureLabel.setGravity(Gravity.CENTER);captureLabel.setPadding(0,dp(3),0,0);
        captureColumn.addView(captureLabel);cameraBar.addView(captureColumn,new LinearLayout.LayoutParams(diameter,-2));
        playMenu = new Button(this);
        playMenu.setText(AppText.t(MainActivity.this,"Ⅱ")); playMenu.setTextColor(Color.WHITE);
        playMenu.setBackgroundColor(Color.TRANSPARENT);
        playMenu.setContentDescription("Pause and game options");
        playMenu.setVisibility(View.INVISIBLE);
        playMenu.setOnClickListener(v -> stopOrReview());
        cameraBar.addView(playMenu, new LinearLayout.LayoutParams(0, -2, 1));
        controls.addView(cameraBar);
        controls.addOnLayoutChangeListener((v, left, top, right, bottomEdge, oldLeft, oldTop, oldRight, oldBottom) ->
            overlay.setBottomReserved(bottomEdge - top));
        FrameLayout.LayoutParams bottom = new FrameLayout.LayoutParams(-1, -2, Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL);
        root.addView(controls, bottom);
        root.addOnLayoutChangeListener((v,l,t,r,b,ol,ot,or,ob)->{
            int width=r-l,height=b-t;
            float density=getResources().getDisplayMetrics().density;
            int gameWidth=AdaptiveViewport.overlayWidth(width,height,density);
            FrameLayout.LayoutParams gameParams=(FrameLayout.LayoutParams)overlay.getLayoutParams();
            if(gameParams.width!=gameWidth){gameParams.width=gameWidth;gameParams.gravity=Gravity.TOP|Gravity.CENTER_HORIZONTAL;overlay.setLayoutParams(gameParams);}
            int controlWidth=AdaptiveViewport.controlsWidth(width,density);
            FrameLayout.LayoutParams controlParams=(FrameLayout.LayoutParams)controls.getLayoutParams();
            if(controlParams.width!=controlWidth){controlParams.width=controlWidth;controls.setLayoutParams(controlParams);}
            if(or>ol&&ob>ot&&(width!=or-ol||height!=ob-ot))pauseForWindowChange();
        });
        root.setOnApplyWindowInsetsListener((v, insets) -> {
            if (android.os.Build.VERSION.SDK_INT >= 30) {
                android.graphics.Insets safe = insets.getInsets(android.view.WindowInsets.Type.systemBars() | android.view.WindowInsets.Type.displayCutout());
                controls.setPadding(20 + safe.left, 12, 20 + safe.right, 12 + safe.bottom);
                overlay.setSafeTop(safe.top);
            } else {
                controls.setPadding(20 + insets.getSystemWindowInsetLeft(), 12, 20 + insets.getSystemWindowInsetRight(), 12 + insets.getSystemWindowInsetBottom());
                overlay.setSafeTop(insets.getSystemWindowInsetTop());
            }
            return insets;
        });
        Button options=new Button(this);options.setText(AppText.t(MainActivity.this,"..."));options.setTextColor(Color.WHITE);options.setBackgroundColor(Color.TRANSPARENT);
        options.setContentDescription("Game options, recordings and rating");options.setOnClickListener(v -> showAppOptions());
        FrameLayout.LayoutParams optionsLayout=new FrameLayout.LayoutParams(dp(56),dp(48),Gravity.TOP|Gravity.RIGHT);optionsLayout.topMargin=dp(32);optionsLayout.rightMargin=dp(6);
        root.addView(options,optionsLayout);
        setContentView(root);gameAudio=new GameAudio(this);
        String clipName=getPreferences(0).getString("last_clip","");
        if(clipName.matches("facechaos_[0-9]+\\.mp4")){File folder=getExternalFilesDir(Environment.DIRECTORY_MOVIES);if(folder!=null){File saved=new File(folder,clipName);if(saved.isFile())lastClip=saved;}}
        camera = new SelfieCamera(this, preview, message -> runOnUiThread(() -> showCameraStatus(message)));
        overlay.select(0);overlay.setVisibility(View.VISIBLE);setRoundPaused(true);
    }
    private void closeGame() {
        if(activeRound()){confirmGameSwitch();return;}
        endGameAndChoose();
    }
    private boolean activeRound(){return gameVisible()&&!awaitingStart&&!overlay.game.finished;}
    private void confirmGameSwitch(){
        if(switchConfirmation!=null&&switchConfirmation.isShowing())return;
        final MiniGame owner=overlay.game;final boolean wasPaused=paused;final boolean[] confirmed={false};
        setRoundPaused(true);
        switchConfirmation=new AlertDialog.Builder(this,android.R.style.Theme_Material_Light_Dialog_Alert)
            .setTitle(R.string.fc_close_game_question).setMessage(R.string.fc_close_game_note)
            .setPositiveButton(R.string.fc_yes,(dialog,which)->{confirmed[0]=true;if(overlay.game==owner)endGameAndChoose();})
            .setNegativeButton(R.string.fc_no,null).create();
        switchConfirmation.setOnDismissListener(dialog->{switchConfirmation=null;if(!confirmed[0]&&overlay.game==owner&&resumed)setRoundPaused(wasPaused);});
        switchConfirmation.show();
    }
    private void endGameAndChoose() {
        final int requestedSession=++gameGeneration;
        stopAnimalGuide();
        stopCountrySpeech();stopMicrophone();stopFaceRecognition();
        if(gameAudio!=null)gameAudio.suspend();
        setRoundPaused(true);overlay.saveCoins();overlay.game.stopRound();handledResult=true;
        Runnable close=()->{
            if(requestedSession!=gameGeneration)return;
            actions.setVisibility(View.GONE);overlay.restart();overlay.setVisibility(View.VISIBLE);
            awaitingStart=true;setRoundPaused(true);showGames();
        };
        if(encoder!=null)stopVideo(close);else close.run();
    }

    private boolean soundChoice(int mode){return SoundInputSettings.enabled(getSharedPreferences("controls",0),mode);}
    private void rememberSoundChoice(int mode,boolean enabled){SoundInputSettings.remember(getSharedPreferences("controls",0),mode,enabled);}
    private void previewGame(int mode){previewGame(mode,1);}
    private void previewGame(int mode,int stage) {previewGame(mode,stage,0);}
    private void previewGame(int mode,int stage,int carriedCoins) {
        final int requestedSession=++gameGeneration;
        pendingInputSwitch=false;
        stopAnimalGuide();stopCountrySpeech();stopMicrophone();stopFaceRecognition();
        if(gameAudio!=null)gameAudio.suspend();
        overlay.saveCoins();overlay.game.cancelTouch();
        setRoundPaused(true);overlay.game.stopRound();handledResult=true;
        Runnable start=()->{if(requestedSession==gameGeneration)startGameSession(mode,stage,carriedCoins);};
        if(encoder!=null)stopVideo(start);else start.run();
    }
    private void startGameSession(int mode,int stage,int carriedCoins) {
        stopAnimalGuide();
        stopCountrySpeech();stopMicrophone();stopFaceRecognition();
        nextCountrySpeech=0;lastFaceResult=0;
        awaitingStart=false;paused = false; overlay.select(mode,stage,carriedCoins); overlay.setVisibility(View.VISIBLE);
        playMenu.setVisibility(View.VISIBLE);
        boolean illustrated = true;
        overlay.setOnClickListener(null);
        overlay.setOnTouchListener((view,event)->{
            int action=event.getActionMasked();
            if(mode==9&&overlay.animalGuideAt(event.getX(),event.getY())){if(action==android.view.MotionEvent.ACTION_UP){hearCurrentAnimal();view.performClick();}return true;}
            if(mode==8){if(action==android.view.MotionEvent.ACTION_UP)view.performClick();return true;}
            if(mode==14){if(action==android.view.MotionEvent.ACTION_UP&&event.getY()>overlay.getHeight()*.55f){showFlagAnswerOptions();view.performClick();}return true;}
            if(mode==10){if(action==android.view.MotionEvent.ACTION_UP){int choice=overlay.memoryActionAt(event.getX(),event.getY());if(choice>=0)overlay.input(choice);view.performClick();}return true;}
            if(mode==13){if(action==android.view.MotionEvent.ACTION_DOWN||action==android.view.MotionEvent.ACTION_MOVE)overlay.game.touchLane(overlay.headTouchFraction(event.getX()));if(action==android.view.MotionEvent.ACTION_UP)view.performClick();overlay.invalidate();return true;}
            if(action==android.view.MotionEvent.ACTION_DOWN)overlay.game.touchDown();
            else if(action==android.view.MotionEvent.ACTION_UP){overlay.game.touchUp();view.performClick();}
            else if(action==android.view.MotionEvent.ACTION_CANCEL)overlay.game.cancelTouch();
            overlay.invalidate();return true;
        });
        overlay.game.touchSoundPatterns=mode==9;
        if(mode>=12)overlay.setContentDescription(GameCatalog.ALL[mode].controlBrief());
        else if(mode==9)overlay.setContentDescription("Animal Choir: match short and long presses in touch mode, or sound rhythms with the microphone. This checks timing, not animal identity.");
        else if(mode==10)overlay.setContentDescription("Face Memory: watch each expression, then repeat it with your face or use illustrated touch cards when face mode is off.");
        else if(mode==11)overlay.setContentDescription("Firefly Glow: hum to fly toward each lantern. Keep your sound in the gold range to light it and earn a coin. Microphone required; touching does not move the firefly.");
        else if(mode==8)overlay.setContentDescription("Comet Launch: sound only. Hum gently to charge, then go quiet in the gold zone. Watch the rocket land. Screen touches cannot charge or launch.");
        else if(mode==1||mode==4)overlay.setContentDescription("Jump controls: quick tap for low jumps; hold briefly and release for higher jumps. Optional microphone: sound volume controls height.");
        else if(mode==5)overlay.setContentDescription("Star Duet: tap to simulate a closed-lip smile, then hum gently. When microphone mode is off, tap to simulate the sound too.");
        else if(mode!=3)overlay.setContentDescription(illustrated ? "Follow the requested facial expression, or turn off face mode to use touch." : "Game overlay");
        actions.removeAllViews();
        LinearLayout inputs = new LinearLayout(this);
        String[] labels = overlay.game.controls();
        for (int i = 0; i < labels.length; i++) {
            final int action = i;
            addButton(inputs, labels[i], () -> overlay.input(action));
        }
        actions.addView(inputs);
        LinearLayout navigation = new LinearLayout(this);
        addButton(navigation, "Pause / Resume", () -> setRoundPaused(!paused));
        addButton(navigation, "Restart", () -> retryRound());
        addButton(navigation, "Close game", () -> closeGame());
        if(mode!=1&&mode!=10)actions.addView(navigation);
        if(mode==10){for(int i=0;i<inputs.getChildCount();i++){Button button=(Button)inputs.getChildAt(i);button.setAllCaps(false);button.setTextColor(0xFF334966);button.setBackground(cardBackground(new int[]{0xFFFFD5B0,0xFFD0EBD9,0xFFE0D0F6}[i],Color.WHITE));button.setBackgroundTintList(null);button.setMinHeight(dp(52));}}
        actions.setVisibility(illustrated ? View.GONE : View.VISIBLE);
        handledResult=false;
        useVoiceMicrophone=MiniGame.supportsMicrophone(mode)&&soundChoice(mode);
        overlay.game.microphoneMode=mode==14?quizVoice:MiniGame.supportsMicrophone(mode)&&useVoiceMicrophone;
        overlay.game.faceMode=MiniGame.supportsFace(mode)&&faceChoice;
        if(mode==14&&!quizVoice)overlay.game.quizStatus="Microphone off. Tap the card to type.";
        if(recordSwitch.isChecked()){setRoundPaused(true);beginVideo(()->setRoundPaused(false));}
        else setRoundPaused(false);
        updateCaptureControls();
    }
    private void showAppOptions(){
        boolean wasPaused=paused;setRoundPaused(true);boolean[] selected={false};
        Dialog menu=new Dialog(this);menu.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        AppOptionsPanel panel=new AppOptionsPanel(this,new AppOptionsPanel.Actions(){
            public void close(){menu.dismiss();}
            public void select(int which){
                selected[0]=true;menu.dismiss();setRoundPaused(wasPaused);
                if(which==0){if(awaitingStart)showGameBrief(GameCatalog.ALL[overlay.game.mode],null);else if(gameVisible())showPlayMenu();else showGames();}
                else if(which==1)showRecordings();else if(which==2)showRating(null);else if(which==4)showLanguage();else if(which==5)showPrivacyPolicy();
                else{setRoundPaused(true);AlertDialog info=new AlertDialog.Builder(MainActivity.this).setTitle(AppText.t(MainActivity.this,"Your videos")).setMessage(getString(R.string.fc_storage_info)).setPositiveButton(AppText.t(MainActivity.this,"Got it"),null).create();info.setOnDismissListener(x->{if(resumed)setRoundPaused(wasPaused);});info.show();}
            }
        });
        ScrollView scroll=new ScrollView(this){@Override protected void onMeasure(int width,int height){super.onMeasure(width,View.MeasureSpec.makeMeasureSpec((int)(getResources().getDisplayMetrics().heightPixels*.86f),View.MeasureSpec.AT_MOST));}};scroll.setFillViewport(false);scroll.setClipToPadding(false);scroll.addView(panel);scroll.setBackgroundColor(Color.TRANSPARENT);menu.setContentView(scroll);
        menu.setOnDismissListener(d->{if(!selected[0]&&resumed)setRoundPaused(wasPaused);});menu.show();
        android.view.Window window=menu.getWindow();if(window!=null){window.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));window.addFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND);window.setDimAmount(.48f);window.setGravity(Gravity.BOTTOM);int width=Math.min(getResources().getDisplayMetrics().widthPixels,dp(560));window.setLayout(width,android.view.ViewGroup.LayoutParams.WRAP_CONTENT);}
    }
    private void showPrivacyPolicy(){
        setRoundPaused(true);
        try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://topreach.github.io/facechaos/privacy.html")));}
        catch(android.content.ActivityNotFoundException error){new AlertDialog.Builder(this).setTitle(R.string.fc_privacy).setMessage("https://topreach.github.io/facechaos/privacy.html").setPositiveButton(android.R.string.ok,null).show();}
    }
    private void showLanguage(){
        boolean wasPaused=paused;setRoundPaused(true);
        if(encoder!=null){Toast.makeText(this,getString(R.string.fc_language_notice),Toast.LENGTH_LONG).show();setRoundPaused(wasPaused);return;}
        String[] names=AppLanguage.NAMES.clone();names[0]=getString(R.string.fc_system);int checked=0;String code=AppLanguage.current(this);for(int i=0;i<AppLanguage.CODES.length;i++)if(AppLanguage.CODES[i].equals(code))checked=i;
        boolean[] confirming={false};AlertDialog picker=new AlertDialog.Builder(this).setTitle(R.string.fc_language).setSingleChoiceItems(names,checked,(dialog,index)->{
            String target=AppLanguage.CODES[index];if(target.equals(AppLanguage.current(this))){dialog.dismiss();return;}confirming[0]=true;dialog.dismiss();
            AlertDialog confirm=new AlertDialog.Builder(this).setTitle(R.string.fc_language).setMessage(R.string.fc_language_notice).setPositiveButton(android.R.string.ok,(d,w)->AppLanguage.set(this,target)).setNegativeButton(android.R.string.cancel,null).create();confirm.setOnDismissListener(d->{if(resumed)setRoundPaused(wasPaused);});confirm.show();
        }).setNegativeButton(android.R.string.cancel,null).create();picker.setOnDismissListener(d->{if(!confirming[0]&&resumed)setRoundPaused(wasPaused);});picker.show();
    }
    private void showRecordings(){
        if(encoder!=null){Toast.makeText(this,"Finish recording before opening your clips",Toast.LENGTH_LONG).show();return;}
        File folder=getExternalFilesDir(Environment.DIRECTORY_MOVIES);File[] clips=folder==null?null:folder.listFiles(f->f.isFile()&&ClipSharing.validName(f.getName()));
        boolean wasPaused=paused;setRoundPaused(true);
        if(clips==null||clips.length==0){AlertDialog empty=new AlertDialog.Builder(this).setTitle(AppText.t(MainActivity.this,"My recordings")).setMessage(getString(R.string.fc_no_clips)).setPositiveButton(AppText.t(MainActivity.this,"Done"),null).create();empty.setOnDismissListener(d->{if(resumed)setRoundPaused(wasPaused);});empty.show();return;}
        java.util.Arrays.sort(clips,(a,b)->Long.compare(b.lastModified(),a.lastModified()));String[] labels=new String[clips.length];
        java.text.DateFormat date=java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.MEDIUM,java.text.DateFormat.SHORT);
        for(int i=0;i<clips.length;i++)labels[i]=date.format(new java.util.Date(clips[i].lastModified()))+" - "+String.format(Locale.US,"%.1f MB",clips[i].length()/1048576f);
        boolean[] selected={false};AlertDialog library=new AlertDialog.Builder(this).setTitle(AppText.t(MainActivity.this,"My recordings - newest first")).setItems(labels,(d,index)->{selected[0]=true;setRoundPaused(wasPaused);showClip(clips[index]);}).setNegativeButton(AppText.t(MainActivity.this,"Close"),null).create();library.setOnDismissListener(d->{if(!selected[0]&&resumed)setRoundPaused(wasPaused);});library.show();
    }
    private void showRating(Runnable after){
        if(encoder!=null){Toast.makeText(this,"Finish recording before rating",Toast.LENGTH_LONG).show();return;}
        boolean wasPaused=paused;setRoundPaused(true);android.content.SharedPreferences prefs=getSharedPreferences("ratings",0);prefs.edit().putLong("last_prompt",System.currentTimeMillis()).apply();
        RatingPanel body=new RatingPanel(this);android.widget.RatingBar stars=body.stars;ScrollView ratingScroll=new ScrollView(this);ratingScroll.addView(body);
        AlertDialog dialog=new AlertDialog.Builder(this,android.R.style.Theme_Material_Light_Dialog_Alert).setTitle(AppText.t(MainActivity.this,"Rate Face Chaos")).setView(ratingScroll).setPositiveButton(AppText.t(MainActivity.this,"Save rating"),null).setNegativeButton(AppText.t(MainActivity.this,"Not now"),null).setNeutralButton(AppText.t(MainActivity.this,"Don't ask again"),(d,w)->prefs.edit().putBoolean("disabled",true).apply()).create();
        dialog.setOnDismissListener(d->{if(resumed){setRoundPaused(wasPaused);if(after!=null)after.run();}});
        dialog.setOnShowListener(d->{Button save=dialog.getButton(AlertDialog.BUTTON_POSITIVE);save.setEnabled(false);stars.setOnRatingBarChangeListener((bar,value,user)->save.setEnabled(RatingPolicy.valid(value)));save.setOnClickListener(v->{float rating=stars.getRating();if(!RatingPolicy.valid(rating))return;prefs.edit().putInt("stars",(int)rating).putLong("rated_at",System.currentTimeMillis()).apply();Toast.makeText(this,getString(R.string.fc_rating_thanks),Toast.LENGTH_LONG).show();dialog.dismiss();});});dialog.show();
    }
    private void showPlayMenu() {
        boolean wasPaused = paused;
        setRoundPaused(true);
        boolean[] handled = {false};
        AlertDialog menu = new AlertDialog.Builder(this).setTitle(AppText.t(MainActivity.this,"Game options"))
            .setItems(AppText.items(MainActivity.this,new String[]{wasPaused ? "Resume" : "Pause", "Restart from Stage 1", "Input method", "Stages", "Sounds", "Choose another game", "Face / head check"}), (dialog, choice) -> {
                handled[0] = true;
                if(choice == 0) { setRoundPaused(!wasPaused); }
                else if(choice == 1) { retryRound(); }
                else if(choice == 3)showStages(wasPaused);
                else if(choice == 4)showSounds(wasPaused);
                else if(choice == 5) closeGame();
                else if(choice == 6) showFaceCheck(wasPaused);
                else showTouchControls(wasPaused);
            }).setNegativeButton(AppText.t(MainActivity.this,"Back"), null).create();
        menu.setOnDismissListener(dialog -> { if(!handled[0]) { setRoundPaused(wasPaused); } });
        menu.show();
    }
    private void showStages(boolean wasPaused){
        int mode=overlay.game.mode;StringBuilder description=new StringBuilder("Clear stages in order. If a run fails, Try again starts at Stage 1.\n\n");
        for(int i=1;i<=MiniGame.STAGE_COUNT;i++)description.append("Stage ").append(i).append(" - ").append(new MiniGame(mode,0,i).stageName()).append("\n");
        AlertDialog stages=new AlertDialog.Builder(this).setTitle(AppText.t(MainActivity.this,"Your game journey")).setMessage(description.toString()).setPositiveButton(AppText.t(MainActivity.this,"Back"),null).create();
        stages.setOnDismissListener(dialog->setRoundPaused(wasPaused));stages.show();
    }

    private void showSounds(boolean wasPaused){
        LinearLayout body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(dp(20),dp(8),dp(20),dp(8));
        Switch effects=new Switch(this);effects.setText(AppText.t(MainActivity.this,"Game sound effects"));effects.setChecked(gameAudio.effects());body.addView(effects);
        Switch music=new Switch(this);music.setText(AppText.t(MainActivity.this,"Background music"));music.setChecked(gameAudio.music());body.addView(music);
        effects.setOnCheckedChangeListener((button,value)->gameAudio.settings(value,music.isChecked()));music.setOnCheckedChangeListener((button,value)->gameAudio.settings(effects.isChecked(),value));
        Button previewSound=new Button(this);previewSound.setText(AppText.t(MainActivity.this,"Preview game sounds"));previewSound.setOnClickListener(v->gameAudio.preview());body.addView(previewSound);
        body.addView(sheetText("Speaker effects and music pause during live microphone rounds to avoid triggering sound controls. Result jingles still play. Recorded clips remain silent.",14,0xFF526765));
        AlertDialog sound=new AlertDialog.Builder(this,android.R.style.Theme_Material_Light_Dialog_Alert).setTitle(AppText.t(MainActivity.this,"Sounds")).setView(body).setPositiveButton(AppText.t(MainActivity.this,"Done"),null).create();sound.setOnDismissListener(dialog->{if(resumed)setRoundPaused(wasPaused);});sound.show();
    }
    private void showTouchControls(boolean wasPaused) {
        if(overlay.game.mode==14){setRoundPaused(false);showFlagAnswerOptions();return;}
        LinearLayout body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(dp(20),dp(12),dp(20),dp(12));
        Switch faces=new Switch(this);faces.setText(AppText.t(this,overlay.game.mode==13?"Head steering":"Face controls (beta)"));faces.setChecked(overlay.game.faceMode);faces.setVisibility(MiniGame.supportsFace(overlay.game.mode)?View.VISIBLE:View.GONE);body.addView(faces);
        Switch sound=new Switch(this);sound.setText(AppText.t(MainActivity.this,"Sound controls (optional microphone)"));sound.setChecked(overlay.game.microphoneMode);if(MiniGame.requiresMicrophone(overlay.game.mode)){sound.setChecked(true);sound.setEnabled(false);sound.setText(AppText.t(MainActivity.this,"Microphone required - sound controls only"));}sound.setVisibility(MiniGame.supportsMicrophone(overlay.game.mode)?View.VISIBLE:View.GONE);body.addView(sound);
        Switch reverse=new Switch(this);reverse.setText(AppText.t(MainActivity.this,"Reverse head steering"));reverse.setChecked(reverseHead);reverse.setVisibility(overlay.game.mode==13?View.VISIBLE:View.GONE);body.addView(reverse);
        body.addView(sheetText(overlay.game.mode==11?"Hum gently to guide the firefly toward each lantern. Keep the sound in the gold range, and go quiet to rest. Touches do not control flight. If microphone access is unavailable, choose another game.":overlay.game.mode==8?"Comet Launch is sound-only. Hum gently to charge, go quiet in gold to launch, then watch the landing. No screen hold or release controls.":overlay.game.mode==13?"Switch head steering off for touch-only lanes. Reverse steering if the car moves opposite to your head. Your stage, score and coins stay the same.":overlay.game.mode==12?"Sound Grand Prix remembers its own microphone setting. Stay quiet for the first second, then hum or say aaa: soft sound gives less acceleration, stronger comfortable sound gives more. Switch sound off only to use touch acceleration.":overlay.game.mode==3?"Use your voice: hum or say aaa to rise, go quiet to descend. Switch sound off for touch control. Voice Runner remembers this choice separately; calibration pauses flight.":"Switch face and sound off for touch-only play. Your stage, score, hearts and coins stay the same. Face mode pauses action if tracking is lost. Teeth detection is experimental.",14,0xFF52736B));
        AlertDialog controls=new AlertDialog.Builder(this,android.R.style.Theme_Material_Light_Dialog_Alert).setTitle(AppText.t(MainActivity.this,"Choose how to play")).setView(body).setPositiveButton(AppText.t(MainActivity.this,"Apply"),(dialog,which)->{
            reverseHead=reverse.isChecked();getSharedPreferences("controls",0).edit().putBoolean("reverse_head",reverseHead).apply();
            stopFaceRecognition();if(MiniGame.supportsFace(overlay.game.mode)){faceChoice=faces.isChecked();getSharedPreferences("controls",0).edit().putBoolean("face",faceChoice).apply();}overlay.game.faceMode=faceChoice&&MiniGame.supportsFace(overlay.game.mode);overlay.game.faceReady=false;
            inputSwitchPaused=wasPaused;
            if(sound.isChecked()&&checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){pendingInputSwitch=true;requestGamePermission(new String[]{Manifest.permission.RECORD_AUDIO},11);return;}
            applyInputMode(sound.isChecked(),wasPaused);
        }).setNegativeButton(AppText.t(MainActivity.this,"Back"),(d,w)->setRoundPaused(wasPaused)).create();
        controls.setOnCancelListener(d->setRoundPaused(wasPaused));controls.show();
    }
    private void showFaceCheck(boolean wasPaused){
        faceLab=true;overlay.setFaceCheckOpen(true);java.util.Arrays.fill(faceTrialCounts,0);
        faceLabAction=FaceGameInput.expected(overlay.game);
        Dialog check=new Dialog(this);
        LinearLayout shell=new LinearLayout(this);shell.setOrientation(LinearLayout.VERTICAL);shell.setPadding(dp(12),dp(8),dp(12),dp(8));shell.setBackground(cardBackground(0xF5F9F9F0,0xFF335C5F));
        ScrollView scroll=new ScrollView(this);LinearLayout body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);
        TextView title=sheetText(overlay.game.mode==13?"Head calibration":"Face calibration",14,0xFF173E42);title.setTypeface(null,android.graphics.Typeface.BOLD);body.addView(title);
        if(overlay.game.mode!=13){
            android.widget.Spinner reaction=new android.widget.Spinner(this);
            reaction.setAdapter(new android.widget.ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"Test all reactions","Open mouth","Closed-lip smile","Show teeth","Blink","Eyes wide open","Face squeeze"}){
                @Override public View getView(int position,View recycled,android.view.ViewGroup parent){View row=super.getView(position,recycled,parent);((TextView)row).setTextColor(0xFF173E42);return row;}
                @Override public View getDropDownView(int position,View recycled,android.view.ViewGroup parent){View row=super.getDropDownView(position,recycled,parent);((TextView)row).setTextColor(0xFF173E42);row.setBackgroundColor(0xFFF9F9F0);return row;}
            });
            reaction.setSelection(faceLabAction+1);reaction.setContentDescription("Expression to test");
            reaction.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onNothingSelected(android.widget.AdapterView<?> parent){}public void onItemSelected(android.widget.AdapterView<?> parent,View view,int position,long id){faceLabAction=position-1;}});
            body.addView(reaction,new LinearLayout.LayoutParams(-1,dp(44)));
        }
        faceCheckText=sheetText("Loading face detector...",13,0xFF214F67);faceCheckText.setMinLines(2);body.addView(faceCheckText);
        body.addView(sheetText(overlay.game.mode==13?"Look comfortably centred, then move left and right.":"Relax briefly, then try one expression at a time. Return to neutral between reactions.",12,0xFF52736B));
        TextView sensitivity=sheetText("Sensitivity: "+Math.round(100/faceSensitivity)+"%",12,0xFF52736B);body.addView(sensitivity);
        android.widget.SeekBar slider=new android.widget.SeekBar(this);slider.setMax(60);slider.setProgress(Math.round((1.3f-faceSensitivity)*100));body.addView(slider,new LinearLayout.LayoutParams(-1,dp(36)));
        slider.setOnSeekBarChangeListener(new android.widget.SeekBar.OnSeekBarChangeListener(){public void onStartTrackingTouch(android.widget.SeekBar b){}public void onStopTrackingTouch(android.widget.SeekBar b){}public void onProgressChanged(android.widget.SeekBar b,int value,boolean user){faceSensitivity=1.3f-value/100f;if(faceRecognition!=null)faceRecognition.sensitivity=faceSensitivity;sensitivity.setText(AppText.t(MainActivity.this,"Sensitivity: ")+Math.round(100/faceSensitivity)+"%");getSharedPreferences("controls",0).edit().putFloat("face_sensitivity",faceSensitivity).apply();}});
        scroll.addView(body);shell.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout buttons=new LinearLayout(this);addButton(buttons,"Recalibrate",()->{java.util.Arrays.fill(faceTrialCounts,0);overlay.game.faceReady=false;if(faceRecognition!=null)faceRecognition.recalibrate();else startFaceRecognition();});addButton(buttons,"Done",check::dismiss);shell.addView(buttons,new LinearLayout.LayoutParams(-1,dp(44)));
        check.setContentView(shell);check.setOnDismissListener(d->{faceLab=false;faceCheckText=null;overlay.setFaceCheckOpen(false);stopFaceRecognition();setRoundPaused(wasPaused);});check.show();
        check.getWindow().setBackgroundDrawableResource(android.R.color.transparent);check.getWindow().setGravity(Gravity.BOTTOM);check.getWindow().setDimAmount(0);check.getWindow().setLayout(-1,Math.round(getResources().getDisplayMetrics().heightPixels*.32f));startFaceRecognition();
    }
    private void startFaceRecognition(){
        if(faceRecognition!=null||!resumed)return;
        if(checkSelfPermission(Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED){if(faceCheckText!=null)faceCheckText.setText(AppText.t(MainActivity.this,"Enable the camera to test facial controls. Touch play remains available."));overlay.game.faceMode=false;return;}
        final int generation=++faceGeneration;lastFaceResult=SystemClock.uptimeMillis();overlay.game.faceReady=false;overlay.game.faceStatus="Relax your face to calibrate";
        faceRecognition=new FaceRecognition(this,new FaceRecognition.Listener(){
            public void bounds(float left,float top,float right,float bottom){ui.post(()->{if(generation==faceGeneration&&resumed)overlay.setFaceBottom(bottom);});}
            public void result(boolean ready,String state,int action,String measurements){ui.post(()->{
                if(generation!=faceGeneration||!resumed)return;lastFaceResult=SystemClock.uptimeMillis();if(overlay.game.mode==13)return;
                if(faceLab&&action>=0&&action<faceTrialCounts.length)faceTrialCounts[action]++;
                if(faceCheckText!=null)faceCheckText.setText(state+"\nMouth "+faceTrialCounts[0]+"   Smile "+faceTrialCounts[1]+"   Teeth "+faceTrialCounts[2]+"   Blink "+faceTrialCounts[3]+"\nWide eyes "+faceTrialCounts[4]+"   Face squeeze "+faceTrialCounts[5]+"\n"+measurements);
                if(faceLab||overlay.game.mode==13)return;
                if(!camera.hasFreshAnalysisFrame()){overlay.game.faceReady=false;overlay.game.faceStatus="Camera paused - reconnect or use touch";overlay.invalidate();return;}
                overlay.game.faceReady=ready;overlay.game.faceStatus=state;
                if(action>=0)FaceGameInput.send(overlay.game,action);overlay.invalidate();
            });}
            public void head(boolean ready,int lane,float offset,String state){ui.post(()->{
                if(generation!=faceGeneration||!resumed||overlay.game.mode!=13)return;
                lastFaceResult=SystemClock.uptimeMillis();int direction=reverseHead?-lane:lane;
                if(faceLab){if(faceCheckText!=null)faceCheckText.setText(state+String.format(java.util.Locale.US,"\nHead offset %.2f - %s",offset,direction<0?"LEFT":direction>0?"RIGHT":"MIDDLE"));return;}
                if(!camera.hasFreshAnalysisFrame())readyHeadLost();
                else{if(ready&&!overlay.game.faceReady&&overlay.game.countdown<=0)overlay.game.headGrace=.6f;overlay.game.faceReady=ready;overlay.game.headLane=direction;overlay.game.faceStatus=ready?(direction<0?"Steering LEFT":direction>0?"Steering RIGHT":"Steering MIDDLE"):state;overlay.invalidate();}
            });}
            public void failed(){ui.post(()->{if(generation!=faceGeneration)return;stopFaceRecognition();overlay.game.faceMode=false;overlay.game.faceReady=false;setRoundPaused(true);if(faceCheckText!=null)faceCheckText.setText(AppText.t(MainActivity.this,"Face detector unavailable. Touch controls remain available."));Toast.makeText(MainActivity.this,"Face detector unavailable - resume for touch controls",Toast.LENGTH_LONG).show();});}
        },overlay.game.mode==13);faceRecognition.sensitivity=faceSensitivity;ui.removeCallbacks(faceFrame);ui.post(faceFrame);
    }
    private void readyHeadLost(){overlay.game.faceReady=false;overlay.game.faceStatus="Head camera interrupted - reconnect or use touch";overlay.invalidate();}
    private void stopFaceRecognition(){faceGeneration++;ui.removeCallbacks(faceFrame);if(faceRecognition!=null){faceRecognition.close();faceRecognition=null;}if(overlay!=null){overlay.game.faceReady=false;overlay.setFaceBottom(-1);}}
    private void applyInputMode(boolean microphoneMode,boolean stayPaused){
        if(MiniGame.requiresMicrophone(overlay.game.mode))microphoneMode=true;
        stopMicrophone();overlay.game.cancelTouch();
        if(MiniGame.supportsMicrophone(overlay.game.mode)){useVoiceMicrophone=microphoneMode;rememberSoundChoice(overlay.game.mode,microphoneMode);}
        overlay.game.microphoneMode=MiniGame.supportsMicrophone(overlay.game.mode)&&microphoneMode;
        overlay.game.soundPulses=0;overlay.game.soundDuration=0;overlay.game.soundQuiet=0;overlay.game.soundArmed=false;overlay.game.jumpSampling=false;overlay.game.jumpArmed=false;
        setRoundPaused(stayPaused||!resumed);overlay.invalidate();
        Toast.makeText(this,(overlay.game.faceMode?"Face controls on; ":"Touch controls on; ")+(microphoneMode?"sound on":"microphone off"),Toast.LENGTH_SHORT).show();
    }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
    private TextView sheetText(String text, int size, int color) {
        TextView label = new TextView(this); label.setText(AppText.t(this,text)); label.setTextSize(size);
        label.setTextColor(color); label.setPadding(dp(8), dp(8), dp(8), dp(8)); return label;
    }
    private void showGames() {
        if(activeRound()){confirmGameSwitch();return;}
        final MiniGame owner=overlay.game;
        boolean wasPaused = paused;
        setRoundPaused(true);
        Dialog picker = new Dialog(this);
        LinearLayout sheet = new LinearLayout(this); sheet.setOrientation(LinearLayout.VERTICAL);
        sheet.setPadding(dp(16), dp(12), dp(16), dp(24));
        GradientDrawable background = new GradientDrawable(); background.setColor(0xFF173E42);
        background.setCornerRadii(new float[]{dp(24),dp(24),dp(24),dp(24),0,0,0,0});
        sheet.setBackground(background);
        sheet.addView(sheetText("Choose your game", 25, Color.WHITE));
        sheet.addView(sheetText("Face, head and sound - "+GameCatalog.ALL.length+" games. New flag game first.", 13, 0xFFB9DCCE));
        ScrollView scroll = new ScrollView(this);
        LinearLayout cards = new LinearLayout(this); cards.setOrientation(LinearLayout.VERTICAL);
        for(int choice=0;choice<GameCatalog.ALL.length;choice++){
            final GameCatalog.Entry entry=GameCatalog.ALL[choice==0?14:choice-1];
            Button card = new Button(this); card.setAllCaps(false);
            GradientDrawable tile = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{entry.color, 0xFFF6F6EE}); tile.setCornerRadius(dp(20));
            card.setBackground(tile); card.setBackgroundTintList(null); card.setTextColor(0xFF173E42); card.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            card.setText(AppText.english(this)?(entry.id==14?"NEW  ":"")+entry.name + "\n" + entry.input + " · " + entry.description + "\n" + (entry.id==14?"View instructions - spoken or typed country names":MiniGame.requiresMicrophone(entry.id)?"View instructions - microphone required":MiniGame.supportsMicrophone(entry.id)?"View instructions - optional microphone":"View instructions - camera or touch"):entry.name+"\n"+AppText.shortBrief(this,entry.id));
            card.setTextSize(15); card.setPadding(dp(16),dp(12),dp(16),dp(12));
            card.setContentDescription(entry.name+". "+AppText.brief(this,entry.id));
            card.setOnClickListener(v -> showGameBrief(entry, picker));
            LinearLayout.LayoutParams tileLayout = new LinearLayout.LayoutParams(-1,dp(112));
            tileLayout.bottomMargin = dp(12); cards.addView(card,tileLayout);
        }
        scroll.addView(cards);
        sheet.addView(scroll, new LinearLayout.LayoutParams(-1,0,1));
        Button clear = new Button(this); clear.setText(AppText.t(MainActivity.this,"Back to play")); clear.setAllCaps(false);
        clear.setOnClickListener(v -> { picker.dismiss(); }); sheet.addView(clear);
        picker.setContentView(sheet);
        picker.setOnDismissListener(dialog -> { if(overlay.game==owner)setRoundPaused(wasPaused); });
        picker.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        picker.getWindow().setGravity(Gravity.BOTTOM);
        picker.getWindow().setDimAmount(.15f);
        picker.getWindow().setLayout(-1, Math.round(getResources().getDisplayMetrics().heightPixels * .68f));
        picker.show();
        picker.getWindow().setLayout(-1, Math.round(getResources().getDisplayMetrics().heightPixels * .68f));
    }
    private GradientDrawable cardBackground(int color,int stroke){
        GradientDrawable bg=new GradientDrawable();bg.setColor(color);bg.setCornerRadius(dp(22));bg.setStroke(dp(2),stroke);return bg;
    }
    private void tutorialStep(LinearLayout body,String number,String title,String copy,int color){tutorialStep(body,number,title,copy,color,-1);}
    private void tutorialStep(LinearLayout body,String number,String title,String copy,int color,int escapeAction){
        LinearLayout card=new LinearLayout(this);card.setPadding(dp(12),dp(12),dp(12),dp(12));
        card.setBackground(cardBackground(Color.WHITE,0xFFD5E7DF));
        TextView badge=sheetText(number,22,0xFF173E42);badge.setGravity(Gravity.CENTER);badge.setTypeface(null,android.graphics.Typeface.BOLD);
        badge.setBackground(cardBackground(color,0xFF173E42));card.addView(badge,new LinearLayout.LayoutParams(dp(48),dp(48)));
        LinearLayout words=new LinearLayout(this);words.setOrientation(LinearLayout.VERTICAL);
        TextView heading=sheetText(title,18,0xFF173E42);heading.setTypeface(null,android.graphics.Typeface.BOLD);words.addView(heading);
        words.addView(sheetText(copy,15,0xFF45605C));
        if(escapeAction>=0){int tutorialMode=escapeAction<3?1:escapeAction<6?3:escapeAction<9?5:escapeAction<12?6:escapeAction<15?7:escapeAction<18?8:escapeAction<21?9:escapeAction<24?10:11;words.addView(new TutorialArt(this,tutorialMode,escapeAction%3),new LinearLayout.LayoutParams(-1,dp(112)));}
        card.addView(words,new LinearLayout.LayoutParams(0,-2,1));
        LinearLayout.LayoutParams spacing=new LinearLayout.LayoutParams(-1,-2);spacing.topMargin=dp(10);body.addView(card,spacing);
    }
    private void showGameBrief(GameCatalog.Entry entry, Dialog picker) {
        final MiniGame owner=overlay.game;
        boolean wasPaused=paused;setRoundPaused(true);
        MiniGame stageGame=new MiniGame(entry.id,0,1);String stageRules=AppText.english(this)?StageRules.describe(stageGame):AppText.summary(this,stageGame);
        Dialog brief=new Dialog(this);
        LinearLayout shell=new LinearLayout(this);shell.setOrientation(LinearLayout.VERTICAL);shell.setPadding(dp(14),dp(12),dp(14),dp(12));
        shell.setBackground(cardBackground(0xFFFFFBF0,0xFF173E42));
        TextView title=sheetText(entry.name,25,0xFF173E42);title.setTypeface(null,android.graphics.Typeface.BOLD);shell.addView(title);
        TextView subtitle=sheetText(AppText.english(this)?entry.description:getString(R.string.fc_instructions),14,0xFF52736B);shell.addView(subtitle);
        shell.addView(sheetText(AppText.summary(this,stageGame),15,0xFF6F5884));
        shell.addView(sheetText(getString(R.string.fc_run_best,0,RunRecords.best(getSharedPreferences("games",0),entry.id)),13,0xFF52736B));
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(false);
        LinearLayout body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);
        body.addView(new TutorialArt(this,entry.id,-1,stageGame.stage),new LinearLayout.LayoutParams(-1,dp(160)));
        body.addView(sheetText(stageRules+(AppText.english(this)?" Progress saves after each completed stage.":""),14,0xFF52736B));
        if(AppText.english(this)&&GameSections.applies(entry.id)){body.addView(sheetText(GameSections.at(entry.id,stageGame.stage).brief,16,0xFF476B64));StringBuilder route=new StringBuilder("Your five sections\n");for(int section=1;section<=5;section++)route.append(section==stageGame.stage?"? ":"").append(section).append(". ").append(GameSections.at(entry.id,section).name).append(section==5?"":"\n");body.addView(sheetText(route.toString(),14,0xFF6B5682));}
        if(AppText.english(this)){
        if(entry.id==14){
            tutorialStep(body,"1","See the flag","A country flag arrives below your face. The answer stays hidden. Watch the time bar and name the country in English.",entry.color);
            tutorialStep(body,"2","Say the country name","Wait for Listening, then say one country clearly. A correct answer shows Good job!, earns one coin and brings the next flag. Tap the card to retry speech or type.",0xFFFFE5A4);
            tutorialStep(body,"3","Travel through five stages","Later stages introduce more countries and shorten the answer time. Wrong answers or timeouts cost a life. The last stage has one life.",0xFFD8D4F6);
        }else if(entry.id==12){
            tutorialStep(body,"1","Your sound is the accelerator","A soft sound gives less power. A stronger comfortable sound gives more speed. Quiet lets your car coast; no shouting is required.",entry.color);
            tutorialStep(body,"2","Beat the computer","The coral CPU car changes its pace. Watch both progress bars and cross the finish first. You earn a coin at every quarter of the track.",0xFFFFD4BD);
            tutorialStep(body,"3","Race your way","Sound controls start enabled. Stay quiet for the first second to calibrate, then make a comfortable hum to accelerate. Without a microphone, hold to accelerate and release to coast. Each stage is faster and the CPU gets stronger.",0xFFCDEAD8);
        }else if(entry.id==13){
            tutorialStep(body,"1","Calibrate your centred head","Look at the front camera and hold your head comfortably centred. Head steering has its own calibration; you do not need to smile or open your mouth.",entry.color);
            tutorialStep(body,"2","Move toward the clear lane","Move your head left or right to dodge oncoming cars. Return to centre for the middle lane. Follow the clear-lane popup above your head.",0xFFD4F0E8);
            tutorialStep(body,"3","Survive the traffic","Later stages bring faster cars and two blocked lanes. Camera tracking loss freezes traffic; a short recovery pause lets you steer when tracking returns. Turn head mode off for touch steering.",0xFFFFDDBC);
        }else if(entry.id==0){
            tutorialStep(body,"1","Open mouth","Open your mouth comfortably without showing teeth, then relax.",entry.color);
            tutorialStep(body,"2","Closed-lip smile","Smile with your lips together, then return to neutral.",0xFFBFE9DF);
            tutorialStep(body,"3","Show teeth","Make your front teeth visible. Either tooth row counts. Relax afterwards.",0xFFE4D4FF);
            tutorialStep(body,"4","Eyes wide open","Open both eyes wide while keeping your mouth relaxed. Hold briefly, then relax.",0xFFBFE9DF);
            body.addView(new TutorialArt(this,0,3),new LinearLayout.LayoutParams(-1,dp(112)));
            tutorialStep(body,"5","Face squeeze","Gently scrunch your face: squeeze both eyes shut and bunch your cheeks or nose for a brief moment, then relax. A normal blink does not count. Keep it comfortable.",0xFFFFDDBC);
            body.addView(new TutorialArt(this,0,4),new LinearLayout.LayoutParams(-1,dp(112)));
        }else if(entry.id==1){
            body.addView(sheetText("Follow each face popup. Every correct response earns 1 coin. "+(stageGame.stage==5?"Final challenge: one life.":"You have 3 lives."),15,0xFF713E2B));
            tutorialStep(body,"1","Sound-powered jumps","Make a short soft sound for low hurdles, a stronger comfortable sound for tall hurdles. Wait for the highlighted window; go quiet between jumps. Without a microphone, tap for low jumps or hold briefly and release for high jumps during the window.",entry.color,0);
            tutorialStep(body,"2","Smile with lips closed","When the smile popup appears, smile with lips together to block the incoming projectiles. Face mode detects this expression; with face mode off, tap when the popup lights up.",0xFFBFE9DF,1);
            tutorialStep(body,"3","Show your teeth","When the teeth popup appears, show your teeth comfortably to bite the biscuit. Face mode detects this expression; with face mode off, tap when the popup lights up.",0xFFE4D4FF,2);
        }else if(entry.id==3){
            body.addView(sheetText("Explore five worlds. Clear the stage target from eight obstacles; later stages speed up and add moving hazards. Each safe gap earns one coin.",15,0xFF214F67));
            tutorialStep(body,"1","Hum gently to rise","A quiet mmm lifts your aviator. Use short sounds to steer toward the star. In touch practice, hold the screen instead.",0xFFBCEBF9,3);
            tutorialStep(body,"2","Stay quiet to drift down","Stop humming and let the aviator float down. In touch practice, release the screen. Avoid shouting or holding your breath.",0xFFDDD1F5,4);
            tutorialStep(body,"3","Clear each world","Cloud Meadow has hoops; Jungle Ruins has vines and thorns; Clockwork Canyon has pistons and wind; Frost Caverns has drifting ice; Ember Keep has lava and firebars. Follow each safe gap. Collect a coin for every clear.",0xFFFFE8AC,5);
        }else if(entry.id==4){
            tutorialStep(body,"1","Listen to your jump","Microphone starts enabled. Stay quiet briefly to calibrate, then make a soft short sound for a low hop or a stronger comfortable sound for a high hop. Volume controls height, not musical pitch.",entry.color);
            tutorialStep(body,"2","Wait, sound, then quiet","Make your short sound during the highlighted window. Go quiet between hops so the next jump can respond.",entry.color);
            tutorialStep(body,"3","Clear the higher notes","The course moves toward you: ember gaps, steps, twin fire pits, towers and drifting ledges. Increase sound power gradually as platform heights rise. Some later steps have an overhang, so too much power can also cause a fall. Cross the fire and reach the next platform. A weak, missing or mistimed jump falls into the flames and costs one life. Timing and comfortable sound strength both matter. No shouting is needed.",entry.color);
        }else if(entry.id==5){
            body.addView(sheetText("Complete 3 duets: smile, then sound. Six cues in total. Each completed pair earns 1 coin; three missed cues ends the round.",15,0xFF5E3979));
            tutorialStep(body,"1","Smile for a spark","Smile with lips together. Facial recognition is still a preview: tap your character to create the spark.",0xFFFFD6E7,6);
            tutorialStep(body,"2","Then hum for a star","Wait for the sound cue. Be quiet briefly, then make one gentle mmm to light the star. If microphone mode is off, tap the star to simulate the hum.",0xFFE1D1FF,7);
            tutorialStep(body,"3","Light the constellation","Alternate smile and sound, one cue at a time. Complete three pairs to win. Each pair shows Good job! and earns a saved coin.",0xFFFFE8AC,8);
        }else if(entry.id==6){
            tutorialStep(body,"1","Catch the strawberries","Open your mouth only when a strawberry reaches the catch window. In this preview, tap at the right time.",entry.color,9);
            tutorialStep(body,"2","Avoid the peppers","Keep your mouth closed and do nothing for peppers. Tapping a pepper costs a heart.",0xFFFFDFD1,10);
            tutorialStep(body,"3","Fill your snack bowl","Catch at least "+stageGame.goal()+" of 6 treats. There are 3 peppers to avoid. Watch your remaining lives; every treat earns one coin.",0xFFFFE9B5,11);
        }else if(entry.id==7){
            tutorialStep(body,"1","Watch the block","A new block slides across your tower. Wait until it overlaps the gold centre mark.",entry.color,12);
            tutorialStep(body,"2","Smile to place it","Smile with lips together when the block is aligned. In this preview, tap to simulate the smile.",0xFFD9EADB,13);
            tutorialStep(body,"3","Build your tower","Place at least "+stageGame.goal()+" of 8 blocks. Watch your remaining lives. Each placed block earns one coin.",0xFFFFE9B5,14);
        }else if(entry.id==8){
            tutorialStep(body,"1","Hum to charge","This game uses sound only. Stay quiet for calibration, then hum mmm to fill the power meter. No shouting and nothing to hold.",entry.color,15);
            tutorialStep(body,"2","Go quiet in gold","Stop humming when the meter reaches the gold zone. Quiet launches the rocket. Watch the flight: too little power falls short, too much flies past.",0xFFFFE9B5,16);
            tutorialStep(body,"3","Reach the next destination","Land at least "+stageGame.goal()+" of 6 rockets. Watch your remaining lives. A successful landing earns one coin after the flight. Pausing clears an unlaunched charge and preserves a flight already in progress.",0xFFD9EADB,17);
        }else if(entry.id==9){
            tutorialStep(body,"1","Dog: woof, woof","Make two short woofs with a brief quiet gap. The two beat markers light up as your calls are heard. Match the rhythm; it does not classify the animal.",entry.color,18);
            tutorialStep(body,"2","Cat: one meow","Make one longer gentle meow, about a second, then go quiet. Use Hear pattern during play to listen again; the microphone and round pause during the example.",0xFFEAD9FF,19);
            tutorialStep(body,"3","Bird: three tweets","Meet "+AnimalPatterns.ALL.length+" animals as you progress. Match "+stageGame.goal()+" of "+AnimalPatterns.cues(stageGame.stage)+" patterns in this stage. Mixed short/long calls arrive later. Each match earns one coin.",0xFFD4E9DC,20);
            body.addView(sheetText("Hear the rhythm, then copy it (cartoon sound guides)",14,0xFF654565));for(int row=0;row<4;row++){LinearLayout guides=new LinearLayout(this);for(int col=0;col<3;col++){final int choice=row*3+col;addButton(guides,AnimalPatterns.get(choice).name,()->playAnimalGuide(choice));}body.addView(guides);}body.addView(sheetText("Choir: "+AnimalPatterns.names(),13,0xFF52736B));
        }else if(entry.id==10){
            tutorialStep(body,"1","Watch the faces","Remember each expression in order. Sequence lengths are shown in the stage target above; later stages use longer sequences.",entry.color,21);
            tutorialStep(body,"2","Repeat the order","When the pictures disappear, tap the illustrated face cards below to repeat the sequence. There is no automatic facial detection yet.",0xFFEAD9FF,22);
            tutorialStep(body,"3","Remember all three","Complete three sequences to win. A wrong face or timeout costs a heart and replays the sequence. Each completed sequence earns one coin.",0xFFFFE9B5,23);
        }else if(entry.id==11){
            tutorialStep(body,"1","Hum to fly","Stay quiet for the first second to calibrate. Then make a soft mmm or aaa. Your sound moves the firefly toward the glowing lantern; no touching.",entry.color,24);
            tutorialStep(body,"2","Guide the firefly","Keep the sound meter inside gold to move forward. Quiet or out-of-range sound makes the firefly drift back. Reach a lantern to light it and earn one coin. No shouting.",0xFFFFE9B5,25);
            tutorialStep(body,"3","Light the garden","Light at least "+stageGame.goal()+" of 6 lanterns. Rest between lanterns. Each lantern earns a coin; watch your remaining lives.",0xFFD4E9DC,26);
        }else{
            tutorialStep(body,"1","Your mission",stageRules,entry.color);
            tutorialStep(body,"2","Make your move",entry.controlBrief(),0xFFBFE9DF);
            tutorialStep(body,"3","Follow the cue", "Watch the next-action prompt. Keep each movement comfortable. A missed round? Try again!",0xFFE4D4FF);
        }
        TextView previewNote=sheetText(entry.id==14?"SPOKEN COUNTRY NAMES: Android speech recognition checks words, not loudness. On-device recognition is preferred; otherwise your phone speech provider may process audio online. English only for this first version. No audio is saved by Face Chaos. Typed answers always work without a microphone.":entry.id==12?"SOUND RACE: live microphone is enabled by default for this game. Keep quiet for the first second, then hum or say aaa. Sound volume controls speed. Touch mode: hold the accelerator. You race a local computer opponent, not an online player.":entry.id==13?"HEAD STEERING: calibrate centred, then move left/right. Touch mode: tap a screen third to choose a lane. Reverse steering is available in Game options > Input method.":entry.id==1||entry.id==4?"JUMP CONTROLS: touch is ready by default: quick tap = low jump; hold briefly and release = high jump. Optional sound: soft = low; stronger = high. Keep quiet for the first second to set the noise baseline. No shouting. Expression Escape supports face controls; teeth detection is experimental.":entry.id==9?"ANIMAL RHYTHM: microphone starts enabled for this game. Stay quiet for calibration, then copy the current animal rhythm. There are twelve animals and later stages mix short and long calls. Hear pattern pauses the round and microphone. This checks rhythm, not animal identity; touch rhythm practice is available when sound is off.":entry.id==10?"FACE MEMORY: repeat by making each expression, returning to neutral between faces. Switch face mode off to use the illustrated cards.":entry.id==11?"SOUND ONLY: your phone microphone controls the firefly. Hum in the gold range to fly to a lantern. Go quiet to rest. Touching the screen does not move it.":entry.id==5?"FACE + OPTIONAL SOUND: smile for a spark, then make a fresh sound. With face or sound off, use touch for that action.":entry.id==8?"COMET LAUNCH - SOUND ONLY: hum gently to charge, then go quiet in gold. Watch the full flight and landing before earning a coin. Nothing to hold or press during play.":entry.id>=6?"FACE MODE: open mouth for treats or smile to place blocks. Switch face mode off for touch control.":entry.id==3?"VOICE MODE: optional microphone sound-level control. Touch practice: hold to rise, release to drift down.":entry.id==0||entry.id==1||entry.id==2||entry.id==4?"FACE CONTROLS: enable facial expressions below, or switch off for touch. Teeth baring is experimental.":"Choose face, optional sound or touch controls below.",13,0xFF52736B);body.addView(previewNote);
        }else{body.addView(sheetText(AppText.brief(this,entry.id),16,0xFF173E42));body.addView(sheetText(getString(R.string.fc_calibration_tip),13,0xFF52736B));}
        if(MiniGame.supportsFace(entry.id)){
            Switch faceMode=new Switch(this);faceMode.setText(AppText.t(this,entry.id==13?"Play with head steering":"Play with facial expressions (beta)"));faceMode.setTextColor(0xFF173E42);faceMode.setChecked(faceChoice);faceMode.setOnCheckedChangeListener((button,checked)->{faceChoice=checked;getSharedPreferences("controls",0).edit().putBoolean("face",checked).apply();});body.addView(faceMode);
            if(AppText.english(this))body.addView(sheetText(entry.id==13?"Keep your head centred during calibration. Move comfortably toward the clear lane. Tracking loss freezes traffic. Switch off for touch steering.":"Look at the front camera and relax for neutral calibration. Return to neutral between expressions. Face tracking loss freezes face actions. Teeth baring is experimental; use Face check to compare it with mouth opening and smiles. Switch off for touch-only play.",13,0xFF52736B));
        }
            if(AppText.english(this))if(MiniGame.requiresMicrophone(entry.id)){body.addView(sheetText("Microphone required - sound controls only. Nothing to tap during flight.",14,0xFF52736B));}
        else if(MiniGame.supportsMicrophone(entry.id)){
            Switch soundMode=new Switch(this);soundMode.setText(AppText.t(this,entry.id==3||entry.id==8||entry.id==9||entry.id==12?"Use my voice (phone microphone)":"Play with sound (optional microphone)"));soundMode.setTextColor(0xFF173E42);soundMode.setChecked(soundChoice(entry.id));soundMode.setOnCheckedChangeListener((button,checked)->rememberSoundChoice(entry.id,checked));body.addView(soundMode);
            if(AppText.english(this))body.addView(sheetText("Stay quiet for the first second to set the sound level. Microphone samples stay on this phone and are discarded. Video clips remain silent. Switch off to play entirely by touch. No external microphone or headset is needed for sound mode; the phone microphone is enough.",13,0xFF52736B));
        }
        if(entry.id==14){Switch spoken=new Switch(this);spoken.setText(AppText.t(MainActivity.this,"Say country names (microphone)"));spoken.setTextColor(0xFF173E42);spoken.setChecked(quizVoice);spoken.setOnCheckedChangeListener((button,checked)->quizVoice=checked);body.addView(spoken);}
        Switch roundRecording=new Switch(this);roundRecording.setText(AppText.t(MainActivity.this,"Record my round (silent)"));roundRecording.setTextColor(0xFF173E42);roundRecording.setPadding(dp(10),dp(12),dp(10),dp(12));roundRecording.setChecked(recordSwitch.isChecked());
        roundRecording.setOnCheckedChangeListener((button,checked)->recordSwitch.setChecked(checked));body.addView(roundRecording);
        scroll.addView(body);shell.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        Button start=new Button(this);start.setText(AppText.t(MainActivity.this,"Let's play!  \u25B6"));start.setAllCaps(false);start.setTextSize(18);start.setTextColor(0xFF173E42);start.setTypeface(null,android.graphics.Typeface.BOLD);start.setBackground(cardBackground(entry.color,0xFF173E42));start.setBackgroundTintList(null);
        start.setOnClickListener(v->{stopAnimalGuide();brief.setOnDismissListener(null);brief.dismiss();
            // Starting takes ownership of playback. A queued picker dismissal must not
            // restore the pause state captured before this new round existed.
            if(picker!=null){picker.setOnDismissListener(null);picker.dismiss();}
            if(entry.id==14&&quizVoice&&checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){requestGamePermission(new String[]{Manifest.permission.RECORD_AUDIO},12);return;}
            if(MiniGame.supportsMicrophone(entry.id)){useVoiceMicrophone=soundChoice(entry.id);pendingSoundMode=entry.id;
                if(useVoiceMicrophone&&checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){requestGamePermission(new String[]{Manifest.permission.RECORD_AUDIO},11);return;}}
            previewGame(entry.id);});shell.addView(start,new LinearLayout.LayoutParams(-1,dp(56)));
        Button back=new Button(this);back.setText(AppText.t(MainActivity.this,"Back to games"));back.setAllCaps(false);back.setTextColor(0xFF173E42);back.setBackgroundColor(Color.TRANSPARENT);back.setOnClickListener(v->{brief.dismiss();if(picker==null)showGames();});shell.addView(back,new LinearLayout.LayoutParams(-1,dp(48)));
        brief.setContentView(shell);brief.setOnDismissListener(d->{if(overlay.game==owner){stopAnimalGuide();setRoundPaused(wasPaused);}});brief.show();
        brief.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        brief.getWindow().setLayout(Math.min(getResources().getDisplayMetrics().widthPixels-dp(24),dp(440)),Math.round(getResources().getDisplayMetrics().heightPixels*.87f));
    }
    private void showCameraStatus(String message) {
        boolean show = message != null && !message.isEmpty();
        if(show&&overlay!=null&&overlay.game.faceMode){overlay.game.faceReady=false;overlay.game.faceStatus="Camera unavailable - reconnect or use touch";overlay.invalidate();}
        status.setText(show ? message : "");
        status.setVisibility(show ? View.VISIBLE : View.GONE);
        permission.setVisibility(show ? View.VISIBLE : View.GONE);
    }
    private void addButton(LinearLayout row, String label, Runnable action) {
        Button button = new Button(this); button.setText(label); button.setTextSize(12);
        button.setOnClickListener(v -> action.run());
        row.addView(button, new LinearLayout.LayoutParams(0, -2, 1));
    }
    private boolean gameVisible(){return overlay.getVisibility()==View.VISIBLE;}
    private void primaryCaptureAction(){
        if(awaitingStart){showGameBrief(GameCatalog.ALL[overlay.game.mode],null);return;}
        if(preparingVideo||finishingVideo)return;
        if(gameVisible()){
            if(overlay.game.finished)nextOrRetryStage();else setRoundPaused(!paused);
        }else if(encoder!=null)setRoundPaused(!paused);
        else if(recordSwitch.isChecked())beginVideo(()->setRoundPaused(false));
        else showGames();
    }
    private void runStage(int mode,int stage,int carriedCoins){previewGame(mode,stage,carriedCoins);}
    private void retryRound(){runStage(overlay.game.mode,1,0);}
    private void nextOrRetryStage(){
        MiniGame current=overlay.game;Runnable next=()->runStage(current.mode,current.nextRunStage(),current.coinsForNextStage());
        android.content.SharedPreferences ratings=getSharedPreferences("ratings",0);
        if(encoder==null&&RatingPolicy.due(ratings.getInt("completed_rounds",0),ratings.getLong("last_prompt",0),ratings.contains("stars"),ratings.getBoolean("disabled",false),System.currentTimeMillis()))showRating(next);else next.run();
    }
    private void stopCountrySpeech(){
        speechGeneration++;
        if(countrySpeech!=null){countrySpeech.close();countrySpeech=null;}
        if(overlay!=null){overlay.game.quizListening=false;overlay.game.quizProcessing=false;}
    }
    private void startCountrySpeech(){
        MiniGame game=overlay.game;
        if(countrySpeech!=null||!resumed||paused||game.mode!=14||game.finished||game.resolved||game.countdown>0)return;
        if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED||!CountrySpeech.available(this)){
            quizVoice=false;game.microphoneMode=false;game.quizStatus="Speech unavailable. Tap the card to type.";return;
        }
        gameAudio.suspend();game.microphoneMode=true;game.quizListening=true;game.quizProcessing=true;game.quizStatus="Starting microphone...";
        final int generation=++speechGeneration,card=game.encounter,attempt=game.flagAttempt;
        try{
            countrySpeech=new CountrySpeech(this,new CountrySpeech.Listener(){
                private boolean valid(){return generation==speechGeneration&&resumed&&!paused&&overlay.game==game&&!game.finished&&game.encounter==card&&game.flagAttempt==attempt;}
                public void processing(){if(valid()){game.quizProcessing=true;game.quizStatus="Checking your answer...";overlay.invalidate();}}
                public void ready(){if(valid()){game.quizProcessing=false;game.quizStatus="Listening - say the country name";overlay.invalidate();}}
                public void answer(String text){if(!valid())return;countrySpeech=null;game.quizListening=false;game.flagAnswer(text);nextCountrySpeech=SystemClock.uptimeMillis()+1400;overlay.invalidate();}
                public void failed(String message){if(!valid())return;countrySpeech=null;game.quizListening=false;game.quizProcessing=false;game.microphoneMode=false;quizVoice=false;game.quizStatus=message;overlay.invalidate();}
            });countrySpeech.start();
        }catch(RuntimeException error){stopCountrySpeech();quizVoice=false;game.microphoneMode=false;game.quizStatus="Speech unavailable. Tap to retry or type.";}
    }
    private void showFlagAnswerOptions(){
        MiniGame game=overlay.game;if(game.mode!=14||paused||game.finished||game.resolved||game.countdown>0)return;
        setRoundPaused(true);final boolean[] selected={false};
        AlertDialog choices=new AlertDialog.Builder(this,android.R.style.Theme_Material_Light_Dialog_Alert).setTitle(AppText.t(MainActivity.this,"Name this country")).setItems(AppText.items(MainActivity.this,new String[]{"Say country name","Type country name"}),(dialog,which)->{
            selected[0]=true;
            if(which==1){showFlagKeyboard(game);return;}
            quizVoice=true;game.microphoneMode=true;nextCountrySpeech=0;
            if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){requestGamePermission(new String[]{Manifest.permission.RECORD_AUDIO},13);return;}
            setRoundPaused(false);startCountrySpeech();
        }).setNegativeButton(AppText.t(MainActivity.this,"Back"),null).create();
        choices.setOnDismissListener(d->{if(!selected[0])setRoundPaused(false);});choices.show();
    }
    private void showFlagKeyboard(MiniGame game){
        android.widget.EditText answer=new android.widget.EditText(this);answer.setSingleLine(true);answer.setHint("Country name in English");answer.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS|android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        AlertDialog keyboard=new AlertDialog.Builder(this,android.R.style.Theme_Material_Light_Dialog_Alert).setTitle(AppText.t(MainActivity.this,"Which country?")).setView(answer).setPositiveButton(AppText.t(MainActivity.this,"Submit"),(d,w)->{if(overlay.game!=game)return;quizVoice=false;game.microphoneMode=false;setRoundPaused(false);game.flagAnswer(answer.getText().toString());overlay.invalidate();}).setNegativeButton(AppText.t(MainActivity.this,"Back"),null).create();
        keyboard.setOnDismissListener(d->{if(overlay.game==game&&resumed)setRoundPaused(false);});keyboard.setOnShowListener(d->{answer.requestFocus();keyboard.getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);});keyboard.show();
    }
    private void setRoundPaused(boolean value){
        if(animalGuideOwner!=null)stopAnimalGuide();
        value=value||awaitingStart;paused=value;overlay.setPaused(value);
        if(value){stopCountrySpeech();stopMicrophone();stopFaceRecognition();}else{if(gameVisible()&&overlay.game.faceMode&&!overlay.game.finished)startFaceRecognition();}
        if(!value&&gameVisible()&&MiniGame.supportsMicrophone(overlay.game.mode)&&useVoiceMicrophone&&!overlay.game.finished)startMicrophone();
        if(encoder!=null&&!preparingVideo&&!finishingVideo){
            encoder.setPaused(value);
            if(value){if(pauseStart==0)pauseStart=SystemClock.elapsedRealtime();ui.removeCallbacks(captureFrame);}
            else{if(pauseStart!=0){pausedTime+=SystemClock.elapsedRealtime()-pauseStart;pauseStart=0;}ui.removeCallbacks(captureFrame);ui.post(captureFrame);}
        }
        updateCaptureControls();
    }
    private void stopOrReview(){
        if(awaitingStart){showGames();return;}
        if(preparingVideo||finishingVideo)return;
        stopMicrophone();stopFaceRecognition();
        if(encoder!=null||gameVisible()&&!overlay.game.finished){
            if(gameVisible())overlay.game.stopRound();
            overlay.invalidate();handledResult=true;
            if(encoder!=null)stopVideo(null);
            updateCaptureControls();
        }else if(lastClip!=null)showLastClip();else showGames();
    }
    private void updateCaptureControls(){
        if(captureButton==null||captureLabel==null||recordSwitch==null)return;
        boolean game=gameVisible(),active=encoder!=null,busy=preparingVideo||finishingVideo;
        CaptureButton.Mode mode;String label;
        if(busy){mode=CaptureButton.Mode.BUSY;label=finishingVideo?"Finishing":"Preparing";}
        else if(awaitingStart){mode=CaptureButton.Mode.PLAY;label="Start";}
        else if(game&&overlay.game.finished){mode=CaptureButton.Mode.RETRY;label=overlay.game.resultAction();}
        else if(game||active){mode=paused?CaptureButton.Mode.PLAY:CaptureButton.Mode.PAUSE;label=paused?"Resume":"Pause";}
        else{mode=recordSwitch.isChecked()?CaptureButton.Mode.RECORD:CaptureButton.Mode.PLAY;label=recordSwitch.isChecked()?"Record":"Play";}
        captureButton.state(mode,active&&!busy);captureButton.setEnabled(!busy);captureLabel.setText(AppText.t(this,label));
        recordSwitch.setEnabled(!active);recordSwitch.setVisibility(View.GONE);
        gamesButton.setEnabled(!busy);
        boolean canStop=active||game&&!awaitingStart&&!overlay.game.finished;
        playMenu.setVisibility(canStop||lastClip!=null||game?View.VISIBLE:View.INVISIBLE);
        playMenu.setEnabled(!busy);playMenu.setText(canStop?"■":AppText.t(this,lastClip!=null?"Clip":"Games"));
        playMenu.setContentDescription(canStop?"Stop round and finish recording":lastClip!=null?"Review recorded clip":"Choose a game");
        if(active){
            recordState.setVisibility(View.VISIBLE);
            long ms=recordingStart==0?0:SystemClock.elapsedRealtime()-recordingStart-pausedTime-(pauseStart==0?0:SystemClock.elapsedRealtime()-pauseStart);
            String timer=String.format(Locale.US,"%02d:%02d",Math.max(0,ms)/60000,Math.max(0,ms)/1000%60);
            recordState.setText(preparingVideo?"Preparing video...":finishingVideo?"Finishing clip...":(paused?"Paused ":"REC ")+timer+"  - silent video");
            recordState.setTextColor(paused?0xFFFFD66D:0xFFFFA9BA);
        }else recordState.setVisibility(View.GONE);
    }
    private void beginVideo(Runnable onReady){
        if(encoder!=null)return;
        if(!preview.isAvailable()||!camera.hasFreshFrame()||checkSelfPermission(Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this,"Enable the camera before recording",Toast.LENGTH_LONG).show();recordSwitch.setChecked(false);if(gameVisible())onReady.run();return;
        }
        File folder=getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        if(folder==null){Toast.makeText(this,"Video storage is unavailable",Toast.LENGTH_LONG).show();recordSwitch.setChecked(false);if(gameVisible())onReady.run();return;}
        if(folder.getUsableSpace()<128L*1024*1024){Toast.makeText(this,"Free some phone storage before recording",Toast.LENGTH_LONG).show();recordSwitch.setChecked(false);if(gameVisible())onReady.run();return;}
        RecordingQuality quality;
        try{quality=RecordingQuality.choose(preview.getWidth(),preview.getHeight());}
        catch(IllegalStateException error){Toast.makeText(this,"Video recording is unavailable on this device",Toast.LENGTH_LONG).show();recordSwitch.setChecked(false);if(gameVisible())onReady.run();return;}
        recordedWidth=quality.width;recordedHeight=quality.height;
        File file=new File(folder,"facechaos_"+System.currentTimeMillis()+".mp4");
        preparingVideo=true;finishingVideo=false;recordingStart=0;pausedTime=0;pauseStart=0;updateCaptureControls();
        VideoEncoder[] owner=new VideoEncoder[1];
        encoder=owner[0]=new VideoEncoder(file,recordedWidth,recordedHeight,quality.bitrate,new VideoEncoder.Listener(){
            @Override public void ready(){
                if(encoder!=owner[0])return;
                preparingVideo=false;
                if(!resumed||finishingVideo){stopVideo(null);return;}
                videoFrame=Bitmap.createBitmap(recordedWidth,recordedHeight,Bitmap.Config.ARGB_8888);
                recordingStart=SystemClock.elapsedRealtime();onReady.run();
                if(switchConfirmation!=null&&switchConfirmation.isShowing())setRoundPaused(true);
                ui.removeCallbacks(captureFrame);ui.post(captureFrame);updateCaptureControls();
            }
            @Override public void complete(File clip,int frames){
                if(encoder!=owner[0])return;
                lastClip=clip;getPreferences(0).edit().putString("last_clip",clip.getName()).apply();
                finishVideoState();
                if(resumed)Toast.makeText(MainActivity.this,"Clip ready. Tap Clip to review.",Toast.LENGTH_LONG).show();
            }
            @Override public void failed(String message){
                if(encoder!=owner[0])return;
                boolean switching=afterVideo!=null;
                finishVideoState();
                if(switching)return;
                recordSwitch.setChecked(false);
                if(resumed){Toast.makeText(MainActivity.this,message,Toast.LENGTH_LONG).show();if(gameVisible()&&!overlay.game.finished)setRoundPaused(false);}
            }
        });
        encoder.start();updateCaptureControls();
    }
    private void stopVideo(Runnable after){
        ui.removeCallbacks(captureFrame);
        if(encoder==null){if(after!=null&&resumed)after.run();return;}
        if(after!=null){Runnable previous=afterVideo;afterVideo=()->{if(previous!=null)previous.run();after.run();};}
        if(finishingVideo)return;
        finishingVideo=true;updateCaptureControls();encoder.stop();
    }
    private void finishVideoState(){
        encoder=null;preparingVideo=false;finishingVideo=false;ui.removeCallbacks(captureFrame);
        if(videoFrame!=null){videoFrame.recycle();videoFrame=null;}
        Runnable next=afterVideo;afterVideo=null;updateCaptureControls();if(next!=null&&resumed)next.run();
    }
    private void showLastClip(){
        if(encoder!=null){Toast.makeText(this,"Stop recording before reviewing a clip",Toast.LENGTH_SHORT).show();return;}
        if(lastClip==null||!lastClip.isFile()){Toast.makeText(this,"Record a clip first. Turn on Record video, then press the circle.",Toast.LENGTH_LONG).show();return;}
        showClip(lastClip);
    }
    private void showClip(File clip){
        if(!clip.isFile()){Toast.makeText(this,"This recording is no longer available",Toast.LENGTH_LONG).show();return;}
        boolean wasPaused=paused;setRoundPaused(true);
        LinearLayout body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(dp(12),dp(8),dp(12),dp(8));
        VideoView video=new VideoView(this);body.addView(video,new LinearLayout.LayoutParams(-1,dp(320)));
        body.addView(sheetText("Camera + game - silent video. Saved inside Face Chaos. Save to Gallery copies it to Movies / Face Chaos. Share opens your chosen app; you decide whether to post.",13,0xFF314B50));
        Button delete=new Button(this);delete.setText(AppText.t(MainActivity.this,"Delete this clip"));body.addView(delete);
        AlertDialog review=new AlertDialog.Builder(this,android.R.style.Theme_Material_Light_Dialog_Alert).setTitle(AppText.t(MainActivity.this,"Review your clip")).setView(body)
            .setPositiveButton(AppText.t(MainActivity.this,"Save to Gallery"),(dialog,which)->saveClip(clip))
            .setNeutralButton(AppText.t(MainActivity.this,"Share"),(dialog,which)->shareClip(clip)).setNegativeButton(AppText.t(MainActivity.this,"Close"),null).create();
        delete.setOnClickListener(v->new AlertDialog.Builder(this).setTitle(AppText.t(MainActivity.this,"Delete this local clip?")).setMessage("A copy already saved to your gallery will remain.")
            .setPositiveButton(AppText.t(MainActivity.this,"Delete"),(d,w)->{if(clip.delete()){if(clip.equals(lastClip)){lastClip=null;getPreferences(0).edit().remove("last_clip").apply();}review.dismiss();updateCaptureControls();}}).setNegativeButton(AppText.t(MainActivity.this,"Keep"),null).show());
        review.setOnDismissListener(dialog->{video.stopPlayback();if(resumed)setRoundPaused(wasPaused);});review.show();
        MediaController media=new MediaController(this);media.setAnchorView(video);video.setMediaController(media);video.setVideoURI(Uri.fromFile(clip));video.setOnPreparedListener(player->video.start());
    }
    private void shareClip(File file){
        if(!file.isFile()){Toast.makeText(this,"Recording not found",Toast.LENGTH_LONG).show();return;}
        new AlertDialog.Builder(this).setTitle(AppText.t(MainActivity.this,"Share your game video")).setItems(AppText.items(MainActivity.this,new String[]{"TikTok","Instagram","YouTube","WhatsApp","Other apps"}),(d,choice)->{
            String[] packages={"com.zhiliaoapp.musically","com.instagram.android","com.google.android.youtube","com.whatsapp"};
            Intent send=ClipSharing.video(this,file,choice<4?packages[choice]:null);
            try{startActivity(choice<4?send:Intent.createChooser(send,"Share your game video"));}
            catch(android.content.ActivityNotFoundException error){
                if(choice==0){try{startActivity(ClipSharing.video(this,file,"com.ss.android.ugc.trill"));return;}catch(android.content.ActivityNotFoundException ignored){}}
                try{startActivity(Intent.createChooser(ClipSharing.video(this,file,null),"Choose an installed video app"));}catch(android.content.ActivityNotFoundException unavailable){Toast.makeText(this,"No video-sharing app available. Save to Gallery instead.",Toast.LENGTH_LONG).show();}
            }
        }).setNegativeButton(AppText.t(MainActivity.this,"Cancel"),null).show();
    }
    private void saveClip(File file){
        if(android.os.Build.VERSION.SDK_INT<29){exportClip=file;startActivityForResult(new Intent(Intent.ACTION_CREATE_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType("video/mp4").putExtra(Intent.EXTRA_TITLE,file.getName()),90);return;}
        new Thread(()->{
            Uri target=null;
            try{
                ContentValues values=new ContentValues();values.put(MediaStore.MediaColumns.DISPLAY_NAME,file.getName());values.put(MediaStore.MediaColumns.MIME_TYPE,"video/mp4");
                values.put(MediaStore.MediaColumns.RELATIVE_PATH,"Movies/Face Chaos");values.put(MediaStore.MediaColumns.IS_PENDING,1);
                target=getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,values);if(target==null)throw new IOException("No video destination");
                try(InputStream in=new FileInputStream(file);OutputStream out=getContentResolver().openOutputStream(target)){if(out==null)throw new IOException("Cannot save video");copy(in,out);}
                ContentValues complete=new ContentValues();complete.put(MediaStore.MediaColumns.IS_PENDING,0);getContentResolver().update(target,complete,null,null);
                ui.post(()->Toast.makeText(this,"Saved to Movies / Face Chaos",Toast.LENGTH_LONG).show());
            }catch(Exception error){if(target!=null)getContentResolver().delete(target,null,null);ui.post(()->Toast.makeText(this,"Could not save this clip",Toast.LENGTH_LONG).show());}
        },"FaceChaosSave").start();
    }
    private static void copy(InputStream in,OutputStream out)throws IOException{byte[] data=new byte[65536];int count;while((count=in.read(data))!=-1)out.write(data,0,count);}
    @Override protected void onActivityResult(int request,int result,Intent data){
        super.onActivityResult(request,result,data);
        if(request==90&&result==RESULT_OK&&data!=null&&data.getData()!=null&&exportClip!=null){File file=exportClip;Uri target=data.getData();exportClip=null;
            new Thread(()->{try(InputStream in=new FileInputStream(file);OutputStream out=getContentResolver().openOutputStream(target)){if(out==null)throw new IOException();copy(in,out);ui.post(()->Toast.makeText(this,"Video saved",Toast.LENGTH_SHORT).show());}catch(Exception error){ui.post(()->Toast.makeText(this,"Could not save video",Toast.LENGTH_LONG).show());}},"FaceChaosSave").start();
        }
    }
    private void showRequiredSoundPermission(){
        setRoundPaused(true);boolean firefly=overlay.game.mode==11;
        AlertDialog.Builder dialog=new AlertDialog.Builder(this).setTitle(firefly?"Microphone needed to fly":overlay.game.mode==4?"Microphone needed for Melody Hop":overlay.game.mode==8?"Microphone needed to charge the rocket":overlay.game.mode==9?"Microphone needed for animal rhythms":"Microphone needed for sound acceleration").setMessage(firefly?"Firefly Glow uses sound only. Allow microphone access to guide your firefly, or choose another game.":overlay.game.mode==4?"Allow microphone access for sound-powered jumps. You can also choose touch practice in input settings.":overlay.game.mode==8?"Allow microphone access to hum and charge the rocket, or choose another game.":overlay.game.mode==9?"Allow microphone access to copy animal sound rhythms, or choose touch rhythm practice.":"Allow microphone access to accelerate with your voice, or choose touch controls.").setPositiveButton(AppText.t(MainActivity.this,"Allow microphone"),(d,w)->{pendingInputSwitch=true;inputSwitchPaused=false;requestGamePermission(new String[]{Manifest.permission.RECORD_AUDIO},11);}).setNegativeButton(AppText.t(MainActivity.this,"Stay paused"),null);
        if(MiniGame.requiresMicrophone(overlay.game.mode))dialog.setNeutralButton(AppText.t(MainActivity.this,"Other games"),(d,w)->closeGame());else dialog.setNeutralButton(overlay.game.mode==9?"Touch practice":"Use touch",(d,w)->applyInputMode(false,false));dialog.show();
    }
    private void showSoundFailure(){
        setRoundPaused(true);boolean firefly=overlay.game.mode==11;
        AlertDialog.Builder dialog=new AlertDialog.Builder(this).setTitle(AppText.t(MainActivity.this,"Microphone could not start")).setMessage("Your game is paused. Check microphone permission and privacy controls, then close other recording apps and retry.").setPositiveButton(AppText.t(MainActivity.this,"Retry sound"),(d,w)->setRoundPaused(false)).setNegativeButton(AppText.t(MainActivity.this,"Stay paused"),null);
        if(MiniGame.requiresMicrophone(overlay.game.mode))dialog.setNeutralButton(AppText.t(MainActivity.this,"Other games"),(d,w)->closeGame());else dialog.setNeutralButton(overlay.game.mode==9?"Touch practice":"Use touch",(d,w)->applyInputMode(false,false));dialog.show();
    }
    private void startMicrophone(){
        if(microphone!=null||!resumed)return;
        if(gameAudio!=null)gameAudio.suspend();
        if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){
            if(overlay.game.mode==4||overlay.game.mode==12||overlay.game.mode==11||overlay.game.mode==9||overlay.game.mode==8){showRequiredSoundPermission();return;}
            useVoiceMicrophone=false;overlay.game.microphoneMode=false;Toast.makeText(this,"Microphone off - touch controls are ready",Toast.LENGTH_LONG).show();return;}

        final int generation=++micGeneration;overlay.game.micReady=false;overlay.game.duetSoundArmed=false;overlay.game.duetSoundPrevious=false;
        microphone=new MicrophoneInput(new MicrophoneInput.Listener(){
            public void level(float level,float strength,boolean active,boolean ready){ui.post(()->{
                if(generation!=micGeneration||!resumed||paused||!gameVisible()||!MiniGame.supportsMicrophone(overlay.game.mode)||overlay.game.finished)return;
                overlay.game.acceptSoundLevel(level,strength,active,ready);overlay.invalidate();
            });}
            public void failed(){ui.post(()->{if(generation!=micGeneration)return;if(overlay.game.mode==3||overlay.game.mode==4||overlay.game.mode==4||overlay.game.mode==12||overlay.game.mode==11||overlay.game.mode==9||overlay.game.mode==8){showSoundFailure();return;}applyInputMode(false,paused);Toast.makeText(MainActivity.this,"Microphone unavailable - switched to touch controls",Toast.LENGTH_LONG).show();});}
        },overlay.game.mode==9||overlay.game.mode==8);microphone.start();
    }
    private void stopMicrophone(){
        if(microphone!=null){micGeneration++;MicrophoneInput current=microphone;microphone=null;current.stop();}
        if(overlay!=null){overlay.game.voiceActive=false;overlay.game.micLevel=0;overlay.game.soundStrength=0;overlay.game.jumpSampling=false;overlay.game.jumpArmed=false;}
    }
    private void stopAnimalGuide(){if(animalGuideOwner!=null){animalGuideOwner.animalDemoPlaying=false;animalGuideOwner=null;if(overlay!=null)overlay.invalidate();}if(animalGuideTrack!=null){try{animalGuideTrack.stop();}catch(IllegalStateException ignored){}animalGuideTrack.release();animalGuideTrack=null;}}
    private void hearCurrentAnimal(){
        MiniGame owner=overlay.game;if(owner.mode!=9||owner.paused||owner.finished||owner.resolved||owner.countdown>0)return;
        setRoundPaused(true);owner.resetAnimalAttempt();animalGuideOwner=owner;owner.animalDemoPlaying=true;overlay.invalidate();
        playAnimalGuide(owner.target(),()->{if(overlay.game==owner&&resumed&&gameVisible()&&!owner.finished)setRoundPaused(false);},false);
    }

    private void playAnimalGuide(int animal){playAnimalGuide(animal,null,true);}
    private void playAnimalGuide(int animal,Runnable after,boolean stopExisting){
        if(stopExisting)stopAnimalGuide();try{
            short[] samples=AnimalGuide.samples(animal);
            android.media.AudioTrack track=new android.media.AudioTrack.Builder().setAudioAttributes(new android.media.AudioAttributes.Builder().setUsage(android.media.AudioAttributes.USAGE_GAME).setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION).build()).setAudioFormat(new android.media.AudioFormat.Builder().setSampleRate(16000).setEncoding(android.media.AudioFormat.ENCODING_PCM_16BIT).setChannelMask(android.media.AudioFormat.CHANNEL_OUT_MONO).build()).setTransferMode(android.media.AudioTrack.MODE_STATIC).setBufferSizeInBytes(samples.length*2).build();
            animalGuideTrack=track;track.write(samples,0,samples.length);track.setVolume(.4f);track.play();ui.postDelayed(()->{if(animalGuideTrack==track){stopAnimalGuide();if(after!=null)after.run();}},samples.length*1000L/16000+300);
        }catch(Exception error){stopAnimalGuide();Toast.makeText(this,"Sound guide unavailable. Follow the written pattern.",Toast.LENGTH_LONG).show();if(after!=null)after.run();}
    }
    private void requestCamera() { requestPermissions(new String[]{Manifest.permission.CAMERA}, 10); }
    private void requestGamePermission(String[] permissions,int request){permissionGeneration=gameGeneration;requestPermissions(permissions,request);}
    private void pauseForWindowChange(){
        if(camera==null)return;
        setRoundPaused(true);stopAnimalGuide();stopCountrySpeech();
        if(encoder!=null)stopVideo(null);
    }
    @Override public void onConfigurationChanged(android.content.res.Configuration configuration){
        super.onConfigurationChanged(configuration);
        pauseForWindowChange();
        camera.stop();
        preview.post(()->{if(resumed&&checkSelfPermission(Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED)camera.start();});
    }
    @Override protected void onResume() {
        super.onResume(); resumed = true;ui.removeCallbacks(tick);ui.post(tick);if(faceLab)startFaceRecognition();
        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) camera.start();
        else if (!getPreferences(0).getBoolean("asked", false)) {
            getPreferences(0).edit().putBoolean("asked", true).apply(); requestCamera();
        } else showCameraStatus("Allow camera access to see yourself.");
    }
    @Override protected void onPause() { if(gameAudio!=null)gameAudio.suspend();stopAnimalGuide();stopMicrophone();stopFaceRecognition();resumed=false;ui.removeCallbacks(tick);ui.removeCallbacks(captureFrame);setRoundPaused(true);if(encoder!=null)stopVideo(null);camera.stop();super.onPause(); }
    @Override protected void onDestroy(){if(gameAudio!=null)gameAudio.release();super.onDestroy();}
    @Override public void onRequestPermissionsResult(int request, String[] permissions, int[] grants) {
        super.onRequestPermissionsResult(request, permissions, grants);
        if((request==11||request==12||request==13)&&permissionGeneration!=gameGeneration)return;
        if(request==12){quizVoice=grants.length>0&&grants[0]==PackageManager.PERMISSION_GRANTED;previewGame(14);if(!resumed)setRoundPaused(true);return;}
        if(request==13){quizVoice=grants.length>0&&grants[0]==PackageManager.PERMISSION_GRANTED;overlay.game.microphoneMode=quizVoice;setRoundPaused(false);return;}
        if(request==11){
            useVoiceMicrophone=grants.length>0&&grants[0]==PackageManager.PERMISSION_GRANTED;
            if(pendingInputSwitch){pendingInputSwitch=false;applyInputMode(useVoiceMicrophone,inputSwitchPaused);return;}
            rememberSoundChoice(pendingSoundMode,useVoiceMicrophone);
            if(!useVoiceMicrophone)Toast.makeText(this,MiniGame.requiresMicrophone(pendingSoundMode)?"Microphone permission is required for "+GameCatalog.ALL[pendingSoundMode].name:"Microphone off - touch controls are ready",Toast.LENGTH_LONG).show();
            previewGame(pendingSoundMode);if(!resumed)setRoundPaused(true);return;
        }
        if (request == 10 && resumed && grants.length > 0 && grants[0] == PackageManager.PERMISSION_GRANTED) camera.start();
        else showCameraStatus("Camera access is off. Tap Enable camera to change it.");
    }
}
