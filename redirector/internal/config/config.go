package config

import (
	"fmt"
	"time"

	"github.com/caarlos0/env/v11"
)

type Config struct {
	RedirectPort int    `env:"REDIRECTOR_PORT"`
	RoutePrefix  string `env:"REDIRECTOR_ROUTE_PREFIX"`

	RedisHost       string `env:"REDIS_HOST"`
	RedisPort       int    `env:"REDIS_PORT"`
	RedisPassword   string `env:"REDIS_PASSWORD"`
	RedisDB         int    `env:"REDIS_DB"`
	CacheTTLSeconds int    `env:"CACHE_TTL_SECONDS"`

	PgHost string `env:"POSTGRES_HOST"`
	PgPort int    `env:"POSTGRES_PORT"`
	PgUser string `env:"POSTGRES_USER"`
	PgPass string `env:"POSTGRES_PASSWORD"`
	PgDB   string `env:"POSTGRES_DB"`

	KafkaHost        string `env:"KAFKA_HOST"`
	KafkaPort        int    `env:"KAFKA_PORT"`
	KafkaTopicClicks string `env:"KAFKA_TOPIC_CLICKS"`
}

func Load() (*Config, error) {
	var cfg Config
	opts := env.Options{
		RequiredIfNoDef: true,
	}
	if err := env.ParseWithOptions(&cfg, opts); err != nil {
		return nil, err
	}
	return &cfg, nil
}

func (c *Config) Addr() string {
	return fmt.Sprintf(":%d", c.RedirectPort)
}

func (c *Config) RedisAddr() string {
	return fmt.Sprintf("%s:%d", c.RedisHost, c.RedisPort)
}

func (c *Config) PgDSN() string {
	return fmt.Sprintf(
		"postgresql://%s:%s@%s:%d/%s",
		c.PgUser, c.PgPass, c.PgHost, c.PgPort, c.PgDB,
	)
}

func (c *Config) CacheTTL() time.Duration {
	return time.Duration(c.CacheTTLSeconds) * time.Second
}

func (c *Config) KafkaAddr() string {
	return fmt.Sprintf("%s:%d", c.KafkaHost, c.KafkaPort)
}
