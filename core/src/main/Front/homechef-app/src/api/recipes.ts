import { apiFetch } from "./client";
import type { RecipeResponseDto, SubmitUrlRequest } from "../types/api";

const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "";
const SUBMIT_URL_ENDPOINT = "/api/recipe";

export async function submitUrl(payload: SubmitUrlRequest) {
    return apiFetch<RecipeResponseDto>(`${BASE_URL}${SUBMIT_URL_ENDPOINT}`, {
        method: "POST",
        body: JSON.stringify(payload),
    });
}
