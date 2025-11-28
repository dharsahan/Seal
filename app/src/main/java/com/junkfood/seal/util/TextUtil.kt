package com.junkfood.seal.util

import android.content.Context
import android.widget.Toast
import androidx.annotation.MainThread
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.core.text.isDigitsOnly
import com.junkfood.seal.App
import com.junkfood.seal.App.Companion.applicationScope
import com.junkfood.seal.App.Companion.context
import com.junkfood.seal.R
import java.util.regex.Pattern
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Deprecated("Use extension functions of Context to show a toast")
object ToastUtil {
    fun makeToast(text: String) {
        Toast.makeText(context.applicationContext, text, Toast.LENGTH_SHORT).show()
    }

    fun makeToastSuspend(text: String) {
        applicationScope.launch(Dispatchers.Main) { makeToast(text) }
    }

    fun makeToast(stringId: Int) {
        Toast.makeText(context.applicationContext, context.getString(stringId), Toast.LENGTH_SHORT)
            .show()
    }
}

@MainThread
fun Context.makeToast(stringId: Int) {
    Toast.makeText(applicationContext, getString(stringId), Toast.LENGTH_SHORT).show()
}

@MainThread
fun Context.makeToast(message: String) {
    Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
}

private const val GIGA_BYTES = 1024f * 1024f * 1024f
private const val MEGA_BYTES = 1024f * 1024f

@Composable
fun Number?.toFileSizeText(): String {
    if (this == null) return stringResource(id = R.string.unknown)

    return this.toFloat().run {
        if (this > GIGA_BYTES) stringResource(R.string.filesize_gb).format(this / GIGA_BYTES)
        else stringResource(R.string.filesize_mb).format(this / MEGA_BYTES)
    }
}

/** Convert time in **seconds** to `hh:mm:ss` or `mm:ss` */
fun Int.toDurationText(): String =
    this.run {
        if (this > 3600) "%d:%02d:%02d".format(this / 3600, (this % 3600) / 60, this % 60)
        else "%02d:%02d".format(this / 60, this % 60)
    }

fun String.isNumberInRange(start: Int, end: Int): Boolean {
    return this.isNotEmpty() &&
        this.isDigitsOnly() &&
        this.length < 10 &&
        this.toInt() >= start &&
        this.toInt() <= end
}

private const val URL_REGEX_PATTERN =
    "(http|https)://[\\w\\-_]+(\\.[\\w\\-_]+)+([\\w\\-.,@?^=%&:/~+#]*[\\w\\-@?^=%&/~+#])?"

/** Regex pattern for Instagram Reels URLs */
private const val INSTAGRAM_REEL_REGEX_PATTERN =
    "(http|https)://(www\\.)?instagram\\.com/(reel|reels)/[A-Za-z0-9_-]+"

/** Regex pattern for Instagram Post URLs */
private const val INSTAGRAM_POST_REGEX_PATTERN =
    "(http|https)://(www\\.)?instagram\\.com/p/[A-Za-z0-9_-]+"

/** Regex pattern for Instagram Story URLs */
private const val INSTAGRAM_STORY_REGEX_PATTERN =
    "(http|https)://(www\\.)?instagram\\.com/stories/[A-Za-z0-9._]+/[0-9]+"

/** Regex pattern for any Instagram URL */
private const val INSTAGRAM_URL_REGEX_PATTERN =
    "(http|https)://(www\\.)?instagram\\.com/.*"

fun String.isNumberInRange(range: IntRange): Boolean = this.isNumberInRange(range.first, range.last)

fun ClosedFloatingPointRange<Float>.toIntRange() =
    IntRange(start.roundToInt(), endInclusive.roundToInt())

fun String?.toHttpsUrl(): String =
    this?.run { if (matches(Regex("^(http:).*"))) replaceFirst("http", "https") else this } ?: ""

fun matchUrlFromClipboard(string: String, isMatchingMultiLink: Boolean = false): String {
    findURLsFromString(string, !isMatchingMultiLink).joinToString(separator = "\n").run {
        if (isEmpty()) ToastUtil.makeToast(R.string.paste_fail_msg)
        else ToastUtil.makeToast(R.string.paste_msg)
        return this
    }
}

fun matchUrlFromSharedText(s: String): String {
    findURLsFromString(s, true).joinToString(separator = "\n").run {
        if (isEmpty()) ToastUtil.makeToast(R.string.share_fail_msg)
        //            else makeToast(R.string.share_success_msg)
        return this
    }
}

fun Number?.toBitrateText(): String {
    val br = this?.toFloat() ?: return ""
    return when {
        br <= 0f -> "" // i don't care
        br < 1024f -> "%.1f Kbps".format(br)

        else -> "%.2f Mbps".format(br / 1024f)
    }
}

fun getErrorReport(th: Throwable, url: String): String =
    App.getVersionReport() + "\nURL: ${url}\n${th.message}"

/**
 * Checks if the error message indicates that login/authentication is required.
 * This detects common patterns from yt-dlp errors that suggest the user needs to provide cookies.
 */
fun isLoginRequired(errorMessage: String?): Boolean {
    if (errorMessage.isNullOrEmpty()) return false
    val lowerCaseMessage = errorMessage.lowercase()
    return lowerCaseMessage.contains("login required") ||
        lowerCaseMessage.contains("login page") ||
        lowerCaseMessage.contains("provide account credentials") ||
        lowerCaseMessage.contains("sign in") ||
        lowerCaseMessage.contains("use --cookies") ||
        lowerCaseMessage.contains("authentication required")
}

@Deprecated(
    "Use findURLsFromString instead",
    ReplaceWith("findURLsFromString(s, !isMatchingMultiLink).joinToString(separator = \"\\n\")"),
)
fun matchUrlFromString(s: String, isMatchingMultiLink: Boolean = false): String =
    findURLsFromString(s, !isMatchingMultiLink).joinToString(separator = "\n")

fun findURLsFromString(input: String, firstMatchOnly: Boolean = false): List<String> {
    val result = mutableListOf<String>()
    val pattern = Pattern.compile(URL_REGEX_PATTERN)

    with(pattern.matcher(input)) {
        if (!firstMatchOnly) {
            while (find()) {
                result += group()
            }
        } else {
            if (find()) result += (group())
        }
    }
    return result
}

fun connectWithDelimiter(vararg strings: String?, delimiter: String): String =
    strings
        .toList()
        .filter { !it.isNullOrBlank() }
        .joinToString(separator = delimiter) { it.toString() }

fun connectWithBlank(s1: String, s2: String): String {
    val blank = if (s1.isEmpty() || s2.isEmpty()) "" else " "
    return s1 + blank + s2
}

/** Pre-compiled patterns for Instagram URL detection */
private object InstagramPatterns {
    val instagramUrlPattern: Pattern = Pattern.compile(INSTAGRAM_URL_REGEX_PATTERN)
    val instagramReelPattern: Pattern = Pattern.compile(INSTAGRAM_REEL_REGEX_PATTERN)
    val instagramPostPattern: Pattern = Pattern.compile(INSTAGRAM_POST_REGEX_PATTERN)
    val instagramStoryPattern: Pattern = Pattern.compile(INSTAGRAM_STORY_REGEX_PATTERN)
}

/**
 * Checks if the given URL is an Instagram URL
 *
 * @param url The URL to check
 * @return true if the URL is from Instagram, false otherwise
 */
fun isInstagramUrl(url: String): Boolean {
    return InstagramPatterns.instagramUrlPattern.matcher(url).find()
}

/**
 * Checks if the given URL is an Instagram Reel URL
 *
 * @param url The URL to check
 * @return true if the URL is an Instagram Reel, false otherwise
 */
fun isInstagramReelUrl(url: String): Boolean {
    return InstagramPatterns.instagramReelPattern.matcher(url).find()
}

/**
 * Checks if the given URL is an Instagram Post URL
 *
 * @param url The URL to check
 * @return true if the URL is an Instagram Post, false otherwise
 */
fun isInstagramPostUrl(url: String): Boolean {
    return InstagramPatterns.instagramPostPattern.matcher(url).find()
}

/**
 * Checks if the given URL is an Instagram Story URL
 *
 * @param url The URL to check
 * @return true if the URL is an Instagram Story, false otherwise
 */
fun isInstagramStoryUrl(url: String): Boolean {
    return InstagramPatterns.instagramStoryPattern.matcher(url).find()
}

/**
 * Determines the type of Instagram content from the URL
 *
 * @param url The URL to analyze
 * @return A string describing the Instagram content type: "reel", "post", "story", "other" for
 *   other Instagram URLs, or empty string for non-Instagram URLs
 */
fun getInstagramContentType(url: String): String {
    return when {
        isInstagramReelUrl(url) -> "reel"
        isInstagramPostUrl(url) -> "post"
        isInstagramStoryUrl(url) -> "story"
        isInstagramUrl(url) -> "other"
        else -> ""
    }
}
