import { useState } from 'react';
import { useAuthStore } from '../../store/authStore';
import { userService } from '../../services/user.service';
import { Button } from '../ui/Button';
import { Input } from '../ui/Input';
import { User, Save } from 'lucide-react';
import { resolveErrorMessage } from '../../lib/errorMessage';

export function UserProfileEditor() {
    const { user, setAuth, accessToken, refreshToken } = useAuthStore();
    const [username, setUsername] = useState(user?.username || '');
    const [avatarUrl, setAvatarUrl] = useState(user?.avatarUrl || '');
    const [isLoading, setIsLoading] = useState(false);
    const [message, setMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsLoading(true);
        setMessage(null);

        try {
            const updateData: any = {};
            if (username !== user?.username) updateData.username = username;
            if (avatarUrl !== user?.avatarUrl) updateData.avatarUrl = avatarUrl;

            if (Object.keys(updateData).length === 0) {
                setMessage({ type: 'error', text: 'No changes to save' });
                setIsLoading(false);
                return;
            }

            await userService.updateProfile(updateData);

            // Update local user state
            if (user) {
                setAuth(
                    { ...user, username: username || user.username, avatarUrl: avatarUrl || user.avatarUrl },
                    accessToken,
                    refreshToken
                );
            }

            setMessage({ type: 'success', text: 'Profile updated successfully!' });
        } catch (error: any) {
            setMessage({ type: 'error', text: resolveErrorMessage(error, 'Failed to update profile') });
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="space-y-6">
            <div className="flex items-center gap-2">
                <User className="h-5 w-5 text-primary" />
                <h2 className="text-xl font-semibold">Profile Settings</h2>
            </div>

            <form onSubmit={handleSubmit} className="space-y-4">
                <div className="space-y-2">
                    <label htmlFor="username" className="text-sm font-medium">
                        Username
                    </label>
                    <Input
                        id="username"
                        type="text"
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                        placeholder="Enter your username"
                        minLength={2}
                        maxLength={50}
                    />
                </div>

                <div className="space-y-2">
                    <label htmlFor="email" className="text-sm font-medium text-muted-foreground">
                        Email (read-only)
                    </label>
                    <Input
                        id="email"
                        type="email"
                        value={user?.email || ''}
                        disabled
                        className="bg-muted/50 cursor-not-allowed"
                    />
                </div>

                <div className="space-y-2">
                    <label htmlFor="avatarUrl" className="text-sm font-medium">
                        Avatar URL
                    </label>
                    <Input
                        id="avatarUrl"
                        type="url"
                        value={avatarUrl}
                        onChange={(e) => setAvatarUrl(e.target.value)}
                        placeholder="https://example.com/avatar.jpg"
                        maxLength={500}
                    />
                    {avatarUrl && (
                        <div className="mt-2">
                            <img
                                src={avatarUrl}
                                alt="Avatar preview"
                                className="h-16 w-16 rounded-full object-cover border-2 border-border"
                                onError={(e) => {
                                    e.currentTarget.style.display = 'none';
                                }}
                            />
                        </div>
                    )}
                </div>

                {message && (
                    <div
                        className={`p-3 rounded-md text-sm ${message.type === 'success'
                                ? 'bg-green-50 dark:bg-green-900/20 text-green-800 dark:text-green-200 border border-green-200 dark:border-green-800'
                                : 'bg-red-50 dark:bg-red-900/20 text-red-800 dark:text-red-200 border border-red-200 dark:border-red-800'
                            }`}
                    >
                        {message.text}
                    </div>
                )}

                <Button type="submit" disabled={isLoading} className="w-full sm:w-auto">
                    <Save className="h-4 w-4 mr-2" />
                    {isLoading ? 'Saving...' : 'Save Changes'}
                </Button>
            </form>
        </div>
    );
}
