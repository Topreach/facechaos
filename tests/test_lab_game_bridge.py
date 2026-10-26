import json
import sys
import unittest
from pathlib import Path
from unittest.mock import Mock, patch

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / 'recognition'))
from lab_game_bridge import LabGameBridge


class LabGameBridgeTest(unittest.TestCase):
    def test_preview_and_avatar_are_optional_and_no_frame_enters_udp(self):
        packet = self.packet(preview={'port': 47778, 'token': 'a' * 32},
                             avatar={'blink_left': .8, 'jaw': .4, 'bad': .9, 'smile': float('nan')})
        self.assertEqual(packet['preview']['port'], 47778)
        self.assertEqual(packet['avatar'], {'blink_left': .8, 'jaw': .4})
        self.assertNotIn('image', packet)

    def setUp(self):
        self.socket = Mock()
        with patch('lab_game_bridge.socket.socket', return_value=self.socket):
            self.bridge = LabGameBridge()

    def packet(self, tracked=True, controls=None, events=None, meters=None, **kwargs):
        self.bridge.publish(tracked, controls or {'teeth': {}}, events or [], meters or {}, **kwargs)
        payload, target = self.socket.sendto.call_args.args
        self.assertEqual(target, ('127.0.0.1', 47777))
        return json.loads(payload)

    def test_teeth_transmits_and_advanced_controls_are_filtered(self):
        packet = self.packet(controls={'teeth': {}, 'tongue_out': {}}, events=['teeth', 'tongue_out'],
                             meters={'teeth': .9, 'tongue_out': 1})
        self.assertEqual(packet['enabled'], ['teeth'])
        self.assertEqual([e['reaction'] for e in packet['events']], ['teeth'])
        self.assertEqual(packet['meters'], {'teeth': .9})

    def test_testing_and_calibration_never_issue_actions(self):
        self.packet(events=['teeth'])
        packet = self.packet(events=['teeth'], busy=True)
        self.assertFalse(packet['calibrated'])
        self.assertEqual(packet['events'], [])

    def test_loss_stop_and_close_clear_input(self):
        self.packet(events=['teeth'])
        self.assertEqual(self.packet(False)['events'], [])
        self.bridge.close()
        self.assertTrue(self.bridge.closed)
        self.assertFalse(json.loads(self.socket.sendto.call_args.args[0])['calibrated'])
        self.socket.close.assert_called_once()

    def test_disabled_control_cannot_retransmit_stale_event(self):
        self.packet(events=['teeth'])
        packet = self.packet(controls={'smile': {}}, events=['smile'])
        self.assertEqual([e['reaction'] for e in packet['events']], ['smile'])


if __name__ == '__main__':
    unittest.main()
