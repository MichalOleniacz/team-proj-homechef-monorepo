import { createContext, useContext, useState } from "react";
import * as authApi from "../api/auth";
import { getStoredToken, setStoredToken } from "../api/client";
import type { AuthResponse } from "../types/auth";

type AuthContextType = {
    user: AuthResponse | null;
    login: (data: { email: string; password: string }) => Promise<void>;
    register: (data: { email: string; password: string }) => Promise<void>;
    logout: () => void;
    error: string | null;
};

const AuthContext = createContext<AuthContextType>(null!);

/**
 * Decode JWT payload without signature verification.
 * Signature validation occurs server-side on each API request.
 * This is only used to restore UI state from a previously-authenticated session.
 */
function parseJwt(token: string): { sub: string; email: string; exp: number } | null {
    try {
        const parts = token.split(".");
        if (parts.length !== 3) return null;
        const base64Url = parts[1];
        const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/");
        const payload = JSON.parse(atob(base64));
        if (typeof payload.sub !== "string" || typeof payload.email !== "string" || typeof payload.exp !== "number") {
            return null;
        }
        return payload;
    } catch {
        return null;
    }
}

function restoreUserFromToken(): AuthResponse | null {
    const token = getStoredToken();
    if (!token) return null;

    const payload = parseJwt(token);
    if (!payload) {
        setStoredToken(null);
        return null;
    }

    const now = Math.floor(Date.now() / 1000);
    if (payload.exp < now) {
        setStoredToken(null);
        return null;
    }

    return {
        userId: payload.sub,
        email: payload.email,
        accessToken: token,
        expiresIn: payload.exp - now,
    };
}

export function AuthProvider({ children }: { children: React.ReactNode }) {
    const [user, setUser] = useState<AuthResponse | null>(restoreUserFromToken);
    const [error, setError] = useState<string | null>(null);

    const login = async (data: { email: string; password: string }) => {
        setError(null);
        try {
            const res = await authApi.login(data);
            setUser(res);
        } catch (e) {
            const message = e instanceof Error ? e.message : "Login failed";
            setError(message);
            throw e;
        }
    };

    const register = async (data: { email: string; password: string }) => {
        setError(null);
        try {
            const res = await authApi.register(data);
            setUser(res);
        } catch (e) {
            const message = e instanceof Error ? e.message : "Register failed";
            setError(message);
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
