"""Local sound instructions and opt-in microphone level check; no classifier."""
import queue
import tkinter as tk
import webbrowser
from tkinter import ttk
from urllib.parse import quote_plus

import numpy as np


SOUNDS = (
    ('Humming', 'humming sound mmm',
     'Keep your lips together and make a gentle, steady mmm for about one second.',
     'Humming Hills - power a platform'),
    ('Sh sound', 'sh sound pronunciation voiceless postalveolar fricative',
     'Make shhh, the beginning sound in shoe, for about one second. Do not say the word shoe.',
     'Windy Valley - move objects with wind'),
    ('Ah vowel', 'open back unrounded vowel ah sound pronunciation',
     'Open your mouth comfortably and voice a short, steady ah. Do not shout.',
     'Echo Caves - send a sonic pulse'),
    ('Whistling', 'pursed lip whistling sound tutorial',
     'Round your lips and make a short, comfortable whistle. Optional: use a keyboard alternative.',
     'Whistling Heights - call a flying companion'),
)


class SoundPractice:
    def __init__(self, parent):
        self.window = tk.Toplevel(parent)
        self.window.title('Face Chaos - Sound Practice')
        self.window.geometry('760x620')
        self.stream = None
        self.messages = queue.Queue(maxsize=8)
        self.status = tk.StringVar(value='Microphone OFF. Sound recognition is not implemented.')
        self.level = tk.DoubleVar(value=0)
        self.device = tk.StringVar()
        self.devices = {}
        self.closed = False
        body = ttk.Frame(self.window, padding=14)
        body.pack(fill='both', expand=True)
        ttk.Label(body, text='SOUND PRACTICE', font=('Segoe UI', 18, 'bold')).pack(anchor='w')
        ttk.Label(body, text='Learn the sound, then check that your microphone hears it.\n'
                  'The level meter measures volume only; it cannot identify which sound you made.',
                  wraplength=700).pack(anchor='w', pady=8)
        notebook = ttk.Notebook(body)
        notebook.pack(fill='both', expand=True)
        for name, search, instruction, stage in SOUNDS:
            tab = ttk.Frame(notebook, padding=16)
            notebook.add(tab, text=name)
            ttk.Label(tab, text=name, font=('Segoe UI', 16, 'bold')).pack(anchor='w')
            ttk.Label(tab, text=instruction, wraplength=650).pack(anchor='w', pady=10)
            ttk.Label(tab, text='Search online: ' + search, wraplength=650).pack(anchor='w', pady=6)
            ttk.Button(tab, text='Search this sound online',
                       command=lambda phrase=search: webbrowser.open(
                           'https://www.google.com/search?q=' + quote_plus(phrase))).pack(anchor='w')
            ttk.Label(tab, text='Planned stage: ' + stage + '\nStatus: candidate control - not connected to gameplay.',
                      wraplength=650).pack(anchor='w', pady=15)
        ttk.Button(body, text='Hear speech examples (university reference)',
                   command=lambda: webbrowser.open('https://seeingspeech.ac.uk/speechstar/ipa-charts/')).pack(anchor='w', pady=8)
        ttk.Label(body, text='Microphone level check', font=('Segoe UI', 11, 'bold')).pack(anchor='w')
        row = ttk.Frame(body)
        row.pack(fill='x', pady=6)
        self.selector = ttk.Combobox(row, textvariable=self.device, state='readonly', width=45)
        self.selector.pack(side='left')
        ttk.Button(row, text='Refresh devices', command=self.refresh_devices).pack(side='left', padx=4)
        ttk.Button(row, text='Start mic', command=self.start).pack(side='left', padx=4)
        ttk.Button(row, text='Stop mic', command=self.stop).pack(side='left')
        ttk.Progressbar(body, variable=self.level, maximum=100).pack(fill='x', pady=5)
        ttk.Label(body, textvariable=self.status, wraplength=700).pack(anchor='w')
        ttk.Label(body, text='Audio stays in memory for the level check and is discarded. No recordings or game events.\n'
                  'Search buttons open your browser; microphone audio is not sent to those pages.',
                  wraplength=700).pack(anchor='w', pady=8)
        self.window.protocol('WM_DELETE_WINDOW', self.close)
        self.timer = self.window.after(75, self.poll)

    def refresh_devices(self):
        if self.stream is not None:
            self.status.set('Stop the microphone before changing devices.')
            return
        try:
            import sounddevice as sd
            self.devices = {f'{i}: {d["name"]}': i for i, d in enumerate(sd.query_devices())
                            if d['max_input_channels'] > 0}
            self.selector['values'] = tuple(self.devices)
            if self.device.get() not in self.devices:
                self.device.set(next(iter(self.devices), ''))
            self.status.set('Choose an input and press Start mic.' if self.devices else 'No microphone input found.')
        except Exception as error:
            self.status.set('Microphone unavailable: ' + str(error))

    def start(self):
        if self.stream is not None:
            return
        if not self.devices:
            self.refresh_devices()
        if self.device.get() not in self.devices:
            return
        try:
            import sounddevice as sd
            self.stream = sd.InputStream(device=self.devices[self.device.get()], channels=1,
                                         dtype='float32', callback=self.receive)
            self.stream.start()
            self.status.set('Listening locally - volume only, no sound classification.')
        except Exception as error:
            self.stop()
            self.status.set('Cannot start microphone: ' + str(error))

    def receive(self, data, frames, timing, status):
        valid = data.size > 0 and np.isfinite(data).all()
        rms = float(np.sqrt(np.mean(np.square(data)))) if valid else 0.0
        peak = float(np.max(np.abs(data))) if valid else 0.0
        try:
            self.messages.put_nowait((rms, peak, str(status) if valid else 'invalid or empty audio input'))
        except queue.Full:
            pass

    def poll(self):
        try:
            active = self.stream is None or bool(self.stream.active)
        except Exception:
            active = False
        if not active:
            self.stop()
            self.status.set('Microphone stopped or disconnected. Select an input and start again.')
        latest = None
        while not self.messages.empty():
            latest = self.messages.get_nowait()
        if self.stream is not None and latest:
            rms, peak, error = latest
            # dBFS mapped -60..0 to 0..100. This is not sound confidence.
            self.level.set(float(np.clip((20 * np.log10(max(rms, 1e-6)) + 60) / 60 * 100, 0, 100)))
            self.status.set('Audio input warning: ' + error if error else
                            'Input clipping - reduce microphone gain.' if peak >= .98 else
                            'Listening locally - volume only, no sound classification.')
        if not self.closed:
            self.timer = self.window.after(75, self.poll)

    def stop(self):
        shutdown_failed = False
        if self.stream is not None:
            stream, self.stream = self.stream, None
            try:
                stream.stop()
            except Exception:
                try:
                    stream.abort()
                except Exception:
                    pass
            finally:
                try:
                    stream.close()
                except Exception:
                    shutdown_failed = True
        while not self.messages.empty():
            self.messages.get_nowait()
        self.level.set(0)
        self.status.set('Microphone cleanup failed; close the lab and check the device.' if shutdown_failed else
                        'Microphone OFF. Sound recognition is not implemented.')

    def close(self):
        self.closed = True
        self.window.after_cancel(self.timer)
        self.stop()
        self.window.destroy()
