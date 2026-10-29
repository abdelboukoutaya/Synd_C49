# Application Android « إدارة السانديك » — Cahier des charges

Gestion d'un syndicat de copropriété (cotisations, impayés, dépenses, caisse) avec **interface 100 % en arabe (RTL)**, pensée pour un usage **sur téléphone**, avec relances WhatsApp et exports PDF/image.

Ce document part de la version web actuelle (cotisations, dépenses, relances, affiche) et la transforme en application Android autonome et réutilisable pour **n'importe quel immeuble**.

---

## 1. Objectifs

1. Permettre à un trésorier/syndic de configurer son immeuble en quelques minutes (étages, appartements, propriétaires, cotisation).
2. Suivre les paiements mois par mois sur toute l'année, y compris les mois passés.
3. Relancer les retardataires par WhatsApp en un clic, avec un message en arabe personnalisé.
4. Produire des documents prêts à afficher ou à partager : **affiche de relance**, **affiche de cotisation**, **situation des dépenses et état de caisse**.
5. Fonctionner **hors ligne**, avec les données **stockées sur le téléphone** et une sauvegarde simple.

## 2. Choix techniques recommandés

| Besoin | Choix | Pourquoi |
|---|---|---|
| Framework | **React Native + Expo (TypeScript)** | Même écosystème React/TypeScript que vos autres projets, build Android simple (EAS) |
| Alternative | **Capacitor + React** | Réutilise directement le code HTML/JS de la version web actuelle |
| Base locale | **SQLite** (`expo-sqlite`) + **Drizzle ORM** | Prisma ne fonctionne pas dans React Native ; Drizzle donne un typage similaire |
| Arabe / RTL | `I18nManager.forceRTL(true)` + `i18n-js` ou `i18next` | Interface entièrement inversée |
| Police | **Cairo** ou **Tajawal** (Google Fonts, embarquée) | Lisible sur petit écran, chiffres propres |
| PDF | `expo-print` (HTML → PDF) | Mise en page contrôlée en HTML/CSS, A4 |
| Image (PNG) | `react-native-view-shot` | Affiche en image pour WhatsApp |
| Partage | `expo-sharing` + `Linking` (`https://wa.me/...`) | Partage PDF/image et ouverture de WhatsApp |
| Notifications | `expo-notifications` | Rappels locaux (sans serveur) |
| Sécurité | `expo-local-authentication` | Code/empreinte pour ouvrir l'application |
| Sauvegarde | `expo-file-system` + `expo-document-picker` | Export/import d'un fichier JSON |

> Si vous voulez aller vite : commencer avec **Capacitor** en réutilisant la page actuelle, puis migrer vers React Native si besoin.

## 3. Interface en arabe

- **Sens RTL** partout (navigation, tableaux, formulaires, icônes directionnelles inversées).
- **Textes** : tous les libellés en arabe (fichier de traduction unique `ar.json`, prévoir `fr.json` en option).
- **Mois** (arabe marocain) : يناير، فبراير، مارس، أبريل، ماي، يونيو، يوليوز، غشت، شتنبر، أكتوبر، نونبر، دجنبر.
- **Monnaie** : « درهم » (ou « د.م. »), séparateur de milliers, chiffres occidentaux (0-9) par défaut, option chiffres arabes (٠-٩).
- **Étages** : الطابق الأرضي، الطابق الأول، الثاني، الثالث…
- **Noms des propriétaires** : champ **nom en arabe** (obligatoire pour l'affichage et les messages) + champ **nom latin** (optionnel, utile pour la recherche).
- **Thème** : clair et sombre (suit le système).

Exemples de libellés :

| Écran / bouton | Libellé arabe |
|---|---|
| Tableau de bord | لوحة القيادة |
| المساهمات (cotisations) | المساهمات |
| المصاريف (dépenses) | المصاريف |
| الملصقات (affiches) | الملصقات |
| Relancer | تذكير |
| Sauvegarder | حفظ نسخة احتياطية |
| Restaurer | استرجاع |
| Exporter | تصدير |
| Caisse | الصندوق |

## 3 bis. Design (charte graphique inspirée de DESIGN.md)

Le fichier `DESIGN.md` décrit un langage visuel « fintech calme » : **vert citron comme unique accent**, fond **sauge** clair, **cartes blanches très arrondies**, titres **très gras** et peu d'ombres. Cette section l'adapte à une application de syndic, sur téléphone, en arabe.

### Ce qui est repris et ce qui est adapté

| Élément de DESIGN.md | Dans l'application |
|---|---|
| Vert citron `#9fe870` = seule couleur d'action | Tous les boutons principaux (ajouter un paiement, exporter, **تذكير**) |
| Fond sauge `#e8ebe6` + cartes blanches | Fond de tous les écrans ; chaque appartement, dépense et chiffre clé est une carte blanche |
| Rayon 24 px pour cartes et boutons | 24 dp partout (champs de saisie : 12 dp) |
| Pas d'ombre : le contraste des surfaces fait l'élévation | Idem ; seule la fenêtre modale a une ombre légère |
| Titres en poids 900, reste en 600 | Montants et titres d'écran en **Cairo Black 900**, texte courant en 400/600 |
| Carte sombre à texte vert (`card-feature-dark`) | Carte « المتبقي للتحصيل » (impayés) sur le tableau de bord |
| Le vert de marque n'indique jamais un succès | « Payé » = palette sémantique (`positive`), pas le vert citron |
| **Wise Sans / Inter** | Ne gèrent pas l'arabe → remplacées par **Cairo** (voir ci-dessous) |
| Titres de 64 à 126 px | Réduits pour mobile : 40 à 48 sp au maximum |
| Pas de second accent de marque | Le bouton WhatsApp n'a **pas** son propre vert : c'est le bouton primaire citron avec l'icône WhatsApp |
| Mode sombre (non défini dans DESIGN.md) | Dérivé : fond `ink`, cartes `#171916`, textes et boutons en citron (valeurs à valider) |

### Couleurs

| Rôle | Valeur | Utilisation |
|---|---|---|
| `primary` | `#9fe870` | Boutons principaux, onglet actif, indicateur de sélection |
| `on-primary` / `ink` | `#0e0f0c` | Texte sur le vert citron, titres, bordures des champs |
| `primary-pale` | `#e2f6d5` | Fond du badge « مؤدّى », carte d'information positive |
| `primary-active` | `#cdffad` | État pressé d'un bouton principal |
| `canvas-soft` | `#e8ebe6` | Fond des écrans, bouton secondaire, en-tête de tableau |
| `canvas` | `#ffffff` | Cartes, barre de navigation, champs, affiches |
| `body` | `#454745` | Texte secondaire (noms de colonnes, sous-titres) |
| `mute` | `#868685` | Texte indicatif (placeholder, légendes) |
| `positive` / `positive-deep` | `#2ead4b` / `#054d28` | Montants payés, solde positif, texte du badge « payé » |
| `warning` / `warning-content` | `#ffd11a` / `#4a3b1c` | Paiement partiel, rappel envoyé récemment |
| `negative` / `negative-deep` | `#d03238` / `#a72027` | Retard, solde négatif ; `negative-deep` sur fond sauge |
| `negative-bg` | `#320707` | Fond du badge « متأخر » (texte blanc) |
| `ink-deep` | `#163300` | Texte sur surfaces vertes |

Contrastes à respecter : le texte `ink` sur le vert citron est très lisible (environ 13:1). `mute` sur blanc n'atteint qu'environ 3,6:1, donc ne l'utiliser que pour les placeholders ; pour une information utile, prendre `body`. Sur fond sauge, écrire les retards en `negative-deep` (environ 6:1) plutôt qu'en `negative`.

### Typographie

- **Police : Cairo** (arabe + chiffres latins). Poids 900 pour les titres et les chiffres clés, 600 pour les libellés et boutons, 400 pour le texte courant. Alternative : **Tajawal**.
- **Arabe** : pas d'espacement négatif entre lettres (il coupe la liaison des lettres) et interligne **≥ 1,3** (les interlignes très serrés de DESIGN.md sont prévus pour le latin).
- Alignement à droite, chiffres et téléphones en LTR isolé.

| Style | Taille | Poids | Usage |
|---|---|---|---|
| `display` | 40–48 sp | 900 | Montant de la caisse sur le tableau de bord |
| `title` | 24 sp | 900 | Titre d'écran |
| `section` | 20 sp | 600 | Titre de carte |
| `body` | 16 sp | 400 | Texte courant |
| `body-strong` | 14–16 sp | 600 | Noms, libellés, boutons |
| `caption` | 12 sp | 400 | En-têtes de tableau, notes |

### Formes et espacements

- Rayons : boutons et cartes **24 dp**, champs **12 dp**, badges **pilule (999)**, petites puces **8 dp**.
- Espacements (base 4) : 4 · 8 · 12 · 16 · **24** (intérieur des cartes) · 32 · 48.
- Marge latérale d'écran : 16 dp ; espace entre cartes : 12 dp.
- Zones tactiles **≥ 48 dp** (bouton : 12 dp de marge verticale + ligne de 24 dp).

### Composants

| Composant | Spécification |
|---|---|
| **Bouton principal** | Fond `primary`, texte `ink` 600 16 sp, rayon 24, marge 12 × 24. Exemples : « تسجيل دفعة », « تصدير », « تذكير » (avec icône WhatsApp) |
| **Bouton secondaire** | Fond `canvas-soft`, texte `ink`, même forme |
| **Bouton tertiaire** | Fond blanc, bordure 1 dp `ink`, même forme (annuler, retour) |
| **Bouton icône rond** | Fond blanc, icône `ink`, cercle (recherche, filtre, partage) |
| **Champ de saisie** | Fond blanc, bordure 1 dp `ink`, rayon 12, marge 12 × 16, texte 16 sp |
| **Carte** | Fond blanc, rayon 24, marge 24, sans bordure ni ombre, posée sur fond sauge |
| **Carte sauge** | Fond `canvas-soft` sur fond blanc (zones de résumé, état vide) |
| **Carte verte pâle** | Fond `primary-pale` (information positive : « كل الشقق مؤدّية ») |
| **Carte sombre** | Fond `ink`, texte `primary` (montant des impayés, appel à l'action de relance) |
| **Badge « مؤدّى »** | Fond `primary-pale`, texte `positive-deep`, pilule, 14 sp en 600 |
| **Badge « متأخر »** | Fond `negative-bg`, texte blanc, pilule |
| **Badge « جزئي »** | Fond `warning`, texte `warning-content`, pilule |
| **Barre de navigation (bas)** | Fond blanc, 5 onglets max ; onglet actif = pastille `primary` derrière l'icône et libellé en 600 |
| **Tableau de données** | En-tête fond `canvas-soft`, texte `caption` ; lignes séparées par un trait `canvas-soft` ; marge de cellule 12 × 16 ; colonne du nom figée |
| **Toast / message** | Carte blanche arrondie 24, texte 14 sp, apparaît en bas au-dessus de la barre |
| **Fenêtre modale** | Carte blanche arrondie 24 avec ombre légère |

### Écrans

- **Tableau de bord** : fond sauge. Une carte blanche avec la **caisse en grand** (`display`, 900), puis une grille 2 colonnes de cartes (encaissé, dépenses, solde, taux de recouvrement). Une **carte sombre** « المتبقي للتحصيل » (texte vert citron) avec le bouton principal « إرسال التذكيرات ». Si tout est payé, carte verte pâle.
- **المساهمات** : barre de recherche (champ arrondi + bouton rond de filtre), puis une **carte blanche par appartement** : numéro, nom arabe, badge de statut, reste en gras ; bouton « تذكير » à l'extrémité de la carte. Bascule en **vue grille** (tableau appartements × 12 mois) pour la saisie rapide.
- **المصاريف** : cartes de dépenses (libellé, montant, barre de progression payé/reste en `primary`), bouton principal flottant « إضافة مصروف ».
- **الملصقات** : trois grandes cartes (affiche de cotisation, affiche de relance, situation et caisse), chacune avec aperçu miniature, bouton principal « تصدير PDF » et bouton secondaire « صورة ».
- **Assistant d'installation** : une question par écran, barre de progression en `primary`, grand titre 900, carte blanche contenant les champs, bouton principal en bas (« التالي »), bouton tertiaire « رجوع ».
- **Affiches exportées** : fond blanc, titre en Cairo 900, tableau à en-tête sauge ; montants payés en `positive-deep` sur `primary-pale`, mois manquants en `negative-deep`, total encaissé dans une bande `ink` avec texte vert citron. Version « économie d'encre » sans aplats sombres en option.

### Adaptation RTL
- Mise en page inversée : titre et nom à droite, actions à gauche, barre de progression qui se remplit de droite à gauche.
- Flèches et chevrons inversés ; l'onglet actif et les indicateurs suivent le sens de lecture.
- Montants avec « درهم » après le chiffre, numéros de téléphone isolés en LTR.

### Jetons de design (TypeScript)

```ts
export const colors = {
  primary: "#9fe870", onPrimary: "#0e0f0c", primaryActive: "#cdffad", primaryPale: "#e2f6d5",
  ink: "#0e0f0c", inkDeep: "#163300", body: "#454745", mute: "#868685",
  canvas: "#ffffff", canvasSoft: "#e8ebe6",
  positive: "#2ead4b", positiveDeep: "#054d28",
  warning: "#ffd11a", warningContent: "#4a3b1c",
  negative: "#d03238", negativeDeep: "#a72027", negativeBg: "#320707",
};
// Mode sombre : dérivé de DESIGN.md (non défini dans le fichier source), à valider visuellement
export const colorsDark = {
  ...colors, canvasSoft: "#0e0f0c", canvas: "#171916", ink: "#e8ebe6", body: "#b9bdb6",
};
export const radius = { field: 12, small: 8, card: 24, button: 24, pill: 999 };
export const space = { xs: 4, sm: 8, md: 12, lg: 16, xl: 24, xxl: 32, xxxl: 48 };
export const font = {
  family: { display: "Cairo_900Black", semibold: "Cairo_600SemiBold", regular: "Cairo_400Regular" },
  size: { display: 44, title: 24, section: 20, body: 16, small: 14, caption: 12 },
  lineHeightArabic: 1.4,
};
```

---

## 4. Assistant de première installation (onboarding)

Au premier lancement, un assistant en étapes demande les informations essentielles. Chaque étape peut être modifiée plus tard dans **الإعدادات**.

### Étape 1 — L'immeuble
- Nom de l'immeuble / résidence (ex. « C49 GH12 – بيت المستقبل 2 »)
- Ville (optionnel)
- Nom du syndic/trésorier (optionnel, apparaît sur les documents)

### Étape 2 — Étages et appartements
- **Nombre d'étages** (avec ou sans rez-de-chaussée : interrupteur « يوجد طابق أرضي »).
- **Nombre d'appartements par étage** :
  - mode simple : même nombre pour tous les étages ;
  - mode avancé : un nombre différent par étage.
- **Numérotation** : automatique (1, 2, 3… en continu) ou personnalisée (A1, B2…).
- Aperçu : liste des appartements générés, modifiables un par un.
- Option par appartement : **فارغة** (vide), **معفاة** (exonérée), **كراء** (locataire).

### Étape 3 — Les propriétaires
Pour chaque appartement :
- Nom en arabe (obligatoire), nom latin (optionnel)
- **Numéro de téléphone WhatsApp** (utilisé pour les relances), validation du format marocain (06 / 07, converti en +212)
- Option : **importer depuis les contacts** du téléphone
- Option : **saisie rapide** en liste (une ligne par appartement) ou import d'un fichier Excel/CSV

### Étape 4 — La cotisation
- **Année de gestion** (ex. 2026) : l'application génère les **12 mois**.
- **Montant par défaut** de la cotisation mensuelle.
- **Personnalisation de mois précis** : tableau des 12 mois où l'on peut modifier le montant d'un mois (ex. juin–juillet à 100 DH, le reste à 50 DH) ou désactiver un mois (aucune cotisation).
- **Montant différent par appartement** (option : tarif spécial, exonération).
- **Jour limite de paiement** (ex. le 5 de chaque mois) pour calculer les retards.

### Étape 5 — Historique (mois précédents)
- Choix : **« أريد إدخال الدفعات السابقة »** ou **« ابدأ من الصفر »** (laisser vide).
- Si saisie : grille appartements × mois déjà écoulés, avec saisie rapide (appui long = « payé en totalité »).
- Possibilité de renseigner un **solde initial de la caisse** (report des années précédentes).

### Étape 6 — Récapitulatif
- Résumé (nombre d'appartements, cotisation, mois ouverts, total déjà encaissé).
- Bouton **« ابدأ »** → tableau de bord.

## 5. Modèle de données (SQLite)

```
building(id, name, city, treasurer, currency, rtl, digits_ar, created_at)
floor(id, building_id, level, label_ar)
unit(id, floor_id, number, status)               -- status: occupied | empty | exempt
owner(id, unit_id, name_ar, name_latin, phone_e164, is_tenant)
fiscal_year(id, building_id, year, opening_balance)
month_fee(id, fiscal_year_id, month_index, amount_default, enabled)
unit_fee_override(id, unit_id, month_index, amount)    -- tarif spécial par appartement/mois
payment(id, unit_id, fiscal_year_id, month_index, amount, paid_at, method, note, receipt_no)
expense(id, fiscal_year_id, label, category, amount, paid_amount, date, supplier, receipt_photo_uri)
reminder_log(id, unit_id, sent_at, channel, message_hash)
setting(key, value)                                 -- langue, modèle de message, PIN, etc.
```

Règles de calcul :
- **Dû à fin du mois M** = somme des cotisations activées de janvier (ou premier mois ouvert) à M.
- **Reste** = max(0, dû − total payé). **Avance** = max(0, total payé − dû).
- **Encaissé** = somme de tous les paiements.
- **Dépenses** = somme des montants ; **payé** = somme des montants réglés ; **reste à payer aux prestataires** = dépenses − payé.
- **Solde (recettes − dépenses)** = encaissé + solde initial − dépenses.
- **En caisse maintenant** = encaissé + solde initial − dépenses payées.
- Un paiement peut couvrir **plusieurs mois** (le montant se répartit sur les mois les plus anciens en retard).

## 6. Écrans principaux

1. **لوحة القيادة (Tableau de bord)** : encaissé, dépenses, solde, caisse, taux de recouvrement, nombre d'impayés, liste de relance, raccourcis d'export.
2. **المساهمات (Cotisations)** :
   - vue **liste** (une carte par appartement : nom, statut, reste, bouton « تذكير ») ;
   - vue **grille** (appartements × mois) avec défilement horizontal et colonne du nom figée ;
   - saisie d'un paiement par appui sur la case du mois (clavier numérique, bouton « دفع كامل »).
3. **المصاريف (Dépenses)** : liste, ajout rapide, montant payé / reste, catégorie, photo du justificatif.
4. **الصندوق (Caisse)** : situation de caisse, mouvements, graphique simple mensuel.
5. **الملصقات (Affiches et exports)** : les trois exports (section 8).
6. **الإعدادات (Réglages)** : immeuble, étages/appartements, propriétaires, cotisations, modèles de messages, sécurité, sauvegarde, langue.

### Affichage adapté au téléphone
- Navigation par **barre d'onglets en bas** (5 onglets max), zones tactiles ≥ 48 dp.
- Tableaux larges : **colonne du nom figée** + défilement horizontal, ou bascule en vue « cartes ».
- Saisie : clavier numérique pour les montants, `type=tel` pour les téléphones, pas de fenêtres de confirmation bloquantes (confirmation par second appui ou « تراجع » après action).
- Gestion des **zones sûres** (encoche, barre de navigation) et du **mode paysage** (affiche en grille complète).
- Taille de police réglable (petite / normale / grande).

## 7. Relances WhatsApp

**Modèle de message par défaut (arabe)**, modifiable dans les réglages :

```
السلام عليكم {الاسم}
تذكير بسيط بخصوص المساهمة الشهرية، المبلغ المتبقي هو {المبلغ} درهم الى غاية نهاية شهر {الشهر}.
و شكرا على تفهمكم
```

- **Variables** : `{الاسم}`, `{المبلغ}`, `{الشهر}`, `{رقم_الشقة}`, `{اسم_العمارة}`.
- **Envoi** : ouverture de `https://wa.me/<numéro>?text=<message encodé>` (numéro converti en format international `212…`).
- **File de relance** : liste des retardataires triée par montant dû, un bouton **« تذكير »** par ligne, et marquage automatique « تم التذكير » (date) après ouverture de WhatsApp.
- **Choix du mois de référence** (mois en cours par défaut) et **seuil minimal** de relance (ex. ne pas relancer sous 20 DH).
- **Journal** des relances (qui, quand) pour éviter les doublons.

> Limite : un envoi 100 % automatique (sans appui sur « Envoyer ») nécessite l'API WhatsApp Business et un serveur ; ce n'est pas prévu dans la v1.

## 8. Exports

Chaque export est disponible en **PDF (A4)** et en **image PNG** (adaptée à WhatsApp), avec boutons **تصدير** et **مشاركة**.

### 8.1 Affiche de cotisation
- Titre : nom de l'immeuble + année + date de mise à jour.
- Tableau : étage, appartement, propriétaire, **12 mois** (montant payé ou tiret pour un mois en retard), total par ligne.
- Pied : total encaissé (caisse).
- Options : afficher ou masquer les noms, afficher uniquement jusqu'au mois en cours, **QR code** vers une version consultable (option avancée).

### 8.2 Affiche de relance
- Liste des appartements en retard avec **montant restant** et mois concernés.
- Deux modes : **nominatif** (noms visibles) ou **anonyme** (numéros d'appartement seulement), à choisir avant l'export.
- Texte d'en-tête personnalisable (ex. « نرجو من السادة المتأخرين تسوية وضعيتهم »).

### 8.3 Situation des dépenses et état de caisse
- **Recettes** : total encaissé, report initial.
- **Dépenses** : tableau (libellé, montant, payé, reste, date, prestataire) + total.
- **État de caisse** : solde (recettes − dépenses), caisse disponible, reste à payer aux prestataires.
- Récapitulatif par catégorie et mention « حرر بتاريخ … ».
- Option : bilan annuel prêt pour l'**assemblée générale**.

## 9. Sauvegarde et sécurité

- **Sauvegarde manuelle** : export d'un fichier `.json` (partage par WhatsApp, Drive, e-mail) et **restauration** par import.
- **Sauvegarde automatique** hebdomadaire dans le stockage de l'application, avec rappel « لم تقم بنسخة احتياطية منذ 7 أيام ».
- **Verrouillage** par code PIN ou empreinte.
- Aucune donnée n'est envoyée sur un serveur : tout reste sur le téléphone.

## 10. Propositions pour faciliter l'utilisation

Classées par priorité.

### Priorité haute (v1)
| Proposition | Intérêt |
|---|---|
| **Import Excel/CSV** du fichier existant | Éviter la ressaisie de 30 appartements |
| **Import des contacts** du téléphone | Remplir les numéros en quelques appuis |
| **Paiement partiel / multi-mois / avance** géré automatiquement | Cas réels très fréquents |
| **Reçu de paiement** (PDF ou image, numéroté) à envoyer au payeur | Preuve de paiement, moins de litiges |
| **Rappel mensuel** par notification (« حان وقت جمع المساهمات ») | Régularité du suivi |
| **Annuler la dernière action** (« تراجع ») | Corriger une erreur de saisie |
| **Recherche rapide** d'un appartement ou d'un nom | Gain de temps |
| **Alertes d'incohérence** (total ≠ somme des lignes, paiement négatif) | Éviter les écarts de caisse |

### Priorité moyenne (v1.5)
| Proposition | Intérêt |
|---|---|
| **Catégories de dépenses** + photo du justificatif | Transparence et preuve |
| **Dépenses prévues / budget** et fonds de réserve | Anticiper les gros travaux |
| **Historique complet par appartement** (paiements, relances) | Réponse rapide aux contestations |
| **Cotisation exceptionnelle** (ex. travaux) à répartir sur tous | Gérer les appels de fonds ponctuels |
| **Locataire / propriétaire** : contact de relance différent | Relancer la bonne personne |
| **Mode lecture seule** partageable (PDF mis à jour automatiquement) | Informer sans donner l'accès |
| **Graphiques** (encaissements par mois, taux de recouvrement par étage) | Lecture rapide de la situation |
| **Export Excel** de toutes les données | Archivage et comptabilité |
| **Texte de relance progressif** (1er rappel poli, 2e plus ferme) | Meilleur taux de paiement |

### Priorité basse (v2)
| Proposition | Intérêt |
|---|---|
| **Multi-utilisateurs** (trésorier + adjoint) avec synchronisation cloud optionnelle | Travail à deux sans doublons |
| **Compte rendu d'assemblée** (convocation, présence, décisions) | Tout le suivi du syndicat au même endroit |
| **Suivi des prestataires et contrats** (caméras, ascenseur, nettoyage) | Échéances et garanties |
| **Signalement de pannes** par les résidents | Maintenance mieux suivie |
| **Envoi automatique WhatsApp** via API Business | Zéro action manuelle |
| **Multi-immeubles** | Gérer plusieurs syndicats |
| **Version française** de l'interface | Syndics non arabophones |

## 11. Plan de développement

| Étape | Contenu | Durée indicative |
|---|---|---|
| 1 | Projet Expo/TS, RTL, thème, base SQLite, modèle de données | 2 à 3 jours |
| 2 | Assistant de première installation (6 étapes) | 3 à 4 jours |
| 3 | Écrans cotisations (liste + grille), saisie de paiements, calculs | 4 à 5 jours |
| 4 | Dépenses, caisse, tableau de bord | 3 jours |
| 5 | Relances WhatsApp, file de relance, modèles de messages | 2 jours |
| 6 | Exports PDF/PNG (3 documents) et partage | 3 à 4 jours |
| 7 | Sauvegarde/restauration, PIN, import Excel/contacts | 3 jours |
| 8 | Tests sur plusieurs téléphones, correction, icône, build APK/AAB | 3 jours |

## 12. Critères d'acceptation

- L'assistant génère correctement les appartements pour **N étages × M appartements** (uniforme ou variable).
- Toute l'interface est en arabe, sans texte coupé ni inversé, sur écrans de 5" à 6,7".
- Un mois peut avoir une **cotisation différente** ; le calcul du dû et du reste en tient compte.
- Les **mois précédents** peuvent être saisis à l'installation ou laissés vides.
- Le bouton « تذكير » ouvre WhatsApp avec le **message exact du modèle**, le prénom en arabe, le bon montant et le bon mois.
- Les **trois exports** se génèrent en PDF et en image, et se partagent depuis le téléphone.
- Le total encaissé de l'affiche est **égal** à la somme des paiements ; le solde = recettes − dépenses.
- Les données survivent à la fermeture de l'application et sont **restaurables** depuis une sauvegarde.
- L'application fonctionne **sans connexion Internet** (hors ouverture de WhatsApp).

## 13. Enseignements de la version web actuelle

- Les totaux saisis à la main provoquaient des **écarts** (reste de caisse différent selon les feuilles) : tous les totaux de la nouvelle application doivent être **calculés**, jamais saisis.
- Un même chiffre a été écrit avec deux valeurs (prix unitaire / total) : prévoir un contrôle de cohérence entre montant, payé et reste.
- Les données stockées dans le navigateur peuvent être perdues : prévoir la **sauvegarde automatique** et les rappels de sauvegarde dès la v1.
- Les fenêtres de confirmation du navigateur ne sont pas toujours disponibles : privilégier la confirmation par second appui ou l'annulation.
