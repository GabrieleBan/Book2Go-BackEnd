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

export function randomPerson(): Person {
    const firstName = faker.person.firstName();
    const lastName = faker.person.lastName();
    const fullName = `${firstName} ${lastName}`;
    const username = faker.internet.username({ firstName, lastName });
    const email = faker.internet.email({ firstName, lastName });
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
        phone: faker.phone.number(),
        birthDate: faker.date.birthdate(),
    };
}