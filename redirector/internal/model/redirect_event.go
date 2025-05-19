package model

import "time"

type RedirectEvent struct {
	Timestamp   time.Time `json:"timestamp"`
	Slug        string    `json:"slug"`
	ResolvedURL string    `json:"resolved_url"`

	IP            string `json:"ip"`
	XForwardedFor string `json:"x_forwarded_for"`
	XRealIP       string `json:"x_real_ip"`

	UserAgent  string `json:"user_agent"`
	AcceptLang string `json:"accept_language"`

	Referer string `json:"referer"`
	Origin  string `json:"origin"`
	Host    string `json:"host"`

	IsCacheHit bool `json:"is_cache_hit"`
}
