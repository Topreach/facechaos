extends SceneTree

var failures := 0

func check(condition: bool, message: String) -> void:
	if not condition:
		failures += 1
		push_error(message)

func _initialize() -> void:
	call_deferred("run_checks")

func run_checks() -> void:
	var probe := PacketPeerUDP.new()
	check(probe.bind(0, "127.0.0.1") == OK, "Isolated test port available")
	var port := probe.get_local_port()
	probe.close()
	var game = load("res://scripts/game.gd").new()
	game.bridge_port = port
	root.add_child(game)
	game.set_process(false)
	game.set_physics_process(false)
	var invalid := {"version": 1, "session_id": "invalid-preview", "sequence": 0,
		"tracking": "tracked", "calibrated": true, "enabled": ["mouth_open"], "events": [],
		"preview": {"port": 70000, "token": "a".repeat(32)}}
	check(not game.bridge.accept_packet(invalid), "Invalid preview port is rejected")
	invalid.preview = {"port": 47778, "token": "https://example.com/remote"}
	check(not game.bridge.accept_packet(invalid), "Remote URL cannot be substituted for the preview token")
	invalid.erase("preview")
	invalid.avatar = {"jaw": 2.0}
	check(not game.bridge.accept_packet(invalid), "Invalid avatar measurement is rejected")
	game.start_game()
	game.facial_mode = true
	var pid := OS.create_process(ProjectSettings.globalize_path("res://.venv/Scripts/python.exe"),
		[ProjectSettings.globalize_path("res://tests/send_preview_fixture.py"), str(port)])
	check(pid > 0, "Synthetic local preview service starts")
	for i in range(80):
		game._process(.05)
		await create_timer(.05).timeout
		if game.camera_view.texture != null:
			break
	check(game.camera_view.texture != null, "Real loopback HTTP JPEG reaches a Godot texture")
	game.update_menu()
	check(game.visibility_button.visible, "Integrated play exposes its hide-camera control")
	check(float(game.bridge.avatar.get("blink_left", 0)) > .8, "Avatar blink signal accompanies preview without a control event")
	game.toggle_camera_visibility()
	check(game.camera_view.texture == null and not game.camera_view.enabled, "Hide camera immediately clears preview and stops requests")
	game.toggle_camera_visibility()
	check(game.camera_view.enabled, "Camera can be shown again")
	await create_timer(3.5).timeout
	game._process(.05)
	check(game.bridge.preview.is_empty(), "Source shutdown/disconnect clears preview metadata")
	check(game.camera_view.texture == null, "Disconnect cannot leave a frozen camera image")
	game.free()
	print("Camera connection: %d failures; synthetic Python JPEG -> local HTTP -> Godot, hide and disconnect" % failures)
	quit(1 if failures else 0)
