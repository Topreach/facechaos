package com.facechaos.mobile;
public final class FaceInputCheck {
 static void require(boolean b,String m){if(!b)throw new AssertionError(m);}
 static FaceSignals calibrated(){FaceSignals f=new FaceSignals();for(int i=0;i<12;i++)f.accept(.03f,.05f,.02f,0,i*125,1);require(f.ready,"Neutral calibration completes");return f;}
 static int detect(FaceSignals f,int expression,long start){float jaw=expression==0?.75f:.03f,smile=expression==1?.8f:.05f,blink=expression==3?.85f:.02f,teeth=expression==2?.9f:0;int found=-1;for(int i=0;i<4;i++){int event=f.accept(jaw,smile,blink,teeth,start+i*125,1);if(event>=0)found=event;}return found;}
 static void neutral(FaceSignals f,long start){for(int i=0;i<3;i++)f.accept(.03f,.05f,.02f,0,start+i*125,1);}
 public static void main(String[] args){
  for(int expression=0;expression<4;expression++){FaceSignals f=calibrated();require(detect(f,expression,2000)==expression,"Distinct expression "+expression);require(detect(f,expression,3000)==-1,"Held expression fires once");neutral(f,4000);require(detect(f,expression,4500)==expression,"Neutral rearms expression");f.lost();require(detect(f,expression,6000)==-1,"Lost tracking requires neutral before rearm");}
  FaceSignals f=calibrated();require(f.accept(.8f,.8f,.02f,.9f,2000,1)==-1,"Ambiguous tooth display waits for stability");require(f.accept(.8f,.8f,.02f,.9f,2200,1)==2,"Actual teeth evidence has priority over mouth/smile");
  FaceSignals uncalibrated=new FaceSignals();for(int i=0;i<20;i++)uncalibrated.accept(.8f,0,0,0,i*125,1);require(!uncalibrated.ready,"Open mouth cannot become neutral baseline");
  int[] image=new int[80*40];float[] xs={10,70,70,10},ys={5,5,35,35};java.util.Arrays.fill(image,0xFF301010);require(TeethEvidence.measure(image,80,40,xs,ys)==0,"Dark open mouth has no teeth evidence");java.util.Arrays.fill(image,0xFFE9A4A2);require(TeethEvidence.measure(image,80,40,xs,ys)==0,"Pink lip/tongue pixels are not teeth");java.util.Arrays.fill(image,0xFFF2EEE0);require(TeethEvidence.measure(image,80,40,xs,ys)>.8f,"White/ivory mouth pixels give direct evidence");java.util.Arrays.fill(image,0xFF301010);for(int y=5;y<15;y++)for(int x=10;x<70;x++)image[y*80+x]=0xFFF2EEE0;require(TeethEvidence.measure(image,80,40,xs,ys)>.6f,"One visible tooth row counts as Show teeth");
  for(int mode:new int[]{0,1,2,5,6,7,10}){
   MiniGame g=new MiniGame(mode);g.countdown=0;g.faceMode=true;g.faceReady=false;float phase=g.phase;g.step(.02f);if(g.needsFaceNow())require(g.phase==phase,"Missing face freezes action mode "+mode);
   g.faceReady=true;if(mode==1){g.encounter=1;g.phase=1.6f;}if(mode==6)g.phase=1.5f;if(mode==7){g.phase=.5f;while(Math.abs(g.stackOffset())>.1f)g.phase+=.01f;}if(mode==10)g.phase=g.memoryShowDuration()+.1f;
   int expected=mode==2||mode==5||mode==7||mode==1?1:mode==10?g.target():0;
   FaceGameInput.send(g,expected);
   if(mode==7)for(int tick=0;tick<20;tick++)g.step(.02f);
   require(mode==0?g.gateMatched:mode==10?g.memoryIndex==1:g.score==1,"Face event routes into mode "+mode);
  }
  MiniGame gates=new MiniGame(0);gates.countdown=0;gates.faceMode=true;gates.faceReady=true;gates.touchDown();gates.touchUp();require(!gates.gateMatched,"Touch cannot masquerade as face input");FaceGameInput.send(gates,1);require(!gates.gateMatched,"Wrong expression cannot clear gate");FaceGameInput.send(gates,0);require(gates.gateMatched,"Mouth event clears mouth gate");gates.faceReady=false;int hearts=gates.hearts;for(int i=0;i<600;i++)gates.step(.02f);require(gates.hearts==hearts,"Face loss never costs lives");
  System.out.println("PASS: neutral calibration, four distinct reactions, teeth pixel negatives, held/rearm/loss handling and all seven face-game adapters");
 }
}
