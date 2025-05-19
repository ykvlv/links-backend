package main

import (
	"context"
	"log"
	"os/signal"
	"path"
	"syscall"
	"time"

	"github.com/gofiber/fiber/v2"
	"github.com/gofiber/fiber/v2/middleware/logger"
	"github.com/gofiber/fiber/v2/middleware/recover"
	"github.com/jackc/pgx/v5/pgxpool"
	"github.com/redis/go-redis/v9"
	"github.com/segmentio/kafka-go"

	"github.com/ykvlv/links-backend/redirector/internal/config"
	"github.com/ykvlv/links-backend/redirector/internal/handler"
	kafkarepo "github.com/ykvlv/links-backend/redirector/internal/repository/kafka"
	pgrepo "github.com/ykvlv/links-backend/redirector/internal/repository/postgres"
	redisrepo "github.com/ykvlv/links-backend/redirector/internal/repository/redis"
)

func main() {
	// Signal handling
	ctx, stop := signal.NotifyContext(context.Background(), syscall.SIGINT, syscall.SIGTERM)
	defer stop()

	// Load config
	cfg, err := config.Load()
	if err != nil {
		log.Fatalf("config: failed to load: %v", err)
	}

	// Postgres pool
	pgPool, err := pgxpool.New(ctx, cfg.PgDSN())
	if err != nil {
		log.Fatalf("postgres: failed to create pool: %v", err)
	} else if err := pgPool.Ping(ctx); err != nil {
		log.Fatalf("postgres: failed to ping: %v", err)
	}
	defer pgPool.Close()

	// Redis client
	rdb := redis.NewClient(&redis.Options{
		Addr:     cfg.RedisAddr(),
		Password: cfg.RedisPassword,
		DB:       cfg.RedisDB,
	})
	if err := rdb.Ping(ctx).Err(); err != nil {
		log.Fatalf("redis: failed to ping: %v", err)
	}
	defer func() {
		if err := rdb.Close(); err != nil {
			log.Printf("redis: failed to close: %v", err)
		}
	}()

	// Kafka writer
	kafkaWriter := &kafka.Writer{
		Addr:     kafka.TCP(cfg.KafkaAddr()),
		Topic:    cfg.KafkaTopicClicks,
		Balancer: &kafka.LeastBytes{},
	}
	kafkaConn, err := kafka.Dial("tcp", cfg.KafkaAddr())
	if err != nil {
		log.Fatalf("kafka: failed to connect: %v", err)
	}
	_ = kafkaConn.Close()
	defer func() {
		if err := kafkaWriter.Close(); err != nil {
			log.Printf("kafka: failed to close: %v", err)
		}
	}()

	// Repositories
	cache := redisrepo.New(rdb, cfg.CacheTTL())
	store := pgrepo.New(pgPool)
	producer := kafkarepo.New(kafkaWriter)

	// Fiber app
	app := fiber.New(fiber.Config{Prefork: true})
	app.Use(recover.New(), logger.New())

	// Routes
	redirect := handler.NewRedirect(cache, store, producer)
	app.Get(path.Join(cfg.RoutePrefix, ":slug"), redirect.Handle)

	// Start server
	go func() {
		if err := app.Listen(cfg.Addr()); err != nil {
			log.Fatalf("listen: %v", err)
		}
	}()

	// Graceful shutdown
	<-ctx.Done()
	log.Println("shutting down server...")
	shutdownCtx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()
	if err := app.ShutdownWithContext(shutdownCtx); err != nil {
		log.Printf("server shutdown error: %v", err)
	}
}
