import { expect } from 'chai';
import { fakerIT as faker } from '@faker-js/faker';
import axios, { AxiosInstance } from 'axios';
import { randomPerson } from '../utils';

const BASE_URL = 'http://localhost:8090';
const MAILHOG_URL = 'http://localhost:8025';

interface AuthTokens {
    accessToken: string;
    refreshToken: string;
}

interface MailHogEmail {
    Content: {
        Headers: {
            To: string[];
            Subject: string[];
        };
        Body: string;
    };
}

interface MailHogResponse {
    items: MailHogEmail[];
}

const api: AxiosInstance = axios.create({
    baseURL: BASE_URL,
    timeout: 10000,
    headers: { 'Content-Type': 'application/json' }
});

const sleep = (ms: number): Promise<void> =>
    new Promise(resolve => setTimeout(resolve, ms));

function extractUuidFromEmail(emailBody: string): string | null {
    const regex = /\/auth\/confirm\/([a-f0-9-]{36})/i;
    const match = emailBody.match(regex);
    return match ? match[1] : null;
}

async function getLatestEmail(retries: number = 10, delayMs: number = 500): Promise<MailHogEmail> {
    for (let i = 0; i < retries; i++) {
        try {
            const response = await axios.get<MailHogResponse>(`${MAILHOG_URL}/api/v2/messages`);
            if (response.data.items?.length > 0) {
                return response.data.items[0];
            }
            await sleep(delayMs);
        } catch (error) {
            await sleep(delayMs);
        }
    }
    throw new Error('No email received');
}

async function clearEmails(): Promise<void> {
    try {
        await axios.delete(`${MAILHOG_URL}/api/v1/messages`);
    } catch (error) {
        // Ignore
    }
}

describe('Auth Flow', () => {
    let person: ReturnType<typeof randomPerson>;
    let password: string;
    let confirmationUuid: string | null;
    let accessToken: string;
    let refreshToken: string;

    before(async () => {
        await clearEmails();
    });

    beforeEach(() => {
        person = randomPerson();
    });

    it('should register user', async () => {
        const response = await api.post('/auth/register', {
            email: person.email,
            password: person.password,
            username: person.username
        });

        expect(response.status).to.equal(200);
    });

    it('should receive confirmation email', async () => {
        await api.post('/auth/register', {
            email: person.email,
            password: person.password,
            username: person.username
        });

        await sleep(1000);
        const email = await getLatestEmail();

        expect(email.Content.Headers.To[0]).to.include(person.email);

        confirmationUuid = extractUuidFromEmail(email.Content.Body);
        expect(confirmationUuid).to.not.be.null;
    });

    it('should confirm registration', async () => {
        await api.post('/auth/register', {
            email: person.email,
            password: person.password,
            username: person.username
        });

        await sleep(1000);
        const email = await getLatestEmail();
        confirmationUuid = extractUuidFromEmail(email.Content.Body);

        const response = await api.get(`/auth/confirm/${confirmationUuid}`);
        expect(response.status).to.equal(200);
    });

    it('should login with confirmed user', async () => {
        await api.post('/auth/register', {
            email: person.email,
            password: person.password,
            username: person.username
        });

        await sleep(1000);
        const email = await getLatestEmail();
        confirmationUuid = extractUuidFromEmail(email.Content.Body);
        await api.get(`/auth/confirm/${confirmationUuid}`);

        const response = await api.post<AuthTokens>('/auth/login', {
            email: person.email,
            password: password
        });

        expect(response.status).to.equal(200);
        expect(response.data).to.have.property('accessToken');
        expect(response.data).to.have.property('refreshToken');

        accessToken = response.data.accessToken;
        refreshToken = response.data.refreshToken;
    });

    it('should refresh token', async () => {
        await api.post('/auth/register', {
            email: person.email,
            password: person.password,
            username: person.username
        });

        await sleep(1000);
        const email = await getLatestEmail();
        confirmationUuid = extractUuidFromEmail(email.Content.Body);
        await api.get(`/auth/confirm/${confirmationUuid}`);

        const loginResponse = await api.post<AuthTokens>('/auth/login', {
            email: person.email,
            password: password
        });
        refreshToken = loginResponse.data.refreshToken;

        const response = await api.post<AuthTokens>('/auth/refresh', {
            refreshToken: refreshToken
        });

        expect(response.status).to.equal(200);
        expect(response.data).to.have.property('accessToken');
        expect(response.data).to.have.property('refreshToken');
        expect(response.data.refreshToken).to.not.equal(refreshToken);
    });

    it('should logout and invalidate refresh token', async () => {
        await api.post('/auth/register', {
            email: person.email,
            password: person.password,
            username: person.username
        });

        await sleep(1000);
        const email = await getLatestEmail();
        confirmationUuid = extractUuidFromEmail(email.Content.Body);
        await api.get(`/auth/confirm/${confirmationUuid}`);

        const loginResponse = await api.post<AuthTokens>('/auth/login', {
            email: person.email,
            password: password
        });
        refreshToken = loginResponse.data.refreshToken;

        const logoutResponse = await api.post('/auth/logout', {
            refreshToken: refreshToken
        });
        expect(logoutResponse.status).to.equal(200);

        try {
            await api.post('/auth/refresh', {
                refreshToken: refreshToken
            });
            expect.fail('Should have thrown error');
        } catch (error: any) {
            expect(error.response.status).to.be.oneOf([401, 403]);
        }
    });

    it('should complete full auth flow', async () => {
        await api.post('/auth/register', {
            email: person.email,
            password: person.password,
            username: person.username
        });

        await sleep(1000);
        const email = await getLatestEmail();
        confirmationUuid = extractUuidFromEmail(email.Content.Body);

        await api.get(`/auth/confirm/${confirmationUuid}`);

        const loginResponse = await api.post<AuthTokens>('/auth/login', {
            email: person.email,
            password: password
        });
        refreshToken = loginResponse.data.refreshToken;

        const refreshResponse = await api.post<AuthTokens>('/auth/refresh', {
            refreshToken: refreshToken
        });
        const newRefreshToken = refreshResponse.data.refreshToken;

        await api.post('/auth/logout', {
            refreshToken: newRefreshToken
        });

        try {
            await api.post('/auth/refresh', {
                refreshToken: newRefreshToken
            });
            expect.fail('Should have thrown error');
        } catch (error: any) {
            expect(error.response.status).to.be.oneOf([401, 403]);
        }
    });
});