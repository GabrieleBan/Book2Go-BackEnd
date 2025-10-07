import {faker} from "@faker-js/faker/locale/it";

export function randomPerson() {
    const firstName = faker.person.firstName();
    const lastName = faker.person.lastName();
    const fullName = `${firstName} ${lastName}`;
    const username = faker.internet.username({ firstName, lastName });
    const email = faker.internet.email({ firstName, lastName });

    return {
        firstName,
        lastName,
        fullName,
        username,
        email,
        address: faker.location.streetAddress(),
        city: faker.location.city(),
        country: faker.location.country(),
        phone: faker.phone.number(),
        birthDate: faker.date.birthdate(),
    };
}