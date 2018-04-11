
// @GENERATOR:play-routes-compiler
// @SOURCE:/home/naza/Desktop/proyectoFinal/core/conf/routes
// @DATE:Wed Apr 11 20:08:33 ART 2018


package router {
  object RoutesPrefix {
    private var _prefix: String = "/"
    def setPrefix(p: String): Unit = {
      _prefix = p
    }
    def prefix: String = _prefix
    val byNamePrefix: Function0[String] = { () => prefix }
  }
}
