# La SPA se compila con Node y la sirve Nginx (página 3a: «SPA servida por Nginx»).
FROM node:22.11.0-alpine AS compilar
WORKDIR /spa
COPY package.json package-lock.json ./
RUN npm ci --no-audit --no-fund
COPY . .
RUN npm run build

FROM nginx:1.28.0-alpine
COPY --from=compilar /spa/dist /usr/share/nginx/html
