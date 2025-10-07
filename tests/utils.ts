import { faker } from "@faker-js/faker/locale/it";

export interface Person {
    firstName: string;
    lastName: string;
    fullName: string;
    username: string;
    email: string;
    password: string;
    address: string;
    city: string;
    country: string;
    phone: string;
    birthDate: Date;
}

// Set per tracciare i valori già generati
const generatedUsernames = new Set<string>();
const generatedEmails = new Set<string>();
const generatedPhones = new Set<string>();
const generatedFullNames = new Set<string>();

// Funzione helper per generare un valore univoco
function generateUnique<T>(
    generator: () => T,
    validator: (value: T) => boolean,
    maxAttempts: number = 100
): T {
    for (let i = 0; i < maxAttempts; i++) {
        const value = generator();
        if (validator(value)) {
            return value;
        }
    }
    throw new Error(`Failed to generate unique value after ${maxAttempts} attempts`);
}

export function randomPerson(): Person {
    // Genera nome e cognome unici
    const { firstName, lastName, fullName } = generateUnique(
        () => {
            const first = faker.person.firstName();
            const last = faker.person.lastName();
            const full = `${first} ${last}`;
            return { firstName: first, lastName: last, fullName: full };
        },
        ({ fullName }) => !generatedFullNames.has(fullName)
    );
    generatedFullNames.add(fullName);

    // Genera username unico
    const username = generateUnique(
        () => faker.internet.username({ firstName, lastName }),
        (value) => !generatedUsernames.has(value)
    );
    generatedUsernames.add(username);

    // Genera email unica
    const email = generateUnique(
        () => faker.internet.email({ firstName, lastName }),
        (value) => !generatedEmails.has(value)
    );
    generatedEmails.add(email);

    // Genera telefono unico
    const phone = generateUnique(
        () => faker.phone.number(),
        (value) => !generatedPhones.has(value)
    );
    generatedPhones.add(phone);

    // Password è sempre unica per definizione (random)
    const password = faker.internet.password({ length: 12 });

    return {
        firstName,
        lastName,
        fullName,
        username,
        email,
        password,
        address: faker.location.streetAddress(),
        city: faker.location.city(),
        country: faker.location.country(),
        phone,
        birthDate: faker.date.birthdate(),
    };
}