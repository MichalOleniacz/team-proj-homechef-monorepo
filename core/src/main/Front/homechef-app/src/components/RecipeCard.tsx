import { useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import type { RecipeResponseDto } from "../types/api";

function formatIngredient(i: { quantity?: string; unit?: string | null; name: string }) {
    const q = i.quantity?.trim();
    const u = i.unit?.trim();
    if (q && u) return `${q} ${u} ${i.name}`;
    if (q) return `${q} ${i.name}`;
    return i.name;
}

export default function RecipeCard({ recipe }: { recipe: RecipeResponseDto }) {
    const { t } = useTranslation();
    const [copied, setCopied] = useState(false);

    const ingredientsLines = useMemo(
        () => recipe.ingredients.map((x) => `- ${formatIngredient(x)}`).join("\n"),
        [recipe.ingredients]
    );

    const instructionsLines = useMemo(() => {
        const ins = recipe.instructions ?? [];
        if (!ins.length) return "";
        return ins.map((x, idx) => `${idx + 1}. ${x}`).join("\n");
    }, [recipe.instructions]);

    const textToCopy = useMemo(() => {
        const parts = [
            recipe.title,
            recipe.url ? `Source: ${recipe.url}` : "",
            "",
            `${t("recipe.ingredients")}:\n${ingredientsLines}`,
        ];
        if (instructionsLines) {
            parts.push("", `${t("recipe.steps")}:\n${instructionsLines}`);
        }
        return parts.filter(Boolean).join("\n");
    }, [recipe.title, recipe.url, t, ingredientsLines, instructionsLines]);

    const onCopy = async () => {
        try {
            await navigator.clipboard.writeText(textToCopy);
            setCopied(true);
            window.setTimeout(() => setCopied(false), 1100);
        } catch {

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
            <div className="flex items-start justify-between gap-4">
                <div>
                    <div className="text-lg font-semibold">{recipe.title}</div>
                    <div className="text-xs opacity-70 break-all">{recipe.url}</div>
                </div>

                <button
                    onClick={onCopy}
                    className="rounded-xl border px-3 py-2 text-sm transition hover:opacity-90"
                    style={{ borderColor: "var(--color-border)", background: "var(--color-surface)" }}
                >
                    {copied ? t("recipe.copied") : t("recipe.copy")}
                </button>
            </div>

            <div className="mt-5">
                <div className="text-sm font-semibold mb-2">{t("recipe.ingredients")}</div>
                <ul className="space-y-2 text-sm">
                    {recipe.ingredients.map((x, i) => (
                        <li key={i} className="flex gap-2">
                            <span className="mt-1">•</span>
                            <span className="opacity-90">{formatIngredient(x)}</span>
                        </li>
                    ))}
                </ul>

                {!!recipe.instructions?.length && (
                    <>
                        <div className="mt-5 text-sm font-semibold mb-2">{t("recipe.steps")}</div>
                        <ol className="space-y-2 text-sm">
                            {recipe.instructions.map((x, i) => (
                                <li key={i} className="flex gap-3">
                  <span
                      className="inline-flex h-6 w-6 items-center justify-center rounded-lg text-xs font-semibold"
                      style={{
                          background: "var(--color-bg)",
                          border: "1px solid var(--color-border)",
                      }}
                  >
                    {i + 1}
                  </span>
                                    <span className="opacity-90">{x}</span>
                                </li>
                            ))}
                        </ol>
                    </>
                )}
            </div>
        </div>
    );
}
