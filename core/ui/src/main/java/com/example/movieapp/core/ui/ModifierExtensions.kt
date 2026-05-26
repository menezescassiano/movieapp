package com.example.movieapp.core.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId

fun Modifier.invisible(): Modifier = this.alpha(0f)

/**
 * Sets a [testTag] on this composable so that Appium (UIAutomator2 driver) can locate
 * the element via `By.res("<tag>")` or the `resource-id` accessibility attribute.
 *
 * To activate resource-id mapping, call [enableTestIds] on the root container of each
 * screen (or at the app root level once).
 */
fun Modifier.testId(tag: String): Modifier = this.testTag(tag)

/**
 * Enables [testTagsAsResourceId] for the subtree rooted at this node, which causes
 * every [testTag] to be exposed as the element's `resource-id` — the attribute
 * Appium's UIAutomator2 driver reads with `By.res(...)`.
 *
 * Place this on the outermost container of each screen (or once on the app root).
 */
fun Modifier.enableTestIds(): Modifier = this.semantics { testTagsAsResourceId = true }
