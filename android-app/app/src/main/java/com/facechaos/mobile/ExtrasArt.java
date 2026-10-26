package com.facechaos.mobile;

import android.content.Context;
import android.graphics.*;

/** Animal patterns, expression memory and a gentle sound lantern garden. */
final class ExtrasArt {
    private static Bitmap shared,sharedFaces,sharedChoir;
    private final Bitmap animals,faces,choir;
    private final Paint p=new Paint(3);
    private final RectF dst=new RectF();
    private final Rect[] pets={new Rect(25,8,510,499),new Rect(575,10,1005,500),new Rect(1040,55,1525,490),new Rect(100,495,415,1000),new Rect(560,525,995,950),new Rect(1135,515,1470,990)};
    private final Rect[] expressions={new Rect(48,30,465,440),new Rect(560,30,976,440),new Rect(1073,30,1490,440)};
    ExtrasArt(Context context){if(shared==null){BitmapFactory.Options options=new BitmapFactory.Options();options.inScaled=false;shared=BitmapFactory.decodeResource(context.getResources(),R.drawable.animal_atlas,options);}animals=shared;if(sharedFaces==null)sharedFaces=BitmapFactory.decodeResource(context.getResources(),R.drawable.emoji_atlas);faces=sharedFaces;if(sharedChoir==null)sharedChoir=BitmapFactory.decodeResource(context.getResources(),R.drawable.animal_choir_atlas);choir=sharedChoir;}
    private void draw(Canvas c,Bitmap bitmap,Rect source,float x,float bottom,float width,int alpha){dst.set(x-width/2,bottom-width*source.height()/source.width(),x+width/2,bottom);p.setColor(Color.WHITE);p.setAlpha(alpha);c.drawBitmap(bitmap,source,dst,p);p.setAlpha(255);}
    private final Rect[] choirSprites={new Rect(12,0,350,362),new Rect(390,0,719,362),new Rect(736,25,1077,362),new Rect(1080,0,1448,362),new Rect(16,368,364,710),new Rect(398,367,716,711),new Rect(748,365,1060,713),new Rect(1097,393,1448,719),new Rect(4,703,357,1086),new Rect(375,708,724,1086),new Rect(728,709,1081,1086),new Rect(1079,712,1448,1086)};
    void choirPet(Canvas c,int animal,float x,float bottom,float width){draw(c,choir,choirSprites[Math.floorMod(animal,12)],x,bottom,width,255);}
    void pet(Canvas c,int index,float x,float bottom,float width){draw(c,animals,pets[index],x,bottom,width,255);}
    void face(Canvas c,int index,float x,float bottom,float width){draw(c,faces,expressions[index],x,bottom,width,255);}
    void tutorial(Canvas c,float w,float h,int mode,int action){
        c.drawColor(mode==9?0xFFFFF2E4:mode==10?0xFFEFF2FF:0xFFF5EDFF);
        if(mode==9){if(action<0){for(int i=0;i<12;i++)choirPet(c,i,w*((i%4)+.5f)/4,h*((i/4)+1)/3,Math.min(w*.21f,h*.29f));}else choirPet(c,action,w*.5f,h*.95f,h*.86f);}
        else if(mode==10){for(int i=0;i<3;i++){face(c,i,w*(i+.5f)/3,h*.88f,Math.min(w*.26f,h*.73f));label(c,String.valueOf(i+1),w*(i+.5f)/3,h*.98f,12,0xFF52658B);}}
        else{pet(c,4,w*.3f,h*.82f,h*.7f);pet(c,3,w*.73f,h*.98f,h*.53f);}
    }
    void scene(Canvas c,MiniGame g,float height,float safeTop,float controls){
        float ground=height-Math.max(110,controls)+4;
        if(g.mode==9){
            p.setColor(g.animalDemoPlaying?0xFFF8DE9D:0xE82C555D);c.drawRoundRect(115,ground-190,275,ground-156,12,12,p);label(c,g.animalDemoPlaying?"LISTEN...":"HEAR PATTERN",195,ground-168,12,g.animalDemoPlaying?0xFF244C53:Color.WHITE);
            AnimalPatterns.Entry pattern=AnimalPatterns.get(g.target());int calls=pattern.calls.length;float gap=12,total=0;for(float duration:pattern.calls)total+=duration>=.55f?2:1;float unit=(240-gap*(calls-1))/total,x=75;
            for(int i=0;i<calls;i++){float width=unit*(pattern.duration(i)>=.55f?2:1);p.setColor(i<g.soundPulses?0xFF80DFC0:0x99728C97);c.drawRoundRect(x,ground-140,x+width,ground-123,7,7,p);if(i==g.soundPulses&&g.soundDuration>0){p.setColor(0xFFFFD778);float fill=Math.min(1,g.soundDuration/pattern.duration(i));c.drawRoundRect(x,ground-140,x+width*fill,ground-123,7,7,p);}x+=width+gap;}
            for(int i=0;i<3;i++){float cx=65+i*130;int cue=g.encounter+(i-1);if(cue<0)cue=AnimalPatterns.cues(g.stage)-1;int animal=AnimalPatterns.at(g.stage,cue);boolean selected=i==1;p.setColor(selected?0x77FFE1A5:0x338FB7AA);c.drawOval(cx-43,ground-9,cx+43,ground+8,p);choirPet(c,animal,cx,ground-(selected&&g.voiceActive?(float)Math.sin(g.elapsed*12)*3:0),selected?110:77);}
            label(c,pattern.name+" - animal "+Math.min(AnimalPatterns.cues(g.stage),g.encounter+1)+" / "+AnimalPatterns.cues(g.stage),195,ground+34,12,Color.WHITE);
        }else if(g.mode==10){
            drawMemory(c,g,ground);
        }else{
            for(int i=0;i<6;i++){float x=65+(i%3)*130,y=ground-(i<3?100:0);int alpha=g.glowLit[i]?255:i==g.encounter?(int)(125+130*Math.min(1,g.glowProgress/g.glowRequired())):70;draw(c,animals,pets[3],x,y,45,alpha);}
            pet(c,4,g.glowFlightX(),g.glowFlightY(ground),58);
            label(c,"Lantern "+Math.min(6,g.encounter+1)+" / 6",195,ground+37,12,Color.WHITE);
        }
        if(!g.finished)popup(c,g,safeTop);
    }
    static int memoryChoiceAt(float x,float y,float ground){
        if(y<ground-102||y>ground+17)return -1;
        for(int i=0;i<3;i++)if(x>=12+i*130&&x<=118+i*130)return i;
        return -1;
    }
    private void drawMemory(Canvas c,MiniGame g,float ground){
        boolean watch=g.memoryShowing()&&!g.resolved;int current=g.memoryWatchIndex();
        float start=195-(g.memoryLength()*50+(g.memoryLength()-1)*8)/2f;
        for(int i=0;i<g.memoryLength();i++){
            float x=start+i*58;boolean shown=watch&&i<=current&&current>=0||!watch&&i<g.memoryIndex;
            p.setColor(shown?0xFFFDE9B2:0xCBE6EDF5);c.drawRoundRect(x,ground-181,x+50,ground-128,12,12,p);
            if(shown)face(c,g.memoryAt(i),x+25,ground-136,34);else label(c,"?",x+25,ground-145,22,0xFF7A8BA3);
            if(watch&&i==current){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3);p.setColor(0xFFFFCB65);c.drawRoundRect(x,ground-181,x+50,ground-128,12,12,p);p.setStyle(Paint.Style.FILL);}
        }
        label(c,watch?"WATCH - remember the order":g.resolved?"Sequence complete!":g.faceMode?"YOUR TURN - make the faces in order":"YOUR TURN - tap the faces in order",195,ground-111,11,Color.WHITE);
        for(int i=0;i<3;i++){
            float x=12+i*130;boolean correct=!watch&&g.memoryIndex>0&&g.flash>0&&g.memoryAt(g.memoryIndex-1)==i;
            p.setColor(watch?0xB9DFE5EC:correct?0xFFFFE2A0:new int[]{0xFFFCE4CD,0xFFDCEEDD,0xFFEBDDFA}[i]);c.drawRoundRect(x,ground-102,x+106,ground+17,17,17,p);
            draw(c,faces,expressions[i],x+53,ground-22,66,watch?120:255);
            label(c,new String[]{"Open mouth","Closed-lip smile","Show teeth"}[i],x+53,ground+4,10,0xFF4C536F);
        }
        label(c,"Sequence "+Math.min(3,g.memoryRound+1)+" / 3  -  "+g.memoryLength()+" faces",195,ground+39,12,Color.WHITE);
    }
    private void memoryPopup(Canvas c,MiniGame g,float safeTop){
        float y=safeTop+82;boolean watch=g.memoryShowing()&&!g.resolved;
        boolean success=g.resolved&&g.earnedCoin,matched=!watch&&g.memoryIndex>0&&g.flash>0&&!g.resolved;
        p.setColor(success?0xFFFFEAB1:matched?0xFFE2F3DC:0xF2EDF2FF);c.drawRoundRect(20,y,370,y+88,20,20,p);
        if(watch&&g.memoryFaceVisible())face(c,g.memoryAt(g.memoryWatchIndex()),75,y+76,61);
        else{p.setColor(0xFFD3DEF0);c.drawRoundRect(47,y+18,103,y+72,12,12,p);label(c,success?"+1":watch?"...":"?",75,y+54,22,0xFF61799A);}
        String cue=g.paused?"Paused":g.countdown>0?"Ready in "+(int)Math.ceil(g.countdown):success?"Good job! +1 coin":matched?"Good job! Face matched":watch?g.memoryFaceVisible()?new String[]{"Open your mouth","Smile with lips closed","Show your teeth"}[g.memoryAt(g.memoryWatchIndex())]:g.phase<.55f&&g.hearts<3?"Try again - watch closely":"Watch and remember":"Your turn - repeat the order";
        String hint=g.paused?"Resume when you are ready":success?"Next sequence coming up":watch?g.memoryWatchIndex()>=0?"Remember face "+(g.memoryWatchIndex()+1)+" of "+g.memoryLength():"Remember all "+g.memoryLength()+" faces":"Choose face "+Math.min(g.memoryLength(),g.memoryIndex+1)+" of "+g.memoryLength()+(g.faceMode?" - make it, then relax":" - tap the answer");
        label(c,cue,225,y+33,15,0xFF485F84);label(c,hint,225,y+59,10,0xFF71829D);
    }

    private void popup(Canvas c,MiniGame g,float safeTop){
        if(g.mode==10){memoryPopup(c,g,safeTop);return;}
        float y=safeTop+82;boolean reward=g.resolved&&g.earnedCoin;p.setColor(reward?0xF5FFF0B3:0xF2FFF5EA);c.drawRoundRect(20,y,370,y+88,20,20,p);
        if(g.mode==10)face(c,1,75,y+74,59);else if(g.mode==9)choirPet(c,g.target(),75,y+75,58);else pet(c,4,75,y+75,58);
        String cue=g.paused?"Paused":g.countdown>0?"Ready in "+(int)Math.ceil(g.countdown):reward?"Good job! +1 coin":g.resolved?"Try the next cue":g.mode==9?g.microphoneMode&&!g.micReady?"Stay quiet for a second":g.animalRhythm():g.mode==10?g.memoryShowing()?"Watch and remember":"Repeat the faces":g.microphoneMode&&!g.micReady?"Stay quiet for a second":g.micLevel>g.glowHigh()?"Softer hum to guide the firefly":"Hum gently to fly to the lantern";
        label(c,cue,225,y+31,15,0xFF654565);
        String hint=g.paused?"Resume when you are ready":g.mode==9?g.microphoneMode?"Rhythm pattern, not animal identity":"Copy the short / long beat markers":g.mode==10?g.memoryShowing()?"Remember the order":g.faceMode?"Make each face, then relax":"Choose the face buttons below":"Flight "+Math.round(g.glowFlightProgress()*100)+"% - sound only, stay in gold";
        label(c,hint,225,y+53,10,0xFF8B728B);
        if(g.mode!=10){
            float level=g.mode==9?Math.min(1,g.soundPulses/(float)g.animalPulsesRequired()):g.microphoneMode?g.micLevel:g.voiceActive?.5f:0;
            p.setColor(0xFFE1D6E9);c.drawRoundRect(140,y+64,320,y+72,4,4,p);
            if(g.mode==11){p.setColor(0xFFF9D78E);c.drawRect(140+180*g.glowLow(),y+64,140+180*g.glowHigh(),y+72,p);}
            p.setColor(0xFFBD92D6);c.drawRoundRect(140,y+66,140+180*level,y+70,3,3,p);
        }

    }
    private void label(Canvas c,String text,float x,float y,float size,int color){p.setColor(color);p.setTextSize(size);p.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));p.setTextAlign(Paint.Align.CENTER);c.drawText(text,x,y,p);p.setTextAlign(Paint.Align.LEFT);}
}
