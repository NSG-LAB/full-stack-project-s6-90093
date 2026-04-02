# full-stack-project-s6-90093

Local startup:

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


## Cleanup (April 2, 2026)


The following unwanted files and folders were removed as part of repository maintenance:
- backend/coverage/ (coverage reports, not needed in repo)
- backend/.env.example (example env file, not needed in production)
- All non-essential .md and summary files from the project root (e.g., API_DOCUMENTATION.md, DEMO_ACCOUNTS.md, ENHANCEMENT_IDEAS.md, IMPROVEMENTS_CHECKLIST.md, IMPROVEMENTS_SUMMARY.md, MISSING_FEATURES_ANALYSIS.md, MONITORING_README.md, PROJECT_ASSESSMENT.md, SAMPLE_DATA.md, TEST_AND_VERIFICATION_REPORT.md, VALIDATION_AND_IMPROVEMENTS.md, and others).

Only essential documentation (README.md) is retained for clarity and maintainability.

Quick local test/debug tip (without Redis):

- Windows PowerShell: `$env:REDIS_DISABLED='true'; $env:NODE_ENV='test'; npm test`
- Linux/macOS: `REDIS_DISABLED=true NODE_ENV=test npm test`
