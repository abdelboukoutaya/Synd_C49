package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.i18n.LocalStrings
import com.example.ui.theme.LocalSyndicatTokens

/**
 * Icône officielle de l'application SYNDICAT (Éditeur DYNIC) présentée dans un médaillon arrondi.
 */
@Composable
fun SyndicatBrandIconBadge(
  size: Dp = 44.dp,
  cornerRadius: Dp = 12.dp,
  modifier: Modifier = Modifier,
) {
  val strings = LocalStrings.current
  val tokens = LocalSyndicatTokens.current
  val shape = RoundedCornerShape(cornerRadius)

  Surface(
    shape = shape,
    color = tokens.cardBg,
    border = BorderStroke(1.dp, tokens.cardBorder),
    tonalElevation = 0.dp,
    shadowElevation = 0.dp,
    modifier = modifier.size(size),
  ) {
    Image(
      painter = painterResource(id = R.drawable.img_syndicat_icon),
      contentDescription = "${strings.appBrandName} — ${strings.appPublisherShort}",
      contentScale = ContentScale.Crop,
      modifier = Modifier
        .fillMaxSize()
        .clip(shape),
    )
  }
}

/**
 * Bandeau compact d'identité applicative : Icône + SYNDICAT · Éditeur DYNIC + badge Version 2.0 · 2026.
 */
@Composable
fun SyndicatTopBrandBar(
  modifier: Modifier = Modifier,
  tag: String = "dashboard_app_brand_bar",
  statusLabel: String? = null,
  statusIsWarning: Boolean = false,
  statusIsSynced: Boolean = false,
  onStatusClick: (() -> Unit)? = null,
  onSearchClick: (() -> Unit)? = null,
  isPrivacyMode: Boolean = false,
  onTogglePrivacy: (() -> Unit)? = null,
  showBrandHeader: Boolean = true,
) {
  val strings = LocalStrings.current
  val tokens = LocalSyndicatTokens.current

  if (!showBrandHeader) {
    Row(
      modifier = modifier
        .fillMaxWidth()
        .testTag(tag),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      if (onSearchClick != null) {
        val pillShape = RoundedCornerShape(100.dp)
        Surface(
          shape = pillShape,
          color = tokens.cardBg,
          border = BorderStroke(tokens.borderStrokeDp.dp, tokens.cardBorder),
          tonalElevation = 0.dp,
          shadowElevation = 0.dp,
          modifier = Modifier
            .weight(1f)
            .minimumInteractiveComponentSize()
            .heightIn(min = 48.dp)
            .clip(pillShape)
            .clickable(onClick = onSearchClick)
            .testTag("${tag}_search_btn"),
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Icon(
              imageVector = Icons.Filled.Search,
              contentDescription = strings.globalSearchTitle,
              tint = tokens.mute,
              modifier = Modifier.size(18.dp),
            )
            Text(
              text = strings.globalSearchHint,
              style = MaterialTheme.typography.bodySmall,
              color = tokens.mute,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }
      } else {
        Spacer(modifier = Modifier.weight(1f))
      }

      if (statusIsWarning && !statusLabel.isNullOrBlank()) {
        val warnShape = RoundedCornerShape(100.dp)
        Surface(
          shape = warnShape,
          color = tokens.warning.copy(alpha = 0.16f),
          border = BorderStroke(1.dp, tokens.warning.copy(alpha = 0.45f)),
          modifier = Modifier
            .minimumInteractiveComponentSize()
            .heightIn(min = 48.dp)
            .clip(warnShape)
            .let { if (onStatusClick != null) it.clickable(onClick = onStatusClick) else it }
            .testTag("${tag}_status_pill"),
        ) {
          Box(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Filled.WarningAmber,
              contentDescription = statusLabel,
              tint = tokens.warningContent,
              modifier = Modifier.size(18.dp),
            )
          }
        }
      }

      if (onTogglePrivacy != null) {
        val eyeShape = RoundedCornerShape(100.dp)
        Surface(
          shape = eyeShape,
          color = if (isPrivacyMode) tokens.primaryTint else tokens.cardBg,
          border = BorderStroke(
            tokens.borderStrokeDp.dp,
            if (isPrivacyMode) tokens.linkColor.copy(alpha = 0.45f) else tokens.cardBorder,
          ),
          tonalElevation = 0.dp,
          shadowElevation = 0.dp,
          modifier = Modifier
            .minimumInteractiveComponentSize()
            .heightIn(min = 48.dp)
            .clip(eyeShape)
            .clickable(onClick = onTogglePrivacy)
            .testTag("${tag}_privacy_btn"),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            Icon(
              imageVector = if (isPrivacyMode) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
              contentDescription = if (isPrivacyMode) strings.privacyToggleShowDesc else strings.privacyToggleHideDesc,
              tint = if (isPrivacyMode) tokens.linkColor else tokens.inkDeep,
              modifier = Modifier.size(18.dp),
            )
            if (isPrivacyMode) {
              Text(
                text = "••••",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = tokens.linkColor,
              )
            }
          }
        }
      }
    }
    return
  }

  Surface(
    shape = RoundedCornerShape(tokens.contentCardRadiusDp.dp),
    color = tokens.cardBg,
    border = BorderStroke(tokens.borderStrokeDp.dp, tokens.cardBorder),
    tonalElevation = 0.dp,
    shadowElevation = 0.dp,
    modifier = modifier
      .fillMaxWidth()
      .testTag(tag),
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      SyndicatBrandIconBadge(
        size = 42.dp,
        cornerRadius = 11.dp,
        modifier = Modifier.testTag("${tag}_icon"),
      )

      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(2.dp),
      ) {
        Text(
          text = strings.appBrandName,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = tokens.inkDeep,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Text(
          text = strings.appPublisherLabel,
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Medium,
          color = tokens.body,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }

      if (!statusLabel.isNullOrBlank()) {
        val pillBg = when {
          statusIsWarning -> tokens.warning.copy(alpha = 0.16f)
          statusIsSynced -> tokens.positive.copy(alpha = 0.14f)
          else -> tokens.surfaceSoft
        }
        val pillFg = when {
          statusIsWarning -> tokens.warningContent
          statusIsSynced -> tokens.positiveDeep
          else -> tokens.body
        }
        val pillIcon = when {
          statusIsWarning -> Icons.Filled.WarningAmber
          statusIsSynced -> Icons.Filled.CloudDone
          else -> Icons.Filled.PhoneAndroid
        }
        val pillShape = RoundedCornerShape(tokens.badgeRadiusDp.dp)
        Surface(
          shape = pillShape,
          color = pillBg,
          border = BorderStroke(1.dp, if (statusIsWarning) tokens.warning.copy(alpha = 0.45f) else tokens.divider),
          tonalElevation = 0.dp,
          shadowElevation = 0.dp,
          modifier = Modifier
            .clip(pillShape)
            .let { if (onStatusClick != null) it.clickable(onClick = onStatusClick) else it }
            .testTag("${tag}_status_pill"),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Icon(
              imageVector = pillIcon,
              contentDescription = statusLabel,
              tint = pillFg,
              modifier = Modifier.size(13.dp),
            )
            Text(
              text = statusLabel,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.SemiBold,
              color = pillFg,
              maxLines = 1,
            )
          }
        }
      }

      Surface(
        shape = RoundedCornerShape(tokens.badgeRadiusDp.dp),
        color = tokens.primaryTint,
        border = BorderStroke(1.dp, tokens.linkColor.copy(alpha = 0.22f)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = Modifier.testTag("${tag}_version_pill"),
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
          Icon(
            imageVector = Icons.Filled.Verified,
            contentDescription = null,
            tint = tokens.linkColor,
            modifier = Modifier.size(14.dp),
          )
          Text(
            text = strings.appVersionCompactPill,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = tokens.linkColor,
            maxLines = 1,
          )
        }
      }
    }

    if (onSearchClick != null || onTogglePrivacy != null) {
      HorizontalDivider(thickness = 1.dp, color = tokens.divider)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        if (onSearchClick != null) {
          val pillShape = RoundedCornerShape(100.dp)
          Surface(
            shape = pillShape,
            color = tokens.surfaceSoft,
            border = BorderStroke(1.dp, tokens.divider),
            modifier = Modifier
              .weight(1f)
              .minimumInteractiveComponentSize()
              .heightIn(min = 48.dp)
              .clip(pillShape)
              .clickable(onClick = onSearchClick)
              .testTag("${tag}_search_btn"),
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = strings.globalSearchTitle,
                tint = tokens.mute,
                modifier = Modifier.size(18.dp),
              )
              Text(
                text = strings.globalSearchHint,
                style = MaterialTheme.typography.bodySmall,
                color = tokens.mute,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }
        } else {
          Spacer(modifier = Modifier.weight(1f))
        }

        if (onTogglePrivacy != null) {
          val eyeShape = RoundedCornerShape(100.dp)
          Surface(
            shape = eyeShape,
            color = if (isPrivacyMode) tokens.primaryTint else tokens.surfaceSoft,
            border = BorderStroke(
              1.dp,
              if (isPrivacyMode) tokens.linkColor.copy(alpha = 0.45f) else tokens.divider,
            ),
            modifier = Modifier
              .minimumInteractiveComponentSize()
              .heightIn(min = 48.dp)
              .clip(eyeShape)
              .clickable(onClick = onTogglePrivacy)
              .testTag("${tag}_privacy_btn"),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Icon(
                imageVector = if (isPrivacyMode) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                contentDescription = if (isPrivacyMode) strings.privacyToggleShowDesc else strings.privacyToggleHideDesc,
                tint = if (isPrivacyMode) tokens.linkColor else tokens.inkDeep,
                modifier = Modifier.size(18.dp),
              )
              if (isPrivacyMode) {
                Text(
                  text = "••••",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = tokens.linkColor,
                )
              }
            }
          }
        }
      }
    }
    } // end Column
  }
}

/**
 * Carte officielle « À propos de l'application » affichant l'icône SYNDICAT,
 * l'éditeur DYNIC, la Version 2.0 et l'Année d'édition 2026.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SyndicatAboutEditionCard(
  modifier: Modifier = Modifier,
) {
  val strings = LocalStrings.current
  val tokens = LocalSyndicatTokens.current

  AirtableSoftCard(
    modifier = modifier
      .fillMaxWidth()
      .testTag("settings_about_card"),
    padding = 24.dp,
  ) {
    Text(
      text = strings.appAboutCardTitle,
      style = MaterialTheme.typography.labelLarge,
      fontWeight = FontWeight.SemiBold,
      color = tokens.mute,
    )

    Spacer(modifier = Modifier.height(12.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      SyndicatBrandIconBadge(
        size = 60.dp,
        cornerRadius = 16.dp,
        modifier = Modifier.testTag("about_app_icon"),
      )

      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        Text(
          text = strings.appBrandName,
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.Bold,
          color = tokens.inkDeep,
        )
        Text(
          text = strings.appAboutCardSubtitle,
          style = MaterialTheme.typography.bodySmall,
          color = tokens.body,
        )
      }
    }

    Spacer(modifier = Modifier.height(14.dp))
    HorizontalDivider(color = tokens.divider)
    Spacer(modifier = Modifier.height(14.dp))

    FlowRow(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      AboutMetadataPill(
        icon = Icons.Filled.Business,
        label = strings.appPublisherLabel,
        tag = "about_editor_badge",
      )
      AboutMetadataPill(
        icon = Icons.Filled.Verified,
        label = strings.appVersionBadge,
        tag = "about_version_badge",
        highlighted = true,
      )
      AboutMetadataPill(
        icon = Icons.Filled.CalendarMonth,
        label = strings.appEditionYearFull,
        tag = "about_edition_year_badge",
      )
    }
  }
}

@Composable
private fun AboutMetadataPill(
  icon: ImageVector,
  label: String,
  tag: String,
  highlighted: Boolean = false,
) {
  val tokens = LocalSyndicatTokens.current
  val containerBg = if (highlighted) tokens.primaryTint else tokens.cardBg
  val contentTint = if (highlighted) tokens.linkColor else tokens.inkDeep
  val borderColor = if (highlighted) tokens.linkColor.copy(alpha = 0.25f) else tokens.cardBorder

  Surface(
    shape = RoundedCornerShape(tokens.badgeRadiusDp.dp),
    color = containerBg,
    border = BorderStroke(1.dp, borderColor),
    tonalElevation = 0.dp,
    shadowElevation = 0.dp,
    modifier = Modifier.testTag(tag),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = contentTint,
        modifier = Modifier.size(16.dp),
      )
      Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = contentTint,
      )
    }
  }
}
