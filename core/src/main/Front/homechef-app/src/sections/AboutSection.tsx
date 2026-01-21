import { useTranslation } from "react-i18next";
import Container from "../components/Container";

export default function AboutSection() {
    const { t } = useTranslation();

    return (
        <section id="about" className="scroll-mt-24 py-16">
            <Container>
                <div
                    className="rounded-2xl border p-8"
                    style={{
                        background: "var(--color-surface)",
                        borderColor: "var(--color-border)",
                        boxShadow: "var(--shadow-card)",
                        borderRadius: "var(--radius-card)"
                    }}
                >
                    <h2 className="text-3xl font-extrabold">{t("about.title")}</h2>
                    <p className="mt-4 text-sm opacity-80 max-w-3xl">{t("about.text")}</p>
                </div>
            </Container>
        </section>
    );
}
