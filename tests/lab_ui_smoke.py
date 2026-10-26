"""Hidden Tk workflow check. No camera access, screenshots, or network connection."""
import sys
import tempfile
import tkinter as tk
from pathlib import Path
from unittest.mock import patch

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / 'recognition'))
from facial_lab import FacialLab
from lab_core import KEYS


def main():
    root = tk.Tk()
    root.withdraw()
    with patch('socket.socket', side_effect=AssertionError('Lab must not connect to a game')):
        app = FacialLab(root, auto_open=False)
        try:
            root.update_idletasks()
            assert len(app.tree.get_children()) == 14
            assert 'eyes_wide' not in app.tree.get_children()
            assert 'frown' not in app.tree.get_children()
            assert app.tree.item('brows_up', 'text') == 'Eye widening'
            assert app.cap is None and app.model is None
            assert app.game_bridge is None and not app.game_enabled.get()
            from unittest.mock import Mock
            with patch('lab_game_bridge.LabGameBridge') as bridge_factory:
                app.game_enabled.set(True)
                app.toggle_game_connection()
                connected = bridge_factory.return_value
                app.cap = Mock()
                app.send_game_frame(True, ['teeth'], {'teeth': .9})
                assert connected.publish.call_args.args[2] == ['teeth']
                assert connected.publish.call_args.kwargs['available']
                app.cap = None
                app.game_enabled.set(False)
                app.toggle_game_connection()
                connected.close.assert_called_once()
                assert app.game_bridge is None
            app.open_sound_practice()
            sound = app.sound_practice
            sound.window.withdraw()
            root.update_idletasks()
            assert sound.stream is None and not sound.devices
            assert 'not implemented' in sound.status.get()
            app.open_sound_practice()
            assert app.sound_practice is sound
            sound.close()
            assert sound.closed
            app.tree.selection_set('face_scrunch')
            app.select_section()
            app.values = dict.fromkeys(KEYS, 0.0)
            app.start_calibration()
            while app.run:
                run = app.run
                kind, duration, _prompt = run['stages'][run['index']]
                start = run['start']
                for i in range(1, int(duration / .03) + 2):
                    values = dict.fromkeys(KEYS, 0.0)
                    values['face_scrunch'] = .9 if kind == 'positive' else 0
                    app.task_tick(values, None, [], start + i * .03, .03)
            assert 'face_scrunch' in app.detector.controls, app.prompt.get()
            app.tree.selection_set('tongue_out')
            app.select_section()
            app.start_calibration()
            assert not any(stage[0] == 'negative' for stage in app.run['stages'])
            while app.run:
                run = app.run
                kind, duration, _prompt = run['stages'][run['index']]
                start = run['start']
                for i in range(1, int(duration / .03) + 2):
                    values = dict.fromkeys(KEYS, 0.0)
                    values['tongue_out'] = .9 if kind == 'positive' else 0
                    app.task_tick(values, None, [], start + i * .03, .03)
            assert 'tongue_out' in app.detector.controls, app.prompt.get()
            assert 'tongue_out' not in app.appearance
            import numpy as np
            app.tree.selection_set('cheek_puff')
            app.select_section()
            app.start_cheek_calibration()
            assert app.run['key'] == 'cheek_puff'
            while app.run:
                run = app.run
                kind, duration, _prompt = run['stages'][run['index']]
                start = run['start']
                for i in range(1, int(duration / .03) + 2):
                    descriptor = np.full(217, .9) if kind == 'positive' else np.full(217, .1)
                    app.task_tick(app.values, descriptor, [], start + i * .03, .03)
            assert 'cheek_puff' in app.detector.controls, app.prompt.get()
            assert 'cheek_puff' in app.appearance
            app.detector.controls['smile'] = dict(app.detector.controls['face_scrunch'])
            app.calibration_camera = 0
            with tempfile.TemporaryDirectory() as folder:
                path = str(Path(folder) / 'calibration.json')
                with patch('facial_lab.filedialog.asksaveasfilename', return_value=path):
                    app.save_calibration()
                assert Path(path).exists()
                app.detector.controls = {}
                app.appearance = {}
                # Satisfy the UI's connected-camera precondition without opening hardware.
                app.cap = object()
                try:
                    with patch('facial_lab.filedialog.askopenfilename', return_value=path):
                        app.load_calibration()
                finally:
                    app.cap = None
                assert 'cheek_puff' in app.appearance and 'face_scrunch' in app.detector.controls
                assert 'tongue_out' in app.detector.controls and 'tongue_out' not in app.appearance
            app.start_all_trials()
            assert app.run is not None
            while app.run:
                run = app.run
                if run['phase'] == 'rest':
                    app.task_tick(app.values, None, [], run['start'] + 2.01, .03)
                else:
                    app.task_tick(app.values, None, [run['key']], run['start'] + .1, .03)
                    app.task_tick(app.values, None, [], run['start'] + 3.01, .03)
            assert app.book.results['face_scrunch']['attempts'] == 5
            assert app.book.results['smile']['detected'] == 5
            app.start_neutral()
            app.task_tick(app.values, None, ['face_scrunch'], app.run['start'] + 2.1, .1)
            assert app.book.results['face_scrunch']['neutral_events'] == 1
            app.cancel()
            app.sensitivity.set(.7)
            app.change_sensitivity(None)
            assert app.book.neutral_seconds == 0
            assert all(r['attempts'] == 0 for r in app.book.results.values())
            app.start_advanced_calibration()
            assert app.run['key'] == 'teeth'
            assert app.calibration_queue == ['tongue_out', 'cheek_puff']
            app.cancel()
            assert app.run is None and not app.calibration_queue
            print('Lab UI: 14 rows; renamed eye control; calibration, trial, save/load workflows: PASSED')
        finally:
            app.close()


if __name__ == '__main__':
    main()
