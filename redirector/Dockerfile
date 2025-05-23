# Stage 1: Build the Go application
FROM golang:1.24.3-alpine AS builder

WORKDIR /app

# Copy go.mod and go.sum files to leverage Docker cache
COPY go.mod go.sum ./
RUN go mod download && go mod verify

COPY . .

# Install dumb-init to act as PID 1, crucial with Fiber's Prefork mode
RUN apk add --no-cache dumb-init
RUN CGO_ENABLED=0 go build -v -o /app/redirector ./cmd/server

# Stage 2: Create the final lightweight image
FROM alpine:3.21

WORKDIR /app

COPY --from=builder /usr/bin/dumb-init /usr/bin/dumb-init
COPY --from=builder /app/redirector /app/redirector

# Create a non-root user and group for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Use dumb-init as the entrypoint to handle signals correctly
ENTRYPOINT ["/usr/bin/dumb-init", "--"]
CMD /app/redirector
