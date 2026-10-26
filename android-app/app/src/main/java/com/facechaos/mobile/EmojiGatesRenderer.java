package com.facechaos.mobile;

import android.content.Context;
import android.graphics.*;

/** Sprite-based 2.5D scene: only game objects cover the live selfie texture. */
final class EmojiGatesRenderer {
    private final Context context;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Bitmap atlas;
    private final GateExtraArt extraGates;
    private final Bitmap gardenMusic;
    private final EscapeArt escapeArt;
    private final VoiceArt voiceArt;
    private final DuetArt duetArt;
    private final CollectionArt collectionArt;
    private final ExtrasArt extrasArt;
    private final RacingArt racingArt=new RacingArt();
    private final SectionArt sectionArt=new SectionArt();
    private final Rect[] plants = {new Rect(90,0,470,462),new Rect(578,0,958,462),new Rect(1073,0,1453,462)};
    private final Rect gardener = new Rect(58,475,493,956);
    private final Rect musician = new Rect(560,480,977,958);
    private final Rect stone = new Rect(1080,785,1480,954);
    private final Rect note = new Rect(1135,505,1445,780);
    private final Rect[] actors = {
        new Rect(48, 30, 465, 440), new Rect(560, 30, 976, 440), new Rect(1073, 30, 1490, 440)
    };
    private final Rect[] arches = {
        new Rect(12, 460, 498, 948), new Rect(526, 460, 1013, 948), new Rect(1041, 460, 1527, 946)
    };
    private final Path path = new Path();
    private final RectF destination = new RectF();
    private float horizon, ground;

    EmojiGatesRenderer(Context context) {
        this.context=context;
        extraGates=new GateExtraArt(context);
        escapeArt=new EscapeArt(context);voiceArt=new VoiceArt(context);duetArt=new DuetArt(context);collectionArt=new CollectionArt(context);extrasArt=new ExtrasArt(context);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        atlas = BitmapFactory.decodeResource(context.getResources(), R.drawable.emoji_atlas, options);
        gardenMusic = BitmapFactory.decodeResource(context.getResources(), R.drawable.garden_music_atlas, options);
        if (atlas == null || gardenMusic == null) throw new IllegalStateException("Game artwork could not load");
    }

    void draw(Canvas canvas, MiniGame game, float height, float safeTop, float controlsHeight) {
        ground = height - Math.max(94, controlsHeight) + 43;
        horizon = Math.max(height * .56f, ground - 254);
        sectionArt.scene(canvas,game,height,controlsHeight);
        if(game.mode==14){new FlagArt().scene(canvas,game,height,safeTop,controlsHeight);drawHud(canvas,game,safeTop);escapeArt.wallet(canvas,game,safeTop);if(game.finished)drawResults(canvas,game);return;}
        if(game.mode==12||game.mode==13){racingArt.scene(canvas,game,height,safeTop,controlsHeight);drawHud(canvas,game,safeTop);escapeArt.wallet(canvas,game,safeTop);if(game.finished)drawResults(canvas,game);return;}
        if(game.mode>=9){
            extrasArt.scene(canvas,game,height,safeTop,controlsHeight);drawHud(canvas,game,safeTop);escapeArt.wallet(canvas,game,safeTop);
            if(game.finished)drawResults(canvas,game);return;
        }
        if(game.mode>=6){
            collectionArt.scene(canvas,game,height,safeTop,controlsHeight);drawHud(canvas,game,safeTop);escapeArt.wallet(canvas,game,safeTop);
            if(game.finished)drawResults(canvas,game);return;
        }
        if(game.mode==5){
            duetArt.scene(canvas,game,height,safeTop,controlsHeight);drawHud(canvas,game,safeTop);escapeArt.wallet(canvas,game,safeTop);
            if(game.finished)drawResults(canvas,game);return;
        }
        if(game.mode==3){
            voiceArt.scene(canvas,game,height,safeTop,controlsHeight);drawHud(canvas,game,safeTop);escapeArt.wallet(canvas,game,safeTop);
            if(game.finished)drawResults(canvas,game);return;
        }
        if(game.mode==1){
            escapeArt.scene(canvas,game,ground-35,safeTop);drawHud(canvas,game,safeTop);escapeArt.wallet(canvas,game,safeTop);
            if(game.finished)drawResults(canvas,game);return;
        }
        if(game.mode==2||game.mode==4) {
            if(game.mode==2)drawGarden(canvas,game);else{drawMelody(canvas,game);escapeArt.wallet(canvas,game,safeTop);}
            drawHud(canvas,game,safeTop);
            if(game.finished)drawResults(canvas,game);
            return;
        }
        // The rendered scene remains in the lower camera region, never a full backdrop.
        drawRunway(canvas, game);
        drawEdgeClouds(canvas, game);
        int pose = game.target();
        float approach = Math.min(1, game.phase / game.gateApproach());
        float passage = game.gateState == 0 ? 0 : Math.min(1, game.gateAnimation / .95f);
        drawGate(canvas, GameSections.faceAt(game.stage,game.score+2), .08f, 180);
        drawGate(canvas, GameSections.faceAt(game.stage,game.score+1), .33f, 240);
        if (game.gateState == 0) {
            drawGate(canvas, pose, .44f + .56f * approach, 255);
            drawActor(canvas, game, game.gateMatched||pose>=3?pose:1, 0);
        } else if (game.gateState == 1) {
            // The actor travels into the opening; the arch occludes its outer edges.
            drawActor(canvas, game, pose, passage);
            drawGate(canvas, pose, 1 + passage * .44f, (int)(255 * (1 - passage)));
            drawCelebration(canvas, passage);
        } else {
            drawGate(canvas, pose, 1 + passage * .2f, (int)(255 * (1 - passage)));
            drawActor(canvas, game, pose, 0);
        }
        drawHud(canvas, game, safeTop);

        if (game.finished) drawResults(canvas, game);
    }

    private float x(float depth, float lateral) {
        return 260 - 65 * depth + lateral * (30 + 215 * (float)Math.pow(depth, 1.25));
    }
    private float y(float depth) { return horizon + (ground - horizon) * depth; }

    private void quad(Canvas c, float ax, float ay, float bx, float by,
                      float cx, float cy, float dx, float dy, int color) {
        path.reset(); path.moveTo(ax, ay); path.lineTo(bx, by);
        path.lineTo(cx, cy); path.lineTo(dx, dy); path.close();
        paint.setShader(null); paint.setStyle(Paint.Style.FILL); paint.setColor(color);
        c.drawPath(path, paint);
    }

    private void drawRunway(Canvas c, MiniGame game) {
        // Beveled front thickness makes this a miniature physical track, not a panel.
        quad(c,x(1,-1),y(1),x(1,1),y(1),x(1,1),y(1)+13,x(1,-1),y(1)+13,0xFF4F998D);
        for (int row = 0; row < 12; row++) {
            float near = (row + 1) / 12f, far = row / 12f;
            for (int col = 0; col < 5; col++) {
                float a = -1 + col * .4f, b = a + .4f;
                quad(c,x(far,a),y(far),x(far,b),y(far),x(near,b),y(near),x(near,a),y(near),
                    (row + col) % 2 == 0 ? SectionArt.light(game) : SectionArt.sky(game));
            }
        }
        // Warm golden railings in perspective, with tiny individual posts.
        paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(5);
        paint.setShader(new LinearGradient(0,horizon,0,ground,0xFFFFEFC1,0xFFEFB848,Shader.TileMode.CLAMP));
        for (int side : new int[]{-1,1}) {
            path.reset(); path.moveTo(x(0,side),y(0)-5); path.lineTo(x(1,side),y(1)-14); c.drawPath(path,paint);
            for (int post=1;post<6;post++) {
                float depth=post/6f;
                c.drawLine(x(depth,side),y(depth),x(depth,side),y(depth)-7-12*depth,paint);
            }
        }
        paint.setShader(null); paint.setStyle(Paint.Style.FILL);
        for (int i=0;i<5;i++) {
            float depth=.16f+i*.16f;
            float bob=(float)Math.sin(game.elapsed*2+i)*3;
            drawStar(c,x(depth,i%2==0?-1.17f:1.17f),y(depth)-25+bob,6+depth*6,0xFFFFD86D);
        }
        // Flags are small silhouettes in the distance, below the player's face.
        for(int i=0;i<4;i++) {
            float px=307+i*13, py=horizon-8+i*3;
            paint.setColor(0xFFC2B16F);paint.setStrokeWidth(1.5f);c.drawLine(px,py,px,py+33,paint);
            quad(c,px,py,px+10,py+2,px+9,py+14,px,py+12,i%2==0?0xFFEEB9D2:0xFFAAE6EB);
        }
    }

    private void drawEdgeClouds(Canvas c, MiniGame game) {
        for (int side : new int[]{0,1}) {
            for(int i=0;i<5;i++) {
                float cx=(side==0?-18:408)+(side==0?1:-1)*i*8;
                float cy=horizon+64+i*28;
                paint.setShader(new RadialGradient(cx-7,cy-9,32+i*2,new int[]{0xFFFFFDF2,0xFFF0EDFA,0xFFCACBE9},null,Shader.TileMode.CLAMP));
                c.drawCircle(cx,cy,24+i*2,paint);paint.setShader(null);
            }
        }
    }

    private void drawGate(Canvas c, int expression, float depth, int alpha) {
        if(alpha<=0)return;
        float width=48+203*depth;
        float bottom=y(depth)-8;
        float center=x(depth,0);
        destination.set(center-width*.5f,bottom-width,center+width*.5f,bottom);
        paint.setShader(null);paint.setStyle(Paint.Style.FILL);paint.setAlpha(alpha);
        if(expression>=3)extraGates.gate(c,expression,destination,paint);else c.drawBitmap(atlas,arches[expression],destination,paint);
        paint.setAlpha(255);
    }

    private void drawActor(Canvas c, MiniGame game, int pose, float passing) {
        float cx=194+(float)Math.sin(game.elapsed*1.6f)*3;
        float foot=ground-9;
        float width=122;
        if(passing>0) {
            cx=194+passing*43;foot-=passing*100;width*=1-passing*.45f;
        }
        float hop=(float)Math.sin(passing*Math.PI)*24;
        float breathe=1+(float)Math.sin(game.elapsed*3)*.018f;
        paint.setShader(null);paint.setColor(0x45496957);
        c.drawOval(cx-width*.3f,foot-5,cx+width*.3f,foot+7,paint);
        c.save(); c.translate(cx,foot-hop);
        if(game.gateState==2)c.rotate((float)Math.sin(game.gateAnimation*24)*5);
        else c.rotate((float)Math.sin(game.elapsed*2)*2);
        destination.set(-width*.5f,-width*breathe,width*.5f,0);
        paint.setAlpha(255);if(pose>=3)extraGates.actor(c,pose,destination,paint);else c.drawBitmap(atlas,actors[pose],destination,paint);c.restore();
    }

    private void drawStar(Canvas c,float cx,float cy,float radius,int color) {
        path.reset();
        for(int i=0;i<10;i++) {
            double a=-Math.PI/2+i*Math.PI/5;float r=i%2==0?radius:radius*.47f;
            float px=cx+(float)Math.cos(a)*r,py=cy+(float)Math.sin(a)*r;
            if(i==0)path.moveTo(px,py);else path.lineTo(px,py);
        }
        path.close();paint.setColor(color);paint.setShader(null);paint.setStyle(Paint.Style.FILL);c.drawPath(path,paint);
        paint.setColor(0xFFFFF5BE);c.drawCircle(cx-radius*.2f,cy-radius*.2f,radius*.15f,paint);
    }

    private void drawCelebration(Canvas c,float progress) {
        for(int i=0;i<12;i++) {
            double angle=i*Math.PI*2/12;
            float radius=20+progress*95;
            drawStar(c,195+(float)Math.cos(angle)*radius,ground-130+(float)Math.sin(angle)*radius,
                7*(1-progress)+2,i%2==0?0xFFFFD66D:0xFFFFB9CF);
        }
    }

    void status(Canvas c,MiniGame game,float safeTop){drawHud(c,game,safeTop);escapeArt.wallet(c,game,safeTop);}
    private void drawHud(Canvas c,MiniGame game,float safeTop) {
        float cy=safeTop+22;
        paint.setColor(0xD5304C52);canvasStageBadge(c,game,cy);
        if(game.mode==12){label(c,game.racing.playerDistance>=game.racing.rivalDistance?"1st":"2nd",18,cy+10,20,Color.WHITE,false);}
        for(int i=0;i<(game.mode==12?0:3);i++) {
            float cx=25+i*26;
            path.reset();path.moveTo(cx,cy+9);
            path.cubicTo(cx-24,cy-4,cx-8,cy-18,cx,cy-7);
            path.cubicTo(cx+8,cy-18,cx+24,cy-4,cx,cy+9);path.close();
            paint.setShader(new LinearGradient(cx,cy-12,cx,cy+10,
                i<game.hearts?0xFFFFA8B4:0xFFD6D8D7,i<game.hearts?0xFFED5576:0xFF999E9B,Shader.TileMode.CLAMP));
            paint.setShadowLayer(2,0,1,0x44000000);c.drawPath(path,paint);paint.clearShadowLayer();paint.setShader(null);
        }
        int target=game.mode>=6?game.goal():game.mode==3?game.voiceTarget():game.mode==1||game.mode==4?game.timedTarget():game.mode==0?game.goal():6;
        int progress=game.mode==2?game.score/2:game.score;
        label(c,Math.min(progress,target)+" / "+target,18,cy+36,17,Color.WHITE,false);
    }

    private void canvasStageBadge(Canvas c,MiniGame game,float cy){
        paint.setColor(0xD5304C52);c.drawRoundRect(136,cy-14,250,cy+13,13,13,paint);
        label(c,context.getString(R.string.fc_stage_badge,game.stage,game.paceLabel()),193,cy+4,11,0xFFFFE8AC,true);
    }
    private void drawCue(Canvas c,MiniGame game,int pose) {
        float center=195, baseline=horizon-76;
        String cue=game.finished?game.feedback:game.paused?"Paused":game.countdown>0?"Ready in "+(int)Math.ceil(game.countdown):game.prompt();
        if(game.mode==2)sprite(c,gardenMusic,plants[game.target()==0?0:2],center,baseline-20,45);
        else if(game.mode==4)sprite(c,gardenMusic,note,center,baseline-20,42);
        else {destination.set(center-24,baseline-68,center+24,baseline-20);paint.setAlpha(255);if(pose>=3)extraGates.actor(c,pose,destination,paint);else c.drawBitmap(atlas,actors[pose],destination,paint);}
        if(game.mode==0&&game.gateState==0&&!game.gateMatched&&game.countdown<=0&&!game.paused&&!game.finished)
            cue=game.prompt();
        label(c,cue,center,baseline,19,Color.WHITE,true);
        if(game.countdown<=0&&!game.finished&&!game.paused) {
            String hint=game.mode==0?(game.gateMatched?"Matched - relax":game.gateState==2?"Try the next gate":game.faceMode?"Match the face, then relax":"Tap the hero in touch mode"):
                game.mode==4?(game.resolved?game.feedback:game.microphoneMode&&!game.micReady?"Stay quiet for a second":game.jumpHint()):
                "Flower "+Math.min(6,game.score/2+1)+" of 6 - tap the hero to try";
            label(c,hint,center,baseline+22,11,0xFFECF8EF,true);
        }
    }

    private void sprite(Canvas c,Bitmap bitmap,Rect source,float center,float bottom,float width) {
        float height=width*source.height()/source.width();
        destination.set(center-width/2,bottom-height,center+width/2,bottom);
        paint.setShader(null);paint.setStyle(Paint.Style.FILL);paint.setAlpha(255);
        c.drawBitmap(bitmap,source,destination,paint);
    }

    private void drawGarden(Canvas c,MiniGame game) {
        // Separate pots and plants, with camera visible around and between them.
        int blooms=game.score/2, page=blooms>=3?3:0;
        for(int i=0;i<3;i++) {
            float cx=130+i*105, bottom=ground+20-i*27, width=122-i*15;
            int index=page+i, stage=index<blooms?2:index==blooms&&game.target()==1?1:0;
            float pop=game.flash>0&&index==Math.max(0,blooms-1)?1+(float)Math.sin(game.flash/.7f*Math.PI)*.07f:1;
            paint.setColor(0x35365733);c.drawOval(cx-width*.45f,bottom-8,cx+width*.45f,bottom+5,paint);
            sprite(c,gardenMusic,plants[stage],cx,bottom,width*pop);
        }
        sprite(c,gardenMusic,gardener,65,ground+28,132);
        for(int side:new int[]{0,1}) {
            for(int i=0;i<7;i++) {
                float cx=(side==0?9:382)+(float)Math.sin(i)*13, cy=ground-i*15;
                paint.setShader(new LinearGradient(cx-8,cy-25,cx+8,cy+12,0xFFACDA69,0xFF397F4B,Shader.TileMode.CLAMP));
                c.save();c.rotate((side==0?-1:1)*(20+i*9),cx,cy);c.drawOval(cx-9,cy-40,cx+9,cy+6,paint);c.restore();paint.setShader(null);
            }
        }
        bubble(c,30,horizon+45+(float)Math.sin(game.elapsed)*5,21);
        bubble(c,350,horizon+80+(float)Math.sin(game.elapsed+1)*6,28);
        bubble(c,290,ground+17,17);
        if(game.flash>0)for(int i=0;i<9;i++)drawStar(c,55+i*36,horizon+60+(float)Math.sin(i+game.elapsed)*19,4,0xFFFFE5A0);
    }

    private void bubble(Canvas c,float x,float y,float r) {
        paint.setShader(new RadialGradient(x-r*.3f,y-r*.4f,r*1.4f,new int[]{0x4FFFFFFF,0x0595C9DD,0x7BC9B2F0},null,Shader.TileMode.CLAMP));
        c.drawCircle(x,y,r,paint);paint.setShader(null);paint.setColor(0xB8FFFFFF);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(1.1f);c.drawCircle(x,y,r,paint);paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xDBFFFFFF);c.drawOval(x-r*.53f,y-r*.63f,x-r*.08f,y-r*.42f,paint);
    }

    private void melodyFire(Canvas c,float left,float right,float y,float time,boolean twin){
        left=Math.max(18,left);right=Math.min(372,right);if(right<=left)return;
        paint.setColor(0xFF55384C);c.drawRect(left,y-5,right,y+19,paint);
        paint.setColor(0xFFEF6039);c.drawRect(left,y+3,right,y+17,paint);
        for(float x=left+5;x<right;x+=13){float flame=12+7*(float)Math.sin(time*10+x*.12f);path.reset();path.moveTo(x-6,y+10);path.quadTo(x-8,y,x+2,y-flame);path.quadTo(x+9,y,x+6,y+10);path.close();paint.setColor(0xFFFFB548);c.drawPath(path,paint);paint.setColor(0xFFFFE895);c.drawOval(x-2,y,x+3,y+10,paint);}
        if(twin){float mid=(left+right)/2;paint.setColor(0xFF69536F);c.drawRoundRect(mid-6,y-6,mid+6,y+18,3,3,paint);}
    }
    private void melodyPlatform(Canvas c,float left,float right,float top,float bottom,boolean target,int kind){
        left=Math.max(18,left);right=Math.min(372,right);if(right<=left)return;
        paint.setColor(kind==3?0xFF674D91:0xFF6475A1);c.drawRect(left,top,right,bottom,paint);
        paint.setColor(target?0xFFFFDF91:0xFFE1DAFF);c.drawRect(left,top,right,top+5,paint);
        paint.setColor(0x40624D78);for(float x=left+12;x<right;x+=30)c.drawRect(x,top+13,Math.min(right,x+15),Math.min(bottom,top+20),paint);
    }
    private void drawMelody(Canvas c,MiniGame game) {
        float base=ground-80+game.melodyCameraLift(),heroX=85;
        float rise=game.resolved?MelodyCourse.height(game.stage,game.encounter):game.jumpRequired(),targetY=base-rise;
        float edge=game.melodyGapStart(),landing=game.melodyLandingX(),nextEdge=game.melodyNextGap();
        int kind=MelodyCourse.kind(game.stage,game.encounter);
        boolean now=!game.resolved&&game.phase>=game.windowOpens()&&game.phase<=game.windowCloses();
        c.save();c.clipRect(18,ground-252,372,ground-44);
        // Platforms and fire move from right to left; the runner stays at the take-off marker.
        melodyFire(c,edge,landing,ground-71,game.elapsed,kind==2);
        melodyPlatform(c,-300,edge,base,ground-52,false,0);
        melodyPlatform(c,landing,nextEdge,targetY,ground-52,now,kind);
        float preview=nextEdge+(2.65f-game.windowOpens())*MelodyCourse.speed(game.stage);
        melodyFire(c,nextEdge,preview,ground-71,game.elapsed,false);
        melodyPlatform(c,preview,preview+170,targetY-MelodyCourse.height(game.stage,game.encounter+1),ground-52,false,MelodyCourse.kind(game.stage,game.encounter+1));
        if(Float.isFinite(MelodyCourse.ceilingMargin(game.stage,game.encounter))){
            float underside=targetY-74-MelodyCourse.ceilingMargin(game.stage,game.encounter);
            paint.setColor(0xFF6B477A);c.drawRect(Math.max(18,landing),underside-12,Math.min(372,landing+100),underside,paint);
            paint.setColor(0xFFFFB37B);for(float x=Math.max(18,landing)+5;x<Math.min(372,landing+100);x+=15){path.reset();path.moveTo(x,underside);path.lineTo(x+5,underside+6);path.lineTo(x+10,underside);path.close();c.drawPath(path,paint);}
        }
        if(!game.resolved){
            sprite(c,gardenMusic,note,landing+22,targetY-11,28);
            paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(2);paint.setColor(now?0xFFFFD775:0x668DDBFF);
            c.drawOval(landing+3,targetY-29,landing+41,targetY+3,paint);paint.setStyle(Paint.Style.FILL);
        }
        float runnerFeet=game.melodyFeet(base);if(!game.jumpUsed&&!game.resolved&&game.phase<game.windowOpens())runnerFeet-=(float)Math.abs(Math.sin(game.elapsed*13))*3;
        if(!game.melodyImpact)sprite(c,gardenMusic,musician,heroX,runnerFeet,64);
        else for(int i=0;i<7;i++)drawStar(c,heroX+(i-3)*7,ground-76+(i%2)*8,5,0xFFFFE194);
        if(game.earnedCoin)for(int i=0;i<5;i++)drawStar(c,heroX+(i-2)*14,runnerFeet-55-(i%2)*12,4+game.flash*3,0xFFFFDF88);
        c.restore();
        float meterY=ground-57,strength=game.microphoneMode?game.soundStrength:game.touchHolding?MiniGame.touchStrength(game.touchHoldTime):0;
        paint.setColor(0xBB354763);c.drawRoundRect(114,meterY,344,meterY+8,4,4,paint);
        paint.setColor(0xFFFFD36B);c.drawRoundRect(114,meterY,114+230*Math.max(0,Math.min(1,strength)),meterY+8,4,4,paint);
        paint.setColor(Color.WHITE);paint.setStrokeWidth(2);float mark=114+230*MelodyCourse.targetStrength(game.stage,game.encounter);c.drawLine(mark,meterY-3,mark,meterY+11,paint);
        label(c,game.microphoneMode?"POWER":"PRACTICE",36,meterY+8,9,Color.WHITE,false);
        label(c,context.getString(R.string.fc_obstacle_count,Math.min(8,game.encounter+1)),310,ground-205,9,Color.WHITE,true);
    }

    private void label(Canvas c,String text,float x,float y,float size,int color,boolean center) {
        text=AppText.t(context,text);paint.setShader(null);paint.setStyle(Paint.Style.FILL);paint.setColor(color);
        paint.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));paint.setTextSize(size);
        while(paint.measureText(text)>348&&size>12)paint.setTextSize(--size);
        paint.setShadowLayer(3,0,1.5f,0xCC193C38);
        LocalizedCanvas.draw(context,c,paint,text,x,y,center,348);paint.clearShadowLayer();
    }

    private void drawResults(Canvas c,MiniGame game) {
        float cy=horizon+100;
        paint.setColor(0xE826514B);c.drawRoundRect(46,cy-43,344,cy+87,24,24,paint);
        for(int i=0;i<3;i++)drawStar(c,154+i*41,cy-16,14,game.won?0xFFFFD66D:0xFF9CB1AE);
        label(c,AppText.english(context)?game.resultTitle():GameLanguage.action(context,game),195,cy+22,23,Color.WHITE,true);
        label(c,context.getString(R.string.fc_run_best,game.runCoins(),game.bestRunCoins),195,cy+45,13,0xFFFFDE8A,true);
        label(c,AppText.english(context)?game.resultAction()+(game.nextRunStage()==1?" from Stage 1":"")+" - tap the circle":AppText.t(context,game.resultAction()),195,cy+69,11,0xFFDBEFDE,true);
    }
}
