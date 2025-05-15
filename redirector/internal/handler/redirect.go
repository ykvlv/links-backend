package handler

import (
	"context"
	"time"

	"github.com/gofiber/fiber/v2"

	pgrepo "github.com/ykvlv/links-backend/redirector/internal/repository/postgres"
	redisrepo "github.com/ykvlv/links-backend/redirector/internal/repository/redis"
)

type Redirect struct {
	cache *redisrepo.Cache
	store *pgrepo.Store
}

func NewRedirect(c *redisrepo.Cache, s *pgrepo.Store) *Redirect {
	return &Redirect{cache: c, store: s}
}

func (h *Redirect) Handle(c *fiber.Ctx) error {
	slug := c.Params("slug")
	ctx, cancel := context.WithTimeout(c.Context(), 100*time.Millisecond)
	defer cancel()

	if url, ok, _ := h.cache.Get(ctx, slug); ok {
		return c.Redirect(url, fiber.StatusFound)
	}

	url, ok, err := h.store.URLBySlug(ctx, slug)
	if err != nil {
		return fiber.ErrInternalServerError
	}
	if !ok {
		return fiber.ErrNotFound
	}
	_ = h.cache.Set(context.Background(), slug, url)
	return c.Redirect(url, fiber.StatusFound)
}
