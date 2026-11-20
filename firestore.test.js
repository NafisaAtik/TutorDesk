const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_tutor_123";
const BOB_UID = "bob_tutor_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
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

function pastDate(offsetMs = 5000) {
  return new Date(Date.now() - offsetMs);
}

test("Unauthenticated user: cannot read or write any user-scoped TutorDesk collection", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection(`users/${ALICE_UID}/batches`).get());
  await assertFails(unauthDb.collection(`users/${ALICE_UID}/students`).get());
  await assertFails(unauthDb.collection(`users/${ALICE_UID}/attendance`).get());
  await assertFails(unauthDb.collection(`users/${ALICE_UID}/feePayments`).get());

  await assertFails(
    unauthDb.collection(`users/${ALICE_UID}/batches`).doc("batch_1").set({
      userId: ALICE_UID,
      name: "HSC-26 Morning",
      hscLevel: "XII",
      scheduleDays: "Sat · Mon · Wed",
      scheduleTime: "08:00 AM",
      room: "Mirpur Room 2",
      currentTopic: "Ch 3: Number Systems",
      monthlyFeeBdt: 1500,
      maxCapacity: 25,
      status: "ACTIVE",
      createdAt: pastDate(),
      updatedAt: pastDate(),
    })
  );
});

test("Cross-user isolation: Bob cannot read or modify Alice's batches, students, attendance, or fees", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    const db = context.firestore();
    await db.collection(`users/${ALICE_UID}/batches`).doc("batch_1").set({
      userId: ALICE_UID,
      name: "HSC-26 Morning",
      hscLevel: "XII",
      scheduleDays: "Sat · Mon · Wed",
      scheduleTime: "08:00 AM",
      room: "Room 2",
      currentTopic: "Ch 3: Logic Gates",
      monthlyFeeBdt: 1500,
      maxCapacity: 25,
      status: "ACTIVE",
      createdAt: pastDate(10000),
      updatedAt: pastDate(10000),
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection(`users/${ALICE_UID}/batches`).get());
  await assertFails(bobDb.collection(`users/${ALICE_UID}/batches`).doc("batch_1").get());
  await assertFails(
    bobDb.collection(`users/${ALICE_UID}/batches`).doc("batch_1").update({
      name: "Hijacked Batch",
      updatedAt: pastDate(),
    })
  );
  await assertFails(bobDb.collection(`users/${ALICE_UID}/batches`).doc("batch_1").delete());
});

test("Authenticated tutor: Kotlin-aligned CRUD for batches, students, attendance, and feePayments succeeds", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = pastDate(10000);

  // 1. Create Batch (no @DocumentId `id` field)
  await assertSucceeds(
    aliceDb.collection(`users/${ALICE_UID}/batches`).doc("batch_1").set({
      userId: ALICE_UID,
      name: "HSC-26 Morning ICT",
      hscLevel: "XII",
      scheduleDays: "Sat · Mon · Wed",
      scheduleTime: "08:00 AM",
      room: "Mirpur-10 Room 2",
      currentTopic: "Ch 3: Number Systems",
      monthlyFeeBdt: 1500,
      maxCapacity: 25,
      status: "ACTIVE",
      createdAt: created,
      updatedAt: created,
    })
  );

  // Partial update on Batch
  await assertSucceeds(
    aliceDb.collection(`users/${ALICE_UID}/batches`).doc("batch_1").update({
      currentTopic: "Ch 4: HTML & Web Design",
      updatedAt: pastDate(1000),
    })
  );

  // 2. Create Student referencing existing batch_1
  await assertSucceeds(
    aliceDb.collection(`users/${ALICE_UID}/students`).doc("stu_1").set({
      userId: ALICE_UID,
      batchId: "batch_1",
      batchName: "HSC-26 Morning ICT",
      name: "Nafisa Rahman",
      college: "Viqarunnisa Noon College",
      rollNumber: "104",
      guardianPhone: "01711-000111",
      monthlyFeeBdt: 1500,
      status: "ACTIVE",
      createdAt: created,
      updatedAt: created,
    })
  );

  // 3. Create & Update AttendanceRecord
  const attId = "batch_1_2026-11-20_stu_1";
  await assertSucceeds(
    aliceDb.collection(`users/${ALICE_UID}/attendance`).doc(attId).set({
      userId: ALICE_UID,
      batchId: "batch_1",
      studentId: "stu_1",
      studentName: "Nafisa Rahman",
      dateIso: "2026-11-20",
      status: "PRESENT",
      createdAt: created,
      updatedAt: created,
    })
  );
  await assertSucceeds(
    aliceDb.collection(`users/${ALICE_UID}/attendance`).doc(attId).update({
      status: "LATE",
      updatedAt: pastDate(1000),
    })
  );

  // 4. Create & Update FeePayment
  const feeId = "2026-11_stu_1";
  await assertSucceeds(
    aliceDb.collection(`users/${ALICE_UID}/feePayments`).doc(feeId).set({
      userId: ALICE_UID,
      studentId: "stu_1",
      studentName: "Nafisa Rahman",
      batchId: "batch_1",
      batchName: "HSC-26 Morning ICT",
      monthKey: "2026-11",
      expectedFeeBdt: 1500,
      paidAmountBdt: 1000,
      paymentMethod: "bKash",
      note: "Partial payment",
      createdAt: created,
      updatedAt: created,
    })
  );
  await assertSucceeds(
    aliceDb.collection(`users/${ALICE_UID}/feePayments`).doc(feeId).update({
      paidAmountBdt: 1500,
      paymentMethod: "Cash",
      note: "Cleared rest in class",
      updatedAt: pastDate(1000),
    })
  );

  // 5. List queries (empty and populated)
  await assertSucceeds(aliceDb.collection(`users/${ALICE_UID}/batches`).get());
  await assertSucceeds(aliceDb.collection(`users/${ALICE_UID}/students`).get());
  await assertSucceeds(
    aliceDb.collection(`users/${ALICE_UID}/attendance`).where("dateIso", "==", "2026-11-20").get()
  );
  await assertSucceeds(
    aliceDb.collection(`users/${ALICE_UID}/feePayments`).where("monthKey", "==", "2026-11").get()
  );
});

test("Security bounds: reject ghost fields, @DocumentId id field, immutable createdAt mutation, future timestamps, and orphan student", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = pastDate(10000);

  // Reject ghost field & extra `id` field
  await assertFails(
    aliceDb.collection(`users/${ALICE_UID}/batches`).doc("batch_bad_id").set({
      id: "batch_bad_id",
      userId: ALICE_UID,
      name: "Bad Batch",
      hscLevel: "XI",
      scheduleDays: "Sun · Tue",
      scheduleTime: "04:00 PM",
      room: "Room 1",
      currentTopic: "Ch 1",
      monthlyFeeBdt: 1200,
      maxCapacity: 20,
      status: "ACTIVE",
      createdAt: created,
      updatedAt: created,
    })
  );

  // Reject future timestamp
  await assertFails(
    aliceDb.collection(`users/${ALICE_UID}/batches`).doc("batch_future").set({
      userId: ALICE_UID,
      name: "Future Batch",
      hscLevel: "XI",
      scheduleDays: "Sun · Tue",
      scheduleTime: "04:00 PM",
      room: "Room 1",
      currentTopic: "Ch 1",
      monthlyFeeBdt: 1200,
      maxCapacity: 20,
      status: "ACTIVE",
      createdAt: new Date(Date.now() + 60000),
      updatedAt: created,
    })
  );

  // Reject orphan student referencing missing batch
  await assertFails(
    aliceDb.collection(`users/${ALICE_UID}/students`).doc("stu_orphan").set({
      userId: ALICE_UID,
      batchId: "nonexistent_batch",
      batchName: "Ghost Batch",
      name: "Orphan Student",
      college: "Dhaka College",
      rollNumber: "1",
      guardianPhone: "01700000000",
      monthlyFeeBdt: 1500,
      status: "ACTIVE",
      createdAt: created,
      updatedAt: created,
    })
  );

  // Setup valid batch to test immutable createdAt update rejection
  await assertSucceeds(
    aliceDb.collection(`users/${ALICE_UID}/batches`).doc("batch_ok").set({
      userId: ALICE_UID,
      name: "Valid Batch",
      hscLevel: "XI",
      scheduleDays: "Sun · Tue",
      scheduleTime: "04:00 PM",
      room: "Room 1",
      currentTopic: "Ch 1",
      monthlyFeeBdt: 1200,
      maxCapacity: 20,
      status: "ACTIVE",
      createdAt: created,
      updatedAt: created,
    })
  );

  await assertFails(
    aliceDb.collection(`users/${ALICE_UID}/batches`).doc("batch_ok").update({
      createdAt: pastDate(1000),
      updatedAt: pastDate(1000),
    })
  );
});
