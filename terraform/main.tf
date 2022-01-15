provider "kubernetes" {
  config_context_cluster   = "minikube"
  config_path = "~/.kube/config"
}

resource "kubernetes_deployment" "dse" {
  metadata {
    name = "dse"
    labels = {
      app = "dse"
    }
  }
  spec {
    replicas = 1

    selector {
      match_labels = {
        app = "dse"
      }
    }

    template {
      metadata {
        labels = {
          app = "dse"
        }
      }
      spec {
        container {
          image = "datastax/dse-server:6.8.18-1"
          name = "dse"
          args = ["-s", "-g"]
          env {
            name = "DS_LICENSE"
            value = "accept"
          }
          port {
            container_port = 9042
          }
          port {
            container_port = 8983
          }
          port {
            container_port = 8182
          }
          security_context {
            capabilities {
              add = ["IPC_LOCK"]
            }
          }
        }
        restart_policy = "Always"
      }
    }
  }
}

resource "kubernetes_service" "dse" {
  metadata {
    name = "dse"
    labels = {
      app = "dse"
    }
  }
  spec {
    selector = {
      app = "dse"
    }
    port {
      name = "dse"
      port = 9042
      target_port = "9042"
    }
    port {
      name = "search"
      port = 8983
      target_port = "8983"
    }
    port {
      name = "graph"
      port = 8182
      target_port = "8182"
    }
  }
}

resource "kubernetes_pod" "dse-config" {
  metadata {
    name = "dse-config"
    labels = {
      app = "dse-config"
    }
  }
  spec {
    container {
      name = "dse-config"
      image = "killrvideo/killrvideo-dse-config:3"
    }
    restart_policy = "OnFailure"
  }
}
