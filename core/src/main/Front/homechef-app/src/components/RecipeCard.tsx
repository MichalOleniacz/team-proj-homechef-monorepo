import { useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import type { IngredientResponse, RecipeResponse } from "../types/recipe";

/**
 * Formats a single ingredient line
 */
function formatIngredient(i: IngredientResponse) {
    const q = i.quantity?.trim();
    const u = i.unit?.trim();
    const name = i.name?.trim();

    if (q && u) return `${q} ${u} ${name}`;
    if (q) return `${q} ${name}`;
    return name;
}

type Props = {
    recipe: RecipeResponse;
};

export default function RecipeCard({ recipe }: Props) {
    const { t } = useTranslation();
    const [copied, setCopied] = useState(false);

    /**
     * SAFETY:
     * ingredients MAY be undefined if backend is still processing
     */
    const ingredients = recipe.ingredients ?? [];

    const ingredientsLines = useMemo(() => {
        if (!ingredients.length) return "";
        return ingredients.map((x) => `- ${formatIngredient(x)}`).join("\n");
    }, [ingredients]);

    const textToCopy = useMemo(() => {
        const parts = [
            recipe.title,
            "",
            `${t("recipe.ingredients")}:\n${ingredientsLines}`,
        ];
        return parts.join("\n");
    }, [recipe.title, t, ingredientsLines]);

    const onCopy = async () => {
        try {
            await navigator.clipboard.writeText(textToCopy);
            setCopied(true);
            window.setTimeout(() => setCopied(false), 1100);
        } catch {
            // optional: toast / error message
        }
    };

    return (
        <div
            className="rounded-2xl border p-5 transition hover:-translate-y-1 hover:scale-[1.01]"
            style={{
                background: "var(--color-surface-strong)",
                borderColor: "var(--color-border)",
                boxShadow: "var(--shadow-card)",
                borderRadius: "var(--radius-card)",
            }}
        >
            {/* Header */}
            <div className="flex items-start justify-between gap-4">
                <div className="min-w-0">
                    <div className="text-lg font-semibold">{recipe.title}</div>
                </div>

                <button
                    onClick={onCopy}
                    disabled={!ingredients.length}
                    className="shrink-0 rounded-xl border px-3 py-2 text-sm transition hover:opacity-90 disabled:opacity-50"
                    style={{
                        borderColor: "var(--color-border)",
                        background: "var(--color-surface)",
                    }}
                >
                    {copied ? t("recipe.copied") : t("recipe.copy")}
                </button>
            </div>

            {/* Ingredients */}
            <div className="mt-5">
                <div className="text-left text-sm font-semibold mb-2">
                    {t("recipe.ingredients")}
                </div>

                {ingredients.length === 0 ? (
                    <div className="text-sm opacity-70">
                        {t("recipe.loading")}
                    </div>
                ) : (
                    <ul className="space-y-2 text-sm">
                        {ingredients.map((x) => (
                            <li
                                key={`${x.quantity ?? ""}|${x.unit ?? ""}|${x.name}`}
                                className="flex gap-2"
                            >
                                <span className="mt-1">•</span>
                                <span className="opacity-90">
                  {formatIngredient(x)}
                </span>
                            </li>
                        ))}
                    </ul>
                )}
            </div>
        </div>
    );
}
