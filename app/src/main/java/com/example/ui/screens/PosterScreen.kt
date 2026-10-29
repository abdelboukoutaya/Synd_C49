package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ui.components.AirtableSoftCard
import com.example.ui.components.FintechWhiteCard
import com.example.ui.components.MetaCobaltBuyButton
import com.example.ui.components.SageSecondaryButton
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LocalStrings
import com.example.ui.i18n.bidiLtr
import com.example.ui.i18n.formatNumber
import com.example.ui.i18n.localizeDigits
import com.example.ui.theme.AirtableCanvas
import com.example.ui.theme.AirtableHairline
import com.example.ui.theme.AirtableInk
import com.example.ui.theme.AirtableMuted
import com.example.ui.theme.AirtableSignatureCoral
import com.example.ui.theme.AirtableSignatureForest
import com.example.ui.theme.AirtableSuccess
import com.example.ui.theme.AirtableSurfaceDark
import com.example.ui.theme.AirtableSurfaceSoft
import com.example.ui.theme.AirtableSurfaceStrong
import com.example.ui.theme.LocalSyndicatTokens
import com.example.ui.theme.MetaCobaltTintLight
import com.example.ui.theme.MetaInkDeep
import com.example.ui.theme.MetaSuccessTintLight
import com.example.ui.viewmodel.SyndicatDashboardData
import com.example.ui.viewmodel.formatDh
import com.example.util.CotisationExportOptions
import com.example.util.ExportVariant
import com.example.util.PdfPosterExporter
import com.example.util.RelanceExportOptions

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PosterScreen(
  data: SyndicatDashboardData,
  onExportError: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val strings = LocalStrings.current
  val tokens = LocalSyndicatTokens.current
  val cfg = data.config
  val cur = cfg.currency
  val dig = cfg.digitsAr
  val isAr = strings.language == AppLanguage.AR
  val numericFont = tokens.numericFontFamily ?: tokens.monoFontFamily

  val activeVariant = ExportVariant.PRINT

  // Card 1 Options (8.1 Affiche de cotisation)
  var cotShowNames by remember { mutableStateOf(true) }
  var cotUntilCutoffOnly by remember { mutableStateOf(false) }
  var cotInkSaver by remember { mutableStateOf(false) }

  // Card 2 Options (8.2 Affiche de relance)
  var relAnonymousMode by remember { mutableStateOf(false) }
  var relInkSaver by remember { mutableStateOf(false) }

  // Card 3 Options (8.3 Situation des dépenses & caisse)
  var caisseInkSaver by remember { mutableStateOf(false) }

  val a4PortraitBadgeLabel = if (isAr) "A4 عمودي" else "A4 Portrait"

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(tokens.screenBg)
      .testTag("poster_screen"),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp),
    verticalArrangement = Arrangement.spacedBy(20.dp),
  ) {
    // Page Header
    item {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = strings.posterPageTitle,
          style = MaterialTheme.typography.headlineLarge,
          fontWeight = FontWeight.Medium,
          color = tokens.inkDeep,
        )
        Text(
          text = strings.posterPageSub,
          style = MaterialTheme.typography.headlineMedium,
          fontWeight = FontWeight.Light,
          color = tokens.body,
        )
      }
    }

    // =========================================================================
    // CARD 1 (FintechWhiteCard elevatedCheckout) — 8.1 AFFICHE DE COTISATION (FORMAT PORTRAIT A4)
    // Porte le bouton Cobalt Meta `button-buy-cta` : « تصدير PDF »
    // =========================================================================
    item {
      val previewCols = if (cotUntilCutoffOnly) {
        cfg.cutoffMonthColumns.ifEmpty { cfg.activeMonthColumns }
      } else {
        cfg.activeMonthColumns
      }
      val visibleMonths = previewCols.take(if (cotShowNames) 3 else 4)

      val paperBg = AirtableCanvas
      val paperInk = AirtableInk
      val paperMute = AirtableMuted
      val paperHeaderBg = AirtableSurfaceSoft
      val paperHairline = AirtableHairline

      FintechWhiteCard(
        modifier = Modifier.fillMaxWidth().testTag("export_card_cotisation"),
        padding = 24.dp,
        elevatedCheckout = true,
      ) {
        ExportCardHeader(
          icon = Icons.Default.TableChart,
          title = strings.exportCard1Title,
          subtitle = strings.exportCard1Sub,
          formatBadge = a4PortraitBadgeLabel,
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Aperçu feuille A4 Portrait — adapté sans débordement horizontal sur téléphone
        Surface(
          shape = RoundedCornerShape(tokens.fieldRadiusDp.dp),
          color = paperBg,
          border = BorderStroke(1.dp, paperHairline),
          tonalElevation = 0.dp,
          shadowElevation = 0.dp,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Text(
              text = cfg.buildingTitle,
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Normal,
              color = paperInk,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(12.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
              Text(
                text = "✓ ${strings.posterLegendStandard} (${formatNumber(cfg.defaultMonthlyFee, dig)})",
                style = MaterialTheme.typography.labelSmall,
                color = AirtableSuccess,
              )
              Text(
                text = "◐ ${strings.posterLegendSpecial}",
                style = MaterialTheme.typography.labelSmall,
                color = AirtableInk,
              )
            }
            Spacer(modifier = Modifier.height(8.dp))

            // En-tête du tableau Portrait
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .background(paperHeaderBg, RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Text(
                text = strings.colAppt,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = paperMute,
                maxLines = 1,
                modifier = Modifier.width(34.dp),
              )
              if (cotShowNames) {
                Text(
                  text = strings.colOwner,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Medium,
                  color = paperMute,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                  modifier = Modifier.weight(1.25f),
                )
              }
              visibleMonths.forEach { col ->
                Text(
                  text = strings.monthShort(col),
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Medium,
                  color = paperMute,
                  textAlign = TextAlign.Center,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                  modifier = Modifier.weight(1f),
                )
              }
              Text(
                text = strings.colTotal,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = paperMute,
                textAlign = TextAlign.End,
                maxLines = 1,
                modifier = Modifier.width(44.dp),
              )
            }

            var prevFloor = ""
            data.apartments.take(6).forEach { apt ->
              if (prevFloor.isNotEmpty() && prevFloor != apt.floor) {
                HorizontalDivider(thickness = 1.dp, color = paperInk.copy(alpha = 0.6f))
              }
              prevFloor = apt.floor
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Column(modifier = Modifier.width(34.dp)) {
                  Text(
                    text = localizeDigits(apt.displayNumber, dig),
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = numericFont ?: MaterialTheme.typography.labelSmall.fontFamily,
                    fontWeight = FontWeight.Medium,
                    color = paperInk,
                    maxLines = 1,
                  )
                  Text(
                    text = strings.floorDisplay(apt.floor),
                    style = MaterialTheme.typography.labelSmall,
                    color = paperMute,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                }
                if (cotShowNames) {
                  Text(
                    text = apt.displayOwner(isAr).ifBlank { "—" },
                    style = MaterialTheme.typography.labelSmall,
                    color = paperInk,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1.25f),
                  )
                }
                visibleMonths.forEach { col ->
                  val v = apt.amountFor(col)
                  val exp = apt.expectedFeeForMonth(col, cfg)
                  val isPart = v > 0L && exp > 0L && v < exp
                  val cellTxt = if (v > 0L) {
                    "${if (isPart) "◐" else "✓"}${formatNumber(v, dig)}"
                  } else {
                    "!"
                  }
                  val cellCol = when {
                    v > 0L -> if (isPart) AirtableInk else AirtableSuccess
                    else -> AirtableSignatureCoral
                  }
                  Text(
                    text = cellTxt,
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = numericFont ?: MaterialTheme.typography.labelSmall.fontFamily,
                    fontWeight = if (v > 0L) FontWeight.Medium else FontWeight.Normal,
                    color = cellCol,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                  )
                }
                Text(
                  text = formatNumber(apt.totalForConfig(cfg), dig),
                  style = MaterialTheme.typography.labelSmall,
                  fontFamily = numericFont ?: MaterialTheme.typography.labelSmall.fontFamily,
                  fontWeight = FontWeight.Medium,
                  color = paperInk,
                  textAlign = TextAlign.End,
                  maxLines = 1,
                  modifier = Modifier.width(44.dp),
                )
              }
            }

            Spacer(modifier = Modifier.height(6.dp))
            val caisseBandBg = if (cotInkSaver) AirtableSurfaceStrong else AirtableSurfaceDark
            val caisseBandFg = if (cotInkSaver) AirtableInk else Color.White
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = caisseBandBg,
              tonalElevation = 0.dp,
              shadowElevation = 0.dp,
              modifier = Modifier.fillMaxWidth(),
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                  text = strings.rowCaisse,
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Medium,
                  color = caisseBandFg,
                )
                Text(
                  text = formatDh(data.totalCaisseDh, cur, dig),
                  style = MaterialTheme.typography.titleSmall,
                  fontFamily = numericFont ?: MaterialTheme.typography.titleSmall.fontFamily,
                  fontWeight = FontWeight.Medium,
                  color = caisseBandFg,
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        PosterOptionCheckbox(
          checked = cotShowNames,
          label = strings.exportOptShowNames,
          onCheckedChange = { cotShowNames = it },
        )
        PosterOptionCheckbox(
          checked = cotUntilCutoffOnly,
          label = strings.exportOptUntilCutoffOnly,
          onCheckedChange = { cotUntilCutoffOnly = it },
        )
        PosterOptionCheckbox(
          checked = cotInkSaver,
          label = strings.exportOptInkSaver,
          onCheckedChange = { cotInkSaver = it },
        )

        Spacer(modifier = Modifier.height(12.dp))

        val cotOpts = CotisationExportOptions(
          showNames = cotShowNames,
          untilCutoffOnly = cotUntilCutoffOnly,
          inkSaver = cotInkSaver,
          variant = activeVariant,
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          MetaCobaltBuyButton(
            text = strings.exportPdfPrimaryBtn,
            icon = Icons.Default.PictureAsPdf,
            onClick = {
              PdfPosterExporter.shareOfficialPosterPdf(context, data, cotOpts, onExportError)
            },
            modifier = Modifier.weight(1f).testTag("export_official_poster_pdf"),
          )
          SageSecondaryButton(
            text = strings.exportPngSecondaryBtn,
            icon = Icons.Default.Image,
            onClick = {
              PdfPosterExporter.shareOfficialPosterImage(context, data, cotOpts, onExportError)
            },
            modifier = Modifier.weight(1f).testTag("export_official_poster_png"),
          )
        }
      }
    }

    // =========================================================================
    // CARD 2 (AirtableSoftCard #F8FAFC — alternance après FintechWhiteCard) — 8.2 AFFICHE DE RELANCE
    // Boutons secondaires blancs pour que Card 1 reste le seul bouton primaire noir
    // =========================================================================
    item {
      AirtableSoftCard(
        modifier = Modifier.fillMaxWidth().testTag("export_card_relance"),
        padding = 24.dp,
      ) {
        ExportCardHeader(
          icon = Icons.Default.NotificationsActive,
          title = strings.exportCard2Title,
          subtitle = strings.exportCard2Sub,
          formatBadge = a4PortraitBadgeLabel,
        )

        Spacer(modifier = Modifier.height(14.dp))

        Surface(
          shape = RoundedCornerShape(tokens.fieldRadiusDp.dp),
          color = AirtableCanvas,
          border = BorderStroke(1.dp, AirtableHairline),
          tonalElevation = 0.dp,
          shadowElevation = 0.dp,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
              text = cfg.relanceHeaderNote,
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.Medium,
              color = AirtableInk,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis,
            )
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = AirtableSurfaceSoft,
              border = BorderStroke(1.dp, AirtableHairline),
              tonalElevation = 0.dp,
              shadowElevation = 0.dp,
              modifier = Modifier.fillMaxWidth(),
            ) {
              Text(
                text = "! ${strings.relanceBannerSummary(
                  data.lateApartments.size,
                  formatDh(data.totalRemainingToCollectDh, cur, dig),
                )}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = AirtableSignatureCoral,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              )
            }

            data.lateApartments.take(4).forEachIndexed { idx, apt ->
              if (idx > 0) {
                HorizontalDivider(thickness = 1.dp, color = AirtableHairline)
              }
              val mainLabel = if (relAnonymousMode) {
                "${strings.colAppt} ${localizeDigits(apt.displayNumber, dig)} · ${strings.floorDisplay(apt.floor)}"
              } else {
                "${strings.colAppt} ${localizeDigits(apt.displayNumber, dig)} · ${apt.displayOwner(isAr).ifBlank { "—" }}"
              }
              val missingMonthsStr = apt.missingMonths(cfg).joinToString(" · ") { strings.monthShort(it) }
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                  Text(
                    text = mainLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = AirtableInk,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                  if (missingMonthsStr.isNotEmpty()) {
                    Text(
                      text = missingMonthsStr,
                      style = MaterialTheme.typography.labelSmall,
                      color = AirtableMuted,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                    )
                  }
                }
                Text(
                  text = "! ${formatDh(apt.remainingDh(cfg), cur, dig)}",
                  style = MaterialTheme.typography.labelSmall,
                  fontFamily = numericFont ?: MaterialTheme.typography.labelSmall.fontFamily,
                  fontWeight = FontWeight.Medium,
                  color = AirtableSignatureCoral,
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        PosterOptionCheckbox(
          checked = relAnonymousMode,
          label = strings.exportOptAnonymousMode,
          onCheckedChange = { relAnonymousMode = it },
          testTag = "relance_anonymous_checkbox",
        )
        PosterOptionCheckbox(
          checked = relInkSaver,
          label = strings.exportOptInkSaver,
          onCheckedChange = { relInkSaver = it },
        )

        Spacer(modifier = Modifier.height(12.dp))

        val relOpts = RelanceExportOptions(
          anonymousMode = relAnonymousMode,
          inkSaver = relInkSaver,
          variant = activeVariant,
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          SageSecondaryButton(
            text = strings.exportPdfPrimaryBtn,
            icon = Icons.Default.PictureAsPdf,
            onClick = {
              PdfPosterExporter.shareRelanceListPdf(context, data, relOpts, onExportError)
            },
            modifier = Modifier.weight(1f).testTag("export_relance_pdf"),
          )
          SageSecondaryButton(
            text = strings.exportPngSecondaryBtn,
            icon = Icons.Default.Image,
            onClick = {
              PdfPosterExporter.shareRelanceListImage(context, data, relOpts, onExportError)
            },
            modifier = Modifier.weight(1f).testTag("export_relance_png"),
          )
        }
      }
    }

    // =========================================================================
    // CARD 3 (FintechWhiteCard — alternance après AirtableSoftCard) — 8.3 SITUATION & ÉTAT DE CAISSE
    // Miniature Portrait A4 : Carte Forêt #0A2E0E + cartes Menthe/Pêche + liste verticale des dépenses
    // =========================================================================
    item {
      FintechWhiteCard(
        modifier = Modifier.fillMaxWidth().testTag("export_card_caisse"),
        padding = 24.dp,
      ) {
        ExportCardHeader(
          icon = Icons.Default.AccountBalanceWallet,
          title = strings.exportCard3Title,
          subtitle = strings.exportCard3Sub,
          formatBadge = a4PortraitBadgeLabel,
        )

        Spacer(modifier = Modifier.height(14.dp))

        Surface(
          shape = RoundedCornerShape(tokens.fieldRadiusDp.dp),
          color = AirtableCanvas,
          border = BorderStroke(1.dp, AirtableHairline),
          tonalElevation = 0.dp,
          shadowElevation = 0.dp,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Carte Forêt en haut (ou blanche à contour en mode économie d'encre)
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (caisseInkSaver) AirtableCanvas else AirtableSignatureForest,
              border = if (caisseInkSaver) BorderStroke(1.dp, AirtableInk) else null,
              tonalElevation = 0.dp,
              shadowElevation = 0.dp,
              modifier = Modifier.fillMaxWidth(),
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                  text = strings.caisseAvailableTitle,
                  style = MaterialTheme.typography.labelMedium,
                  color = if (caisseInkSaver) AirtableInk else Color.White,
                  modifier = Modifier.weight(1f),
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.width(8.dp))
                val cashVal = if (data.cashNowDh < 0L) {
                  "▼ ${formatDh(data.cashNowDh, cur, dig)}"
                } else {
                  formatDh(data.cashNowDh, cur, dig)
                }
                Text(
                  text = cashVal,
                  style = MaterialTheme.typography.titleSmall,
                  fontFamily = numericFont ?: MaterialTheme.typography.titleSmall.fontFamily,
                  fontWeight = FontWeight.Medium,
                  color = if (caisseInkSaver) AirtableInk else Color.White,
                )
              }
            }

            // Cartes sémantiques Meta côte à côte (papier A4 clair)
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (caisseInkSaver) AirtableCanvas else MetaSuccessTintLight,
                border = if (caisseInkSaver) BorderStroke(1.dp, AirtableHairline) else null,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
                modifier = Modifier.weight(1f),
              ) {
                Column(modifier = Modifier.padding(10.dp)) {
                  Text(strings.dashCollectedLabel, style = MaterialTheme.typography.labelSmall, color = MetaInkDeep, maxLines = 1, overflow = TextOverflow.Ellipsis)
                  Text(
                    text = formatDh(data.totalCaisseDh, cur, dig),
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = numericFont ?: MaterialTheme.typography.labelMedium.fontFamily,
                    fontWeight = FontWeight.Medium,
                    color = MetaInkDeep,
                  )
                }
              }
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (caisseInkSaver) AirtableCanvas else MetaCobaltTintLight,
                border = if (caisseInkSaver) BorderStroke(1.dp, AirtableHairline) else null,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
                modifier = Modifier.weight(1f),
              ) {
                Column(modifier = Modifier.padding(10.dp)) {
                  Text(strings.dashExpensesLabel, style = MaterialTheme.typography.labelSmall, color = MetaInkDeep, maxLines = 1, overflow = TextOverflow.Ellipsis)
                  Text(
                    text = formatDh(data.totalExpensesDh, cur, dig),
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = numericFont ?: MaterialTheme.typography.labelMedium.fontFamily,
                    fontWeight = FontWeight.Medium,
                    color = MetaInkDeep,
                  )
                }
              }
            }

            // Aperçu de la liste verticale des dépenses (format Portrait 2 lignes par dépense)
            if (data.expenses.isNotEmpty()) {
              HorizontalDivider(thickness = 1.dp, color = AirtableHairline)
              data.expenses.take(3).forEach { exp ->
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                      text = exp.title,
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.Medium,
                      color = AirtableInk,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                    )
                    val meta = buildList {
                      add(strings.categoryLabel(exp.category))
                      if (exp.supplier.isNotBlank()) add(exp.supplier)
                      if (exp.dateLabel.isNotBlank()) add(bidiLtr(localizeDigits(exp.dateLabel, dig)))
                    }.joinToString(" · ")
                    Text(
                      text = meta,
                      style = MaterialTheme.typography.labelSmall,
                      color = AirtableMuted,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                    )
                  }
                  Text(
                    text = formatDh(exp.amountDh, cur, dig),
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = numericFont ?: MaterialTheme.typography.labelSmall.fontFamily,
                    fontWeight = FontWeight.Medium,
                    color = AirtableInk,
                  )
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        PosterOptionCheckbox(
          checked = caisseInkSaver,
          label = strings.exportOptInkSaver,
          onCheckedChange = { caisseInkSaver = it },
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          SageSecondaryButton(
            text = strings.exportPdfPrimaryBtn,
            icon = Icons.Default.PictureAsPdf,
            onClick = {
              PdfPosterExporter.shareCaisseReportPdf(context, data, caisseInkSaver, onExportError, activeVariant)
            },
            modifier = Modifier.weight(1f).testTag("export_caisse_pdf"),
          )
          SageSecondaryButton(
            text = strings.exportPngSecondaryBtn,
            icon = Icons.Default.Image,
            onClick = {
              PdfPosterExporter.shareCaisseReportImage(context, data, caisseInkSaver, onExportError, activeVariant)
            },
            modifier = Modifier.weight(1f).testTag("export_caisse_png"),
          )
        }
      }
    }

    // Text Summary Share via WhatsApp
    item {
      SageSecondaryButton(
        text = strings.posterShareWhatsappText,
        icon = Icons.AutoMirrored.Filled.Send,
        onClick = { PdfPosterExporter.shareWhatsAppSummary(context, data) },
        modifier = Modifier.fillMaxWidth().testTag("share_whatsapp_text_summary"),
      )
    }
  }
}

@Composable
private fun ExportCardHeader(
  icon: ImageVector,
  title: String,
  subtitle: String,
  formatBadge: String,
) {
  val tokens = LocalSyndicatTokens.current
  Row(
    verticalAlignment = Alignment.Top,
    horizontalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Surface(
      shape = CircleShape,
      color = tokens.surfaceSoft,
      border = BorderStroke(1.dp, tokens.divider),
      tonalElevation = 0.dp,
      shadowElevation = 0.dp,
      modifier = Modifier.size(40.dp),
    ) {
      Box(contentAlignment = Alignment.Center) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = tokens.ink,
          modifier = Modifier.size(19.dp),
        )
      }
    }
    Column(modifier = Modifier.weight(1f)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Medium,
          color = tokens.inkDeep,
          modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Surface(
          shape = RoundedCornerShape(tokens.badgeRadiusDp.dp),
          color = tokens.surfaceSoft,
          border = BorderStroke(1.dp, tokens.divider),
          tonalElevation = 0.dp,
          shadowElevation = 0.dp,
        ) {
          Text(
            text = formatBadge,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = tokens.inkDeep,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
          )
        }
      }
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = tokens.body,
      )
    }
  }
}

@Composable
private fun PosterOptionCheckbox(
  checked: Boolean,
  label: String,
  onCheckedChange: (Boolean) -> Unit,
  testTag: String? = null,
) {
  val tokens = LocalSyndicatTokens.current
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .minimumInteractiveComponentSize()
      .heightIn(min = 48.dp)
      .clip(RoundedCornerShape(tokens.fieldRadiusDp.dp))
      .clickable { onCheckedChange(!checked) }
      .let { if (testTag != null) it.testTag(testTag) else it }
      .padding(vertical = 2.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Checkbox(
      checked = checked,
      onCheckedChange = onCheckedChange,
    )
    Text(
      text = label,
      style = MaterialTheme.typography.bodyMedium,
      color = tokens.ink,
    )
  }
}
