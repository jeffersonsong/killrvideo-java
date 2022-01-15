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
      target_port = 9042
    }
    port {
      name = "search"
      port = 8983
      target_port = 8983
    }
    port {
      name = "graph"
      port = 8182
      target_port = 8182
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

resource "kubernetes_deployment" "redis" {
  metadata {
    name = "redis"
    labels = {
      app = "redis"
    }
  }

  spec {
    replicas = 1
    selector {
      match_labels = {
        app = "redis"
      }
    }

    template {
      metadata {
        labels = {
          app = "redis"
        }
      }
      spec {
        container {
          name = "redis"
          image = "redis:latest"
          args = ["redis-server"]
          port {
            container_port = 6379
          }
        }
        restart_policy = "Always"
      }
    }
  }
}

resource "kubernetes_service" "redis" {
  metadata {
    name = "redis"
    labels = {
      app = "redis"
    }
  }
  spec {
    selector = {
      app = "redis"
    }
    port {
      name = "redis"
      port = 6379
      target_port = 6379
    }
  }
}

resource "kubernetes_deployment" "backend" {
  metadata {
    name = "backend"
    labels = {
      app = "backend"
    }
  }
  spec {
    replicas = 1
    selector {
      match_labels = {
        app = "backend"
      }
    }
    template {
      metadata {
        labels = {
          app = "backend"
        }
      }
      spec {
        container {
          name = "backend"
          image = "killrvideo-java-local:latest"
          port {
            container_port = 50101
          }
          env {
            name = "KILLRVIDEO_LOGGING_LEVEL"
            value = "debug"
          }
          env {
            name = "KILLRVIDEO_DSE_CONTACT_POINTS"
            value = "dse"
          }
          env {
            name = "KILLRVIDEO_REDIS_CONTACT_POINTS"
            value = "redis"
          }
          image_pull_policy = "Never"
        }
        restart_policy = "Always"
      }
    }
  }
}

resource "kubernetes_service" "backend" {
  metadata {
    name = "backend"
    labels = {
      app = "backend"
    }
  }
  spec {
    selector = {
      app = "backend"
    }
    port {
      name = "backend"
      port = 50101
      target_port = 50101
    }
  }
}
