.PHONY: all db server .executable desktop wasm lint .clean-gradle .clean-docker clean

all:
	@# do nothing by default

db:
	docker compose --file docker-compose.yml --env-file .env build bring-dev-postgres
	docker compose --file docker-compose.yml --env-file .env up bring-dev-postgres

server:
	docker compose --file docker-compose.yml --env-file .env build
	docker compose --file docker-compose.yml --env-file .env up

.executable:
	chmod +X ./gradlew

desktop: .executable
	./gradlew composeApp:runReleaseDistributable

wasm: .executable
	./gradlew composeApp:wasmJsBrowserProductionRun

.clean-docker:
	docker compose --file docker-compose.yml --env-file .env down

.clean-gradle: .executable
	./gradlew clean

clean: .clean-gradle .clean-docker