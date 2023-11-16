terraform {
  required_providers {
    google = {
      source = "hashicorp/google"
      version = "4.51.0"
    }
  }
}

provider "google" {
  project = "curious-athlete-401708"
  region  = "asia-southeast1"
}

resource "google_compute_network" "vpc_network" {
  name = "terraform-network"
}