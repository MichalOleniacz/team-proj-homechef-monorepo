import { useTranslation } from "react-i18next";
import Container from "../components/Container";
import { useInView } from "../hooks/useInView";

export default function AboutSection() {
    const { t } = useTranslation();
    const { ref, inView } = useInView<HTMLDivElement>();

    return (
        <section id="about" className="scroll-mt-24 py-24">
            <Container>
                <div
                    ref={ref}
                    className={`reveal ${inView ? "is-visible" : ""}`}
                >
                    <div
                        className="rounded-2xl border p-8 transition
                       hover:-translate-y-1 hover:scale-[1.01]"
                        style={{
                            background: "var(--color-surface-strong)",
                            borderColor: "var(--color-border)",
                            boxShadow: "var(--shadow-card)",
                            borderRadius: "var(--radius-card)",
                        }}
                    >
                        <h2 className="text-4xl font-extrabold">{t("about.title")}</h2>
                        <p className="mt-4 text-sm opacity-80 max-w-3xl">{t("about.text")}</p>
                    </div>
                </div>
            </Container>
        </section>
    );
}
