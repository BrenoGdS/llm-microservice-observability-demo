# LLM Microservice Observability Demo

Spring Boot demo microservice showcasing how to call a locally running Ollama LLM via LangChain4j and expose a `/api/chat` endpoint ready for observability and memory/RAG extensions.

## Current Features
- LangChain4j + Ollama integration (`ChatLanguageModel` bean with configurable base URL, model, and temperature).
- Validated REST contract (`ChatRequest`, `ChatResponse`, `MessageDTO`) served via `POST /api/chat`.
- Service layer instrumentation: latency/token logging plus Langfuse-ready `ChatModelListener`.
- Unit and slice tests covering the service and controller layers.

## Prerequisites
- Java 17+ and Gradle (wrapper included).
- Ollama running locally (default `http://localhost:11434` with model `llama3` or update `application.yaml`).

## Run & Test
```bash
./gradlew bootRun    # start the API
./gradlew test       # run unit tests
```
Hit the API with:
```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"Tell me about LangChain4j"}'
```

## Langfuse observability (optional for now)
1. Provision Langfuse yourself (no Docker compose in this repo):
   - Fastest path: create a [Langfuse Cloud](https://langfuse.com/) workspace.
   - Or follow the [self-hosting guide](https://langfuse.com/self-hosting) to run Langfuse locally with your own Postgres/ClickHouse/Redis stack.
2. Once Langfuse is running (default UI http://localhost:3000 if local), create a project and generate API keys under **Settings → API Keys**.
3. Export the keys and enable the integration before starting Spring Boot:
   ```bash
   export LANGFUSE_PUBLIC_KEY=pk_xxx
   export LANGFUSE_SECRET_KEY=sk_xxx
   export LANGFUSE_ENABLED=true
   ./gradlew bootRun
   ```
4. Exercise `/api/chat`, then inspect traces/generations inside the Langfuse UI to review prompts, completions, latency, and token usage emitted by `LangfuseChatModelListener`.
5. Leave `langfuse.enabled=false` whenever Langfuse isn’t reachable to skip the extra HTTP calls.
6. (Collector path) Populate the project-root `.env` (shared by Spring + Docker Compose) with the Langfuse keys and OTLP settings, e.g.:
   ```dotenv
   LANGFUSE_PUBLIC_KEY=pk-lf-xxxxxxxx
   LANGFUSE_SECRET_KEY=sk-lf-xxxxxxxx
   LANGFUSE_BASE_URL=http://langfuse-web:3000
   LANGFUSE_TRACES_URL=http://langfuse-web:3000/api/public/otel/v1/traces
   LANGFUSE_BASIC_AUTH=cGstbGYtLi4uOnNrLS4uLg==  # base64 of "public:secret"
   ```
   You can generate `LANGFUSE_BASIC_AUTH` with `echo -n "pk-lf-…:sk-lf-…" | base64`.
   then run (from the repo root so Compose picks up `.env`, or pass it explicitly as below):
   ```bash
   docker-compose --env-file .env \
     -f gradle/docker-compose/docker-compose.yml \
     up -d langfuse-web langfuse-worker clickhouse postgres redis minio otel-collector
   ```
   The collector now sends OTLP payloads directly to Langfuse’s `/api/public/otel/v1/traces` endpoint, so no additional path mangling is necessary.

## Testing Steps

1. **Step 6.1** – Run the command above to boot Langfuse + OTEL collector, then confirm `docker-compose ... ps` shows them healthy.
2. **Step 6.2** – Start Spring Boot (defaults point to `http://localhost:4318/v1/traces`), hit `/api/chat`, and watch `docker-compose ... logs --tail=50 otel-collector` for incoming spans.
3. **Step 6.3** – Verify the collector forwards events to Langfuse by checking for HTTP 200 responses in the collector log and confirming the new traces/ generations appear in the Langfuse UI.

## Roadmap (next up)
1. Langfuse-based observability (ChatModelListener + OpenTelemetry).
2. Conversation memory backed by Redis.
3. RAG enrichment with a vector store (Redis Stack/Qdrant).
4. Minimal frontend/live-coding client.
5. Manual deployment guide tying together the Spring app, Langfuse, Redis, and a small frontend.
