package dev.nexcraft.latch.controlplane.service.organization;

import dev.nexcraft.latch.controlplane.core.organization.error.OrganizationSlugConflictException;
import java.text.Normalizer;
import java.util.Locale;
import java.util.function.Predicate;

/**
 * Generates deterministic URL-safe organization slugs.
 */
public final class SlugGenerator {

    private static final int MAX_ATTEMPTS = 1_000_000;

    /**
     * Generates a unique slug for a display name.
     *
     * @param name display name
     * @param slugExists persistence-backed uniqueness check
     * @return unique slug no longer than 80 characters
     */
    public String generateUnique(String name, Predicate<String> slugExists) {
        String base = normalize(name);
        if (!slugExists.test(base)) {
            return base;
        }
        for (int suffix = 2; suffix <= MAX_ATTEMPTS; suffix++) {
            String suffixText = "-" + suffix;
            String candidate = truncate(base, 80 - suffixText.length()) + suffixText;
            if (!slugExists.test(candidate)) {
                return candidate;
            }
        }
        throw new OrganizationSlugConflictException(base);
    }

    private String normalize(String name) {
        String decomposed = Normalizer.normalize(name.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD);
        StringBuilder slug = new StringBuilder();
        boolean pendingDash = false;
        for (int index = 0; index < decomposed.length(); index++) {
            char character = decomposed.charAt(index);
            if (character >= 'a' && character <= 'z' || character >= '0' && character <= '9') {
                if (pendingDash && !slug.isEmpty()) {
                    slug.append('-');
                }
                slug.append(character);
                pendingDash = false;
            } else if (!slug.isEmpty()) {
                pendingDash = true;
            }
        }
        return slug.isEmpty() ? "organization" : truncate(slug.toString(), 80);
    }

    private String truncate(String value, int maxLength) {
        return value.length() <= maxLength ? value : value.substring(0, maxLength).replaceAll("-$", "");
    }
}
