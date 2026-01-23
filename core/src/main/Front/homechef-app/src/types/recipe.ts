export type SubmitUrlRequest = {
    url: string;
};

export type RecipeStatus = "PENDING" | "PROCESSING" | "COMPLETED" | "FAILED";

export type IngredientResponse = {
    quantity?: string;
    unit?: string;
    name: string;
};

export type RecipeResponse = {
    urlHash: string;
    title: string;
    ingredients: IngredientResponse[];
    parsedAt: string;
};

export type SubmitUrlResponse = {
    status: RecipeStatus;
    requestId: string;
    recipe?: RecipeResponse;
};

export type ParseStatusResponse = {
    requestId: string;
    status: RecipeStatus;
    error?: string;
    recipe?: RecipeResponse;
};
