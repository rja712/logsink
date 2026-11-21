# TODO

What the library should do inside, step by step.
Each item is also marked `// TODO` in the code where it belongs.

## initialize(yamlPath) — `LogManager`, `manager/ConfigManager`
- [ ] Create the output destinations (console, file) from config
- [ ] Create the formatter (how a line looks) from config
- [x] Pick SyncLogger or AsyncLogger from "mode" (`manager/LoggerRegistry`)
- [ ] If mode is async, start the background thread that empties the queue
- [x] Read mode, level and outputs in `ConfigManager.load()`

## logging a message — `api/SyncLogger`, `api/AsyncLogger`
- [ ] SyncLogger: send the line to every output destination on the caller's thread
- [ ] AsyncLogger: put the message on a BlockingQueue; a background thread writes it out
- [ ] Use the formatter instead of the fixed line in `util/LineFormatter`

## shutdown() — `manager/ShutdownManager`
- [ ] Write out any messages still waiting (needed once async exists)
- [ ] Close the output destinations (e.g. close open files)

## config — `model/LoggerConfig`, `util/YamlFileReader`, `src/main/resources/logger.yaml`
- [x] Add settings: mode, level, outputs (console, file) with format, location, time-duration
- [x] Output type is an enum (`model/output/OutputType`); each type has its own config class
- [x] Support nested sections in `YamlFileReader`
- [ ] Support lists in `YamlFileReader` if the config needs them
- [x] Read db and kafka outputs from config
- [ ] Confirm db and kafka settings (first guess: db url + table, kafka brokers + topic)
- [ ] Check the format text once the formatter exists

## later
- [ ] Tests with JUnit
