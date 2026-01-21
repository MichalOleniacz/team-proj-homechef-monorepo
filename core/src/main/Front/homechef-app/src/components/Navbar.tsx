import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import Container from "./Container";
import LanguageToggle from "./LanguageToggle";
import ThemeToggle from "./ThemeToggle";

function scrollToId(id: string) {
    const el = document.getElementById(id);
    if (!el) return;

    const y = el.getBoundingClientRect().top + window.scrollY - 80;
    window.scrollTo({ top: y, behavior: "smooth" });
}

export default function Navbar() {
    const { t } = useTranslation();
    const [open, setOpen] = useState(false);

    useEffect(() => {
        const onResize = () => {
            if (window.innerWidth >= 768) setOpen(false);
        };
        window.addEventListener("resize", onResize);
        return () => window.removeEventListener("resize", onResize);
    }, []);

    const NavButtons = ({ onClick }: { onClick?: () => void }) => (
        <>
            <button
                onClick={() => {
                    scrollToId("home");
                    onClick?.();
                }}
                className="opacity-80 hover:opacity-100 transition"
            >
                {t("nav.home")}
            </button>
            <button
                onClick={() => {
                    scrollToId("how");
                    onClick?.();
                }}
                className="opacity-80 hover:opacity-100 transition"
            >
                {t("nav.how")}
            </button>
            <button
                onClick={() => {
                    scrollToId("about");
                    onClick?.();
                }}
                className="opacity-80 hover:opacity-100 transition"
            >
                {t("nav.about")}
            </button>
            <button
                onClick={() => {
                    scrollToId("contact");
                    onClick?.();
                }}
                className="opacity-80 hover:opacity-100 transition"
            >
                {t("nav.contact")}
            </button>
        </>
    );

    return (
        <header
            className="sticky top-0 z-50"
            style={{
                background: "rgba(255,255,255,0.65)",
                backdropFilter: "blur(10px)",
                borderBottom: "1px solid var(--color-border)",
            }}
        >
            <Container>
                <div className="relative flex h-16 items-center">
                    <button
                        onClick={() => scrollToId("home")}
                        className="flex items-center gap-2"
                        aria-label="Go to top"
                    >
            <span
                className="flex h-9 w-9 items-center justify-center rounded-xl text-lg"
                style={{
                    background: "var(--color-surface-strong)",
                    boxShadow: "var(--shadow-soft)",
                }}
            >
              🍳
            </span>
                        <span className="font-semibold">Home Chef</span>
                    </button>

                    <nav className="absolute left-1/2 -translate-x-1/2 hidden md:flex gap-8 text-sm font-medium">
                        <NavButtons />
                    </nav>

                    <div className="ml-auto hidden md:flex items-center gap-3">
                        <LanguageToggle />
                        <ThemeToggle />
                    </div>

                    <div className="ml-auto flex items-center gap-2 md:hidden">
                        <button
                            onClick={() => setOpen((v) => !v)}
                            className="rounded-xl border px-3 py-2 text-sm"
                            style={{
                                borderColor: "var(--color-border)",
                                background: "var(--color-surface-strong)",
                                boxShadow: "var(--shadow-soft)",
                            }}
                            aria-label="Open menu"
                        >
                            {open ? "✕" : "☰"}
                        </button>
                    </div>
                </div>

                {open && (
                    <div className="md:hidden pb-4">
                        <div
                            className="mt-2 rounded-2xl border p-4"
                            style={{
                                background: "var(--color-surface-strong)",
                                borderColor: "var(--color-border)",
                                boxShadow: "var(--shadow-soft)",
                                borderRadius: "var(--radius-card)",
                            }}
                        >
                            <div className="flex flex-col gap-3 text-sm font-medium">
                                <NavButtons onClick={() => setOpen(false)} />
                            </div>

                            <div className="mt-4 flex flex-col items-center gap-3">
                                <LanguageToggle />
                                <ThemeToggle compact />
                            </div>
                        </div>
                    </div>
                )}
            </Container>
        </header>
    );
}
