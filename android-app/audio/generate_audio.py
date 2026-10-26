"""Original Face Chaos PCM cues. Standard library only; no downloaded samples."""
from pathlib import Path
import math, struct, wave
ROOT=Path(__file__).resolve().parents[1]/'app/src/main/res/raw'
RATE=22050

def note(freq, duration, gain=.36):
    result=[]
    for i in range(round(RATE*duration)):
        t=i/RATE
        envelope=min(1,t/.008)*max(0,1-t/duration)**1.8
        value=(math.sin(2*math.pi*freq*t)+.2*math.sin(4*math.pi*freq*t))*gain*envelope
        result.append(value)
    return result

def phrase(notes, step=.12, gain=.36):
    result=[]
    for frequency in notes:
        result+=note(frequency,step,gain) if frequency else [0.]*round(RATE*step)
    return result

def save(name, values):
    pcm=b''.join(struct.pack('<h',round(max(-.9,min(.9,x))*32767)) for x in values)
    with wave.open(str(ROOT/(name+'.wav')),'wb') as stream:
        stream.setnchannels(1);stream.setsampwidth(2);stream.setframerate(RATE);stream.writeframes(pcm)

cues={
 'fx_start':phrase([523,659,784],.09),
 'fx_countdown':note(660,.09,.25),
 'fx_go':phrase([784,1047],.1),
 'fx_correct':phrase([659,880],.08),
 'fx_coin':phrase([1047,1568,2093],.065,.28),
 'fx_miss':phrase([330,247],.14,.24),
 'fx_stage':phrase([523,659,784,1047,0,1175,1047],.12),
 'fx_complete':phrase([523,659,784,1047,784,1047,1319],.17),
 'fx_fail':phrase([392,330,262,220],.17,.25),
 'fx_pause':phrase([440,349],.08,.22),
 'fx_click':note(880,.045,.18),
}
for name,values in cues.items():save(name,values)
melody=[60,64,67,64,62,65,69,65,60,64,67,72,69,67,64,62]
values=[]
for index,midi in enumerate(melody):
    frequency=440*2**((midi-69)/12)
    top=note(frequency,.3,.13);bass=note(130.81 if index<8 else 164.81,.3,.08)
    values.extend(a+b for a,b in zip(top,bass))
save('music_garden',values)
for file in ROOT.glob('*.wav'):
    with wave.open(str(file),'rb') as stream:
        assert stream.getnchannels()==1 and stream.getframerate()==RATE
        assert 0<stream.getnframes()<RATE*8
print('Generated and checked 12 original PCM audio assets.')
