import React, { createContext, useContext, useEffect, useMemo, useState } from "react";
import type { AuthUser, LoginRequest, RegisterRequest } from "../types/auth";
import * as authApi from "../api/auth";
import { ApiError } from "../api/client";

type AuthState = {
    user: AuthUser | null;
    loading: boolean;
    error: string | null;
    login: (p: LoginRequest) => Promise<void>;
    register: (p: RegisterRequest) => Promise<void>;
    logout: () => Promise<void>;
    refreshMe: () => Promise<void>;
};

const AuthContext = createContext<AuthState | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
    const [user, setUser] = useState<AuthUser | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const refreshMe = async () => {
        setError(null);
        try {
            const res = await authApi.me();
            setUser(res.user ?? null);
        } catch (e) {
            setUser(null);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        refreshMe();
    }, []);

    const login = async (p: LoginRequest) => {
        setError(null);
        try {
            const res = await authApi.login(p);
            if (res.user) setUser(res.user);
            else await refreshMe();
        } catch (e: any) {
            const msg = e instanceof ApiError ? e.message : "Login failed";
            setError(msg);
            throw e;
        }
    };

    const register = async (p: RegisterRequest) => {
        setError(null);
        try {
            const res = await authApi.register(p);
            if (res.user) setUser(res.user);
            else await refreshMe();
        } catch (e: any) {
            const msg = e instanceof ApiError ? e.message : "Register failed";
            setError(msg);
            throw e;
        }
    };

    const logout = async () => {
        setError(null);
        await authApi.logout();
        setUser(null);
    };

    const value = useMemo(
        () => ({ user, loading, error, login, register, logout, refreshMe }),
        [user, loading, error]
    );

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
    const ctx = useContext(AuthContext);
    if (!ctx) throw new Error("useAuth must be used within AuthProvider");
    return ctx;
}
