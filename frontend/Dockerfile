# Build stage
FROM node:20-alpine AS builder
WORKDIR /app

# Copy package files
COPY package.json ./

# Install dependencies
RUN npm install

# Copy source
COPY . .

# Optional: pass NEXT_PUBLIC_S3_IMAGE_HOSTNAME when using S3 for product images
# e.g. docker build --build-arg NEXT_PUBLIC_S3_IMAGE_HOSTNAME=my-bucket.s3.us-east-1.amazonaws.com
ARG NEXT_PUBLIC_S3_IMAGE_HOSTNAME
ENV NEXT_PUBLIC_S3_IMAGE_HOSTNAME=$NEXT_PUBLIC_S3_IMAGE_HOSTNAME

# Build application
ENV NEXT_TELEMETRY_DISABLED=1
RUN npm run build

# Runtime stage
FROM node:20-alpine AS runner
WORKDIR /app

ENV NODE_ENV=production
ENV NEXT_TELEMETRY_DISABLED=1

# Create non-root user
RUN addgroup --system --gid 1001 nodejs && \
    adduser --system --uid 1001 nextjs

# Copy necessary files for standalone
COPY --from=builder /app/public ./public
COPY --from=builder /app/.next/standalone ./
COPY --from=builder /app/.next/static ./.next/static

# Set ownership
RUN chown -R nextjs:nodejs /app

# Switch to non-root user
USER nextjs

# Expose port
EXPOSE 3000

ENV PORT=3000
ENV HOSTNAME="0.0.0.0"

# Run application
CMD ["node", "server.js"]
