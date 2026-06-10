export interface PageResp<T> {
  pageNum: number;
  pageSize: number;
  records: T[];
  total: number;
  totalPages: number;
}
