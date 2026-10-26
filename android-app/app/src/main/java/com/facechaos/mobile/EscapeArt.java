package com.facechaos.mobile;

import android.content.Context;
import android.graphics.*;

/** Shared Expression Escape artwork: the tutorial and game use identical sprites. */
final class EscapeArt {
    private final Context context;
    private final Bitmap atlas;
    private static Bitmap sharedAtlas;
    private final Paint paint=new Paint(3);
    private final SectionArt sections=new SectionArt();
    private final Rect[] sources={new Rect(18,65,505,565),new Rect(527,75,1010,568),new Rect(1055,72,1508,565),new Rect(30,650,513,936),new Rect(548,585,1008,980),new Rect(1080,585,1505,970)};
    private final RectF target=new RectF();
    EscapeArt(Context context){this.context=context;
        BitmapFactory.Options options=new BitmapFactory.Options();options.inScaled=false;
        if(sharedAtlas==null)sharedAtlas=BitmapFactory.decodeResource(context.getResources(),R.drawable.escape_atlas,options);
        atlas=sharedAtlas;
        if(atlas==null)throw new IllegalStateException("Expression Escape artwork missing");
    }
    void sprite(Canvas c,int index,float x,float bottom,float width){
        Rect source=sources[index];
        float height=width*source.height()/source.width();target.set(x-width/2,bottom-height,x+width/2,bottom);
        paint.setStyle(Paint.Style.FILL);paint.setShader(null);paint.setColor(Color.WHITE);c.drawBitmap(atlas,source,target,paint);
    }
    void tutorial(Canvas c,float w,float h,int action){
        c.drawColor(0xFFFFF0DC);paint.setColor(0xFFFFDFAD);c.drawCircle(w*.84f,h*.2f,h*.65f,paint);
        if(action<0){
            for(int i=0;i<3;i++){
                float center=w*(i+.5f)/3;
                sprite(c,i,center-w*.035f,h*.78f,w*.23f);
                sprite(c,i+3,center+w*.07f,h*.77f,w*.13f);
                label(c,new String[]{"SOUND: JUMP","CLOSED-LIP SMILE","SHOW TEETH"}[i],center,h*.94f,Math.min(w/40,h*.1f),0xFF713E2B);
            }
        }else{
            sprite(c,action,w*.33f,h*.93f,Math.min(w*.42f,h*.82f));
            sprite(c,action+3,w*.73f,h*.87f,Math.min(w*.33f,h*.7f));
            paint.setColor(0xFFBD7C52);paint.setStrokeWidth(3);c.drawLine(w*.5f,h*.52f,w*.57f,h*.52f,paint);
        }
    }
    void scene(Canvas c,MiniGame game,float ground,float safeTop){
        // A short floating confectionery track leaves the central selfie area clear.
        paint.setShader(new LinearGradient(0,ground+3,0,ground+34,SectionArt.light(game),SectionArt.floor(game),Shader.TileMode.CLAMP));
        c.drawRoundRect(12,ground+3,378,ground+34,18,18,paint);paint.setShader(null);
        paint.setColor(0xFFFFF3CC);c.drawRoundRect(17,ground+1,373,ground+10,10,10,paint);
        for(int i=0;i<11;i++){paint.setColor(i%2==0?0xFFE9AA73:0xFFF7D09A);c.drawCircle(28+i*33,ground+22,3,paint);}
        boolean action=game.resolved&&game.flash>0;
        float bounce=game.jumpHeight;
        float obstacle=344-Math.min(1,game.phase/2.65f)*248;
        if(!game.resolved||game.flash<=0){
            if(!game.resolved){if(game.target()==0)sections.hurdle(c,game,obstacle,ground+7,game.highJump()?92:69);else if(game.target()==1)sections.projectile(c,game,obstacle,ground-50);else sprite(c,5,obstacle,ground+7,75);}
        }
        sprite(c,action?game.target():0,87,ground+7-bounce,game.target()==1&&action?130:116);
        if(action){
            paint.setColor(0xFFFFE79A);
            for(int i=0;i<7;i++){float a=i*.9f;float r=28+(1-game.flash/.7f)*45;c.drawCircle(87+(float)Math.cos(a)*r,ground-48+(float)Math.sin(a)*r,3,paint);}
        }
        if(game.soundJump()){
            paint.setColor(0x99405B60);c.drawRoundRect(145,ground+26,345,ground+34,4,4,paint);
            paint.setColor(0xFFFFD36B);c.drawRoundRect(145,ground+26,145+200*(game.microphoneMode?game.soundStrength:game.touchHolding?MiniGame.touchStrength(game.touchHoldTime):0),ground+34,4,4,paint);
            label(c,game.microphoneMode?"VOLUME":"PRESS",101,ground+34,9,Color.WHITE);
        }
        float cueY=safeTop+82;
        boolean window=game.phase>=game.windowOpens()&&game.phase<=game.windowCloses()&&!game.resolved&&game.countdown<=0&&!game.paused;
        boolean reward=game.earnedCoin&&game.resolved;
        if(!game.finished){
            // Fixed compact banner above the central selfie area; respects the safe top inset.
            paint.setColor(reward?0xF5FFF0B3:window?0xF5FFF7E3:0xDA334F4C);
            c.drawRoundRect(20,cueY,370,cueY+86,20,20,paint);
            paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(window?3:1.5f);paint.setColor(reward?0xFFFFC344:window?0xFFFFDE82:0xFFA6C8B7);c.drawRoundRect(20,cueY,370,cueY+86,20,20,paint);paint.setStyle(Paint.Style.FILL);
            if(reward){
                coin(c,81,cueY+43,23);
                label(c,"Good job!",225,cueY+36,23,0xFF713E2B);
                label(c,"+1 coin",225,cueY+62,16,0xFF916218);
            }else{
                sprite(c,game.target(),81,cueY+76,64);
                String cue=game.paused?"Paused":game.countdown>0?"Ready in "+(int)Math.ceil(game.countdown):game.resolved?"Next action coming up":game.microphoneMode&&!game.micReady?"Stay quiet for a second":game.prompt();
                label(c,cue,225,cueY+34,17,window?0xFF713E2B:Color.WHITE);
                String hint=game.paused?"Resume when you are ready":game.countdown>0?"Follow the face popup":game.resolved?"Take a breath and relax":game.soundJump()?game.jumpHint():window?"Your turn - match this face":"Get ready for this face";
                label(c,hint,225,cueY+60,11,window?0xFF916218:0xFFFFEDCF);
            }
        }
    }
    void wallet(Canvas c,MiniGame game,float safeTop){
        float y=safeTop+10;
        paint.setColor(0xD5334F4C);c.drawRoundRect(257,y,375,y+37,18,18,paint);
        coin(c,276,y+18,11);label(c,String.valueOf(game.runCoins()),326,y+17,16,0xFFFFE39A);label(c,context.getString(R.string.fc_best_badge,game.bestRunCoins),326,y+31,9,0xFFDBEFDE);
    }
    private void coin(Canvas c,float x,float y,float radius){
        paint.setShader(new LinearGradient(x-radius,y-radius,x+radius,y+radius,0xFFFFEF9D,0xFFF2AB22,Shader.TileMode.CLAMP));c.drawCircle(x,y,radius,paint);paint.setShader(null);
        paint.setStyle(Paint.Style.STROKE);paint.setColor(0xFFC78616);paint.setStrokeWidth(2);c.drawCircle(x,y,radius*.74f,paint);paint.setStyle(Paint.Style.FILL);
        label(c,"+",x,y+radius*.35f,radius*1.15f,0xFF9F6816);
    }

    private void label(Canvas c,String value,float x,float y,float size,int color){
        value=AppText.t(context,value);paint.setColor(color);paint.setTextSize(size);paint.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));paint.setTextAlign(Paint.Align.CENTER);paint.setShadowLayer(2,0,1,0x88000000);paint.setTextAlign(Paint.Align.LEFT);LocalizedCanvas.draw(context,c,paint,value,x,y,true,340);paint.clearShadowLayer();paint.setTextAlign(Paint.Align.LEFT);
    }
}
