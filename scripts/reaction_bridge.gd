extends Node

const PORT := 47777
const REACTIONS := ["mouth_open", "brows_up", "smile", "teeth"]
var listen_port := PORT
var udp := PacketPeerUDP.new()
var status := "keyboard / start camera setup separately"
var pending: Dictionary = {}
var last_packet_ms := -1
var session_id := ""
var sequence := -1
var event_id := -1
var tracking := "unavailable"
var calibrated := false
var enabled: Array = []
var meters: Dictionary = {}
var preview: Dictionary = {}
var avatar: Dictionary = {}

func _ready() -> void:
	if udp.bind(listen_port, "127.0.0.1") != OK:
		status = "keyboard (UDP unavailable: another game may be running)"

func ready_for_face() -> bool:
	return last_packet_ms >= 0 and tracking == "tracked" and calibrated and not enabled.is_empty()

func _process(_delta: float) -> void:
	if last_packet_ms >= 0 and Time.get_ticks_msec() - last_packet_ms > 1000:
		status = "camera bridge disconnected / keyboard available"
		pending.clear()
		meters.clear()
		preview.clear()
		avatar.clear()
		tracking = "unavailable"
		calibrated = false
		last_packet_ms = -1
	for _packet_index in range(64):
		if udp.get_available_packet_count() <= 0:
			break
		var bytes := udp.get_packet()
		if udp.get_packet_error() != OK:
			break
		if bytes.is_empty() or bytes.size() > 4096:
			continue
		var parser := JSON.new()
		if parser.parse(bytes.get_string_from_utf8()) == OK:
			accept_packet(parser.data)

func valid_integer(value: Variant) -> bool:
	return (value is int or value is float) and is_finite(float(value)) and float(value) >= 0.0 and float(value) <= 9007199254740991.0 and floorf(float(value)) == float(value)

func accept_packet(data: Variant) -> bool:
	if not data is Dictionary:
		return false
	if not valid_integer(data.get("version")) or data.version != 1:
		return false
	if not data.get("session_id") is String or data.session_id.length() < 1 or data.session_id.length() > 64:
		return false
	if not valid_integer(data.get("sequence")) or not data.get("calibrated") is bool:
		return false
	if not data.get("tracking") in ["tracked", "lost", "unavailable"]:
		return false
	if not data.get("events") is Array or data.events.size() > 12:
		return false
	if not data.get("enabled") is Array or data.enabled.size() > REACTIONS.size():
		return false
	for reaction in data.enabled:
		if not reaction in REACTIONS:
			return false
	if not data.get("meters", {}) is Dictionary or data.get("meters", {}).size() > REACTIONS.size():
		return false
	for reaction in data.get("meters", {}):
		var value = data.meters[reaction]
		if not reaction in REACTIONS or not (value is int or value is float):
			return false
		if not is_finite(float(value)) or float(value) < 0.0 or float(value) > 1.0:
			return false
	var previous_id := -1
	var frame_source = data.get("preview", {})
	if not frame_source is Dictionary:
		return false
	if not frame_source.is_empty():
		if frame_source.size() != 2 or not valid_integer(frame_source.get("port")) or frame_source.port < 1 or frame_source.port > 65535:
			return false
		if not frame_source.get("token") is String or frame_source.token.length() != 32 or not frame_source.token.is_valid_hex_number():
			return false
	var face_pose = data.get("avatar", {})
	if not face_pose is Dictionary or face_pose.size() > 4:
		return false
	for key in face_pose:
		var value = face_pose[key]
		if key not in ["blink_left", "blink_right", "jaw", "smile"] or not (value is int or value is float) or not is_finite(float(value)) or value < 0 or value > 1:
			return false
	for event in data.events:
		if not event is Dictionary or not valid_integer(event.get("id")):
			return false
		if not event.get("reaction") in data.enabled or not event.get("reaction") in REACTIONS:
			return false
		if int(event.id) <= previous_id:
			return false
		previous_id = int(event.id)
	if data.session_id == session_id and int(data.sequence) <= sequence:
		return false
	if data.session_id != session_id:
		session_id = data.session_id
		sequence = -1
		event_id = -1
		pending.clear()
	sequence = int(data.sequence)
	last_packet_ms = Time.get_ticks_msec()
	tracking = data.tracking
	calibrated = data.calibrated
	enabled = data.enabled.duplicate()
	meters = data.get("meters", {}).duplicate()
	preview = frame_source.duplicate() if tracking != "unavailable" else {}
	avatar = face_pose.duplicate() if tracking == "tracked" else {}
	for event in data.events:
		if int(event.id) > event_id:
			event_id = int(event.id)
			if ready_for_face():
				pending[event.reaction] = true
	if not ready_for_face():
		pending.clear()
		meters.clear()
	if tracking == "unavailable":
		status = "camera unavailable / keyboard available"
	elif tracking == "lost":
		status = "camera connected / no face"
	elif not calibrated or enabled.is_empty():
		status = "face tracked / calibration required"
	else:
		status = "face tracked / %d calibrated controls" % enabled.size()
	return true

func consume_reactions() -> Dictionary:
	var result := pending.duplicate()
	pending.clear()
	return result

func _exit_tree() -> void:
	udp.close()
