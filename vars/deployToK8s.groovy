def call(Map cfg = [:]) {
  def release   = cfg.release   ?: 'my-app'
  def chart     = cfg.chart     ?: "helm/${release}"
  def namespace = cfg.namespace ?: 'demo'
  def image     = cfg.image     ?: error('deployToK8s: image is required')
  def tag       = cfg.tag       ?: env.BUILD_NUMBER
  def cluster   = cfg.cluster   ?: 'cka'
  def kubeId    = cfg.kubeconfigId ?: 'kubeconfig-kind'

  withCredentials([file(credentialsId: kubeId, variable: 'KUBECONFIG')]) {
    sh """
      kind load docker-image ${image}:${tag} --name ${cluster}
      helm upgrade --install ${release} ${chart} -n ${namespace} --create-namespace \\
        --set image.tag=${tag} --rollback-on-failure --timeout 2m
      kubectl -n ${namespace} rollout status deploy/${release} --timeout=60s
    """
  }
}
