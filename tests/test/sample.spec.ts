import { expect } from 'chai';
import { fakerIT as faker } from '@faker-js/faker';

import {randomPerson} from '../utils';

describe('Sample Test', () => {

    const person = randomPerson();

    it('should pass', () => {
        
    });


    it("Should be able to register" , () => {

        console.log(person)
    });
});
 