FROM node:24.19.0-bookworm-slim AS builder

ENV PNPM_HOME=/pnpm
ENV PATH=$PNPM_HOME:$PATH

RUN corepack enable && corepack prepare pnpm@11.2.2 --activate

WORKDIR /workspace/frontend/admin-vben
COPY frontend/admin-vben/pnpm-lock.yaml frontend/admin-vben/pnpm-workspace.yaml frontend/admin-vben/package.json ./
COPY frontend/admin-vben ./

RUN pnpm install --frozen-lockfile \
    && pnpm --filter @travis/travis-admin build

FROM busybox:1.37.0-musl AS artifact
COPY --from=builder /workspace/frontend/admin-vben/apps/travis-admin/dist /dist
CMD ["true"]
