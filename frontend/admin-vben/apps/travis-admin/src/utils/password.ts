const PASSWORD_GROUPS = [
  'ABCDEFGHJKLMNPQRSTUVWXYZ',
  'abcdefghjkmnpqrstuvwxyz',
  '23456789',
  '~!@#$%^&*',
];

function randomIndex(length: number) {
  const limit = Math.floor(0x1_00_00_00_00 / length) * length;
  const values = new Uint32Array(1);
  let value: number;
  do {
    crypto.getRandomValues(values);
    value = values[0] ?? 0;
  } while (value >= limit);
  return value % length;
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
    const currentValue = password[index];
    const targetValue = password[target];
    if (currentValue === undefined || targetValue === undefined) {
      throw new RangeError('密码字符索引越界');
    }
    password[index] = targetValue;
    password[target] = currentValue;
  }
  return password.join('');
}
