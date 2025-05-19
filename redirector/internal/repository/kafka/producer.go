package kafka

import (
	"context"
	"github.com/segmentio/kafka-go"
)

type Producer struct {
	writer *kafka.Writer
}

func New(writer *kafka.Writer) *Producer {
	return &Producer{writer: writer}
}

func (p *Producer) Send(ctx context.Context, value []byte) error {
	return p.writer.WriteMessages(ctx, kafka.Message{
		Value: value,
	})
}
