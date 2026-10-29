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
