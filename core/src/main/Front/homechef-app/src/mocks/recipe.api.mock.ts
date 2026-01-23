import type { SubmitUrlRequest, RecipeResponseDto } from "../types/recipe.ts";
import { mockRecipeResponse } from "./recipe.mock";

export function submitUrlMock(
    payload: SubmitUrlRequest
): Promise<RecipeResponseDto> {
    return new Promise((resolve, reject) => {
        const delay = 1200 + Math.random() * 800;

        setTimeout(() => {
            if (!payload.url || !payload.url.startsWith("http")) {
                reject(new Error("Invalid URL"));
                return;
            }

            if (Math.random() < 0.1) {
                reject(new Error("AI service temporarily unavailable"));
                return;
            }

            resolve({
                ...mockRecipeResponse,
                url: payload.url, // echo inputu
            });
        }, delay);
    });
}
