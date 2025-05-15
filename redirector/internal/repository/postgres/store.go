package pgrepo

import (
	"context"
	"errors"

	"github.com/jackc/pgx/v5"
	"github.com/jackc/pgx/v5/pgxpool"
)

type Store struct {
	db *pgxpool.Pool
}

func New(db *pgxpool.Pool) *Store {
	return &Store{db: db}
}

func (s *Store) URLBySlug(ctx context.Context, slug string) (string, bool, error) {
	var url string
	err := s.db.QueryRow(ctx,
		`SELECT url FROM link WHERE slug=$1 LIMIT 1`, slug,
	).Scan(&url)
	if errors.Is(pgx.ErrNoRows, err) {
		return "", false, nil
	}
	if err != nil {
		return "", false, err
	}
	return url, true, nil
}
