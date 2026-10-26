extends SceneTree
## Render-only layout review. No camera/microphone, input automation or saves.

func _initialize() -> void:
	call_deferred("render_previews")

func snapshot(name: String) -> void:
	await process_frame
	await RenderingServer.frame_post_draw
	root.get_texture().get_image().save_png("res://dist/" + name + ".png")

func render_previews() -> void:
	var game = load("res://scripts/game.gd").new()
	root.add_child(game)
	game.set_process(false)
	game.set_physics_process(false)
	game.bridge.set_process(false)
	game.update_menu()
	await snapshot("campaign-menu")
	game.start_game()
	game.elapsed = 12
	game.event_cursor = 3
	game.spawn_course_obstacle(0)
	game.obstacles[0].x = 620
	game.banner_time = 0
	game.queue_redraw()
	await snapshot("campaign-meadow")
	game.course_index = 1
	game.course_data = game.Campaign.course(1)
	game.completed = true
	game.running = false
	game.unlocked_course = 2
	game.score = 650
	game.update_menu()
	game.queue_redraw()
	await snapshot("campaign-results")
	for index in [3, 6]:
		game.course_index = index
		game.start_game()
		game.elapsed = 10
		game.spawn_course_obstacle(int(game.course_data.kind))
		game.obstacles[0].x = 600
		if index == 6:
			game.obstacles[0].y = 350
		game.banner_time = 0
		game.queue_redraw()
		await snapshot("campaign-factory" if index == 3 else "campaign-stars")
	game.facial_mode = true
	game.bridge.last_packet_ms = Time.get_ticks_msec()
	game.bridge.tracking = "tracked"
	game.bridge.calibrated = true
	game.bridge.enabled = ["mouth_open", "smile", "teeth"]
	game.bridge.meters = {"mouth_open": .72, "smile": .15, "teeth": .05}
	game.last_detection = "Jaw drop detected"
	game.queue_redraw()
	await snapshot("campaign-face-feedback")
	# Deliberately synthetic face-like image for layout review, never a camera read.
	var image := Image.create(640, 480, false, Image.FORMAT_RGB8)
	image.fill(Color("6d9299"))
	for y in range(480):
		for x in range(640):
			var face := Vector2((x - 320.0) / 120.0, (y - 250.0) / 170.0)
			if face.length_squared() < 1:
				image.set_pixel(x, y, Color("d6b59c"))
			if (Vector2(x, y) - Vector2(275, 220)).length_squared() < 100 or (Vector2(x, y) - Vector2(365, 220)).length_squared() < 100:
				image.set_pixel(x, y, Color("24323b"))
			if absf(x - 320) < 35 and absf(y - 325) < 8:
				image.set_pixel(x, y, Color("24323b"))
	game.camera_view.set_process(false)
	game.camera_view.texture = ImageTexture.create_from_image(image)
	game.input_feedback = "LAYOUT DEMO: synthetic camera image, not a live webcam"
	game.update_menu()
	game.queue_redraw()
	await snapshot("campaign-integrated-camera")
	game.free()
	quit()
