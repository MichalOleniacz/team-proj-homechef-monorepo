import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import Container from "./Container";
import LanguageToggle from "./LanguageToggle";
import ThemeToggle from "./ThemeToggle";
import { useAuth } from "../auth/AuthProvider";
import AuthModal from "./AuthModal";

function scrollToId(id: string) {
    const el = document.getElementById(id);
    if (!el) return;

    const y = el.getBoundingClientRect().top + window.scrollY - 80;
    window.scrollTo({ top: y, behavior: "smooth" });
}

export default function Navbar() {
    const { t } = useTranslation();

    const [open, setOpen] = useState(false);
    const [authOpen, setAuthOpen] = useState(false);

    const { user, logout } = useAuth();

    useEffect(() => {
        const onResize = () => {
            if (window.innerWidth >= 768) setOpen(false);
        };
        window.addEventListener("resize", onResize);
        return () => window.removeEventListener("resize", onResize);
    }, []);

    const closeMobile = () => setOpen(false);

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

    const AuthButton = ({
                            fullWidth = false,
                            onAfterAction,
                        }: {
        fullWidth?: boolean;
        onAfterAction?: () => void;
    }) => {
        if (user) {
            return (
                <div className={fullWidth ? "w-full" : ""}>
                    <button
                        onClick={async () => {
                            await logout();
                            onAfterAction?.();
                        }}
                        className={[
                            "rounded-full border px-3 py-2 text-sm transition hover:-translate-y-[1px] active:translate-y-0",
                            fullWidth ? "w-full" : "",
                        ].join(" ")}
                        style={{
                            borderColor: "var(--color-border)",
                            background: "var(--color-surface-strong)",
                            boxShadow: "var(--shadow-soft)",
                        }}
                    >
                        {t("auth.logout")}
                    </button>
                </div>
            );
        }

        return (
            <div className={fullWidth ? "w-full" : ""}>
                <button
                    onClick={() => {
                        setAuthOpen(true);
                        onAfterAction?.();
                    }}
                    className={[
                        "rounded-full border px-3 py-2 text-sm transition hover:-translate-y-[1px] active:translate-y-0",
                        fullWidth ? "w-full" : "",
                    ].join(" ")}
                    style={{
                        borderColor: "var(--color-border)",
                        background: "var(--color-surface-strong)",
                        boxShadow: "var(--shadow-soft)",
                    }}
                >
                    {t("auth.open")}
                </button>
            </div>
        );
    };

    return (
        <>
            <header
                className="sticky top-0 z-50"
                style={{
                    background: "rgba(255,255,255,0.65)",
                    backdropFilter: "blur(10px)",
                    borderBottom: "1px solid var(--color-border)",
                }}
            >
                <Container>
                    <div className="grid h-16 items-center grid-cols-[auto_1fr_auto]">
                        <button
                            onClick={() => scrollToId("home")}
                            className="flex items-center gap-2 justify-self-start"
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
                            <span className="font-semibold whitespace-nowrap">Home Chef</span>
                        </button>

                        <nav className="hidden md:flex justify-self-center gap-8 text-sm font-medium">
                            <NavButtons />
                        </nav>

                        <div className="flex items-center justify-self-end gap-2">
                            <div className="hidden md:flex items-center gap-3">
                                <LanguageToggle />
                                <ThemeToggle />
                                <AuthButton />
                            </div>

                            <button
                                onClick={() => setOpen((v) => !v)}
                                className="md:hidden rounded-xl border px-3 py-2 text-sm transition hover:-translate-y-[1px] active:translate-y-0"
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
                                className="mt-2 rounded-2xl border p-4 pop-in"
                                style={{
                                    background: "var(--color-surface-strong)",
                                    borderColor: "var(--color-border)",
                                    boxShadow: "var(--shadow-soft)",
                                    borderRadius: "var(--radius-card)",
                                }}
                            >
                                <div className="flex flex-col gap-3 text-sm font-medium">
                                    <NavButtons onClick={closeMobile} />
                                </div>

                                <div className="mt-4 flex flex-col items-center gap-3">
                                    <LanguageToggle />
                                    <ThemeToggle compact />
                                    <AuthButton fullWidth onAfterAction={closeMobile} />
                                </div>
                            </div>
                        </div>
                    )}
                </Container>
            </header>

            {/* AUTH MODAL */}
            <AuthModal open={authOpen} onClose={() => setAuthOpen(false)} />
        </>
    );
}
