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

export async function apiFetch<TResponse>(
    input: RequestInfo | URL,
    init?: RequestInit
): Promise<TResponse> {
    const res = await fetch(input, {
        ...init,
        headers: {
            "Content-Type": "application/json",
            ...(init?.headers ?? {}),
        },
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
