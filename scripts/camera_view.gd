extends Node
## Local, memory-only preview. Never follows a remote URL from a packet.

var texture: ImageTexture
var enabled := false
var url := ""
var requested_url := ""
var request: HTTPRequest
var busy := false
var next_poll := 0.0
var last_frame := -1

func _ready() -> void:
	request = HTTPRequest.new()
	request.timeout = .8
	request.body_size_limit = 350000
	request.use_threads = true
	add_child(request)
	request.request_completed.connect(received)

func configure(metadata: Dictionary, show: bool) -> void:
	var target := ""
	if not metadata.is_empty():
		target = "http://127.0.0.1:%d/frame/%s" % [metadata.port, metadata.token]
	if target != url or show != enabled:
		request.cancel_request()
		busy = false
		texture = null
		last_frame = -1
		url = target
		enabled = show
		next_poll = 0

func _process(delta: float) -> void:
	if last_frame >= 0 and Time.get_ticks_msec() - last_frame > 1000:
		texture = null
	if not enabled or url.is_empty():
		return
	next_poll -= delta
	if next_poll <= 0 and not busy:
		next_poll = .12
		requested_url = url
		busy = request.request(url) == OK

func received(result: int, code: int, _headers: PackedStringArray, body: PackedByteArray) -> void:
	busy = false
	if not enabled or requested_url != url:
		return
	if result != HTTPRequest.RESULT_SUCCESS or code != 200:
		texture = null
		return
	if body.size() < 4 or body.size() > 350000 or body[0] != 255 or body[1] != 216:
		texture = null
		return
	var image := Image.new()
	if image.load_jpg_from_buffer(body) != OK or image.get_width() > 640 or image.get_height() > 480:
		texture = null
		return
	if texture == null or texture.get_size() != Vector2(image.get_size()):
		texture = ImageTexture.create_from_image(image)
	else:
		texture.update(image)
	last_frame = Time.get_ticks_msec()
