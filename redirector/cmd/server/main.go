package main

import (
	"context"
	"log"
	"os"
	"os/signal"
	"path"
	"syscall"
	"time"

	"github.com/gofiber/fiber/v2"
	"github.com/gofiber/fiber/v2/middleware/logger"
	"github.com/gofiber/fiber/v2/middleware/recover"
	"github.com/jackc/pgx/v5/pgxpool"
	"github.com/redis/go-redis/v9"

	"github.com/ykvlv/links-backend/redirector/internal/config"
	"github.com/ykvlv/links-backend/redirector/internal/handler"
	pgrepo "github.com/ykvlv/links-backend/redirector/internal/repository/postgres"
	redisrepo "github.com/ykvlv/links-backend/redirector/internal/repository/redis"
)

func main() {
	cfg, err := config.Load()
	if err != nil {
		log.Fatalf("config: %v", err)
	}

	// Postgres pool
	pgPool, err := pgxpool.New(context.Background(), cfg.PgDSN())
	if err != nil {
		log.Fatalf("pgx: %v", err)
	}
	defer pgPool.Close()

	// Redis client
	rdb := redis.NewClient(&redis.Options{
		Addr:     cfg.RedisAddr(),
		Password: cfg.RedisPassword,
		DB:       cfg.RedisDB,
	})
	if err := rdb.Ping(context.Background()).Err(); err != nil {
		log.Fatalf("redis: %v", err)
	}

	cache := redisrepo.New(rdb, cfg.CacheTTL())
	store := pgrepo.New(pgPool)

	app := fiber.New(fiber.Config{
		Prefork: true,
	})
	app.Use(recover.New(), logger.New())

	redirect := handler.NewRedirect(cache, store)
	app.Get(path.Join(cfg.RoutePrefix, ":slug"), redirect.Handle)

	go func() {
		if err := app.Listen(cfg.Addr()); err != nil {
			log.Fatalf("listen: %v", err)
		}
	}()

	// graceful shutdown
	quit := make(chan os.Signal, 1)
	signal.Notify(quit, syscall.SIGINT, syscall.SIGTERM)
	<-quit
	_ = app.ShutdownWithTimeout(5 * time.Second)
}
