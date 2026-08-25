import { describe, expect, it, vi } from 'vitest';

import { generateRandomPassword } from './password';

describe('generateRandomPassword', () => {
  it('uses Web Crypto and includes every character group', () => {
    const getRandomValues = vi.spyOn(globalThis.crypto, 'getRandomValues');

    const password = generateRandomPassword();

    expect(getRandomValues).toHaveBeenCalled();
    expect(password).toHaveLength(8);
    expect(password).toMatch(/[A-Z]/);
    expect(password).toMatch(/[a-z]/);
    expect(password).toMatch(/\d/);
    expect(password).toMatch(/[~!@#$%^&*]/);
  });
});
