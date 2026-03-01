# full-stack-project-s6-90093

Quick local test/debug tip (without Redis):

- Windows PowerShell: `$env:REDIS_DISABLED='true'; $env:NODE_ENV='test'; npm test`
- Linux/macOS: `REDIS_DISABLED=true NODE_ENV=test npm test`
