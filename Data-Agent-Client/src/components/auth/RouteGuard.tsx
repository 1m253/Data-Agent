import { useEffect } from 'react';
import { useAuthStore, triggerLoginModal } from '../../store/authStore';

interface RouteGuardProps {
    children: React.ReactNode;
}

export function RouteGuard({ children }: RouteGuardProps) {
    const { user } = useAuthStore();

    useEffect(() => {
        if (!user) {
            triggerLoginModal();
        }
    }, [user]);

    if (user) {
        return <>{children}</>;
    }

    return null;
}
