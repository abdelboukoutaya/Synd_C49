const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_syndic_1";
const BOB_UID = "bob_syndic_2";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read or write apartments or projects", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("apartments").get()
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("projects").get()
  );
});

test("Authenticated owner can create and update apartment with Arabic name, WhatsApp phone, and unit status", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date();
  const apptRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("apartments")
    .doc("appt_01");

  await assertSucceeds(
    apptRef.set({
      userId: ALICE_UID,
      number: 1,
      customNumberLabel: "A1",
      floor: "RDC",
      status: "occupied",
      ownerName: "Abdelfetah",
      ownerNameAr: "عبد الفتاح",
      phone: "+212612345678",
      isTenant: false,
      customMonthlyFee: -1,
      lastReminderAt: 0,
      receiptCount: 1,
      jan: 50,
      feb: 50,
      mar: 50,
      apr: 50,
      may: 50,
      juin: 100,
      juil: 100,
      aout: 50,
      sept: 50,
      oct: 0,
      nov: 0,
      dec: 0,
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    apptRef.update({
      oct: 50,
      lastReminderAt: Date.now(),
      updatedAt: new Date(),
    })
  );
});

test("SyndicatConfig supports Arabic RTL, WhatsApp template, openingBalanceDh, and variable floorAptCounts", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date();
  const cfgRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("settings")
    .doc("syndicat_config");

  await assertSucceeds(
    cfgRef.set({
      userId: ALICE_UID,
      language: "ar",
      digitsAr: false,
      themeStyle: "airtable",
      themeMode: "system",
      fontScale: "normal",
      syndicatName: "إدارة السانديك",
      residenceName: "C49 GH12 – بيت المستقبل 2",
      address: "GH12",
      city: "الدار البيضاء",
      treasurerName: "عبد العظيم",
      buildingTitle: "المساهمة الشهرية 2026",
      currency: "درهم",
      year: 2026,
      openingBalanceDh: 1500,
      dueDayOfMonth: 5,
      minReminderAmountDh: 20,
      whatsappTemplate: "السلام عليكم {الاسم} المبلغ المتبقي {المبلغ} درهم",
      relanceHeaderNote: "نرجو من السادة المتأخرين تسوية وضعيتهم",
      includeRdc: true,
      floorsCount: 5,
      apartmentsCount: 30,
      floorAptCounts: [6, 6, 6, 6, 6],
      defaultMonthlyFee: 50,
      cutoffMonth: 10,
      activeMonths: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12],
      feeJan: 50,
      feeFeb: 50,
      feeMar: 50,
      feeApr: 50,
      feeMay: 50,
      feeJuin: 100,
      feeJuil: 100,
      feeAout: 50,
      feeSept: 50,
      feeOct: 50,
      feeNov: 50,
      feeDec: 50,
      footerNote: "يحين كل أسبوع",
      onboardingCompleted: true,
      seeded: true,
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    cfgRef.update({
      language: "fr",
      themeStyle: "xai",
      openingBalanceDh: 2500,
      updatedAt: new Date(),
    })
  );

  await assertFails(
    cfgRef.update({
      themeStyle: "invalid_theme",
      updatedAt: new Date(),
    })
  );

  await assertFails(
    cfgRef.update({
      unexpectedGhostKey: "hack",
      updatedAt: new Date(),
    })
  );
});

test("ExpenseRecord supports partial payment (paidAmountDh) and supplier while blocking cross-user access and ghost fields", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date();
  const expRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("expenses")
    .doc("exp_01");

  await assertSucceeds(
    expRef.set({
      userId: ALICE_UID,
      title: "إصلاح المصعد",
      category: "Ascenseur",
      amountDh: 1200,
      paidAmountDh: 800,
      supplier: "شركة المصاعد",
      dateLabel: "24/10/2026",
      notes: "الدفعة الأولى",
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertFails(
    bobDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("expenses")
      .doc("exp_01")
      .get()
  );

  await assertFails(
    expRef.update({
      isAdminApproved: true,
      updatedAt: new Date(),
    })
  );
});

test("ProjectRecord supports bilingual AR/FR fields, priority 1..4, progress 0..100, and caisse advance while enforcing strict security rules", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date();
  const projRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("projects")
    .doc("proj_01");

  await assertSucceeds(
    projRef.set({
      userId: ALICE_UID,
      titleAr: "تركيب كاميرات المراقبة",
      titleFr: "Installation caméras de surveillance",
      descriptionAr: "تأمين مدخل العمارة والدرج",
      descriptionFr: "Sécurisation du hall et des escaliers",
      category: "Sécurité",
      priority: 1,
      status: "planned",
      targetBudgetDh: 2200,
      minCashToStartDh: 1100,
      allocatedAmountDh: 0,
      progressPercent: 0,
      contractor: "SecuriMaroc",
      targetDateLabel: "01/12/2026",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Client-aligned query with userId filter succeeds
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("projects")
      .where("userId", "==", ALICE_UID)
      .get()
  );

  // Starting the project and updating advancement state succeeds
  await assertSucceeds(
    projRef.update({
      status: "in_progress",
      progressPercent: 45,
      allocatedAmountDh: 1100,
      updatedAt: new Date(),
    })
  );

  // Cross-user read & write rejected
  await assertFails(
    bobDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("projects")
      .doc("proj_01")
      .get()
  );
  await assertFails(
    bobDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("projects")
      .doc("proj_01")
      .delete()
  );

  // Out-of-bounds progress (> 100) or priority (5) rejected
  await assertFails(
    projRef.update({
      progressPercent: 120,
      updatedAt: new Date(),
    })
  );
  await assertFails(
    projRef.update({
      priority: 5,
      updatedAt: new Date(),
    })
  );

  // Both titles empty rejected
  await assertFails(
    projRef.update({
      titleAr: "",
      titleFr: "",
      updatedAt: new Date(),
    })
  );

  // Ghost field rejected
  await assertFails(
    projRef.update({
      extraSecretField: "nope",
      updatedAt: new Date(),
    })
  );
});

test("SyndicatConfig accepts meta, coinbase, and minimax themeStyle values and rejects unknown styles", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date();
  const cfgRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("settings")
    .doc("syndicat_config");

  const baseConfig = {
    userId: ALICE_UID,
    language: "fr",
    digitsAr: false,
    themeStyle: "meta",
    themeMode: "system",
    fontScale: "normal",
    syndicatName: "Syndic C49",
    residenceName: "Résidence C49",
    address: "Quartier El Mostaqbal",
    city: "Casablanca",
    treasurerName: "Trésorier",
    buildingTitle: "Cotisation Mensuelle – C49",
    currency: "DH",
    year: 2026,
    openingBalanceDh: 0,
    dueDayOfMonth: 5,
    minReminderAmountDh: 20,
    whatsappTemplate: "Bonjour {الاسم}",
    relanceHeaderNote: "Merci de votre régularisation",
    includeRdc: true,
    floorsCount: 5,
    apartmentsCount: 30,
    floorAptCounts: [],
    defaultMonthlyFee: 50,
    cutoffMonth: 10,
    activeMonths: [6, 7, 8, 9, 10, 11, 12],
    feeJan: 50,
    feeFeb: 50,
    feeMar: 50,
    feeApr: 50,
    feeMay: 50,
    feeJuin: 100,
    feeJuil: 100,
    feeAout: 50,
    feeSept: 50,
    feeOct: 50,
    feeNov: 50,
    feeDec: 50,
    footerNote: "—",
    onboardingCompleted: true,
    seeded: true,
    createdAt: now,
    updatedAt: now,
  };

  await assertSucceeds(cfgRef.set(baseConfig));

  for (const validStyle of ["coinbase", "minimax", "meta", "airtable", "wise", "xai"]) {
    await assertSucceeds(
      cfgRef.update({
        themeStyle: validStyle,
        updatedAt: new Date(),
      })
    );
  }

  await assertFails(
    cfgRef.update({
      themeStyle: "unsupported_theme",
      updatedAt: new Date(),
    })
  );
});

test("allow list succeeds on owner subcollections without where('userId', '==', uid) filter while denying cross-user list", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  // Unfiltered list queries on Alice's own subcollections succeed (even when empty or populated)
  for (const subcol of ["settings", "apartments", "payments", "expenses", "projects"]) {
    await assertSucceeds(
      aliceDb.collection("users").doc(ALICE_UID).collection(subcol).get()
    );
    // Cross-user unfiltered list queries remain strictly denied
    await assertFails(
      bobDb.collection("users").doc(ALICE_UID).collection(subcol).get()
    );
  }
});

test("ExpenseRecord accepts unified categories (Sécurité, Peinture, Plomberie, Étanchéité, Jardin) and rejects paidAmountDh > amountDh", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date();
  const cameraExpRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("expenses")
    .doc("exp_cameras");

  // "Sécurité" (surveillance cameras) now accepted on expenses just like projects
  await assertSucceeds(
    cameraExpRef.set({
      userId: ALICE_UID,
      title: "Achat et pose de 4 caméras de surveillance",
      category: "Sécurité",
      amountDh: 2400,
      paidAmountDh: 1200,
      supplier: "SecuriMaroc",
      dateLabel: "29/10/2026",
      notes: "Avance 50%",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Other unified project/expense categories also succeed
  for (const sharedCat of ["Peinture", "Plomberie", "Étanchéité", "Jardin", "Ménage"]) {
    await assertSucceeds(
      cameraExpRef.update({
        category: sharedCat,
        updatedAt: new Date(),
      })
    );
  }

  // Exact full payment (paidAmountDh == amountDh) and sentinel (-1) succeed
  await assertSucceeds(
    cameraExpRef.update({
      paidAmountDh: 2400,
      updatedAt: new Date(),
    })
  );
  await assertSucceeds(
    cameraExpRef.update({
      paidAmountDh: -1,
      updatedAt: new Date(),
    })
  );

  // paidAmountDh > amountDh (2500 > 2400) MUST be rejected by rules
  await assertFails(
    cameraExpRef.update({
      paidAmountDh: 2500,
      updatedAt: new Date(),
    })
  );

  // Creating an expense directly with paidAmountDh > amountDh MUST fail
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("expenses").doc("exp_overpaid").set({
      userId: ALICE_UID,
      title: "Surpaiement invalide",
      category: "Sécurité",
      amountDh: 500,
      paidAmountDh: 501,
      supplier: "",
      dateLabel: "29/10/2026",
      notes: "",
      createdAt: now,
      updatedAt: now,
    })
  );
});

test("ApartmentRecord enforces coherence between document ID (appt_XX) and apartment number", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date();
  const baseApt = {
    userId: ALICE_UID,
    number: 5,
    customNumberLabel: "",
    floor: "RDC",
    status: "occupied",
    ownerName: "Karim",
    ownerNameAr: "كريم",
    phone: "",
    isTenant: false,
    customMonthlyFee: -1,
    lastReminderAt: 0,
    receiptCount: 0,
    jan: 0,
    feb: 0,
    mar: 0,
    apr: 0,
    may: 0,
    juin: 100,
    juil: 100,
    aout: 50,
    sept: 50,
    oct: 50,
    nov: 0,
    dec: 0,
    createdAt: now,
    updatedAt: now,
  };

  // Matching ID appt_05 <-> number 5 and appt_12 <-> number 12 succeed
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("apartments").doc("appt_05").set(baseApt)
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("apartments").doc("appt_12").set({
      ...baseApt,
      number: 12,
    })
  );

  // Mismatched ID appt_09 with number 5 MUST fail
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("apartments").doc("appt_09").set(baseApt)
  );

  // Arbitrary ID "apt_5" with number 5 MUST fail
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("apartments").doc("apt_5").set(baseApt)
  );

  // Mutating number on update so it diverges from appt_05 MUST fail
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("apartments").doc("appt_05").update({
      number: 6,
      updatedAt: new Date(),
    })
  );
});

test("PaymentRecord supports multi-year payments, payment date, payment method, receiptNumber, and enforces apartmentId coherence", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date();

  const pay2026Ref = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("payments")
    .doc("pay_2026_01");
  const pay2027Ref = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("payments")
    .doc("pay_2027_01");

  // Record 2026 transfer payment with receipt #1
  await assertSucceeds(
    pay2026Ref.set({
      userId: ALICE_UID,
      apartmentId: "appt_01",
      apartmentNumber: 1,
      year: 2026,
      month: 0,
      monthsCovered: [11, 12],
      amountDh: 100,
      paymentDate: "29/10/2026",
      paymentMethod: "transfer",
      receiptNumber: 1,
      notes: "Virement CIH Nov-Déc 2026",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Record 2027 cash payment on same apartment (multi-year support)
  await assertSucceeds(
    pay2027Ref.set({
      userId: ALICE_UID,
      apartmentId: "appt_01",
      apartmentNumber: 1,
      year: 2027,
      month: 1,
      monthsCovered: [1],
      amountDh: 50,
      paymentDate: "05/01/2027",
      paymentMethod: "cash",
      receiptNumber: 2,
      notes: "Janvier 2027 espèces",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Mismatched apartmentId ("appt_02") vs apartmentNumber (1) MUST fail
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("payments").doc("pay_bad_apt").set({
      userId: ALICE_UID,
      apartmentId: "appt_02",
      apartmentNumber: 1,
      year: 2026,
      month: 10,
      monthsCovered: [10],
      amountDh: 50,
      paymentDate: "29/10/2026",
      paymentMethod: "cash",
      receiptNumber: 0,
      notes: "",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Unsupported paymentMethod MUST fail
  await assertFails(
    pay2026Ref.update({
      paymentMethod: "crypto",
      updatedAt: new Date(),
    })
  );

  // Cross-user access denied
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("payments").doc("pay_2026_01").get()
  );
});

