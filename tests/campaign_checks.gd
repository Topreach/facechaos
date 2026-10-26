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
	var suffix := str(Time.get_ticks_usec())
	game.score_path = "user://campaign_test_scores_" + suffix + ".cfg"
	game.progress_path = "user://campaign_test_progress_" + suffix + ".cfg"
	root.add_child(game)
	game.set_process(false)
	game.set_physics_process(false)
	game.bridge.set_process(false)
	check(game.unlocked_course == 0, "New campaign unlocks only the first course")
	game.select_course(4)
	check(game.course_index == 0, "Locked courses cannot be selected")
	game.start_game()
	game.take_hit()
	check(game.hearts == 2 and game.running, "One hit costs one heart, not the whole run")
	game.take_hit()
	check(game.hearts == 2, "Recovery grace rejects repeated damage")
	game.invulnerable = 0
	game.take_hit()
	game.invulnerable = 0
	game.take_hit()
	check(not game.running and game.game_over and game.unlocked_course == 0, "Failure cannot unlock content")
	game.start_game()
	check(game.hearts == 3 and not game.completed and game.event_cursor == 0, "Retry resets course state")
	game.paused = true
	game.simulate_frame(.1)
	check(game.elapsed == 0 and game.event_cursor == 0, "Pause freezes course progression")
	game.paused = false
	for index in range(game.Campaign.COURSE_COUNT):
		game.select_course(index)
		game.start_game()
		var definition: Dictionary = game.course_data
		check(int(definition.stage) == index / 3 + 1 and int(definition.level) == index % 3 + 1, "Campaign stage numbering")
		var last_time := -1.0
		for cue in definition.events:
			check(float(cue.time) - last_time >= 3.0, "Authored cues must leave comfortable reaction spacing")
			check(float(cue.time) < float(definition.duration) - 4.5, "Final hazard clears before finish")
			last_time = cue.time
		# Exercise complete physics/collision paths, not just direct completion calls.
		for frame in range(4500):
			for hazard in game.obstacles:
				var distance: float = float(hazard.x) - game.PLAYER_X
				if distance > 0:
					match int(hazard.kind):
						0:
							if distance < 175:
								game.key_events[KEY_SPACE] = true
						1:
							if distance < 110:
								game.key_events[KEY_A] = true
						2:
							if distance < 130:
								game.key_events[KEY_S] = true
			game.simulate_frame(.01)
			if not game.running:
				break
		check(game.completed and not game.game_over, "Course %d must be finishable through ordinary controls" % index)
		check(game.hearts == 3, "Course %d must permit a no-damage completion" % index)
		check(game.unlocked_course == mini(8, index + 1), "Success unlocks exactly the next course")
		var finished_score: float = game.score
		game.finish_course()
		check(game.score == finished_score, "Completion bonus must be idempotent")
		if index == 0:
			game.next_course()
			check(game.course_index == 1 and game.running and game.resume_countdown == 3.0, "Next level starts the unlocked course with a safe countdown")
	game.unlocked_course = 0
	game.load_progress()
	check(game.unlocked_course == 8, "Unlocked campaign courses persist across reload")
	game.course_index = 8
	game.next_course()
	check(game.course_index == 8, "Final next action must not exceed available content")
	for path in [game.score_path, game.progress_path]:
		for extension in ["", ".bak", ".tmp", ".corrupt"]:
			if FileAccess.file_exists(path + extension):
				DirAccess.remove_absolute(ProjectSettings.globalize_path(path + extension))
	game.free()
	print("Campaign checks: %d failures; 9 full courses simulated" % failures)
	quit(1 if failures else 0)
