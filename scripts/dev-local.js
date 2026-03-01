const spawn = require('cross-spawn');
const net = require('net');

function isPortAvailable(port) {
  return new Promise((resolve) => {
    const server = net.createServer();

    server.once('error', () => {
      resolve(false);
    });

    server.once('listening', () => {
      server.close(() => resolve(true));
    });

    server.listen(port, '0.0.0.0');
  });
}

async function findAvailablePort(startPort, maxAttempts = 25) {
  for (let port = startPort; port < startPort + maxAttempts; port += 1) {
    const available = await isPortAvailable(port);
    if (available) {
      return port;
    }
  }

  return null;
}

const rawEnv = { ...process.env };

['MYSQL_DB', 'MYSQL_USER', 'MYSQL_PASSWORD', 'MYSQL_HOST', 'MYSQL_PORT'].forEach((key) => {
  delete rawEnv[key];
});

rawEnv.REDIS_DISABLED = 'true';
rawEnv.NODE_ENV = 'development';
rawEnv.LOCAL_API_PORT = rawEnv.LOCAL_API_PORT || '5001';

const parseBoolean = (value, defaultValue) => {
  if (value === undefined) {
    return defaultValue;
  }

  return value.toLowerCase() === 'true';
};

const AUTO_SELECT_API_PORT = parseBoolean(rawEnv.AUTO_SELECT_API_PORT, true);

const env = Object.fromEntries(
  Object.entries(rawEnv).filter(([, value]) => value !== undefined && value !== null)
);

const npmCommand = process.platform === 'win32' ? 'npm.cmd' : 'npm';

async function start() {
  const preferredPort = Number.parseInt(rawEnv.LOCAL_API_PORT, 10);

  if (!Number.isInteger(preferredPort) || preferredPort <= 0) {
    console.error('Invalid LOCAL_API_PORT value. Please provide a valid port number.');
    process.exit(1);
  }

  let selectedPort = preferredPort;

  if (AUTO_SELECT_API_PORT) {
    const candidatePort = await findAvailablePort(preferredPort);

    if (candidatePort === null) {
      console.error(`No available backend port found between ${preferredPort} and ${preferredPort + 24}.`);
      process.exit(1);
    }

    selectedPort = candidatePort;

    if (selectedPort !== preferredPort) {
      console.log(`[dev:local] Backend port ${preferredPort} is busy. Using ${selectedPort} instead.`);
    }
  }

  env.LOCAL_API_PORT = String(selectedPort);
  env.PORT = String(selectedPort);

  if (!rawEnv.VITE_API_URL) {
    env.VITE_API_URL = `http://localhost:${selectedPort}/api`;
  }

  const child = spawn(npmCommand, ['run', 'dev'], {
    stdio: 'inherit',
    env,
    cwd: process.cwd(),
    windowsHide: false
  });

  child.on('exit', (code) => {
    process.exit(code ?? 0);
  });

  child.on('error', (error) => {
    console.error('Failed to start local dev command:', error.message);
    process.exit(1);
  });
}

start().catch((error) => {
  console.error('Failed to resolve local dev ports:', error.message);
  process.exit(1);
});
