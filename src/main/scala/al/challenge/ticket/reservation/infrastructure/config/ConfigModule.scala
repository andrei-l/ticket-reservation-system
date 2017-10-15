package al.challenge.ticket.reservation.infrastructure.config
import com.softwaremill.macwire._

trait ConfigModule {
  lazy val config: Config = wire[Config]
}
