const PASSWORD_GROUPS = [
  'ABCDEFGHJKLMNPQRSTUVWXYZ',
  'abcdefghjkmnpqrstuvwxyz',
  '23456789',
  '~!@#$%^&*',
];

function randomIndex(length: number) {
  const limit = Math.floor(0x1_0000_0000 / length) * length;
  const values = new Uint32Array(1);
  do {
    crypto.getRandomValues(values);
  } while (values[0]! >= limit);
  return values[0]! % length;
}

function pickChar(chars: string) {
  return chars.charAt(randomIndex(chars.length));
}

export function generateRandomPassword() {
  const chars = PASSWORD_GROUPS.join('');
  const password = PASSWORD_GROUPS.map((group) => pickChar(group));
  while (password.length < 8) {
    password.push(pickChar(chars));
  }
  for (let index = password.length - 1; index > 0; index--) {
    const target = randomIndex(index + 1);
    [password[index], password[target]] = [password[target]!, password[index]!];
  }
  return password.join('');
}
