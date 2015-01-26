package app.probe

import io.shaka.http.Http._
import io.shaka.http.Request.GET
import io.shaka.http.TrustAllSslCertificates

object HttpClient {
  TrustAllSslCertificates

  def unsafeGet(resource: String, useProxy: Boolean) = {
    val response = http(GET(resource))

    //TODO: support proxy in http-tim
    //if (useProxy) {
    //  val httpViaProxy = http(proxy("my.proxy.server", 8080))
    //  val response = httpViaProxy(GET(resource))
    //}

//    http(GET()) //default to proxy = None
//    http(GET(),proxy = Some(Proxy(url = "foo.bar.com:80"))) //default to auth = None
//    http(GET(),proxy = Some(Proxy(url = "foo.bar.com:80", auth = Some(Auth(username = "x", password = "y")))))

    response.entityAsString
  }
}