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

## Roadmap (next up)
1. Langfuse-based observability (ChatModelListener + OpenTelemetry).
2. Conversation memory backed by Redis.
3. RAG enrichment with a vector store (Redis Stack/Qdrant).
4. Minimal frontend/live-coding client.
5. Manual deployment guide tying together the Spring app, Langfuse, Redis, and a small frontend.
