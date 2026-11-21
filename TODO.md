# TODO

What the library should do inside, step by step.
Each item is also marked `// TODO` in the code where it belongs.

## initialize(yamlPath) — `LogManager`, `manager/ConfigManager`
- [x] Read mode, level and outputs in `ConfigManager.load()`
- [x] Pick SyncOrchestrator or AsyncOrchestrator from "mode", once (`manager/LoggerRegistry`)
- [x] If mode is async, start the background thread that empties the queue (`orchestrator/AsyncListener`)
- [x] Create the formatter (how a line looks) from the output's `format` (`util/LineFormatter`)
- [ ] JVM shutdown hook that calls shutdown(), so queued events aren't lost if the app forgets (`orchestrator/AsyncListener`)

## logging a message — `api/impl/DefaultLogger`, `orchestrator/`
- [x] One logger (`DefaultLogger`); sync/async is decided by the `Orchestrator` picked from "mode"
- [x] sync: `SyncOrchestrator` → every sink, one by one, on the caller's thread
- [x] async: `AsyncOrchestrator` → BlockingQueue → `AsyncListener` (background thread) → every sink
- [x] Read the queue size from the YAML (`mode.async.queue-size`, default 10,000)
- [x] `LineFormatter` turns a `LogEvent` into text using the `format` from the YAML
- [x] `<ip>`, `<service_name>`, `<version>` come from the app: `api/ServiceInfo`, passed to `LogManager.initialize(path, serviceInfo)`
- [x] `<line_number>` is looked up by the library (`util/LineNumberUitl`), only if a format uses it

## sinks (destinations) — `sink/`
- [x] `Sink` interface and `SinkFactory` (builds the list once)
- [x] `ConsoleSink` and `FileSink`; `SinkFactory` builds one per output, each with its own format
- [x] `FileSink`: new file per hour/day (`time-range`), moves a full file to a backup (`max-size`)
- [ ] `max-backups` setting, to delete the oldest backups (`sink/FileSink`)
- [ ] `DbSink` and `KafkaSink` (`SinkFactory` fails at startup for db/kafka until then)
- [x] Delete the temporary `sink/PrintSink`
- [x] Add `close()` to `Sink`

## shutdown() — `manager/ShutdownManager`
- [x] Write out any messages still waiting in the queue (async)
- [x] Close the sinks (`SinkFactory.closeAll()`)

## config — `manager/model/LoggerConfig`, `util/YamlFileReader`, `src/main/resources/logger.yaml`
- [x] Add settings: mode, level, outputs (console, file) with format, location, time-duration
- [x] Output type is an enum (`manager/model/output/OutputType`); each type has its own config class
- [x] Support nested sections in `YamlFileReader`
- [x] `mode` is a section: `type` picks sync or async, each with its own settings
      (`model/Mode` lists them, `model/mode/` holds one config class per mode)
- [ ] Support lists in `YamlFileReader` if the config needs them
- [x] Read db and kafka outputs from config
- [ ] Confirm db and kafka settings (first guess: db url + table, kafka brokers + topic)
- [x] Unknown placeholders in `format` fail at startup (`LineFormatter`)

## later
- [ ] Tests with JUnit
