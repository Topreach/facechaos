extends SceneTree
## Read-only runtime probes. Reports observations without accepting known gaps.

func _initialize() -> void:
	call_deferred("audit")

func audit() -> void:
	var game = load("res://scripts/game.gd").new()
	root.add_child(game)
	game.set_process(false)
	game.set_physics_process(false)
	game.bridge.set_process(false)
	game.key_events[KEY_SPACE] = true
	game.simulate_frame(.016)
	print("AUDIT_START_SHORTCUT: running=%s countdown=%.2f (expected: running, approximately 3s)" % [game.running, game.resume_countdown])
	game.facial_mode = false
	game.start_game()
	game.obstacles.append({"x": 260.0, "y": game.FLOOR_Y - 25.0, "radius": 25.0, "kind": 0})
	game.simulate_frame(.4)
	print("AUDIT_COLLISION_HITCH: hearts=%d hazards=%d (expected: 2 hearts, hit hazard removed)" % [game.hearts, game.obstacles.size()])
	game.start_game()
	game.attack_time = .3
	game.obstacles.append({"x": game.PLAYER_X - 35.0, "y": game.FLOOR_Y - 25.0, "radius": 25.0, "kind": 1})
	game.simulate_frame(.016)
	print("AUDIT_BACKWARD_CHOMP: remaining=%d score=%.2f (expected: no 35-point chomp reward)" % [game.obstacles.size(), game.score])
	game.free()
	quit()
