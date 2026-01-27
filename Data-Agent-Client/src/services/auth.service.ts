import http from '../lib/http';
import { TokenManager } from '../lib/token-manager';
import { LoginRequest, RegisterRequest, ResetPasswordRequest, TokenPairResponse } from '../types/auth';

export const authService = {
    /**
     * Login with email and password
     */
    login: async (data: LoginRequest): Promise<TokenPairResponse> => {
        const response = await http.post<TokenPairResponse>('/auth/login', data);
        const { accessToken, refreshToken } = response.data;
        TokenManager.setTokens(accessToken, refreshToken, data.rememberMe || false);
        return response.data;
    },

    /**
     * Register a new user
     */
    register: async (data: RegisterRequest): Promise<boolean> => {
        const response = await http.post<boolean>('/auth/register', data);
        return response.data;
    },

    /**
     * Logout the current user
     */
    logout: async (): Promise<boolean> => {
        try {
            await http.post<boolean>('/auth/logout');
        } finally {
            TokenManager.clearTokens();
        }
        return true;
    },

    /**
     * Reset password
     */
    resetPassword: async (data: ResetPasswordRequest): Promise<boolean> => {
        const response = await http.post<boolean>('/auth/reset-password', data);
        return response.data;
    },

    /**
     * Get OAuth authorization URL
     * @param provider 'google' | 'github'
     * @param fromUrl URL to redirect back to after login
     */
    getOAuthUrl: (provider: 'google' | 'github', fromUrl: string): string => {
        // Note: This is a direct browser redirect, not an AJAX call
        // We construct the URL pointing to our backend
        const encodedFromUrl = encodeURIComponent(fromUrl);
        return `/api/oauth/${provider}?fromUrl=${encodedFromUrl}`;
    },
};
