extends RefCounted
## Authored, repeatable introductory campaign. Sound stages remain future content.

const STAGES := [
	{"name": "First Steps", "world": "MEADOW", "action": "SPACE / jaw drop: jump", "kind": 0, "sky": "172f48", "ground": "459e83"},
	{"name": "Biscuit Factory", "world": "FACTORY", "action": "A: chomp biscuits   SPACE: jump", "kind": 1, "sky": "392d49", "ground": "b78052"},
	{"name": "Falling Stars", "world": "NIGHT GARDEN", "action": "S / smile: shield   SPACE: jump", "kind": 2, "sky": "242544", "ground": "57649a"},
]
const LEVELS_PER_STAGE := 3
const COURSE_COUNT := 9
const TITLES := [
	["The First Bounce", "Meadow Hopscotch", "The Picnic Escape"],
	["A Taste of Trouble", "Cookie Conveyor", "The Great Biscuit Breakout"],
	["Catch a Falling Star", "Moonlight Shuffle", "The Midnight Dash"],
]
const MISSIONS := [
	"The picnic has come alive. Hop over the grumpy crumbs!",
	"Escape the snack shift. Bite the biscuits; jump the orange hazards!",
	"The sky is throwing a party. Shield the stars; jump the orange hazards!",
]

static func course(index: int) -> Dictionary:
	index = clampi(index, 0, COURSE_COUNT - 1)
	var stage: int = index / LEVELS_PER_STAGE
	var level: int = index % LEVELS_PER_STAGE
	var definition: Dictionary = STAGES[stage].duplicate()
	definition["stage"] = stage + 1
	definition["level"] = level + 1
	definition["duration"] = 28.0 + level * 6.0
	definition["lesson"] = ["Learn", "Practice", "Challenge"][level]
	definition["title"] = TITLES[stage][level]
	definition["mission"] = MISSIONS[stage]
	var events: Array[Dictionary] = []
	var time := 2.0
	var ordinal := 0
	while time < float(definition["duration"]) - 4.5:
		# At most two actions per course. Later lessons interleave jump hazards.
		var kind: int = definition["kind"]
		if level > 0 and ordinal % 3 == 2:
			kind = 0
		events.append({"time": time, "kind": kind})
		time += 4.0 - level * .4
		ordinal += 1
	definition["events"] = events
	return definition
