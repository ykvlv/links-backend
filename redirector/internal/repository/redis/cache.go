package redisrepo

import (
	"context"
	"errors"
	"time"

	"github.com/redis/go-redis/v9"
)

type Cache struct {
	cli *redis.Client
	ttl time.Duration
}

func New(cli *redis.Client, ttl time.Duration) *Cache {
	return &Cache{cli: cli, ttl: ttl}
}

func (c *Cache) Get(ctx context.Context, slug string) (string, bool, error) {
	res, err := c.cli.Get(ctx, slug).Result()
	if errors.Is(redis.Nil, err) {
		return "", false, nil
	}
	if err != nil {
		return "", false, err
	}
	return res, true, nil
}

func (c *Cache) Set(ctx context.Context, slug, url string) error {
	return c.cli.Set(ctx, slug, url, c.ttl).Err()
}
