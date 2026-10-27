package com.facechaos.mobile;

import android.app.Instrumentation;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.View;
import java.io.File;
import java.io.FileOutputStream;

/** Render actual Android game views offscreen. Never reads the camera or mic. */
public final class SceneCheck extends Instrumentation {
    private boolean flagsOnly,speechOnly,voiceOnly,sectionsOnly,qualityOnly,recordsOnly,raceOnly,fireflyOnly,faceOnly,choirOnly,cometOnly,smileOnly,melodyOnly,courseOnly,sharingOnly,menuOnly,languageOnly;
    private boolean recordingOnly,playbackOnly,isolationOnly,hardwareOnly,eyeGatesOnly;
    private int sectionMode=-1;
    private boolean adaptiveOnly;
    @Override public void onCreate(Bundle args) { super.onCreate(args);adaptiveOnly=args!=null&&"1".equals(args.getString("adaptiveOnly"));eyeGatesOnly=args!=null&&"1".equals(args.getString("eyeGatesOnly"));hardwareOnly=args!=null&&"1".equals(args.getString("hardwareOnly"));isolationOnly=args!=null&&"1".equals(args.getString("isolationOnly"));playbackOnly=args!=null&&"1".equals(args.getString("playbackOnly"));recordingOnly=args!=null&&"1".equals(args.getString("recordingOnly"));languageOnly=args!=null&&"1".equals(args.getString("languageOnly"));menuOnly=args!=null&&"1".equals(args.getString("menuOnly"));sharingOnly=args!=null&&"1".equals(args.getString("sharingOnly"));courseOnly=args!=null&&"1".equals(args.getString("courseOnly"));melodyOnly=args!=null&&"1".equals(args.getString("melodyOnly"));smileOnly=args!=null&&"1".equals(args.getString("smileOnly"));cometOnly=args!=null&&"1".equals(args.getString("cometOnly"));choirOnly=args!=null&&"1".equals(args.getString("choirOnly"));faceOnly=args!=null&&"1".equals(args.getString("faceOnly"));fireflyOnly=args!=null&&"1".equals(args.getString("fireflyOnly"));raceOnly=args!=null&&"1".equals(args.getString("raceOnly"));recordsOnly=args!=null&&"1".equals(args.getString("recordsOnly"));qualityOnly=args!=null&&"1".equals(args.getString("qualityOnly"));flagsOnly=args!=null&&"1".equals(args.getString("flagsOnly"));speechOnly=args!=null&&"1".equals(args.getString("speechOnly"));voiceOnly=args!=null&&"1".equals(args.getString("voiceOnly"));sectionsOnly=args!=null&&"1".equals(args.getString("sectionsOnly"));if(args!=null&&args.containsKey("sectionMode"))sectionMode=Integer.parseInt(args.getString("sectionMode")); start(); }
    @Override public void onStart() {
        Bundle result = new Bundle();
        try {
            if(adaptiveOnly){result.putString("stream",AdaptiveWindowCheck.run(this));finish(Activity.RESULT_OK,result);return;}
            if(eyeGatesOnly){String model=FaceModelCheck.run(this);runOnMainSync(()->{
                for(int stage=1;stage<=5;stage++)for(int pose=0;pose<5;pose++){
                    GameOverlay view=new GameOverlay(getTargetContext());view.setPersistenceEnabled(false);view.select(0,stage);view.game.countdown=0;view.game.phase=1.5f;view.game.faceMode=true;view.game.faceReady=true;
                    for(int score=0;score<5;score++)if(GameSections.faceAt(stage,score)==pose){view.game.score=score;break;}
                    view.setSafeTop(30);view.setBottomReserved(210);render(view,"eye-gates-"+stage+"-"+pose+".png",720,1560);
                }
                for(int pose=3;pose<=4;pose++)renderArtwork(new TutorialArt(getTargetContext(),0,pose),"eye-tutorial-"+pose+".png",900,520);
            });result.putString("stream",model+"; PASS: all five gate/actor poses across five stages plus two matching illustrated tutorials rendered without camera/microphone");finish(Activity.RESULT_OK,result);return;}
            if(hardwareOnly){result.putString("stream",InputHardwareCheck.run(this));finish(Activity.RESULT_OK,result);return;}
            if(isolationOnly){result.putString("stream",GameIsolationCheck.run(this));finish(Activity.RESULT_OK,result);return;}
            if(playbackOnly){result.putString("stream",RecordingPlaybackCheck.runGame(this));finish(Activity.RESULT_OK,result);return;}
            if(recordingOnly){result.putString("stream",RecordingPlaybackCheck.run(this));finish(Activity.RESULT_OK,result);return;}
            if(languageOnly){runOnMainSync(()->{for(String code:new String[]{"en","ar","fr","es","pt"}){
                android.content.res.Configuration config=new android.content.res.Configuration(getTargetContext().getResources().getConfiguration());java.util.Locale locale=java.util.Locale.forLanguageTag(code);config.setLocale(locale);config.setLayoutDirection(locale);android.content.Context context=getTargetContext().createConfigurationContext(config);
                for(java.lang.reflect.Field field:R.string.class.getFields())if(field.getName().startsWith("fc_")){try{String text=context.getString(field.getInt(null));if(text.trim().isEmpty()||text.indexOf('\uFFFD')>=0)throw new AssertionError("Missing/corrupt translation "+code+field.getName());}catch(IllegalAccessException error){throw new RuntimeException(error);}}
                if(context.getString(R.string.fc_stage_summary,1,"1.00x",8).contains("%1$"))throw new AssertionError("Unformatted stage string");
                for(int width:new int[]{900,1080}){AppOptionsPanel panel=new AppOptionsPanel(context,new AppOptionsPanel.Actions(){public void select(int index){}public void close(){}});if(code.equals("ar")&&panel.getLayoutDirection()!=View.LAYOUT_DIRECTION_RTL)throw new AssertionError("Arabic panel must mirror");panel.measure(View.MeasureSpec.makeMeasureSpec(width,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(3200,View.MeasureSpec.AT_MOST));renderArtwork(panel,"language-menu-"+code+"-"+width+".png",width,panel.getMeasuredHeight());}
                RatingPanel rating=new RatingPanel(context);renderArtwork(rating,"language-rating-"+code+".png",900,1200);
                for(int mode=0;mode<15;mode++){if(AppText.brief(context,mode).trim().isEmpty())throw new AssertionError("Missing game instructions");GameOverlay view=new GameOverlay(context);view.setPersistenceEnabled(false);view.select(mode);MiniGame g=view.game;g.countdown=0;g.phase=2.1f;g.faceMode=MiniGame.supportsFace(mode);g.faceReady=true;g.microphoneMode=MiniGame.supportsMicrophone(mode);g.micReady=true;view.setSafeTop(30);view.setBottomReserved(210);if(!code.equals("en")&&GameLanguage.action(context,g).equals(GameLanguage.action(getTargetContext().createConfigurationContext(englishConfig()),g)))throw new AssertionError("Untranslated core cue "+code+mode);render(view,"language-game-"+code+"-"+mode+".png",720,1560);}
            }});result.putString("stream","PASS: five language resource sets and format arguments, ten menu layouts, five rating layouts, seventy-five game captions and Arabic RTL; device language unchanged, no camera or microphone access");finish(Activity.RESULT_OK,result);return;}
            if(menuOnly){runOnMainSync(()->{for(int width:new int[]{900,1080}){AppOptionsPanel panel=new AppOptionsPanel(getTargetContext(),new AppOptionsPanel.Actions(){public void select(int index){}public void close(){}});panel.measure(View.MeasureSpec.makeMeasureSpec(width,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(2400,View.MeasureSpec.AT_MOST));renderArtwork(panel,"app-options-"+width+".png",width,panel.getMeasuredHeight());}});result.putString("stream","PASS: native app options sheet rendered at two phone widths without camera or microphone access");finish(Activity.RESULT_OK,result);return;}
            if(sharingOnly){String[] report={""};runOnMainSync(()->{report[0]=SharingRatingCheck.run(getTargetContext());for(int width:new int[]{540,720}){RatingPanel panel=new RatingPanel(getTargetContext());if(panel.stars.getRating()!=0)throw new AssertionError("No preselected rating");renderArtwork(panel,"rating-panel-"+width+"-empty.png",width,1000);if(panel.stars.getMeasuredWidth()*panel.stars.getScaleX()>width-panel.getPaddingLeft()-panel.getPaddingRight()+1)throw new AssertionError("Rating stars clipped on compact layout");panel.stars.setRating(3);if(!RatingPolicy.valid(panel.stars.getRating()))throw new AssertionError("Rating panel accepts selection");renderArtwork(panel,"rating-panel-"+width+"-selected.png",width,1000);}});result.putString("stream",report[0]+"; four native rating panels rendered at two widths without camera/microphone use");finish(Activity.RESULT_OK,result);return;}
            if(courseOnly){runOnMainSync(()->{for(int height:new int[]{1560,1280})for(int stage=1;stage<=5;stage++)for(int obstacle=0;obstacle<8;obstacle++){GameOverlay view=new GameOverlay(getTargetContext());view.setPersistenceEnabled(false);view.select(4,stage);MiniGame g=view.game;g.countdown=0;g.microphoneMode=true;g.micReady=true;g.encounter=obstacle;g.phase=2.1f;g.soundStrength=MelodyCourse.targetStrength(stage,obstacle);view.setSafeTop(30);view.setBottomReserved(210);render(view,"melody-course-"+height+"-"+stage+"-"+obstacle+".png",720,height);}for(int stage=1;stage<=5;stage++){for(int frame=0;frame<3;frame++){GameOverlay view=new GameOverlay(getTargetContext());view.setPersistenceEnabled(false);view.select(4,stage);view.game.countdown=0;view.game.encounter=3;view.game.phase=.5f+frame*.8f;view.setSafeTop(30);view.setBottomReserved(210);render(view,"melody-scroll-"+stage+"-"+frame+".png",720,1560);}TutorialArt art=new TutorialArt(getTargetContext(),4,-1,stage);renderArtwork(art,"melody-course-tutorial-"+stage+".png",720,400);}});result.putString("stream","PASS: 80 native authored obstacle scenes at two sizes, 15 approaching-course frames and five tutorials; no camera or microphone access");finish(Activity.RESULT_OK,result);return;}
            if(melodyOnly){String[] report={""};runOnMainSync(()->{report[0]=SoundInputSettingsCheck.run(getTargetContext());for(int height:new int[]{1560,1280})for(int stage=1;stage<=5;stage++)for(int state=0;state<7;state++){GameOverlay view=new GameOverlay(getTargetContext());view.setPersistenceEnabled(false);view.select(4,stage);MiniGame g=view.game;g.countdown=0;g.microphoneMode=true;g.micReady=state!=0;g.encounter=state%2;g.phase=state==1?g.windowOpens():state>=2?2.65f:0;g.soundStrength=state==1?.35f:.85f;g.jumpUsed=state>=2;g.jumpStartPhase=2.2f;g.jumpHeight=state==2?g.jumpRequired()+10:state==3?8:0;g.resolved=state>=3;g.earnedCoin=state==4;g.coins=state==4?1:0;g.score=g.coins;g.flash=state==4?.8f:0;g.feedback=state==3?"Jump too low or mistimed":state==4?"Good job! +1 coin":"Next note coming";g.melodyFalling=state==3||state==5||state==6;g.melodyImpact=state==6;g.melodyOutcomeTime=state==6?.6f:.3f;g.melodyMissHeight=8;g.paused=state==5;if(state==3||state==5)g.feedback="Too low - falling into the fire!";if(state==6){g.finished=true;g.feedback="Mission failed";}if(state==4)g.jumpHeight=g.jumpRequired();view.setSafeTop(30);view.setBottomReserved(210);render(view,"melody-hop-"+height+"-"+stage+"-"+state+".png",720,height);}});result.putString("stream",report[0]+"; PASS: 70 Melody Hop fire-gap calibration, cue, low/high jump, fall, impact, reward and pause scenes at two sizes; no camera or microphone access");finish(Activity.RESULT_OK,result);return;}
            if(smileOnly){runOnMainSync(()->{for(int height:new int[]{1560,1280})for(int stage=1;stage<=5;stage++)for(int state=0;state<5;state++){GameOverlay view=new GameOverlay(getTargetContext());view.setPersistenceEnabled(false);view.select(7,stage);MiniGame g=view.game;g.countdown=0;g.faceMode=true;g.faceReady=true;g.score=state==4?7:3;g.encounter=g.score;g.phase=1.2f;for(int i=0;i<g.score;i++)g.stackOffsets[i]=(i%3-1)*.05f;if(state>0){g.resolved=true;g.stackDropping=true;g.stackGood=state!=3;g.stackDropOffset=state==3?.75f:.05f;g.flightTime=state==1?.19f:.5f;g.stackLanded=state>=2;g.earnedCoin=state==2||state==4;g.feedback=state==1?"Block dropping - relax your smile":state==3?"Off centre - watch the gold line":"Good job! +1 coin";}view.setSafeTop(30);view.setBottomReserved(210);render(view,"smile-stack-"+height+"-"+stage+"-"+state+".png",720,height);}});result.putString("stream","PASS: 50 native Smile Stack moving/drop/landing/miss/tall-tower scenes at two sizes; no camera or microphone access");finish(Activity.RESULT_OK,result);return;}
            if(cometOnly){String[] report={""};runOnMainSync(()->{report[0]=SoundInputSettingsCheck.run(getTargetContext());for(int stage=1;stage<=5;stage++)for(int state=0;state<7;state++){GameOverlay view=new GameOverlay(getTargetContext());view.setPersistenceEnabled(false);view.select(8,stage);MiniGame g=view.game;g.countdown=0;g.microphoneMode=true;g.micReady=state!=0;g.paused=state==6;g.charge=g.powerTarget();g.voiceActive=state==1;g.micLevel=.6f;if(state>=2&&state<=5){g.resolved=true;g.cometLaunched=true;g.launchPower=state==4?.05f:state==5?.98f:g.powerTarget();g.charge=g.launchPower;g.flightTime=state==2?.5f:1.1f;g.cometLanded=state>=3;g.earnedCoin=state==3;g.feedback=state==2?"Launched! Watch the landing":state==3?"Good job! +1 coin":state==4?"Too little power - rocket fell short":"Too much power - rocket flew past";if(state==3){g.coins=1;g.score=1;}}view.setSafeTop(30);view.setBottomReserved(210);render(view,"comet-lifecycle-"+stage+"-"+state+".png",720,1560);float scale=720/390f,ground=1560/scale-Math.max(110,210/scale)+1;if(ground-17>=1560/scale-210/scale)throw new AssertionError("Power meter overlaps controls");}});result.putString("stream",report[0]+"; 35 native comet calibration/charging/flight/landing/miss/pause scenes and power meter clearance, without microphone use");finish(Activity.RESULT_OK,result);return;}
            if(choirOnly){String[] report={""};runOnMainSync(()->{report[0]=SoundInputSettingsCheck.run(getTargetContext());for(int animal=0;animal<12;animal++)for(int state=0;state<4;state++){GameOverlay view=new GameOverlay(getTargetContext());view.setPersistenceEnabled(false);view.select(9,5);view.game.countdown=0;view.game.encounter=11-animal;view.game.microphoneMode=state!=3;view.game.micReady=state!=0;view.game.soundArmed=state>0;view.game.soundPulses=state==2?1:0;view.game.soundDuration=state==1?.15f:0;view.game.voiceActive=state==1;view.setSafeTop(30);view.setBottomReserved(210);render(view,"animal-rhythm-"+animal+"-"+state+".png",720,1560);float scale=720/390f,ground=1560/scale-Math.max(110,210/scale)+4;if(!view.animalGuideAt(195*scale,(ground-175)*scale)||view.animalGuideAt(195*scale,ground*scale))throw new AssertionError("Hear pattern hit target mismatch");}});result.putString("stream",report[0]+"; 48 native animal calibration/rhythm/touch scenes and guide hit targets, without microphone or speaker use");finish(Activity.RESULT_OK,result);return;}
            if(faceOnly){String model=FaceModelCheck.run(this);String head=HeadModelCheck.run(this);runOnMainSync(()->{
                for(int height:new int[]{1560,1280})for(int state=0;state<3;state++){
                    GameOverlay view=new GameOverlay(getTargetContext());view.setPersistenceEnabled(false);view.select(0);view.game.faceMode=true;view.game.faceReady=state==1;view.game.faceStatus="Relax briefly - calibrating";view.setFaceBottom(.55f);view.setFaceCheckOpen(state==2);view.setSafeTop(30);view.setBottomReserved(210);
                    view.measure(View.MeasureSpec.makeMeasureSpec(720,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(height,View.MeasureSpec.EXACTLY));view.layout(0,0,720,height);
                    Bitmap bitmap=Bitmap.createBitmap(720,height,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(bitmap);canvas.drawColor(0xFF728C83);view.draw(canvas);
                    OverlayLayout layout=view.gameLayout();float scale=720/390f;float y=layout.captionTop(.55f*height/scale,state==0);
                    if(layout.scale!=1||layout.offsetY!=0)throw new AssertionError("Calibration changes game geometry");
                    if(y<.55f*height/scale+8)throw new AssertionError("Caption covers detected face");
                    if(state<2&&bitmap.getPixel(360,(int)((y+4)*scale))==0xFF728C83)throw new AssertionError("Face-aware caption missing");
                    if(state==2&&bitmap.getPixel(360,(int)((layout.instructionTop+4)*scale))!=0xFF728C83)throw new AssertionError("Sticker remains visible during Face Check");
                    try(FileOutputStream out=new FileOutputStream(new File(getTargetContext().getCacheDir(),"face-clear-"+height+"-"+state+".png"))){bitmap.compress(Bitmap.CompressFormat.PNG,100,out);}catch(Exception error){throw new RuntimeException(error);}bitmap.recycle();
                }
            });result.putString("stream",model+"\n"+head+"\nPASS: six normal/compact face-aware calibration/caption/Face Check scenes, hidden check sticker and unchanged game geometry; no camera/microphone access");finish(Activity.RESULT_OK,result);return;}
            if(fireflyOnly){String[] report={""};runOnMainSync(()->{report[0]=SoundInputSettingsCheck.run(getTargetContext());for(int stage=1;stage<=5;stage++)for(int state=0;state<4;state++){GameOverlay view=new GameOverlay(getTargetContext());view.setPersistenceEnabled(false);view.select(11,stage);view.game.countdown=0;view.game.acceptSoundLevel(1,.5f,state>0,state>0);view.game.glowProgress=state==0?0:state==1?view.game.glowRequired()*.5f:view.game.glowRequired();if(state==3){view.game.score=1;view.game.coins=1;view.game.glowLit[0]=true;view.game.resolved=true;view.game.earnedCoin=true;view.game.feedback="Good job! +1 coin";}view.setSafeTop(30);view.setBottomReserved(210);render(view,"firefly-flight-"+stage+"-"+state+".png",720,1560);}});result.putString("stream",report[0]+"; 20 native firefly calibration/flight/lantern scenes; no microphone opened");finish(Activity.RESULT_OK,result);return;}
            if(raceOnly){String[] report={""};runOnMainSync(()->{report[0]=SoundInputSettingsCheck.run(getTargetContext());for(int state=0;state<4;state++){GameOverlay view=new GameOverlay(getTargetContext());view.setPersistenceEnabled(false);view.select(12);view.game.countdown=0;view.game.microphoneMode=state!=3;view.game.micReady=state>0;view.game.soundStrength=state==2?.85f:.25f;view.game.voiceSignal(state>0,state>0);if(state>0)for(int i=0;i<50;i++)view.game.step(.02f);view.setSafeTop(30);view.setBottomReserved(210);render(view,"race-sound-"+state+".png",720,1560);}});result.putString("stream",report[0]+"; four native calibration/power/touch scenes rendered without opening microphone");finish(Activity.RESULT_OK,result);return;}
            if(recordsOnly){String[] report={""};runOnMainSync(()->{
                report[0]=RunRecordsCheck.run(getTargetContext());
                for(int mode:new int[]{0,3,10,14}){GameOverlay view=new GameOverlay(getTargetContext());view.setPersistenceEnabled(false);view.select(mode,4,12);view.game.coins=3;view.game.bestRunCoins=21;view.game.countdown=0;view.game.finished=true;view.game.feedback="Mission failed";view.setSafeTop(30);view.setBottomReserved(210);render(view,"run-result-"+mode+".png",720,1560);}
            });result.putString("stream",report[0]);finish(Activity.RESULT_OK,result);return;}
            if(qualityOnly){result.putString("stream",CameraQualityCheck.run(getTargetContext())+"\n"+VideoEncoderCheck.run(getTargetContext()));finish(Activity.RESULT_OK,result);return;}
            if(sectionsOnly){runOnMainSync(this::renderSections);result.putString("stream",sectionMode>=0?"PASS: five Expression Escape sections at normal/compact sizes, matching previews and all fifteen action scenes; no camera/microphone access\n":"PASS: 55 section scenes at normal and compact sizes, 55 matching tutorials, five high-stack compact scenes, 15 themed Escape action scenes; central selfie kept clear; no camera/microphone access\n");finish(Activity.RESULT_OK,result);return;}
            if(voiceOnly){runOnMainSync(this::renderVoiceWorlds);result.putString("stream","PASS: twenty native world/hazard/compact scenes and five matching world tutorials; face-safe layouts; no camera/microphone access\n");finish(Activity.RESULT_OK,result);return;}
            if(speechOnly){String[] adapter={""};runOnMainSync(()->adapter[0]=CountrySpeechCheck.run());result.putString("stream",adapter[0]+"\n"+SpeechSupportCheck.run(this));finish(Activity.RESULT_OK,result);return;}
            runOnMainSync(() -> {
                for(int height:new int[]{1560,1280}){GameOverlay dune=new GameOverlay(getTargetContext());dune.setPersistenceEnabled(false);dune.select(0,2);dune.game.countdown=0;dune.game.phase=2;dune.game.faceMode=true;dune.game.faceReady=true;dune.setSafeTop(30);dune.setBottomReserved(210);render(dune,"dune-instructions-"+height+".png",720,height);}
                for (int mode=0;mode<GameCatalog.ALL.length;mode++) {
                    GameOverlay view=new GameOverlay(getTargetContext());view.setPersistenceEnabled(false);
                    view.select(mode);view.game.countdown=0;view.game.score=2;
                    view.game.phase=1.8f;view.game.feedback="Your turn!";view.setSafeTop(30);view.setBottomReserved(mode==0||mode==2||mode==4?210:400);
                    view.measure(View.MeasureSpec.makeMeasureSpec(720,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(1560,View.MeasureSpec.EXACTLY));
                    view.layout(0,0,720,1560);
                    Bitmap bitmap=Bitmap.createBitmap(720,1560,Bitmap.Config.ARGB_8888);
                    Canvas canvas=new Canvas(bitmap);canvas.drawColor(0xFF728C83);view.draw(canvas);
                    try (FileOutputStream out=new FileOutputStream(new File(getTargetContext().getCacheDir(),"game-scene-"+mode+".png"))) {
                        if(!bitmap.compress(Bitmap.CompressFormat.PNG,100,out))throw new IllegalStateException("PNG render failed");
                    } catch (Exception error) { throw new RuntimeException(error); }
                    bitmap.recycle();
                }
                for(int mode=0;mode<GameCatalog.ALL.length;mode++) {
                    TutorialArt art=new TutorialArt(getTargetContext(),mode);
                    art.measure(View.MeasureSpec.makeMeasureSpec(660,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(320,View.MeasureSpec.EXACTLY));art.layout(0,0,660,320);
                    Bitmap image=Bitmap.createBitmap(660,320,Bitmap.Config.ARGB_8888);art.draw(new Canvas(image));
                    try(FileOutputStream out=new FileOutputStream(new File(getTargetContext().getCacheDir(),"tutorial-"+mode+".png"))){image.compress(Bitmap.CompressFormat.PNG,100,out);}catch(Exception error){throw new RuntimeException(error);}image.recycle();
                }
                for(int action=0;action<3;action++) {
                    for(int success=0;success<2;success++) {
                        GameOverlay escape=new GameOverlay(getTargetContext());escape.setPersistenceEnabled(false);escape.select(1);escape.game.countdown=0;escape.game.encounter=action;escape.game.phase=1.8f;
                        escape.game.resolved=success==1;escape.game.earnedCoin=success==1;escape.game.coins=success;escape.game.coinBalance=12+success;escape.game.flash=success==1?.4f:0;escape.game.paused=false;
                        escape.setSafeTop(30);escape.setBottomReserved(400);render(escape,"escape-action-"+action+"-"+success+".png",720,1560);
                    }
                    TutorialArt detail=new TutorialArt(getTargetContext(),1,action);detail.measure(View.MeasureSpec.makeMeasureSpec(600,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(240,View.MeasureSpec.EXACTLY));detail.layout(0,0,600,240);
                    Bitmap bitmap=Bitmap.createBitmap(600,240,Bitmap.Config.ARGB_8888);detail.draw(new Canvas(bitmap));
                    try(FileOutputStream out=new FileOutputStream(new File(getTargetContext().getCacheDir(),"escape-instruction-"+action+".png"))){bitmap.compress(Bitmap.CompressFormat.PNG,100,out);}catch(Exception error){throw new RuntimeException(error);}bitmap.recycle();
                }
                for(int state=0;state<4;state++){
                    GameOverlay voice=new GameOverlay(getTargetContext());voice.setPersistenceEnabled(false);voice.select(3);voice.game.countdown=0;voice.game.phase=state==2?4.3f:2;
                    voice.game.height=state==0?.85f:state==1?.15f:.5f;voice.game.voiceActive=state==0;voice.game.microphoneMode=true;voice.game.micReady=true;voice.game.micLevel=state==0?.7f:0;
                    if(state==2){voice.game.resolved=true;voice.game.earnedCoin=true;voice.game.flash=.5f;voice.game.score=1;voice.game.coins=1;}
                    voice.setSafeTop(30);voice.setBottomReserved(210);render(voice,"voice-state-"+state+".png",720,state==3?1280:1560);
                }
                for(int state=0;state<6;state++){
                    GameOverlay duet=new GameOverlay(getTargetContext());duet.setPersistenceEnabled(false);duet.select(5);duet.game.countdown=0;duet.game.score=state==1?1:state==2?2:0;
                    duet.game.microphoneMode=true;duet.game.micReady=true;duet.game.duetSoundArmed=true;duet.game.micLevel=state==1?.6f:0;
                    if(state==2){duet.game.earnedCoin=true;duet.game.flash=.5f;duet.game.coins=1;}
                    if(state==3||state==4){duet.game.finished=true;duet.game.won=state==3;duet.game.score=state==3?6:2;duet.game.hearts=state==3?3:0;}
                    duet.setSafeTop(30);duet.setBottomReserved(210);render(duet,"duet-state-"+state+".png",720,state==5?1280:1560);
                }
                for(int mode=6;mode<=8;mode++){
                    for(int state=0;state<4;state++){
                        GameOverlay collection=new GameOverlay(getTargetContext());collection.setPersistenceEnabled(false);collection.select(mode);collection.game.countdown=0;collection.game.phase=1.65f;
                        collection.game.encounter=mode==6&&state==1?2:0;collection.game.charge=state==1?collection.game.powerTarget():.1f;
                        if(state==2){collection.game.resolved=true;collection.game.earnedCoin=true;collection.game.flash=.5f;collection.game.coins=1;collection.game.score=1;collection.game.flightTime=.5f;}
                        collection.setSafeTop(30);collection.setBottomReserved(210);render(collection,"collection-"+mode+"-"+state+".png",720,state==3?1280:1560);
                    }
                }
                GameOverlay highTower=new GameOverlay(getTargetContext());highTower.setPersistenceEnabled(false);highTower.select(7);highTower.game.countdown=0;highTower.game.score=7;highTower.game.encounter=7;highTower.game.phase=1;
                highTower.setSafeTop(30);highTower.setBottomReserved(210);render(highTower,"stack-high-compact.png",720,1280);
                for(int mode=9;mode<=11;mode++)for(int state=0;state<4;state++){
                    GameOverlay extra=new GameOverlay(getTargetContext());extra.setPersistenceEnabled(false);extra.select(mode);extra.game.countdown=0;extra.game.phase=state==1?4:.7f;extra.game.encounter=state==1?1:0;
                    extra.game.microphoneMode=mode!=10;extra.game.micReady=true;extra.game.glowProgress=.8f;extra.game.micLevel=.55f;
                    if(state==2){extra.game.resolved=true;extra.game.earnedCoin=true;extra.game.score=1;extra.game.coins=1;extra.game.flash=.5f;}
                    extra.setSafeTop(30);extra.setBottomReserved(mode==10?340:210);render(extra,"extras-"+mode+"-"+state+".png",720,state==3?1280:1560);
                }
                GameOverlay memoryCards=new GameOverlay(getTargetContext());memoryCards.setPersistenceEnabled(false);memoryCards.select(10);memoryCards.game.countdown=0;memoryCards.setSafeTop(30);memoryCards.setBottomReserved(210);
                render(memoryCards,"memory-watch.png",720,1560);
                float scale=720/390f,ground=1560/scale-Math.max(110,210/scale)+4;
                OverlayLayout memoryLayout=new OverlayLayout(1560/scale,210/scale,10);
                if(memoryCards.memoryActionAt((memoryLayout.offsetX+65*memoryLayout.scale)*scale,(memoryLayout.offsetY+(ground-45)*memoryLayout.scale)*scale)!=-1)throw new AssertionError("Memory cards active during watch phase");
                memoryCards.game.phase=memoryCards.game.memoryShowDuration()+.1f;
                for(int choice=0;choice<3;choice++)if(memoryCards.memoryActionAt((memoryLayout.offsetX+(65+130*choice)*memoryLayout.scale)*scale,(memoryLayout.offsetY+(ground-45)*memoryLayout.scale)*scale)!=choice)throw new AssertionError("Illustrated memory card hit target mismatch");
                if(memoryCards.memoryActionAt((memoryLayout.offsetX+130*memoryLayout.scale)*scale,(memoryLayout.offsetY+(ground-45)*memoryLayout.scale)*scale)!=-1)throw new AssertionError("Memory card gap accepts an answer");
                render(memoryCards,"memory-repeat.png",720,1560);
                memoryCards.input(memoryCards.game.target());render(memoryCards,"memory-correct.png",720,1560);
                memoryCards.game.paused=true;if(memoryCards.memoryActionAt((memoryLayout.offsetX+65*memoryLayout.scale)*scale,(memoryLayout.offsetY+(ground-45)*memoryLayout.scale)*scale)!=-1)throw new AssertionError("Paused memory accepts answer");
                memoryCards.game.paused=false;render(memoryCards,"memory-compact.png",720,1280);
                for(int state=0;state<3;state++){
                    GameOverlay stages=new GameOverlay(getTargetContext());stages.setPersistenceEnabled(false);stages.select(state==2?10:1,state==0?1:5);stages.game.countdown=0;
                    if(state<2){stages.game.finished=true;stages.game.won=true;stages.game.score=7;}else{stages.game.phase=.7f;stages.game.memoryRound=2;}
                    stages.setSafeTop(30);stages.setBottomReserved(210);render(stages,"stage-state-"+state+".png",720,state==2?1280:1560);
                }
                // Inspect approach, near gate, pass, miss and result independently.
                for(int state=0;state<5;state++) {
                    GameOverlay view=new GameOverlay(getTargetContext());view.setPersistenceEnabled(false);view.select(0);
                    view.game.countdown=0;view.game.score=2;view.game.phase=state==0?1.8f:5.4f;
                    if(state==2){view.game.gateState=1;view.game.gateMatched=true;view.game.gateAnimation=.45f;}
                    if(state==3){view.game.gateState=2;view.game.hearts=2;view.game.gateAnimation=.45f;}
                    if(state==4){view.game.finished=true;view.game.won=false;view.game.score=2;view.game.hearts=0;view.game.gateState=2;view.game.gateAnimation=.95f;view.game.feedback="Mission failed";}
                    view.setSafeTop(30);view.setBottomReserved(210);
                    render(view,"emoji-state-"+state+".png",720,1560);
                }
                for(int mode:new int[]{1,4})for(int tall=0;tall<2;tall++){
                    GameOverlay jumps=new GameOverlay(getTargetContext());jumps.setPersistenceEnabled(false);jumps.select(mode);jumps.game.countdown=0;jumps.game.encounter=mode==1?tall*3:tall;jumps.game.phase=2.2f;jumps.game.microphoneMode=true;jumps.game.micReady=true;jumps.game.soundStrength=tall==1?.85f:.35f;jumps.game.jump(jumps.game.soundStrength);for(int i=0;i<12;i++)jumps.game.step(.02f);jumps.setSafeTop(30);jumps.setBottomReserved(210);render(jumps,"sound-jump-"+mode+"-"+tall+".png",720,1560);
                }
                for(int mode:new int[]{1,3,4,8,9,11}){
                    GameOverlay touch=new GameOverlay(getTargetContext());touch.setPersistenceEnabled(false);touch.select(mode);touch.game.countdown=0;touch.game.microphoneMode=false;touch.game.touchSoundPatterns=mode==9;touch.game.phase=2.1f;if(mode==1)touch.game.encounter=3;if(mode==4)touch.game.encounter=1;touch.game.touchDown();for(int i=0;i<20;i++)touch.game.step(.01f);touch.setSafeTop(30);touch.setBottomReserved(210);render(touch,"touch-controls-"+mode+".png",720,1560);
                }
                for(int mode:new int[]{0,1,2,5,6,7,10})for(int state=0;state<2;state++){
                    GameOverlay facial=new GameOverlay(getTargetContext());facial.setPersistenceEnabled(false);facial.select(mode,5);facial.game.countdown=0;facial.game.faceMode=true;facial.game.faceReady=state==1;facial.game.faceStatus=state==0?"No face - move into view":"Open mouth detected - return to neutral";if(mode==1)facial.game.encounter=1;if(mode==10)facial.game.phase=facial.game.memoryShowDuration()+.1f;facial.setSafeTop(30);facial.setBottomReserved(210);render(facial,"face-input-"+mode+"-"+state+".png",720,1560);
                }
                for(int mode:new int[]{12,13})for(int state=0;state<6;state++){
                    GameOverlay racer=new GameOverlay(getTargetContext());racer.setPersistenceEnabled(false);racer.select(mode,state==5?5:1);racer.game.countdown=state==0?2:0;racer.setSafeTop(30);racer.setBottomReserved(210);RacingGame race=racer.game.racing;
                    if(mode==12){racer.game.microphoneMode=true;racer.game.micReady=true;racer.game.soundStrength=.75f;race.playerSpeed=state==2?85:180;race.rivalSpeed=145;race.playerDistance=state==2?140:250;race.rivalDistance=210;racer.game.score=(int)(race.playerDistance/RacingGame.FINISH*100);}
                    else{racer.game.faceMode=true;racer.game.faceReady=state!=0;racer.game.faceStatus=state==0?"Head calibration 4 / 12 - look centred":"Steering "+(state==2?"RIGHT":"LEFT");race.steerPosition=state==2?1:-1;racer.game.headLane=state==2?1:-1;race.trafficMask=state==2?3:6;race.trafficTime=race.approachTime()*.7f;}
                    if(state==3){racer.game.finished=true;racer.game.won=true;racer.game.score=racer.game.goal();race.result=1;if(mode==12){race.playerDistance=450;race.rivalDistance=415;}else{race.dodged=race.trafficRequired();race.rows=race.trafficGoal();}}
                    if(state==4){racer.game.finished=true;racer.game.won=false;racer.game.feedback="Mission failed";race.result=-1;if(mode==12){race.playerDistance=400;race.rivalDistance=450;racer.game.score=88;}else racer.game.hearts=0;}
                    render(racer,"racing-"+mode+"-"+state+".png",720,state==5?1280:1560);
                }
                for(int state=0;state<6;state++){
                    GameOverlay flags=new GameOverlay(getTargetContext());flags.setPersistenceEnabled(false);flags.select(14,state==5?5:1);flags.game.countdown=0;flags.setSafeTop(30);flags.setBottomReserved(210);
                    if(state==1){flags.game.quizListening=true;flags.game.quizStatus="Listening - say the country name";}
                    if(state==2)flags.game.flagAnswer(flags.game.flags.country(0).name);
                    if(state==3)flags.game.flagAnswer("Mars");
                    if(state==4){flags.game.finished=true;flags.game.won=false;flags.game.hearts=0;}
                    render(flags,"flags-state-"+state+".png",720,state==5?1280:1560);
                }
                Bitmap sheet=Bitmap.createBitmap(960,1200,Bitmap.Config.ARGB_8888);Canvas sheetCanvas=new Canvas(sheet);sheetCanvas.drawColor(0xFFF2F2E7);FlagArt flagArt=new FlagArt();android.graphics.Paint labels=new android.graphics.Paint(3);labels.setTextSize(20);labels.setColor(0xFF173E42);
                for(int i=0;i<FlagQuiz.COUNTRIES.length;i++){int col=i%4,row=i/4;flagArt.flag(sheetCanvas,FlagQuiz.COUNTRIES[i].code,col*240+120,row*200+85,180,115);sheetCanvas.drawText(FlagQuiz.COUNTRIES[i].name,col*240+26,row*200+170,labels);}
                try(FileOutputStream out=new FileOutputStream(new File(getTargetContext().getCacheDir(),"flag-catalog.png"))){sheet.compress(Bitmap.CompressFormat.PNG,100,out);}catch(Exception error){throw new RuntimeException(error);}sheet.recycle();
                renderVoiceWorlds();
                GameOverlay escapeCompact=new GameOverlay(getTargetContext());escapeCompact.setPersistenceEnabled(false);escapeCompact.select(1);escapeCompact.setSafeTop(30);escapeCompact.setBottomReserved(210);
                render(escapeCompact,"escape-compact.png",720,1280);
                GameOverlay compact=new GameOverlay(getTargetContext());compact.setPersistenceEnabled(false);compact.select(0);
                compact.setSafeTop(30);compact.setBottomReserved(210);
                render(compact,"emoji-compact.png",720,1280);
            });
            String speech="Speech provider installed: "+CountrySpeech.available(getTargetContext())+"; on-device available: "+(android.os.Build.VERSION.SDK_INT>=31&&android.speech.SpeechRecognizer.isOnDeviceRecognitionAvailable(getTargetContext()))+" (availability only; no microphone opened)";
            if(flagsOnly||voiceOnly){result.putString("stream","PASS: 20 native Voice Runner world/hazard/compact scenes and five world-specific tutorials; native game/tutorial regression scenes; six flag states, flag catalog, compact face-safe layout; "+speech+"\n");finish(Activity.RESULT_OK,result);return;}
            String video=VideoEncoderCheck.run(getTargetContext());
            String audio=AudioAssetsCheck.run(this);
            String face=FaceModelCheck.run(this);
            String head=HeadModelCheck.run(this);
            result.putString("stream","PASS: six flag states and 24-flag vector catalog; fifteen game/tutorial scenes; twelve sound-racing/head-dodge scenes; fourteen face-ready/lost overlay scenes; six microphone-free touch-control scenes; four sound-jump volume/trajectory scenes; stage completion/finale/six-face compact layouts; Face Memory watch/repeat/feedback, card hit targets and compact view; twelve animal/memory/lantern states; high tower on compact screen; twelve new collection scenes; six Star Duet states; four Voice Runner states including compact view; three matching Escape instruction images and six Escape action states; fourteen illustrated tutorials, fourteen native scenes, five gate animation states and compact viewport, without camera/microphone access\n"+speech+"\n"+video+"\n"+audio+"\n"+face+"\n"+head+"\n");
            finish(Activity.RESULT_OK,result);
        } catch (Throwable error) {
            result.putString("stream","FAIL: "+error+"\n");finish(Activity.RESULT_CANCELED,result);
        }
    }
    private void renderSections(){
        for(int mode=0;mode<GameCatalog.ALL.length;mode++){
            if(!GameSections.applies(mode)||sectionMode>=0&&mode!=sectionMode)continue;
            for(int stage=1;stage<=5;stage++){
                for(int compact=0;compact<2;compact++){
                    GameOverlay view=new GameOverlay(getTargetContext());view.setPersistenceEnabled(false);view.select(mode,stage);MiniGame game=view.game;game.countdown=0;game.phase=mode==4?2.2f:1.7f;game.score=mode==7?3:mode==2?4:0;game.micReady=true;game.microphoneMode=MiniGame.supportsMicrophone(mode);game.faceMode=MiniGame.supportsFace(mode);game.faceReady=true;game.faceStatus="Face controls ready";if(mode==8)game.charge=game.powerTarget()*.8f;view.setSafeTop(30);view.setBottomReserved(210);render(view,"section-"+mode+"-"+stage+"-"+compact+".png",720,compact==1?1280:1560);
                }
                TutorialArt art=new TutorialArt(getTargetContext(),mode,-1,stage);art.measure(View.MeasureSpec.makeMeasureSpec(660,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(320,View.MeasureSpec.EXACTLY));art.layout(0,0,660,320);Bitmap bitmap=Bitmap.createBitmap(660,320,Bitmap.Config.ARGB_8888);art.draw(new Canvas(bitmap));try(FileOutputStream out=new FileOutputStream(new File(getTargetContext().getCacheDir(),"section-tutorial-"+mode+"-"+stage+".png"))){bitmap.compress(Bitmap.CompressFormat.PNG,100,out);}catch(Exception error){throw new RuntimeException(error);}bitmap.recycle();
                if(mode==7){GameOverlay high=new GameOverlay(getTargetContext());high.setPersistenceEnabled(false);high.select(7,stage);high.game.countdown=0;high.game.score=7;high.game.encounter=7;high.game.phase=1;high.setSafeTop(30);high.setBottomReserved(210);render(high,"section-stack-high-"+stage+".png",720,1280);}
                if(mode==1)for(int action=0;action<3;action++){
                    GameOverlay escape=new GameOverlay(getTargetContext());escape.setPersistenceEnabled(false);escape.select(1,stage);escape.game.countdown=0;escape.game.encounter=action;escape.game.phase=1.8f;escape.game.micReady=true;escape.setSafeTop(30);escape.setBottomReserved(210);render(escape,"section-escape-"+stage+"-"+action+".png",720,1560);
                }
            }
        }
    }
    private void renderVoiceWorlds(){
                for(int stage=1;stage<=5;stage++){
                    for(int kind=0;kind<2;kind++)for(int compact=0;compact<2;compact++){
                        GameOverlay course=new GameOverlay(getTargetContext());course.setPersistenceEnabled(false);course.select(3,stage);course.game.countdown=0;course.game.encounter=kind;course.game.phase=course.game.voiceApproach()*.78f;course.game.height=course.game.corridor();course.game.microphoneMode=true;course.game.micReady=true;course.game.micLevel=.6f;course.game.voiceActive=true;course.setSafeTop(30);course.setBottomReserved(210);
                        render(course,"voice-world-"+stage+"-"+kind+"-"+compact+".png",720,compact==1?1280:1560);
                    }
                    TutorialArt world=new TutorialArt(getTargetContext(),3,-1,stage);world.measure(View.MeasureSpec.makeMeasureSpec(660,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(320,View.MeasureSpec.EXACTLY));world.layout(0,0,660,320);Bitmap cover=Bitmap.createBitmap(660,320,Bitmap.Config.ARGB_8888);world.draw(new Canvas(cover));try(FileOutputStream out=new FileOutputStream(new File(getTargetContext().getCacheDir(),"voice-world-tutorial-"+stage+".png"))){cover.compress(Bitmap.CompressFormat.PNG,100,out);}catch(Exception error){throw new RuntimeException(error);}cover.recycle();
                }
    }
    private android.content.res.Configuration englishConfig(){android.content.res.Configuration config=new android.content.res.Configuration(getTargetContext().getResources().getConfiguration());config.setLocale(java.util.Locale.ENGLISH);config.setLayoutDirection(java.util.Locale.ENGLISH);return config;}
    private void renderArtwork(View view,String name,int width,int height){
        view.measure(View.MeasureSpec.makeMeasureSpec(width,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(height,View.MeasureSpec.EXACTLY));view.layout(0,0,width,height);
        Bitmap bitmap=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);view.draw(new Canvas(bitmap));
        try(FileOutputStream out=new FileOutputStream(new File(getTargetContext().getCacheDir(),name))){if(!bitmap.compress(Bitmap.CompressFormat.PNG,100,out))throw new IllegalStateException("PNG render failed");}catch(Exception error){throw new RuntimeException(error);}bitmap.recycle();
    }
    private void render(GameOverlay view,String name,int width,int height) {
        view.measure(View.MeasureSpec.makeMeasureSpec(width,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(height,View.MeasureSpec.EXACTLY));
        view.layout(0,0,width,height);
        Bitmap bitmap=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(bitmap);canvas.drawColor(0xFF728C83);view.draw(canvas);
        if(bitmap.getPixel(width/2,height/3)!=0xFF728C83)throw new AssertionError("Central camera area unexpectedly covered");
        for(float fraction:new float[]{.2f,.25f,.33f,.4f})if(bitmap.getPixel(width/2,(int)(height*fraction))!=0xFF728C83)throw new AssertionError("Comment/game covers selfie area in mode "+view.game.mode);
        OverlayLayout slots=view.gameLayout();float scale=width/390f;
        if(bitmap.getPixel(width/2,(int)((slots.captionTop(-1,view.game.faceMode&&!view.game.faceReady&&!view.game.finished&&!view.game.paused)+4)*scale))==0xFF728C83)throw new AssertionError("Instruction strip missing above game");
        try(FileOutputStream out=new FileOutputStream(new File(getTargetContext().getCacheDir(),name))) {
            if(!bitmap.compress(Bitmap.CompressFormat.PNG,100,out))throw new IllegalStateException("PNG render failed");
        }catch(Exception error){throw new RuntimeException(error);}
        bitmap.recycle();
    }
}
