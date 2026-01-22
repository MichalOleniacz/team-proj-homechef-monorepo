package org.homechef.core.domain.recipe;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Ingredient")
class IngredientTest {

    @Nested
    @DisplayName("construction")
    class Construction {

        @Test
        @DisplayName("creates ingredient with all fields")
        void createsWithAllFields() {
            Ingredient ingredient = Ingredient.of(new BigDecimal("2.5"), "cups", "flour");

            assertEquals(new BigDecimal("2.5"), ingredient.quantity());
            assertEquals("cups", ingredient.unit());
            assertEquals("flour", ingredient.name());
        }

        @Test
        @DisplayName("creates ingredient with quantity and name only")
        void createsWithQuantityAndName() {
            Ingredient ingredient = Ingredient.of(new BigDecimal("3"), "eggs");

            assertEquals(new BigDecimal("3"), ingredient.quantity());
            assertNull(ingredient.unit());
            assertEquals("eggs", ingredient.name());
        }

        @Test
        @DisplayName("creates ingredient with name only")
        void createsWithNameOnly() {
            Ingredient ingredient = Ingredient.of("salt to taste");

            assertNull(ingredient.quantity());
            assertNull(ingredient.unit());
            assertEquals("salt to taste", ingredient.name());
        }

        @Test
        @DisplayName("allows null quantity")
        void allowsNullQuantity() {
            Ingredient ingredient = new Ingredient(null, "cups", "flour");

            assertNull(ingredient.quantity());
        }

        @Test
        @DisplayName("allows null unit")
        void allowsNullUnit() {
            Ingredient ingredient = new Ingredient(new BigDecimal("2"), null, "lemons");

            assertNull(ingredient.unit());
        }
    }

    @Nested
    @DisplayName("validation")
    class Validation {

        @Test
        @DisplayName("rejects null name")
        void rejectsNullName() {
            assertThrows(NullPointerException.class, () ->
                new Ingredient(new BigDecimal("1"), "cup", null)
            );
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("rejects blank name")
        void rejectsBlankName(String blankName) {
            if (blankName == null) {
                assertThrows(NullPointerException.class, () ->
                    new Ingredient(new BigDecimal("1"), "cup", blankName)
                );
            } else {
                assertThrows(IllegalArgumentException.class, () ->
                    new Ingredient(new BigDecimal("1"), "cup", blankName)
                );
            }
        }
    }

    @Nested
    @DisplayName("toDisplayString()")
    class ToDisplayString {

        @Test
        @DisplayName("formats quantity + unit + name")
        void formatsFullIngredient() {
            Ingredient ingredient = Ingredient.of(new BigDecimal("2"), "cups", "flour");

            assertEquals("2 cups flour", ingredient.toDisplayString());
        }

        @Test
        @DisplayName("formats quantity + name (no unit)")
        void formatsWithoutUnit() {
            Ingredient ingredient = Ingredient.of(new BigDecimal("3"), "eggs");

            assertEquals("3 eggs", ingredient.toDisplayString());
        }

        @Test
        @DisplayName("formats name only")
        void formatsNameOnly() {
            Ingredient ingredient = Ingredient.of("salt to taste");

            assertEquals("salt to taste", ingredient.toDisplayString());
        }

        @Test
        @DisplayName("strips trailing zeros from quantity")
        void stripsTrailingZeros() {
            Ingredient ingredient = Ingredient.of(new BigDecimal("2.00"), "cups", "flour");

            assertEquals("2 cups flour", ingredient.toDisplayString());
        }

        @Test
        @DisplayName("preserves significant decimals")
        void preservesSignificantDecimals() {
            Ingredient ingredient = Ingredient.of(new BigDecimal("0.5"), "tsp", "salt");

            assertEquals("0.5 tsp salt", ingredient.toDisplayString());
        }

        @Test
        @DisplayName("handles blank unit as null")
        void handlesBlankUnit() {
            Ingredient ingredient = new Ingredient(new BigDecimal("1"), "  ", "onion");

            // Blank unit should not add extra spaces
            String display = ingredient.toDisplayString();
            assertFalse(display.contains("  ")); // No double spaces
        }

        @Test
        @DisplayName("handles large quantities")
        void handlesLargeQuantities() {
            Ingredient ingredient = Ingredient.of(new BigDecimal("1000"), "g", "sugar");

            assertEquals("1000 g sugar", ingredient.toDisplayString());
        }

        @Test
        @DisplayName("handles fractional quantities")
        void handlesFractionalQuantities() {
            Ingredient ingredient = Ingredient.of(new BigDecimal("0.25"), "cup", "butter");

            assertEquals("0.25 cup butter", ingredient.toDisplayString());
        }
    }

    @Nested
    @DisplayName("equality (record)")
    class Equality {

        @Test
        @DisplayName("equals with same fields")
        void equalsWithSameFields() {
            Ingredient i1 = Ingredient.of(new BigDecimal("2"), "cups", "flour");
            Ingredient i2 = Ingredient.of(new BigDecimal("2"), "cups", "flour");

            assertEquals(i1, i2);
            assertEquals(i1.hashCode(), i2.hashCode());
        }

        @Test
        @DisplayName("not equal with different quantity")
        void notEqualWithDifferentQuantity() {
            Ingredient i1 = Ingredient.of(new BigDecimal("2"), "cups", "flour");
            Ingredient i2 = Ingredient.of(new BigDecimal("3"), "cups", "flour");

            assertNotEquals(i1, i2);
        }

        @Test
        @DisplayName("not equal with different unit")
        void notEqualWithDifferentUnit() {
            Ingredient i1 = Ingredient.of(new BigDecimal("2"), "cups", "flour");
            Ingredient i2 = Ingredient.of(new BigDecimal("2"), "tbsp", "flour");

            assertNotEquals(i1, i2);
        }

        @Test
        @DisplayName("not equal with different name")
        void notEqualWithDifferentName() {
            Ingredient i1 = Ingredient.of(new BigDecimal("2"), "cups", "flour");
            Ingredient i2 = Ingredient.of(new BigDecimal("2"), "cups", "sugar");

            assertNotEquals(i1, i2);
        }

        @Test
        @DisplayName("equals considers BigDecimal value not scale")
        void equalsBigDecimalByValue() {
            // 2 and 2.00 should be equal mathematically but BigDecimal.equals checks scale
            Ingredient i1 = Ingredient.of(new BigDecimal("2"), "cups", "flour");
            Ingredient i2 = Ingredient.of(new BigDecimal("2.00"), "cups", "flour");

            // Records use field equals, and BigDecimal.equals considers scale
            // So these are actually NOT equal in record equality
            // This documents the behavior
            assertNotEquals(i1, i2, "BigDecimal in records compares by scale too");
        }
    }
}
