import { useState } from "react";
import { Button } from "../ui/Button";
import { Input } from "../ui/Input";
import { DialogContent, DialogDescription, DialogHeader, DialogTitle } from "../ui/Dialog";
import { authService } from "../../services/auth.service";
import { Alert } from "../ui/Alert";
import { useToast } from "../../hooks/useToast";

interface RegisterModalProps {
    onSwitchToLogin: () => void;
}

export function RegisterModal({ onSwitchToLogin }: RegisterModalProps) {
    const [username, setUsername] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const toast = useToast();

    const handleRegister = async () => {
        setError(null);
        // Validation
        if (!username || !email || !password) {
            setError("Please fill in all fields.");
            return;
        }

        if (password !== confirmPassword) {
            setError("Passwords do not match.");
            return;
        }

        if (password.length < 6) {
            setError("Password must be at least 6 characters long.");
            return;
        }

        try {
            setLoading(true);
            await authService.register({ username, email, password });
            toast.success("Account created successfully! You can now sign in.");
            onSwitchToLogin();
        } catch (error) {
            console.error("Registration failed", error);
            setError("Registration failed. Please try again.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <DialogContent className="sm:max-w-[425px]">
            <DialogHeader>
                <DialogTitle className="text-2xl text-center">Sign Up</DialogTitle>
                <DialogDescription className="text-center">
                    Create a new account to get started
                </DialogDescription>
            </DialogHeader>
            <div className="grid gap-4 py-4">
                {error && (
                    <Alert variant="destructive">
                        {error}
                    </Alert>
                )}
                <div className="grid gap-2">
                    <label htmlFor="username" className="text-sm font-medium text-foreground">Username</label>
                    <Input
                        id="username"
                        type="text"
                        placeholder="Your username"
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                    />
                </div>
                <div className="grid gap-2">
                    <label htmlFor="email" className="text-sm font-medium text-foreground">Email</label>
                    <Input
                        id="email"
                        type="email"
                        placeholder="m@example.com"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                    />
                </div>
                <div className="grid gap-2">
                    <label htmlFor="password" className="text-sm font-medium text-foreground">Password</label>
                    <Input
                        id="password"
                        type="password"
                        placeholder="At least 6 characters"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                    />
                </div>
                <div className="grid gap-2">
                    <label htmlFor="confirm-password" className="text-sm font-medium text-foreground">Confirm Password</label>
                    <Input
                        id="confirm-password"
                        type="password"
                        placeholder="Re-enter your password"
                        value={confirmPassword}
                        onChange={(e) => setConfirmPassword(e.target.value)}
                    />
                </div>
                <Button className="w-full" onClick={handleRegister} disabled={loading}>
                    {loading ? "Creating Account..." : "Create Account"}
                </Button>
            </div>
            <div className="text-sm text-center text-muted-foreground">
                Already have an account?{" "}
                <button className="text-primary hover:underline" onClick={onSwitchToLogin}>
                    Sign in
                </button>
            </div>
        </DialogContent>
    );
}
