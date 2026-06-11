import { requestClient } from '#/api/request';

const getJobEntry = () => requestClient.get<{ url: string }>('/ops/job/entry');

export { getJobEntry };
