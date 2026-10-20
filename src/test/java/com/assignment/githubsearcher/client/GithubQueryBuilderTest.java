package com.assignment.githubsearcher.client;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class GithubQueryBuilderTest {
    @Test
    void shouldBuildNameOnlyQueryWhenLanguageIsNull() {
        assertThat(GithubQueryBuilder.build("spring boot", null)).isEqualTo("spring boot in:name");
    }

    @Test
    void shouldIgnoreBlankLanguage() {
        assertThat(GithubQueryBuilder.build("spring boot", "   ")).isEqualTo("spring boot in:name");
    }

    @Test
    void shouldAppendLanguage() {
        assertThat(GithubQueryBuilder.build("spring boot", "Java")).isEqualTo("spring boot in:name language:Java");
    }

    @Test
    void shouldQuoteLanguageContainingSpaces() {
        assertThat(GithubQueryBuilder.build("notebook", "Jupyter Notebook")).isEqualTo("notebook in:name language:\"Jupyter Notebook\"");
    }

    @Test
    void shouldTrimQueryAndLanguage() {
        assertThat(GithubQueryBuilder.build("  spring  ", "  Java ")).isEqualTo("spring in:name language:Java");
    }
}