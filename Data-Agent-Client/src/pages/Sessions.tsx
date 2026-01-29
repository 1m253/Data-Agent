import { SessionManager } from '../components/common/SessionManager';

export default function Sessions() {
    return (
        <div>
            <div className="mb-6">
                <h2 className="text-xl font-semibold">Active Sessions</h2>
                <p className="text-sm text-muted-foreground mt-1">
                    Manage your active sessions and logout from other devices
                </p>
            </div>
            <SessionManager />
        </div>
    );
}
