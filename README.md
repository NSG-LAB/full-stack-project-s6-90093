# full-stack-project-s6-90093

This repository currently contains two backend tracks:

- Node.js backend used by the main full-stack app (`backend/` + `frontend/`)
- Java Spring Boot migration workspace (`new Spring Boot project/`)

## Workspace Structure

- `backend/`: Node.js API, middleware, models, routes, services, tests
- `frontend/`: React app (Vite), Redux store, components, pages
- `new Spring Boot project/`: Java migration backend (Maven, Spring Boot)
- `docker/`, `k8s/`, `scripts/`: Deployment and operational assets

## Local Startup (Node.js Full Stack)

- Run `npm run dev:local`
- Default backend port is `5001`
- If `5001` is busy, launcher auto-selects the next available backend port
- Frontend API URL is auto-aligned to the selected backend port

Optional overrides:

- `AUTO_SELECT_API_PORT=false` forces strict use of `LOCAL_API_PORT`
- `LOCAL_API_PORT=5010` changes the preferred backend port

Examples:

- PowerShell: `$env:AUTO_SELECT_API_PORT='false'; $env:LOCAL_API_PORT='5010'; npm run dev:local`
- bash: `AUTO_SELECT_API_PORT=false LOCAL_API_PORT=5010 npm run dev:local`

## Deployed Backend (Railway)

- Backend base URL: `https://full-stack-project-s6-90093-production.up.railway.app`
- API base URL to use in frontend builds: `https://full-stack-project-s6-90093-production.up.railway.app/api`
- Frontend production env key: `VITE_API_URL`
- Backend CORS allowlist envs: `FRONTEND_URL` and `CORS_ALLOWED_ORIGINS` (comma-separated)

This repo now includes `frontend/.env.production` with `VITE_API_URL` set to the Railway API URL.

## Spring Boot Migration Module

From `new Spring Boot project/`:

- Build: `mvn clean install`
- Test: `mvn test`

Notes:

- Build artifacts are generated in `new Spring Boot project/target/` and are ignored by git.
- Java upgrade tool logs under `new Spring Boot project/.github/java-upgrade/` are ignored by git.

### API Parity Progress (Node -> Spring Boot)

Implemented in `new Spring Boot project/`:

- Auth: `POST /api/auth/register`, `POST /api/auth/login`, `POST /api/auth/logout`
- Users/Admin: profile read/update and admin user listing
- Properties: CRUD, filtering/pagination, CSV export, image upload
- Valuations: `POST /api/valuations/estimate`
- Notifications: list/filter, create, mark-as-read
- Recommendations: list/filter/pagination, property-specific suggestions, admin create/update/delete
- ROI planner: `POST /api/roi/plan`
- Reports: `POST /api/reports/valuation-pdf`
- Analytics (admin): overview, user activity, properties, performance

Remaining parity modules are being migrated incrementally (for example monitoring and enhancement checklist flows).

## Quick Local Test Tip (Node Backend Without Redis)

- Windows PowerShell: `$env:REDIS_DISABLED='true'; $env:NODE_ENV='test'; npm test`
- Linux/macOS: `REDIS_DISABLED=true NODE_ENV=test npm test`
