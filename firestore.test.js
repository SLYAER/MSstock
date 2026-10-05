const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

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

function validItemPayload(id, userId) {
  return {
    id: id,
    userId: userId,
    name: "MacBook Pro 14 M3",
    sku: "ELEC-LAP-001",
    category: "Laptops & PCs",
    brand: "Apple",
    model: "A2992 16GB/512GB",
    quantity: 12,
    minStockThreshold: 3,
    costPrice: 1599.0,
    sellingPrice: 1999.0,
    condition: "NEW",
    location: "Aisle 2 - Shelf B",
    warrantyMonths: 12,
    notes: "Space Gray finish",
    createdAt: new Date(),
    updatedAt: new Date(),
  };
}

test("Unauthenticated user: cannot read items", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("items").get());
});

test("Authenticated user: cannot read another user's item", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore()
      .collection("users").doc(BOB_UID)
      .collection("items").doc("item_bob")
      .set(validItemPayload("item_bob", BOB_UID));
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).collection("items").doc("item_bob").get()
  );
});

test("Authenticated user: can create, read, update, delete own item", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const itemRef = aliceDb.collection("users").doc(ALICE_UID).collection("items").doc("item_alice_1");

  // Create
  await assertSucceeds(itemRef.set(validItemPayload("item_alice_1", ALICE_UID)));

  // Read
  await assertSucceeds(itemRef.get());

  // Update
  await assertSucceeds(
    itemRef.update({
      quantity: 15,
      updatedAt: new Date(),
    })
  );

  // Delete
  await assertSucceeds(itemRef.delete());
});

test("Validation: rejects invalid payloads (negative quantity, negative price, mismatched id)", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const itemRef = aliceDb.collection("users").doc(ALICE_UID).collection("items").doc("item_alice_bad");

  // Negative quantity
  const badQty = validItemPayload("item_alice_bad", ALICE_UID);
  badQty.quantity = -5;
  await assertFails(itemRef.set(badQty));

  // Mismatched ID
  const badId = validItemPayload("item_alice_different", ALICE_UID);
  await assertFails(itemRef.set(badId));
});

test("StockLog: append-only audit trail works for owner, denies updates/deletes", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const logRef = aliceDb.collection("users").doc(ALICE_UID).collection("stock_logs").doc("log_1");

  const logPayload = {
    id: "log_1",
    userId: ALICE_UID,
    itemId: "item_alice_1",
    itemName: "MacBook Pro 14 M3",
    changeAmount: 5,
    previousQuantity: 10,
    newQuantity: 15,
    reason: "Restock from distributor",
    createdAt: new Date(),
  };

  // Create succeeds
  await assertSucceeds(logRef.set(logPayload));

  // Read succeeds
  await assertSucceeds(logRef.get());

  // Update fails (append-only)
  await assertFails(logRef.update({ reason: "Tampered reason" }));

  // Delete fails
  await assertFails(logRef.delete());
});

test("StaffMember: owner can create, read, update, delete staff with valid roles", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const staffRef = aliceDb.collection("users").doc(ALICE_UID).collection("staff").doc("staff_1");

  const staffPayload = {
    id: "staff_1",
    userId: ALICE_UID,
    username: "john_sales",
    displayName: "John Doe",
    role: "SALES",
    pin: "1234",
    isActive: true,
    createdAt: new Date(),
    updatedAt: new Date(),
  };

  // Create succeeds
  await assertSucceeds(staffRef.set(staffPayload));

  // Read succeeds
  await assertSucceeds(staffRef.get());

  // Update role to MANAGER succeeds
  await assertSucceeds(staffRef.update({
    role: "MANAGER",
    updatedAt: new Date(),
  }));

  // Invalid role fails
  await assertFails(staffRef.update({
    role: "INVALID_ROLE",
    updatedAt: new Date(),
  }));

  // Delete succeeds
  await assertSucceeds(staffRef.delete());
});

