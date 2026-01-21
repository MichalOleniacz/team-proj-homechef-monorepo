import type { RecipeResponseDto } from "../types/api";

export const mockRecipeResponse: RecipeResponseDto = {
    url: "https://www.example.com/recipes/spaghetti-bolognese",
    title: "Classic Spaghetti Bolognese",
    ingredients: [
        { quantity: "500", unit: "g", name: "spaghetti" },
        { quantity: "400", unit: "g", name: "ground beef" },
        { quantity: "1", name: "onion, diced" },
        { quantity: "3", unit: "cloves", name: "garlic, minced" },
        { quantity: "400", unit: "g", name: "canned tomatoes" },
        { quantity: "2", unit: "tbsp", name: "tomato paste" },
        { quantity: "1", unit: "tsp", name: "dried oregano" },
        { quantity: "1", unit: "tsp", name: "dried basil" },
        { name: "salt and pepper to taste" },
        { quantity: "1/2", unit: "cup", name: "red wine" },
        { name: "fresh parsley for garnish" },
        { name: "parmesan cheese for serving" }
    ],
};
