import { useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import Container from "../components/Container";
import RecipeCard from "../components/RecipeCard";
import RecipeSkeleton from "../components/RecipeSkeleton";
import { useParseRecipe } from "../hooks/useParseRecipe";
import type { RecipeStatus } from "../types/recipe";

function isValidUrl(url: string) {
    try {
        const u = new URL(url);
        return u.protocol === "http:" || u.protocol === "https:";
    } catch {
        return false;
    }
}

function isBusy(status?: RecipeStatus) {
    return status === "PENDING" || status === "PROCESSING";
}

export default function HomeSection() {
    const { t } = useTranslation();

    const [url, setUrl] = useState("");
    const [localError, setLocalError] = useState<string | null>(null);
    const [hasSubmitted, setHasSubmitted] = useState(false);

    const { data, loading, error, run, reset } = useParseRecipe();

    const status = data?.status;
    const requestId = data?.requestId;
    const recipe = data && "recipe" in data ? data.recipe : undefined;

    const showRecipe = status === "COMPLETED" && !!recipe;
    const showPending = !!status && isBusy(status);
    const showFailed = status === "FAILED";

    const finalError = useMemo(() => {
        if (localError) return localError;
        if (showFailed) return (data && "error" in data ? data.error : null) ?? t("api.failed");
        return error ?? null;
    }, [localError, showFailed, data, error, t]);

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
        setHasSubmitted(true);

        await run(trimmed);
    };

    const onReset = () => {
        reset();
        setLocalError(null);
        setHasSubmitted(false);
    };

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
                        <h1 className="text-4xl font-extrabold leading-tight">{t("hero.title")}</h1>
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
                                onKeyDown={(e) => {
                                    if (e.key === "Enter") onGetRecipe();
                                }}
                            />

                            <div className="flex w-full sm:w-auto gap-2">
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

                                {(data || finalError) && (
                                    <button
                                        onClick={onReset}
                                        className="w-full sm:w-auto rounded-xl border px-4 py-3 text-sm font-semibold transition hover:-translate-y-[1px] active:translate-y-0"
                                        style={{
                                            background: "var(--color-surface-strong)",
                                            borderColor: "var(--color-border)",
                                            boxShadow: "var(--shadow-soft)",
                                        }}
                                    >
                                        {t("api.reset")}
                                    </button>
                                )}
                            </div>
                        </div>

                        {finalError && <div className="mt-3 text-sm text-red-600">{finalError || t("api.genericError")}</div>}

                        <div className="mt-4 text-xs opacity-70">{t("api.noSignup")}</div>

                        <div className="mt-10">
                            {loading ? (
                                <div className="pop-in">
                                    <RecipeSkeleton />
                                </div>
                            ) : showPending ? (
                                <div
                                    className="mx-auto max-w-xl rounded-2xl border p-6 text-left pop-in"
                                    style={{
                                        background: "var(--color-surface-strong)",
                                        borderColor: "var(--color-border)",
                                        boxShadow: "var(--shadow-soft)",
                                        borderRadius: "var(--radius-card)",
                                    }}
                                >
                                    <div className="text-sm font-semibold mb-2">
                                        {status === "PROCESSING" ? t("api.processing") : t("api.pending")}
                                    </div>

                                    <div className="text-sm opacity-70">{t("api.pendingHint")}</div>

                                    {requestId && <div className="mt-3 text-xs opacity-70 break-all">requestId: {requestId}</div>}
                                </div>
                            ) : showRecipe ? (
                                <div className="pop-in">
                                    <RecipeCard recipe={recipe!} />
                                </div>
                            ) : hasSubmitted ? (
                                <div
                                    className="mx-auto max-w-xl rounded-2xl border p-6 text-left pop-in"
                                    style={{
                                        background: "var(--color-surface-strong)",
                                        borderColor: "var(--color-border)",
                                        boxShadow: "var(--shadow-soft)",
                                        borderRadius: "var(--radius-card)",
                                    }}
                                >
                                    <div className="text-sm font-semibold mb-2">{t("api.resultHintTitle")}</div>
                                    <div className="text-sm opacity-70">{t("api.resultHintText")}</div>
                                </div>
                            ) : null}
                        </div>
                    </div>
                </Container>
            </div>
        </section>
    );
}
