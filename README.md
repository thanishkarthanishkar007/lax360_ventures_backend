# LAX360 Ventures — Backend

Java 17 · Spring Boot 3.3.4 (Maven) · MongoDB Atlas · Resend (email)

Handles the "Book a Demo" form submitted from the frontend: validates the
input, saves it to MongoDB Atlas, and emails a notification via Resend.
Built to deploy on **Render** as a Docker web service with zero code
changes.

## Deployed instances

- **Frontend:** https://lax360-ventures-frontend.vercel.app
- **Backend:** https://lax360-ventures-backend.onrender.com

These are wired together already:
- The frontend's `DemoForm.jsx` defaults `VITE_API_BASE_URL` to the
  Render backend URL above, so it needs no environment variable set on
  Vercel to work.
- The backend's `CORS_ALLOWED_ORIGINS` default now includes the Vercel
  URL above (in `application.properties`, `.env`, and `render.yaml`).

**I could not verify this live end-to-end myself** — this sandbox's
network is restricted to a fixed allowlist that doesn't include
`onrender.com` or `vercel.app`, so I can't `curl` either deployed URL
from here. Please check these three things after redeploying both sides
(Render redeploys automatically on a git push if it's connected to your
repo; otherwise trigger a manual deploy):

```bash
# 1. Backend is up and can reach MongoDB
curl https://lax360-ventures-backend.onrender.com/actuator/health
# expect: {"status":"UP"}  (Render free tier can take ~30-60s to wake from sleep)

# 2. CORS is actually allowing the Vercel origin
curl -i -X OPTIONS https://lax360-ventures-backend.onrender.com/api/demo-requests \
  -H "Origin: https://lax360-ventures-frontend.vercel.app" \
  -H "Access-Control-Request-Method: POST"
# expect a 200 with an Access-Control-Allow-Origin header matching the Vercel URL

# 3. A real submission works end-to-end
curl -i -X POST https://lax360-ventures-backend.onrender.com/api/demo-requests \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Test User","email":"test@example.com","mobileNumber":"+919876543210","company":"Test Co","product":"FlowOps"}'
# expect 201 Created, plus a notification email at lax360salem@gmail.com
```

If step 2 fails (no `Access-Control-Allow-Origin` header, or a browser
console CORS error when submitting the real form), double-check the
`CORS_ALLOWED_ORIGINS` value actually set in the Render dashboard — a
value set there overrides the default and must match the Vercel URL
**exactly** (no trailing slash).

## ⚠️ About the credentials you shared

You pasted a live MongoDB connection string and a live Resend API key
directly in chat. I've used them so everything works out of the box, but
please treat them as compromised the moment they're shared anywhere
outside a secrets manager:

- **Rotate the MongoDB Atlas database user's password** (Atlas → Database
  Access → edit user) and **regenerate the Resend API key**
  (resend.com/api-keys) once you're done testing, especially before this
  ZIP or a repo containing it is ever made public.
- Neither value is hardcoded in the source — both are read from
  environment variables (`MONGODB_URI`, `RESEND_API_KEY`). They're only
  pre-filled in the local `.env` file below, which is gitignored.

## Why demo request emails arrived late (and what's fixed)

There were two separate causes stacked on top of each other:

**1. Email sending was blocking the HTTP response (fixed in code).**
Previously, `DemoRequestService` saved to MongoDB, then waited on the
full Resend API round-trip (network + TLS + their server) *before*
returning a response to the frontend. If Resend was slow for any reason,
the whole form submission felt slow too, and the email — despite being
technically sent — only went out after that same blocking call finished.

Fixed: `EmailService.sendDemoRequestNotification()` is now `@Async` (see
`AsyncConfig.java`), running on its own small thread pool. The controller
now returns `201 Created` to the frontend the instant MongoDB save
succeeds, and the Resend call fires in the background milliseconds later.

**2. Render free-tier cold starts (needs a dashboard change — I can't fix
this from code).** Render's **free** web services spin down after ~15
minutes with no traffic. The *next* request has to wait for Render to
boot a fresh container — and a JVM/Spring Boot app typically takes
20–50+ seconds to cold-start, sometimes longer under load. If a demo
request lands right after the service has been idle, that entire
cold-start delay happens *before* your code even runs, which reads
exactly like "the email arrived late." This is almost certainly the
dominant cause here, since Resend itself is normally sub-second.

Two ways to fix #2 (pick one):
- **Upgrade the Render service off the Free plan** — paid instances
  don't spin down, so there's no cold start at all. This is the
  reliable fix.
- **Keep the free instance warm** — have something ping
  `https://lax360-ventures-backend.onrender.com/actuator/health` every
  10–14 minutes (before the 15-minute sleep timer), e.g. a free account
  on [cron-job.org](https://cron-job.org) or
  [UptimeRobot](https://uptimerobot.com). This costs nothing but is a
  workaround, not a real fix — the instance can still sleep during low
  traffic windows if the ping ever fails, and it wastes the free tier's
  usage hours.

I can't tell from here which of these two causes was contributing more
in your case (no access to your Render deployment/logs), but #1 is fixed
either way, and #2 is worth checking first since it's usually the bigger
number.

## Project layout

```
src/main/java/com/lax360/backend/
  BackendApplication.java        entry point
  model/DemoRequest.java         MongoDB document
  dto/DemoRequestDto.java        validated request body
  repository/DemoRequestRepository.java
  service/DemoRequestService.java   save + trigger email
  service/EmailService.java         Resend HTTP API call
  controller/DemoRequestController.java   POST/GET /api/demo-requests
  controller/HealthController.java        GET /api/health
  config/CorsConfig.java         allowed origins (env-driven)
  config/RestTemplateConfig.java
  exception/GlobalExceptionHandler.java
src/main/resources/application.properties   fully env-var driven
Dockerfile / .dockerignore       production image for Render
render.yaml                      Render Blueprint (optional, for IaC deploy)
.env.example / .env              local dev env vars (.env is gitignored)
```

## API

### `POST /api/demo-requests`
```json
{
  "fullName": "Jane Doe",
  "email": "jane@company.com",
  "mobileNumber": "+91 98765 43210",
  "company": "Acme Inc",
  "product": "FlowOps"
}
```
→ `201 Created` with the saved record, plus a best-effort email to
`NOTIFY_EMAIL` via Resend. Validation errors return `400` with a
`fields` map of per-field messages.

### `GET /api/demo-requests`
Lists all saved requests, newest first. **Unauthenticated** — put this
behind Spring Security or an API-key check before relying on it with
real data.

### `GET /api/health` and `GET /actuator/health`
Both return `200 OK` when the app (and its MongoDB connection) is
healthy. `/actuator/health` is what `render.yaml` uses as the health
check path.

## Environment variables

| Variable | Required | Notes |
|---|---|---|
| `PORT` | set by Render automatically | app reads it via `server.port=${PORT:8080}` |
| `MONGODB_URI` | **yes** | full Atlas connection string, include a database name in the path |
| `RESEND_API_KEY` | **yes** | from resend.com/api-keys |
| `RESEND_FROM_EMAIL` | no (defaults to `LAX360 Ventures <onboarding@resend.dev>`) | must be a domain verified in Resend once you move off the sandbox sender |
| `NOTIFY_EMAIL` | no (defaults to `lax360salem@gmail.com`) | who receives the notification |
| `CORS_ALLOWED_ORIGINS` | no (defaults to the live frontend + localhost) | comma-separated frontend origins; default already includes `https://lax360-ventures-frontend.vercel.app` |
| `JWT_SECRET` | no | reserved for future authenticated endpoints, unused today |
| `LOG_LEVEL`, `APP_LOG_LEVEL` | no | default `INFO` |

Note: the Mongo URI you gave (`.../?appName=lax360`) has no database name
in it, so Atlas would fall back to a database called `test`. The `.env`
file below adds `/lax360` before the `?` so data lands in a clearly named
`lax360` database — adjust if you'd rather use something else.

## Run locally

Requires Java 17 and Maven.

```bash
cd lax360-backend
mvn spring-boot:run
```

That's it — no `export`, no `$env:`, nothing shell-specific. The app
auto-loads the `.env` file sitting next to `pom.xml` on startup (see
`BackendApplication.loadDotEnvIfPresent()`), so it works the same on
Windows PowerShell/cmd, macOS, and Linux. A real OS/Render environment
variable always wins over anything in `.env` if both are set.

The API is then live at `http://localhost:8080`.

## Run with Docker

```bash
docker build -t lax360-backend .
docker run --env-file .env -p 8080:8080 lax360-backend
```

> **I could not run `docker build` or `mvn package` in the sandbox this
> was built in** — that environment blocks outbound access to Maven
> Central (`repo.maven.apache.org`) and doesn't have Docker installed, so
> dependency resolution fails there (confirmed with a 403 on the parent
> POM). I did verify the project manually instead: every `package`
> declaration matches its file's directory, every `.java` file's braces
> balance, and `pom.xml` is well-formed XML. This is a completely
> standard Spring Boot 3 + Maven layout, and Render's build servers (like
> any normal dev machine) have full internet access, so `docker build`
> will succeed there without any changes. Still, **please run
> `mvn clean package` or `docker build` locally once before deploying**,
> just so you're not debugging a first-time build failure in production.

## Deploy to Render

**Option A — Blueprint (`render.yaml`)**
1. Push this project to a GitHub repo.
2. In Render: New → Blueprint → point at the repo. It reads `render.yaml`
   automatically.
3. Render will prompt for the `sync: false` variables — paste in
   `MONGODB_URI` and `RESEND_API_KEY`. `CORS_ALLOWED_ORIGINS` is already
   pre-filled with the Vercel frontend URL and `JWT_SECRET` is
   auto-generated — change either later in the dashboard if needed.
4. Deploy. Render builds the `Dockerfile`, sets `PORT` automatically, and
   health-checks `/actuator/health`.

**Option B — Manual web service**
1. New → Web Service → connect the repo → Environment: **Docker**.
2. Health Check Path: `/actuator/health`.
3. Add the environment variables listed in the table above (skip `PORT`,
   Render sets it).
4. Deploy.

Once live, the frontend needs no changes — `DemoForm.jsx` already
defaults to `https://lax360-ventures-backend.onrender.com`. If you ever
move the backend to a different URL, either update that default in
`DemoForm.jsx` or set `VITE_API_BASE_URL` in Vercel's project settings.

## Production notes already handled

- **No hardcoded config** — every credential/URL comes from an env var.
- **Cross-platform local dev** — `BackendApplication` auto-loads `.env` on startup (Windows/macOS/Linux alike); real environment variables always take priority, so this has zero effect in Docker/Render.
- **`PORT` support** — binds to Render's dynamic port via `server.port=${PORT:8080}`.
- **Health check** — `/actuator/health` (Spring Boot Actuator) + a plain `/api/health`.
- **CORS** — origins are entirely env-driven, not hardcoded.
- **Non-root Docker user**, slim `eclipse-temurin:17-jre-alpine` runtime image.
- **`.dockerignore`** keeps `target/`, `.git/`, `.env`, and docs out of the build context.
- Email failures never fail the form submission — the lead is already saved in MongoDB by the time Resend is called.

## Not yet done (call these out if you need them)
- No authentication on `GET /api/demo-requests` — add Spring Security or an API key before using it for anything real.
- No rate limiting on the public POST endpoint.
- No automated tests included.

## Update log (frontend/backend connected)
- Frontend's `VITE_API_BASE_URL` now defaults to
  `https://lax360-ventures-backend.onrender.com` (see `DemoForm.jsx`) —
  no env var needed on Vercel.
- Backend's `CORS_ALLOWED_ORIGINS` default now includes
  `https://lax360-ventures-frontend.vercel.app` (in
  `application.properties`, `.env`, `.env.example`, and `render.yaml`).
- See "Deployed instances" above for manual curl-based verification steps
  I couldn't run myself from this sandbox.
