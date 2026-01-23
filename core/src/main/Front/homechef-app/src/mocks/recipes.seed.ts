import type { ParseStatusResponse, RecipeResponse, SubmitUrlResponse } from "../types/recipe";

function uuid() {
    // prosty uuid v4 (wystarczy do mocka)
    return crypto.randomUUID();
}

function nowIso() {
    return new Date().toISOString();
}

export function makeCompletedRecipe(url: string): RecipeResponse {
    // urlHash może być dowolny w mocku
    const urlHash = `mock_${btoa(url).slice(0, 10).replace(/=/g, "")}`;

    const recipes: Array<Omit<RecipeResponse, "urlHash" | "parsedAt"> & { urlHash?: string; parsedAt?: string }> = [
        {
            title: "Classic Spaghetti Bolognese",
            ingredients: [
                { quantity: "500", unit: "g", name: "spaghetti" },
                { quantity: "400", unit: "g", name: "ground beef" },
                { quantity: "1", unit: "", name: "onion, diced" },
                { quantity: "3", unit: "cloves", name: "garlic, minced" },
                { quantity: "400", unit: "g", name: "canned tomatoes" },
                { quantity: "2", unit: "tbsp", name: "tomato paste" },
                { quantity: "1", unit: "tsp", name: "dried oregano" },
                { quantity: "1", unit: "tsp", name: "dried basil" },
                { name: "salt and pepper to taste" },
                { quantity: "1/2", unit: "cup", name: "red wine" },
                { name: "fresh parsley for garnish" },
                { name: "parmesan cheese for serving" },
            ],
        },
        {
            title: "Pancakes (Fluffy)",
            ingredients: [
                { quantity: "200", unit: "g", name: "flour" },
                { quantity: "2", unit: "", name: "eggs" },
                { quantity: "300", unit: "ml", name: "milk" },
                { quantity: "1", unit: "tbsp", name: "sugar" },
                { quantity: "1", unit: "tsp", name: "baking powder" },
                { quantity: "1", unit: "pinch", name: "salt" },
                { quantity: "2", unit: "tbsp", name: "butter (melted)" },
            ],
        },
        {
            title: "Chicken Curry (Quick)",
            ingredients: [
                { quantity: "500", unit: "g", name: "chicken breast" },
                { quantity: "1", unit: "", name: "onion" },
                { quantity: "2", unit: "tbsp", name: "curry powder" },
                { quantity: "400", unit: "ml", name: "coconut milk" },
                { quantity: "1", unit: "tbsp", name: "oil" },
                { name: "salt to taste" },
                { name: "rice for serving" },
            ],
        },
    ];

    // deterministyczny wybór na podstawie URL
    const idx = Math.abs(hash(url)) % recipes.length;
    const base = recipes[idx];

    return {
        urlHash,
        title: base.title,
        ingredients: base.ingredients,
        parsedAt: nowIso(),
    };
}

function hash(s: string) {
    let h = 0;
    for (let i = 0; i < s.length; i++) h = (h * 31 + s.charCodeAt(i)) | 0;
    return h;
}

/**
 * Mock “DB” statusów per requestId
 */
const statusStore = new Map<string, { step: number; url: string }>();

export function mockSubmit(url: string): SubmitUrlResponse {
    const requestId = uuid();
    statusStore.set(requestId, { step: 0, url });

    return {
        requestId,
        status: "PENDING",
        // recipe w PENDING może być puste — trzymamy się realnego świata
        recipe: undefined,
    };
}

export function mockGetStatus(id: string): ParseStatusResponse {
    const item = statusStore.get(id);

    // nieznany id -> symulujemy FAIL
    if (!item) {
        return { requestId: id, status: "FAILED", error: "Unknown requestId" };
    }

    // krokujemy: PENDING -> PROCESSING -> COMPLETED
    item.step += 1;
    statusStore.set(id, item);

    if (item.step === 1) {
        return { requestId: id, status: "PENDING" };
    }

    if (item.step === 2) {
        return { requestId: id, status: "PROCESSING" };
    }

    // final
    return {
        requestId: id,
        status: "COMPLETED",
        recipe: makeCompletedRecipe(item.url),
    };
}
