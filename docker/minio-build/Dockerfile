FROM golang:1.24.8-alpine AS builder
ENV CGO_ENABLED=0
ENV GOPROXY=https://goproxy.cn,https://proxy.golang.org,direct
RUN go install -trimpath github.com/minio/minio@7aac2a2c5b7c882e68c1ce017d8256be2feea27f
FROM alpine:3.22
RUN apk add --no-cache ca-certificates tzdata
COPY --from=builder /go/bin/minio /usr/local/bin/minio
EXPOSE 9000 9001
ENTRYPOINT ["minio"]
CMD ["server", "/data", "--console-address", ":9001"]

