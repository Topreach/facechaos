package com.facechaos.mobile;

import android.content.Context;
import android.graphics.*;

/** A sequential smile/sound constellation, with shared game/tutorial illustrations. */
final class DuetArt {
    private final Bitmap faces;
    private final VoiceArt voice;
    private final Paint p=new Paint(3);
    private static final float[][] patterns={{83,141,195,249,307,195},{90,155,230,307,240,160},{83,130,195,260,307,195},{105,155,195,235,285,195},{95,120,195,270,295,195}};
    private static final float[][] levels={{20,-4,22,-4,20,60},{45,0,30,0,45,65},{50,5,-10,5,50,70},{25,0,60,0,25,-15},{55,-5,28,-5,55,70}};
    private final Rect smile=new Rect(560,30,976,440);
    private final RectF dst=new RectF();
    DuetArt(Context context){faces=BitmapFactory.decodeResource(context.getResources(),R.drawable.emoji_atlas);voice=new VoiceArt(context);}
    private void hero(Canvas c,float x,float bottom,float width){dst.set(x-width/2,bottom-width,x+width/2,bottom);p.setColor(Color.WHITE);c.drawBitmap(faces,smile,dst,p);}
    void tutorial(Canvas c,float w,float h,int action){
        c.drawColor(0xFFF6EBFF);p.setColor(0xFFFFEAC5);c.drawCircle(w*.83f,h*.23f,h*.6f,p);
        if(action==0){hero(c,w*.38f,h*.91f,Math.min(w*.4f,h*.8f));voice.sprite(c,2,w*.76f,h*.69f,h*.25f);}
        else if(action==1){voice.sprite(c,5,w*.32f,h*.82f,h*.58f);voice.sprite(c,2,w*.73f,h*.89f,h*.65f);}
        else{
            hero(c,w*.23f,h*.9f,h*.78f);
            p.setColor(0xFFD1AEDF);p.setStrokeWidth(3);c.drawLine(w*.41f,h*.51f,w*.64f,h*.51f,p);
            voice.sprite(c,5,w*.51f,h*.62f,h*.28f);voice.sprite(c,2,w*.78f,h*.88f,h*.74f);
        }
    }
    void scene(Canvas c,MiniGame g,float height,float safeTop,float controls){
        float bottom=height-Math.max(110,controls)+7;
        float stageTop=Math.max(height*.64f,bottom-200);
        float actorBottom=bottom-30;
        voice.sprite(c,4,83,bottom+5,145);voice.sprite(c,4,307,bottom+5,145);
        float pulse=g.flash>0?(float)Math.sin(g.flash/.7f*Math.PI)*8:0;
        hero(c,83,actorBottom-pulse,100);
        voice.sprite(c,2,307,actorBottom-pulse,100);
        p.setColor(g.target()==1?0xFFFFDF92:0x889FBAD8);p.setStrokeWidth(3);
        for(int i=0;i<7;i++)c.drawCircle(150+i*14,actorBottom-50+(float)Math.sin(i*.5f+g.elapsed)*7,2.5f,p);
        if(g.target()==1)voice.sprite(c,5,205,actorBottom-39,34);
        float[] xs=patterns[g.stage-1],ys=new float[6];for(int i=0;i<6;i++)ys[i]=stageTop+levels[g.stage-1][i];
        p.setStrokeWidth(1.6f);for(int i=1;i<6;i++){p.setColor(i<g.score?0xFFFFDB86:0x669DBAC9);c.drawLine(xs[i-1],ys[i-1],xs[i],ys[i],p);}
        for(int i=0;i<6;i++){p.setColor(i<g.score?0xFFFFDC8A:0x558DB0BD);c.drawCircle(xs[i],ys[i],i<g.score?6:4,p);}
        if(!g.finished){
            float y=safeTop+82;
            boolean reward=g.earnedCoin&&g.flash>0;
            p.setColor(reward?0xF5FFF0B3:0xF0F6ECFF);c.drawRoundRect(20,y,370,y+88,20,20,p);
            if(g.target()==0)hero(c,75,y+76,62);else voice.sprite(c,5,75,y+74,55);
            String cue=g.paused?"Paused":g.countdown>0?"Ready in "+(int)Math.ceil(g.countdown):reward?"Good job! +1 coin":g.target()==0?"Smile with lips closed":!g.microphoneMode?"Tap to light a star":g.microphoneMode&&!g.micReady?"Stay quiet for a second":g.microphoneMode&&!g.duetSoundArmed?"Relax, then hum gently":"Hum gently to light a star";
            label(c,cue,226,y+32,15,0xFF5E3979);
            String hint=g.paused?"Resume when you are ready":g.target()==0?g.faceMode?"Smile, then relax your face":"Tap the hero for the smile":g.microphoneMode?"Sound level only - one gentle hum":"Tap the star - sound preview";
            label(c,hint,225,y+53,10,0xFF826995);
            if(g.target()==1){p.setColor(0xFFDDCDE8);c.drawRoundRect(139,y+64,321,y+72,4,4,p);p.setColor(0xFF9D79D6);c.drawRoundRect(139,y+64,139+182*g.micLevel,y+72,4,4,p);}
        }
        label(c,"Duet "+Math.min(3,g.score/2+1)+" / 3  -  smile, then sound",195,bottom+29,12,Color.WHITE);
    }
    private void label(Canvas c,String text,float x,float y,float size,int color){p.setShader(null);p.setColor(color);p.setTextSize(size);p.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));p.setTextAlign(Paint.Align.CENTER);c.drawText(text,x,y,p);p.setTextAlign(Paint.Align.LEFT);}
}
