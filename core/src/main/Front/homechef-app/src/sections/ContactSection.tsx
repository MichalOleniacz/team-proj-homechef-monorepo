import { useState, type FormEvent } from "react";
import { useTranslation } from "react-i18next";
import Container from "../components/Container";
import { useInView } from "../hooks/useInView";

export default function ContactSection() {
    const { t } = useTranslation();
    const { ref, inView } = useInView<HTMLDivElement>();

    const [email, setEmail] = useState("");
    const [msg, setMsg] = useState("");

    const onSubmit = (e: FormEvent) => {
        e.preventDefault();
        alert("This is just a DEMO dude xd");
        setEmail("");
        setMsg("");
    };

    return (
        <section id="contact" className="scroll-mt-24 py-24">
            <Container>
                <div
                    ref={ref}
                    className={`reveal ${inView ? "is-visible" : ""}`}
                >
                    <div className="grid gap-10 md:grid-cols-2 md:items-start">
                        <div>
                            <h2 className="text-4xl font-extrabold">{t("contact.title")}</h2>
                            <p className="mt-3 text-sm opacity-80">{t("contact.text")}</p>
                            <p className="mt-6 text-sm opacity-70">{t("contact.note")}</p>
                        </div>

                        <form
                            onSubmit={onSubmit}
                            className="rounded-2xl border p-6 transition
                         hover:-translate-y-1 hover:scale-[1.01]"
                            style={{
                                background: "var(--color-surface-strong)",
                                borderColor: "var(--color-border)",
                                boxShadow: "var(--shadow-card)",
                                borderRadius: "var(--radius-card)",
                            }}
                        >
                            <label className="block text-sm font-semibold">
                                {t("contact.emailLabel")}
                            </label>
                            <input
                                className="mt-2 w-full rounded-xl border px-4 py-3 text-sm outline-none focus:ring-2 transition"
                                style={{
                                    background: "var(--color-bg)",
                                    borderColor: "var(--color-border)",
                                }}
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                type="email"
                                required
                            />

                            <label className="mt-5 block text-sm font-semibold">
                                {t("contact.messageLabel")}
                            </label>
                            <textarea
                                className="mt-2 w-full rounded-xl border px-4 py-3 text-sm outline-none focus:ring-2 transition"
                                style={{
                                    background: "var(--color-bg)",
                                    borderColor: "var(--color-border)",
                                }}
                                value={msg}
                                onChange={(e) => setMsg(e.target.value)}
                                rows={5}
                                required
                            />

                            <button
                                type="submit"
                                className="mt-5 w-full rounded-xl px-5 py-3 text-sm font-semibold transition
                           hover:-translate-y-[1px] active:translate-y-0 disabled:opacity-60"
                                style={{
                                    background: "var(--color-primary)",
                                    color: "#0b1220",
                                    boxShadow: "var(--shadow-soft)",
                                }}
                            >
                                {t("contact.send")}
                            </button>
                        </form>
                    </div>
                </div>
            </Container>
        </section>
    );
}
