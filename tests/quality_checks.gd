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
	var prefix := "user://quality_%d" % Time.get_ticks_usec()
	game.score_path = prefix + "_scores.cfg"
	game.progress_path = prefix + "_progress.cfg"
	root.add_child(game)
	game.set_process(false)
	game.set_physics_process(false)
	game.bridge.set_process(false)
	game.key_events[KEY_SPACE] = true
	game.simulate_frame(.016)
	check(game.running and game.resume_countdown > 2.9 and game.elapsed == 0, "Menu shortcut must use countdown without advancing the course")
	game.key_events[KEY_ESCAPE] = true
	game.simulate_frame(.016)
	var frozen: float = game.resume_countdown
	game.simulate_frame(.4)
	check(game.paused and game.resume_countdown == frozen, "Pause must freeze countdown")
	game.key_events[KEY_ESCAPE] = true
	game.simulate_frame(.016)
	check(not game.paused and game.resume_countdown > 2.9, "Resume must provide a fresh safe countdown")
	game.key_events[KEY_R] = true
	game.simulate_frame(.016)
	check(game.elapsed == 0 and game.resume_countdown > 2.9, "Keyboard retry must provide countdown")
	for delta in [.1, .4, .9]:
		game.start_game()
		game.obstacles.append({"x": 240.0 if delta < .2 else 260.0, "y": 417.0, "radius": 25.0, "kind": 0})
		game.simulate_frame(delta)
		check(game.hearts == 2, "A crossing hazard must hit exactly once during a %.1fs simulated hitch" % delta)
	for side in [-1, 1]:
		game.start_game()
		game.attack_time = .3
		game.obstacles.append({"x": game.PLAYER_X + side * 35.0, "y": 417.0, "radius": 25.0, "kind": 1})
		game.simulate_frame(.016)
		check((game.score >= 35) == (side == 1), "Chomp must only reward a forward biscuit")
	# Compare equal real-time durations across render/input sample rates.
	var heights: Array[float] = []
	for rate in [30, 60, 120]:
		game.start_game()
		game.activate("jump")
		for frame in range(rate / 2):
			game.simulate_frame(1.0 / rate)
		heights.append(game.player_y)
	check(absf(heights[0] - heights[2]) < .1 and absf(heights[1] - heights[2]) < .1, "Jump arc must match across 30/60/120 fps samples")
	game.unlocked_course = 2
	game.save_progress()
	game.unlocked_course = 3
	game.save_progress()
	check(FileAccess.file_exists(game.progress_path + ".bak"), "Save updates must preserve previous valid backup")
	var damaged := FileAccess.open(game.progress_path, FileAccess.WRITE)
	damaged.store_string("not a valid config [")
	damaged.close()
	game.unlocked_course = 0
	game.load_progress()
	check(game.unlocked_course == 2 and not game.save_notice.is_empty(), "Corrupt primary must recover backup and tell the player")
	game.unlocked_course = 4
	game.save_progress()
	check(FileAccess.file_exists(game.progress_path + ".corrupt"), "Saving after recovery must retain damaged original")
	game.unlocked_course = 0
	game.load_progress()
	check(game.unlocked_course == 4, "Recovered save can be safely updated")
	for path in [game.score_path, game.progress_path]:
		for suffix in ["", ".bak", ".tmp", ".corrupt"]:
			if FileAccess.file_exists(path + suffix):
				DirAccess.remove_absolute(ProjectSettings.globalize_path(path + suffix))
	game.free()
	print("Quality checks: %d failures; transitions, frame hitches, attack direction, save recovery" % failures)
	quit(1 if failures else 0)
