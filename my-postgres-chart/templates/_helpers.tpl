{{- define "my-postgres-chart.name" -}}
{{- .Chart.Name -}}
{{- end -}}

{{- define "my-postgres-chart.fullname" -}}
{{ include "my-postgres-chart.name" . }}-{{ .Release.Name }}
{{- end -}}