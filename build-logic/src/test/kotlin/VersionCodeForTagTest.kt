import org.junit.Assert.assertEquals
import org.junit.Test

/** [versionCodeFor] is the one place a wrong number is invisible until a store rejects it. */
class VersionCodeForTagTest {

    @Test
    fun `a release tag encodes major, minor and patch two digits each`() {
        assertEquals(10203, versionCodeFor("1.2.3"))
    }

    @Test
    fun `a hotfix tag sorts above its base version and below the next minor`() {
        val hotfix = versionCodeFor("1.2.1")
        val base = versionCodeFor("1.2.0")
        val nextMinor = versionCodeFor("1.3.0")
        assertEquals(true, base < hotfix && hotfix < nextMinor)
    }

    @Test
    fun `a build not on a tag falls back to the constant`() {
        assertEquals(ProjectConfig.VERSION_CODE, versionCodeFor(ProjectConfig.VERSION_NAME))
    }

    @Test
    fun `a malformed tag falls back to the constant`() {
        assertEquals(ProjectConfig.VERSION_CODE, versionCodeFor("1.2"))
        assertEquals(ProjectConfig.VERSION_CODE, versionCodeFor("1.2.3.4"))
        assertEquals(ProjectConfig.VERSION_CODE, versionCodeFor("1.2.beta"))
        assertEquals(ProjectConfig.VERSION_CODE, versionCodeFor("1.100.0"))
    }
}
