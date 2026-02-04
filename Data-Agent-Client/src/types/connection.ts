/** Request body for test connection and open connection (no name). */
export interface ConnectRequest {
  dbType: string;
  host: string;
  port: number;
  database?: string;
  username: string;
  password?: string;
  driverJarPath: string;
  timeout?: number;
  properties?: Record<string, string>;
}

/** Request body for create/update connection (includes name). */
export interface ConnectionCreateRequest {
  name: string;
  dbType: string;
  host: string;
  port: number;
  database?: string;
  username?: string;
  password?: string;
  driverJarPath: string;
  timeout?: number;
  properties?: Record<string, string>;
}

/** Connection config as returned by API (no password). */
export interface DbConnection {
  id: number;
  name: string;
  dbType: string;
  host: string;
  port: number;
  database?: string;
  username?: string;
  driverJarPath: string;
  timeout?: number;
  properties?: Record<string, string>;
  createdAt?: string;
  updatedAt?: string;
}

/** Response from POST /connections/test */
export interface ConnectionTestResponse {
  status: 'SUCCEEDED' | 'FAILED';
  dbmsInfo: string;
  driverInfo: string;
  ping: number;
}

/** Response from POST /connections/open */
export interface OpenConnectionResponse {
  connectionId: string;
  dbType: string;
  host: string;
  port: number;
  database?: string;
  username?: string;
  connected: boolean;
  createdAt: string;
}

/** @deprecated Use ConnectionCreateRequest */
export type CreateConnectionRequest = ConnectionCreateRequest;

/** Form/test payload without name; use ConnectRequest or build from ConnectionCreateRequest. */
export type TestConnectionRequest = ConnectRequest;
