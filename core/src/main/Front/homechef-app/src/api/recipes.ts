import { apiFetch } from "./client";
import type { ParseStatusResponse, SubmitUrlRequest, SubmitUrlResponse } from "../types/recipe";
import { mockGetStatus, mockSubmit } from "../mocks/recipes.seed";

const API_BASE = ""; // jeśli używasz vite proxy

const USE_MOCK = import.meta.env.VITE_USE_MOCK_API === "true";

export async function parseRecipe(payload: SubmitUrlRequest): Promise<SubmitUrlResponse> {
    if (USE_MOCK) {
        // symulacja opóźnienia
        await new Promise((r) => setTimeout(r, 600));
        return mockSubmit(payload.url);
    }

    return apiFetch<SubmitUrlResponse>(`${API_BASE}/api/v1/recipes/parse`, {
        method: "POST",
        body: JSON.stringify(payload),
    });
}

export async function getParseRequest(id: string): Promise<ParseStatusResponse> {
    if (USE_MOCK) {
        await new Promise((r) => setTimeout(r, 600));
        return mockGetStatus(id);
    }

    return apiFetch<ParseStatusResponse>(`${API_BASE}/api/v1/recipes/parse-requests/${id}`, {
        method: "GET",
    });
}
