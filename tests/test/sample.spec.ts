import { expect } from 'chai';

import { fakerIT as faker } from '@faker-js/faker';

describe('Sample Test', () => {
    it('should pass', () => {
        const randomName = faker.person.fullName(); // Rowan Nikolaus
        const randomEmail = faker.internet.email(); // Kassandra.Haley@erich.biz

        console.log(`Random Name: ${randomName}`);
        console.log(`Random Email: ${randomEmail}`);
        expect(true).to.be.true;
    });
});
 