import Container from "../components/Container";

export default function Footer() {
    return (
        <footer
            className="py-10"
            style={{ borderTop: "1px solid var(--color-border)" }}
        >
            <Container>
                <div className="text-sm opacity-70">
                    © {new Date().getFullYear()} ChomeChef — frontend demo
                </div>
            </Container>
        </footer>
    );
}
