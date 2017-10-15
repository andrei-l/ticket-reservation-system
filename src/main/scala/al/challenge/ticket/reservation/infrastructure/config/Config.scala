package al.challenge.ticket.reservation.infrastructure.config

import com.typesafe.config.ConfigFactory

private[config] class Config {
  private final val Conf = ConfigFactory.load()


  def loadString(key: String): String = Conf.getString(key)

  def loadInt(key: String): Int = Conf.getInt(key)
}
