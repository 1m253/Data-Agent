import { UserProfileEditor } from '../components/common/UserProfileEditor';

export default function Profile() {
    return (
        <div>
            <div className="mb-6">
                <h2 className="text-xl font-semibold">Profile Settings</h2>
                <p className="text-sm text-muted-foreground mt-1">
                    Manage your personal information and profile settings
                </p>
            </div>
            <UserProfileEditor />
        </div>
    );
}
