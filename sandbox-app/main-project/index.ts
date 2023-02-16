import { Client } from "child-project";

const client = new Client("https://octoalex.edge.aidbox.app", {
  username: "test",
  password: "test",
});

async function getResource() {
  const response = await client.getResource(
    "Patient",
    "1aba4bb1-c0fa-47ed-90a6-46393bd47be0"
  );
  console.dir(response, { depth: 10 });
}

async function getResources() {
  const response = await client.getResources("Patient");
  console.dir(response, { depth: 10 });
}

async function findResources() {
  const response = await client.findResources("Patient", { _count: 10 });
  console.dir(response, { depth: 10 });
}

async function patchResource() {
  const response = await client.patchResource(
    "Patient",
    "1aba4bb1-c0fa-47ed-90a6-46393bd47be0",
      {name: [{ family: "A" }]}
  );
  console.dir(response, { depth: 10 });
}

async function deleteResource() {
  const response = await client.deleteResource(
    "Patient",
    "1aba4bb1-c0fa-47ed-90a6-46393bd47be0"
  );
  console.dir(response, { depth: 10 });
}

async function createResource() {
  const response = await client.createResource("Patient", {name: [{ family: "abc" }]});
  console.dir(response, { depth: 10 });
}
