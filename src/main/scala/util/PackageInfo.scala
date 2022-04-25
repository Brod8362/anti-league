package pw.byakuren.nolol
package util

import scala.util.control.NonFatal

object PackageInfo {
  private def getPackageInfo(f: Package => String): Option[String] =
    try Option(f(getClass.getPackage)) catch {
      case NonFatal(_) => None
    }

  lazy val VERSION: String = getPackageInfo(_.getImplementationVersion).getOrElse("unknown")

}
