import { createContext, useContext, useState } from "react";
import * as authApi from "../api/auth";
import type { AuthResponse } from "../types/auth";

type AuthContextType = {
    user: AuthResponse | null;
    login: (data: { email: string; password: string }) => Promise<void>;
    register: (data: { email: string; password: string }) => Promise<void>;
    logout: () => void;
    error: string | null;
};

const AuthContext = createContext<AuthContextType>(null!);

export function AuthProvider({ children }: { children: React.ReactNode }) {
    const [user, setUser] = useState<AuthResponse | null>(null);
    const [error, setError] = useState<string | null>(null);

    const login = async (data: { email: string; password: string }) => {
        setError(null);
        try {
            const res = await authApi.login(data);
            setUser(res);
        } catch (e: any) {
            setError(e.message ?? "Login failed");
            throw e;
        }
    };

    const register = async (data: { email: string; password: string }) => {
        setError(null);
        try {
            const res = await authApi.register(data);
            setUser(res);
        } catch (e: any) {
            setError(e.message ?? "Register failed");
            throw e;
        }
    };

    const logout = () => {
        authApi.logout();
        setUser(null);
    };

    return (
        <AuthContext.Provider value={{ user, login, register, logout, error }}>
            {children}
        </AuthContext.Provider>
    );
}

export const useAuth = () => useContext(AuthContext);
