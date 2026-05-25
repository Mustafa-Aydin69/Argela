# Argela OpenTelemetry Demo

OpenTelemetry'nin gerçek dünya senaryosunda nasıl kullanıldığını anlamak için geliştirdiğim demo projedir.  
Argela Technologies staj kapsamında geliştirilmiştir.

## Senaryo

Ağ yönetim sistemlerinde oluşan alarmlar **fault-collector** servisi tarafından alınır, doğrulanır ve **fault-processor** servisine iletilir. Fault-processor, alarma severity seviyesi atar ve PostgreSQL'e kaydeder. Tüm süreç boyunca üretilen traces, metrics ve logs OpenTelemetry aracılığıyla toplanır ve görselleştirilir.

## Mimari

```
         [Alarm Simülatörü]
                  │
                  │  HTTP POST /api/v1/alarms
                  ▼
┌───────────────────────┐     REST API     ┌───────────────────────────┐
│    fault-collector    │ ───────────────▶ │     fault-processor       │
│        :8080          │                  │         :8081             │
│  (alarm toplar,       │                  │  (severity atar,          │
│   doğrular)           │                  │   DB'ye yazar)            │
└──────────┬────────────┘                  └───────────┬───────────────┘
           │ OTLP                             OTLP │   │ SQL
           │                                       │   ▼
           │                                       │  ┌────────────┐
           └───────────────────┬───────────────────┘  │ PostgreSQL │
                               │                       │   :5432    │
                               ▼                       └────────────┘
                  ┌────────────────────────┐
                  │     OTel Collector     │
                  │     :4317 (gRPC)       │
                  │     :4318 (HTTP)       │
                  └────────────┬───────────┘
                               │
               ┌───────────────┴────────────────┐
               │ Traces                         │ Metrics
               ▼                                ▼
        ┌────────────┐                  ┌────────────┐
        │   Jaeger   │                  │ Prometheus │
        │   :16686   │                  │   :9090    │
        └────────────┘                  └──────┬─────┘
                                               │
                                               ▼
                                        ┌────────────┐
                                        │   Grafana  │
                                        │   :3000    │
                                        └────────────┘
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

# Alarm simülatörünü çalıştır (3 mod)
bash alarm-simulator/simulate.sh slow   # 1 alarm/sn
bash alarm-simulator/simulate.sh fast   # 10 alarm/sn
bash alarm-simulator/simulate.sh error  # geçersiz payload senaryosu
```

### Arayüzler

| Arayüz | URL |
|---|---|
| Jaeger UI | http://localhost:16686 |
| Prometheus | http://localhost:9090 |
| Grafana | http://localhost:3000 |
| fault-collector Health | http://localhost:8080/actuator/health |
| fault-processor Health | http://localhost:8081/actuator/health |

## Sunum Akışı

1. **Simülatörü başlat**
   ```bash
   bash alarm-simulator/simulate.sh slow
   ```

2. **Jaeger'da trace izle** → `http://localhost:16686`
   - Service: `fault-collector` → Find Traces
   - `fault-collector → fault-processor → INSERT faultdb.alarms` zincirini aç

3. **Prometheus'ta metrik sorgula** → `http://localhost:9090`
   - `rate(alarms_received_total[5m])` — geliş hızı
   - `alarms_processed_total` — severity dağılımı

4. **Grafana dashboard'u izle** → `http://localhost:3000`
   - Alarm geliş hızı grafiğinin canlı değişimini gözlemle
   - Severity dağılımı pie chart'ının güncellenmesini izle

## Ekran Görüntüleri

### Jaeger — Distributed Trace
Simülatör çalışırken `fault-collector` → `fault-processor` → `INSERT faultdb.alarms` zinciri tek trace altında görselleşir. Her alarm için auto-instrumented (HTTP, JDBC, Hibernate) ve manual span'lar (alarm.validate, alarm.process, severity.calculate) iç içe listelenir.

### Grafana — Fault Management Dashboard
Dört panel:
- **Alarm Geliş Hızı** — alarm tipine göre rate grafiği (timeseries)
- **Severity Dağılımı** — CRITICAL / MEDIUM dağılımı (pie chart)
- **Alarm Tipine Göre Ortalama İşlem Süresi** — her tip için renkli gauge
- **Aktif Alarm Sayısı** — anlık PROCESSING durumundaki alarm sayısı (stat)

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
