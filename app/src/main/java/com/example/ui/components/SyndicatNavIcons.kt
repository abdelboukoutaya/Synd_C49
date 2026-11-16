package com.example.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Jeu d'icônes minimalistes sur mesure (grille 24x24, traits arrondis 1.9dp, remplissage duotone léger à l'état actif)
 * conçues pour identifier chaque section d'un seul coup d'œil sans texte dans la barre de navigation.
 */
object SyndicatNavIcons {

  val DashboardOutlined: ImageVector by lazy { buildDashboard(filled = false) }
  val DashboardFilled: ImageVector by lazy { buildDashboard(filled = true) }

  val ApartmentsOutlined: ImageVector by lazy { buildApartments(filled = false) }
  val ApartmentsFilled: ImageVector by lazy { buildApartments(filled = true) }

  val ExpensesOutlined: ImageVector by lazy { buildExpenses(filled = false) }
  val ExpensesFilled: ImageVector by lazy { buildExpenses(filled = true) }

  val ProjectsOutlined: ImageVector by lazy { buildProjects(filled = false) }
  val ProjectsFilled: ImageVector by lazy { buildProjects(filled = true) }

  val PosterOutlined: ImageVector by lazy { buildPoster(filled = false) }
  val PosterFilled: ImageVector by lazy { buildPoster(filled = true) }

  val SettingsOutlined: ImageVector by lazy { buildSettings(filled = false) }
  val SettingsFilled: ImageVector by lazy { buildSettings(filled = true) }

  private val InkBrush = SolidColor(Color.Black)

  private fun iconBuilder(name: String): ImageVector.Builder = ImageVector.Builder(
    name = name,
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
  )

  private fun PathBuilder.circlePath(cx: Float, cy: Float, r: Float) {
    moveTo(cx - r, cy)
    arcToRelative(r, r, 0f, isMoreThanHalf = true, isPositiveArc = true, dx1 = 2f * r, dy1 = 0f)
    arcToRelative(r, r, 0f, isMoreThanHalf = true, isPositiveArc = true, dx1 = -2f * r, dy1 = 0f)
    close()
  }

  /**
   * 1. Accueil / Tableau de bord : silhouette de maison épurée encadrant 3 barres d'activité montantes.
   */
  private fun buildDashboard(filled: Boolean): ImageVector {
    val sw = if (filled) 2.15f else 1.85f
    return iconBuilder(if (filled) "NavDashboardFilled" else "NavDashboardOutlined").apply {
      if (filled) {
        path(fill = InkBrush, fillAlpha = 0.26f) {
          moveTo(4f, 10.2f)
          lineTo(12f, 3.6f)
          lineTo(20f, 10.2f)
          lineTo(18.5f, 10.2f)
          lineTo(18.5f, 19.2f)
          curveTo(18.5f, 20.1f, 17.8f, 20.8f, 16.9f, 20.8f)
          lineTo(7.1f, 20.8f)
          curveTo(6.2f, 20.8f, 5.5f, 20.1f, 5.5f, 19.2f)
          lineTo(5.5f, 10.2f)
          close()
        }
      }
      // Toit minimaliste
      path(
        stroke = InkBrush,
        strokeLineWidth = sw,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(3.4f, 10.6f)
        lineTo(12f, 3.5f)
        lineTo(20.6f, 10.6f)
      }
      // Murs arrondis
      path(
        stroke = InkBrush,
        strokeLineWidth = sw,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(5.5f, 9.6f)
        lineTo(5.5f, 19f)
        curveTo(5.5f, 20.1f, 6.4f, 21f, 7.5f, 21f)
        lineTo(16.5f, 21f)
        curveTo(17.6f, 21f, 18.5f, 20.1f, 18.5f, 19f)
        lineTo(18.5f, 9.6f)
      }
      // 3 barres de statistiques KPI
      path(
        stroke = InkBrush,
        strokeLineWidth = sw,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(9f, 17.5f)
        lineTo(9f, 14.5f)
        moveTo(12f, 17.5f)
        lineTo(12f, 11.4f)
        moveTo(15f, 17.5f)
        lineTo(15f, 13.3f)
      }
    }.build()
  }

  /**
   * 2. Cotisations / Appartements : façade d'immeuble avec fenêtres de lots + pastille de validation (coche).
   */
  private fun buildApartments(filled: Boolean): ImageVector {
    val sw = if (filled) 2.15f else 1.85f
    return iconBuilder(if (filled) "NavApartmentsFilled" else "NavApartmentsOutlined").apply {
      if (filled) {
        path(fill = InkBrush, fillAlpha = 0.25f) {
          moveTo(4.5f, 20.2f)
          lineTo(4.5f, 5.4f)
          curveTo(4.5f, 4.3f, 5.4f, 3.4f, 6.5f, 3.4f)
          lineTo(13.5f, 3.4f)
          curveTo(14.6f, 3.4f, 15.5f, 4.3f, 15.5f, 5.4f)
          lineTo(15.5f, 12.2f)
          lineTo(12.2f, 16.5f)
          lineTo(12.2f, 20.2f)
          close()
        }
        path(fill = InkBrush, fillAlpha = 0.30f) {
          circlePath(16.8f, 16.8f, 4.2f)
        }
      }
      // Bâtiment principal
      path(
        stroke = InkBrush,
        strokeLineWidth = sw,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(3.5f, 20.5f)
        lineTo(10.8f, 20.5f)
        moveTo(4.6f, 20.5f)
        lineTo(4.6f, 5.5f)
        curveTo(4.6f, 4.4f, 5.5f, 3.5f, 6.6f, 3.5f)
        lineTo(13.4f, 3.5f)
        curveTo(14.5f, 3.5f, 15.4f, 4.4f, 15.4f, 5.5f)
        lineTo(15.4f, 10.3f)
      }
      // Fenêtres des appartements
      path(
        stroke = InkBrush,
        strokeLineWidth = sw,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(7.8f, 7.3f)
        lineTo(8.8f, 7.3f)
        moveTo(11.4f, 7.3f)
        lineTo(12.4f, 7.3f)
        moveTo(7.8f, 11.2f)
        lineTo(8.8f, 11.2f)
        moveTo(7.8f, 15.1f)
        lineTo(8.8f, 15.1f)
      }
      // Cercle + coche de cotisation validée
      path(
        stroke = InkBrush,
        strokeLineWidth = sw,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        circlePath(16.8f, 16.8f, 4.2f)
      }
      path(
        stroke = InkBrush,
        strokeLineWidth = sw,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(15f, 16.8f)
        lineTo(16.3f, 18.1f)
        lineTo(18.8f, 15.4f)
      }
    }.build()
  }

  /**
   * 3. Dépenses & Caisse : portefeuille épuré + flèche de sortie vers le haut à droite.
   */
  private fun buildExpenses(filled: Boolean): ImageVector {
    val sw = if (filled) 2.15f else 1.85f
    return iconBuilder(if (filled) "NavExpensesFilled" else "NavExpensesOutlined").apply {
      if (filled) {
        path(fill = InkBrush, fillAlpha = 0.26f) {
          moveTo(3.6f, 9.5f)
          curveTo(3.6f, 8.1f, 4.7f, 7f, 6.1f, 7f)
          lineTo(12.5f, 7f)
          lineTo(17.5f, 12f)
          lineTo(17.5f, 17.5f)
          curveTo(17.5f, 18.9f, 16.4f, 20f, 15f, 20f)
          lineTo(6.1f, 20f)
          curveTo(4.7f, 20f, 3.6f, 18.9f, 3.6f, 17.5f)
          close()
        }
      }
      // Portefeuille minimaliste
      path(
        stroke = InkBrush,
        strokeLineWidth = sw,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(12f, 7f)
        lineTo(6.1f, 7f)
        curveTo(4.7f, 7f, 3.6f, 8.1f, 3.6f, 9.5f)
        lineTo(3.6f, 17.5f)
        curveTo(3.6f, 18.9f, 4.7f, 20f, 6.1f, 20f)
        lineTo(15.2f, 20f)
        curveTo(16.6f, 20f, 17.7f, 18.9f, 17.7f, 17.5f)
        lineTo(17.7f, 13.5f)
        moveTo(3.8f, 11.4f)
        lineTo(11.6f, 11.4f)
      }
      // Repère pièce dans le portefeuille
      path(
        stroke = InkBrush,
        strokeLineWidth = sw,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        circlePath(8f, 15.6f, 1.3f)
      }
      // Flèche sortante (dépense)
      path(
        stroke = InkBrush,
        strokeLineWidth = sw,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(14.3f, 9.7f)
        lineTo(20.4f, 3.6f)
        moveTo(16f, 3.6f)
        lineTo(20.4f, 3.6f)
        lineTo(20.4f, 8f)
      }
    }.build()
  }

  /**
   * 4. Projets & Travaux : casque de chantier géométrique très épuré.
   */
  private fun buildProjects(filled: Boolean): ImageVector {
    val sw = if (filled) 2.15f else 1.85f
    return iconBuilder(if (filled) "NavProjectsFilled" else "NavProjectsOutlined").apply {
      if (filled) {
        path(fill = InkBrush, fillAlpha = 0.26f) {
          moveTo(4.8f, 15.2f)
          curveTo(4.8f, 10.6f, 8f, 7f, 12f, 7f)
          curveTo(16f, 7f, 19.2f, 10.6f, 19.2f, 15.2f)
          close()
        }
      }
      // Dôme du casque
      path(
        stroke = InkBrush,
        strokeLineWidth = sw,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(4.8f, 15.2f)
        curveTo(4.8f, 10.6f, 8f, 7f, 12f, 7f)
        curveTo(16f, 7f, 19.2f, 10.6f, 19.2f, 15.2f)
      }
      // Crête supérieure et nervure centrale
      path(
        stroke = InkBrush,
        strokeLineWidth = sw,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(10.2f, 7.1f)
        lineTo(10.2f, 5.2f)
        curveTo(10.2f, 4.5f, 10.7f, 4f, 11.4f, 4f)
        lineTo(12.6f, 4f)
        curveTo(13.3f, 4f, 13.8f, 4.5f, 13.8f, 5.2f)
        lineTo(13.8f, 7.1f)
        moveTo(12f, 7.2f)
        lineTo(12f, 12.6f)
      }
      // Visière horizontale nette
      path(
        stroke = InkBrush,
        strokeLineWidth = sw,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(3f, 15.4f)
        lineTo(21f, 15.4f)
        curveTo(21f, 17.3f, 19.6f, 18.6f, 17.8f, 18.6f)
        lineTo(6.2f, 18.6f)
        curveTo(4.4f, 18.6f, 3f, 17.3f, 3f, 15.4f)
        close()
      }
    }.build()
  }

  /**
   * 5. Affiches & Exports PDF : feuille officielle à coin plié avec tableau minimal + flèche d'export.
   */
  private fun buildPoster(filled: Boolean): ImageVector {
    val sw = if (filled) 2.15f else 1.85f
    return iconBuilder(if (filled) "NavPosterFilled" else "NavPosterOutlined").apply {
      if (filled) {
        path(fill = InkBrush, fillAlpha = 0.25f) {
          moveTo(5f, 5.3f)
          curveTo(5f, 4.2f, 5.9f, 3.4f, 7f, 3.4f)
          lineTo(14f, 3.4f)
          lineTo(18.6f, 8f)
          lineTo(18.6f, 18.7f)
          curveTo(18.6f, 19.8f, 17.7f, 20.6f, 16.6f, 20.6f)
          lineTo(7f, 20.6f)
          curveTo(5.9f, 20.6f, 5f, 19.8f, 5f, 18.7f)
          close()
        }
      }
      // Contour feuille + coin plié
      path(
        stroke = InkBrush,
        strokeLineWidth = sw,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(12.2f, 20.5f)
        lineTo(7f, 20.5f)
        curveTo(5.9f, 20.5f, 5f, 19.6f, 5f, 18.5f)
        lineTo(5f, 5.5f)
        curveTo(5f, 4.4f, 5.9f, 3.5f, 7f, 3.5f)
        lineTo(13.8f, 3.5f)
        lineTo(18.5f, 8.2f)
        lineTo(18.5f, 12.2f)
        moveTo(13.6f, 3.7f)
        lineTo(13.6f, 7.2f)
        curveTo(13.6f, 7.9f, 14.1f, 8.4f, 14.8f, 8.4f)
        lineTo(18.3f, 8.4f)
      }
      // Lignes du tableau d'affichage
      path(
        stroke = InkBrush,
        strokeLineWidth = sw,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(8.3f, 11.4f)
        lineTo(14.5f, 11.4f)
        moveTo(8.3f, 14.8f)
        lineTo(12.2f, 14.8f)
      }
      // Flèche d'exportation vers le bas
      path(
        stroke = InkBrush,
        strokeLineWidth = sw,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(17.2f, 14f)
        lineTo(17.2f, 20.4f)
        moveTo(14.9f, 18.1f)
        lineTo(17.2f, 20.4f)
        lineTo(19.5f, 18.1f)
      }
    }.build()
  }

  /**
   * 6. Réglages : 2 curseurs horizontaux minimalistes avec poignées circulaires décalées.
   */
  private fun buildSettings(filled: Boolean): ImageVector {
    val sw = if (filled) 2.15f else 1.85f
    return iconBuilder(if (filled) "NavSettingsFilled" else "NavSettingsOutlined").apply {
      if (filled) {
        path(fill = InkBrush, fillAlpha = 0.28f) {
          circlePath(14.5f, 8.4f, 2.5f)
          circlePath(9.5f, 15.6f, 2.5f)
        }
      }
      // Curseur supérieur
      path(
        stroke = InkBrush,
        strokeLineWidth = sw,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(3.8f, 8.4f)
        lineTo(11.6f, 8.4f)
        moveTo(17.4f, 8.4f)
        lineTo(20.2f, 8.4f)
        circlePath(14.5f, 8.4f, 2.5f)
      }
      // Curseur inférieur
      path(
        stroke = InkBrush,
        strokeLineWidth = sw,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(3.8f, 15.6f)
        lineTo(6.6f, 15.6f)
        moveTo(12.4f, 15.6f)
        lineTo(20.2f, 15.6f)
        circlePath(9.5f, 15.6f, 2.5f)
      }
    }.build()
  }
}
