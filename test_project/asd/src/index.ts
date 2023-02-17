import { Client } from "aidbox-javascript-sdk";

const client = new Client("url", { username: "sd", password: "sd" });

client.getResource("ServiceRequest", {});
