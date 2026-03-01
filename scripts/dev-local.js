const { spawn } = require('child_process');

const env = { ...process.env };

['MYSQL_DB', 'MYSQL_USER', 'MYSQL_PASSWORD', 'MYSQL_HOST', 'MYSQL_PORT'].forEach((key) => {
  delete env[key];
});

env.REDIS_DISABLED = 'true';
env.NODE_ENV = 'development';

const npmCmd = process.platform === 'win32' ? 'npm.cmd' : 'npm';
const child = spawn(npmCmd, ['run', 'dev'], {
  stdio: 'inherit',
  env,
  cwd: process.cwd()
});

child.on('exit', (code) => {
  process.exit(code ?? 0);
});

child.on('error', (error) => {
  console.error('Failed to start local dev command:', error.message);
  process.exit(1);
});
