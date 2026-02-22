import http from '../lib/http';
import { ApiPaths } from '../constants/apiPaths';

export const databaseExportImportService = {
  exportDatabase: async (
    connectionId: string,
    databaseName: string
  ): Promise<string> => {
    const params: Record<string, string> = {
      connectionId,
      databaseName,
    };

    const response = await http.get<string>(ApiPaths.DATABASE_EXPORT, { params });
    return response.data;
  },

  exportTables: async (
    connectionId: string,
    databaseName: string
  ): Promise<string[]> => {
    const params: Record<string, string> = {
      connectionId,
      databaseName,
    };

    const response = await http.get<string[]>(ApiPaths.DATABASE_EXPORT_TABLES, { params });
    return response.data;
  },

  importSql: async (
    connectionId: string,
    sqlScript: string
  ): Promise<void> => {
    const params: Record<string, string> = {
      connectionId,
      sqlScript,
    };

    await http.post(ApiPaths.DATABASE_IMPORT, null, { params });
  },

  importFile: async (
    connectionId: string,
    file: File
  ): Promise<void> => {
    const formData = new FormData();
    formData.append('connectionId', connectionId);
    formData.append('file', file);

    const response = await http.post<void>(ApiPaths.DATABASE_IMPORT_FILE, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },
};
