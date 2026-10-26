package com.facechaos.mobile;

import android.content.Context;
import android.graphics.*;

/** Shared sky sprites and matching Voice Runner tutorial. */
final class VoiceArt {
    private static Bitmap shared;
    private final Bitmap atlas;
    private final Paint p=new Paint(3);
    private final VoiceCourseArt course=new VoiceCourseArt();
    private final RectF dest=new RectF();
    private final Rect[] src={new Rect(38,22,570,485),new Rect(607,22,1048,492),new Rect(1155,140,1505,480),new Rect(25,500,585,993),new Rect(605,680,1130,952),new Rect(1180,585,1520,960)};
    VoiceArt(Context context){if(shared==null){BitmapFactory.Options o=new BitmapFactory.Options();o.inScaled=false;shared=BitmapFactory.decodeResource(context.getResources(),R.drawable.voice_atlas,o);}atlas=shared;if(atlas==null)throw new IllegalStateException("Voice Runner artwork missing");}
    void sprite(Canvas c,int index,float x,float bottom,float width){Rect r=src[index];dest.set(x-width/2,bottom-width*r.height()/r.width(),x+width/2,bottom);p.setShader(null);p.setColor(Color.WHITE);p.setStyle(Paint.Style.FILL);c.drawBitmap(atlas,r,dest,p);}
    void tutorial(Canvas c,float w,float h,int action){tutorial(c,w,h,action,1);}
    void tutorial(Canvas c,float w,float h,int action,int stage){
        if(action<0){c.save();c.scale(w/390f,h/200f);MiniGame demo=new MiniGame(3,0,stage);demo.phase=1.2f;course.backdrop(c,demo,44,170);course.obstacle(c,demo,290,50,95,170);if(stage==1)sprite(c,3,290,155,140);sprite(c,2,290,50+demo.corridor()*95+16,32);sprite(c,0,85,145,102);p.setColor(0xFF214F67);c.drawRoundRect(25,5,365,35,12,12,p);label(c,VoiceCourse.world(stage),195,26,17,Color.WHITE);c.restore();return;}
        c.drawColor(0xFFE9F7FB);p.setColor(0xFFE6D8FF);c.drawCircle(w*.82f,h*.2f,h*.65f,p);
        if(action<0){sprite(c,3,w*.74f,h*.93f,Math.min(w*.4f,h));sprite(c,2,w*.74f,h*.68f,h*.22f);sprite(c,0,w*.27f,h*.88f,Math.min(w*.4f,h*.9f));}
        else if(action==0){sprite(c,0,w*.4f,h*.93f,Math.min(w*.47f,h*.86f));sprite(c,5,w*.76f,h*.68f,h*.35f);}
        else if(action==1){sprite(c,1,w*.4f,h*.93f,Math.min(w*.4f,h*.8f));sprite(c,4,w*.73f,h*.9f,w*.33f);}
        else{sprite(c,3,w*.5f,h*.97f,Math.min(w*.55f,h*1.1f));sprite(c,2,w*.5f,h*.65f,h*.3f);}
    }
    void scene(Canvas c,MiniGame game,float height,float safeTop,float controls){
        float bottom=height-Math.max(108,controls)+8;
        float flightTop=Math.max(height*.61f,bottom-195),travel=Math.max(95,bottom-flightTop-30);
        course.backdrop(c,game,flightTop,bottom);
        float heroY=flightTop+game.height*travel;
        float ringY=flightTop+game.corridor()*travel;
        float progress=Math.min(1,game.phase/game.voiceApproach()),ringX=346-progress*258;
        if(!game.resolved||!game.earnedCoin){
            if(game.stage==1)sprite(c,3,ringX,ringY+150*493/560f*.43f,150);
            else course.obstacle(c,game,ringX,flightTop,travel,bottom);
            sprite(c,2,ringX,ringY+16,32);
        }
        // A dotted approach line makes the target height legible without covering the face.
        p.setColor(0x77D8F8FF);for(int i=0;i<7;i++)c.drawCircle(128+i*23,ringY,2,p);
        sprite(c,game.voiceActive?0:1,88,heroY+33,76);
        if(game.resolved&&game.earnedCoin)for(int i=0;i<7;i++){float angle=i*.9f,r=25+(1-game.flash/.9f)*50;p.setColor(0xFFFFDE93);c.drawCircle(88+(float)Math.cos(angle)*r,heroY+(float)Math.sin(angle)*r,3,p);}
        if(!game.finished){
            float y=safeTop+82;p.setColor(game.resolved&&game.earnedCoin?0xF5FFF0B3:0xECEDF9FF);c.drawRoundRect(20,y,370,y+88,20,20,p);
            sprite(c,game.voiceActive?0:1,72,y+75,65);
            String cue=game.paused?"Paused":game.microphoneMode&&!game.micReady?"Calibrating - stay quiet":game.countdown>0?"Ready in "+(int)Math.ceil(game.countdown):game.resolved?(game.earnedCoin?"Good job! +1 coin":"Hit! Try the next gap"):!game.microphoneMode?(game.height<game.corridor()?"Release to drift down":"Hold to rise"):Math.abs(game.height-game.corridor())<.1f?"Easy, short hums":game.height<game.corridor()?"Stay quiet to drift down":"Hum gently to rise";
            label(c,cue,225,y+32,16,0xFF214F67);
            String hint=game.paused?"Resume when you are ready":game.resolved?"Obstacle "+Math.min(8,game.encounter+1)+" of 8":game.microphoneMode?"LIVE VOICE: hum up / quiet down":"TOUCH: hold up / release down";
            label(c,hint,225,y+53,10,0xFF4F7585);
            p.setColor(0xFFCCDEE8);c.drawRoundRect(140,y+64,320,y+72,4,4,p);p.setColor(game.voiceActive?0xFF58BEA5:0xFF9CB6D6);c.drawRoundRect(140,y+64,140+180*(game.microphoneMode?game.micLevel:game.voiceActive?1:0),y+72,4,4,p);
        }
        label(c,VoiceCourse.world(game.stage)+"  |  "+Math.min(8,game.encounter+1)+" / 8",195,bottom+36,12,Color.WHITE);
        if(!game.finished)label(c,VoiceCourse.hint(game.stage,game.encounter),195,flightTop-22,12,Color.WHITE);
    }
    private void label(Canvas c,String text,float x,float y,float size,int color){p.setColor(color);p.setTextSize(size);p.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));p.setTextAlign(Paint.Align.CENTER);c.drawText(text,x,y,p);p.setTextAlign(Paint.Align.LEFT);}
}
