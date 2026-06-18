.PHONY: all db server .executable desktop wasm lint .clean-gradle .clean-docker clean

all:
	@# do nothing by default

db:
	docker compose --file docker-compose.yml --env-file .env build savvry-dev-postgres
	docker compose --file docker-compose.yml --env-file .env up savvry-dev-postgres

server:
	docker compose --file docker-compose.yml --env-file .env build
	docker compose --file docker-compose.yml --env-file .env up

.executable:
	chmod +X ./gradlew

server-local: .executable
	./gradlew server:runShadow

desktop: .executable
	./gradlew :app:desktopApp:runReleaseDistributable

wasm: .executable
	./gradlew :app:webApp:wasmJsBrowserProductionRun

screenshots: .executable
	./gradlew :app:shared:testAndroidHostTest

.clean-docker:
	docker compose --file docker-compose.yml --env-file .env down

.clean-gradle: .executable
	./gradlew clean

clean: .clean-gradle .clean-docker