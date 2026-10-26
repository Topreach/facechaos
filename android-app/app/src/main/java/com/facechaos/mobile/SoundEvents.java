package com.facechaos.mobile;

/** Event selection is independent of playback and never changes game scores. */
final class SoundEvents {
    private MiniGame last;
    private int score,coins,hearts,count,memoryIndex;
    private boolean matched;
    private boolean finished,playing;
    String observe(MiniGame game,boolean nowPlaying){
        String event=null;int nextCount=(int)Math.ceil(game.countdown);
        if(game!=last)event=nowPlaying?"start":null;
        else if(game.finished&&!finished)event=game.stopped?"pause":!game.won?"fail":game.stage<MiniGame.STAGE_COUNT?"stage":"complete";
        else if(nowPlaying&&!playing&&game.countdown>0)event="start";
        else if(nowPlaying){
            if(game.hearts<hearts)event="miss";
            else if(game.coins>coins)event="coin";
            else if(game.mode!=12&&game.score>score||game.memoryIndex>memoryIndex||game.gateMatched&&!matched)event="correct";
            else if(nextCount<count)event=nextCount==0?"go":"countdown";
        }else if(playing&&!game.finished)event="pause";
        last=game;score=game.score;coins=game.coins;hearts=game.hearts;count=nextCount;finished=game.finished;playing=nowPlaying;memoryIndex=game.memoryIndex;matched=game.gateMatched;return event;
    }
    static boolean effectsAllowed(MiniGame game){return !game.microphoneMode||game.finished;}
    static boolean musicAllowed(MiniGame game,boolean playing){return playing&&!game.finished&&!game.microphoneMode;}
}
