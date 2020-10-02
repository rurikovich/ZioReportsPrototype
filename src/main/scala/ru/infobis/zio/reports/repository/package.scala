package ru.infobis.zio.reports

import zio._

package object repository {
  type ReportsRepository = Has[ReportsRepository.Service]
}
