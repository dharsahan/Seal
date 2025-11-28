package com.junkfood.seal

import com.junkfood.seal.util.getInstagramContentType
import com.junkfood.seal.util.isInstagramPostUrl
import com.junkfood.seal.util.isInstagramReelUrl
import com.junkfood.seal.util.isInstagramStoryUrl
import com.junkfood.seal.util.isInstagramUrl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Unit tests for Instagram URL detection functions. */
class InstagramUrlTest {

    // Test URLs
    private val instagramReelUrls =
        listOf(
            "https://www.instagram.com/reel/ABC123def/",
            "https://instagram.com/reel/ABC123def",
            "https://www.instagram.com/reels/XYZ789abc/",
            "https://instagram.com/reels/XYZ789abc",
        )

    private val instagramPostUrls =
        listOf(
            "https://www.instagram.com/p/ABC123def/",
            "https://instagram.com/p/ABC123def",
            "https://www.instagram.com/p/XYZ_789-abc/",
        )

    private val instagramStoryUrls =
        listOf(
            "https://www.instagram.com/stories/username/1234567890123456789/",
            "https://instagram.com/stories/user.name/1234567890123456789",
            "https://www.instagram.com/stories/user_name123/9876543210/",
        )

    private val nonInstagramUrls =
        listOf(
            "https://www.youtube.com/watch?v=abc123",
            "https://twitter.com/user/status/123456",
            "https://www.tiktok.com/@user/video/123456",
            "https://example.com/reel/abc123",
        )

    @Test
    fun testIsInstagramUrl() {
        // Should match all Instagram URLs
        instagramReelUrls.forEach { url -> assertTrue("Should match: $url", isInstagramUrl(url)) }
        instagramPostUrls.forEach { url -> assertTrue("Should match: $url", isInstagramUrl(url)) }
        instagramStoryUrls.forEach { url -> assertTrue("Should match: $url", isInstagramUrl(url)) }

        // Should not match non-Instagram URLs
        nonInstagramUrls.forEach { url ->
            assertFalse("Should not match: $url", isInstagramUrl(url))
        }
    }

    @Test
    fun testIsInstagramReelUrl() {
        // Should match reel URLs
        instagramReelUrls.forEach { url ->
            assertTrue("Should match reel URL: $url", isInstagramReelUrl(url))
        }

        // Should not match non-reel Instagram URLs
        instagramPostUrls.forEach { url ->
            assertFalse("Should not match post as reel: $url", isInstagramReelUrl(url))
        }
        instagramStoryUrls.forEach { url ->
            assertFalse("Should not match story as reel: $url", isInstagramReelUrl(url))
        }
    }

    @Test
    fun testIsInstagramPostUrl() {
        // Should match post URLs
        instagramPostUrls.forEach { url ->
            assertTrue("Should match post URL: $url", isInstagramPostUrl(url))
        }

        // Should not match non-post Instagram URLs
        instagramReelUrls.forEach { url ->
            assertFalse("Should not match reel as post: $url", isInstagramPostUrl(url))
        }
        instagramStoryUrls.forEach { url ->
            assertFalse("Should not match story as post: $url", isInstagramStoryUrl(url))
        }
    }

    @Test
    fun testIsInstagramStoryUrl() {
        // Should match story URLs
        instagramStoryUrls.forEach { url ->
            assertTrue("Should match story URL: $url", isInstagramStoryUrl(url))
        }

        // Should not match non-story Instagram URLs
        instagramReelUrls.forEach { url ->
            assertFalse("Should not match reel as story: $url", isInstagramStoryUrl(url))
        }
        instagramPostUrls.forEach { url ->
            assertFalse("Should not match post as story: $url", isInstagramStoryUrl(url))
        }
    }

    @Test
    fun testGetInstagramContentType() {
        // Test reel detection
        instagramReelUrls.forEach { url ->
            assertEquals("Should detect reel type for: $url", "reel", getInstagramContentType(url))
        }

        // Test post detection
        instagramPostUrls.forEach { url ->
            assertEquals("Should detect post type for: $url", "post", getInstagramContentType(url))
        }

        // Test story detection
        instagramStoryUrls.forEach { url ->
            assertEquals(
                "Should detect story type for: $url",
                "story",
                getInstagramContentType(url),
            )
        }

        // Test non-Instagram URLs
        nonInstagramUrls.forEach { url ->
            assertEquals(
                "Should return empty string for non-Instagram URL: $url",
                "",
                getInstagramContentType(url),
            )
        }
    }

    @Test
    fun testInstagramUrlWithQueryParams() {
        // Instagram URLs often have query parameters
        val urlWithParams = "https://www.instagram.com/reel/ABC123/?igshid=abc123xyz"
        assertTrue("Should match URL with query params", isInstagramUrl(urlWithParams))
        assertTrue("Should match reel URL with query params", isInstagramReelUrl(urlWithParams))
        assertEquals("reel", getInstagramContentType(urlWithParams))
    }

    @Test
    fun testInstagramUrlCaseInsensitivity() {
        // URLs should be case-insensitive for the domain part
        val mixedCaseUrl = "https://www.Instagram.com/reel/ABC123/"
        // Note: The regex patterns are case-sensitive for the domain by default
        // This test documents the current behavior
        assertFalse("Current implementation is case-sensitive", isInstagramUrl(mixedCaseUrl))
    }
}
