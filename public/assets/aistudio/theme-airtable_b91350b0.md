# Thème « الأبيض الهادئ » (style Airtable) — Application Syndicat

Description du **nouveau thème** à ajouter à l'application. Ce document décrit uniquement ce thème.

- **Source** : `airtable.md` (interprétation du langage visuel d'Airtable).
- **Identifiant** : `airtable` — libellé dans l'application : **« الأبيض الهادئ »**.
- **Choix du thème** : الإعدادات ← المظهر ← النمط, avec réglage clair / sombre / automatique (le mode sombre est dérivé, voir section 8).
- **Nombres en arabe** : la règle d'affichage (espace fine insécable + isolation gauche-à-droite) est décrite dans `syndicat-android-cahier-des-charges.md`, section 3, et s'applique ici sans changement.

---

## 1. Intention

Un style **éditorial, sobre et très lisible** : fond blanc, texte presque noir, beaucoup d'espace, un bouton principal noir. La couleur n'est pas diffuse : elle arrive par de **grandes cartes pleines** (corail foncé, vert forêt, crème, pastels chauds) qui rythment l'écran. Entre deux cartes de couleur, l'écran revient au blanc.

Règles de base du thème (reprises du fichier source) :
1. **Fond blanc, texte `ink`** (`#181d26`) ; pas de dégradé, pas de fond décoré.
2. **Bouton principal noir**, un seul par écran ; bouton secondaire blanc à contour fin.
3. **La couleur vit dans les cartes pleines** (signature cards), jamais dans de petits éléments d'accent.
4. **Poids 400 pour les titres, 500 pour les libellés et boutons** ; jamais de gras pour insister (la taille et le contraste suffisent).
5. **Couleur d'abord, ombre ensuite** : pas d'ombre, la profondeur vient du contraste entre blanc et cartes colorées.
6. **Alternance des fonds** : ne pas mettre deux bandes de même nature à la suite (blanc → carte colorée → blanc → carte crème…).

## 2. Couleurs

### Base

| Jeton | Valeur | Rôle |
|---|---|---|
| `primary` / `ink` | `#181d26` | Bouton principal, titres, texte fort |
| `primaryActive` | `#0d1218` | État pressé du bouton principal |
| `onPrimary` | `#ffffff` | Texte sur bouton principal et surfaces sombres |
| `body` | `#333840` | Texte courant |
| `muted` | `#41454d` | Légendes, libellés secondaires |
| `canvas` | `#ffffff` | Fond des écrans, cartes blanches, champs |
| `surfaceSoft` | `#f8fafc` | Cartes douces, en-têtes de tableau |
| `surfaceStrong` | `#e0e2e6` | Bandeau gris clair, piste des barres de progression |
| `surfaceDark` | `#181d26` | Carte sombre (même couleur que `primary`) |
| `hairline` | `#dddddd` | Traits fins, séparateurs, contour des boutons secondaires |
| `borderStrong` | `#9297a0` | Contour des champs de saisie (adaptation, voir contrastes) |

### Cartes de couleur (signature)

| Jeton | Valeur | Usage dans l'application |
|---|---|---|
| `signatureForest` | `#0a2e0e` | Carte « الصندوق » (caisse), texte blanc |
| `signatureCoral` | `#aa2d00` | Carte « المتبقي للتحصيل » (impayés), texte blanc ; badge « متأخر » |
| `signatureCream` | `#f5e9d4` | Carte d'information ou de rappel (sauvegarde, astuce) |
| `signatureMint` | `#a8d8c4` | Carte « المحصّل » ; badge « مؤدّى » |
| `signaturePeach` | `#fcab79` | Carte « المصاريف » |
| `signatureYellow` | `#f4d35e` | Carte « نسبة التحصيل » ; badge « جزئي » |
| `signatureMustard` | `#d9a441` | Illustrations, réserve |

> Rappel du fichier source : les valeurs de peach, mint, yellow et mustard sont inférées d'une capture d'écran ; à valider visuellement sur téléphone.

### Liens et messages

| Jeton | Valeur | Rôle |
|---|---|---|
| `link` / `linkActive` | `#1b61c9` / `#1a3866` | Liens dans le texte uniquement (jamais bouton principal) |
| `info` / `infoBorder` | `#254fad` / `#458fff` | Messages d'information, contour de focus |
| `success` / `successBorder` | `#006400` / `#39bf45` | Confirmation, montants positifs |

### Statuts (adaptation : le fichier source n'a ni « avertissement » ni « erreur »)

Proposition **à valider**, qui n'utilise que la palette du thème, toujours avec **texte + icône** :

| Statut | Fond | Texte | Icône |
|---|---|---|---|
| **مؤدّى** (payé) | `signatureMint` | `success` | ✓ |
| **جزئي** (partiel) | `signatureYellow` | `ink` | ◐ |
| **متأخر** (retard) | `signatureCoral` | blanc | ! |
| **تم التذكير** (relance envoyée) | `surfaceSoft` + contour `infoBorder` | `info` | ✉ |
| Solde positif (texte) | — | `success` | — |
| Solde négatif (texte) | — | `signatureCoral` | ▼ |

### Contrastes (valeurs approximatives)
- `ink` sur blanc : très lisible (environ 17:1) ; `body` et `muted` sur blanc : au-dessus de 9:1.
- Blanc sur `signatureCoral` : environ 6,8:1 ; blanc sur `signatureForest` : très lisible.
- `success` sur `signatureMint` : environ 4,6:1 (limite, donc badges en 14 sp minimum, poids 500).
- `hairline` sur blanc : environ 1,4:1, donc **réservé aux séparateurs**. Les **champs de saisie** utilisent `borderStrong` (environ 3:1) ; au focus, bordure `infoBorder` de 2 dp (environ 3,2:1).

## 3. Typographie

DESIGN utilise Haas / Haas Groot Disp et Inter Display, qui **ne gèrent pas l'arabe**. Le thème utilise :
- **IBM Plex Sans Arabic** (poids 400 et 500) pour tout le texte arabe ; alternatives : Cairo ou Tajawal en 400/500.
- **Inter** (poids 500 ; 475 si la police variable est disponible) pour les **grands montants**, comme la page de tarification d'Airtable le fait pour les prix. Chiffres en **chiffres tabulaires** dans les colonnes.

Règles du thème :
- **Poids** : 400 pour les titres, 500 pour libellés, boutons et grands montants. **Jamais 600 ou 700.**
- **Texte arabe** : espacement des lettres à **0** (l'espacement positif de 0,12 à 0,16 px prévu pour certains styles latins casse la liaison des lettres).
- **Interligne ≥ 1,4** (DESIGN utilise 1,25 pour le texte courant, trop serré pour l'arabe).
- **Taille du texte courant : 16 sp** (DESIGN utilise 14 px, trop petit sur téléphone pour un public varié).

| Style | Taille | Poids | Interligne | Usage |
|---|---|---|---|---|
| `display` | 40 sp | 500 | 1,2 | Montant de la caisse (Inter) |
| `title` | 28 sp | 400 | 1,4 | Titre d'écran |
| `section` | 20 sp | 400 | 1,5 | Titre de carte |
| `label` | 16 sp | 500 | 1,5 | Libellés, noms, boutons |
| `body` | 16 sp | 400 | 1,5 | Texte courant |
| `small` | 14 sp | 400 | 1,5 | Texte secondaire |
| `caption` | 14 sp | 500 | 1,4 | En-têtes de tableau, notes (`muted`) |

## 4. Formes, espacements, élévation

| Élément | Valeur |
|---|---|
| Bouton principal et secondaire | rayon **12 dp**, marge 16 × 24 (hauteur ≥ 48 dp) |
| Cartes de couleur et cartes douces | rayon **12 dp** |
| Cartes de contenu (appartements, dépenses, crème) | rayon **10 dp** |
| Champs de saisie | rayon **6 dp**, hauteur 48 dp (44 dp dans DESIGN), contour `borderStrong` |
| Badges de statut | rayon 6 dp (la forme **pilule est réservée** à la page de tarification d'Airtable et n'est pas utilisée ici) |
| Boutons icônes | cercle de 40 dp avec zone tactile de 48 dp |
| Espacements (base 4) | 4 · 8 · 12 · 16 · 24 · 32 · 48 |
| Marge d'écran | 16 dp |
| Marge intérieure des cartes de couleur | 24 dp (48 px sur le site, réduit pour téléphone) |
| Espace entre bandes | 32 à 48 dp (96 px sur le site, réduit pour téléphone) |

Élévation : **aucune ombre**. Niveau 0 : à plat. Niveau 1 : trait fin `hairline`. Cartes de couleur : contraste avec le fond blanc seulement. Focus clavier : anneau de 2 dp `infoBorder`. Fenêtres modales : carte blanche de 12 dp sur un voile `ink` à 40 % (pas d'ombre).

## 5. Composants

| Composant | Spécification |
|---|---|
| **Bouton principal** | Fond `primary`, texte blanc 16 sp en 500, rayon 12, marge 16 × 24. **Un seul par écran** (ex. « تسجيل دفعة », « إرسال التذكيرات »). Pressé : `primaryActive` |
| **Bouton secondaire** | Fond blanc, texte `ink`, contour 1 dp `hairline`, rayon 12. Ex. « تذكير » (avec icône WhatsApp) sur chaque ligne, « تصدير », « رجوع » |
| **Bouton secondaire sur carte colorée** | Fond blanc, texte `ink`, **reste blanc** sur les cartes corail, forêt et sombre |
| **Bouton icône** | Cercle 40 dp, fond blanc, contour `hairline`, icône `ink` |
| **Lien texte** | Texte `link`, sans soulignement |
| **Carte de couleur (signature)** | Fond forêt, corail ou sombre, texte blanc, rayon 12, marge 24 ; titre en `title`, chiffre en `display`, un bouton secondaire blanc |
| **Carte douce** | Fond `surfaceSoft`, rayon 12, marge 24 |
| **Carte crème (callout)** | Fond `signatureCream`, rayon 10, marge 16 à 24, texte `ink` |
| **Carte de contenu** | Fond blanc, rayon 10, marge 16, contour 1 dp `hairline` (adaptation, DESIGN ne précise pas de contour) ; une carte par appartement ou par dépense |
| **Carte pastel de chiffre** | Fond menthe, pêche ou jaune, rayon 10, marge 16 ; libellé 14 sp en haut, valeur en `label`/`display` dessous |
| **Champ de saisie** | Fond blanc, texte `ink`, rayon 6, marge 12 × 16, hauteur 48 dp, contour 1 dp `borderStrong` ; focus : contour `infoBorder` de 2 dp ; erreur (adaptation) : contour et message `signatureCoral` avec icône |
| **Pastille de statut** | Fond et texte selon le tableau des statuts, rayon 6, 14 sp en 500 |
| **Barre de navigation (bas)** | Fond blanc, trait `hairline` en haut, 5 onglets maximum ; onglet actif : icône et libellé `ink` en 500 avec une barre de 2 dp ; inactifs en `muted` |
| **Tableau** | En-tête fond `surfaceSoft` (`caption`), lignes séparées par `hairline`, marge de cellule 12 dp, colonne du nom figée |
| **Barre de progression** | Piste `surfaceStrong` de 4 dp, remplissage `ink` |
| **Bandeau gris clair** | Fond `surfaceStrong`, rayon 12, marge 24 (appel à l'action de fin d'écran) |
| **Toast** | Carte blanche, rayon 10, contour `hairline`, texte 14 sp |

Interdits du thème : lien bleu utilisé comme bouton principal, dégradé de fond, gras (600 ou 700), forme pilule pour les boutons et badges, couleurs d'accent hors de la palette ci-dessus, plus d'un bouton principal par écran, deux bandes de même nature consécutives.

## 6. Écrans dans ce thème

- **لوحة القيادة** :
  1. Fond blanc, titre de l'immeuble en `title`.
  2. **Carte forêt** « الصندوق » : montant de la caisse en `display` blanc (Inter).
  3. Deux **cartes pastel** côte à côte : « المحصّل » (menthe) et « المصاريف » (pêche).
  4. **Carte corail** « المتبقي للتحصيل » : montant des impayés et bouton blanc « عرض المتأخرين ».
  5. **Carte crème** : rappel de sauvegarde ou astuce.
  6. Un seul **bouton principal noir** : « إرسال التذكيرات ».
- **المساهمات** : champ de recherche, puis une **carte de contenu par appartement** (numéro, nom arabe, pastille de statut, reste, bouton secondaire « تذكير ») ; vue grille appartements × 12 mois avec colonne du nom figée à droite.
- **المصاريف** : cartes de dépenses (libellé, montant, barre payé/reste en `ink` sur `surfaceStrong`) ; bouton principal « إضافة مصروف ».
- **الملصقات** : trois cartes (cotisation, relance, situation et caisse) avec aperçu, bouton principal « تصدير PDF » (un seul sur l'écran, les deux autres cartes ont un bouton secondaire).
- **Assistant d'installation** : un écran par étape ; titre en `title` ; barre de progression fine ; champs dans une carte blanche ; bouton principal « التالي » et bouton secondaire « رجوع ».

## 7. Exports

Le fond blanc du thème convient directement au **papier** et à **WhatsApp** : une seule variante pour l'écran et l'impression.

- **Affiche de cotisation** : titre de l'immeuble en `title`, tableau à en-tête `surfaceSoft`, montants payés en `success`, mois manquants en `signatureCoral`, total encaissé dans un bandeau `surfaceDark` à texte blanc (variante « économie d'encre » : bandeau gris clair `surfaceStrong`).
- **Affiche de relance** : cartes ou lignes des appartements en retard avec le reste en `signatureCoral` ; mode nominatif ou anonyme.
- **Situation et état de caisse** : carte forêt pour la caisse, cartes pastel pour recettes et dépenses, tableau des dépenses ; sur papier, les cartes colorées peuvent passer en version « contour » pour économiser l'encre.
- Les nombres suivent la règle d'isolation du cahier des charges.

## 8. Mode sombre (dérivé)

Le fichier source ne définit pas de mode sombre complet (seulement des surfaces sombres). Valeurs proposées, **à valider visuellement** :

| Jeton | Valeur |
|---|---|
| Fond d'écran | `#181d26` (`surfaceDark`) |
| Carte | `#1d1f25` (`surfaceDarkElevated`) |
| Texte principal | `#ffffff` |
| Texte courant | `#c9ccd2` |
| Texte secondaire | `#9aa0aa` |
| Traits fins | `#2b2f38` |
| Bouton principal | fond blanc, texte `ink` (inversion) |
| Cartes de couleur | inchangées (corail, forêt, menthe, pêche, jaune) |

## 9. Adaptation RTL

- Mise en page inversée : titres, noms et colonne figée à droite ; boutons d'action à gauche des cartes.
- Barre de progression de droite à gauche ; icônes directionnelles inversées.
- Montants en Inter, isolés en lecture gauche-à-droite, alignés à droite dans les cartes et les colonnes.

## 10. Jetons du thème (TypeScript)

Le type `ThemeTokens` est défini dans `theme-citron-wise.md` (section 9). Ce thème utilise le champ ajouté `font.family.numeric` (police des grands montants).

```ts
export const themeAirtable: ThemeTokens = {
  id: "airtable",
  label: "الأبيض الهادئ",
  scheme: "auto",
  colors: {
    primary: "#181d26", primaryActive: "#0d1218", onPrimary: "#ffffff",
    ink: "#181d26", body: "#333840", muted: "#41454d",
    canvas: "#ffffff", surfaceSoft: "#f8fafc", surfaceStrong: "#e0e2e6",
    surfaceDark: "#181d26", surfaceDarkElevated: "#1d1f25",
    hairline: "#dddddd", borderStrong: "#9297a0",
    signatureCoral: "#aa2d00", signatureForest: "#0a2e0e", signatureCream: "#f5e9d4",
    signaturePeach: "#fcab79", signatureMint: "#a8d8c4", signatureYellow: "#f4d35e",
    signatureMustard: "#d9a441",
    link: "#1b61c9", linkActive: "#1a3866", info: "#254fad", infoBorder: "#458fff",
    success: "#006400", successBorder: "#39bf45",
    // Statuts (adaptation)
    statusPaidBg: "#a8d8c4", statusPaidText: "#006400",
    statusPartialBg: "#f4d35e", statusPartialText: "#181d26",
    statusLateBg: "#aa2d00", statusLateText: "#ffffff",
  },
  radius: { field: 6, small: 10, card: 12, button: 12, pill: 9999 },
  space: { xs: 4, sm: 8, md: 12, lg: 16, xl: 24, xxl: 32, xxxl: 48 },
  font: {
    family: {
      display: "IBMPlexSansArabic_400Regular",
      semibold: "IBMPlexSansArabic_500Medium", // poids maximal du thème
      regular: "IBMPlexSansArabic_400Regular",
      numeric: "Inter_500Medium",
    },
    size: { display: 40, title: 28, section: 20, body: 16, small: 14, caption: 14 },
    lineHeightArabic: 1.5,
  },
};
```

## 11. Critères de validation

- Fond blanc, cartes de couleur pleines (forêt, corail, menthe, pêche, crème) à rayon 10 ou 12 dp, sans ombre ni dégradé.
- **Un seul bouton principal noir par écran** ; les autres actions sont des boutons secondaires blancs à contour fin.
- Aucun texte en 600 ou 700 ; aucun espacement de lettres sur le texte arabe ; interligne ≥ 1,4.
- Les trois statuts sont lisibles sans la couleur (texte + icône), les champs ont un contour d'environ 3:1 et un focus visible.
- Deux bandes de même nature ne se suivent pas sur le tableau de bord.
- Les grands montants utilisent la police numérique, et tous les cas de test d'affichage des nombres du cahier des charges (section 3) sont corrects à l'écran, en PDF et dans WhatsApp.
- Zones tactiles ≥ 48 dp ; les exports restent lisibles en noir et blanc.
