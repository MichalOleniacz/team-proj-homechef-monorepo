package org.homechef.core.domain.recipe;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Value Object representing a single ingredient.
 * Immutable. Used within Recipe.ingredients JSONB.
 */
public record Ingredient(
        BigDecimal quantity,
        String unit,
        String name
) {
    public Ingredient {
        Objects.requireNonNull(name, "Ingredient name cannot be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("Ingredient name cannot be blank");
        }
        // quantity and unit can be null (e.g., "salt to taste", "1 lemon")
    }

    /**
     * Creates an ingredient with all fields.
     */
    public static Ingredient of(BigDecimal quantity, String unit, String name) {
        return new Ingredient(quantity, unit, name);
    }

    /**
     * Creates an ingredient with quantity and name only (no unit).
     */
    public static Ingredient of(BigDecimal quantity, String name) {
        return new Ingredient(quantity, null, name);
    }

    /**
     * Creates an ingredient with name only (e.g., "salt to taste").
     */
    public static Ingredient of(String name) {
        return new Ingredient(null, null, name);
    }

    /**
     * Returns a human-readable representation (e.g., "2 cups flour").
     */
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();
        if (quantity != null) {
            sb.append(quantity.stripTrailingZeros().toPlainString());
        }
        if (unit != null && !unit.isBlank()) {
            if (!sb.isEmpty()) sb.append(" ");
            sb.append(unit);
        }
        if (!sb.isEmpty()) sb.append(" ");
        sb.append(name);
        return sb.toString().trim();
    }
}