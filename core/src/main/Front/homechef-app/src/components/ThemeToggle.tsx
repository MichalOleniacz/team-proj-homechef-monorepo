import { useTranslation } from "react-i18next";
import { useTheme } from "../hooks/useTheme";

export default function ThemeToggle({ compact = false }: { compact?: boolean }) {
    const { t } = useTranslation();
    const { theme, toggle } = useTheme();

    return (
        <button
            onClick={toggle}
            className={[
                "theme-btn inline-flex items-center gap-2 rounded-full border text-sm",
                "whitespace-nowrap transition",
                "hover:-translate-y-[1px] active:translate-y-0",
                compact ? "px-3 py-2" : "px-4 py-2",
            ].join(" ")}
            style={{
                borderColor: "var(--color-border)",
                background: "var(--color-surface-strong)",
                boxShadow: "var(--shadow-soft)",
            }}
            aria-label="Toggle theme"
            title={t("ui.theme")}
        >
            <span className="opacity-80">{t("ui.theme")}</span>
            <span className="font-semibold">
        {theme === "dark" ? `🌙 ${t("ui.dark")}` : `☀️ ${t("ui.light")}`}
      </span>
        </button>
    );
}
