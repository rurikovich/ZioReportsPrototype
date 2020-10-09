package ru.infobis.zio.reports

import zio.Has

package object fibers {
  type FiberManager = Has[FiberManager.Service]
}
