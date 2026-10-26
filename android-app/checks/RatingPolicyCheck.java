package com.facechaos.mobile;
public final class RatingPolicyCheck {
 static void require(boolean b,String m){if(!b)throw new AssertionError(m);}
 public static void main(String[] args){long now=20*RatingPolicy.COOLDOWN;
 require(!RatingPolicy.due(2,0,false,false,now),"No prompt before enough rounds");require(RatingPolicy.due(3,0,false,false,now),"Prompt after three completed rounds, regardless of outcome");require(!RatingPolicy.due(30,now-1,false,false,now),"Dismissal respects cooldown");require(RatingPolicy.due(30,now-RatingPolicy.COOLDOWN,false,false,now),"Later reminder allowed");require(!RatingPolicy.due(30,now+1,false,false,now),"Clock rollback cannot repeat prompt");require(!RatingPolicy.due(30,0,true,false,now),"Saved rating stops automatic prompts");require(!RatingPolicy.due(30,0,false,true,now),"Don't ask again respected");for(int i=1;i<=5;i++)require(RatingPolicy.valid(i),"All scores accepted equally");for(float value:new float[]{0,6,-1,Float.NaN,Float.POSITIVE_INFINITY,2.5f})require(!RatingPolicy.valid(value),"Invalid rating rejected");System.out.println("PASS: optional rating after sufficient play, seven-day cooldown, clock rollback, opt-out and unbiased 1-5 star validation");
 }
}
