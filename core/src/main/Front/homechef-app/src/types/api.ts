export type SubmitUrlRequest = {
    url: string;
};

export type IngredientDto = {
    quantity?: string;
    unit?: string | null;
    name: string;
};

export type RecipeResponseDto = {
    url: string;
    title: string;
    ingredients: IngredientDto[];
    // TODO: add instructions
    instructions?: string[];
};
