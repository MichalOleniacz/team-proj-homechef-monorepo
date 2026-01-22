import { useState } from "react";
import { useTranslation } from "react-i18next";
import Container from "../components/Container";
import RecipeCard from "../components/RecipeCard";
import { useSubmitUrl } from "../hooks/useSubmitUrl";
import RecipeSkeleton from "../components/RecipeSkeleton";

function isValidUrl(url: string) {
    try {
        const u = new URL(url);
        return u.protocol === "http:" || u.protocol === "https:";
    } catch {
        return false;
    }
}

export default function HomeSection() {
    const { t } = useTranslation();
    const [url, setUrl] = useState("");
    const [localError, setLocalError] = useState<string | null>(null);

    const { data, loading, error, run } = useSubmitUrl();

    const onGetRecipe = async () => {
        const trimmed = url.trim();

        if (!trimmed) {
            setLocalError(t("api.emptyUrl"));
            return;
        }
        if (!isValidUrl(trimmed)) {
            setLocalError(t("api.invalidUrl"));
            return;
        }

        setLocalError(null);
        await run(trimmed);
    };

    const displayError = localError || error || null;
    const finalError = displayError ?? null;

    return (
        <section id="home" className="scroll-mt-24">
            <div
                className="pt-20 pb-24"
                style={{
                    background: `linear-gradient(180deg, var(--hero-top) 0%, var(--hero-mid) 45%, var(--hero-bottom) 100%)`,
                }}
            >
                <Container>
                    <div className="mx-auto max-w-3xl text-center">
                        <h1 className="text-4xl font-extrabold leading-tight">
                            {t("hero.title")}
                        </h1>
                        <p className="mt-4 text-base opacity-80">{t("hero.subtitle")}</p>

                        <div className="mt-8 flex flex-col items-center gap-3 sm:flex-row sm:justify-center">
                            <input
                                value={url}
                                onChange={(e) => setUrl(e.target.value)}
                                placeholder={t("hero.placeholder")}
                                className="w-full sm:w-[520px] rounded-xl border px-4 py-3 text-sm outline-none"
                                style={{
                                    background: "var(--color-surface-strong)",
                                    borderColor: "var(--color-border)",
                                    boxShadow: "var(--shadow-soft)",
                                }}
                            />
                            <button
                                onClick={onGetRecipe}
                                disabled={loading}
                                className="w-full sm:w-auto rounded-xl px-5 py-3 text-sm font-semibold transition hover:-translate-y-[1px] active:translate-y-0 focus:outline-none focus:ring-2 disabled:opacity-60"
                                style={{
                                    background: "var(--color-primary)",
                                    color: "#0b1220",
                                    boxShadow: "var(--shadow-soft)",
                                }}
                            >
                                {loading ? t("api.loading") : t("hero.button")}
                            </button>
                        </div>

                        {finalError && (
                            <div className="mt-3 text-sm text-red-600">
                                {finalError || t("api.genericError")}
                            </div>
                        )}

                        <div className="mt-4 text-xs opacity-70">
                            {t("api.noSignup")}
                        </div>

                        <div className="mt-10">
                            {loading ? (
                                <div className="pop-in">
                                    <RecipeSkeleton />
                                </div>
                            ) : data ? (
                                <div className="pop-in">
                                    <RecipeCard recipe={data} />
                                </div>
                            ) : (
                                <div>

                                </div>
                            )}
                        </div>
                    </div>
                </Container>
            </div>
        </section>
    );
}
