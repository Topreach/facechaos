extends Node2D

const WIDTH := 960.0
const HEIGHT := 540.0
const FLOOR_Y := 442.0
const PLAYER_X := 180.0
const GRAVITY := 1250.0
const JUMP_SPEED := -535.0
const MOVE_SPEED := 330.0
const Campaign = preload("res://scripts/campaign.gd")
const SafeStore = preload("res://scripts/safe_store.gd")

var course_index := 0
var unlocked_course := 0
var course_data: Dictionary = Campaign.course(0)
var event_cursor := 0
var hearts := 3
var completed := false
var progress_path := "user://campaign.cfg"
var course_picker: OptionButton
var next_button: Button
var course_label: Label
var save_notice := ""
var save_label: Label
var cue_style := StyleBoxFlat.new()
var practice_counts := {"mouth_open": 0, "smile": 0, "teeth": 0}
var practice_session := ""
var last_detection := "Waiting for a facial reaction"

var player_y := FLOOR_Y - 36.0
var velocity_y := 0.0
var obstacles: Array[Dictionary] = []
var score := 0.0
var best := 0
var running := false
var game_over := false
var shield_time := 0.0
var fly_time := 0.0
var attack_time := 0.0
var invulnerable := 0.0
var elapsed := 0.0
var banner := "PRESS SPACE TO START"
var banner_time := 0.0
var font: Font
var bridge: Node
var bridge_port := 47777
var key_events: Dictionary = {}
var cooldowns := {"jump": 0.0, "fly": 0.0, "shield": 0.0, "attack": 0.0}
var paused := false
var score_path := "user://scores.cfg"
var facial_mode := false
var face_loss_time := 0.0
var recovery := false
var resume_countdown := 0.0
var input_feedback := "Keyboard mode - F enables calibrated face controls"
var camera_pid := -1
var lab_pid := -1
var face_stable_time := 0.0
var menu_panel: Panel
var menu_status: Label
var face_play_button: Button
var menu_feedback: Label
var camera_button: Button
var pause_button: Button
var camera_view: Node
var show_camera := true
var visibility_button: Button

func make_label(parent: Node, text: String, position: Vector2, size: Vector2, font_size: int) -> Label:
	var label := Label.new()
	label.text = text
	label.position = position
	label.size = size
	label.add_theme_font_size_override("font_size", font_size)
	parent.add_child(label)
	return label

func make_button(parent: Node, text: String, position: Vector2, size: Vector2, callback: Callable) -> Button:
	var button := Button.new()
	button.text = text
	button.position = position
	button.size = size
	button.add_theme_font_size_override("font_size", 19)
	button.pressed.connect(callback)
	parent.add_child(button)
	return button

func build_menu() -> void:
	var layer := CanvasLayer.new()
	add_child(layer)
	menu_panel = Panel.new()
	menu_panel.position = Vector2(80, 28)
	menu_panel.size = Vector2(800, 414)
	var style := StyleBoxFlat.new()
	style.bg_color = Color("101d35")
	style.border_color = Color("48aa8b")
	style.set_border_width_all(2)
	style.set_corner_radius_all(18)
	menu_panel.add_theme_stylebox_override("panel", style)
	layer.add_child(menu_panel)
	make_label(menu_panel, "FACE CHAOS", Vector2(30, 16), Vector2(730, 50), 38)
	make_label(menu_panel, "EXPRESSION ESCAPE  /  Campaign prototype", Vector2(32, 69), Vector2(730, 30), 21)
	course_picker = OptionButton.new()
	course_picker.position = Vector2(32, 111)
	course_picker.size = Vector2(500, 34)
	for index in range(Campaign.COURSE_COUNT):
		var data := Campaign.course(index)
		course_picker.add_item("%d-%d  %s" % [data.stage, data.level, data.title])
	course_picker.item_selected.connect(select_course)
	menu_panel.add_child(course_picker)
	next_button = make_button(menu_panel, "Next level", Vector2(550, 111), Vector2(218, 34), next_course)
	make_button(menu_panel, "Facial Lab - 14 Sections", Vector2(32, 155), Vector2(350, 48), open_facial_lab)
	make_button(menu_panel, "Connect Facial Controls", Vector2(405, 155), Vector2(363, 48), open_camera_setup)
	menu_status = make_label(menu_panel, "", Vector2(32, 218), Vector2(735, 28), 17)
	course_label = make_label(menu_panel, "", Vector2(32, 254), Vector2(735, 26), 18)
	face_play_button = make_button(menu_panel, "Play with Face + Keyboard", Vector2(32, 292), Vector2(350, 48), begin_face_run)
	make_button(menu_panel, "Play with Keyboard", Vector2(405, 292), Vector2(363, 48), begin_keyboard_run)
	menu_feedback = make_label(menu_panel, "", Vector2(32, 348), Vector2(735, 50), 15)
	menu_feedback.autowrap_mode = TextServer.AUTOWRAP_WORD_SMART
	save_label = make_label(menu_panel, "", Vector2(32, 394), Vector2(735, 18), 12)
	camera_button = make_button(layer, "Face Lab", Vector2(710, 20), Vector2(105, 36), open_facial_lab)
	pause_button = make_button(layer, "Pause", Vector2(825, 20), Vector2(105, 36), toggle_pause)
	visibility_button = make_button(layer, "Hide camera", Vector2(570, 20), Vector2(130, 36), toggle_camera_visibility)
	update_menu()

func select_course(index: int) -> void:
	if index < 0 or index > unlocked_course or index >= Campaign.COURSE_COUNT:
		return
	course_index = index
	course_data = Campaign.course(index)
	completed = false
	game_over = false
	input_feedback = course_data.mission
	update_menu()
	queue_redraw()

func next_course() -> void:
	if completed and course_index + 1 < Campaign.COURSE_COUNT and course_index + 1 <= unlocked_course:
		select_course(course_index + 1)
		if facial_mode:
			begin_face_run()
		else:
			begin_keyboard_run()

func load_progress() -> void:
	var loaded := SafeStore.load_config(progress_path, valid_progress)
	if not loaded.notice.is_empty():
		save_notice = loaded.notice
	if loaded.config != null:
		unlocked_course = loaded.config.get_value("campaign", "unlocked")

func valid_progress(config: ConfigFile) -> bool:
	if not config.has_section_key("campaign", "unlocked"):
		return false
	var value = config.get_value("campaign", "unlocked", null)
	return value is int and value >= 0 and value < Campaign.COURSE_COUNT

func valid_scores(config: ConfigFile) -> bool:
	if not config.has_section_key("scores", "best"):
		return false
	var value = config.get_value("scores", "best", null)
	return value is int and value >= 0 and value <= 2147483647

func save_progress() -> void:
	var config := ConfigFile.new()
	config.set_value("campaign", "unlocked", unlocked_course)
	if SafeStore.save_config(config, progress_path, valid_progress) != OK:
		save_notice = "Progress could not be saved. Check available disk space and folder access."

func finish_course() -> void:
	if not running:
		return
	completed = true
	unlocked_course = maxi(unlocked_course, mini(Campaign.COURSE_COUNT - 1, course_index + 1))
	save_progress()
	score += hearts * 100.0
	end_game()
	game_over = false
	update_menu()

func begin_face_run() -> void:
	if not bridge.ready_for_face():
		input_feedback = "Open camera setup and calibrate first"
		return
	facial_mode = true
	start_game()
	resume_countdown = 3.0

func begin_keyboard_run() -> void:
	facial_mode = false
	start_game()
	resume_countdown = 3.0

func toggle_pause() -> void:
	if running:
		paused = not paused
		key_events.clear()
		bridge.consume_reactions()
		if not paused:
			resume_countdown = 3.0

func update_menu() -> void:
	if menu_panel == null:
		return
	menu_panel.visible = not running
	camera_button.visible = running
	pause_button.visible = running
	visibility_button.visible = running and facial_mode
	visibility_button.text = "Hide camera" if show_camera else "Show camera"
	pause_button.text = "Resume" if paused else "Pause"
	pause_button.disabled = recovery
	face_play_button.disabled = not bridge.ready_for_face()
	menu_status.text = "Camera: " + bridge.status
	menu_feedback.text = input_feedback
	if bridge.ready_for_face():
		menu_status.text = "LIVE levels   Jaw %.2f   Smile %.2f   Teeth %.2f" % [bridge.meters.get("mouth_open", 0.0), bridge.meters.get("smile", 0.0), bridge.meters.get("teeth", 0.0)]
		menu_feedback.text = "Practice detections: Jump %d   Shield %d   Chomp %d\nReturn to neutral between faces. These counts do not measure accuracy." % [practice_counts.mouth_open, practice_counts.smile, practice_counts.teeth]
	save_label.text = save_notice
	for index in range(Campaign.COURSE_COUNT):
		course_picker.set_item_disabled(index, index > unlocked_course)
	course_picker.select(course_index)
	course_label.text = "%d-%d %s | %s" % [course_data.stage, course_data.level, course_data.lesson, course_data.action]
	next_button.visible = completed and course_index < Campaign.COURSE_COUNT - 1
	if completed:
		menu_feedback.text = ("INTRO CAMPAIGN COMPLETE!" if course_index == Campaign.COURSE_COUNT - 1 else "LEVEL COMPLETE! Next level unlocked.") + "  Hearts: %d/3 | Score: %d\nReplay, choose an unlocked level, or continue. Sound stages are planned." % [hearts, int(score)]
	if game_over:
		menu_feedback.text = "OUT OF HEARTS - Score %d | Best %d. Retry this level; your unlocks are safe." % [int(score), best]

func facial_lab_script() -> String:
	return ProjectSettings.globalize_path("res://recognition/facial_lab.py")

func open_facial_lab(connect_game: bool = false) -> void:
	if lab_pid > 0 and OS.is_process_running(lab_pid):
		input_feedback = "Facial lab is open. Enable Send controls to game in that window." if connect_game else "Facial lab is already open - switch to its window"
		return
	if (camera_pid > 0 and OS.is_process_running(camera_pid)) or bridge.last_packet_ms >= 0:
		input_feedback = "Close the 3-control camera preview first, then open Facial Lab"
		return
	var executable := ProjectSettings.globalize_path("res://.venv/Scripts/python.exe")
	if not FileAccess.file_exists(executable):
		input_feedback = "Run recognition/setup.ps1 first (see recognition/README.md)"
		return
	var arguments := [facial_lab_script()]
	if connect_game:
		arguments.append("--game-connect")
	lab_pid = OS.create_process(executable, arguments)
	input_feedback = "Opening facial controls - calibrate, then return here to play" if lab_pid > 0 else "Could not open facial lab"
	if running and not recovery:
		paused = true
	update_menu()

func open_camera_setup() -> void:
	open_facial_lab(true)

func _unhandled_key_input(event: InputEvent) -> void:
	if event is InputEventKey and event.pressed and not event.echo:
		key_events[event.keycode] = true

func load_best() -> void:
	var loaded := SafeStore.load_config(score_path, valid_scores)
	if not loaded.notice.is_empty():
		save_notice = loaded.notice
	if loaded.config != null:
		best = loaded.config.get_value("scores", "best")

func activate(action: String) -> void:
	if float(cooldowns[action]) > 0.0:
		input_feedback = action + ": cooling down"
		return
	match action:
		"jump":
			if player_y < FLOOR_Y - 37.0:
				input_feedback = "jump: land first"
				return
			jump()
			cooldowns[action] = 0.35
		"fly":
			fly_time = 0.45
			cooldowns[action] = 1.8
		"shield":
			shield_time = 0.65
			cooldowns[action] = 2.5
		"attack":
			attack_time = 0.30
			cooldowns[action] = 1.2
	input_feedback = action + ": accepted"
	show_banner({"jump": "BOING!", "fly": "UP, UP!", "shield": "BUBBLE UP!", "attack": "CRUNCH!"}[action])

func _ready() -> void:
	load_best()
	load_progress()
	font = ThemeDB.fallback_font
	cue_style.bg_color = Color("101d35")
	cue_style.set_corner_radius_all(6)
	bridge = load("res://scripts/reaction_bridge.gd").new()
	bridge.listen_port = bridge_port
	add_child(bridge)
	camera_view = load("res://scripts/camera_view.gd").new()
	add_child(camera_view)
	build_menu()
	queue_redraw()

func _process(_delta: float) -> void:
	camera_view.configure(bridge.preview, running and facial_mode and show_camera)
	update_menu()
	queue_redraw()

func _physics_process(delta: float) -> void:
	simulate_frame(delta)

func simulate_frame(delta: float) -> void:
	# Fixed physics ticks in production; bounded substeps also protect recovery
	# and explicit simulations from skipping an overlap during a long update.
	if not is_finite(delta) or delta <= 0.0:
		return
	var remaining := minf(delta, 1.0)
	while remaining > .000001:
		var step := minf(remaining, 1.0 / 120.0)
		step_game(step)
		remaining -= step
	update_menu()

func step_game(delta: float) -> void:
	if camera_pid > 0 and not OS.is_process_running(camera_pid):
		camera_pid = -1
		input_feedback = "Camera window closed. Open Camera Setup to try again."
	var keys := key_events.duplicate()
	key_events.clear()
	var reactions: Dictionary = bridge.consume_reactions()
	if practice_session != bridge.session_id:
		practice_session = bridge.session_id
		practice_counts = {"mouth_open": 0, "smile": 0, "teeth": 0}
		last_detection = "Waiting for a facial reaction"
	if bridge.ready_for_face():
		var observed := reactions.duplicate()
		if observed.has("teeth"):
			observed.erase("mouth_open")
			observed.erase("smile")
		for reaction in observed:
			last_detection = {"mouth_open": "Jaw drop detected", "brows_up": "Eye/brow proxy detected", "smile": "Closed-lip smile detected", "teeth": "Teeth candidate detected"}.get(reaction, reaction)
			if not running and practice_counts.has(reaction):
				practice_counts[reaction] += 1
	if keys.has(KEY_C):
		open_facial_lab()
	if keys.has(KEY_K):
		facial_mode = false
		reactions.clear()
		if recovery:
			recovery = false
			resume_countdown = 3.0
		input_feedback = "Keyboard mode"
	if keys.has(KEY_F):
		if bridge.ready_for_face():
			facial_mode = true
			reactions.clear()
			input_feedback = "Face mode: jaw jumps, smile shields, teeth chomp. Keyboard also works."
		else:
			input_feedback = "Start camera setup and calibrate before enabling face mode"
	if not facial_mode:
		reactions.clear()
	if running and facial_mode:
		if not bridge.ready_for_face():
			face_stable_time = 0.0
			face_loss_time += delta
			if not paused and (face_loss_time >= 0.3 or bridge.tracking == "unavailable"):
				recovery = true
				resume_countdown = 0.0
		else:
			face_loss_time = 0.0
			face_stable_time += delta
	if recovery:
		if keys.has(KEY_ENTER) and bridge.ready_for_face() and face_stable_time >= 0.5:
			recovery = false
			resume_countdown = 3.0
		queue_redraw()
		return
	if running and keys.has(KEY_ESCAPE):
		toggle_pause()
	if running and keys.has(KEY_R):
		start_game()
		resume_countdown = 3.0
		queue_redraw()
		return
	if paused:
		queue_redraw()
		return
	if resume_countdown > 0.0:
		resume_countdown = maxf(0.0, resume_countdown - delta)
		return
	if not running:
		if keys.has(KEY_SPACE) or keys.has(KEY_R):
			if facial_mode:
				begin_face_run()
			else:
				begin_keyboard_run()
		queue_redraw()
		return
	for action in cooldowns:
		cooldowns[action] = maxf(0.0, float(cooldowns[action]) - delta)
	if keys.has(KEY_SPACE) or (reactions.get("mouth_open", false) and not reactions.get("teeth", false)):
		activate("jump")
	elif keys.has(KEY_UP) or reactions.get("brows_up", false):
		activate("fly")
	if keys.has(KEY_A) or reactions.get("teeth", false):
		activate("attack")
	elif keys.has(KEY_S) or reactions.get("smile", false):
		activate("shield")
	shield_time = maxf(0.0, shield_time - delta)
	fly_time = maxf(0.0, fly_time - delta)
	attack_time = maxf(0.0, attack_time - delta)
	invulnerable = maxf(0.0, invulnerable - delta)
	banner_time = maxf(0.0, banner_time - delta)
	elapsed += delta
	if fly_time > 0.0:
		velocity_y = minf(velocity_y, -180.0)
	velocity_y += GRAVITY * delta
	player_y += velocity_y * delta
	if player_y < 150.0:
		player_y = 150.0
		velocity_y = maxf(0.0, velocity_y)
	if player_y >= FLOOR_Y - 36.0:
		player_y = FLOOR_Y - 36.0
		velocity_y = 0.0
	var schedule: Array = course_data.events
	while event_cursor < schedule.size() and elapsed >= float(schedule[event_cursor].time):
		spawn_course_obstacle(int(schedule[event_cursor].kind))
		event_cursor += 1
	for i in range(obstacles.size() - 1, -1, -1):
		var o := obstacles[i]
		o["x"] -= MOVE_SPEED * delta
		if int(o.kind) == 2:
			o["y"] = FLOOR_Y - float(o.radius) - maxf(0.0, float(o.x) - PLAYER_X - 50.0) * .18
		if o["x"] < -90.0:
			obstacles.remove_at(i)
			continue
		var dx: float = absf(float(o["x"]) - PLAYER_X)
		var dy: float = absf(float(o["y"]) - player_y)
		if dx < float(o["radius"]) + 24.0 and dy < float(o["radius"]) + 26.0:
			if attack_time > 0.0 and int(o.kind) == 1 and float(o.x) >= PLAYER_X:
				score += 35.0
				obstacles.remove_at(i)
				show_banner("SUPER CHOMP! +35")
			elif shield_time > 0.0 and int(o.kind) == 2:
				shield_time = 0.0
				score += 15.0
				obstacles.remove_at(i)
				show_banner("SMILE SHIELD! +15")
			elif invulnerable <= 0.0:
				obstacles.remove_at(i)
				take_hit()
				if not running:
					break
	if running:
		score += delta * 12.0
		if elapsed >= float(course_data.duration):
			finish_course()
	queue_redraw()

func start_game() -> void:
	recovery = false
	face_loss_time = 0.0
	face_stable_time = 0.0
	resume_countdown = 0.0
	paused = false
	key_events.clear()
	bridge.consume_reactions()
	for action in cooldowns:
		cooldowns[action] = 0.0
	running = true
	game_over = false
	completed = false
	hearts = 3
	event_cursor = 0
	course_data = Campaign.course(course_index)
	player_y = FLOOR_Y - 36.0
	velocity_y = 0.0
	obstacles.clear()
	score = 0.0
	elapsed = 0.0
	shield_time = 0.0
	fly_time = 0.0
	attack_time = 0.0
	invulnerable = 0.0
	input_feedback = course_data.mission
	show_banner("%s: %s" % [course_data.lesson, course_data.action])
	update_menu()

func jump() -> void:
	if player_y >= FLOOR_Y - 37.0:
		velocity_y = JUMP_SPEED

func spawn_obstacle() -> void:
	spawn_course_obstacle(int(course_data.kind))

func spawn_course_obstacle(kind: int) -> void:
	var radius := 25.0
	obstacles.append({"x": WIDTH + 40.0, "y": FLOOR_Y - radius, "radius": radius, "kind": kind})
	show_banner(["JUMP!  SPACE / jaw drop", "BISCUIT!  A to chomp", "STAR!  S / smile to shield"][kind])

func take_hit() -> void:
	if invulnerable > 0.0 or not running:
		return
	hearts -= 1
	invulnerable = 1.5
	show_banner("OUCH! %d hearts left" % hearts)
	if hearts <= 0:
		end_game()

func end_game() -> void:
	running = false
	game_over = true
	best = maxi(best, int(score))
	var config := ConfigFile.new()
	config.set_value("scores", "best", best)
	if SafeStore.save_config(config, score_path, valid_scores) != OK:
		save_notice = "Best score could not be saved. Check disk space and folder access."
	banner = "GAME OVER! PRESS SPACE OR R TO RETRY"
	update_menu()

func show_banner(value: String) -> void:
	banner = value
	banner_time = 1.8

func _draw() -> void:
	var camera_live: bool = running and facial_mode and show_camera and camera_view != null and camera_view.texture != null
	if camera_live:
		var size: Vector2 = camera_view.texture.get_size()
		var scale := maxf(WIDTH / size.x, HEIGHT / size.y)
		var source_size := Vector2(WIDTH, HEIGHT) / scale
		draw_texture_rect_region(camera_view.texture, Rect2(0, 0, WIDTH, HEIGHT), Rect2((size - source_size) / 2, source_size))
		draw_rect(Rect2(0, 0, WIDTH, HEIGHT), Color(0.02, .04, .08, .3))
	else:
		draw_rect(Rect2(0, 0, WIDTH, HEIGHT), Color(course_data.sky))
	var scenery_alpha := .2 if camera_live else 1.0
	draw_rect(Rect2(0, 345, WIDTH, 100), Color(Color("253d60"), scenery_alpha))
	for i in range(7):
		var hill_x := float(i * 200) - fmod(elapsed * 24.0, 200.0)
		if int(course_data.stage) == 2:
			draw_rect(Rect2(hill_x, 290, 100, 155), Color(Color("66516e"), scenery_alpha))
			for row in range(3):
				draw_rect(Rect2(hill_x + 20, 307 + row * 38, 20, 20), Color(Color("e8ba7a"), scenery_alpha))
		else:
			draw_circle(Vector2(hill_x, 440), 115, Color(Color("304c63"), scenery_alpha))
	for i in range(12):
		var x := float(i * 110) - fmod(elapsed * 36.0, 110.0)
		draw_circle(Vector2(x + 40, 100 + 20 * sin(float(i))), 3, Color("e2ecff"))
	draw_rect(Rect2(0, FLOOR_Y, WIDTH, HEIGHT - FLOOR_Y), Color(course_data.ground))
	draw_line(Vector2(0, FLOOR_Y), Vector2(WIDTH, FLOOR_Y), Color("c2ffe0"), 4)
	for i in range(11):
		var x := float(i * 105) - fmod(elapsed * 105.0, 105.0)
		draw_line(Vector2(x, FLOOR_Y + 22), Vector2(x + 46, FLOOR_Y + 22), Color("b8e6ce"), 4)
	for o in obstacles:
		var p := Vector2(float(o["x"]), float(o["y"]))
		var rad: float = o["radius"]
		var palette := [Color("ffb648"), Color("fb6c80"), Color("bca2ff")]
		if p.x > PLAYER_X + 50 and p.x < WIDTH - 30 and running:
			var cue: String = ["JUMP", "CHOMP", "SHIELD"][int(o.kind)]
			draw_style_box(cue_style, Rect2(p.x - 38, p.y - 67, 76, 24))
			draw_string(font, Vector2(p.x - 32, p.y - 49), cue, HORIZONTAL_ALIGNMENT_CENTER, 64, 14, Color.WHITE)
		if int(o.kind) == 0:
			draw_colored_polygon(PackedVector2Array([p + Vector2(-rad, rad), p + Vector2(0, -rad), p + Vector2(rad, rad)]), palette[0])
		elif int(o.kind) == 1:
			draw_circle(p, rad, Color("e6ad68"))
			draw_arc(p, rad - 4, 0, TAU, 24, Color("9e653f"), 2)
			for offset in [Vector2(-10, -10), Vector2(10, 8), Vector2(-8, 12)]:
				draw_circle(p + offset, 4, Color("704639"))
		else:
			var star := PackedVector2Array()
			for vertex in range(10):
				var angle := -PI / 2 + vertex * PI / 5
				star.append(p + Vector2(cos(angle), sin(angle)) * (rad if vertex % 2 == 0 else rad * .5))
			draw_colored_polygon(star, Color("bca2ff"))
		draw_circle(p + Vector2(-rad * 0.25, -5), 4, Color("192339"))
		draw_circle(p + Vector2(rad * 0.25, -5), 4, Color("192339"))
		draw_arc(p + Vector2(0, 5), rad * 0.35, 0, PI, 12, Color("192339"), 3)
	var center := Vector2(PLAYER_X, player_y)
	if shield_time > 0.0:
		draw_arc(center, 47, 0, TAU, 36, Color("5eead4"), 6)
	if attack_time > 0.0:
		draw_arc(center, 45, 0, TAU, 36, Color("ffbb55"), 5)
	draw_circle(center, 34, Color("fff4b0") if invulnerable > 0.0 and int(elapsed * 10) % 2 == 0 else Color("f7d64b"))
	for side in [-1, 1]:
		var eye := center + Vector2(side * 12, -8)
		var closed := float(bridge.avatar.get("blink_left" if side == -1 else "blink_right", 0.0)) if facial_mode else 0.0
		if closed > .55:
			draw_line(eye - Vector2(6, 0), eye + Vector2(6, 0), Color("17233e"), 3)
		else:
			draw_circle(eye, 6, Color("17233e"))
	if attack_time > 0.0:
		draw_circle(center + Vector2(0, 10), 17, Color("17233e"))
		draw_rect(Rect2(center + Vector2(-11, -2), Vector2(22, 8)), Color.WHITE)
	elif facial_mode and bridge.ready_for_face() and float(bridge.avatar.get("jaw", bridge.meters.get("mouth_open", 0.0))) > .2:
		draw_circle(center + Vector2(0, 10), 6 + 11 * float(bridge.avatar.get("jaw", bridge.meters.get("mouth_open", 0.0))), Color("17233e"))
	else:
		draw_arc(center + Vector2(0, 5), 14, 0, PI, 16, Color("17233e"), 4)
	var remaining := maxf(0.0, float(course_data.duration) - elapsed)
	if running and remaining < 2.5:
		var gate_x := PLAYER_X + remaining * MOVE_SPEED
		draw_line(Vector2(gate_x, FLOOR_Y), Vector2(gate_x, FLOOR_Y - 170), Color.WHITE, 5)
		draw_rect(Rect2(gate_x, FLOOR_Y - 170, 100, 32), Color("48aa8b"))
		draw_string(font, Vector2(gate_x + 10, FLOOR_Y - 147), "FINISH", HORIZONTAL_ALIGNMENT_LEFT, -1, 20, Color.WHITE)
	if not running and menu_panel != null:
		return
	if camera_live:
		draw_rect(Rect2(12, 10, 685, 200), Color(0.03, .06, .12, .78))
	draw_string(font, Vector2(24, 45), "EXPRESSION ESCAPE", HORIZONTAL_ALIGNMENT_LEFT, -1, 28, Color("ffffff"))
	draw_string(font, Vector2(24, 80), "Score: %d     Best: %d" % [int(score), best], HORIZONTAL_ALIGNMENT_LEFT, -1, 19, Color("d8e8ff"))
	draw_string(font, Vector2(24, 516), "SPACE: jump   UP: fly   S: shield   A: chomp   ESC: pause   R: retry", HORIZONTAL_ALIGNMENT_LEFT, -1, 16, Color("ffffff"))
	draw_string(font, Vector2(24, 109), course_data.title, HORIZONTAL_ALIGNMENT_LEFT, -1, 22, Color("fff3b0"))
	draw_string(font, Vector2(24, 135), course_data.action, HORIZONTAL_ALIGNMENT_LEFT, -1, 17, Color("d8e8ff"))
	draw_string(font, Vector2(24, 160), input_feedback, HORIZONTAL_ALIGNMENT_LEFT, 900, 14, Color("c5d9ed"))
	if running:
		draw_string(font, Vector2(24, 188), "%d-%d  %s  |  HEARTS %d/3  |  FINISH %.0fs" % [course_data.stage, course_data.level, course_data.name, hearts, remaining], HORIZONTAL_ALIGNMENT_LEFT, -1, 18, Color.WHITE)
		draw_rect(Rect2(24, 199, 660, 6), Color("0c1528"))
		draw_rect(Rect2(24, 199, 660 * clampf(elapsed / float(course_data.duration), 0, 1), 6), Color("5eead4"))
	if facial_mode:
		draw_face_feedback()
		if show_camera:
			draw_style_box(cue_style, Rect2(710, 218, 220, 26))
			draw_string(font, Vector2(710, 235), "CAMERA LIVE - LOCAL" if camera_live else "Camera preview unavailable", HORIZONTAL_ALIGNMENT_LEFT, -1, 13, Color.WHITE)
	draw_string(font, Vector2(24, 488), "%s  |  Chomp %s  |  Shield %s" % ["Face + keyboard" if facial_mode else "Keyboard", ability_status("attack"), ability_status("shield")], HORIZONTAL_ALIGNMENT_LEFT, -1, 15, Color.WHITE)
	if (recovery or resume_countdown > 0.0) and not paused:
		draw_rect(Rect2(100, 172, 760, 142), Color("0c1528ee"))
		var message := "FACE INPUT LOST - GAME PAUSED"
		var detail := "Restore tracking then Enter, or press K for keyboard"
		if resume_countdown > 0.0:
			message = "RESUMING IN %d" % ceili(resume_countdown)
			detail = "Return to neutral; previous reactions were cleared"
		draw_string(font, Vector2(125, 231), message, HORIZONTAL_ALIGNMENT_LEFT, -1, 24, Color.WHITE)
		draw_string(font, Vector2(125, 274), detail, HORIZONTAL_ALIGNMENT_LEFT, -1, 18, Color.WHITE)
		return
	if paused:
		draw_rect(Rect2(130, 172, 700, 142), Color("0c1528dd"))
		draw_string(font, Vector2(165, 231), "PAUSED - PRESS ESC TO CONTINUE", HORIZONTAL_ALIGNMENT_LEFT, -1, 25, Color.WHITE)
		return
	if not running and menu_panel == null:
		draw_rect(Rect2(130, 172, 700, 142), Color("0c1528dd"))
		draw_string(font, Vector2(165, 231), banner, HORIZONTAL_ALIGNMENT_LEFT, -1, 25, Color("ffffff"))
		draw_string(font, Vector2(165, 274), "Use keyboard now; webcam bridge is optional", HORIZONTAL_ALIGNMENT_LEFT, -1, 18, Color("c8e2ff"))
	elif banner_time > 0.0:
		draw_string(font, Vector2(24, 240), banner, HORIZONTAL_ALIGNMENT_LEFT, 900, 18, Color("fff3b0"))

func ability_status(action: String) -> String:
	return "READY" if float(cooldowns[action]) <= 0 else "%.1fs" % cooldowns[action]

func toggle_camera_visibility() -> void:
	show_camera = not show_camera
	camera_view.configure(bridge.preview, running and facial_mode and show_camera)
	queue_redraw()

func draw_face_feedback() -> void:
	draw_style_box(cue_style, Rect2(710, 78, 220, 137))
	draw_string(font, Vector2(722, 99), "LIVE FACE INPUT", HORIZONTAL_ALIGNMENT_LEFT, -1, 15, Color("5eead4"))
	var reactions := ["mouth_open", "smile", "teeth"]
	var names := ["Jaw / Jump", "Smile / Shield", "Teeth / Chomp"]
	for index in range(3):
		var y := 119.0 + index * 26
		var key: String = reactions[index]
		var valid: bool = bridge.ready_for_face() and bridge.meters.has(key)
		draw_string(font, Vector2(722, y), names[index], HORIZONTAL_ALIGNMENT_LEFT, -1, 13, Color.WHITE)
		draw_rect(Rect2(838, y - 8, 76, 6), Color("304c63"))
		if valid:
			draw_rect(Rect2(838, y - 8, 76 * float(bridge.meters[key]), 6), Color("5eead4"))
		else:
			draw_string(font, Vector2(862, y + 1), "--", HORIZONTAL_ALIGNMENT_LEFT, -1, 12, Color.WHITE)
	draw_string(font, Vector2(722, 202), "Levels, not accuracy", HORIZONTAL_ALIGNMENT_LEFT, -1, 12, Color("c5d9ed"))
	draw_style_box(cue_style, Rect2(14, 247, 420, 28))
	draw_string(font, Vector2(24, 267), "Last: " + last_detection if bridge.ready_for_face() else "Facial input unavailable", HORIZONTAL_ALIGNMENT_LEFT, 660, 15, Color("5eead4"))
