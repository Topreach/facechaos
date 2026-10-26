package com.facechaos.mobile;

import android.content.Context;
import android.graphics.*;

/** Three distinct collection games, using the same sprites in play and instructions. */
final class CollectionArt {
    private static Bitmap shared;
    private final Bitmap atlas;
    private final Paint p=new Paint(3);
    private final SectionArt sections=new SectionArt();
    private final RectF dst=new RectF();
    private final Rect[] src={new Rect(65,80,470,470),new Rect(594,28,955,470),new Rect(1090,25,1480,470),new Rect(130,510,405,985),new Rect(580,505,945,1000),new Rect(1080,535,1475,965),new Rect(155,535,380,678),new Rect(155,699,393,811),new Rect(148,828,398,980)};
    CollectionArt(Context context){if(shared==null){BitmapFactory.Options options=new BitmapFactory.Options();options.inScaled=false;shared=BitmapFactory.decodeResource(context.getResources(),R.drawable.collection_atlas,options);}atlas=shared;if(atlas==null)throw new IllegalStateException("Collection artwork missing");}
    void sprite(Canvas c,int index,float x,float bottom,float width){Rect r=src[index];dst.set(x-width/2,bottom-width*r.height()/r.width(),x+width/2,bottom);p.setColor(Color.WHITE);p.setStyle(Paint.Style.FILL);c.drawBitmap(atlas,r,dst,p);}
    void tutorial(Canvas c,float w,float h,int mode,int action){
        c.drawColor(mode==6?0xFFFFF1E7:mode==7?0xFFE7F7F1:0xFFF1EAFF);
        p.setColor(0xFFFFE6AF);c.drawCircle(w*.83f,h*.2f,h*.61f,p);
        if(mode==6){sprite(c,0,w*.3f,h*.94f,Math.min(w*.39f,h*.82f));sprite(c,action==1?2:1,w*.73f,h*.81f,h*.57f);}
        else if(mode==7){sprite(c,3,w*.48f,h*.95f,Math.min(w*.24f,h*.49f));sprite(c,6,w*.76f,h*.45f,h*.4f);}
        else{sprite(c,4,w*.28f,h*.98f,h*.65f);sprite(c,5,w*.75f,h*.91f,h*.74f);}
    }
    void scene(Canvas c,MiniGame g,float height,float safeTop,float controls){
        float ground=height-Math.max(110,controls)+1;
        if(g.mode==6)snacks(c,g,ground);
        else if(g.mode==7)stack(c,g,ground);
        else comet(c,g,ground);
        if(!g.finished)popup(c,g,safeTop);
    }
    private void snacks(Canvas c,MiniGame g,float ground){
        p.setColor(0x99FFDBB5);c.drawRoundRect(35,ground-7,355,ground+13,14,14,p);
        sprite(c,0,195,ground+2,113);
        float t=Math.min(1,g.phase/2.55f);
        if(!g.resolved)sprite(c,g.snackIsTreat()?1:2,340-t*145,ground-170+t*85,57);
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);p.setColor(g.snackIsTreat()?0xFFBDE6B4:0xFFFFA8A0);c.drawOval(156,ground-123,234,ground-90,p);p.setStyle(Paint.Style.FILL);
        label(c,"Treats "+g.score+" / "+g.goal()+"  -  item "+Math.min(9,g.encounter+1)+" / 9",195,ground+36,12,Color.WHITE);
    }
    private void block(Canvas c,float x,float bottom,int layer,boolean gold){
        int[] colors={0xFF62CEBC,0xFF92ACEF,0xFFFFB77C};
        p.setColor(0x400E3741);c.drawRoundRect(x-56,bottom-15,x+58,bottom+3,5,5,p);
        p.setColor(gold?0xFFFFD775:colors[layer%3]);c.drawRoundRect(x-56,bottom-18,x+56,bottom,5,5,p);
        p.setColor(0x70FFFFFF);c.drawRoundRect(x-52,bottom-16,x+52,bottom-12,3,3,p);
        p.setColor(0x400E3741);c.drawRect(x-51,bottom-3,x+51,bottom,p);
    }
    private void stack(Canvas c,MiniGame g,float ground){
        float base=ground-18,top=base-g.score*18;
        p.setColor(0x6645756B);c.drawOval(80,base+2,310,base+19,p);
        p.setColor(0xFF406C69);c.drawRoundRect(112,base,278,base+12,6,6,p);
        float tolerance=g.stackTolerance()*130;
        p.setColor(g.stackAligned()&&!g.resolved?0x77FFE28A:0x28FFE28A);c.drawRoundRect(195-tolerance,base-218,195+tolerance,base,6,6,p);
        p.setColor(0xFFFFD775);p.setStrokeWidth(2);c.drawLine(195,base-221,195,base+4,p);
        for(int i=0;i<g.score;i++)block(c,195+g.stackOffsets[i]*130,base-i*18,i,false);
        if(!g.resolved){
            float x=195+g.stackOffset()*130;
            p.setColor(0x66FFFFFF);p.setStrokeWidth(1);c.drawLine(24,top-61,366,top-61,p);
            block(c,x,top-44,g.score,g.stackAligned());
        }else if(g.stackDropping&&(!g.stackLanded||!g.stackGood)){
            float t=g.stackDropProgress(),x=195+g.stackDropOffset*130;
            float bottom=top-44+44*t*t;
            if(g.stackLanded&&!g.stackGood){float fall=Math.max(0,g.flightTime-.38f);x+=Math.signum(g.stackDropOffset)*fall*120;bottom+=fall*fall*260;}
            c.save();c.clipRect(0,base-240,390,ground+8);block(c,x,bottom,g.score,g.stackGood);c.restore();
        }
        if(g.stackLanded&&g.earnedCoin&&g.flash>0){label(c,"+1",195,top-18-(1-g.flash)*14,18,0xFFFFE298);}
        label(c,"Blocks "+g.score+" / "+g.goal()+"  |  Drop "+Math.min(8,g.encounter+1)+" / 8",195,ground+4,11,Color.WHITE);
    }
    private void comet(Canvas c,MiniGame g,float ground){
        sections.planet(c,g,303,ground-139,43);
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);p.setColor(0xFFFFD579);c.drawOval(271,ground-161,335,ground-117,p);p.setStyle(Paint.Style.FILL);
        float x=g.resolved?g.cometFlightX():85,bottom=g.resolved?g.cometFlightBottom(ground):ground-52;
        if(g.resolved&&!g.cometLanded){p.setColor(0x88FFD98E);for(int i=1;i<=4;i++)c.drawCircle(x-i*9,bottom-20+i*3,Math.max(2,7-i),p);}
        sprite(c,4,x,bottom,64);
        if(g.cometLanded&&g.earnedCoin){p.setColor(0x99FFF0AC);c.drawCircle(303,ground-139,18+g.flash*22,p);}
        float meterY=ground-37;
        p.setColor(0xE12D3D54);c.drawRoundRect(44,meterY,346,meterY+20,8,8,p);
        p.setColor(0xFF8EDBCD);c.drawRoundRect(52,meterY+4,52+286*g.charge,meterY+16,4,4,p);
        float lo=52+286*(g.powerTarget()-g.powerTolerance()),hi=52+286*(g.powerTarget()+g.powerTolerance());
        p.setColor(0x55FFD16E);c.drawRect(lo,meterY+2,hi,meterY+18,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);p.setColor(0xFFFFD16E);c.drawRoundRect(lo,meterY+2,hi,meterY+18,4,4,p);p.setStyle(Paint.Style.FILL);
        p.setColor(Color.WHITE);p.setStrokeWidth(2);c.drawLine(52+286*g.charge,meterY-2,52+286*g.charge,meterY+22,p);
        label(c,"SOUND ONLY - Power "+Math.round(g.charge*100)+"% - shot "+Math.min(6,g.encounter+1)+" / 6",195,ground+1,11,Color.WHITE);
    }
    private void popup(Canvas c,MiniGame g,float safeTop){
        float y=safeTop+82;boolean reward=g.resolved&&g.earnedCoin;
        boolean active=g.mode==6?g.phase>=g.snackWindowOpens()&&g.phase<=g.snackWindowCloses():g.mode==7?g.stackAligned():g.charge>=g.powerTarget()-g.powerTolerance()&&g.charge<=g.powerTarget()+g.powerTolerance();
        p.setColor(reward?0xF5FFF0B3:0xF5F6F3E9);c.drawRoundRect(20,y,370,y+88,20,20,p);
        int icon=g.mode==6?(g.snackIsTreat()?1:2):g.mode==7?3:4;sprite(c,icon,76,y+76,icon==3?39:49);
        String cue=g.paused?"Paused":g.countdown>0?"Ready in "+(int)Math.ceil(g.countdown):reward?"Good job! +1 coin":g.resolved?g.feedback:g.mode==6?(g.snackIsTreat()?(active?"Open your mouth now":"Wait for the treat"):"Keep your mouth closed"):g.mode==7?(active?"Smile now!":"Wait for the gold line"):g.microphoneMode&&!g.micReady?"Stay quiet for a second":active?"Go quiet to launch now":"Hum gently to charge";
        label(c,cue,225,y+33,15,0xFF55415A);
        String hint=g.paused?"Resume when you are ready":reward?"Coin added to your balance":g.mode==6?(g.snackIsTreat()?g.faceMode?"Open for the treat, then relax":"Tap for the treat":"Pepper: do nothing"):g.mode==7?(g.resolved?"Relax your face for the next block":active?g.faceMode?"Closed lips - gentle smile":"Aligned - tap now":"Relax until the block is centred"):"Hum to charge - quiet launches - no touching";
        label(c,hint,225,y+59,10,0xFF856D8A);
    }
    private void label(Canvas c,String text,float x,float y,float size,int color){p.setColor(color);p.setTextSize(size);p.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));p.setTextAlign(Paint.Align.CENTER);c.drawText(text,x,y,p);p.setTextAlign(Paint.Align.LEFT);}
}
