export function disableTransitionsTemporarily() {
    const style = document.createElement("style");
    style.innerHTML = `
    * {
      transition: none !important;
    }
  `;
    document.head.appendChild(style);

    requestAnimationFrame(() => {
        requestAnimationFrame(() => {
            document.head.removeChild(style);
        });
    });
}
