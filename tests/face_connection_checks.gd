extends SceneTree

var failures := 0

func check(condition: bool, message: String) -> void:
	if not condition:
		failures += 1
		push_error(message)

func send_fixture(game: Node, reaction: String) -> void:
	var output: Array = []
	var code := OS.execute(ProjectSettings.globalize_path("res://.venv/Scripts/python.exe"),
		[ProjectSettings.globalize_path("res://tests/send_lab_fixture.py"), str(game.bridge_port), reaction], output, true)
	check(code == 0, "Python production publisher must run: %s" % str(output))
	game.bridge._process(0)

func _initialize() -> void:
	call_deferred("run_checks")

func run_checks() -> void:
	var probe := PacketPeerUDP.new()
	check(probe.bind(0, "127.0.0.1") == OK, "Must acquire isolated loopback test port")
	var port := probe.get_local_port()
	probe.close()
	var game = load("res://scripts/game.gd").new()
	game.bridge_port = port
	root.add_child(game)
	game.set_process(false)
	game.set_physics_process(false)
	game.bridge.set_process(false)
	send_fixture(game, "mouth_open")
	game.simulate_frame(.016)
	check(game.practice_counts.mouth_open == 1 and not game.running, "Menu practice counts a deduplicated onset without starting gameplay")
	check(game.menu_status.text.begins_with("LIVE levels"), "Received facial levels must be visible before play")
	for reaction in ["mouth_open", "smile", "teeth", "brows_up"]:
		game.start_game()
		game.facial_mode = true
		send_fixture(game, reaction)
		check(game.bridge.ready_for_face(), "Lab packets must enable calibrated face mode")
		game.simulate_frame(.016)
		check(game.last_detection != "Waiting for a facial reaction", "Received reaction must produce visible detection feedback")
		match reaction:
			"mouth_open":
				check(game.velocity_y < 0, "Prepared jaw reaction must produce an actual jump")
			"smile":
				check(game.shield_time > 0, "Prepared closed-lip smile must activate shield")
			"teeth":
				check(game.attack_time > 0, "Prepared teeth reaction must activate chomp")
				check(game.shield_time == 0 and game.velocity_y == 0, "Simultaneous teeth wins over facial smile and jaw command")
			"brows_up":
				check(game.fly_time > 0, "Existing eye/brow proxy must still activate flight")
		check(game.key_events.is_empty(), "Connection checks must not use keyboard actions")
	send_fixture(game, "lost")
	game.simulate_frame(.35)
	check(game.recovery, "Tracking loss from the production publisher must pause face play")
	var frozen: float = game.elapsed
	game.simulate_frame(.1)
	check(game.elapsed == frozen, "Tracking recovery freezes course progression")
	game.free()
	print("Face connection: %d failures; Python detector -> UDP -> Godot abilities (no camera)" % failures)
	quit(1 if failures else 0)
