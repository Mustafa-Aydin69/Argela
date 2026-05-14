# Argela OpenTelemetry Demo

OpenTelemetry'nin gerçek dünya senaryosunda nasıl kullanıldığını anlamak için geliştirdiğim demo projedir.  
Argela Technologies staj kapsamında geliştirilmiştir.

## Senaryo

Ağ yönetim sistemlerinde oluşan alarmlar **fault-collector** servisi tarafından alınır, doğrulanır ve **fault-processor** servisine iletilir. Fault-processor, alarma severity seviyesi atar ve PostgreSQL'e kaydeder. Tüm süreç boyunca üretilen traces, metrics ve logs OpenTelemetry aracılığıyla toplanır ve görselleştirilir.

## Mimari

```
[Alarm Simülatörü]
        │  HTTP POST /api/v1/alarms
        ▼
┌──────────────────────┐     REST API      ┌───────────────────────────┐
│  fault-collector     │ ────────────────▶ │  fault-processor          │
│  :8080               │                   │  :8081                    │
│  (alarm toplar,      │                   │  (severity atar,          │
│   doğrular)          │                   │   DB'ye yazar, log üretir)│
└──────────────────────┘                   └──────────────┬────────────┘
         │  OTLP                             OTLP │        │
         └───────────────────────────────────────┘        │
                             │                             ▼
                    ┌────────▼────────┐             ┌────────────┐
                    │  OTel Collector  │             │ PostgreSQL │
                    │  :4317 (gRPC)   │             │ :5432      │
                    │  :4318 (HTTP)   │             └────────────┘
                    └──┬──────────┬───┘
                       │          │
                  Traces        Metrics
                       │          │
                  ┌────▼──┐  ┌───▼──────┐
                  │ Jaeger │  │Prometheus│
                  │ :16686 │  │ :9090    │
                  └────────┘  └────┬─────┘
                                   ▼
                               ┌────────┐
                               │Grafana │
                               │ :3000  │
                               └────────┘
```

## Teknoloji Stack

| Katman | Teknoloji |
|---|---|
| Servisler | Java 17, Spring Boot 3, Maven |
| Traces | OpenTelemetry Java Agent, Jaeger |
| Metrics | OpenTelemetry, Prometheus, Grafana |
| Logs | Logback (JSON), OTel Log Bridge |
| Veritabanı | PostgreSQL, JPA/Hibernate, Flyway |
| Altyapı | Docker, Docker Compose |

## Servisler

| Servis | Port | Açıklama |
|---|---|---|
| fault-collector | 8080 | Alarm alır, doğrular, fault-processor'a iletir |
| fault-processor | 8081 | Severity atar, PostgreSQL'e yazar |
| OTel Collector | 4317, 4318 | Telemetri toplar, backend'lere yönlendirir |
| Jaeger | 16686 | Distributed trace görselleştirme |
| Prometheus | 9090 | Metrics toplama ve sorgulama |
| Grafana | 3000 | Dashboard (admin/admin) |
| PostgreSQL | 5432 | Alarm kayıtları |

## Kurulum

### Gereksinimler
- Docker & Docker Compose
- Java 17+
- Maven 3.8+

### Çalıştırma

```bash
# Tüm altyapıyı ayağa kaldır
docker-compose up --build

# Alarm simülatörünü çalıştır
./infrastructure/simulator/simulate.sh
```

### Arayüzler

| Arayüz | URL |
|---|---|
| Jaeger UI | http://localhost:16686 |
| Prometheus | http://localhost:9090 |
| Grafana | http://localhost:3000 |
| fault-collector Health | http://localhost:8080/actuator/health |
| fault-processor Health | http://localhost:8081/actuator/health |

## OpenTelemetry Sinyalleri

### Traces
Her alarm isteği uçtan uca izlenir. Jaeger UI'da `fault-collector` → `fault-processor` → PostgreSQL zinciri görselleştirilir.

### Metrics
| Metric | Tür | Açıklama |
|---|---|---|
| `alarms.received.total` | Counter | Alınan alarm sayısı (alarmType bazında) |
| `alarms.processed.total` | Counter | İşlenen alarm sayısı (severityLevel bazında) |
| `alarm.processing.duration` | Histogram | İşlem süresi dağılımı |
| `alarms.pending.count` | Gauge | Bekleyen alarm sayısı |

### Logs
JSON formatında yapılandırılmış loglar. Her log satırı `trace_id` ve `span_id` içerir.

## OTel Collector Konfigürasyonu

`infrastructure/otel-collector/otel-collector-config.yaml` dosyası üç temel blok içerir:

```yaml
receivers:    # OTLP gRPC (4317) ve HTTP (4318) ile veri alımı
processors:   # batch ve memory_limiter işlemleri
exporters:    # Jaeger (traces), Prometheus (metrics)
pipelines:    # Sinyal bazında yönlendirme (traces, metrics, logs)
```
