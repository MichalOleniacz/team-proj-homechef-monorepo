import { apiFetch, setStoredToken } from "./client";
import type { AuthResponse, LoginRequest, RegisterRequest } from "../types/auth";

const API_BASE = "";

export async function register(payload: RegisterRequest): Promise<AuthResponse> {
    const res = await apiFetch<AuthResponse>(`${API_BASE}/auth/register`, {
        method: "POST",
        body: JSON.stringify(payload),
    });

    setStoredToken(res.accessToken);

    return res;
}

export async function login(payload: LoginRequest): Promise<AuthResponse> {
    const res = await apiFetch<AuthResponse>(`${API_BASE}/auth/login`, {
        method: "POST",
        body: JSON.stringify(payload),
    });

    setStoredToken(res.accessToken);
    return res;
}

export function logout() {
    setStoredToken(null);
}
