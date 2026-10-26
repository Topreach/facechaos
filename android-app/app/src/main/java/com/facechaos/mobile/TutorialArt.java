package com.facechaos.mobile;

import android.content.Context;
import android.graphics.*;
import android.view.View;

/** Illustrated tutorial panel using the same characters as the games. */
final class TutorialArt extends View {
    private final Paint paint=new Paint(3);
    private final Bitmap atlas;
    private final GateExtraArt gateExtras;
    private final int mode;
    private final int action;
    private final int stage;
    private final EscapeArt escape;
    private final VoiceArt voice;
    private final DuetArt duet;
    private final CollectionArt collection;
    private final ExtrasArt extras;
    private final RacingArt racing;
    TutorialArt(Context context,int mode){this(context,mode,-1);}
    TutorialArt(Context context,int mode,int action){this(context,mode,action,1);}
    TutorialArt(Context context,int mode,int action,int stage){
        super(context);this.mode=mode;this.action=action;this.stage=stage;gateExtras=mode==0?new GateExtraArt(context):null;racing=mode==12||mode==13?new RacingArt():null;extras=mode>=9&&mode<12?new ExtrasArt(context):null;collection=mode>=6&&mode<9?new CollectionArt(context):null;duet=mode==5?new DuetArt(context):null;voice=mode==3?new VoiceArt(context):null;escape=mode==1?new EscapeArt(context):null;
        atlas=mode==1||mode==3||mode==5||mode>=6?null:BitmapFactory.decodeResource(getResources(),mode==2||mode==4?R.drawable.garden_music_atlas:R.drawable.emoji_atlas);
        setContentDescription(mode>=6?GameCatalog.ALL[mode].name+": "+GameCatalog.ALL[mode].controlBrief():mode==5?"Star Duet: smile for a spark, then hum to light a star":mode==3?"Voice Runner, "+VoiceCourse.world(stage)+": hum to rise, stay quiet to descend, and clear each obstacle gap":mode==1?"Expression Escape: jump over spikes, shield against shooting stars, chomp a biscuit":mode==2?"A smiling gardener grows a flower":mode==4?"A scrolling sound-powered obstacle course: short sounds jump gaps, platforms and fire":"A happy character matches a colourful gate");
    }
    private void journey(Canvas c,float w,float h){
        MiniGame demo=new MiniGame(mode,0,stage);demo.countdown=0;demo.finished=true;demo.phase=1.7f;demo.encounter=mode==1&&action>=0?action:0;demo.micReady=true;demo.microphoneMode=true;
        if(mode==0&&action>=0)for(int i=0;i<5;i++)if(GameSections.faceAt(stage,i)==action){demo.score=i;break;}
        SectionArt art=new SectionArt();float scale=Math.min(w/390f,h/220f);c.drawColor(SectionArt.sky(demo));c.save();c.translate((w-390*scale)/2,(h-220*scale)/2);c.scale(scale,scale);art.background(c,demo,0,220);
        c.save();c.translate(0,-403);
        if(escape!=null)escape.scene(c,demo,598,-500);
        else if(racing!=null)racing.scene(c,demo,700,-500,110);
        else if(collection!=null){if(mode==7)demo.score=3;collection.scene(c,demo,700,-500,110);}
        else if(duet!=null){demo.score=3;duet.scene(c,demo,700,-500,110);}
        else if(extras!=null)extras.scene(c,demo,700,-500,110);
        c.restore();
        if(mode==0||mode==2){Rect hero=mode==2?new Rect(58,475,493,956):mode==4?new Rect(560,480,977,958):new Rect(48,30,465,440);Rect prop=mode==2?new Rect(1073,0,1453,462):mode==4?new Rect(1135,505,1445,780):new Rect(526,460,1013,948);if(mode==0){Rect[] heroes={new Rect(48,30,465,440),new Rect(560,30,976,440),new Rect(1073,30,1490,440)};Rect[] gates={new Rect(12,460,498,948),new Rect(526,460,1013,948),new Rect(1041,460,1527,946)};hero=heroes[Math.min(2,demo.target())];prop=gates[Math.min(2,demo.target())];}paint.setColor(Color.WHITE);if(mode==0&&demo.target()>=3){gateExtras.gate(c,demo.target(),new RectF(208,42,350,202),paint);gateExtras.actor(c,demo.target(),new RectF(54,52,188,202),paint);}else{c.drawBitmap(atlas,prop,new RectF(208,42,350,202),paint);c.drawBitmap(atlas,hero,new RectF(54,52,188,202),paint);}}
        if(mode==4){
            paint.setColor(0xFF593E67);c.drawRect(20,173,370,211,paint);
            paint.setColor(0xFFFF9A3F);for(int i=0;i<13;i++){float x=30+i*27;Path flame=new Path();flame.moveTo(x-8,205);flame.lineTo(x,178);flame.lineTo(x+9,205);flame.close();c.drawPath(flame,paint);}
            paint.setColor(0xFF8E7BB8);c.drawRoundRect(30,151,130,211,6,6,paint);c.drawRoundRect(231,112,343,211,6,6,paint);
            paint.setColor(0xFFFFDF91);c.drawRect(30,151,130,156,paint);c.drawRect(231,112,343,117,paint);paint.setColor(Color.WHITE);c.drawBitmap(atlas,new Rect(560,480,977,958),new RectF(48,49,136,151),paint);c.drawBitmap(atlas,new Rect(1135,505,1445,780),new RectF(246,68,282,100),paint);
        }
        art.title(c,demo,5);c.restore();
    }
    @Override protected void onDraw(Canvas c){
        float w=getWidth(),h=getHeight();
        if(GameSections.applies(mode)&&(action<0||mode==1||mode==0)){journey(c,w,h);return;}
        if(mode==14){new FlagArt().tutorial(c,w,h);return;}
        if(racing!=null){racing.tutorial(c,w,h,mode);return;}
        if(extras!=null){extras.tutorial(c,w,h,mode,action);return;}
        if(collection!=null){collection.tutorial(c,w,h,mode,action);return;}
        if(duet!=null){duet.tutorial(c,w,h,action);return;}
        if(voice!=null){voice.tutorial(c,w,h,action,stage);return;}
        if(escape!=null){escape.tutorial(c,w,h,action);return;}
        paint.setColor(0xFFE3F5EA);c.drawColor(0xFFE3F5EA);
        paint.setColor(0xFFFFF3B6);c.drawCircle(w*.8f,h*.2f,h*.55f,paint);
        paint.setColor(0xFFC0E5DC);c.drawOval(w*.08f,h*.79f,w*.94f,h*.96f,paint);
        Rect hero=mode==2?new Rect(58,475,493,956):mode==4?new Rect(560,480,977,958):new Rect(48,30,465,440);
        Rect prop=mode==2?new Rect(1073,0,1453,462):mode==4?new Rect(1135,505,1445,780):new Rect(526,460,1013,948);
        paint.setFilterBitmap(true);
        c.drawBitmap(atlas,prop,new RectF(w*.51f,h*.08f,w*.88f,h*.86f),paint);
        c.drawBitmap(atlas,hero,new RectF(w*.13f,h*.12f,w*.49f,h*.91f),paint);
        paint.setColor(0xFFFFFFFF);
        for(int i=0;i<5;i++){float x=w*(.1f+i*.19f),y=h*(i%2==0?.16f:.7f);c.drawCircle(x,y,4,paint);}
    }
}
