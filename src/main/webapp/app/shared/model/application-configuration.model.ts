export interface ApplicationProperties {
  configFilePath: string;
  forwardStats: boolean;
  manageContainers: boolean;
  startupWait: number;
  server: NoosphereServer;
  chain: Chain;
  docker?: Docker;
  containers: NoosphereContainer[];
}

export interface NoosphereServer {
  port: number;
  rateLimit: RateLimit;
}

export interface RateLimit {
  numRequests: number;
  period: number;
}

export interface Chain {
  enabled: boolean;
  rpcUrl: string;
  trailHeadBlocks: number;
  routerAddress?: string;
  wallet?: Wallet;
  snapshotSync: SnapshotSync;
}

export interface Wallet {
  maxGasLimit: number;
  privateKey?: string;
  paymentAddress?: string;
  allowedSimErrors: string[];
}

export interface SnapshotSync {
  sleep: number;
  batchSize: number;
  startingSubId: number;
  syncPeriod: number;
}

export interface Docker {
  username: string;
  password: string;
}

export interface NoosphereContainer {
  id: string;
  image: string;
  url: string;
  bearer: string;
  port: number;
  external: boolean;
  gpu: boolean;
  acceptedPayments: { [key: string]: number };
  allowedIps: string[];
  allowedAddresses: string[];
  allowedDelegateAddresses: string[];
  description?: string;
  command?: string;
  env: { [key: string]: any };
  generatesProofs: boolean;
  volumes: string[];
}

export interface ValidationResult {
  valid: boolean;
  message: string;
  errors?: string[];
}

export const defaultApplicationProperties = (): ApplicationProperties => ({
  configFilePath: 'config/application-runtime.json',
  forwardStats: true,
  manageContainers: true,
  startupWait: 5.0,
  server: {
    port: 8080,
    rateLimit: {
      numRequests: 100,
      period: 60,
    },
  },
  chain: {
    enabled: true,
    rpcUrl: 'http://localhost:8545',
    trailHeadBlocks: 10,
    snapshotSync: {
      sleep: 1.0,
      batchSize: 100,
      startingSubId: 0,
      syncPeriod: 300.0,
    },
  },
  containers: [],
});
