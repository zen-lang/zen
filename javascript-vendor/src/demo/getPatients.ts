import {aidbox} from "./index";

async function getPatient (summary?: boolean | "text") {
    let patients = aidbox.getResources('Patient')
        .where('name', ['Other', 'Vlad'])
        // .where('general-practitioner', "Practitioner/8bbd5c13-9b78-42c4-a612-0a4cf1fc8f59")
        .sort([{ key: '.name.0', dir: 'acs' }])
        .count(10)

    if (summary) {
        patients.summary(summary)
    }

    const response = await patients


    response.entry.map((patient) => console.log(patient.resource))
}

getPatient("text")
