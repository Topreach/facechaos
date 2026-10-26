package com.facechaos.mobile;
/** Original synthetic rhythm guides, not recordings or animal-identity models. */
final class AnimalGuide {
 static short[] samples(int animal){
  AnimalPatterns.Entry entry=AnimalPatterns.get(animal);int rate=16000;float duration=.4f;for(float call:entry.calls)duration+=call+.27f;
  short[] result=new short[(int)(rate*duration)];double phase=0;java.util.Random noise=new java.util.Random(42+animal);
  for(int i=0;i<result.length;i++){
   double time=i/(double)rate,start=.08,local=-1,length=0;
   for(float call:entry.calls){if(time>=start&&time<start+call){local=time-start;length=call;break;}start+=call+.27;}
   double value=0;
   if(local>=0){double env=Math.sin(Math.PI*local/length),frequency=entry.frequency*(1.12-.24*local/length);phase+=Math.PI*2*frequency/rate;value=env*(.60*Math.sin(phase)+.16*Math.sin(phase*2)+(animal==0||animal==9?.16*(noise.nextDouble()*2-1):0));}
   result[i]=(short)(value*12000);
  }
  return result;
 }
}
