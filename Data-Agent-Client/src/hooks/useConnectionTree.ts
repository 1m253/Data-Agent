import { useState, useMemo, useEffect } from 'react';
import { NodeApi } from 'react-arborist';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { connectionService } from '../services/connection.service';
import { databaseService } from '../services/database.service';
import type { DbConnection } from '../types/connection';

export interface ExplorerNode {
  id: string;
  name: string;
  type: 'root' | 'db' | 'schema' | 'table' | 'folder';
  connectionId?: string;
  dbConnection?: DbConnection;
  children?: ExplorerNode[];
}

export function useConnectionTree() {
  const queryClient = useQueryClient();

  const { data: connections = [], isLoading: isConnectionsLoading, refetch: refetchConnections } = useQuery({
    queryKey: ['connections'],
    queryFn: () => connectionService.getConnections(),
  });

  const treeData = useMemo<ExplorerNode[]>(() => {
    return connections.map((conn) => ({
      id: `conn-${conn.id}`,
      name: conn.name,
      type: 'root' as const,
      dbConnection: conn,
      children: [],
    }));
  }, [connections]);

  const [treeDataState, setTreeDataState] = useState<ExplorerNode[]>([]);

  useEffect(() => {
    setTreeDataState(treeData);
  }, [treeData]);

  const deleteMutation = useMutation({
    mutationFn: (id: number) => connectionService.deleteConnection(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['connections'] });
    },
  });

  const disconnectMutation = useMutation({
    mutationFn: (connectionId: string) => connectionService.closeConnection(connectionId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['connections'] });
    },
  });

  const loadNodeData = async (node: NodeApi<ExplorerNode>) => {
    if (node.data.type !== 'root' || !node.data.dbConnection) return;
    const conn = node.data.dbConnection;
    try {
      const openRes = await connectionService.openConnection({
        dbType: conn.dbType,
        host: conn.host,
        port: conn.port,
        database: conn.database,
        username: conn.username ?? '',
        driverJarPath: conn.driverJarPath,
        timeout: conn.timeout ?? 30,
        properties: conn.properties ?? {},
      });

      const dbNames = await databaseService.listDatabases(openRes.connectionId);
      const childrenNodes: ExplorerNode[] = dbNames.map((name) => ({
        id: `${node.id}-db-${name}`,
        name,
        type: 'db',
        connectionId: openRes.connectionId,
        children: [],
      }));

      setTreeDataState((prev) => {
        const update = (list: ExplorerNode[]): ExplorerNode[] =>
          list.map((n) => {
            if (n.id === node.id) {
              return { ...n, connectionId: openRes.connectionId, children: childrenNodes };
            }
            if (n.children) return { ...n, children: update(n.children) };
            return n;
          });
        return update(prev);
      });
    } catch (err) {
      console.error('Failed to open connection:', err);
    }
  };

  const handleDisconnect = (node: NodeApi<ExplorerNode>) => {
    if (!node.data.connectionId) return;
    disconnectMutation.mutate(node.data.connectionId);
    setTreeDataState((prev) => {
      const update = (list: ExplorerNode[]): ExplorerNode[] =>
        list.map((n) => {
          if (n.id === node.id) return { ...n, connectionId: undefined, children: [] };
          if (n.children) return { ...n, children: update(n.children) };
          return n;
        });
      return update(prev);
    });
  };

  return {
    connections,
    treeDataState,
    setTreeDataState,
    loadNodeData,
    handleDisconnect,
    isConnectionsLoading,
    refetchConnections,
    deleteMutation,
  };
}
