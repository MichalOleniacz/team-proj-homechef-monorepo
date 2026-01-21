import type { RecipeResponseDto, SubmitUrlRequest } from "../types/api";
import { apiFetch } from "./client";
import { submitUrlMock } from "../mocks/recipe.api.mock";

const USE_MOCK_API = true;

const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "";
const ENDPOINT = "/api/recipe";

export async function submitUrl(payload: SubmitUrlRequest): Promise<RecipeResponseDto> {
    if (USE_MOCK_API) {
        return submitUrlMock(payload);
    }

    return apiFetch<RecipeResponseDto>(`${BASE_URL}${ENDPOINT}`, {
        method: "POST",
        body: JSON.stringify(payload),
    });
}
