# Fault Management Alarm Simulator

COLLECTOR_URL="${COLLECTOR_URL:-http://localhost:8080/api/v1/alarms}"

ALARM_TYPES=("LINK_DOWN" "HIGH_LATENCY" "PACKET_LOSS" "NODE_UNREACHABLE" "CONGESTION")

random_ip() {
  echo "192.168.$((RANDOM % 256)).$((RANDOM % 256))"
}

random_alarm_type() {
  echo "${ALARM_TYPES[$((RANDOM % ${#ALARM_TYPES[@]}))]}"
}

generate_metrics() {
  local type="$1"
  case "$type" in
    HIGH_LATENCY)
      local ms_values=(120 250 380 620 780 950)
      local ms="${ms_values[$((RANDOM % ${#ms_values[@]}))]}"
      echo "{\"latencyMs\":$ms}"
      ;;
    PACKET_LOSS)
      local pct_values=(5 12 18 25 35 42)
      local pct="${pct_values[$((RANDOM % ${#pct_values[@]}))]}"
      echo "{\"packetLossPercent\":$pct}"
      ;;
    CONGESTION)
      local bw_values=(55 65 72 83 88 94)
      local bw="${bw_values[$((RANDOM % ${#bw_values[@]}))]}"
      echo "{\"bandwidthUtilizationPercent\":$bw}"
      ;;
    *)
      echo "null"
      ;;
  esac
}

send_alarm() {
  local id="alarm-$(date +%s%N)"
  local ip
  ip=$(random_ip)
  local type
  type=$(random_alarm_type)
  local ts
  ts=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
  local metrics
  metrics=$(generate_metrics "$type")

  curl -s -o /dev/null -w "%{http_code}" \
    -X POST "$COLLECTOR_URL" \
    -H "Content-Type: application/json" \
    -d "{\"alarmId\":\"$id\",\"sourceIp\":\"$ip\",\"alarmType\":\"$type\",\"description\":\"Simulated $type from $ip\",\"timestamp\":\"$ts\",\"metrics\":$metrics}"
}

run_slow() {
  echo "► Yavaş akış modu: 1 alarm/sn (Ctrl+C ile dur)"
  while true; do
    status=$(send_alarm)
    echo "[$(date +%H:%M:%S)] Alarm gönderildi → HTTP $status"
    sleep 1
  done
}

run_fast() {
  echo "► Yoğun trafik modu: 10 alarm/sn (Ctrl+C ile dur)"
  while true; do
    for _ in $(seq 1 10); do
      status=$(send_alarm)
      echo "[$(date +%H:%M:%S)] Alarm gönderildi → HTTP $status"
    done
    sleep 1
  done
}

run_error() {
  echo "► Hata senaryosu: geçersiz payload gönderiliyor (10 istek)"
  for i in $(seq 1 10); do
    status=$(curl -s -o /dev/null -w "%{http_code}" \
      -X POST "$COLLECTOR_URL" \
      -H "Content-Type: application/json" \
      -d "{\"alarmId\":\"\",\"sourceIp\":\"invalid-ip\",\"alarmType\":\"UNKNOWN\"}")
    echo "[$(date +%H:%M:%S)] Hatalı alarm $i → HTTP $status"
    sleep 0.5
  done
}

case "${1:-slow}" in
  slow)  run_slow ;;
  fast)  run_fast ;;
  error) run_error ;;
  *)
    echo "Kullanım: $0 [slow|fast|error]"
    echo "  slow  — 1 alarm/sn (varsayılan)"
    echo "  fast  — 10 alarm/sn"
    echo "  error — geçersiz payload senaryosu"
    exit 1
    ;;
esac
