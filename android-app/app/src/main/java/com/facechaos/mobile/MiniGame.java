package com.facechaos.mobile;
/** Pure game rules. Future validated input adapters feed the same action IDs. */
final class MiniGame {
 final int mode,stage;
 final RacingGame racing;
 final FlagQuiz flags;
 boolean quizListening,quizProcessing;
 int flagAttempt;
 String quizStatus="Say the country name, or tap to type";
 float flagLimit(){return 18;}
 void flagAnswer(String answer){
  if(mode!=14||paused||finished||countdown>0||resolved||answer==null||answer.trim().isEmpty())return;
  quizListening=false;quizProcessing=false;flagAttempt++;
  if(flags.country(encounter).matches(answer)){score++;awardCoin();resolved=true;flightTime=0;flash=.9f;feedback="Good job! "+flags.country(encounter).name+" +1 coin";if(score>=goal())finish(true);}
  else {earnedCoin=false;phase=0;miss("Try again - that is not this country");quizStatus="Try again. Heard: "+answer;}
 }
 int headLane;
 float headGrace;
 static final int STAGE_COUNT=5;
 int score, hearts=3, encounter, coins;
 int coinsBeforeStage,bestRunCoins;
 int runCoins(){return coinsBeforeStage+coins;}
 int nextRunStage(){return finished&&won&&stage<STAGE_COUNT?stage+1:1;}
 int coinsForNextStage(){return nextRunStage()>1?runCoins():0;}
 long coinBalance;
 boolean faceMode,faceReady;
 String faceStatus="Finding your face";
 static boolean supportsFace(int mode){return mode==0||mode==1||mode==2||mode==5||mode==6||mode==7||mode==10||mode==13;}
 boolean needsFaceNow(){return faceMode&&(mode==13||mode==0||mode==2||mode==6||mode==7||mode==1&&target()!=0||mode==5&&target()==0||mode==10&&!memoryShowing()&&!resolved);}
 boolean faceBlocked(){return needsFaceNow()&&!faceReady;}
 boolean earnedCoin, voiceActive, microphoneMode, micReady, duetSoundArmed, duetSoundPrevious;
 float micLevel, soundStrength, charge, flightTime;
 float jumpHeight,jumpVelocity,jumpPeak,jumpSampleTime;
 boolean jumpUsed,jumpSampling,jumpArmed;
 boolean melodyFalling,melodyImpact;
 float melodyOutcomeTime,jumpStartPhase,melodyMissHeight;
 float melodyTravel(){return Math.max(0,Math.min(1,(phase-jumpStartPhase)/Math.max(.05f,2.65f-jumpStartPhase)));}
 float melodyX(){return 85;}
 float melodyScroll(){return phase*MelodyCourse.speed(stage);}
 float melodyCameraLift(){return resolved&&earnedCoin?MelodyCourse.height(stage,encounter)*Math.min(1,melodyOutcomeTime/1.05f):0;}
 float melodyPracticeStrength(){float launch=2.2f,remaining=2.65f-launch,h=MelodyCourse.height(stage,encounter)+4;float apex=(h+500*remaining*remaining)*(h+500*remaining*remaining)/(2000*remaining*remaining);return Math.max(0,Math.min(1,(apex-18)/82));}
 float melodyGapStart(){return 85+(windowOpens()-phase)*MelodyCourse.speed(stage);}
 float melodyLandingX(){return 85+(2.65f-phase)*MelodyCourse.speed(stage)-12;}
 float melodyNextGap(){return 85+(2.65f+1.05f*pace()+windowOpens()-phase)*MelodyCourse.speed(stage);}
 float melodyFallProgress(){return Math.max(0,Math.min(1,melodyOutcomeTime/.55f));}
 float melodyFeet(float base){return melodyFalling?base-melodyMissHeight+135*melodyFallProgress()*melodyFallProgress():base-jumpHeight;}
 boolean soundJump(){return mode==4||mode==1&&target()==0;}
 boolean highJump(){return mode==4?MelodyCourse.height(stage,encounter)>=40:(encounter/3)%2==1;}
 float jumpRequired(){return mode==4?MelodyCourse.required(stage,encounter,phase):highJump()?60:18;}
 void jump(float strength){
  if(!soundJump()||paused||finished||countdown>0||resolved||jumpUsed||phase<windowOpens()||phase>windowCloses()+(microphoneMode?.16f*pace():0))return;
  jumpUsed=true;jumpStartPhase=phase;float apex=18+82*Math.max(0,Math.min(1,strength));
  jumpVelocity=(float)Math.sqrt(2000*apex);feedback="Jump!";
 }
 boolean touchHolding,touchSoundPatterns;
 float touchHoldTime;
 int touchEncounter,touchAction;
 static boolean requiresMicrophone(int mode){return mode==8||mode==11;}
 static boolean supportsMicrophone(int mode){return mode==1||mode==3||mode==4||mode==5||mode==8||mode==9||mode==11||mode==12;}
 static float touchStrength(float seconds){return Math.min(.95f,.35f+Math.max(0,seconds)*1.45f);}
 boolean liveSoundCue(){return microphoneMode&&(soundJump()||mode==3||mode==8||mode==9||mode==11||mode==12||mode==5&&target()==1);}
 float raceThrottle(){return microphoneMode?(micReady&&voiceActive&&Float.isFinite(soundStrength)?Math.max(0,Math.min(1,soundStrength)):0):voiceActive?Math.min(1,.2f+touchHoldTime*.9f):0;}
 void touchLane(float fraction){if(mode==13&&!faceMode&&!paused&&!finished&&countdown<=0)headLane=fraction<1f/3?-1:fraction>2f/3?1:0;}
 void touchDown(){
  if(requiresMicrophone(mode)||paused||finished||countdown>0||liveSoundCue()||needsFaceNow())return;
  touchHolding=true;touchHoldTime=0;touchEncounter=encounter;touchAction=target();
  if(mode==3||mode==8||mode==9||mode==11||mode==12)voiceActive=true;
 }
 void touchUp(){
  if(!touchHolding)return;
  boolean valid=!paused&&!finished&&countdown<=0&&!liveSoundCue()&&touchEncounter==encounter&&touchAction==target();
  touchHolding=false;
  if(mode==3||mode==8||mode==9||mode==11||mode==12){voiceActive=false;return;}
  if(!valid)return;
  if(soundJump())jump(touchStrength(touchHoldTime));else input(touchAction);
 }
 void cancelTouch(){touchHolding=false;voiceActive=false;touchHoldTime=0;if(mode==8&&!resolved){charge=0;wasCharging=false;launchQuietTime=0;}}
 String jumpPrompt(){if(mode==4&&microphoneMode)return MelodyCourse.obstacle(stage,encounter)+": "+(highJump()?"stronger short sound":"gentle short sound");return microphoneMode?(highJump()?"Stronger sound: high jump":"Soft sound: low jump"):(highJump()?"Hold, release: high jump":"Quick tap: low jump");}
 String jumpHint(){return microphoneMode?"Short sound now - then quiet":highJump()?(touchHolding?"Release in the gold window":"Hold briefly; release in gold"):"Tap during the gold window";}
 boolean wasCharging,cometLanded,cometLaunched;
 float launchPower,launchQuietTime;
 int memoryRound,memoryIndex,soundPulses;
 float soundDuration,soundQuiet,glowProgress;
 boolean soundArmed,animalDemoPlaying;
 final int[] memoryFaces=new int[6];
 final boolean[] glowLit=new boolean[6];
 final java.util.Random memoryRandom;
 final float[] stackOffsets=new float[8];
 float stackDropOffset;
 boolean stackDropping,stackLanded,stackGood;
 float stackDropProgress(){return Math.max(0,Math.min(1,flightTime/.38f));}
 boolean stackAligned(){return phase>=.5f&&Math.abs(stackOffset())<=stackTolerance();}
 float elapsed, phase, countdown=2, height=.5f, velocity, flash;
 boolean paused, finished, won, resolved, stopped;
 int gateState;
 boolean gateMatched;
 float gateAnimation;
 String feedback="Get ready";
 MiniGame(int mode){this(mode,0);}
 MiniGame(int mode,int seed){this(mode,seed,1);}
 MiniGame(int mode,int seed,int stage){this.mode=mode;microphoneMode=requiresMicrophone(mode);this.stage=Math.max(1,Math.min(STAGE_COUNT,stage));hearts=this.stage==5?1:3;memoryRandom=new java.util.Random(seed);makeMemorySequence();racing=mode==12||mode==13?new RacingGame(mode,this.stage,seed):null;flags=mode==14?new FlagQuiz(seed,this.stage):null;}
 int goal(){return mode==14?4+stage:mode==12?100:mode==13?racing.trafficRequired():mode==9?(stage==5?AnimalPatterns.cues(stage):AnimalPatterns.cues(stage)-1):mode==11?extrasTarget():mode==10?3:mode==6?(stage==5?6:5):mode==7?(stage==5?8:stage>=3?7:6):mode==8?(stage==5?6:stage>=3?5:4):mode==2?12:mode==1||mode==3||mode==4?8:mode==0?5+stage:6;}
 int target(){if(mode==9)return AnimalPatterns.at(stage,encounter);if(mode==10)return memoryAt(memoryIndex);if(mode==0)return GameSections.faceAt(stage,score);if(mode==1)return encounter%3;if(mode==2||mode==5)return score%2;return 0;}
 String[] controls(){switch(mode){
 case 14:return new String[]{"Say or type country"};
 case 12:return new String[]{"Hold: accelerate"};
 case 13:return new String[]{"Left lane","Centre lane","Right lane"};
 case 0:return new String[]{"Jaw drop","Closed-lip smile","Show teeth","Eyes wide open","Face squeeze"};
 case 1:return new String[]{"Sound: jump","Closed-lip smile","Show teeth"};
 case 2:return new String[]{"Smile: plant","Blink: bloom"};
 case 3:return new String[]{"Sound: lift"};
 case 4:return new String[]{"Sound: jump"};
 case 6:return new String[]{"Open mouth"};
 case 7:return new String[]{"Closed-lip smile"};
 case 8:return new String[]{"Hum to charge; quiet to launch"};
 case 9:return new String[]{"Copy the animal rhythm"};
 case 10:return new String[]{"Open mouth","Closed-lip smile","Show teeth"};
 case 11:return new String[]{"Gentle steady sound"};
 default:return new String[]{"Smile: spark","Hum: star"};}}
 String prompt(){
  if(mode==0&&(gateMatched||gateState!=0))return gateState==2?"Next gate. You can do it!":"Lovely! Relax your face";
  if((mode==1||mode==4)&&resolved)return "Relax. Next action soon";
  switch(mode){
   case 14:return "Which country is this?";
   case 12:return microphoneMode?"Sound volume controls your speed":"Hold to accelerate; release to coast";
   case 13:return faceMode?"Move your head to a clear lane":"Tap left, middle or right lane";
   case 0:return new String[]{"Open your mouth","Smile with lips closed","Show your teeth","Open both eyes wide","Scrunch your face briefly"}[target()];
   case 1:return target()==0?jumpPrompt():target()==1?"Smile with lips closed":"Show your teeth";
   case 2:return target()==0?"Smile to plant a seed":"Blink once to bloom";
   case 3:return Math.abs(height-corridor())<.1f?"Keep steady - short gentle hums":height<corridor()?"Stay quiet to drift down":"Hum gently to rise";
   case 4:return jumpPrompt();
   case 6:return snackIsTreat()?"Open your mouth for the treat":"Keep your mouth closed";
   case 7:return "Smile when the block is centred";
   case 9:return animalRhythm();
   case 10:return memoryShowing()?"Watch and remember":"Repeat the face sequence";
   case 11:return "Keep a gentle steady sound";
   case 8:return charge>=powerTarget()-powerTolerance()&&charge<=powerTarget()+powerTolerance()?"Go quiet to launch now":"Hum gently to charge";
   default:return target()==0?"Smile to create a spark":"Hum gently to collect a star";
  }
 }
 String nextHint(){
  if(finished)return resultAction();
  if(paused)return "Resume when you are ready";
  if(countdown>0)return "Get comfortable. Follow the next cue.";
  if(mode==1||mode==4){
   if(resolved)return "Step "+(encounter+1)+" of 8 done. Next cue soon.";
   boolean window=mode==1?phase>=windowOpens()&&phase<=windowCloses():phase>=windowOpens()&&phase<=windowCloses();
   return "Step "+(encounter+1)+" of 8 - "+(window?"Act now":"Wait for the highlighted window");
  }
  if(mode==2)return "Flower "+Math.min(6,score/2+1)+" of 6 - "+(target()==0?"Plant first":"Now make it blossom");
  if(mode==3)return VoiceCourse.world(stage)+": obstacle "+Math.min(8,encounter+1)+" of 8";
  if(mode==5)return "Turn "+Math.min(6,score+1)+" of 6 - "+(target()==0?"Face cue":"Sound cue");
  return "Gate "+Math.min(6,score+1)+" of 6 - return to neutral after matching";
 }
 void acceptSoundLevel(float level,float strength,boolean active,boolean ready){boolean justReady=!micReady&&ready;soundStrength=strength;micLevel=mode==11?strength:level;micReady=ready;if(mode==9&&justReady&&!active&&!resolved){soundArmed=true;soundQuiet=0;}voiceSignal(active,ready);}
 float glowFlightProgress(){return Math.max(0,Math.min(1,glowProgress/glowRequired()));}
 float glowFlightX(){float start=encounter==0?195:65+((encounter-1)%3)*130,target=65+(encounter%3)*130;return start+(target-start)*glowFlightProgress();}
 float glowFlightY(float ground){float start=encounter==0?ground-45:ground-((encounter-1)<3?100:0)-15,target=ground-(encounter<3?100:0)-15,t=glowFlightProgress();return start+(target-start)*t-(float)Math.sin(t*Math.PI)*24;}
 void voiceSignal(boolean active,boolean ready){
  if(soundJump()){
   voiceActive=active&&ready;
   if(paused||finished||countdown>0||resolved){jumpSampling=false;jumpArmed=false;return;}
   if(!active&&ready)jumpArmed=true;
   if(active&&ready&&jumpArmed&&!jumpUsed&&!jumpSampling&&phase>=windowOpens()&&phase<=windowCloses()){jumpSampling=true;jumpSampleTime=0;jumpPeak=soundStrength;jumpArmed=false;}
   if(jumpSampling)jumpPeak=Math.max(jumpPeak,soundStrength);
   return;
  }
  if(mode==3||mode==8||mode==9||mode==11||mode==12){voiceActive=active&&ready;return;}
  if(mode!=5||paused||finished||countdown>0){duetSoundArmed=false;duetSoundPrevious=active;return;}
  if(target()!=1)duetSoundArmed=false;
  else if(ready){
   if(!active)duetSoundArmed=true;
   if(active&&!duetSoundPrevious&&duetSoundArmed){input(1);duetSoundArmed=false;}
  }
  duetSoundPrevious=active;
 }
 void input(int action){
  if(finished||paused||faceBlocked()||countdown>0||flash>.28f||action<0||action>=controls().length)return;
  if(mode==13){if(!faceMode)headLane=action-1;return;}
  if(mode==12||mode==14)return;
  if(mode==0){
   if(gateState!=0||gateMatched)return;
   if(action!=target()){feedback="Try the expression on the gate";return;}
   gateMatched=true;flash=.7f;feedback="Matched! Relax while your hero passes.";return;
  }
  if(mode>=9){
   if(mode==11)return;
   if(resolved)return;
   if(mode==9)return;
   if(mode==10){
    if(memoryShowing()){feedback="Watch the whole sequence first";return;}
    if(action!=memoryAt(memoryIndex)){memoryIndex=0;phase=0;miss("Try again - watch the sequence");return;}
    memoryIndex++;flash=.55f;feedback="Face matched";
    if(memoryIndex>=memoryLength()){score++;awardCoin();resolved=true;flightTime=0;flash=.9f;}return;
   }
   voiceActive=true;micLevel=.5f;return;
  }
  if(mode>=6){
   if(resolved)return;
   if(mode==6){
    if(phase<snackWindowOpens()||phase>snackWindowCloses()){feedback="Wait for the treat window";return;}
    resolved=true;flash=.7f;
    if(snackIsTreat()){score++;awardCoin();}else{earnedCoin=false;miss("Pepper! Keep your mouth closed");}return;
   }
   if(mode==7){
    if(phase<.5f){feedback="Wait for the moving block";return;}
    resolved=true;flash=0;flightTime=0;earnedCoin=false;stackDropping=true;stackLanded=false;stackDropOffset=stackOffset();stackGood=Math.abs(stackDropOffset)<=stackTolerance();feedback="Block dropping - relax your smile";return;
   }
   return;
  }
  if(soundJump()){if(action==0&&!microphoneMode)jump(mode==4?melodyPracticeStrength():highJump()?.85f:.35f);return;}
  if(mode==3){if(resolved)return;velocity=-.24f;feedback="Float up!";flash=.4f;return;}
  if(mode==1){if(resolved)return;if(phase<windowOpens()||phase>windowCloses()){feedback="Wait for the action window";return;}}
  if(mode==4){if(resolved)return;if(phase<windowOpens()||phase>windowCloses()){feedback="Wait for the note ring";return;}}
  if(action!=target()){feedback="Try the highlighted action";return;}
  if(mode==1||mode==2||mode==5&&action==1){coins++;coinBalance++;earnedCoin=true;}else if(mode==5)earnedCoin=false;
  score++;flash=.7f;feedback=mode==2?(score%2==0?"A new flower!":"Seed planted!"):mode==4?"Lovely timing!":"Lovely!";
  if(mode==1||mode==5&&action==1)feedback="Good job! +1 coin";
  else if(mode==5)feedback="Spark ready! Now hum gently";
  if(mode==1||mode==4)resolved=true;else phase=0;
  if(score>=goal())finish(true);
 }
 void step(float delta){
  if(requiresMicrophone(mode)&&!microphoneMode||mode==9&&animalDemoPlaying||paused||finished||faceBlocked()||(mode==12||mode==3||mode==4||mode==8||mode==9||mode==11)&&microphoneMode&&!micReady||delta<=0||!Float.isFinite(delta))return;
  float realDt=Math.min(delta,.05f),dt=realDt*pace();flash=Math.max(0,flash-dt);
  if(mode==13&&headGrace>0){headGrace=Math.max(0,headGrace-realDt);racing.steer(dt,headLane);return;}
  if(countdown>0){countdown=Math.max(0,countdown-realDt);if(countdown==0){feedback="Your turn!";if(mode==9&&!voiceActive&&(!microphoneMode||micReady))soundArmed=true;}return;}
  elapsed+=dt;phase+=dt;if(touchHolding)touchHoldTime+=realDt;
  if(soundJump()){
   if(jumpSampling){jumpSampleTime+=realDt;if(jumpSampleTime>=.15f){jumpSampling=false;jump(jumpPeak);}}
   if(jumpUsed&&!(mode==4&&resolved)){jumpHeight=Math.max(0,jumpHeight+jumpVelocity*dt-500*dt*dt);jumpVelocity-=1000*dt;}
   if(!resolved&&phase>=2.65f){
    resolved=true;flash=.7f;
    if(jumpHeight>=jumpRequired()&&(mode!=4||jumpHeight<=jumpRequired()+MelodyCourse.ceilingMargin(stage,encounter))){score++;awardCoin();if(mode==4){jumpHeight=jumpRequired();jumpVelocity=0;}}
    else if(mode==4){earnedCoin=false;melodyFalling=true;melodyImpact=false;melodyOutcomeTime=0;melodyMissHeight=jumpHeight;feedback=jumpHeight>jumpRequired()?"Too much power - hit the overhang!":jumpUsed?"Too low - falling into the fire!":"No jump - falling into the fire!";}
    else miss(jumpUsed?"Jump too low or mistimed":"Make a sound for the next jump");
   }
  }
  if(mode==14){
   if(quizProcessing){phase-=dt;return;}
   if(resolved){flightTime+=dt;if(flightTime>=1.2f){encounter++;phase=0;resolved=false;earnedCoin=false;quizStatus="Say the country name, or tap to type";}}
   else if(phase>=flagLimit()){phase=0;flagAttempt++;quizListening=false;quizProcessing=false;earnedCoin=false;miss("Time is up - try this flag again");quizStatus="Try again: say the country name";}return;
  }
  if(mode==12||mode==13){
   int oldCrashes=racing.crashes,oldCoins=racing.coins;
   float throttle=raceThrottle();
   racing.step(dt,throttle,headLane);
   score=mode==12?Math.min(100,(int)(racing.playerDistance/RacingGame.FINISH*100)):racing.dodged;encounter=racing.rows;
   if(racing.coins>oldCoins){int reward=racing.coins-oldCoins;coins+=reward;coinBalance+=reward;earnedCoin=true;flash=.7f;feedback="Good job! +"+reward+" coin";}
   if(racing.crashes>oldCrashes){earnedCoin=false;flash=.8f;miss("Traffic hit - watch the clear lane");}
   if(!finished&&racing.result!=0){if(mode==12&&racing.result>0)score=100;finish(racing.result>0);}return;
  }
  if(mode>=9){stepExtras(dt,realDt);return;}
  if(mode>=6){stepCollection(dt,realDt);return;}
  if(mode==0){
   if(gateState==0&&phase>=gateApproach()){gateState=gateMatched?1:2;gateAnimation=0;feedback=gateMatched?"Through the gate!":"Try the next gate";if(!gateMatched)hearts--;}
   else if(gateState!=0){
    gateAnimation+=dt;
    if(gateAnimation>=.95f){
     if(gateState==1){score++;awardCoin();}
     encounter++;
     if(hearts<=0)finish(false);
     else if(score>=goal())finish(true);
     else{gateState=0;gateAnimation=0;gateMatched=false;phase=0;feedback="Follow the next expression";}
    }
   }
  }else if(mode==3){
   if(!resolved){
    velocity+=((voiceActive?-.8f:.24f)+VoiceCourse.wind(stage,encounter,phase))*dt;velocity=Math.max(-.3f,Math.min(.24f,velocity));height+=velocity*dt;
    if(height<.08f){height=.08f;velocity=.05f;}if(height>.92f){height=.92f;velocity=-.05f;}
   }
   if(phase>=voiceApproach()&&!resolved){
    resolved=true;earnedCoin=Math.abs(height-corridor())<=voiceTolerance();
    if(earnedCoin){score++;coins++;coinBalance++;feedback="Good job! +1 coin";flash=.9f;}
    else miss("Hit "+VoiceCourse.obstacleName(stage,encounter).toLowerCase(java.util.Locale.ROOT)+" - try the next gap");
   }
   if(phase>=voiceApproach()+.9f){encounter++;phase=0;resolved=false;earnedCoin=false;if(encounter>=8)finish(score>=voiceTarget()&&hearts>0);}
  }else if(mode==4){
   if(resolved){
    melodyOutcomeTime+=realDt;
    if(melodyFalling&&!melodyImpact&&melodyOutcomeTime>=.55f){melodyImpact=true;flash=.9f;miss("Fell into the fire - one life lost");}
    if(!finished&&melodyOutcomeTime>=1.05f){phase=0;encounter++;resolved=false;earnedCoin=false;jumpUsed=false;jumpHeight=0;jumpVelocity=0;jumpSampling=false;jumpArmed=false;melodyFalling=false;melodyImpact=false;melodyOutcomeTime=0;melodyMissHeight=0;if(encounter>=8)finish(score>=timedTarget()&&hearts>0);}
   }
  }else if(mode==1){
   if(phase>=3.15f&&!resolved){resolved=true;miss("Next one is yours!");}
   if(phase>=3.8f){phase=0;encounter++;resolved=false;earnedCoin=false;jumpUsed=false;jumpHeight=0;jumpVelocity=0;jumpSampling=false;jumpArmed=false;if(encounter>=8)finish(score>=timedTarget()&&hearts>0);}
  }else if(phase>=cueTimeout()){phase=0;miss("Take a breath. Try again.");}
  if(elapsed>=(mode==0?65+(stage-1)*8:mode==2?60:48))finish(score>=goal());
 }
 int memoryLength(){return 2+Math.min(2,(stage-1)/2)+memoryRound;}
 int memoryAt(int index){return memoryFaces[Math.min(5,Math.max(0,index))];}
 private void makeMemorySequence(){for(int i=0;i<6;i++){int choice=memoryRandom.nextInt(3);if(i>0&&choice==memoryFaces[i-1])choice=(choice+1+memoryRandom.nextInt(2))%3;memoryFaces[i]=choice;}}
 float memoryShowDuration(){return .55f+memoryLength()*1.1f+.45f;}
 boolean memoryShowing(){return phase<memoryShowDuration();}
 int memoryWatchIndex(){if(phase<.55f||phase>=.55f+memoryLength()*1.1f)return -1;return (int)((phase-.55f)/1.1f);}
 boolean memoryFaceVisible(){return memoryWatchIndex()>=0&&(phase-.55f)%1.1f<.85f;}

 int animalPulsesRequired(){return AnimalPatterns.get(target()).calls.length;}
 float animalMinDuration(){return AnimalPatterns.get(target()).minimum(soundPulses);}
 float animalMaxDuration(){return AnimalPatterns.get(target()).maximum(soundPulses);}
 String animalRhythm(){return AnimalPatterns.get(target()).words;}
 void resetAnimalAttempt(){if(mode==9&&!finished&&!resolved){phase=0;voiceActive=false;cancelTouch();resetSoundPattern();}}
 private void rejectAnimal(String reason){resolveAnimal(false);if(!finished)feedback=reason;}
 private void resolveAnimal(boolean success){
  resolved=true;flightTime=0;flash=.9f;earnedCoin=success;voiceActive=false;
  if(success){score++;awardCoin();}else miss("Try the next animal pattern");
 }
 private void resetSoundPattern(){soundPulses=0;soundDuration=0;soundQuiet=0;soundArmed=false;}
 private void stepExtras(float dt,float realDt){
  if(mode==10){
   if(resolved){flightTime+=dt;if(flightTime>=.9f){if(score>=3)finish(true);else{memoryRound++;makeMemorySequence();memoryIndex=0;phase=0;resolved=false;earnedCoin=false;}}}
   else if(phase>memoryShowDuration()+10){memoryIndex=0;phase=0;miss("Time to watch the sequence again");}
  }else if(resolved){
   flightTime+=dt;if(flightTime>=1){encounter++;phase=0;resolved=false;earnedCoin=false;voiceActive=false;glowProgress=0;resetSoundPattern();if(encounter>=(mode==9?AnimalPatterns.cues(stage):6))finish(score>=goal()&&hearts>0);}
  }else if(mode==9){
   if(microphoneMode?micReady:touchSoundPatterns){
    if(!soundArmed){if(!voiceActive)soundQuiet+=realDt;else soundQuiet=0;if(soundQuiet>=.2f){soundArmed=true;soundQuiet=0;}}
    else if(voiceActive){
     soundDuration+=realDt;soundQuiet=0;
     if(soundDuration>animalMaxDuration()+.1f)rejectAnimal(target()==1?"Meow too long - try the next cue":"Keep each call short - try the next cue");
    }else{
     soundQuiet+=realDt;
     // Confirm a real pause before closing a call; brief dropouts stay in one call.
     if(soundDuration>0&&soundQuiet>=.10f){
      float duration=soundDuration;soundDuration=0;
      if(duration>=.045f){
       if(duration<animalMinDuration())rejectAnimal(target()==1?"Stretch one meow a little longer":"Make each call a little clearer");
       else if(duration>animalMaxDuration())rejectAnimal("Keep each call shorter");
       else if(++soundPulses>animalPulsesRequired())rejectAnimal("Too many calls - follow the rhythm");
      }
     }
     if(!resolved&&soundPulses==animalPulsesRequired()&&soundQuiet>=.35f)resolveAnimal(true);
    }
   }
   if(phase>=cueTimeout()&&!resolved)rejectAnimal("Time up - copy the next rhythm");
  }else{
   float level=micLevel;
   if(microphoneMode&&micReady&&voiceActive&&level>=glowLow()&&level<=glowHigh())glowProgress+=realDt;
   else glowProgress=Math.max(0,glowProgress-realDt*.45f);
   if(glowProgress>=glowRequired()){glowLit[encounter]=true;score++;awardCoin();resolved=true;flightTime=0;flash=.9f;voiceActive=false;}
   else if(phase>=7){resolved=true;flightTime=0;earnedCoin=false;voiceActive=false;miss("Let the next firefly glow");}
  }
  if(mode!=9&&elapsed>=80)finish(false);
 }
 boolean snackIsTreat(){return GameSections.treat(stage,encounter);}
 float stackOffset(){float t=phase*stackSpeed()+encounter*.7f;float wave=stage<3?(float)Math.sin(t):(float)(.85*Math.sin(t)+.15*Math.sin(t*(stage==5?2.5:2)));return wave*wave*wave;}
 float powerTarget(){return .35f+((encounter+stage-1)%4)*.13f;}
 private void awardCoin(){coins++;coinBalance++;earnedCoin=true;feedback="Good job! +1 coin";}
 boolean cometInGold(){return Math.abs(charge-powerTarget())<=powerTolerance();}
 boolean cometHitsTarget(){return Math.abs(launchPower-powerTarget())<=powerTolerance();}
 float cometFlightProgress(){return resolved&&cometLaunched?Math.min(1,flightTime/1.1f):0;}
 float cometLandingX(){return Math.max(70,Math.min(354,303+(launchPower-powerTarget())*260));}
 float cometFlightX(){return 85+(cometLandingX()-85)*cometFlightProgress();}
 float cometFlightBottom(float ground){float t=cometFlightProgress();return ground-52-t*(cometHitsTarget()?63:12)-(float)Math.sin(t*Math.PI)*55;}
 private void launchComet(){
  if(resolved||charge<.04f)return;
  resolved=true;cometLanded=false;cometLaunched=true;flightTime=0;flash=0;earnedCoin=false;launchPower=charge;launchQuietTime=0;wasCharging=false;feedback="Launched! Watch the landing";
 }
 private void stepCollection(float dt,float realDt){
  if(mode==6){
   if(phase>=2.55f&&!resolved){resolved=true;flash=.7f;earnedCoin=false;if(snackIsTreat())miss("Treat missed - catch the next one");else feedback="Good job! Pepper avoided";}
   if(phase>=3.2f){encounter++;phase=0;resolved=false;earnedCoin=false;if(encounter>=9)finish(score>=goal()&&hearts>0);}
  }else if(mode==7){
   if(!resolved&&phase>=3){resolved=true;flightTime=0;stackDropping=false;stackLanded=true;earnedCoin=false;miss("Block missed - smile at the gold line");}
   if(resolved){
    flightTime+=realDt;
    if(stackDropping&&!stackLanded&&flightTime>=.38f){stackLanded=true;flash=.9f;if(stackGood){stackOffsets[score]=stackDropOffset;score++;awardCoin();}else miss("Off centre - watch the gold line");}
    if(!finished&&flightTime>=1.05f){encounter++;phase=0;flightTime=0;resolved=false;earnedCoin=false;stackDropping=false;stackLanded=false;if(encounter>=8)finish(score>=goal()&&hearts>0);}
   }
  }else{
   if(!resolved){
    if(voiceActive){charge=Math.min(1,charge+.28f*dt);wasCharging=true;launchQuietTime=0;}
    else if(wasCharging){launchQuietTime+=realDt;if(!microphoneMode||launchQuietTime>=.10f)launchComet();}
    if(phase>=8&&!resolved){resolved=true;cometLanded=true;earnedCoin=false;flightTime=1.1f;launchPower=charge;miss("Launch window missed - charge and release in gold");}
   }else{
    flightTime+=dt;
    if(!cometLanded&&flightTime>=1.1f){cometLanded=true;flash=1;if(cometHitsTarget()){score++;awardCoin();}else miss(launchPower<powerTarget()?"Too little power - rocket fell short":"Too much power - rocket flew past");}
    if(!finished&&flightTime>=1.7f){encounter++;phase=0;charge=0;launchPower=0;launchQuietTime=0;flightTime=0;wasCharging=false;cometLanded=false;cometLaunched=false;resolved=false;earnedCoin=false;if(encounter>=6)finish(score>=goal()&&hearts>0);}
   }
  }
  if(mode!=9&&elapsed>=80)finish(false);
 }
  float windowOpens(){return soundJump()?(microphoneMode?2.05f+(stage-1)*.0125f-.15f*(pace()-1):highJump()?2.05f+(stage-1)*.02f:2.15f+(stage-1)*.01f):(mode==1?1:1.2f)+(stage-1)*.06f;}
 float windowCloses(){return soundJump()?(microphoneMode?2.30f-(stage-1)*.01f-.15f*(pace()-1):2.45f-(stage-1)*.02f):(mode==1?3:2.8f)-(stage-1)*.06f;}
 float cueTimeout(){return mode==2?9-(stage-1)*1.4f:mode==9?9-(stage-1)*.6f:7-(stage-1)*.65f;}
 float stackSpeed(){return 1.8f+(stage-1)*.1f;}
 float gateApproach(){return 5.5f-(stage-1)*.6f;}
 float voiceApproach(){return 4-(stage-1)*.2f;}
 float glowLow(){return .22f+(stage-1)*.02f;}
 float glowHigh(){return .78f-(stage-1)*.02f;}
 float snackWindowOpens(){return .9f+(stage-1)*.06f;}
 float snackWindowCloses(){return 2.4f-(stage-1)*.06f;}
 float stackTolerance(){return .3f-(stage-1)*.025f;}
 float powerTolerance(){return .12f-(stage-1)*.01f;}
 float voiceTolerance(){return .25f-(stage-1)*.02f;}
 int timedTarget(){return stage==5?8:stage>=3?7:6;}
 int voiceTarget(){return stage==5?8:stage>=3?6:5;}
 float glowRequired(){return 1.5f+(stage-1)*.15f;}
 float pace(){return paceAtStage(stage);}
 static float paceAtStage(int stage){switch(stage){case 2:return 1.25f;case 3:return 1.5f;case 4:return 1.8f;case 5:return 2.1f;default:return 1;}}
 int extrasTarget(){return stage==5?6:5;}
 String paceLabel(){return String.format(java.util.Locale.US,"%.2fx",pace());}
 String nextPaceLabel(){return String.format(java.util.Locale.US,"%.2fx",paceAtStage(nextRunStage()));}
 String stageName(){if(mode==3)return VoiceCourse.world(stage);if(GameSections.applies(mode))return GameSections.at(mode,stage).name;return new String[]{"First steps","Find your rhythm","Growing confidence","Mastery","Grand finale"}[stage-1];}
 float corridor(){if(mode==3)return VoiceCourse.target(stage,encounter,phase);return .5f+(float)Math.sin((encounter+stage-1)*1.7)*(.15f+(stage-1)*.02f);}

 void stopRound(){if(!finished){finished=true;won=false;stopped=true;feedback="Round stopped";}}
 String resultTitle(){return stopped?"Round stopped":won?(stage<STAGE_COUNT?"Stage "+stage+" complete!":"All stages complete!"):"Mission failed";}
 String resultAction(){return won?(stage<STAGE_COUNT?"Next stage":"Play again"):stopped?"Play again":"Try again";}
 private void miss(String message){hearts--;feedback=message;if(hearts<=0)finish(false);}
 private void finish(boolean success){finished=true;won=success;feedback=success?"Mission complete":"Mission failed";}
}
