package com.schuwalow.todo

import zio._

package object repository {
  type ReportsRepository = Has[ReportsRepository.Service]
}
