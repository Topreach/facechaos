"""Send prepared detector output through the production publisher; no camera."""
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / 'recognition'))
from lab_core import LabDetector, KEYS
from lab_game_bridge import LabGameBridge

port, target = int(sys.argv[1]), sys.argv[2]
detector = LabDetector()
detector.controls = {k: {'neutral': 0, 'active': 1, 'sensitivity': .55}
                     for k in ('mouth_open', 'brows_up', 'smile', 'teeth')}
bridge = LabGameBridge(port=port)
if target == 'lost':
    bridge.publish(False, detector.controls, [], {})
else:
    values = dict.fromkeys(KEYS, 0.0)
    for i in range(20):
        detector.update(values, i * .02)
    values[target] = .9
    if target == 'teeth':
        values['smile'] = .9
        values['mouth_open'] = .9
    for i in range(20, 40):
        events, meters = detector.update(values, i * .02)
        bridge.publish(True, detector.controls, events, meters)
# Do not send an unavailable shutdown packet until the receiving test has
# observed the event. Socket closure itself sends no UDP packet.
bridge.socket.close()
