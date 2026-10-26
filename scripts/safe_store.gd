extends RefCounted
## Validated config replacement with a previous-valid backup.

static func read_valid(path: String, validator: Callable) -> ConfigFile:
	var config := ConfigFile.new()
	if config.load(path) == OK and validator.call(config):
		return config
	return null

static func load_config(path: String, validator: Callable) -> Dictionary:
	var config := read_valid(path, validator)
	if config != null:
		return {"config": config, "notice": ""}
	var backup := read_valid(path + ".bak", validator)
	if backup != null:
		return {"config": backup, "notice": "Save recovery: previous valid backup loaded. Original file retained."}
	return {"config": null, "notice": "Save could not be read; original file retained. Using defaults." if FileAccess.file_exists(path) else ""}

static func save_config(config: ConfigFile, path: String, validator: Callable) -> Error:
	if not validator.call(config):
		return ERR_INVALID_DATA
	var temporary := path + ".tmp"
	var error := config.save(temporary)
	if error != OK or read_valid(temporary, validator) == null:
		return error if error != OK else ERR_INVALID_DATA
	if FileAccess.file_exists(path):
		# Preserve a damaged original separately; never replace a good backup with it.
		var archive := path + (".bak" if read_valid(path, validator) != null else ".corrupt")
		error = DirAccess.copy_absolute(path, archive)
		if error != OK:
			return error
	return DirAccess.rename_absolute(temporary, path)
