extends SceneTree

var failures := 0

func check(condition: bool, message: String) -> void:
	if not condition:
		failures += 1
		push_error(message)

func _initialize() -> void:
	call_deferred("run_checks")

func run_checks() -> void:
	var game = load("res://scripts/game.gd").new()
	game.score_path = "user://regression_scores_%d.cfg" % Time.get_ticks_usec()
	root.add_child(game)
	game.set_process(false)
	game.set_physics_process(false)
	game.bridge.set_process(false)
	check(game.menu_panel.visible, "Startup must show the camera and play menu")
	check(game.facial_lab_script().ends_with("/recognition/facial_lab.py"), "Full lab must launch the 14-section script")
	var lab_button_found := false
	for child in game.menu_panel.get_children():
		if child is Button and child.text == "Facial Lab - 14 Sections":
			lab_button_found = child.pressed.is_connected(game.open_facial_lab)
	check(lab_button_found, "Main menu must expose the full facial lab directly")
	check(game.face_play_button.disabled, "Face play must stay disabled before calibration")
	var packet := {"version": 1, "session_id": "regression-session", "sequence": 0,
		"tracking": "tracked", "calibrated": true, "enabled": ["mouth_open"],
		"events": [{"id": 1, "reaction": "mouth_open"}], "meters": {"mouth_open": 0.8}}
	check(game.bridge.accept_packet(packet), "Valid recognition packet must be accepted")
	game.update_menu()
	check(not game.face_play_button.disabled, "Calibrated tracking must enable face play")
	check(game.bridge.consume_reactions().has("mouth_open"), "Recognition packet must deliver action")
	packet.sequence = 1
	check(game.bridge.accept_packet(packet), "Repeated event in new packet must be valid")
	check(game.bridge.consume_reactions().is_empty(), "Repeated event must not trigger twice")
	check(not game.bridge.accept_packet(packet), "Stale sequence must be rejected")
	packet.sequence = 2
	packet.events = [{"id": 2, "reaction": "not-a-control"}]
	check(not game.bridge.accept_packet(packet), "Unknown reaction must be rejected")
	packet.events = []
	packet.calibrated = false
	check(game.bridge.accept_packet(packet) and not game.bridge.ready_for_face(), "Uncalibrated tracking must not enable controls")
	packet.sequence = 3
	packet.calibrated = true
	packet.tracking = "lost"
	check(game.bridge.accept_packet(packet) and not game.bridge.ready_for_face(), "Face loss must disable controls")
	packet.sequence = 4
	packet.tracking = "tracked"
	packet.events = [{"id": 3, "reaction": "mouth_open"}]
	check(game.bridge.accept_packet(packet), "Reacquired tracking accepts a fresh event")
	game.bridge.consume_reactions()
	packet.sequence = 5
	packet.session_id = "new-session"
	packet.events = [{"id": 1, "reaction": "mouth_open"}]
	check(game.bridge.accept_packet(packet), "Bridge restart resets event sequence")
	check(game.bridge.consume_reactions().has("mouth_open"), "New session event ID can restart at one")
	packet.sequence = 6
	packet.meters = {"mouth_open": 999.0}
	check(not game.bridge.accept_packet(packet), "Invalid meters must reject the entire packet")
	game.start_game()
	check(not game.menu_panel.visible, "Starting a round must hide setup menu")
	game.activate("shield")
	game.shield_time = 0.1
	game.activate("shield")
	check(is_equal_approx(game.shield_time, 0.1), "Cooldown must prevent shield refresh")
	game.activate("fly")
	game.player_y = 149.0
	game.simulate_frame(0.016)
	check(game.player_y >= 150.0, "Flight must remain on screen")
	game.paused = true
	var old_score: float = game.score
	game.bridge.pending["mouth_open"] = true
	game.simulate_frame(0.1)
	check(game.score == old_score, "Pause must freeze score")
	check(game.bridge.pending.is_empty(), "Paused reactions must be discarded")
	game.start_game()
	check(not game.paused and game.cooldowns["shield"] == 0.0, "Restart must clear pause and cooldowns")
	game.running = false
	game.bridge.pending["smile"] = true
	game.simulate_frame(0.1)
	check(game.bridge.pending.is_empty(), "Menu reactions must be discarded")
	game.start_game()
	game.facial_mode = true
	game.bridge.tracking = "lost"
	game.simulate_frame(0.31)
	check(game.recovery, "Face-mode tracking loss must suspend gameplay")
	var frozen_score: float = game.score
	game.simulate_frame(0.1)
	check(game.score == frozen_score, "Recovery must freeze gameplay")
	game.key_events[KEY_K] = true
	game.simulate_frame(0.1)
	check(not game.facial_mode and not game.recovery and game.resume_countdown > 0.0, "Keyboard fallback must start safe countdown")
	game.best = 0
	game.score = 123.0
	game.end_game()
	check(game.menu_panel.visible, "Results must restore the clickable play menu")
	game.best = 0
	game.load_best()
	check(game.best == 123, "Best score must survive save and reload")
	DirAccess.remove_absolute(ProjectSettings.globalize_path(game.score_path))
	if FileAccess.file_exists(game.score_path + ".bak"):
		DirAccess.remove_absolute(ProjectSettings.globalize_path(game.score_path + ".bak"))
	await create_timer(1.1).timeout
	game.bridge.last_packet_ms = 0
	game.bridge.pending["smile"] = true
	game.bridge._process(0.0)
	check(game.bridge.last_packet_ms == -1 and game.bridge.pending.is_empty(), "Disconnect must clear input")
	game.free()
	print("Gameplay checks: %d failures" % failures)
	quit(1 if failures else 0)
