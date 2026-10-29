# Thème « الداكن الأنيق » (style xAI) — Application Syndicat

Description du **nouveau thème** à ajouter à l'application. Ce document décrit uniquement ce thème.

- **Source** : `X_DESIGN_1.md` (interprétation du langage visuel de xAI). Le fichier `X_DESIGN.md` reçu auparavant était un doublon de `DESIGN.md` (style Wise) et n'a pas servi ici.
- **Identifiant** : `xai` — libellé dans l'application : **« الداكن الأنيق »**.
- **Choix du thème** : الإعدادات ← المظهر ← النمط. **Ce thème est uniquement sombre** : quand il est choisi, le réglage clair / sombre / automatique est désactivé.
- **Nombres en arabe** : la règle d'affichage (espace fine insécable + isolation gauche-à-droite) est décrite dans `syndicat-android-cahier-des-charges.md`, section 3, et s'applique ici sans changement.

---

## 1. Intention

Un style **sobre, technique et très sombre** : fond presque noir, texte blanc, boutons en **pilules à contour fin**, cartes à hairline, aucune ombre. L'écran donne une impression de tableau de bord d'ingénieur : peu de couleurs, beaucoup d'espace, des chiffres nets.

Règles de base du thème (reprises de DESIGN) :
1. **Un seul fond** : `#0a0a0a` partout, sans dégradé.
2. **Le blanc est la couleur de marque** : texte, contours des boutons, bouton principal rempli.
3. **Tous les boutons sont des pilules** ; presque tous sont des contours, un seul bouton blanc rempli par écran.
4. **Pas d'ombre** : les traits fins (hairline) portent toute la hiérarchie.
5. **Pas de gras** : un seul poids (400), la hiérarchie vient de la taille.
6. **Les couleurs d'accent sont rares** (orange coucher de soleil, violet crépuscule, bleu brise).

## 2. Couleurs

| Jeton | Valeur | Rôle |
|---|---|---|
| `canvas` | `#0a0a0a` | Fond de tous les écrans, barre de navigation |
| `canvasSoft` | `#1a1c20` | Fond des champs de saisie, en-tête de tableau, élément survolé |
| `canvasCard` | `#191919` | Fond des cartes |
| `canvasMid` | `#363a3f` | Surfaces imbriquées, piste des barres de progression |
| `hairline` | `#212327` | Traits fins, bordures de cartes et de champs |
| `ink` / `primary` | `#ffffff` | Texte principal, contours des boutons, bouton principal |
| `onPrimary` | `#0a0a0a` | Texte sur le bouton blanc |
| `body` | `#dadbdf` | Texte secondaire |
| `mute` | `#7d8187` | Légendes, étiquettes, placeholder |
| `outlineTranslucent` | `rgba(255,255,255,0.25)` | Contour des boutons pilules |
| `accentSunset` | `#ff7a17` | Accent chaud (voir statuts) |
| `accentSunsetSoft` | `#ffc285` | Variante claire de l'accent chaud |
| `accentDusk` | `#7c3aed` | Violet (illustrations, usage rare) |
| `accentTwilight` | `#c4b5fd` | Violet clair (illustrations) |
| `accentBreeze` | `#a0c3ec` | Bleu clair (information) |
| `accentMidnight` | `#0d1726` | Fond bleu-noir (illustrations) |

### Statuts (adaptation : DESIGN n'a pas de palette sémantique)

DESIGN indique que les signaux de validation passent par la hiérarchie blanc sur noir. Une application de cotisations a besoin de distinguer payé, partiel et retard. Proposition **à valider visuellement**, en utilisant uniquement les accents du thème, toujours avec **icône + texte** (jamais la couleur seule) :

| Statut | Pastille (contour + texte) | Icône |
|---|---|---|
| **مؤدّى** (payé) | `ink` blanc, contour `outlineTranslucent` | ✓ |
| **جزئي** (partiel) | `accentSunsetSoft` | ◐ |
| **متأخر** (retard) | `accentSunset` | ! |
| **تم التذكير** (relance envoyée) | `accentBreeze` | ✉ |
| Solde négatif | `accentSunset`, avec signe moins | ▼ |
| Solde positif | `ink` blanc | — |

### Contrastes (valeurs approximatives)
- Blanc sur `#0a0a0a` : très lisible (environ 19:1).
- `accentSunset` sur `#0a0a0a` : environ 7,6:1 ; `accentSunsetSoft` et `accentBreeze` : plus élevé.
- `mute` sur `#0a0a0a` : environ 5:1 ; sur `canvasCard` : environ 4,5:1 (limite) → pas de texte plus petit que 12 sp en `mute`, et utiliser `body` pour les informations importantes sur carte.
- **Bordures de champs** : `hairline` sur `canvas` est très discret (environ 1,3:1), sous le seuil de 3:1 recommandé pour les composants d'interface. Compensation : fond `canvasSoft` pour les champs, et **bordure blanche de 1,5 dp au focus**.

## 3. Typographie

DESIGN utilise Universal Sans (latin) et Geist Mono. Elles **ne gèrent pas l'arabe**. Le thème utilise :
- **IBM Plex Sans Arabic** (poids 400) pour tous les textes arabes ; alternative : **Cairo** ou **Tajawal** en 400.
- **Geist Mono** (ou **IBM Plex Mono**) pour les **montants, chiffres de tableau, codes et étiquettes latines** (« DH », codes de mois). Les chiffres en chasse fixe alignent bien les colonnes.

Règles du thème :
- **Un seul poids : 400.** Aucun gras ; l'emphase vient de la taille et de la couleur (`ink` plutôt que `body`).
- **Texte arabe** : espacement des lettres à **0** (l'espacement négatif de DESIGN casse la liaison des lettres arabes ; l'espacement positif des étiquettes aussi). Interligne **≥ 1,3** (DESIGN utilise un interligne égal à la taille, prévu pour le latin).
- **Étiquettes (eyebrows)** : DESIGN les met en majuscules espacées ; l'arabe n'a pas de majuscules. En arabe : petit texte 12–14 sp en `mute`, sans espacement. En latin (codes, unités) : majuscules en mono avec espacement de 1,2 à 1,4.

| Style | Taille | Poids | Usage |
|---|---|---|---|
| `display` | 44 sp | 400 | Montant de la caisse (tableau de bord) |
| `title` | 32 sp | 400 | Titre d'écran |
| `section` | 20 sp | 400 | Titre de carte |
| `body` | 16 sp | 400 | Texte courant |
| `small` | 14 sp | 400 | Texte secondaire, boutons |
| `eyebrow` | 12–14 sp | 400 | Étiquette au-dessus d'un titre (`mute`) |
| `mono` | 14–16 sp | 400 | Montants, nombres de tableau |

DESIGN prévoit des titres de 96 à 48 px ; sur téléphone la taille maximale est **44 sp**.

## 4. Formes, espacements, élévation

| Élément | Valeur |
|---|---|
| Boutons | **pilule** (rayon 999), contour 1 dp `outlineTranslucent` |
| Cartes | rayon **8 dp**, fond `canvasCard`, bordure 1 dp `hairline`, marge intérieure 24 dp |
| Champs de saisie | rayon 8 dp, fond `canvasSoft`, bordure 1 dp `hairline` |
| Espacements | 2 · 4 · 8 · 12 · 16 · 24 · 32 · 48 · 64 (base 4) |
| Marge d'écran | 16 dp |
| Séparateur | trait de 1 dp `hairline` |

Élévation : **aucune ombre**. Niveau 0 : à plat. Niveau 1 : bordure hairline. Les fenêtres modales (adaptation) sont une carte `canvasCard` avec hairline, au-dessus d'un voile noir à 60 % ; pas d'ombre portée.

**Zones tactiles (adaptation)** : DESIGN prévoit des boutons de 32 à 40 px de haut. Sur Android, la hauteur minimale est de **48 dp** : marge verticale de 12 dp pour les boutons, zone de toucher élargie pour les petits boutons.

## 5. Composants

| Composant | Spécification |
|---|---|
| **Bouton principal** | Pilule remplie : fond `primary` blanc, texte `onPrimary`, 14–16 sp, hauteur ≥ 48 dp. **Un seul par écran** (ex. « تسجيل دفعة », « إرسال التذكيرات ») |
| **Bouton contour** | Pilule transparente, contour 1 dp `outlineTranslucent`, texte `ink`. Pour toutes les autres actions (ex. « تصدير », « رجوع ») |
| **Bouton contour petit** | Même style, marge réduite ; zone tactile 48 dp. Ex. « تذكير » avec icône WhatsApp sur chaque ligne |
| **Carte** | Fond `canvasCard`, bordure `hairline`, rayon 8, marge 24 |
| **Champ de saisie** | Fond `canvasSoft`, bordure `hairline`, rayon 8, marge 12 × 16 ; focus : bordure blanche 1,5 dp ; erreur : bordure et message `accentSunset` avec icône |
| **Pastille de statut** | Pilule à contour, texte et icône colorés selon le tableau des statuts |
| **Barre de navigation (bas)** | Fond `canvas`, trait hairline en haut ; onglet actif : icône et libellé en `ink` avec une petite barre blanche de 2 dp ; onglets inactifs en `mute` |
| **Étiquette (eyebrow)** | Texte `mute` 12–14 sp au-dessus d'un titre |
| **Séparateur** | Trait 1 dp `hairline` entre sections et lignes |
| **Tableau** | En-tête fond `canvasSoft`, étiquettes 12 sp `mute` ; lignes séparées par hairline ; marge de cellule 12 × 16 ; colonne du nom figée ; nombres en mono |
| **Barre de progression** | Piste `canvasMid` de 4 dp, remplissage blanc, du côté droit vers la gauche en RTL |
| **Toast** | Carte `canvasCard` + hairline, texte 14 sp, au-dessus de la barre de navigation |
| **Fenêtre modale** | Carte `canvasCard` + hairline sur voile noir à 60 % |

## 6. Écrans dans ce thème

- **لوحة القيادة** : fond `canvas`. Étiquette « الصندوق » en `mute`, puis le **montant de la caisse** en `display` (44 sp, mono, blanc). Un trait hairline, puis une grille 2 colonnes de cartes (encaissé, dépenses, solde, recouvrement) : étiquette `mute` en haut, valeur en mono dessous. Les impayés et le solde négatif en `accentSunset`. Un seul bouton blanc : « إرسال التذكيرات ».
- **المساهمات** : champ de recherche, puis une **carte par appartement** : numéro et nom arabe, pastille de statut, reste en mono, bouton contour petit « تذكير ». Vue grille (appartements × 12 mois) avec colonne du nom figée.
- **المصاريف** : cartes de dépenses (libellé, montant en mono, barre payé/reste en blanc sur `canvasMid`) ; bouton blanc « إضافة مصروف ».
- **الملصقات** : trois cartes (cotisation, relance, situation et caisse) avec aperçu miniature, bouton contour « تصدير PDF » et bouton contour « صورة ».
- **Assistant d'installation** : une question par écran ; barre de progression fine ; étiquette « الخطوة 2 من 6 » en `mute` ; titre en `title` ; champs dans une carte ; bouton blanc « التالي » et bouton contour « رجوع ».

## 7. Exports et impression

Un fond noir est adapté à l'écran, pas au papier. Deux variantes pour chaque export (affiche de cotisation, affiche de relance, situation et caisse) :

| Variante | Usage | Style |
|---|---|---|
| **Écran** (image PNG, 1080 px de large) | Partage WhatsApp | Fond `#0a0a0a`, texte blanc, hairline, statuts en accents |
| **Impression** (PDF A4) | Affichage dans l'immeuble | Adaptation : fond blanc `#ffffff`, texte `#0a0a0a`, traits `#d9dbde`, texte secondaire `#5b5f66` |

Statuts en impression (adaptation, `accentSunset` étant trop clair sur blanc, environ 2,6:1) :
- payé : ✓ en noir ;
- partiel : ◐ en `#8a5a00` ;
- retard : ! en `#b34a00` (environ 5,4:1).

Le choix de la variante se fait dans l'écran d'export (« للشاشة » / « للطباعة »), la variante impression étant proposée par défaut pour le PDF. Les nombres suivent la même règle d'isolation que dans l'application.

## 8. Adaptation RTL

- Mise en page inversée : titres et noms à droite, boutons d'action à gauche, barre de progression de droite à gauche.
- Pilules et hairlines inchangés ; icônes directionnelles inversées.
- Montants en mono, isolés en lecture gauche-à-droite (voir cahier des charges, section 3), alignés à droite dans les cartes et dans les colonnes de tableau.
- Barre système Android : icônes claires sur fond `#0a0a0a` (`StatusBar` en style clair, barre de navigation `#0a0a0a`).

## 9. Jetons du thème (TypeScript)

Le type `ThemeTokens` est défini dans `theme-citron-wise.md` (section 9). Ce thème utilise les deux champs ajoutés : `font.family.mono` et `scheme: "dark"`.

```ts
export const themeXai: ThemeTokens = {
  id: "xai",
  label: "الداكن الأنيق",
  scheme: "dark", // pas de mode clair
  colors: {
    primary: "#ffffff", onPrimary: "#0a0a0a", ink: "#ffffff", body: "#dadbdf", mute: "#7d8187",
    canvas: "#0a0a0a", canvasSoft: "#1a1c20", canvasCard: "#191919", canvasMid: "#363a3f",
    hairline: "#212327", outlineTranslucent: "rgba(255,255,255,0.25)",
    accentSunset: "#ff7a17", accentSunsetSoft: "#ffc285", accentDusk: "#7c3aed",
    accentTwilight: "#c4b5fd", accentBreeze: "#a0c3ec", accentMidnight: "#0d1726",
    // Impression (adaptation)
    printBg: "#ffffff", printInk: "#0a0a0a", printHairline: "#d9dbde", printMute: "#5b5f66",
    printPartial: "#8a5a00", printLate: "#b34a00",
  },
  radius: { field: 8, small: 8, card: 8, button: 999, pill: 999 },
  space: { xs: 4, sm: 8, md: 12, lg: 16, xl: 24, xxl: 32, xxxl: 48 },
  font: {
    family: {
      display: "IBMPlexSansArabic_400Regular",
      semibold: "IBMPlexSansArabic_400Regular", // pas de gras dans ce thème
      regular: "IBMPlexSansArabic_400Regular",
      mono: "GeistMono_400Regular",
    },
    size: { display: 44, title: 32, section: 20, body: 16, small: 14, caption: 12 },
    lineHeightArabic: 1.4,
  },
};
```

## 10. Interdits et critères de validation

Interdits du thème :
- mode clair pour l'application (les seules surfaces blanches sont les exports pour impression) ;
- texte en gras ;
- ombres portées sur les cartes ;
- plus d'un bouton blanc rempli par écran ;
- espacement négatif ou positif entre lettres arabes ;
- couleur seule pour signaler un statut (toujours icône + texte).

Critères de validation :
- Fond `#0a0a0a` sur tous les écrans, cartes `#191919` avec hairline, rayon 8 dp ; boutons en pilule.
- Au maximum un bouton principal blanc par écran ; les autres actions en contour.
- Un seul poids de police (400) dans toute l'application.
- Les trois statuts sont distinguables sans la couleur (icône + texte), et le focus d'un champ est visible.
- Montants en mono, tous les cas de test d'affichage des nombres du cahier des charges (section 3) sont corrects, à l'écran, en PDF et dans WhatsApp.
- Chaque export existe en variante écran (sombre) et en variante impression (blanche), et la variante impression reste lisible en noir et blanc.
- Les zones tactiles font au moins 48 dp.
