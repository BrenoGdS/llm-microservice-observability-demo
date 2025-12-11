# llm-microservice-observability-demo

Spring Boot microservice that uses LangChain4j + Ollama to explain financing quotes. Upcoming milestones include Langfuse observability, Redis-backed memory, and a lightweight RAG demo tailored for workshops.

## Prerequisites

- Java 17+
- [Ollama](https://ollama.com/download) installed locally (macOS, Windows, or Linux). Allocate ~6 GiB of memory inside Docker Desktop if you use the desktop app.
- Curl/Postman for manual API calls
- Docker + Docker Compose (used to run Redis for conversation memory)

## Ollama workflow

### One-time setup

1. Install/launch Ollama.
2. Pull at least one model (e.g., `llama3`). This downloads the weights once:
   ```bash
   ollama pull llama3
   ```
3. Check which models are available:
   ```bash
   ollama list
   ```

### Every time you run the project

1. Start the Ollama daemon (or open the desktop app, which starts it automatically):
   ```bash
   ollama serve
   ```
2. Confirm the API is up:
   ```bash
   curl http://localhost:11434/api/tags
   ```
3. When you finish testing, stop the daemon from the menu bar or with:
   ```bash
   pkill -f "ollama serve"
   ```

> The Spring app expects `langchain4j.ollama.chat-model.base-url=http://localhost:11434`. Update `application.yaml` if you change ports or models.

## Redis via Docker Compose

1. Start (or restart) the container:
   ```bash
   docker-compose up -d redis
   ```
2. Check status:
   ```bash
   docker-compose ps redis
   redis-cli -h localhost ping
   ```
3. Stop when finished:
   ```bash
   docker-compose stop redis        # or `docker-compose down` to remove it
   ```

## Run the Spring Boot app

```bash
./gradlew bootRun
```

Sample request:

```bash
curl --location 'http://localhost:8080/api/chat' \
  --header 'Content-Type: application/json' \
  --data '{
    "conversationId": "",
    "message": "Can you remind me which financing plan you suggested?",
    "history": [],
    "memory": { "enabled": false }
  }'
```

## Conversation memory (Redis)

- The app ships with `chat.memory.enabled=true`, but it only uses Redis for calls where the payload includes `"memory": { "enabled": true }`. Requests without that block behave exactly like before (stateless, using user-provided `history` if any).
- Using Redis-backed memory:
  1. Start the Redis container (`docker-compose up -d redis`) and keep reusing the same `conversationId` across turns.
  2. Add `"memory": { "enabled": true }` to the JSON payload to load/persist turns automatically; the server will ignore client-provided `history` and rely on Redis.
  3. Tweak `chat.memory.window-size`, `chat.memory.ttl`, or `chat.memory.key-prefix` in `application.yaml` as needed.
- To disable the feature globally (e.g., when Redis is unavailable), set `chat.memory.enabled=false` through an environment variable, JVM property, or profile override.

## Tests

```bash
./gradlew test
```

## Roadmap

The detailed implementation plan lives in `/.cursor/plans/ll-69754e9c.plan.md`.

