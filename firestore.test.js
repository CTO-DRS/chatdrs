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

test("Unauthenticated user: cannot read chats", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("chats").get());
});

test("Authenticated user: cannot read another user's private chat", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("chats").doc("bob_chat").set({
      chatId: "bob_chat",
      participantIds: [BOB_UID],
      lastMessage: "Hello",
      lastSenderId: BOB_UID,
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("chats").doc("bob_chat").get());
});

test("Authenticated user: can create and query their own chats", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("chats").doc("alice_chat").set({
      chatId: "alice_chat",
      participantIds: [ALICE_UID, BOB_UID],
      lastMessage: "Welcome",
      lastSenderId: ALICE_UID,
    })
  );

  await assertSucceeds(
    aliceDb.collection("chats").where("participantIds", "array-contains", ALICE_UID).get()
  );
});

test("Authenticated user: can send message in participant chat", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("chats").doc("chat_1").set({
      chatId: "chat_1",
      participantIds: [ALICE_UID, BOB_UID],
      lastMessage: "Init",
      lastSenderId: ALICE_UID,
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("chats").doc("chat_1").collection("messages").doc("msg_1").set({
      messageId: "msg_1",
      chatId: "chat_1",
      senderId: ALICE_UID,
      text: "Hello Bob!",
      status: "SENT",
    })
  );
});
