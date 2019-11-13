package io.quarkus.code.quarkus

import io.quarkus.test.junit.QuarkusTest
import io.quarkus.code.quarkus.model.CodeQuarkusExtension
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.nio.file.Path
import java.util.function.Function

@QuarkusTest
internal class QuarkusExtensionCatalogTest {

    @Test
    internal fun testFields() {
        assertThat(QuarkusExtensionCatalog.platformVersion, not(emptyOrNullString()))
        assertThat(QuarkusExtensionCatalog.bundledQuarkusVersion, not(emptyOrNullString()))
        assertThat(QuarkusExtensionCatalog.descriptor, notNullValue())
        assertThat(QuarkusExtensionCatalog.processedExtensions, not(empty<CodeQuarkusExtension>()))
    }
}