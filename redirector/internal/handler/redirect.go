package handler

import (
	"context"
	"encoding/json"
	"time"

	"github.com/gofiber/fiber/v2"

	"github.com/ykvlv/links-backend/redirector/internal/model"

	kafkarepo "github.com/ykvlv/links-backend/redirector/internal/repository/kafka"
	pgrepo "github.com/ykvlv/links-backend/redirector/internal/repository/postgres"
	redisrepo "github.com/ykvlv/links-backend/redirector/internal/repository/redis"
)

type Redirect struct {
	cache    *redisrepo.Cache
	store    *pgrepo.Store
	producer *kafkarepo.Producer
}

func NewRedirect(c *redisrepo.Cache, s *pgrepo.Store, p *kafkarepo.Producer) *Redirect {
	return &Redirect{cache: c, store: s, producer: p}
}

func (h *Redirect) Handle(c *fiber.Ctx) error {
	// Allow only GET requests
	if c.Method() != fiber.MethodGet {
		return fiber.ErrMethodNotAllowed
	}

	// Fill event data
	event := model.RedirectEvent{
		Timestamp:     time.Now().UTC(),
		Slug:          c.Params("slug"),
		IP:            c.IP(),
		XForwardedFor: c.Get("X-Forwarded-For"),
		XRealIP:       c.Get("X-Real-IP"),
		UserAgent:     c.Get("User-Agent"),
		AcceptLang:    c.Get("Accept-Language"),
		Referer:       c.Get("Referer"),
		Origin:        c.Get("Origin"),
		Host:          c.Hostname(),
	}

	// Context with timeout
	slug := c.Params("slug")
	ctx, cancel := context.WithTimeout(c.Context(), 100*time.Millisecond)
	defer cancel()

	// Try cache first
	if url, ok, err := h.cache.Get(ctx, slug); err == nil && ok {
		event.ResolvedURL = url
		event.IsCacheHit = true
		go h.sendEvent(event)
		return c.Redirect(url, fiber.StatusFound)
	} else if err != nil {
		return fiber.ErrInternalServerError
	}

	// If not in cache, get from store
	url, ok, err := h.store.URLBySlug(ctx, slug)
	if err != nil {
		return fiber.ErrInternalServerError
	} else if !ok {
		return fiber.ErrNotFound
	}

	// Respond to client
	event.ResolvedURL = url
	event.IsCacheHit = true
	go h.setCache(slug, url)
	go h.sendEvent(event)
	return c.Redirect(url, fiber.StatusFound)
}

func (h *Redirect) setCache(slug, url string) {
	_ = h.cache.Set(context.Background(), slug, url)
}

func (h *Redirect) sendEvent(event model.RedirectEvent) {
	payload, _ := json.Marshal(event)
	_ = h.producer.Send(context.Background(), payload)
}
