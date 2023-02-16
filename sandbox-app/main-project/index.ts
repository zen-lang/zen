import { Client } from "child-project";

const client = new Client("https://octoalex.edge.aidbox.app", {
  username: "test",
  password: "test",
});

async function get() {
  const response = await client.getResource(
    "Patient",
    "1aba4bb1-c0fa-47ed-90a6-46393bd47be0"
  );

  console.dir(response, { depth: 10 });
}

get();
