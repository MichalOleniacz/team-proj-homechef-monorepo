export class ApiError extends Error {
    status: number;
    details?: unknown;

    constructor(message: string, status: number, details?: unknown) {
        super(message);
        this.name = "ApiError";
        this.status = status;
        this.details = details;
    }
}

async function safeReadJson(res: Response) {
    const text = await res.text();
    if (!text) return undefined;
    try {
        return JSON.parse(text);
    } catch {
        return text;
    }
}

export const AUTH_MODE: "cookie" | "bearer" = "bearer";

export function getStoredToken() {
    return localStorage.getItem("access_token");
}

export function setStoredToken(token: string | null) {
    if (!token) localStorage.removeItem("access_token");
    else localStorage.setItem("access_token", token);
}

export async function apiFetch<TResponse>(
    input: RequestInfo | URL,
    init?: RequestInit
): Promise<TResponse> {
    const headers: Record<string, string> = {
        "Content-Type": "application/json",
        ...(init?.headers as any),
    };

    if (AUTH_MODE === "bearer") {
        const token = getStoredToken();
        if (token) headers["Authorization"] = `Bearer ${token}`;
    }

    console.log("[apiFetch]", input);
    console.log("[apiFetch] AUTH_MODE:", AUTH_MODE);
    console.log("[apiFetch] token:", getStoredToken());
    console.log("[apiFetch] headers:", headers);


    const res = await fetch(input, {
        ...init,
        headers,
        credentials: "include",
    });

    if (!res.ok) {
        const details = await safeReadJson(res);
        const msg =
            (details && typeof details === "object" && "message" in (details as any) && (details as any).message) ||
            `Request failed (${res.status})`;
        throw new ApiError(String(msg), res.status, details);
    }

    return (await res.json()) as TResponse;
}
