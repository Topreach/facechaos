"""Local facial test lab with optional calibrated-control connection to Godot."""
import argparse
import json
import math
import time
import tkinter as tk
from collections import Counter
from datetime import datetime, timezone
from pathlib import Path
from tkinter import filedialog, messagebox, ttk

from camera_capture import dark_frame, open_camera
from lab_core import (BY_KEY, KEYS, SECTIONS, FeatureEngine, LabDetector, TrialBook,
                      calibrate_section, prepare_detection_values)
from lab_tones import EventTones


class FacialLab:
    def __init__(self, root, camera=0, backend='auto', auto_open=True, game_connect=False):
        self.root = root
        root.title('Face Chaos - Facial Controls and Test Lab')
        width, height = min(1300, root.winfo_screenwidth() - 60), min(850, root.winfo_screenheight() - 80)
        root.geometry(f'{width}x{height}')
        root.minsize(min(960, width), min(680, height))
        self.cap = self.model = None
        self.engine, self.detector, self.book = FeatureEngine(), LabDetector(), TrialBook()
        self.appearance = {}
        self.appearance_diagnostics = {}
        self.calibration_failures = {}
        self.calibration_queue = []
        self.counts = Counter()
        self.values = None
        self.descriptor = None
        self.selected = KEYS[0]
        self.run = None
        self.clock = 0
        self.last_tick = None
        self.revision = 0
        self.calibration_camera = None
        self.camera_index = tk.IntVar(value=camera)
        self.backend = tk.StringVar(value=backend)
        self.status = tk.StringVar(value='Close OBS and the old webcam preview before opening the camera.')
        self.prompt = tk.StringVar(value='Teeth and tongue have live Raw readings. Select a section to calibrate its event threshold.')
        self.detail = tk.StringVar()
        self.sensitivity = tk.DoubleVar(value=.55)
        self.attempts = tk.IntVar(value=5)
        self.neutral_duration = tk.IntVar(value=60)
        self.sound_on = tk.BooleanVar(value=True)
        self.tones = EventTones()
        self.sound_practice = None
        self.game_bridge = None
        self.camera_preview = None
        self.render_avatar = {}
        self.preview_enabled = tk.BooleanVar(value=game_connect)
        self.game_enabled = tk.BooleanVar(value=False)
        self._build()
        self.select_section()
        root.protocol('WM_DELETE_WINDOW', self.close)
        if auto_open:
            root.after(150, self.connect_camera)
        root.after(30, self.tick)
        if game_connect:
            self.game_enabled.set(True)
            self.toggle_game_connection()

    def _build(self):
        style = ttk.Style()
        style.configure('Lab.Treeview', rowheight=28)
        top = ttk.Frame(self.root, padding=10)
        top.pack(fill='x')
        ttk.Label(top, text='FACIAL REACTION LAB', font=('Segoe UI', 19, 'bold')).pack(side='left')
        ttk.Checkbutton(top, text='Send controls to game', variable=self.game_enabled,
                        command=self.toggle_game_connection).pack(side='left', padx=8)
        ttk.Button(top, text='Test tone', command=self.test_tone).pack(side='right', padx=8)
        ttk.Button(top, text='Sound Practice', command=self.open_sound_practice).pack(side='right', padx=4)
        ttk.Checkbutton(top, text='Musical tones', variable=self.sound_on,
                        command=self.toggle_tones).pack(side='right')
        config = ttk.Frame(self.root, padding=(10, 0, 10, 6))
        config.pack(fill='x')
        ttk.Label(config, text='Camera index').pack(side='left')
        ttk.Spinbox(config, from_=0, to=9, textvariable=self.camera_index, width=4).pack(side='left', padx=6)
        ttk.Combobox(config, textvariable=self.backend, values=('auto', 'dshow', 'msmf'), state='readonly', width=9).pack(side='left')
        ttk.Button(config, text='Open / reconnect camera', command=self.connect_camera).pack(side='left', padx=6)
        ttk.Button(config, text='Stop camera', command=self.stop_camera).pack(side='left')
        ttk.Button(config, text='Save calibration', command=self.save_calibration).pack(side='left', padx=5)
        ttk.Button(config, text='Load calibration', command=self.load_calibration).pack(side='left')
        ttk.Button(config, text='Export test report', command=self.export_report).pack(side='right')
        ttk.Label(self.root, textvariable=self.status, wraplength=1100, padding=(10, 5)).pack(fill='x')
        body = ttk.Panedwindow(self.root, orient='horizontal')
        body.pack(fill='both', expand=True, padx=10)
        left, right = ttk.Frame(body), ttk.Frame(body)
        body.add(left, weight=2)
        body.add(right, weight=3)
        self.preview = ttk.Label(left, text='Camera preview', anchor='center', background='#17233e')
        self.preview.pack(fill='both', expand=True)
        ttk.Button(left, text='Capture cheek samples', command=self.start_cheek_calibration).pack(fill='x', pady=5)
        ttk.Checkbutton(left, text='Show live camera in game (local only)', variable=self.preview_enabled,
                        command=self.toggle_camera_preview).pack(anchor='w')
        ttk.Button(left, text='Minimize setup - play in Godot', command=self.root.iconify).pack(fill='x')
        ttk.Label(left, textvariable=self.prompt, font=('Segoe UI', 13, 'bold'), wraplength=400,
                  padding=(5, 10)).pack(fill='x')
        ttk.Label(left, text='Keep movements comfortable. A held reaction should fire once.\n'
                  'Raw/meter values are measurements, not accuracy or probability.\n'
                  'Tracking loss is shown as --, not a zero expression.', wraplength=400).pack(fill='x', pady=8)
        table = ttk.Frame(right)
        table.pack(fill='both', expand=True)
        self.tree = ttk.Treeview(table, columns=('raw', 'level', 'state', 'count'), style='Lab.Treeview',
                                 height=len(SECTIONS), selectmode='browse')
        self.tree.heading('#0', text='Facial section')
        self.tree.column('#0', width=190, minwidth=150)
        for key, title, width in [('raw', 'Raw', 55), ('level', 'Meter', 55),
                                   ('state', 'Status', 150), ('count', 'Events', 55)]:
            self.tree.heading(key, text=title)
            self.tree.column(key, width=width, minwidth=45)
        for section in SECTIONS:
            self.tree.insert('', 'end', iid=section.key, text=section.title,
                             values=('--', '--', 'Not calibrated', 0))
        scrollbar = ttk.Scrollbar(table, orient='vertical', command=self.tree.yview)
        self.tree.configure(yscrollcommand=scrollbar.set)
        scrollbar.pack(side='right', fill='y')
        self.tree.pack(side='left', fill='both', expand=True)
        self.tree.bind('<<TreeviewSelect>>', self.select_section)
        self.tree.selection_set(self.selected)
        ttk.Label(right, textvariable=self.detail, wraplength=650, padding=(5, 8)).pack(fill='x')
        self.log = tk.Text(right, height=4, state='disabled', wrap='word', font=('Consolas', 9))
        self.log.pack(fill='x')
        controls = ttk.Frame(self.root, padding=10)
        controls.pack(fill='x')
        ttk.Button(controls, text='Calibrate selected', command=self.start_calibration).pack(side='left')
        ttk.Button(controls, text='Capture teeth / tongue / cheeks', command=self.start_advanced_calibration).pack(side='left', padx=5)
        ttk.Button(controls, text='Disable selected', command=self.disable_selected).pack(side='left', padx=6)
        ttk.Label(controls, text='Activation threshold').pack(side='left', padx=(12, 2))
        ttk.Scale(controls, from_=.20, to=.85, variable=self.sensitivity,
                  command=self.change_sensitivity, length=130).pack(side='left')
        ttk.Button(controls, text='Cancel current task', command=self.cancel).pack(side='right')
        tests = ttk.Frame(self.root, padding=(10, 0, 10, 10))
        tests.pack(fill='x')
        ttk.Spinbox(tests, from_=5, to=100, textvariable=self.attempts, width=5).pack(side='left')
        ttk.Button(tests, text='Prompted trials: selected', command=self.start_trials).pack(side='left', padx=6)
        ttk.Button(tests, text='Test all calibrated', command=self.start_all_trials).pack(side='left', padx=3)
        ttk.Spinbox(tests, from_=10, to=3600, textvariable=self.neutral_duration, width=6).pack(side='left', padx=(10, 0))
        ttk.Button(tests, text='Seconds: neutral/confound test', command=self.start_neutral).pack(side='left', padx=6)
        ttk.Label(self.root, text='Save calibration to reuse your personal templates. Export report saves test numbers only. '
                  'Changing any detector clears previous test evidence.', padding=(10, 0, 10, 6)).pack(fill='x')

    def write_log(self, text):
        self.log.configure(state='normal')
        self.log.insert('end', text + '\n')
        if int(self.log.index('end-1c').split('.')[0]) > 100:
            self.log.delete('1.0', '30.0')
        self.log.see('end')
        self.log.configure(state='disabled')

    def toggle_game_connection(self):
        if self.camera_preview is not None:
            self.camera_preview.close()
            self.camera_preview = None
        if self.game_bridge is not None:
            self.game_bridge.close()
            self.game_bridge = None
        self.detector.reset()
        if self.game_enabled.get():
            try:
                from lab_game_bridge import LabGameBridge
                self.game_bridge = LabGameBridge()
                self.toggle_camera_preview()
                self.write_log('Game connection ON: jaw drop / jump; smile / shield; teeth / chomp; eye proxy / fly. Keyboard remains available.')
            except OSError as error:
                self.game_enabled.set(False)
                self.write_log('Cannot connect to game: ' + str(error))
        else:
            self.write_log('Game connection OFF. Local testing only.')

    def toggle_camera_preview(self):
        if self.camera_preview is not None:
            self.camera_preview.close()
            self.camera_preview = None
        if self.preview_enabled.get() and self.game_bridge is not None:
            try:
                from camera_preview import CameraPreview
                self.camera_preview = CameraPreview()
                self.write_log('Local in-game camera preview ON. No frame recording.')
            except OSError as error:
                self.preview_enabled.set(False)
                self.write_log('Camera preview unavailable (face controls still work): ' + str(error))

    def send_game_frame(self, tracked, events, meters):
        if self.game_bridge is not None:
            self.game_bridge.publish(tracked, self.detector.controls, events, meters,
                                     busy=bool(self.run) or bool(self.calibration_queue),
                                     available=self.cap is not None,
                                     preview=self.camera_preview.metadata() if self.camera_preview else None,
                                     avatar=self.render_avatar if tracked else {})

    def open_sound_practice(self):
        if self.sound_practice is None or self.sound_practice.closed:
            from sound_practice import SoundPractice
            self.sound_practice = SoundPractice(self.root)
        else:
            self.sound_practice.window.lift()

    def toggle_tones(self):
        self.tones.enabled = self.sound_on.get()

    def test_tone(self):
        self.toggle_tones()
        if not self.sound_on.get():
            self.prompt.set('Enable Musical tones to hear the test chord.')
            return
        self.tones.play(['mouth_open', 'brows_up', 'smile'])

    def select_section(self, _event=None):
        chosen = self.tree.selection()
        if chosen:
            self.selected = chosen[0]
        section = BY_KEY[self.selected]
        explanation = ('Needs personal training first; no built-in live score exists. '
                       'Click Capture cheek samples and follow the prompts. Calibration is not validation.') if section.kind == 'appearance' else (
            'Direct image candidate, independent of jaw opening. Live raw reading; calibrate for event counting.' if section.kind == 'visual' else
            'Experimental wink discrimination: test ordinary blinks and facial scrunches too.' if section.kind == 'experimental' else
            'Temporal/head movements require motion, not merely a held position.' if section.kind in ('temporal', 'motion') else
            'Uses calibrated observable movements; it does not infer emotions.')
        if section.key == 'brows_up':
            explanation = 'Current detector uses eyebrow movement as a proxy; pure eye widening is not verified.'
        self.detail.set(section.prompt + '\nSearch online: ' + section.title + '\n' + explanation)
        self.sensitivity.set(self.detector.controls.get(self.selected, {}).get('sensitivity', .55))

    def invalidate(self, key):
        self.revision += 1
        # Every test block describes one complete, unchanged detector configuration.
        self.book = TrialBook()
        self.detector.reset()

    def change_sensitivity(self, _value):
        if self.run:
            return
        config = self.detector.controls.get(self.selected)
        if config and abs(config['sensitivity'] - self.sensitivity.get()) > .001:
            config['sensitivity'] = self.sensitivity.get()
            self.invalidate(self.selected)
            self.prompt.set('Threshold changed: test evidence cleared for the changed detector configuration.')

    def disable_selected(self):
        if self.run:
            self.prompt.set('Cancel the current task before disabling a control.')
            return
        self.detector.controls.pop(self.selected, None)
        self.appearance.pop(self.selected, None)
        self.calibration_failures.pop(self.selected, None)
        self.invalidate(self.selected)
        self.prompt.set('Selected detector disabled; configuration changed and test evidence was cleared.')

    def connect_camera(self):
        self.stop_camera()
        self.detector.controls.clear()
        self.appearance.clear()
        self.appearance_diagnostics.clear()
        self.calibration_failures.clear()
        self.calibration_queue.clear()
        self.book = TrialBook()
        self.counts.clear()
        self.engine.reset()
        self.revision += 1
        self.status.set('Opening camera; close OBS and old camera previews if needed...')
        self.root.update_idletasks()
        try:
            import cv2
            import mediapipe as mp
            from mediapipe.tasks import python
            from mediapipe.tasks.python import vision
            model_path = Path(__file__).resolve().parent / 'models/face_landmarker.task'
            options = vision.FaceLandmarkerOptions(
                base_options=python.BaseOptions(model_asset_path=str(model_path)),
                running_mode=vision.RunningMode.VIDEO, num_faces=1,
                output_face_blendshapes=True, output_facial_transformation_matrixes=True,
                min_face_detection_confidence=.6, min_face_presence_confidence=.6,
                min_tracking_confidence=.6)
            self.model = vision.FaceLandmarker.create_from_options(options)
            self.cap, self.actual_backend, diagnostics = open_camera(self.camera_index.get(), self.backend.get())
            self.calibration_camera = self.camera_index.get()
            self.cv, self.mp = cv2, mp
            self.status.set('; '.join(diagnostics) + '. Select and calibrate a section.')
            self.write_log('Camera connected. Existing calibration and evidence cleared for device change.')
            self.prompt.set('Teeth and tongue read live. Load saved calibration or select a section to calibrate events. Cheeks still need samples.')
        except Exception as error:
            self.stop_camera()
            self.status.set('Camera setup failed: ' + str(error))

    def stop_camera(self):
        if self.camera_preview is not None:
            self.camera_preview.clear()
        self.render_avatar = {}
        if self.run:
            self.cancel('Task cancelled: camera closed.')
        if self.cap:
            self.cap.release()
            self.cap = None
        if self.model:
            self.model.close()
            self.model = None
        self.values = self.descriptor = None
        if hasattr(self, 'preview'):
            self.preview.configure(image='', text='Camera stopped')
            self.photo = None
        self.detector.reset()
        self.last_tick = None
        self.send_game_frame(False, [], {})
        self.status.set('Camera stopped. Game input unavailable; keyboard remains available.')

    def ready(self, require_calibrated=False):
        if self.run:
            self.prompt.set('Finish or cancel the current task first.')
            return False
        if self.values is None:
            self.prompt.set('A visible, tracked face is required. Open the shutter and close other camera apps.')
            return False
        if require_calibrated and self.selected not in self.detector.controls:
            self.prompt.set('Calibrate the selected reaction first.')
            return False
        return True

    def save_calibration(self):
        if self.run or self.calibration_queue:
            self.prompt.set('Finish or cancel sample capture before saving.')
            return
        if not self.detector.controls or self.calibration_camera is None:
            self.prompt.set('Capture and successfully calibrate at least one section first.')
            return
        profiles_dir = Path(__file__).resolve().parent / 'profiles'
        try:
            profiles_dir.mkdir(parents=True, exist_ok=True)
        except OSError as error:
            messagebox.showerror('Cannot create calibration folder', str(error))
            return
        path = filedialog.asksaveasfilename(title='Save personal calibration/templates locally (no images)',
            initialdir=str(profiles_dir),
            defaultextension='.json', initialfile=f'facial-lab-camera-{self.calibration_camera}.json',
            filetypes=[('Calibration JSON', '*.json')])
        if not path:
            return
        try:
            from lab_profile import save_profile
            save_profile(path, self.calibration_camera, self.detector.controls,
                         self.appearance, self.engine.pose_zero)
            self.prompt.set('Saved personal calibration. Next time connect this camera, then click Load calibration.')
            self.write_log('Saved thresholds and derived histogram templates locally; no images/video saved.')
        except (OSError, ValueError) as error:
            messagebox.showerror('Cannot save calibration', str(error))

    def load_calibration(self):
        if self.run or self.calibration_queue or self.cap is None:
            self.prompt.set('Connect the camera and finish/cancel the current task before loading calibration.')
            return
        path = filedialog.askopenfilename(title='Load personal calibration for this camera',
                                          initialdir=str(Path(__file__).resolve().parent / 'profiles'),
                                          filetypes=[('Calibration JSON', '*.json')])
        if not path:
            return
        try:
            from lab_profile import load_profile
            controls, models, pose = load_profile(path, self.calibration_camera)
            # Apply only after the entire file validates; never partially replace a working profile.
            self.detector.controls = controls
            self.appearance = models
            self.engine.reset_motion()
            self.engine.pose_zero = pose
            self.detector.reset()
            self.book = TrialBook()
            self.counts.clear()
            self.calibration_failures.clear()
            self.appearance_diagnostics.clear()
            self.revision += 1
            self.select_section()
            self.prompt.set('Compatible calibration loaded. Updated/removed controls may need recalibration. Return to neutral and retest; no evidence imported.')
        except (OSError, ValueError, TypeError) as error:
            messagebox.showerror('Cannot load calibration', str(error))

    def start_calibration(self):
        if not self.ready():
            return
        key = self.selected
        self.detector.controls.pop(key, None)
        self.appearance.pop(key, None)
        self.calibration_failures.pop(key, None)
        self.invalidate(key)
        stages = [('prepare', 2, 'Relax your face and keep head straight'), ('neutral', 3, 'Keep a relaxed neutral face')]
        for i in range(3):
            stages += [('prepare', 2, f'Relax. Prepare repetition {i + 1}/3'),
                       ('positive', 3, BY_KEY[key].prompt + ('; repeat twice' if BY_KEY[key].kind in ('motion', 'temporal') else ''))]
        if BY_KEY[key].kind == 'appearance':
            confounds = ['Open mouth WITHOUT showing teeth', 'Smile with lips closed',
                         'Move your lips as if speaking'] if key == 'teeth' else [
                             'Open mouth with tongue kept inside', 'Show teeth WITHOUT tongue out',
                             'Move your lips as if speaking']
            if key == 'cheek_puff':
                confounds = ['Smile WITHOUT puffing cheeks', 'Gently scrunch face WITHOUT puffing',
                             'Move lips as if speaking, cheeks relaxed']
            for prompt in confounds:
                stages += [('prepare', 2, 'Relax before the next negative example'), ('negative', 3, prompt)]
        self.run = {'mode': 'calibrate', 'key': key, 'stages': stages, 'index': 0,
                    'start': time.monotonic(), 'neutral': [], 'positives': [], 'negatives': [],
                    'stage_samples': [], 'stage_appearance': [], 'neutral_appearance': []}
        self.detector.reset()

    def start_advanced_calibration(self):
        if not self.ready():
            return
        self.calibration_queue = ['teeth', 'tongue_out', 'cheek_puff']
        self.advance_calibration()

    def start_cheek_calibration(self):
        if not self.ready():
            return
        self.selected = 'cheek_puff'
        self.tree.selection_set(self.selected)
        self.select_section()
        self.start_calibration()

    def advance_calibration(self):
        if not self.calibration_queue:
            return
        self.selected = self.calibration_queue.pop(0)
        self.tree.selection_set(self.selected)
        self.select_section()
        self.start_calibration()

    def start_trials(self, targets=None):
        if not self.ready(True):
            return
        try:
            count = max(5, min(100, self.attempts.get()))
        except (ValueError, tk.TclError):
            self.prompt.set('Enter 5 to 100 trials.')
            return
        targets = targets or [self.selected]
        self.run = {'mode': 'trials', 'key': targets[0], 'keys': targets, 'key_index': 0,
                    'target': count, 'done': 0,
                    'phase': 'rest', 'start': time.monotonic(), 'events': [], 'valid': True}
        self.detector.reset()

    def start_all_trials(self):
        if not self.detector.controls:
            self.prompt.set('Calibrate the reactions you can perform before testing them.')
            return
        targets = [key for key in KEYS if key in self.detector.controls]
        self.selected = targets[0]
        self.tree.selection_set(self.selected)
        self.start_trials(targets)

    def start_neutral(self):
        if not self.ready() or not self.detector.controls:
            return
        try:
            duration = max(10, min(3600, self.neutral_duration.get()))
        except (ValueError, tk.TclError):
            self.prompt.set('Enter a neutral-test duration in seconds.')
            return
        self.run = {'mode': 'neutral', 'start': time.monotonic(), 'duration': duration, 'tracked': 0.0}
        self.detector.reset()

    def cancel(self, reason='Task cancelled. Incomplete trials were not scored.'):
        self.calibration_queue.clear()
        if self.run and self.run['mode'] == 'trials':
            self.book.invalid_trials += 1
        self.run = None
        self.detector.reset()
        self.prompt.set(reason)
        self.write_log(reason)

    def task_tick(self, values, descriptor, events, now, dt):
        run = self.run
        if not run:
            return
        if values is None:
            self.cancel('Tracking lost: task cancelled. Restore face and retry; no fake zero samples saved.')
            return
        if run['mode'] == 'calibrate':
            kind, duration, prompt = run['stages'][run['index']]
            elapsed = now - run['start']
            self.prompt.set(f'{prompt}\n{max(0, duration - elapsed):.1f}s remaining')
            if kind != 'prepare':
                run['stage_samples'].append(values[run['key']])
                if descriptor is not None:
                    run['stage_appearance'].append(descriptor)
            if elapsed >= duration:
                if kind == 'neutral':
                    run['neutral'] = run['stage_samples']
                    run['neutral_appearance'] = run['stage_appearance']
                elif kind == 'positive':
                    run['positives'].append((run['stage_samples'], run['stage_appearance']))
                elif kind == 'negative':
                    run['negatives'].append(run['stage_appearance'])
                run['stage_samples'], run['stage_appearance'] = [], []
                run['index'] += 1
                run['start'] = now
                if run['index'] == len(run['stages']):
                    self.finish_calibration(run)
        elif run['mode'] == 'trials':
            elapsed = now - run['start']
            if run['phase'] == 'rest':
                self.prompt.set(f'Return to NEUTRAL\nTrial {run["done"] + 1}/{run["target"]} begins in {max(0, 2 - elapsed):.1f}s')
                if elapsed >= 2:
                    run.update(phase='active', start=now, events=[])
            else:
                self.prompt.set(f'Trial {run["done"] + 1}/{run["target"]}: {BY_KEY[run["key"]].prompt}\n{max(0, 3 - elapsed):.1f}s remaining')
                run['events'].extend(events)
                if elapsed >= 3:
                    self.book.trial(run['key'], run['events'])
                    self.write_log(f'{BY_KEY[run["key"]].title}: ' + ('DETECTED' if run['key'] in run['events'] else 'MISSED')
                                   + f'; all events: {run["events"]}')
                    run['done'] += 1
                    if run['done'] == run['target']:
                        if run['key_index'] + 1 < len(run['keys']):
                            run['key_index'] += 1
                            run.update(key=run['keys'][run['key_index']], done=0, phase='rest', start=now, events=[])
                            self.tree.selection_set(run['key'])
                        else:
                            self.run = None
                            self.prompt.set('Trial block complete. Review results or export report.')
                    else:
                        run.update(phase='rest', start=now, events=[])
        elif run['mode'] == 'neutral':
            if now - run['start'] < 2:
                self.prompt.set('Prepare: relax your face. Then blink/speak/move naturally, without intentional commands.')
                return
            run['tracked'] += min(dt, .2)
            self.book.neutral(min(dt, .2), events)
            self.prompt.set(f'Neutral/confound test: {max(0, run["duration"] - run["tracked"]):.0f}s tracked time left\n'
                            'Natural blinking, speaking and small head movement. Avoid intentional commands.')
            if run['tracked'] >= run['duration']:
                self.run = None
                self.prompt.set('Neutral test complete. Export report to review false events.')

    def finish_calibration(self, run):
        key = run['key']
        try:
            if BY_KEY[key].kind == 'appearance':
                from lab_appearance import AppearanceModel
                model = AppearanceModel()
                negative, positives = model.fit([r[1] for r in run['positives']],
                                                [run['neutral_appearance']] + run['negatives'])
                config = calibrate_section(negative, positives)
                self.appearance[key] = model
            else:
                config = calibrate_section(run['neutral'], [r[0] for r in run['positives']])
            self.detector.controls[key] = config
            self.calibration_failures.pop(key, None)
            self.sensitivity.set(config['sensitivity'])
            self.prompt.set('Calibrated, NOT validated. Return to neutral, then test. Use Save calibration to keep these samples.')
            self.write_log(BY_KEY[key].title + ': calibrated from three repetitions. Validation still required.')
        except ValueError as error:
            self.detector.controls.pop(key, None)
            self.appearance.pop(key, None)
            self.calibration_failures[key] = str(error)
            self.prompt.set('Calibration rejected: ' + str(error))
            self.write_log(BY_KEY[key].title + ': rejected - ' + str(error))
        self.run = None
        self.detector.reset()
        if self.calibration_queue:
            self.root.after(100, self.advance_calibration)

    def tick(self):
        now = time.monotonic()
        dt = max(0, now - self.last_tick) if self.last_tick is not None else 0
        self.last_tick = now
        if dt > .5 and self.run:
            self.cancel('Long processing gap: incomplete task cancelled. Retry after the camera stabilizes.')
        try:
            if self.cap and self.model:
                self.read_frame(now, dt)
        except Exception as error:
            self.stop_camera()
            self.status.set('Camera/lab error: ' + str(error))
        if self.values is None:
            for key in KEYS:
                self.tree.set(key, 'raw', '--')
                self.tree.set(key, 'level', '--')
        self.root.after(25, self.tick)

    def read_frame(self, now, dt):
        import numpy as np
        from PIL import Image, ImageTk
        from lab_appearance import mouth_descriptor, cheek_descriptor
        from visible_teeth import measure_teeth
        from visible_tongue import measure_tongue
        ok, frame = self.cap.read()
        if not ok:
            raise RuntimeError('Camera stopped returning frames; reconnect it.')
        self.clock = max(self.clock + 1, int(now * 1000))
        result = self.model.detect_for_video(self.mp.Image(image_format=self.mp.ImageFormat.SRGB,
            data=self.cv.cvtColor(frame, self.cv.COLOR_BGR2RGB)), self.clock)
        tracked = bool(result.face_blendshapes and result.face_landmarks) and not dark_frame(frame)
        self.values = self.descriptor = None
        cheek_features = None
        self.appearance_diagnostics.clear()
        if tracked:
            scores = {v.category_name: float(v.score) for v in result.face_blendshapes[0]}
            self.render_avatar = {'blink_left': scores.get('eyeBlinkLeft', 0),
                                  'blink_right': scores.get('eyeBlinkRight', 0),
                                  'jaw': scores.get('jawOpen', 0),
                                  'smile': (scores.get('mouthSmileLeft', 0) + scores.get('mouthSmileRight', 0)) / 2}
            pose = None
            if result.facial_transformation_matrixes:
                rotation = np.asarray(result.facial_transformation_matrixes[0])[:3, :3]
                pitch = math.degrees(math.atan2(rotation[2, 1], rotation[2, 2]))
                yaw = math.degrees(math.atan2(-rotation[2, 0], math.hypot(rotation[2, 1], rotation[2, 2])))
                roll = math.degrees(math.atan2(rotation[1, 0], rotation[0, 0]))
                pose = pitch, yaw, roll
            self.values = self.engine.extract(scores, pose, now)
            teeth = measure_teeth(frame, result.face_landmarks[0])
            self.values['teeth'] = teeth['score']
            self.appearance_diagnostics['teeth'] = teeth
            tongue = measure_tongue(frame, result.face_landmarks[0])
            self.values['tongue_out'] = tongue['score']
            self.appearance_diagnostics['tongue_out'] = tongue
            self.descriptor = mouth_descriptor(frame, result.face_landmarks[0])
            cheek_features = cheek_descriptor(frame, result.face_landmarks[0])
            native_cheek = self.values['cheek_puff']
            if 'cheek_puff' in self.appearance:
                diagnostic = self.appearance['cheek_puff'].explain(cheek_features)
                self.values['cheek_puff'] = diagnostic['score']
            else:
                # Show the actual upstream measurement, not an invented probability.
                # It is diagnostic only until personal appearance training succeeds.
                diagnostic = {'score': native_cheek,
                              'state': 'personal cheek detector not trained; click Capture cheek samples'}
            diagnostic['native_model_measurement'] = native_cheek
            self.appearance_diagnostics['cheek_puff'] = diagnostic
        else:
            self.engine.reset_motion()
            self.render_avatar = {}
        if self.camera_preview is not None:
            self.camera_preview.update(self.cv.flip(frame, 1))
        detection_values = prepare_detection_values(self.values, self.appearance)
        events, meters = self.detector.update(detection_values, now)
        self.send_game_frame(tracked, events, meters)
        if self.run and self.run['mode'] == 'calibrate':
            # Calibration never generates test events or affects counts.
            self.detector.reset()
            events = []
        self.counts.update(events)
        if events:
            self.write_log('Detected: ' + ', '.join(BY_KEY[k].title for k in events))
            if not self.run or self.run['mode'] != 'neutral':
                self.tones.play(events)
        if self.tones.error:
            self.sound_on.set(False)
            self.write_log('Musical tones unavailable: ' + self.tones.error)
            self.tones.error = None
        capture_features = cheek_features if self.run and self.run.get('key') == 'cheek_puff' else self.descriptor
        self.task_tick(self.values, capture_features, events, now, dt)
        report = self.book.report()['results']
        for key in KEYS:
            raw = self.values.get(key) if self.values is not None else None
            state = 'Calibrated / UNTESTED' if key in self.detector.controls else 'Not calibrated'
            data = report[key]
            if key in self.detector.controls and data['attempts']:
                state = f'{data["detected"]}/{data["attempts"]} prompted trials'
            if key in ('teeth', 'tongue_out') and key not in self.detector.controls:
                state = 'LIVE: calibrate for events'
            if key == 'cheek_puff' and key not in self.appearance:
                state = 'Needs cheek samples'
            if key in self.calibration_failures:
                state = 'REJECTED: select for reason'
            if key in self.detector.suppressed:
                reason = self.detector.suppressed[key]
                state = ('Suppressed: show teeth' if 'teeth' in reason else
                         'Suppressed: tongue' if 'tongue' in reason else 'Suppressed: face scrunch')
            if self.run and self.run['mode'] == 'calibrate' and self.run['key'] == key:
                state = 'Training - follow prompt'
            self.tree.item(key, values=(f'{raw:.2f}' if raw is not None else '--',
                f'{meters[key]:.2f}' if key in meters else '--', state, self.counts[key]))
        status = ('TRACKED' if tracked else 'BLACK/DARK IMAGE' if dark_frame(frame) else 'TRACKING LOST')
        connection = 'GAME SEND ON (local UDP)' if self.game_bridge else 'GAME SEND OFF'
        if self.game_bridge and self.game_bridge.error:
            connection = 'GAME SEND ERROR: ' + self.game_bridge.error
        self.status.set(f'{status} | Camera {self.camera_index.get()} / {self.actual_backend} | '
                        f'{len(self.detector.controls)}/{len(SECTIONS)} calibrated | {connection}')
        if self.selected in ('teeth', 'tongue_out', 'cheek_puff'):
            diagnostic = self.appearance_diagnostics.get(self.selected, {'state': 'no tracked region'})
            reason = self.calibration_failures.get(self.selected)
            text = 'Training rejected: ' + reason if reason else 'Detector: ' + diagnostic['state']
            if 'positive_distance' in diagnostic:
                text += f' | sample distance {diagnostic["positive_distance"]:.2f}, allowed {diagnostic["radius"]:.2f}'
            if self.selected == 'tongue_out' and 'candidate_pixels' in diagnostic:
                text += (f'\nCandidate pixels {diagnostic["candidate_pixels"]}; past lip {diagnostic["extended_pixels"]}'
                         f'; lip pixels excluded {diagnostic.get("excluded_lip_pixels", 0)}')
            self.detail.set(BY_KEY[self.selected].prompt + '\nSearch online: '
                            + BY_KEY[self.selected].title + '\n' + text)
        # Never persist the camera image. Tk holds only the current display frame.
        frame = self.cv.cvtColor(self.cv.flip(frame, 1), self.cv.COLOR_BGR2RGB)
        picture = Image.fromarray(frame)
        picture.thumbnail((max(240, self.preview.winfo_width()), max(180, self.preview.winfo_height())))
        self.photo = ImageTk.PhotoImage(picture)
        self.preview.configure(image=self.photo, text='')

    def export_report(self):
        if self.run:
            messagebox.showinfo('Finish current task', 'Finish or cancel the current task before exporting.')
            return
        path = filedialog.asksaveasfilename(title='Export derived test statistics (no camera images)',
                                           defaultextension='.json', initialfile='facechaos-facial-test.json',
                                           filetypes=[('JSON report', '*.json')])
        if not path:
            return
        report = {'schema': 1, 'created_utc': datetime.now(timezone.utc).isoformat(),
                  'lab': 'standalone-v4-teeth-smile-priority', 'detector_revision': 4, 'calibration_revision': self.revision,
                  'camera_index': self.camera_index.get(), 'backend': getattr(self, 'actual_backend', None),
                  'calibrated': list(self.detector.controls), 'thresholds': self.detector.controls,
                  'calibration_failures': dict(self.calibration_failures),
                  'appearance_diagnostics': dict(self.appearance_diagnostics),
                  'experimental': ['teeth', 'tongue_out', 'wink_left', 'wink_right', 'cheek_puff'],
                  'game_connection': self.game_bridge is not None, **self.book.report()}
        try:
            Path(path).write_text(json.dumps(report, indent=2, allow_nan=False), encoding='utf-8')
            self.write_log('Test statistics exported. No camera images or appearance descriptors were included.')
        except OSError as error:
            messagebox.showerror('Export failed', str(error))

    def close(self):
        if self.sound_practice is not None and not self.sound_practice.closed:
            self.sound_practice.close()
        self.stop_camera()
        if self.game_bridge is not None:
            self.game_bridge.close()
        if self.camera_preview is not None:
            self.camera_preview.close()
        self.tones.close()
        self.root.destroy()


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--camera', type=int, default=0)
    parser.add_argument('--backend', choices=('auto', 'dshow', 'msmf'), default='auto')
    parser.add_argument('--no-camera', action='store_true', help='Open the lab without accessing the camera')
    parser.add_argument('--game-connect', action='store_true', help='Send calibrated core controls to the local game')
    args = parser.parse_args()
    root = tk.Tk()
    FacialLab(root, args.camera, args.backend, auto_open=not args.no_camera, game_connect=args.game_connect)
    root.mainloop()


if __name__ == '__main__':
    main()
