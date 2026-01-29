import http from '../lib/http';
import { UpdateUserRequest } from '../types/auth';

export const userService = {
    /**
     * Update current user profile
     */
    updateProfile: async (data: UpdateUserRequest): Promise<boolean> => {
        const response = await http.put<boolean>('/user/me', data);
        return response.data;
    },
};
