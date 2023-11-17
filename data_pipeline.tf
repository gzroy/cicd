resource "google_data_pipeline_pipeline" "primary" {
  name         = "telematics-usage-pipeline"
  display_name = "telematics-usage-pipeline"
  type         = "PIPELINE_TYPE_BATCH"
  state        = "STATE_ACTIVE"
  region       = "asia-southeast2"

  workload {
    dataflow_flex_template_request {
      project_id = "curious-athlete-401708"
      launch_parameter {
        job_name = "telematics-usage-pipeline-`date +%Y%m%d-%H%M%S`"
        parameters = {
          "inputFile" : "gs://curious-athlete-401708-analytics/resultLogFile.log",
          "outputPath" : "gs://curious-athlete-401708-analytics/temp"
        }
        environment {
          service_account_email      = "test-288@curious-athlete-401708.iam.gserviceaccount.com"
          temp_location              = "gs://curious-athlete-401708-analytics/temp/"
          ip_configuration           = "WORKER_IP_PRIVATE"
          worker_region              = "asia-southeast2"
          enable_streaming_engine    = "false"
        }
        update                 = false
        container_spec_gcs_path  = "gs://curious-athlete-401708-analytics/dataflow/templates/wordcount.json"
      }
      location = "asia-southeast2"
    }
  }
  schedule_info {
    schedule = "25 */2 * * *"
  }
}