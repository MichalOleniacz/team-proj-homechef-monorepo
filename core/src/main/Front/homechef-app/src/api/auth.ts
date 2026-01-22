import { apiFetch, AUTH_MODE, setStoredToken } from "./client";
import type { AuthResponse, LoginRequest, RegisterRequest } from "../types/auth";

const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "";

const LOGIN_ENDPOINT = "/api/auth/login";
const REGISTER_ENDPOINT = "/api/auth/register";
const ME_ENDPOINT = "/api/auth/me";
const LOGOUT_ENDPOINT = "/api/auth/logout";

export async function login(payload: LoginRequest) {
    const res = await apiFetch<AuthResponse>(`${BASE_URL}${LOGIN_ENDPOINT}`, {
        method: "POST",
        body: JSON.stringify(payload),
    });

    if (AUTH_MODE === "bearer" && res.token) setStoredToken(res.token);

    return res;
}

export async function register(payload: RegisterRequest) {
    const res = await apiFetch<AuthResponse>(`${BASE_URL}${REGISTER_ENDPOINT}`, {
        method: "POST",
        body: JSON.stringify(payload),
    });

    if (AUTH_MODE === "bearer" && res.token) setStoredToken(res.token);

    return res;
}

export async function me() {
    return apiFetch<AuthResponse>(`${BASE_URL}${ME_ENDPOINT}`, {
        method: "GET",
    });
}

export async function logout() {
    try {
        await apiFetch<{ ok: true }>(`${BASE_URL}${LOGOUT_ENDPOINT}`, {
            method: "POST",
            body: JSON.stringify({}),
        });
    } finally {
        if (AUTH_MODE === "bearer") setStoredToken(null);
    }
}
