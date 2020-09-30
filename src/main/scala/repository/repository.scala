import zio.Has

package object repository {
  type ReportRepository = Has[ReportRepository.Service]
}